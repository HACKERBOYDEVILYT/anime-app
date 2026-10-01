package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val selectedTab: Int = 0, // 0 = Analytics, 1 = Catalog, 2 = Video Pipeline, 3 = Moderation, 4 = Logs, 5 = API Manager
    val animeList: List<Anime> = emptyList(),
    val showAddAnimeDialog: Boolean = false,
    val newAnimeTitle: String = "",
    val newAnimeJapanese: String = "",
    val newAnimeStudio: String = "MAPPA",
    val newAnimeGenre: String = "Action",
    val newAnimeEpisodes: String = "12",
    val newAnimeDescription: String = "",
    val newAnimePosterUrl: String = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600",
    // Dynamic API Management State
    val showAddApiDialog: Boolean = false,
    val newApiName: String = "",
    val newApiUrl: String = "",
    val newApiCategory: String = "Streaming HLS",
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

    init {
        loadCatalog()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun loadCatalog() {
        viewModelScope.launch {
            val list = animeRepository.getPopular()
            _uiState.update { it.copy(animeList = list) }
        }
    }

    fun setShowAddAnimeDialog(show: Boolean) {
        _uiState.update { it.copy(showAddAnimeDialog = show) }
    }

    fun updateNewAnimeField(
        title: String? = null,
        japanese: String? = null,
        studio: String? = null,
        genre: String? = null,
        episodes: String? = null,
        desc: String? = null
    ) {
        _uiState.update {
            it.copy(
                newAnimeTitle = title ?: it.newAnimeTitle,
                newAnimeJapanese = japanese ?: it.newAnimeJapanese,
                newAnimeStudio = studio ?: it.newAnimeStudio,
                newAnimeGenre = genre ?: it.newAnimeGenre,
                newAnimeEpisodes = episodes ?: it.newAnimeEpisodes,
                newAnimeDescription = desc ?: it.newAnimeDescription
            )
        }
    }

    fun createAnime() {
        val state = _uiState.value
        if (state.newAnimeTitle.isBlank()) return

        val newAnime = Anime(
            id = "anime_custom_${System.currentTimeMillis()}",
            slug = state.newAnimeTitle.lowercase().replace(" ", "-"),
            titleEnglish = state.newAnimeTitle,
            titleRomaji = state.newAnimeTitle,
            titleJapanese = state.newAnimeJapanese,
            description = state.newAnimeDescription.ifBlank { "Exciting new anime series uploaded via KuroStream Admin CMS." },
            posterUrl = state.newAnimePosterUrl,
            bannerUrl = state.newAnimePosterUrl,
            rating = 4.8f,
            score = 90,
            type = AnimeType.TV,
            status = AnimeStatus.RELEASING,
            episodesCount = state.newAnimeEpisodes.toIntOrNull() ?: 12,
            releaseYear = 2026,
            season = "Winter 2026",
            durationMinutes = 24,
            studio = state.newAnimeStudio,
            genres = listOf(state.newAnimeGenre, "Fantasy"),
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
                newAnimeDescription = ""
            )
        }
    }

    fun deleteAnime(anime: Anime) {
        adminRepository.deleteAnime(anime.id, anime.titleEnglish)
        loadCatalog()
    }

    fun triggerTranscode(animeTitle: String, episodeNumber: Int) {
        adminRepository.triggerTranscodeJob(animeTitle, episodeNumber, "4K HLS Master")
    }

    fun moderateUser(userId: String, newStatus: String) {
        adminRepository.updateModerationStatus(userId, newStatus)
    }

    // ===================================
    // Dynamic API Management Actions
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

    fun deleteApi(apiId: String) {
        adminRepository.deleteApiConfig(apiId)
    }
}
