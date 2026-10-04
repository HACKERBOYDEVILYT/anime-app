package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchStatus
import com.example.data.repository.AnimeRepository
import com.example.data.repository.WatchRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
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

    private val _uiState = MutableStateFlow(buildInitialState())
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
        purgeFakeDemoNotifications()
    }

    private fun buildInitialState(): HomeUiState {
        val snapshot = animeRepository.getInitialSnapshot()
        if (snapshot.isEmpty()) {
            return HomeUiState(isLoading = true)
        }
        val trendingInit = snapshot.filter { it.isTrending }.ifEmpty { snapshot }
        val popularInit = snapshot.filter { it.isPopular }.ifEmpty { snapshot }
        val topRatedInit = snapshot.sortedByDescending { it.rating }
        val seasonalInit = snapshot.filter { it.isSeasonal }.ifEmpty { snapshot.take(6) }
        val recentInit = snapshot.sortedByDescending { it.releaseYear }
        val allGenres = snapshot.flatMap { it.genres }.distinct()
        return HomeUiState(
            isLoading = false,
            featuredAnime = trendingInit.firstOrNull() ?: snapshot.firstOrNull(),
            trending = trendingInit,
            popular = popularInit,
            topRated = topRatedInit,
            seasonal = seasonalInit,
            recentlyAdded = recentInit,
            genres = allGenres
        )
    }

    fun loadHomeData() {
        viewModelScope.launch {
            if (_uiState.value.featuredAnime == null) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                coroutineScope {
                    val trendingDeferred = async { runCatching { animeRepository.getTrending() }.getOrDefault(emptyList()) }
                    val popularDeferred = async { runCatching { animeRepository.getPopular() }.getOrDefault(emptyList()) }
                    val topRatedDeferred = async { runCatching { animeRepository.getTopRated() }.getOrDefault(emptyList()) }
                    val seasonalDeferred = async { runCatching { animeRepository.getSeasonal() }.getOrDefault(emptyList()) }
                    val recentDeferred = async { runCatching { animeRepository.getRecentlyAdded() }.getOrDefault(emptyList()) }
                    val genresDeferred = async { runCatching { animeRepository.getGenres() }.getOrDefault(emptyList()) }

                    val trending = trendingDeferred.await().ifEmpty { _uiState.value.trending }
                    val popular = popularDeferred.await().ifEmpty { _uiState.value.popular }
                    val topRated = topRatedDeferred.await().ifEmpty { _uiState.value.topRated }
                    val seasonal = seasonalDeferred.await().ifEmpty { _uiState.value.seasonal }
                    val recent = recentDeferred.await().ifEmpty { _uiState.value.recentlyAdded }
                    val genres = genresDeferred.await().ifEmpty { _uiState.value.genres }

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            featuredAnime = current.featuredAnime ?: trending.firstOrNull() ?: popular.firstOrNull(),
                            trending = trending,
                            popular = popular,
                            topRated = topRated,
                            seasonal = seasonal,
                            recentlyAdded = recent,
                            genres = genres
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun purgeFakeDemoNotifications() {
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
