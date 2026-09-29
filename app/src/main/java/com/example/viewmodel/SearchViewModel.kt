package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.repository.AnimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchFilterState(
    val selectedGenre: String = "All",
    val selectedYear: Int? = null,
    val selectedType: String = "All",
    val selectedStatus: String = "All",
    val sortBy: String = "POPULARITY"
)

data class SearchUiState(
    val query: String = "",
    val searchHistory: List<String> = listOf("Frieren", "Jujutsu Kaisen", "Solo Leveling", "Demon Slayer", "MAPPA"),
    val genres: List<String> = emptyList(),
    val studios: List<String> = emptyList(),
    val filters: SearchFilterState = SearchFilterState(),
    val results: List<Anime> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false
)

class SearchViewModel(
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadMetadata()
        performSearch()
    }

    private fun loadMetadata() {
        viewModelScope.launch {
            val genres = animeRepository.getGenres()
            val studios = animeRepository.getStudios()
            _uiState.update { it.copy(genres = genres, studios = studios) }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            performSearch()
        }
    }

    fun onGenreSelected(genre: String) {
        _uiState.update { it.copy(filters = it.filters.copy(selectedGenre = genre)) }
        performSearch()
    }

    fun onTypeSelected(type: String) {
        _uiState.update { it.copy(filters = it.filters.copy(selectedType = type)) }
        performSearch()
    }

    fun onStatusSelected(status: String) {
        _uiState.update { it.copy(filters = it.filters.copy(selectedStatus = status)) }
        performSearch()
    }

    fun onSortSelected(sort: String) {
        _uiState.update { it.copy(filters = it.filters.copy(sortBy = sort)) }
        performSearch()
    }

    fun applySuggestion(suggestion: String) {
        _uiState.update { it.copy(query = suggestion) }
        performSearch()
    }

    fun clearSearchHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
    }

    fun performSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val state = _uiState.value
            val results = animeRepository.search(
                query = state.query,
                genre = state.filters.selectedGenre,
                year = state.filters.selectedYear,
                type = state.filters.selectedType,
                status = state.filters.selectedStatus,
                sortBy = state.filters.sortBy
            )
            // Add to search history if non-empty
            val updatedHistory = if (state.query.isNotBlank() && !state.searchHistory.contains(state.query.trim())) {
                listOf(state.query.trim()) + state.searchHistory.take(8)
            } else state.searchHistory

            _uiState.update {
                it.copy(
                    isSearching = false,
                    results = results,
                    hasSearched = true,
                    searchHistory = updatedHistory
                )
            }
        }
    }
}
