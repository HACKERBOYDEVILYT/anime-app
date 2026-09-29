package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.model.Review
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.data.repository.AnimeRepository
import com.example.data.repository.WatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val isLoading: Boolean = true,
    val anime: Anime? = null,
    val episodes: List<Episode> = emptyList(),
    val recommendations: List<Anime> = emptyList(),
    val watchlistItem: WatchlistItem? = null,
    val episodeSearchQuery: String = "",
    val isEpisodeSortAsc: Boolean = true,
    val selectedTab: Int = 0, // 0 = Episodes, 1 = Overview/Cast, 2 = Reviews, 3 = Related
    val userSelectedRating: Int = 5,
    val reviewTextInput: String = "",
    val showReviewDialog: Boolean = false,
    val error: String? = null
)

class DetailsViewModel(
    private val animeId: String,
    private val animeRepository: AnimeRepository,
    private val watchRepository: WatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    val reviews: StateFlow<List<Review>> = watchRepository.getReviewsForAnime(animeId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadDetails()
        observeWatchlist()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val anime = animeRepository.getAnimeById(animeId)
                val episodes = animeRepository.getEpisodes(animeId)
                val recs = animeRepository.getRecommendations(animeId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        anime = anime,
                        episodes = episodes,
                        recommendations = recs
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            watchRepository.isAnimeInWatchlist(animeId).collect { item ->
                _uiState.update { it.copy(watchlistItem = item) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun onEpisodeSearchChange(q: String) {
        _uiState.update { it.copy(episodeSearchQuery = q) }
    }

    fun toggleEpisodeSort() {
        _uiState.update { it.copy(isEpisodeSortAsc = !it.isEpisodeSortAsc) }
    }

    fun updateWatchStatus(status: WatchStatus) {
        val anime = _uiState.value.anime ?: return
        viewModelScope.launch {
            watchRepository.updateWatchlistStatus(
                animeId = anime.id,
                animeTitle = anime.titleEnglish,
                posterUrl = anime.posterUrl,
                rating = anime.rating,
                episodeCount = anime.episodesCount,
                status = status,
                isFavorite = _uiState.value.watchlistItem?.isFavorite == true
            )
        }
    }

    fun toggleFavorite() {
        val anime = _uiState.value.anime ?: return
        viewModelScope.launch {
            watchRepository.toggleFavorite(
                animeId = anime.id,
                animeTitle = anime.titleEnglish,
                posterUrl = anime.posterUrl,
                rating = anime.rating,
                episodeCount = anime.episodesCount
            )
        }
    }

    fun setRating(stars: Int) {
        _uiState.update { it.copy(userSelectedRating = stars) }
    }

    fun setReviewText(text: String) {
        _uiState.update { it.copy(reviewTextInput = text) }
    }

    fun showReviewDialog(show: Boolean) {
        _uiState.update { it.copy(showReviewDialog = show) }
    }

    fun submitReview() {
        val state = _uiState.value
        if (state.reviewTextInput.isBlank()) return
        viewModelScope.launch {
            watchRepository.submitReview(
                animeId = animeId,
                rating = state.userSelectedRating,
                content = state.reviewTextInput,
                userName = "OtakuStreamer",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
            )
            _uiState.update { it.copy(reviewTextInput = "", showReviewDialog = false) }
        }
    }

    fun likeReview(reviewId: String) {
        viewModelScope.launch {
            watchRepository.likeReview(reviewId)
        }
    }
}
