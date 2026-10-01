package com.example.data.repository

import com.example.data.model.AdminStats
import com.example.data.model.Anime
import com.example.data.model.ApiConfig
import com.example.data.model.AuditLog
import com.example.data.model.Episode
import com.example.data.model.ModeratedUser
import com.example.data.model.UserRole
import com.example.data.model.VideoJob
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdminRepository(
    private val mediaProvider: LocalLicensedMediaProvider
) {
    private val _stats = MutableStateFlow(AdminStats())
    val stats: StateFlow<AdminStats> = _stats.asStateFlow()

    private val _videoJobs = MutableStateFlow(
        listOf(
            VideoJob("job_01", "Solo Leveling", 12, "4K ProRes Master", "Completed", 100, true),
            VideoJob("job_02", "Frieren: Beyond Journey's End", 28, "1080p Master", "Completed", 100, true),
            VideoJob("job_03", "Chainsaw Man: Reze Arc", 1, "4K HDR Master", "Processing", 68, false),
            VideoJob("job_04", "Demon Slayer: Infinity Castle", 1, "8K Raw Master", "Queued", 0, false)
        )
    )
    val videoJobs: StateFlow<List<VideoJob>> = _videoJobs.asStateFlow()

    private val _users = MutableStateFlow(
        listOf(
            ModeratedUser("usr_1", "OtakuKing99", "otaku99@stream.io", UserRole.USER, "Active", 0),
            ModeratedUser("usr_2", "SpamBot_42", "bot42@spammer.net", UserRole.USER, "Banned", 14),
            ModeratedUser("usr_3", "AnimeCriticPro", "critic@review.jp", UserRole.CONTENT_MANAGER, "Active", 1),
            ModeratedUser("usr_4", "NightRaid", "nightraid@guild.gg", UserRole.USER, "Warned", 3),
            ModeratedUser("usr_5", "Mod_Zenith", "zenith@kurostream.app", UserRole.MODERATOR, "Active", 0)
        )
    )
    val users: StateFlow<List<ModeratedUser>> = _users.asStateFlow()

    private val _auditLogs = MutableStateFlow(
        listOf(
            AuditLog("log_1", "Admin", "PUBLISH_EPISODE", "Solo Leveling Ep 12", System.currentTimeMillis() - 1000 * 60 * 30),
            AuditLog("log_2", "Admin", "BAN_USER", "SpamBot_42", System.currentTimeMillis() - 1000 * 60 * 120),
            AuditLog("log_3", "Admin", "TRANSCODE_START", "Chainsaw Man Reze Arc", System.currentTimeMillis() - 1000 * 60 * 240)
        )
    )
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // Configured API endpoints dynamically manageable from the Admin Panel
    private val _apiConfigs = MutableStateFlow(
        listOf(
            ApiConfig(
                id = "api_main",
                name = "KuroStream Global REST API (Primary)",
                baseUrl = "https://api.kurostream.app/",
                category = "Catalog & Auth",
                isActive = true,
                status = "Online",
                latencyMs = 38L,
                lastTested = "1m ago"
            ),
            ApiConfig(
                id = "api_cdn",
                name = "Cloudflare Edge Video HLS CDN",
                baseUrl = "https://stream-cdn.kurostream.app/",
                category = "Streaming HLS",
                isActive = true,
                status = "Online",
                latencyMs = 24L,
                lastTested = "Just now"
            ),
            ApiConfig(
                id = "api_backup",
                name = "Tokyo Failover Mirror Node",
                baseUrl = "https://tokyo-node.kurostream.app/",
                category = "Backup Mirror",
                isActive = false,
                status = "Online",
                latencyMs = 92L,
                lastTested = "5m ago"
            ),
            ApiConfig(
                id = "api_anilist",
                name = "AniList GraphQL Metadata Sync",
                baseUrl = "https://graphql.anilist.co/",
                category = "Metadata Sync",
                isActive = false,
                status = "Online",
                latencyMs = 120L,
                lastTested = "10m ago"
            )
        )
    )
    val apiConfigs: StateFlow<List<ApiConfig>> = _apiConfigs.asStateFlow()

    fun addApiConfig(name: String, baseUrl: String, category: String, apiKey: String?) {
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val newApi = ApiConfig(
            id = "api_${System.currentTimeMillis()}",
            name = name.trim(),
            baseUrl = formattedUrl.trim(),
            category = category,
            apiKey = apiKey?.takeIf { it.isNotBlank() },
            isActive = false,
            status = "Online",
            latencyMs = (30..85).random().toLong(),
            lastTested = "Just added"
        )
        _apiConfigs.update { it + newApi }
        logAction("ADD_API_ENDPOINT", "$name ($formattedUrl)")
    }

    fun setActiveApi(apiId: String) {
        _apiConfigs.update { list ->
            list.map {
                val shouldBeActive = (it.id == apiId)
                if (shouldBeActive) {
                    RetrofitClient.setActiveBaseUrl(it.baseUrl)
                }
                it.copy(isActive = shouldBeActive)
            }
        }
        val target = _apiConfigs.value.firstOrNull { it.id == apiId }
        logAction("SWITCH_ACTIVE_API", target?.name ?: apiId)
    }

    fun testApiConnection(apiId: String) {
        val simulatedPing = (25..75).random().toLong()
        _apiConfigs.update { list ->
            list.map {
                if (it.id == apiId) {
                    it.copy(
                        status = "Online",
                        latencyMs = simulatedPing,
                        lastTested = "Just now"
                    )
                } else it
            }
        }
    }

    fun deleteApiConfig(apiId: String) {
        val target = _apiConfigs.value.firstOrNull { it.id == apiId }
        _apiConfigs.update { list -> list.filterNot { it.id == apiId } }
        logAction("DELETE_API_ENDPOINT", target?.name ?: apiId)
    }

    fun addAnime(anime: Anime) {
        mediaProvider.addAnime(anime)
        _stats.update { it.copy(totalAnime = it.totalAnime + 1) }
        logAction("CREATE_ANIME", anime.titleEnglish)
    }

    fun updateAnime(anime: Anime) {
        mediaProvider.updateAnime(anime)
        logAction("UPDATE_ANIME", anime.titleEnglish)
    }

    fun deleteAnime(animeId: String, title: String) {
        mediaProvider.deleteAnime(animeId)
        _stats.update { it.copy(totalAnime = (it.totalAnime - 1).coerceAtLeast(0)) }
        logAction("DELETE_ANIME", title)
    }

    fun triggerTranscodeJob(animeTitle: String, episodeNumber: Int, resolution: String) {
        val newJob = VideoJob(
            id = "job_${System.currentTimeMillis()}",
            animeTitle = animeTitle,
            episodeNumber = episodeNumber,
            sourceResolution = resolution,
            status = "Processing",
            progressPercent = 15,
            hlsStreamReady = false
        )
        _videoJobs.update { listOf(newJob) + it }
        logAction("START_TRANSCODE", "$animeTitle Ep $episodeNumber")
    }

    fun updateModerationStatus(userId: String, newStatus: String) {
        _users.update { list ->
            list.map { if (it.id == userId) it.copy(status = newStatus) else it }
        }
        logAction("USER_MODERATION", "$userId set to $newStatus")
    }

    private fun logAction(action: String, target: String) {
        val entry = AuditLog(
            id = "log_${System.currentTimeMillis()}",
            adminName = "SuperAdmin",
            action = action,
            target = target
        )
        _auditLogs.update { listOf(entry) + it }
    }
}
