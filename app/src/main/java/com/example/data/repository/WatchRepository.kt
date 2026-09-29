package com.example.data.repository

import com.example.data.local.dao.SocialDao
import com.example.data.local.dao.WatchDao
import com.example.data.local.dao.WatchlistDao
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.WatchHistoryEntity
import com.example.data.local.entity.WatchlistEntity
import com.example.data.model.Review
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WatchRepository(
    private val watchDao: WatchDao,
    private val watchlistDao: WatchlistDao,
    private val socialDao: SocialDao
) {
    val continueWatching: Flow<List<WatchHistoryItem>> = watchDao.getContinueWatching().map { list ->
        list.map { it.toDomain() }
    }

    val watchHistory: Flow<List<WatchHistoryItem>> = watchDao.getAllHistory().map { list ->
        list.map { it.toDomain() }
    }

    val watchlist: Flow<List<WatchlistItem>> = watchlistDao.getAllWatchlist().map { list ->
        list.map { it.toDomain() }
    }

    val favorites: Flow<List<WatchlistItem>> = watchlistDao.getFavorites().map { list ->
        list.map { it.toDomain() }
    }

    fun getWatchlistByStatus(status: WatchStatus): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlistByStatus(status.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun isAnimeInWatchlist(animeId: String): Flow<WatchlistItem?> {
        return watchlistDao.getWatchlistItem(animeId).map { it?.toDomain() }
    }

    suspend fun getEpisodeProgress(episodeId: String): WatchHistoryEntity? {
        return watchDao.getProgressForEpisode(episodeId)
    }

    suspend fun saveWatchProgress(
        animeId: String,
        episodeId: String,
        episodeNumber: Int,
        episodeTitle: String,
        animeTitle: String,
        posterUrl: String,
        progressMs: Long,
        durationMs: Long
    ) {
        val entity = WatchHistoryEntity(
            episodeId = episodeId,
            animeId = animeId,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            animeTitle = animeTitle,
            posterUrl = posterUrl,
            progressMs = progressMs,
            durationMs = durationMs,
            lastWatchedAt = System.currentTimeMillis()
        )
        watchDao.saveProgress(entity)
    }

    suspend fun updateWatchlistStatus(
        animeId: String,
        animeTitle: String,
        posterUrl: String,
        rating: Float,
        episodeCount: Int,
        status: WatchStatus,
        isFavorite: Boolean
    ) {
        val entity = WatchlistEntity(
            animeId = animeId,
            animeTitle = animeTitle,
            posterUrl = posterUrl,
            rating = rating,
            episodeCount = episodeCount,
            status = status.name,
            isFavorite = isFavorite,
            updatedAt = System.currentTimeMillis()
        )
        watchlistDao.insertOrUpdate(entity)
    }

    suspend fun toggleFavorite(
        animeId: String,
        animeTitle: String,
        posterUrl: String,
        rating: Float,
        episodeCount: Int
    ) {
        val current = watchlistDao.getWatchlistItemSync(animeId)
        val isFav = current?.isFavorite == true
        val entity = WatchlistEntity(
            animeId = animeId,
            animeTitle = animeTitle,
            posterUrl = posterUrl,
            rating = rating,
            episodeCount = episodeCount,
            status = current?.status ?: WatchStatus.WATCHING.name,
            isFavorite = !isFav,
            updatedAt = System.currentTimeMillis()
        )
        watchlistDao.insertOrUpdate(entity)
    }

    suspend fun removeFromWatchlist(animeId: String) {
        watchlistDao.removeFromWatchlist(animeId)
    }

    suspend fun deleteHistoryItem(episodeId: String) {
        watchDao.deleteHistoryItem(episodeId)
    }

    suspend fun clearHistory() {
        watchDao.clearHistory()
    }

    fun getReviewsForAnime(animeId: String): Flow<List<Review>> {
        return socialDao.getReviewsForAnime(animeId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun submitReview(animeId: String, rating: Int, content: String, userName: String, avatarUrl: String) {
        val review = ReviewEntity(
            id = "rev_${System.currentTimeMillis()}",
            animeId = animeId,
            userId = "user_me",
            userName = userName,
            userAvatar = avatarUrl,
            rating = rating,
            content = content
        )
        socialDao.insertReview(review)
    }

    suspend fun likeReview(id: String) {
        socialDao.likeReview(id)
    }

    fun getNotifications(): Flow<List<NotificationEntity>> {
        return socialDao.getNotifications()
    }

    suspend fun markNotificationRead(id: String) {
        socialDao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead() {
        socialDao.markAllNotificationsAsRead()
    }

    suspend fun seedInitialNotificationsIfEmpty() {
        // Sample notifications for realistic streaming platform
        socialDao.insertNotification(
            NotificationEntity(
                id = "notif_1",
                title = "New Episode Released!",
                message = "Solo Leveling Episode 12 is now streaming in 4K HDR.",
                animeId = "anime_3",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45
            )
        )
        socialDao.insertNotification(
            NotificationEntity(
                id = "notif_2",
                title = "Frieren Season Finale Available",
                message = "The journey reaches its winter checkpoint. Stream all 28 episodes now.",
                animeId = "anime_1",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5
            )
        )
        socialDao.insertNotification(
            NotificationEntity(
                id = "notif_3",
                title = "Weekly Anime Ranking Updated",
                message = "Check out the top 10 community trending anime of the week.",
                animeId = null,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24
            )
        )
    }

    private fun WatchHistoryEntity.toDomain() = WatchHistoryItem(
        animeId = animeId,
        episodeId = episodeId,
        episodeNumber = episodeNumber,
        episodeTitle = episodeTitle,
        animeTitle = animeTitle,
        posterUrl = posterUrl,
        progressMs = progressMs,
        durationMs = durationMs,
        lastWatchedAt = lastWatchedAt
    )

    private fun WatchlistEntity.toDomain() = WatchlistItem(
        animeId = animeId,
        animeTitle = animeTitle,
        posterUrl = posterUrl,
        rating = rating,
        episodeCount = episodeCount,
        status = runCatching { WatchStatus.valueOf(status) }.getOrDefault(WatchStatus.WATCHING),
        isFavorite = isFavorite,
        updatedAt = updatedAt
    )

    private fun ReviewEntity.toDomain() = Review(
        id = id,
        animeId = animeId,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        rating = rating,
        content = content,
        createdAt = createdAt,
        likesCount = likesCount,
        hasLiked = hasLiked
    )
}
