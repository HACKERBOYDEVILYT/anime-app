package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistCollection
import com.example.data.model.WatchlistItem
import com.example.data.repository.GamificationAndSocialRepository
import com.example.data.repository.WatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WatchlistSortMode(val label: String) {
    RECENT("Recently Added"),
    RATING_DESC("Highest Rated"),
    TITLE_ASC("Title (A–Z)"),
    EPISODES_DESC("Most Episodes")
}

class WatchlistViewModel(
    private val watchRepository: WatchRepository,
    val gamificationRepository: GamificationAndSocialRepository = GamificationAndSocialRepository()
) : ViewModel() {

    private val _selectedStatus = MutableStateFlow<WatchStatus?>(null)
    val selectedStatus: StateFlow<WatchStatus?> = _selectedStatus.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _selectedCollectionId = MutableStateFlow<String?>(null)
    val selectedCollectionId: StateFlow<String?> = _selectedCollectionId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(WatchlistSortMode.RECENT)
    val sortMode: StateFlow<WatchlistSortMode> = _sortMode.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    val customCollections: StateFlow<List<WatchlistCollection>> = gamificationRepository.customCollections

    val continueWatching: StateFlow<List<WatchHistoryItem>> = watchRepository.getContinueWatching()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeContinueWatching(item: WatchHistoryItem) {
        viewModelScope.launch {
            watchRepository.removeHistoryItem(item.episodeId)
        }
    }

    val allWatchlist: StateFlow<List<WatchlistItem>> = watchRepository.getAllWatchlist()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectStatusFilter(status: WatchStatus?) {
        _showFavoritesOnly.value = false
        _selectedCollectionId.value = null
        _selectedStatus.value = status
    }

    fun toggleFavoritesFilter() {
        _selectedStatus.value = null
        _selectedCollectionId.value = null
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun selectCollectionFilter(collectionId: String?) {
        _selectedStatus.value = null
        _showFavoritesOnly.value = false
        _selectedCollectionId.value = if (_selectedCollectionId.value == collectionId) null else collectionId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSortMode(mode: WatchlistSortMode) {
        _sortMode.value = mode
    }

    fun toggleGridListView() {
        _isGridView.value = !_isGridView.value
    }

    fun createCustomCollection(name: String, description: String, emoji: String = "📁") {
        gamificationRepository.createCustomCollection(name, description, emoji)
    }

    fun addAnimeToCollection(collectionId: String, animeId: String) {
        gamificationRepository.addAnimeToCollection(collectionId, animeId)
    }

    fun deleteCustomCollection(collectionId: String) {
        if (_selectedCollectionId.value == collectionId) {
            _selectedCollectionId.value = null
        }
        gamificationRepository.deleteCustomCollection(collectionId)
    }

    fun updateStatus(item: WatchlistItem, newStatus: WatchStatus) {
        viewModelScope.launch {
            watchRepository.updateWatchlistStatus(
                animeId = item.animeId,
                animeTitle = item.animeTitle,
                posterUrl = item.posterUrl,
                rating = item.rating,
                episodeCount = item.episodeCount,
                status = newStatus,
                isFavorite = item.isFavorite
            )
        }
    }

    fun toggleFavorite(item: WatchlistItem) {
        viewModelScope.launch {
            watchRepository.toggleFavorite(
                animeId = item.animeId,
                animeTitle = item.animeTitle,
                posterUrl = item.posterUrl,
                rating = item.rating,
                episodeCount = item.episodeCount
            )
        }
    }

    fun removeFromWatchlist(animeId: String) {
        viewModelScope.launch {
            watchRepository.removeFromWatchlist(animeId)
        }
    }
}
