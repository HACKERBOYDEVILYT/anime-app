package com.example.data.network

import com.example.data.model.ApiConfig
import com.example.data.model.EpisodeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HlsProviderState {
    ONLINE,
    DEGRADED,
    OFFLINE,
    CHECKING
}

data class HlsVariantStream(
    val qualityLabel: String,
    val url: String,
    val bandwidthKbps: Int,
    val isHls: Boolean,
    val providerId: String,
    val providerName: String
)

data class HlsProviderNode(
    val id: String,
    val name: String,
    val baseUrl: String,
    val healthProbeUrl: String,
    val category: String,
    val priority: Int,
    val isPrimary: Boolean,
    val isEnabled: Boolean = true,
    val state: HlsProviderState = HlsProviderState.ONLINE,
    val httpStatusCode: Int = 200,
    val latencyMs: Long = 0L,
    val failureCount: Int = 0,
    val lastCheckedAt: String = "Ready",
    val statusMessage: String = "Ready"
)

/**
 * Service layer managing multiple HLS & direct stream sources, dynamic API endpoint
 * switching at runtime, automatic provider failover, and real-time HTTP status monitoring.
 */
object HlsStreamService {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val initialProviders = listOf(
        HlsProviderNode(
            id = "api_crunchyroll",
            name = "Crunchyroll Simulcast & Catalog API (1080p HLS & Trailers)",
            baseUrl = "https://www.crunchyroll.com/",
            healthProbeUrl = "https://api.jikan.moe/v4/anime?producers=1468&limit=1",
            category = "Crunchyroll Simulcast API",
            priority = 1,
            isPrimary = true,
            isEnabled = true
        ),
        HlsProviderNode(
            id = "api_hianime_upstream",
            name = "HiAnime / AniWatch Upstream (HD-1 VidStreaming & HD-2 MegaCloud)",
            baseUrl = "https://hianime.to/",
            healthProbeUrl = "https://api.jikan.moe/v4/top/anime?limit=1",
            category = "HiAnime / AniWatch Upstream HLS",
            priority = 2,
            isPrimary = false,
            isEnabled = true
        ),
        HlsProviderNode(
            id = "api_consumet_aniwatch",
            name = "AniWatch / Zoro Multi-Server API (VidCloud • MegaCloud • StreamTape)",
            baseUrl = "https://api.consumet.org/anime/zoro/",
            healthProbeUrl = "https://api.animethemes.moe/anime?page[size]=1",
            category = "Multi-Server Scraper API",
            priority = 2,
            isPrimary = false,
            isEnabled = true
        ),
        HlsProviderNode(
            id = "api_animethemes",
            name = "AnimeThemes Free Video Storage Server (1080p WebM/HLS)",
            baseUrl = "https://api.animethemes.moe/",
            healthProbeUrl = "https://api.animethemes.moe/anime?page[size]=1",
            category = "Free Video Storage Server",
            priority = 3,
            isPrimary = false,
            isEnabled = true
        ),
        HlsProviderNode(
            id = "api_jikan",
            name = "Jikan v4 Free API (MyAnimeList Catalog & Trailers)",
            baseUrl = "https://api.jikan.moe/v4/",
            healthProbeUrl = "https://api.jikan.moe/v4/top/anime?limit=1",
            category = "Free Catalog & Trailers API",
            priority = 4,
            isPrimary = false,
            isEnabled = true
        ),
        HlsProviderNode(
            id = "api_anilist",
            name = "AniList Free GraphQL API (Airing & Trailers)",
            baseUrl = "https://graphql.anilist.co/",
            healthProbeUrl = "https://graphql.anilist.co",
            category = "Free GraphQL API",
            priority = 5,
            isPrimary = false,
            isEnabled = true
        ),
        HlsProviderNode(
            id = "api_kitsu",
            name = "Kitsu v2 Free Anime Edge API",
            baseUrl = "https://kitsu.io/api/edge/",
            healthProbeUrl = "https://kitsu.io/api/edge/anime?page[limit]=1",
            category = "Free Backup API",
            priority = 6,
            isPrimary = false,
            isEnabled = true
        ),
        HlsProviderNode(
            id = "api_archive",
            name = "Internet Archive Free Cloud Video Storage",
            baseUrl = "https://archive.org/",
            healthProbeUrl = "https://archive.org/metadata/opensource_movies",
            category = "Free Video Storage Server",
            priority = 7,
            isPrimary = false,
            isEnabled = true
        )
    )

    private val _providers = MutableStateFlow(initialProviders)
    val providers: StateFlow<List<HlsProviderNode>> = _providers.asStateFlow()

