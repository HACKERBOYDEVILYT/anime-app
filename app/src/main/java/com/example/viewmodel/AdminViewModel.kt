package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Auto Web Page Video Link Extractor State
    val webPageScrapeUrl: String = "",
    val isExtractingLinks: Boolean = false,
    val extractedVideoLinks: List<String> = emptyList(),
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

    init {
        loadCatalog()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun loadCatalog() {
        viewModelScope.launch {
            val list = animeRepository.getPopular()
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
        val url = _uiState.value.webPageScrapeUrl.trim()
        if (url.isBlank()) return

        _uiState.update { it.copy(isExtractingLinks = true, extractionMessage = "Scanning page for video & trailer streams...") }
        viewModelScope.launch {
            val links = adminRepository.extractVideoLinksFromWebPage(url)
            _uiState.update {
                it.copy(
                    isExtractingLinks = false,
                    extractedVideoLinks = links,
                    extractionMessage = if (links.isNotEmpty()) {
                        "Found ${links.size} playable video/trailer link(s)! Tap any link to inject."
                    } else {
                        "No direct .m3u8/.mp4/.webm or embed links found on that page. You can paste a direct stream URL via '+ Add Scraped Video'."
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
}
