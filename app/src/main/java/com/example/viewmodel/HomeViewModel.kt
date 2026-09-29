package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchStatus
import com.example.data.repository.AnimeRepository
import com.example.data.repository.WatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredAnime: Anime? = null,
    val trending: List<Anime> = emptyList(),
    val popular: List<Anime> = emptyList(),
    val topRated: List<Anime> = emptyList(),
    val seasonal: List<Anime> = emptyList(),
    val recentlyAdded: List<Anime> = emptyList(),
    val genres: List<String> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val animeRepository: AnimeRepository,
    private val watchRepository: WatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val continueWatching: StateFlow<List<WatchHistoryItem>> = watchRepository.continueWatching
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notifications = watchRepository.getNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadHomeData()
        seedNotifications()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val trending = animeRepository.getTrending()
                val popular = animeRepository.getPopular()
                val topRated = animeRepository.getTopRated()
                val seasonal = animeRepository.getSeasonal()
                val recent = animeRepository.getRecentlyAdded()
                val genres = animeRepository.getGenres()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        featuredAnime = trending.firstOrNull() ?: popular.firstOrNull(),
                        trending = trending,
                        popular = popular,
                        topRated = topRated,
                        seasonal = seasonal,
                        recentlyAdded = recent,
                        genres = genres
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun seedNotifications() {
        viewModelScope.launch {
            watchRepository.seedInitialNotificationsIfEmpty()
        }
    }

    fun toggleWatchlist(anime: Anime) {
        viewModelScope.launch {
            watchRepository.updateWatchlistStatus(
                animeId = anime.id,
                animeTitle = anime.titleEnglish,
                posterUrl = anime.posterUrl,
                rating = anime.rating,
                episodeCount = anime.episodesCount,
                status = WatchStatus.WATCHING,
                isFavorite = false
            )
        }
    }

    fun removeContinueWatching(item: WatchHistoryItem) {
        viewModelScope.launch {
            watchRepository.deleteHistoryItem(item.episodeId)
        }
    }
}
