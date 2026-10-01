package com.example.data.repository

import android.util.Log
import com.example.data.model.Anime
import com.example.data.model.AnimeCharacter
import com.example.data.model.AnimeStatus
import com.example.data.model.AnimeType
import com.example.data.model.Episode
import com.example.data.model.EpisodeAudio
import com.example.data.model.EpisodeSource
import com.example.data.model.EpisodeSubtitle
import com.example.data.network.KuroApiService
import com.example.data.network.model.AnimeDto
import com.example.data.network.model.EpisodeDto
import com.example.data.network.model.PlaybackSessionResponse
import com.example.data.network.model.StreamAuthorizationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class RetrofitMetadataProvider(
    private val apiService: KuroApiService,
    private val fallbackProvider: LocalLicensedMediaProvider = LocalLicensedMediaProvider()
) : MetadataProvider {

    private val tag = "RetrofitMetadata"

    override suspend fun getTrendingAnime(): List<Anime> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTrendingAnime()
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data.map { it.toDomain() }
            } else {
                fallbackProvider.getTrendingAnime()
            }
        } catch (e: Exception) {
            Log.w(tag, "Remote API unreachable, using local licensed catalog: ${e.message}")
            fallbackProvider.getTrendingAnime()
        }
    }

    override suspend fun getPopularAnime(): List<Anime> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPopularAnime()
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data.map { it.toDomain() }
            } else {
                fallbackProvider.getPopularAnime()
            }
        } catch (e: Exception) {
            Log.w(tag, "Remote API unreachable, using local licensed catalog: ${e.message}")
            fallbackProvider.getPopularAnime()
        }
    }

    override suspend fun getTopRatedAnime(): List<Anime> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTopRatedAnime()
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data.map { it.toDomain() }
            } else {
                fallbackProvider.getTopRatedAnime()
            }
        } catch (e: Exception) {
            fallbackProvider.getTopRatedAnime()
        }
    }

    override suspend fun getSeasonalAnime(): List<Anime> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSeasonalAnime()
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data.map { it.toDomain() }
            } else {
                fallbackProvider.getSeasonalAnime()
            }
        } catch (e: Exception) {
            fallbackProvider.getSeasonalAnime()
        }
    }

    override suspend fun getRecentlyAdded(): List<Anime> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecentlyAdded()
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data.map { it.toDomain() }
            } else {
                fallbackProvider.getRecentlyAdded()
            }
        } catch (e: Exception) {
            fallbackProvider.getRecentlyAdded()
        }
    }

    override suspend fun getAnimeById(id: String): Anime? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAnimeById(id)
            if (response.success && response.data != null) {
                response.data.toDomain()
            } else {
                fallbackProvider.getAnimeById(id)
            }
        } catch (e: Exception) {
            fallbackProvider.getAnimeById(id)
        }
    }

    override suspend fun searchAnime(
        query: String,
        genre: String?,
        year: Int?,
        type: String?,
        status: String?,
        sortBy: String
    ): List<Anime> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchAnime(
                query = query,
                genre = if (genre == "All") null else genre,
                year = year,
                type = if (type == "All") null else type,
                status = if (status == "All") null else status,
                sortBy = sortBy
            )
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data.map { it.toDomain() }
            } else {
                fallbackProvider.searchAnime(query, genre, year, type, status, sortBy)
            }
        } catch (e: Exception) {
            fallbackProvider.searchAnime(query, genre, year, type, status, sortBy)
        }
    }

    override suspend fun getEpisodesForAnime(animeId: String): List<Episode> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEpisodesForAnime(animeId)
            if (response.success && !response.data.isNullOrEmpty()) {
                val fallbackEpisodes = fallbackProvider.getEpisodesForAnime(animeId)
                response.data.map { dto ->
                    val fallbackEp = fallbackEpisodes.find { it.episodeNumber == dto.episodeNumber }
                    dto.toDomain(fallbackEp)
                }
            } else {
                fallbackProvider.getEpisodesForAnime(animeId)
            }
        } catch (e: Exception) {
            fallbackProvider.getEpisodesForAnime(animeId)
        }
    }

    override suspend fun getRecommendations(animeId: String): List<Anime> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecommendations(animeId)
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data.map { it.toDomain() }
            } else {
                fallbackProvider.getRecommendations(animeId)
            }
        } catch (e: Exception) {
            fallbackProvider.getRecommendations(animeId)
        }
    }

    override suspend fun getAllGenres(): List<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllGenres()
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data
            } else {
                fallbackProvider.getAllGenres()
            }
        } catch (e: Exception) {
            fallbackProvider.getAllGenres()
        }
    }

    override suspend fun getAllStudios(): List<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllStudios()
            if (response.success && !response.data.isNullOrEmpty()) {
                response.data
            } else {
                fallbackProvider.getAllStudios()
            }
        } catch (e: Exception) {
            fallbackProvider.getAllStudios()
        }
    }

    /**
     * Authorizes and generates signed HLS streaming URLs via the backend API.
     */
    suspend fun authorizeStream(
        episodeId: String,
        animeId: String,
        preferredQuality: String = "1080p",
        authToken: String? = null
    ): PlaybackSessionResponse = withContext(Dispatchers.IO) {
        try {
            val request = StreamAuthorizationRequest(
                episodeId = episodeId,
                animeId = animeId,
                clientSessionId = UUID.randomUUID().toString(),
                preferredQuality = preferredQuality
            )
            val response = apiService.authorizePlaybackSession(
                authToken = authToken?.let { "Bearer $it" },
                request = request
            )
            if (response.success && response.data != null) {
                return@withContext response.data
            }
        } catch (e: Exception) {
            Log.w(tag, "Authorize stream remote call failed, generating authorized local session: ${e.message}")
        }

        // Fallback: create an authorized playback session using local HLS streams
        val ep = fallbackProvider.getEpisodesForAnime(animeId).find { it.id == episodeId }
        val sources = ep?.sources ?: emptyList()
        val subs = ep?.subtitles ?: emptyList()
        val auds = ep?.audioTracks ?: emptyList()

        PlaybackSessionResponse(
            sessionId = "sess_${UUID.randomUUID()}",
            episodeId = episodeId,
            expiresAt = System.currentTimeMillis() + (1000 * 60 * 60 * 4), // 4 hours valid
            masterPlaylistUrl = sources.find { it.quality.contains("1080p") }?.streamUrl ?: sources.firstOrNull()?.streamUrl ?: "",
            streamQualities = sources.map {
                com.example.data.network.model.QualityStreamDto(
                    quality = it.quality,
                    url = it.streamUrl
                )
            },
            subtitles = subs.map {
                com.example.data.network.model.SubtitleDto(
                    id = it.id,
                    language = it.language,
                    label = it.label,
                    url = it.url,
                    isDefault = it.isDefault
                )
            },
            audioTracks = auds.map {
                com.example.data.network.model.AudioTrackDto(
                    id = it.id,
                    language = it.language,
                    label = it.label,
                    isDefault = it.isDefault
                )
            },
            cdnNode = "Cloudflare Global Edge",
            drmToken = null
        )
    }

    private fun AnimeDto.toDomain(): Anime = Anime(
        id = id,
        slug = slug,
        titleEnglish = titleEnglish,
        titleRomaji = titleRomaji ?: titleEnglish,
        titleJapanese = titleJapanese ?: "",
        description = description,
        posterUrl = posterUrl,
        bannerUrl = bannerUrl ?: posterUrl,
        rating = rating,
        score = score ?: (rating * 20).toInt(),
        type = runCatching { AnimeType.valueOf(type) }.getOrDefault(AnimeType.TV),
        status = runCatching { AnimeStatus.valueOf(status) }.getOrDefault(AnimeStatus.FINISHED),
        episodesCount = episodesCount,
        releaseYear = releaseYear,
        season = season ?: "Winter 2026",
        durationMinutes = durationMinutes,
        studio = studio,
        producers = producers ?: emptyList(),
        genres = genres,
        tags = tags ?: emptyList(),
        trailerUrl = trailerUrl ?: "",
        characters = characters?.map {
            AnimeCharacter(
                name = it.name,
                role = it.role,
                avatarUrl = it.avatarUrl,
                voiceActor = it.voiceActor ?: ""
            )
        } ?: emptyList(),
        isFeatured = isFeatured,
        isTrending = isTrending,
        isPopular = isPopular,
        isSeasonal = isSeasonal,
        nextEpisodeAirDate = nextEpisodeAirDate
    )

    private fun EpisodeDto.toDomain(fallbackEp: Episode? = null): Episode {
        return Episode(
            id = id,
            animeId = animeId,
            episodeNumber = episodeNumber,
            title = title,
            thumbnail = thumbnail,
            durationSeconds = durationSeconds,
            airDate = airDate,
            introStartSec = introStartSec,
            introEndSec = introEndSec,
            outroStartSec = outroStartSec,
            outroEndSec = outroEndSec,
            synopsis = synopsis ?: "",
            sources = fallbackEp?.sources ?: emptyList(),
            subtitles = fallbackEp?.subtitles ?: emptyList(),
            audioTracks = fallbackEp?.audioTracks ?: emptyList()
        )
    }
}
