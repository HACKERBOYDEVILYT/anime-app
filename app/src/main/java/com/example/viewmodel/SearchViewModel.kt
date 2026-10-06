package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.model.AnimeSortOption
import com.example.data.model.AnimeStatus
import com.example.data.model.AnimeType
import com.example.data.model.CharacterProfile
import com.example.data.network.CatalogNetworkMonitor
import com.example.data.repository.AnimeRepository
import com.example.data.repository.GamificationAndSocialRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategorizedSearchSuggestion(
    val label: String,
    val category: String, // "Anime", "Characters", "Episodes", "Genres", "Studio / VA"
    val targetQuery: String
)

data class SearchUiState(
    val query: String = "",
    val selectedGenre: String? = null,
    val selectedYear: Int? = null,
    val selectedSeason: String? = null,
    val selectedType: AnimeType? = null,
    val selectedStatus: AnimeStatus? = null,
    val selectedStudio: String? = null,
    val minScoreFilter: Int? = null, // 70, 80, 90
    val episodeCountFilter: String? = null, // "1-12", "13-24", "25+"
    val durationFilter: String? = null, // "<20m", "20-30m", "30m+"
    val languageFilter: String? = null, // "Japanese", "English", "Hindi", "Bengali"
    val subOnly: Boolean = false,
    val dubOnly: Boolean = false,
    val sortOption: AnimeSortOption = AnimeSortOption.POPULARITY,
    val results: List<Anime> = emptyList(),
    val matchedCharacters: List<CharacterProfile> = emptyList(),
    val matchedEpisodesSummary: List<String> = emptyList(),
    val suggestions: List<CategorizedSearchSuggestion> = emptyList(),
    val noResultSuggestions: List<String> = listOf("Frieren: Beyond Journey's End", "Jujutsu Kaisen", "Solo Leveling", "Demon Slayer", "One Piece"),
    val trendingSearches: List<String> = listOf(
        "Frieren Season 2",
        "Jujutsu Kaisen Shibuya",
        "Solo Leveling Ep 12",
        "Naruto Shippuden",
        "One Piece Egghead",
        "Satoru Gojo",
        "MAPPA Studio"
    ),
    val availableGenres: List<String> = emptyList(),
    val availableStudios: List<String> = listOf("MAPPA", "Madhouse", "ufotable", "A-1 Pictures", "Toei Animation", "Bones", "CloverWorks"),
    val isSearching: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val animeRepository: AnimeRepository,
    private val gamificationRepository: GamificationAndSocialRepository = GamificationAndSocialRepository(),
    val catalogNetworkMonitor: CatalogNetworkMonitor = CatalogNetworkMonitor.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    private val _recentSearches = MutableStateFlow(
        listOf("Frieren", "Solo Leveling", "Jujutsu Kaisen", "One Piece")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    init {
        viewModelScope.launch {
            val genres = animeRepository.getGenres()
            _uiState.update { it.copy(availableGenres = genres) }
            executeFilterAndSearch()
        }

        viewModelScope.launch {
            queryFlow
                .debounce(180)
                .distinctUntilChanged()
                .collect {
                    executeFilterAndSearch()
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        val suggestions = buildCategorizedSuggestions(newQuery)
        _uiState.update { it.copy(query = newQuery, suggestions = suggestions) }
        queryFlow.value = newQuery
    }

    private fun buildCategorizedSuggestions(rawQuery: String): List<CategorizedSearchSuggestion> {
        val q = rawQuery.trim()
        if (q.isBlank()) return emptyList()
        val displayTitle = q.replaceFirstChar { it.uppercase() }
        return listOf(
            CategorizedSearchSuggestion(displayTitle, "Anime", q),
            CategorizedSearchSuggestion("$displayTitle Shippuden / Season 2", "Anime", q),
            CategorizedSearchSuggestion("$displayTitle: The Last (Movie)", "Anime", q),
            CategorizedSearchSuggestion("$displayTitle Characters", "Characters", q),
            CategorizedSearchSuggestion("$displayTitle Episodes (1080p)", "Episodes", q),
            CategorizedSearchSuggestion("$displayTitle Genre & Studio Matches", "Genres", q)
        )
    }

    fun onSubmitQuery(submitted: String) {
        val trimmed = submitted.trim()
        if (trimmed.isNotEmpty()) {
            _recentSearches.update { existing ->
                (listOf(trimmed) + existing.filterNot { it.equals(trimmed, ignoreCase = true) }).take(10)
            }
        }
        onQueryChange(trimmed)
    }

    fun selectGenre(genre: String?) {
        _uiState.update {
            it.copy(selectedGenre = if (it.selectedGenre == genre) null else genre)
        }
        executeFilterAndSearch()
    }

    fun selectYear(year: Int?) {
        _uiState.update {
            it.copy(selectedYear = if (it.selectedYear == year) null else year)
        }
        executeFilterAndSearch()
    }

    fun selectSeason(season: String?) {
        _uiState.update {
            it.copy(selectedSeason = if (it.selectedSeason == season) null else season)
        }
        executeFilterAndSearch()
    }

    fun selectType(type: AnimeType?) {
        _uiState.update {
            it.copy(selectedType = if (it.selectedType == type) null else type)
        }
        executeFilterAndSearch()
    }

    fun selectStatus(status: AnimeStatus?) {
        _uiState.update {
            it.copy(selectedStatus = if (it.selectedStatus == status) null else status)
        }
        executeFilterAndSearch()
    }

    fun selectStudio(studio: String?) {
        _uiState.update {
            it.copy(selectedStudio = if (it.selectedStudio == studio) null else studio)
        }
        executeFilterAndSearch()
    }

    fun selectMinScore(minScore: Int?) {
        _uiState.update {
            it.copy(minScoreFilter = if (it.minScoreFilter == minScore) null else minScore)
        }
        executeFilterAndSearch()
    }

    fun selectEpisodeCountRange(range: String?) {
        _uiState.update {
            it.copy(episodeCountFilter = if (it.episodeCountFilter == range) null else range)
        }
        executeFilterAndSearch()
    }

    fun selectDurationRange(range: String?) {
        _uiState.update {
            it.copy(durationFilter = if (it.durationFilter == range) null else range)
        }
        executeFilterAndSearch()
    }

    fun selectLanguageFilter(lang: String?) {
        _uiState.update {
            it.copy(languageFilter = if (it.languageFilter == lang) null else lang)
        }
        executeFilterAndSearch()
    }

    fun toggleSubOnly() {
        _uiState.update { it.copy(subOnly = !it.subOnly) }
        executeFilterAndSearch()
    }

    fun toggleDubOnly() {
        _uiState.update { it.copy(dubOnly = !it.dubOnly) }
        executeFilterAndSearch()
    }

    fun selectSort(sortOption: AnimeSortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
        executeFilterAndSearch()
    }

    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                query = "",
                selectedGenre = null,
                selectedYear = null,
                selectedSeason = null,
                selectedType = null,
                selectedStatus = null,
                selectedStudio = null,
                minScoreFilter = null,
                episodeCountFilter = null,
                durationFilter = null,
                languageFilter = null,
                subOnly = false,
                dubOnly = false,
                suggestions = emptyList(),
                sortOption = AnimeSortOption.POPULARITY
            )
        }
        queryFlow.value = ""
        executeFilterAndSearch()
    }

    fun deleteHistoryItem(q: String) {
        _recentSearches.update { list -> list.filterNot { it == q } }
    }

    fun clearHistory() {
        _recentSearches.value = emptyList()
    }

    fun retryCatalogConnection() {
        catalogNetworkMonitor.retryCatalogConnection {
            executeFilterAndSearch()
        }
    }

    fun toggleSimulatedOfflineMode() {
        catalogNetworkMonitor.toggleSimulatedOfflineCatalogFailure()
    }

    fun dismissFetchError() {
        _uiState.update { it.copy(error = null) }
        catalogNetworkMonitor.dismissFetchNotification()
    }

    private fun executeFilterAndSearch() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSearching = true, error = null) }
            val isOnline = catalogNetworkMonitor.verifyConnectionBeforeCatalogFetch("Search Catalog API")

            val baseFiltered = animeRepository.filterCatalog(
                genre = state.selectedGenre,
                year = state.selectedYear,
                season = state.selectedSeason,
                type = state.selectedType,
                status = state.selectedStatus,
                sort = state.sortOption
            )

            val q = state.query.trim().lowercase()
            val allCharacters = gamificationRepository.characters.value
            val matchedChars = if (q.isEmpty()) {
                emptyList()
            } else {
                allCharacters.filter { ch ->
                    ch.name.lowercase().contains(q) ||
                        ch.japaneseName.lowercase().contains(q) ||
                        ch.animeTitle.lowercase().contains(q) ||
                        ch.voiceActor.lowercase().contains(q)
                }
            }

            val searched = if (q.isEmpty()) {
                baseFiltered
            } else {
                val charAnimeIds = matchedChars.map { it.animeId }.toSet()
                baseFiltered.filter { anime ->
                    anime.titleEnglish.lowercase().contains(q) ||
                        anime.titleJapanese.lowercase().contains(q) ||
                        anime.synopsis.lowercase().contains(q) ||
                        anime.studio.lowercase().contains(q) ||
                        anime.season.lowercase().contains(q) ||
                        anime.releaseYear.toString().contains(q) ||
                        anime.genres.any { it.lowercase().contains(q) } ||
                        anime.tags.any { it.lowercase().contains(q) } ||
                        anime.characters.any { c ->
                            c.name.lowercase().contains(q) || c.voiceActor.lowercase().contains(q)
                        } ||
                        anime.id in charAnimeIds ||
                        q.startsWith("ep") || q.contains("episode")
                }
            }

            val fullyFiltered = searched.filter { anime ->
                val studioMatch = state.selectedStudio == null || anime.studio.equals(state.selectedStudio, ignoreCase = true)
                val scoreMatch = state.minScoreFilter == null || anime.score >= state.minScoreFilter
                val epCountMatch = when (state.episodeCountFilter) {
                    "1-12" -> anime.episodesCount <= 12
                    "13-24" -> anime.episodesCount in 13..24
                    "25+" -> anime.episodesCount >= 25
                    else -> true
                }
                val durationMatch = when (state.durationFilter) {
                    "<20m" -> anime.durationMinutes < 20
                    "20-30m" -> anime.durationMinutes in 20..30
                    "30m+" -> anime.durationMinutes > 30
                    else -> true
                }
                val subMatch = !state.subOnly || anime.hasSub
                val dubMatch = !state.dubOnly || anime.hasDub
                val langMatch = when (state.languageFilter) {
                    "English" -> anime.hasDub
                    "Japanese" -> anime.hasSub
                    else -> true
                }
                studioMatch && scoreMatch && epCountMatch && durationMatch && subMatch && dubMatch && langMatch
            }

            val episodeMatches = if (q.isBlank()) {
                emptyList()
            } else {
                fullyFiltered.take(3).map { anime ->
                    "${anime.titleEnglish} • Episode 1 - ${anime.episodesCount} (1080p Sub/Dub)"
                }
            }

            _uiState.update {
                it.copy(
                    results = fullyFiltered,
                    matchedCharacters = matchedChars,
                    matchedEpisodesSummary = episodeMatches,
                    isSearching = false,
                    error = if (!isOnline) {
                        "Lost internet connection (navigator.onLine = false) while attempting to fetch search catalog data."
                    } else {
                        null
                    }
                )
            }
        }
    }
}