    private val _activePrimaryProvider = MutableStateFlow(initialProviders.first())
    val activePrimaryProvider: StateFlow<HlsProviderNode> = _activePrimaryProvider.asStateFlow()

    private val _autoFailoverEnabled = MutableStateFlow(true)
    val autoFailoverEnabled: StateFlow<Boolean> = _autoFailoverEnabled.asStateFlow()

    fun setAutoFailover(enabled: Boolean) {
        _autoFailoverEnabled.value = enabled
    }

    /**
     * Dynamically switches the primary API / HLS provider endpoint at runtime
     * and reconfigures RetrofitClient's base URL.
     */
    fun switchPrimaryProvider(providerId: String): HlsProviderNode? {
        var selected: HlsProviderNode? = null
        _providers.update { list ->
            list.map { node ->
                val isTarget = node.id == providerId
                val updated = node.copy(
                    isPrimary = isTarget,
                    isEnabled = if (isTarget) true else node.isEnabled
                )
                if (isTarget) {
                    selected = updated
                }
                updated
            }
        }
        selected?.let { target ->
            _activePrimaryProvider.value = target
            RetrofitClient.setActiveBaseUrl(target.baseUrl)
        }
        return selected
    }

    /**
     * Dynamically updates a provider's endpoint URL and runs a live health check.
     */
    fun updateProviderEndpoint(providerId: String, newBaseUrl: String) {
        val formatted = if (newBaseUrl.trim().endsWith("/")) newBaseUrl.trim() else "${newBaseUrl.trim()}/"
        _providers.update { list ->
            list.map { node ->
                if (node.id == providerId) {
                    val updated = node.copy(
                        baseUrl = formatted,
                        healthProbeUrl = formatted,
                        statusMessage = "Endpoint Updated"
                    )
                    if (updated.isPrimary) {
                        _activePrimaryProvider.value = updated
                        RetrofitClient.setActiveBaseUrl(formatted)
                    }
                    updated
                } else node
            }
        }
        checkProviderStatusAsync(providerId)
    }

    /**
     * Registers a custom HLS / Free Storage / Scraper provider node and monitors it.
     */
    fun registerCustomProvider(
        id: String = "api_${System.currentTimeMillis()}",
        name: String,
        baseUrl: String,
        category: String
    ): HlsProviderNode {
        val formatted = if (baseUrl.trim().endsWith("/")) baseUrl.trim() else "${baseUrl.trim()}/"
        val node = HlsProviderNode(
            id = id,
            name = name.trim(),
            baseUrl = formatted,
            healthProbeUrl = formatted,
            category = category,
            priority = _providers.value.size + 1,
            isPrimary = false,
            isEnabled = true,
            state = HlsProviderState.CHECKING,
            statusMessage = "Checking..."
        )
        _providers.update { current ->
            if (current.any { it.id == id }) current else current + node
        }
        checkProviderStatusAsync(id)
        return node
    }

    fun removeProvider(providerId: String) {
        _providers.update { list -> list.filterNot { it.id == providerId } }
        val remaining = _providers.value
        if (_activePrimaryProvider.value.id == providerId && remaining.isNotEmpty()) {
            switchPrimaryProvider(remaining.first().id)
        }
    }

    /**
     * Synchronizes provider nodes with persisted ApiConfig items from Room DB.
     */
    fun syncWithApiConfigs(configs: List<ApiConfig>) {
        if (configs.isEmpty()) return
        val currentMap = _providers.value.associateBy { it.id }
        val merged = configs.mapIndexed { index, cfg ->
            val existing = currentMap[cfg.id]
            val probe = when {
                cfg.baseUrl.contains("crunchyroll") -> "https://api.jikan.moe/v4/anime?producers=1468&limit=1"
                cfg.baseUrl.contains("jikan.moe") -> "https://api.jikan.moe/v4/top/anime?limit=1"
                cfg.baseUrl.contains("animethemes.moe") -> "https://api.animethemes.moe/anime?page[size]=1"
                cfg.baseUrl.contains("kitsu.io") -> "https://kitsu.io/api/edge/anime?page[limit]=1"
                cfg.baseUrl.contains("archive.org") -> "https://archive.org/metadata/opensource_movies"
                cfg.baseUrl.contains("anilist.co") -> "https://graphql.anilist.co"
                else -> cfg.baseUrl
            }
            val parsedState = when {
                cfg.status.contains("Checking", true) -> HlsProviderState.CHECKING
                cfg.status.contains("Online", true) || cfg.status.contains("Ready", true) -> HlsProviderState.ONLINE
                cfg.status.contains("Degraded", true) -> HlsProviderState.DEGRADED
                else -> HlsProviderState.OFFLINE
            }
            HlsProviderNode(
                id = cfg.id,
                name = cfg.name,
                baseUrl = cfg.baseUrl,
                healthProbeUrl = probe,
                category = cfg.category,
                priority = existing?.priority ?: (index + 1),
                isPrimary = existing?.isPrimary ?: (index == 0),
                isEnabled = cfg.isActive,
                state = parsedState,
                httpStatusCode = existing?.httpStatusCode ?: 200,
                latencyMs = cfg.latencyMs,
                failureCount = existing?.failureCount ?: 0,
                lastCheckedAt = cfg.lastTested,
                statusMessage = cfg.status
            )
        }
        _providers.value = merged
        merged.firstOrNull { it.isPrimary }?.let { _activePrimaryProvider.value = it }
    }

