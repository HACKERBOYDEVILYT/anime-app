package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.data.repository.WatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val selectedTab: Int = 0, // 0=All, 1=Watching, 2=Plan to Watch, 3=Completed, 4=Favorites, 5=History
    val showClearHistoryDialog: Boolean = false
)

class WatchlistViewModel(
    private val watchRepository: WatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    val allWatchlist: StateFlow<List<WatchlistItem>> = watchRepository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchingList: StateFlow<List<WatchlistItem>> = watchRepository.getWatchlistByStatus(WatchStatus.WATCHING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val planToWatchList: StateFlow<List<WatchlistItem>> = watchRepository.getWatchlistByStatus(WatchStatus.PLAN_TO_WATCH)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedList: StateFlow<List<WatchlistItem>> = watchRepository.getWatchlistByStatus(WatchStatus.COMPLETED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritesList: StateFlow<List<WatchlistItem>> = watchRepository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyList: StateFlow<List<WatchHistoryItem>> = watchRepository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun removeFromWatchlist(animeId: String) {
        viewModelScope.launch {
            watchRepository.removeFromWatchlist(animeId)
        }
    }

    fun deleteHistoryItem(episodeId: String) {
        viewModelScope.launch {
            watchRepository.deleteHistoryItem(episodeId)
        }
    }

    fun setShowClearHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showClearHistoryDialog = show) }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            watchRepository.clearHistory()
            _uiState.update { it.copy(showClearHistoryDialog = false) }
        }
    }
}
