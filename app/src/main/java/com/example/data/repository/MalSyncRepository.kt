package com.example.data.repository

import com.example.data.local.dao.MalSyncDao
import com.example.data.local.entity.MalSyncEntity
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder

class MalSyncRepository(
    private val malSyncDao: MalSyncDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Automatically purge any legacy fake demo entries (e.g., 142 anime / 1890 episodes)
        scope.launch {
            val existing = malSyncDao.getAllConfigs().firstOrNull().orEmpty()
            existing.forEach { cfg ->
                if (cfg.totalAnimeTracked == 142 && cfg.totalEpisodesWatched == 1890) {
                    malSyncDao.disconnectService(cfg.serviceName)
                }
            }
        }
    }

    fun getConfig(service: String): Flow<MalSyncEntity?> = malSyncDao.getSyncConfig(service)
    fun getAllConfigs(): Flow<List<MalSyncEntity>> = malSyncDao.getAllConfigs()

    /**
     * Connects to a REAL MyAnimeList (via Jikan v4 API) or AniList (via AniList GraphQL API) account.
     * No fake demo numbers are ever generated.
     */
    suspend fun connectService(service: String, username: String): Result<MalSyncEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim()
        if (cleanUsername.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid username."))
        }

        return@withContext when (service.uppercase()) {
            "MAL" -> fetchRealMyAnimeListProfile(cleanUsername)
            "ANILIST" -> fetchRealAniListProfile(cleanUsername)
            else -> Result.failure(IllegalArgumentException("Unsupported tracking service: $service"))
        }
    }

    /**
     * Re-fetches real live statistics from MyAnimeList (Jikan v4) or AniList GraphQL API.
     */
    suspend fun syncNow(service: String, current: MalSyncEntity): Result<MalSyncEntity> = withContext(Dispatchers.IO) {
        connectService(service, current.username)
    }

    private suspend fun fetchRealMyAnimeListProfile(username: String): Result<MalSyncEntity> {
        return try {
            val encoded = URLEncoder.encode(username, "UTF-8")
            val request = Request.Builder()
                .url("https://api.jikan.moe/v4/users/$encoded/full")
                .header("Accept", "application/json")
                .get()
                .build()

            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(
                        IllegalStateException("MyAnimeList user '@$username' not found (HTTP ${response.code}). Enter a real MAL username.")
                    )
                }
                val body = response.body?.string() ?: return Result.failure(
                    IllegalStateException("Empty response from Jikan MyAnimeList API.")
                )
                val root = JSONObject(body)
                val data = root.optJSONObject("data") ?: return Result.failure(
                    IllegalStateException("MyAnimeList profile data not found for '@$username'.")
                )
                val canonicalUsername = data.optString("username", username)
                val animeStats = data.optJSONObject("statistics")?.optJSONObject("anime")

                val totalAnime = animeStats?.optInt("total_entries", 0) ?: 0
                val episodesWatched = animeStats?.optInt("episodes_watched", 0) ?: 0
                val meanScore = animeStats?.optDouble("mean_score", 0.0)?.toFloat() ?: 0f

                val entity = MalSyncEntity(
                    serviceName = "MAL",
                    username = canonicalUsername,
                    isConnected = true,
                    totalAnimeTracked = totalAnime,
                    totalEpisodesWatched = episodesWatched,
                    meanScore = meanScore,
                    autoSyncEnabled = true,
                    lastSyncedTimestamp = System.currentTimeMillis()
                )
                malSyncDao.saveConfig(entity)
                Result.success(entity)
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Failed to verify MyAnimeList account: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    private suspend fun fetchRealAniListProfile(username: String): Result<MalSyncEntity> {
        return try {
            val graphqlPayload = JSONObject().apply {
                put(
                    "query",
                    "query (\$name: String) { User(name: \$name) { name statistics { anime { count episodesWatched meanScore } } } }"
                )
                put("variables", JSONObject().apply { put("name", username) })
            }.toString()

            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(graphqlPayload.toRequestBody("application/json".toMediaType()))
                .build()

            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful || body.isBlank()) {
                    return Result.failure(
                        IllegalStateException("AniList user '@$username' not found (HTTP ${response.code}). Enter a real AniList username.")
                    )
                }
                val root = JSONObject(body)
                val userObj = root.optJSONObject("data")?.optJSONObject("User")
                    ?: return Result.failure(
                        IllegalStateException("AniList user '@$username' does not exist.")
                    )

                val canonicalName = userObj.optString("name", username)
                val animeStats = userObj.optJSONObject("statistics")?.optJSONObject("anime")
                val totalAnime = animeStats?.optInt("count", 0) ?: 0
                val episodesWatched = animeStats?.optInt("episodesWatched", 0) ?: 0
                val rawMean = animeStats?.optDouble("meanScore", 0.0)?.toFloat() ?: 0f
                val normalizedMean = if (rawMean > 10f) rawMean / 10f else rawMean

                val entity = MalSyncEntity(
                    serviceName = "ANILIST",
                    username = canonicalName,
                    isConnected = true,
                    totalAnimeTracked = totalAnime,
                    totalEpisodesWatched = episodesWatched,
                    meanScore = normalizedMean,
                    autoSyncEnabled = true,
                    lastSyncedTimestamp = System.currentTimeMillis()
                )
                malSyncDao.saveConfig(entity)
                Result.success(entity)
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Failed to verify AniList account: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    suspend fun disconnectService(service: String) = withContext(Dispatchers.IO) {
        malSyncDao.disconnectService(service)
    }

    suspend fun updateAutoSync(service: String, enabled: Boolean, current: MalSyncEntity) = withContext(Dispatchers.IO) {
        malSyncDao.saveConfig(current.copy(autoSyncEnabled = enabled))
    }

    suspend fun recordEpisodeWatched(animeTitle: String, episodeNumber: Int) = withContext(Dispatchers.IO) {
        val configs = malSyncDao.getAllConfigs().firstOrNull().orEmpty()
        configs.filter { it.isConnected && it.autoSyncEnabled }.forEach { cfg ->
            malSyncDao.saveConfig(
                cfg.copy(
                    totalEpisodesWatched = cfg.totalEpisodesWatched + 1,
                    lastSyncedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