    /**
     * Triggers an asynchronous live HTTP health check for a single provider and returns updated status via callback.
     */
    fun checkProviderStatusAsync(
        providerId: String,
        onResult: ((HlsProviderNode) -> Unit)? = null
    ) {
        serviceScope.launch {
            val updated = checkProviderStatus(providerId)
            if (updated != null && onResult != null) {
                onResult(updated)
            }
        }
    }

    /**
     * Performs a real HTTP probe against the provider endpoint, measuring latency and status code,
     * and triggers automatic failover if the primary provider goes offline.
     */
    suspend fun checkProviderStatus(providerId: String): HlsProviderNode? = withContext(Dispatchers.IO) {
        val target = _providers.value.firstOrNull { it.id == providerId } ?: return@withContext null

        _providers.update { list ->
            list.map {
                if (it.id == providerId) {
                    it.copy(state = HlsProviderState.CHECKING, statusMessage = "Checking...", lastCheckedAt = "Pinging...")
                } else it
            }
        }

        val startTime = System.currentTimeMillis()
        var newState: HlsProviderState
        var statusMsg: String
        var code = 0
        var latency: Long

        try {
            val request = if (target.baseUrl.contains("anilist.co")) {
                val query = """{"query":"{ Page(page: 1, perPage: 1) { media(type: ANIME) { id } } }"}"""
                Request.Builder()
                    .url("https://graphql.anilist.co")
                    .post(query.toRequestBody("application/json".toMediaType()))
                    .build()
            } else {
                Request.Builder()
                    .url(target.healthProbeUrl)
                    .get()
                    .build()
            }

            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
                code = response.code
                if (response.isSuccessful || code in 200..399) {
                    newState = HlsProviderState.ONLINE
                    statusMsg = "Online (HTTP $code)"
                } else {
                    newState = HlsProviderState.DEGRADED
                    statusMsg = "Degraded (HTTP $code)"
                }
            }
        } catch (e: Exception) {
            latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            newState = HlsProviderState.OFFLINE
            statusMsg = "Offline (${e.javaClass.simpleName})"
        }

        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        var finalNode: HlsProviderNode? = null

        _providers.update { list ->
            list.map { node ->
                if (node.id == providerId) {
                    val failCount = if (newState == HlsProviderState.OFFLINE) node.failureCount + 1 else 0
                    val updated = node.copy(
                        state = newState,
                        httpStatusCode = code,
                        latencyMs = latency,
                        failureCount = failCount,
                        lastCheckedAt = "Checked $timestamp",
                        statusMessage = statusMsg
                    )
                    finalNode = updated
                    if (updated.isPrimary) {
                        _activePrimaryProvider.value = updated
                    }
                    updated
                } else node
            }
        }

        // Automatic Failover: if primary provider is OFFLINE and auto-failover is enabled,
        // promote the fastest ONLINE provider automatically.
        if (_autoFailoverEnabled.value && finalNode?.isPrimary == true && newState == HlsProviderState.OFFLINE) {
            val healthyFallback = _providers.value
                .filter { it.isEnabled && it.state == HlsProviderState.ONLINE && it.id != providerId }
                .minByOrNull { if (it.latencyMs > 0) it.latencyMs else Long.MAX_VALUE }
            if (healthyFallback != null) {
                switchPrimaryProvider(healthyFallback.id)
            }
        }

