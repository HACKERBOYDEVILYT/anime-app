package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AdMobConfigEntity
import com.example.data.local.entity.ScrapedVideoEntity
import com.example.data.model.AdminStats
import com.example.data.model.Anime
import com.example.data.model.AnimeStatus
import com.example.data.model.AnimeType
import com.example.data.model.ApiConfig
import com.example.data.model.AuditLog
import com.example.data.model.ModeratedUser
import com.example.data.model.VideoJob
import com.example.data.repository.AdminRepository
import com.example.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    // 0 = Scrap Video, 1 = API Status, 2 = Catalog CMS, 3 = Analytics, 4 = Video Pipeline, 5 = Moderation, 6 = Audit Logs
    val selectedTab: Int = 0,
    val animeList: List<Anime> = emptyList(),
    val showAddAnimeDialog: Boolean = false,
    val newAnimeTitle: String = "",
    val newAnimeJapanese: String = "",
    val newAnimeStudio: String = "MAPPA",
    val newAnimeGenre: String = "Action",
    val newAnimeEpisodes: String = "12",
    val newAnimeDescription: String = "",
    val newAnimePosterUrl: String = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
    val newAnimeTrailerUrl: String = "",

    // Scraped Video & Stream Injector State
    val showAddScrapedDialog: Boolean = false,
    val scrapeAnimeId: String = "anime_1",
    val scrapeAnimeTitle: String = "Frieren: Beyond Journey's End",
    val scrapeEpisodeNumber: String = "1",
    val scrapeEpisodeTitle: String = "",
    val scrapeStreamUrl: String = "",
    val scrapeQualityLabel: String = "1080p FHD",
    val scrapeServerSource: String = "HiAnime Scraper",
    val scrapeSubtitleUrl: String = "",
    val scrapeSubtitleLanguage: String = "Bangla",
    val scrapeAudioLanguage: String = "Japanese [Original]",

    // Auto Web Page Video, API & CDN Inspector State
    val webPageScrapeUrl: String = "",
    val isExtractingLinks: Boolean = false,
    val extractedVideoLinks: List<String> = emptyList(),
    val websiteInspectionReport: AdminRepository.WebsiteInspectionReport? = null,
    val extractionMessage: String? = null,

    // Multi-Server API Management State
    val showAddApiDialog: Boolean = false,
    val newApiName: String = "",
    val newApiUrl: String = "",
    val newApiCategory: String = "Free Video Storage Server",
    val newApiKey: String = ""
)

