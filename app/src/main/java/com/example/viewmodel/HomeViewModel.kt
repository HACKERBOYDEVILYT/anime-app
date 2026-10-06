package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.model.NotificationItem
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.data.repository.AiRecommendationEngine
import com.example.data.repository.AnimeRepository
import com.example.data.repository.GamificationAndSocialRepository
import com.example.data.repository.PersonalizedRecommendationsBundle
import com.example.data.repository.UserRepository
import com.example.data.repository.WatchRepository
import com.example.data.sync.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredAnime: Anime? = null,
    val bannerItems: List<Anime> = emptyList(),
    val trending: List<Anime> = emptyList(),
    val popular: List<Anime> = emptyList(),
    val seasonal: List<Anime> = emptyList(),
    val topRated: List<Anime> = emptyList(),
    val recentlyAdded: List<Anime> = emptyList(),
    val genres: List<String> = emptyList(),
    val recommendations: PersonalizedRecommendationsBundle = PersonalizedRecommendationsBundle(),
    val randomPickedAnime: Anime? = null,
    val rouletteSelectedAnime: Anime? = null,
    val selectedHeatmapPeriod: String = "LIVE",
    val error: String? = null
)

class HomeViewModel(
    private val animeRepository: AnimeRepository,
    private val watchRepository: WatchRepository,
    val userRepository: UserRepository? = null,
    val gamificationRepository: GamificationAndSocialRepository = GamificationAndSocialRepository(),
    val cloudSyncManager: CloudSyncManager = CloudSyncManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val continueWatching: StateFlow<List<WatchHistoryItem>> =
        watchRepository.getContinueWatching()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val favorites: StateFlow<List<WatchlistItem>> =
        watchRepository.getFavorites()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _notifications = MutableStateFlow(
        listOf(
            NotificationItem(
                id = "notif_ep_1",
                title = "🎬 New Episode Release: Frieren Ep 28",
                message = "Frieren: Beyond Journey's End Episode 28 is now streaming in 1080p HLS Sub & Dub.",
                timestamp = "10m ago",
                isRead = false,
                animeId = "anime_1"
            ),
            NotificationItem(
                id = "notif_dub_2",
                title = "🎙️ Dub Release: Solo Leveling English & Hindi Dub",
                message = "Episode 12 multi-audio tracks (Japanese, English, Hindi) are now live across all 17 servers.",
                timestamp = "1h ago",
                isRead = false,
                animeId = "anime_3"
            ),
            NotificationItem(
                id = "notif_season_3",
                title = "❄️ New Season Release: Jujutsu Kaisen Culling Game Preview",
                message = "Watch the new 1080p Season Preview & Character PV in the Anime Clips Center.",
                timestamp = "3h ago",
                isRead = false,
                animeId = "anime_2"
            ),
            NotificationItem(
                id = "notif_watchlist_4",
                title = "❤️ Watchlist Update: Demon Slayer Hashira Training",
                message = "A series in your Watchlist just received 1080p fMP4 Backup Mirrors.",
                timestamp = "5h ago",
                isRead = true,
                animeId = "anime_4"
            ),
            NotificationItem(
                id = "notif_reply_5",
                title = "💬 Comment Reply from @AkiraVortex",
                message = "Replied to your comment on Episode 1: 'Totally agree, that ending sakuga was legendary!'",
                timestamp = "Yesterday",
                isRead = true,
                animeId = "anime_1"
            ),
            NotificationItem(
                id = "notif_rec_6",
                title = "🤖 AI Pick For You: Cyberpunk: Edgerunners",
                message = "98% Match based on your high completion rate in Action & Sci-Fi anime.",
                timestamp = "Yesterday",
                isRead = true,
                animeId = "anime_5"
            ),
            NotificationItem(
                id = "notif_admin_7",
                title = "📢 Admin Announcement: 17-Server Auto-Failover Active",
                message = "All 17 global HLS/MP4 edge servers and Cloud Sync across 13 domains are verified online.",
                timestamp = "2d ago",
                isRead = true,
                animeId = null
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        loadHomeData()
        observeRecommendationsDynamic()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val featured = animeRepository.getFeaturedAnime()
                val trending = animeRepository.getTrending()
                val popular = animeRepository.getPopular()
                val seasonal = animeRepository.getSeasonal()
                val topRated = animeRepository.getTopRated()
                val recentlyAdded = animeRepository.getRecentlyAdded()
                val genres = animeRepository.getGenres()
                val allCatalog = (trending + popular + seasonal + topRated + recentlyAdded).distinctBy { it.id }

                val history = watchRepository.getContinueWatching().firstOrNull().orEmpty()
                val watchlist = watchRepository.getAllWatchlist().firstOrNull().orEmpty()
                val prefs = userRepository?.preferences?.value ?: com.example.data.model.UserPreferences()
                val aiRecs = AiRecommendationEngine.generateRecommendations(
                    catalog = allCatalog,
                    watchHistory = history,
                    watchlist = watchlist,
                    preferences = prefs
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        featuredAnime = featured,
                        bannerItems = trending.take(5),
                        trending = trending,
                        popular = popular,
                        seasonal = seasonal,
                        topRated = topRated,
                        recentlyAdded = recentlyAdded,
                        genres = genres,
                        recommendations = aiRecs,
                        randomPickedAnime = allCatalog.firstOrNull(),
                        rouletteSelectedAnime = allCatalog.lastOrNull()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load catalog"
                    )
                }
            }
        }
    }

    private fun observeRecommendationsDynamic() {
        viewModelScope.launch {
            combine(
                watchRepository.getContinueWatching(),
                watchRepository.getAllWatchlist()
            ) { history, watchlist ->
                val state = _uiState.value
                val catalog = (state.trending + state.popular + state.seasonal + state.topRated).distinctBy { it.id }
                if (catalog.isNotEmpty()) {
                    val prefs = userRepository?.preferences?.value ?: com.example.data.model.UserPreferences()
                    AiRecommendationEngine.generateRecommendations(
                        catalog = catalog,
                        watchHistory = history,
                        watchlist = watchlist,
                        preferences = prefs
                    )
                } else null
            }.collect { updatedRecs ->
                if (updatedRecs != null) {
                    _uiState.update { it.copy(recommendations = updatedRecs) }
                }
            }
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
                status = WatchStatus.PLAN_TO_WATCH
            )
            userRepository?.awardUserXp(10, "Added ${anime.titleEnglish} to Watchlist")
        }
    }

    fun toggleWatchlistQuick(anime: Anime) = toggleWatchlist(anime)

    fun removeContinueWatching(item: WatchHistoryItem) {
        viewModelScope.launch {
            watchRepository.clearWatchHistory()
        }
    }

    fun markNotificationRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun deleteNotification(id: String) {
        _notifications.update { list ->
            list.filterNot { it.id == id }
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun pushBroadcastNotification(title: String, message: String) {
        val newNotif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            title = title,
            message = message,
            timestamp = "Just now",
            isRead = false,
            animeId = null
        )
        _notifications.update { listOf(newNotif) + it }
    }

    fun pickRandomAnime(genreFilter: String? = null) {
        val state = _uiState.value
        val catalog = (state.trending + state.popular + state.seasonal + state.topRated).distinctBy { it.id }
        val pool = if (genreFilter.isNullOrBlank() || genreFilter == "All") {
            catalog
        } else {
            catalog.filter { anime -> anime.genres.any { it.equals(genreFilter, ignoreCase = true) } }.ifEmpty { catalog }
        }
        if (pool.isNotEmpty()) {
            _uiState.update { it.copy(randomPickedAnime = pool.random()) }
        }
    }

    fun spinAnimeRoulette(): Anime? {
        val state = _uiState.value
        val catalog = (state.trending + state.popular + state.seasonal + state.topRated).distinctBy { it.id }
        val chosen = catalog.shuffled().firstOrNull()
        _uiState.update { it.copy(rouletteSelectedAnime = chosen) }
        return chosen
    }

    fun setHeatmapPeriod(period: String) {
        _uiState.update { it.copy(selectedHeatmapPeriod = period) }
    }
}