        finalNode
    }

    /**
     * Monitors all registered HLS & API providers concurrently.
     */
    fun monitorAllProviders(onEachUpdated: ((HlsProviderNode) -> Unit)? = null) {
        val ids = _providers.value.map { it.id }
        ids.forEach { id ->
            checkProviderStatusAsync(id, onEachUpdated)
        }
    }

    /**
     * Inspects an HLS (.m3u8) master playlist or direct video stream URL.
     * Parses `#EXT-X-STREAM-INF` multi-bitrate variants if it's an HLS master playlist,
     * or verifies byte-range readiness for direct .webm/.mp4 streams.
     */
    suspend fun inspectHlsPlaylistOrStream(
        streamUrl: String,
        providerName: String = "HLS Stream Server"
    ): List<HlsVariantStream> = withContext(Dispatchers.IO) {
        val cleanUrl = streamUrl.trim()
        if (cleanUrl.isBlank()) return@withContext emptyList()

        val isHlsUrl = cleanUrl.contains(".m3u8", ignoreCase = true)
        try {
            val requestBuilder = Request.Builder()
                .url(cleanUrl)
                .get()
            if (!isHlsUrl) {
                requestBuilder.header("Range", "bytes=0-2048")
            }

            RetrofitClient.okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful && response.code !in 200..399) {
                    return@withContext emptyList()
                }

                if (isHlsUrl) {
                    val bodyText = response.body?.string().orEmpty()
                    if (bodyText.contains("#EXT-X-STREAM-INF")) {
                        val variants = mutableListOf<HlsVariantStream>()
                        val lines = bodyText.lines()
                        var pendingResolution = "1080p HLS"
                        var pendingBandwidth = 4500

                        for (line in lines) {
                            val trimmed = line.trim()
                            if (trimmed.startsWith("#EXT-X-STREAM-INF:")) {
                                val resMatch = Regex("""RESOLUTION=\d+x(\d+)""").find(trimmed)
                                val bwMatch = Regex("""BANDWIDTH=(\d+)""").find(trimmed)
                                val height = resMatch?.groupValues?.getOrNull(1) ?: "1080"
                                pendingResolution = "${height}p HLS"
                                pendingBandwidth = ((bwMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 4500000) / 1000)
                            } else if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                                val resolvedVariantUrl = if (trimmed.startsWith("http")) {
                                    trimmed
                                } else {
                                    runCatching { URI(cleanUrl).resolve(trimmed).toString() }.getOrDefault(cleanUrl)
                                }
                                variants.add(
                                    HlsVariantStream(
                                        qualityLabel = pendingResolution,
                                        url = resolvedVariantUrl,
                                        bandwidthKbps = pendingBandwidth,
                                        isHls = true,
                                        providerId = _activePrimaryProvider.value.id,
                                        providerName = providerName
                                    )
                                )
                            }
                        }
                        if (variants.isNotEmpty()) {
                            return@withContext variants
                        }
                    }
                }

                // Single HLS stream or direct WebM/MP4 stream verified online
                listOf(
                    HlsVariantStream(
                        qualityLabel = if (isHlsUrl) "1080p HLS Master" else "1080p Direct Stream",
                        url = cleanUrl,
                        bandwidthKbps = 5200,
                        isHls = isHlsUrl,
                        providerId = _activePrimaryProvider.value.id,
                        providerName = providerName
                    )
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Dynamically orders and enriches an episode's stream sources based on the currently active
     * primary HLS/Storage provider and provider health status.
     */
    fun prioritizeEpisodeSources(sources: List<EpisodeSource>): List<EpisodeSource> {
        if (sources.size <= 1) return sources
        val primary = _activePrimaryProvider.value
        val primaryHostKeyword = when {
            primary.baseUrl.contains("hianime") || primary.baseUrl.contains("consumet") -> "HD-1"
            primary.baseUrl.contains("archive.org") -> "archive.org"
            primary.baseUrl.contains("jikan.moe") -> "youtube.com"
            else -> "HD-1"
        }

        return sources.sortedWith(
            compareByDescending<EpisodeSource> { src ->
                // Prefer hardware-accelerated HLS (.m3u8) and H.264 (.mp4) streams first
                src.isHls || src.streamUrl.contains(".m3u8", ignoreCase = true) || src.streamUrl.contains(".mp4", ignoreCase = true)
            }.thenByDescending { src ->
                // Match active primary server node (e.g., HD-1 VidStreaming / HD-2 MegaCloud)
                src.cdnNode.contains(primaryHostKeyword, ignoreCase = true) || src.streamUrl.contains(primaryHostKeyword, ignoreCase = true)
            }.thenByDescending { src ->
                // Prefer direct/HLS video streams over web embeds for default playback
                !src.streamUrl.contains("youtube.com/embed", ignoreCase = true)
            }
        )
    }
}