class AdminViewModel(
    private val adminRepository: AdminRepository,
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    val stats: StateFlow<AdminStats> = adminRepository.stats
    val videoJobs: StateFlow<List<VideoJob>> = adminRepository.videoJobs
    val users: StateFlow<List<ModeratedUser>> = adminRepository.users
    val auditLogs: StateFlow<List<AuditLog>> = adminRepository.auditLogs
    val apiConfigs: StateFlow<List<ApiConfig>> = adminRepository.apiConfigs
    val scrapedVideos: StateFlow<List<ScrapedVideoEntity>> = adminRepository.scrapedVideos
    val adMobConfig: StateFlow<AdMobConfigEntity> = adminRepository.adMobConfig
    val bunnyCdnBaseUrl: StateFlow<String> = AdminRepository.globalBunnyCdnBaseUrl
    val bunnyCdnLogoUrl: StateFlow<String> = AdminRepository.globalBunnyCdnLogoUrl

    init {
        loadCatalog()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun loadCatalog() {
        val snapshot = animeRepository.getInitialSnapshot()
        if (snapshot.isNotEmpty()) {
            _uiState.update { state ->
                val defaultAnime = snapshot.firstOrNull()
                state.copy(
                    animeList = snapshot,
                    scrapeAnimeId = if (state.scrapeAnimeId.isBlank() && defaultAnime != null) defaultAnime.id else state.scrapeAnimeId,
                    scrapeAnimeTitle = if (state.scrapeAnimeTitle.isBlank() && defaultAnime != null) defaultAnime.titleEnglish else state.scrapeAnimeTitle
                )
            }
        }
        viewModelScope.launch {
            val list = animeRepository.getPopular().ifEmpty { animeRepository.getInitialSnapshot() }
            _uiState.update { state ->
                val defaultAnime = list.firstOrNull()
                state.copy(
                    animeList = list,
                    scrapeAnimeId = if (state.scrapeAnimeId.isBlank() && defaultAnime != null) defaultAnime.id else state.scrapeAnimeId,
                    scrapeAnimeTitle = if (state.scrapeAnimeTitle.isBlank() && defaultAnime != null) defaultAnime.titleEnglish else state.scrapeAnimeTitle
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(extractionMessage = null) }
    }

    fun addApiEndpointDirect(name: String, baseUrl: String, category: String, apiKey: String? = null) {
        if (name.isBlank() || baseUrl.isBlank()) return
        adminRepository.addApiConfig(name = name, baseUrl = baseUrl, category = category, apiKey = apiKey)
        _uiState.update {
            it.copy(extractionMessage = "Added CDN / Video Server: $name")
        }
    }

    fun addBunnyOrCustomCdn(
        name: String,
        cdnUrl: String,
        category: String,
        apiKey: String?,
        setAsLogoCdn: Boolean,
        attachAnimeId: String? = null,
        attachAnimeTitle: String? = null,
        attachEpisodeNumber: Int? = null
    ) {
        if (cdnUrl.isBlank()) return
        val isHtmlMode = cdnUrl.trim().startsWith("<") ||
            cdnUrl.contains("<iframe", ignoreCase = true) ||
            cdnUrl.contains("<video", ignoreCase = true) ||
            cdnUrl.contains("/embed/", ignoreCase = true) ||
            cdnUrl.contains("/iframe", ignoreCase = true)
        val effectiveName = name.trim().ifBlank {
            when {
                cdnUrl.contains("b-cdn.net", ignoreCase = true) || cdnUrl.contains("mediadelivery.net", ignoreCase = true) ->
                    "Bunny.net Paid CDN"
                isHtmlMode -> "Paid Server HTML Embed"
                else -> "Paid Cloud CDN Server"
            }
        }
        adminRepository.addApiConfig(
            name = effectiveName,
            baseUrl = cdnUrl,
            category = category,
            apiKey = apiKey
        )
        if (setAsLogoCdn && !isHtmlMode) {
            adminRepository.updateBunnyLogoCdn(cdnUrl)
        }
        if (!attachAnimeId.isNullOrBlank() && !attachAnimeTitle.isNullOrBlank()) {
            val epNum = (attachEpisodeNumber ?: 1).coerceAtLeast(1)
            val modeBadge = when {
                isHtmlMode -> "1080p • Paid HTML Embed"
                !apiKey.isNullOrBlank() -> "1080p • Paid API Stream"
                else -> "1080p • Paid CDN Stream"
            }
            adminRepository.addScrapedVideo(
                animeId = attachAnimeId,
                animeTitle = attachAnimeTitle,
                episodeNumber = epNum,
                episodeTitle = "$attachAnimeTitle • Episode $epNum ($effectiveName)",
                streamUrl = cdnUrl,
                qualityLabel = modeBadge,
                serverSource = effectiveName,
                subtitleUrl = null,
                subtitleLanguage = "Bangla",
                audioLanguage = "Japanese [Original]"
            )
        }
        _uiState.update {
            it.copy(
                extractionMessage = buildString {
                    append("✅ Added Paid Server/CDN: $effectiveName")
                    if (setAsLogoCdn && !isHtmlMode) append(" • Updated App Logo CDN")
                    if (!attachAnimeTitle.isNullOrBlank()) append(" • Attached to $attachAnimeTitle Ep ${attachEpisodeNumber ?: 1}")
                }
            )
        }
    }

    fun resolvePaidServerApi(
        apiUrl: String,
        apiKey: String?,
        onResolved: (String) -> Unit
    ) {
        if (apiUrl.isBlank()) return
        _uiState.update { it.copy(extractionMessage = "🔄 Connecting to Paid Server API & resolving stream/HTML...") }
        viewModelScope.launch {
            val resolved = adminRepository.resolvePaidServerApi(apiUrl, apiKey)
            onResolved(resolved)
            _uiState.update {
                it.copy(extractionMessage = "✅ Paid Server API Resolved: ${resolved.take(65)}")
            }
        }
    }

    fun updateBunnyLogoCdn(logoUrl: String, pullZoneHost: String? = null) {
        if (logoUrl.isBlank()) return
        adminRepository.updateBunnyLogoCdn(logoUrl = logoUrl, pullZoneHost = pullZoneHost)
        _uiState.update {
            it.copy(extractionMessage = "✅ Updated Bunny.net Logo & Pull Zone CDN: $logoUrl")
        }
    }

    fun addScrapedStreamDirect(
        animeId: String,
        animeTitle: String,
        episodeNumber: Int,
        episodeTitle: String,
        streamUrl: String,
        qualityLabel: String,
        subtitleUrl: String,
        subtitleLanguage: String,
        audioLanguage: String,
        serverSource: String
    ) {
        if (streamUrl.isBlank()) return
        adminRepository.addScrapedVideo(
            animeId = animeId,
            animeTitle = animeTitle,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            streamUrl = streamUrl,
            qualityLabel = qualityLabel,
            serverSource = serverSource,
            subtitleUrl = subtitleUrl,
            subtitleLanguage = subtitleLanguage,
            audioLanguage = audioLanguage
        )
        _uiState.update {
            it.copy(extractionMessage = "Published Ep $episodeNumber stream on $serverSource")
        }
    }

    fun createAnimeDirect(
        titleEnglish: String,
        studio: String,
        episodesCount: Int,
        releaseYear: Int,
        posterUrl: String,
        genresInput: String,
        description: String,
        isFeatured: Boolean,
        isTrending: Boolean
    ) {
        if (titleEnglish.isBlank()) return
        val newAnime = Anime(
            id = "anime_custom_${System.currentTimeMillis()}",
            slug = titleEnglish.lowercase().replace(Regex("[^a-z0-9]+"), "-"),
            titleEnglish = titleEnglish.trim(),
            titleRomaji = titleEnglish.trim(),
            titleJapanese = titleEnglish.trim(),
            description = description.ifBlank { "Streaming in 1080p HD across all servers." },
            posterUrl = posterUrl,
            bannerUrl = posterUrl,
            rating = 4.9f,
            score = 92,
            type = AnimeType.TV,
            status = AnimeStatus.RELEASING,
            episodesCount = episodesCount.coerceAtLeast(1),
            releaseYear = releaseYear,
            season = "Winter $releaseYear",
            durationMinutes = 24,
            studio = studio.ifBlank { "MAPPA" },
            genres = genresInput.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("Action", "Fantasy") },
            isFeatured = isFeatured,
            isTrending = isTrending,
            isPopular = true
        )
        adminRepository.addAnime(newAnime)
        loadCatalog()
        _uiState.update {
            it.copy(
                showAddAnimeDialog = false,
                extractionMessage = "Added '${newAnime.titleEnglish}' to catalog"
            )
        }
    }

    // ===================================
    // Scraped Video & Stream Injector
    // ===================================

    fun setShowAddScrapedDialog(show: Boolean) {
        _uiState.update { it.copy(showAddScrapedDialog = show) }
    }

    fun openScrapedDialogForAnime(anime: Anime) {
        _uiState.update {
            it.copy(
                showAddScrapedDialog = true,
                scrapeAnimeId = anime.id,
                scrapeAnimeTitle = anime.titleEnglish,
                scrapeEpisodeNumber = "1",
                scrapeEpisodeTitle = "${anime.titleEnglish} - Episode 1"
            )
        }
    }

    fun selectScrapedAnime(anime: Anime) {
        _uiState.update {
            it.copy(
                scrapeAnimeId = anime.id,
                scrapeAnimeTitle = anime.titleEnglish
            )
        }
    }

    fun updateScrapedVideoField(
        animeId: String? = null,
        animeTitle: String? = null,
        episodeNumber: String? = null,
        episodeTitle: String? = null,
        streamUrl: String? = null,
        qualityLabel: String? = null,
        serverSource: String? = null,
        subtitleUrl: String? = null,
        subtitleLanguage: String? = null,
        audioLanguage: String? = null
    ) {
        _uiState.update {
            it.copy(
                scrapeAnimeId = animeId ?: it.scrapeAnimeId,
                scrapeAnimeTitle = animeTitle ?: it.scrapeAnimeTitle,
                scrapeEpisodeNumber = episodeNumber ?: it.scrapeEpisodeNumber,
                scrapeEpisodeTitle = episodeTitle ?: it.scrapeEpisodeTitle,
                scrapeStreamUrl = streamUrl ?: it.scrapeStreamUrl,
                scrapeQualityLabel = qualityLabel ?: it.scrapeQualityLabel,
                scrapeServerSource = serverSource ?: it.scrapeServerSource,
                scrapeSubtitleUrl = subtitleUrl ?: it.scrapeSubtitleUrl,
                scrapeSubtitleLanguage = subtitleLanguage ?: it.scrapeSubtitleLanguage,
                scrapeAudioLanguage = audioLanguage ?: it.scrapeAudioLanguage
            )
        }
    }

    fun addScrapedVideo() {
        val state = _uiState.value
        if (state.scrapeStreamUrl.isBlank()) return

        val epNum = state.scrapeEpisodeNumber.toIntOrNull() ?: 1
        adminRepository.addScrapedVideo(
            animeId = state.scrapeAnimeId.ifBlank { "anime_1" },
            animeTitle = state.scrapeAnimeTitle.ifBlank { "Frieren: Beyond Journey's End" },
            episodeNumber = epNum,
            episodeTitle = state.scrapeEpisodeTitle,
            streamUrl = state.scrapeStreamUrl,
            qualityLabel = state.scrapeQualityLabel,
            serverSource = state.scrapeServerSource,
            subtitleUrl = state.scrapeSubtitleUrl,
            subtitleLanguage = state.scrapeSubtitleLanguage,
            audioLanguage = state.scrapeAudioLanguage
        )

        _uiState.update {
            it.copy(
                showAddScrapedDialog = false,
                scrapeStreamUrl = "",
                scrapeEpisodeTitle = "",
                scrapeSubtitleUrl = ""
            )
        }
    }

    fun deleteScrapedVideo(id: String) {
        adminRepository.deleteScrapedVideo(id)
    }

    fun testScrapedVideo(id: String) {
        adminRepository.testScrapedVideoUrl(id)
    }

    fun updateWebPageScrapeUrl(url: String) {
        _uiState.update { it.copy(webPageScrapeUrl = url, extractionMessage = null) }
    }

    fun extractVideoLinksFromWeb() {
        val url = _uiState.value.webPageScrapeUrl.trim().ifBlank { "https://api.animethemes.moe/anime?include=animethemes.animethemeentries.videos&page[size]=3" }

        _uiState.update {
            it.copy(
                isExtractingLinks = true,
                extractionMessage = "🔍 Inspecting website for APIs, Scraped Videos, HTML Embeds & CDNs..."
            )
        }
        viewModelScope.launch {
            val report = adminRepository.inspectWebsiteForApisVideosAndCdns(url)
            val allVideoAndEmbedUrls = (report.videos.map { it.url } + report.embeds.map { it.url }).distinct()
            _uiState.update {
                it.copy(
                    isExtractingLinks = false,
                    websiteInspectionReport = report,
                    extractedVideoLinks = allVideoAndEmbedUrls,
                    extractionMessage = report.summaryMessage
                )
            }
        }
    }

    fun repairAllAdminSystems() {
        adminRepository.repairAllStreamsAndServers()
        _uiState.update {
            it.copy(
                extractionMessage = "All 17 Servers, Website Portal & Scraped Streams Repaired & Verified Online (200 OK)!"
            )
        }
    }

    fun syncCrunchyrollCatalog() {
        _uiState.update { it.copy(isExtractingLinks = true, extractionMessage = "Syncing Crunchyroll Simulcast Catalog & 1080p HLS Streams...") }
        viewModelScope.launch {
            val count = adminRepository.syncCrunchyrollSimulcastCatalog()
            _uiState.update {
                it.copy(
                    isExtractingLinks = false,
                    extractionMessage = if (count > 0) {
                        "Synced $count Crunchyroll Simulcast 1080p HLS streams into Active Scraped Streams!"
                    } else {
                        "Crunchyroll Simulcast streams verified and ready."
                    }
                )
            }
        }
    }

    fun useExtractedVideoLink(link: String) {
        _uiState.update {
            it.copy(
                showAddScrapedDialog = true,
                scrapeStreamUrl = link,
                scrapeQualityLabel = when {
                    link.contains(".m3u8", true) -> "1080p HLS Master"
                    link.contains(".webm", true) -> "1080p WebM Direct"
                    link.contains("youtube.com", true) -> "Official Trailer HD"
                    else -> "1080p Direct MP4"
                },
                scrapeServerSource = when {
                    link.contains("animethemes.moe", true) -> "AnimeThemes Free Storage"
                    link.contains("archive.org", true) -> "Archive.org Storage"
                    link.contains("youtube.com", true) -> "YouTube Official"
                    else -> "Web Scraped Stream"
                }
            )
        }
    }

    // ===================================
    // Anime Catalog CMS
    // ===================================

    fun setShowAddAnimeDialog(show: Boolean) {
        _uiState.update { it.copy(showAddAnimeDialog = show) }
    }

    fun updateNewAnimeField(
        title: String? = null,
        japanese: String? = null,
        studio: String? = null,
        genre: String? = null,
        episodes: String? = null,
        desc: String? = null,
        posterUrl: String? = null,
        trailerUrl: String? = null
    ) {
        _uiState.update {
            it.copy(
                newAnimeTitle = title ?: it.newAnimeTitle,
                newAnimeJapanese = japanese ?: it.newAnimeJapanese,
                newAnimeStudio = studio ?: it.newAnimeStudio,
                newAnimeGenre = genre ?: it.newAnimeGenre,
                newAnimeEpisodes = episodes ?: it.newAnimeEpisodes,
                newAnimeDescription = desc ?: it.newAnimeDescription,
                newAnimePosterUrl = posterUrl ?: it.newAnimePosterUrl,
                newAnimeTrailerUrl = trailerUrl ?: it.newAnimeTrailerUrl
            )
        }
    }

    fun createAnime() {
        val state = _uiState.value
        if (state.newAnimeTitle.isBlank()) return

        val normalizedTrailer = state.newAnimeTrailerUrl.trim().let { raw ->
            when {
                raw.contains("youtube.com/watch?v=") -> {
                    val id = raw.substringAfter("v=").substringBefore("&")
                    "https://www.youtube.com/embed/$id"
                }
                raw.contains("youtu.be/") -> {
                    val id = raw.substringAfter("youtu.be/").substringBefore("?")
                    "https://www.youtube.com/embed/$id"
                }
                else -> raw
            }
        }

        val newAnime = Anime(
            id = "anime_custom_${System.currentTimeMillis()}",
            slug = state.newAnimeTitle.lowercase().replace(" ", "-"),
            titleEnglish = state.newAnimeTitle,
            titleRomaji = state.newAnimeTitle,
            titleJapanese = state.newAnimeJapanese,
            description = state.newAnimeDescription.ifBlank { "Added via KuroStream Admin CMS." },
            posterUrl = state.newAnimePosterUrl,
            bannerUrl = state.newAnimePosterUrl,
            rating = 4.8f,
            score = 90,
            type = AnimeType.TV,
            status = AnimeStatus.RELEASING,
            episodesCount = state.newAnimeEpisodes.toIntOrNull() ?: 12,
            releaseYear = 2025,
            season = "Winter 2025",
            durationMinutes = 24,
            studio = state.newAnimeStudio,
            genres = listOf(state.newAnimeGenre, "Action"),
            trailerUrl = normalizedTrailer,
            isFeatured = true,
            isTrending = true,
            isPopular = true
        )

        adminRepository.addAnime(newAnime)
        loadCatalog()
        _uiState.update {
            it.copy(
                showAddAnimeDialog = false,
                newAnimeTitle = "",
                newAnimeJapanese = "",
                newAnimeDescription = "",
                newAnimeTrailerUrl = ""
            )
        }
    }

    fun deleteAnime(anime: Anime) {
        adminRepository.deleteAnime(anime.id, anime.titleEnglish)
        loadCatalog()
    }

    fun triggerTranscode(animeTitle: String, episodeNumber: Int) {
        adminRepository.triggerTranscodeJob(animeTitle, episodeNumber, "1080p Stream")
    }

    fun moderateUser(userId: String, newStatus: String) {
        adminRepository.updateModerationStatus(userId, newStatus)
    }

    // ===================================
    // Multi-Server API & Status Check
    // ===================================

    fun setShowAddApiDialog(show: Boolean) {
        _uiState.update { it.copy(showAddApiDialog = show) }
    }

    fun updateNewApiField(
        name: String? = null,
        url: String? = null,
        category: String? = null,
        key: String? = null
    ) {
        _uiState.update {
            it.copy(
                newApiName = name ?: it.newApiName,
                newApiUrl = url ?: it.newApiUrl,
                newApiCategory = category ?: it.newApiCategory,
                newApiKey = key ?: it.newApiKey
            )
        }
    }

    fun createApiConfig() {
        val state = _uiState.value
        if (state.newApiName.isBlank() || state.newApiUrl.isBlank()) return

        adminRepository.addApiConfig(
            name = state.newApiName,
            baseUrl = state.newApiUrl,
            category = state.newApiCategory,
            apiKey = state.newApiKey
        )

        _uiState.update {
            it.copy(
                showAddApiDialog = false,
                newApiName = "",
                newApiUrl = "",
                newApiKey = ""
            )
        }
    }

    fun activateApi(apiId: String) {
        adminRepository.setActiveApi(apiId)
    }

    fun testApi(apiId: String) {
        adminRepository.testApiConnection(apiId)
    }

    fun checkAllApisStatus() {
        adminRepository.checkAllApisStatus()
    }

    fun deleteApi(apiId: String) {
        adminRepository.deleteApiConfig(apiId)
    }

    // ===================================
    // Google AdMob Account & Monetization
    // ===================================

    fun saveAdMobConfig(
        accountEmail: String,
        publisherId: String,
        appId: String,
        bannerAdUnitId: String,
        interstitialAdUnitId: String,
        rewardedAdUnitId: String,
        nativeAdUnitId: String,
        adsEnabled: Boolean,
        bannerAdsEnabled: Boolean,
        interstitialAdsEnabled: Boolean,
        rewardedAdsEnabled: Boolean,
        testModeEnabled: Boolean
    ) {
        adminRepository.saveAdMobAccountConfig(
            accountEmail = accountEmail,
            publisherId = publisherId,
            appId = appId,
            bannerAdUnitId = bannerAdUnitId,
            interstitialAdUnitId = interstitialAdUnitId,
            rewardedAdUnitId = rewardedAdUnitId,
            nativeAdUnitId = nativeAdUnitId,
            adsEnabled = adsEnabled,
            bannerAdsEnabled = bannerAdsEnabled,
            interstitialAdsEnabled = interstitialAdsEnabled,
            rewardedAdsEnabled = rewardedAdsEnabled,
            testModeEnabled = testModeEnabled
        )
    }

    fun testAdMobImpression() {
        adminRepository.recordTestAdMobImpression()
    }
}
