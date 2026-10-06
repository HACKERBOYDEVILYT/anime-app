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
import com.example.data.sync.CloudSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class WatchRepository(
    private val watchDao: WatchDao,
    private val watchlistDao: WatchlistDao,
    private val socialDao: SocialDao,
    private val cloudSyncManager: CloudSyncManager? = null
) {

    fun getAllWatchlist(): Flow<List<WatchlistItem>> {
        return watchlistDao.getAllWatchlist().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getWatchlistByStatus(status: WatchStatus): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlistByStatus(status.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getFavorites(): Flow<List<WatchlistItem>> {
        return watchlistDao.getFavorites().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun isAnimeInWatchlist(animeId: String): Flow<WatchlistItem?> {
        return watchlistDao.getWatchlistItem(animeId).map { it?.toDomain() }
    }

    suspend fun updateWatchlistStatus(
        animeId: String,
        animeTitle: String,
        posterUrl: String,
        rating: Float,
        episodeCount: Int,
        status: WatchStatus,
        isFavorite: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val entity = WatchlistEntity(
            animeId = animeId,
            animeTitle = animeTitle,
            posterUrl = posterUrl,
            rating = rating,
            episodeCount = episodeCount,
            status = status.name,
            isFavorite = isFavorite,
            updatedAt = now
        )
        watchlistDao.insertOrUpdate(entity)
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "WATCHLIST",
            entityId = animeId,
            operation = "UPSERT",
            payloadSummary = "$animeTitle → ${status.displayName}",
            updatedAtEpochMs = now
        )
    }

    suspend fun toggleFavorite(
        animeId: String,
        animeTitle: String,
        posterUrl: String,
        rating: Float,
        episodeCount: Int
    ) = withContext(Dispatchers.IO) {
        val current = watchlistDao.getWatchlistItemSync(animeId)
        val newFav = !(current?.isFavorite ?: false)
        val now = System.currentTimeMillis()
        val entity = WatchlistEntity(
            animeId = animeId,
            animeTitle = animeTitle,
            posterUrl = posterUrl,
            rating = rating,
            episodeCount = episodeCount,
            status = current?.status ?: WatchStatus.PLAN_TO_WATCH.name,
            isFavorite = newFav,
            updatedAt = now
        )
        watchlistDao.insertOrUpdate(entity)
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "FAVORITES",
            entityId = animeId,
            operation = if (newFav) "UPSERT" else "DELETE",
            payloadSummary = "$animeTitle favorite=$newFav",
            updatedAtEpochMs = now
        )
    }

    suspend fun removeFromWatchlist(animeId: String) = withContext(Dispatchers.IO) {
        watchlistDao.removeFromWatchlist(animeId)
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "WATCHLIST",
            entityId = animeId,
            operation = "DELETE",
            payloadSummary = "Removed $animeId from watchlist"
        )
    }

    // Watch History & Continue Watching
    fun getContinueWatching(): Flow<List<WatchHistoryItem>> {
        return watchDao.getContinueWatching().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllHistory(): Flow<List<WatchHistoryItem>> {
        return watchDao.getAllHistory().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getProgressForEpisode(episodeId: String): WatchHistoryItem? = withContext(Dispatchers.IO) {
        watchDao.getProgressForEpisode(episodeId)?.toDomain()
    }

    suspend fun getEpisodeProgress(animeId: String, episodeNumber: Int): WatchHistoryItem? = withContext(Dispatchers.IO) {
        val byId = watchDao.getProgressForEpisode("${animeId}_ep_$episodeNumber")
            ?: watchDao.getProgressForEpisode("${animeId}_$episodeNumber")
            ?: watchDao.getLastWatchedEpisodeForAnime(animeId)?.takeIf { it.episodeNumber == episodeNumber }
        byId?.toDomain()
    }

    suspend fun saveWatchProgress(
        animeId: String,
        animeTitle: String,
        episodeId: String,
        episodeNumber: Int,
        episodeTitle: String,
        thumbnailUrl: String = "",
        posterUrl: String = thumbnailUrl,
        watchedPositionMs: Long = 0L,
        progressMs: Long = watchedPositionMs,
        totalDurationMs: Long = 1440_000L,
        durationMs: Long = totalDurationMs
    ) = withContext(Dispatchers.IO) {
        val safeDuration = if (durationMs <= 0L) 1440_000L else durationMs
        val safeProgress = progressMs.coerceAtLeast(0L)
        val now = System.currentTimeMillis()
        val finalEpisodeId = episodeId.ifBlank { "${animeId}_ep_$episodeNumber" }
        val entity = WatchHistoryEntity(
            episodeId = finalEpisodeId,
            animeId = animeId,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            animeTitle = animeTitle,
            posterUrl = posterUrl.ifBlank { thumbnailUrl },
            progressMs = safeProgress,
            durationMs = safeDuration,
            lastWatchedAt = now
        )
        watchDao.saveProgress(entity)
        val remainingSec = ((safeDuration - safeProgress).coerceAtLeast(0L)) / 1000L
        val mins = remainingSec / 60
        val secs = remainingSec % 60
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "CONTINUE_WATCHING",
            entityId = finalEpisodeId,
            operation = "UPSERT",
            payloadSummary = "$animeTitle Ep $episodeNumber (${String.format("%d:%02d", mins, secs)} remaining)",
            updatedAtEpochMs = now
        )
    }

    suspend fun removeHistoryItem(episodeId: String) = withContext(Dispatchers.IO) {
        watchDao.deleteHistoryItem(episodeId)
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        watchDao.clearHistory()
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "WATCH_HISTORY",
            entityId = "all",
            operation = "DELETE",
            payloadSummary = "Cleared watch history"
        )
    }

    // Reviews & Ratings
    fun getReviewsForAnime(animeId: String): Flow<List<Review>> {
        return socialDao.getReviewsForAnime(animeId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun submitReview(
        animeId: String,
        rating: Int,
        content: String,
        userName: String = "Robiul",
        avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val reviewId = UUID.randomUUID().toString()
        val entity = ReviewEntity(
            id = reviewId,
            animeId = animeId,
            userId = "u_default_01",
            userName = userName,
            userAvatar = avatarUrl,
            rating = rating.coerceIn(1, 5),
            content = content.trim(),
            createdAt = now,
            likesCount = 1,
            hasLiked = true
        )
        socialDao.insertReview(entity)
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "REVIEWS",
            entityId = reviewId,
            operation = "UPSERT",
            payloadSummary = "Rated $animeId ${rating}★ & posted review",
            updatedAtEpochMs = now
        )
    }

    suspend fun likeReview(reviewId: String) = withContext(Dispatchers.IO) {
        socialDao.likeReview(reviewId)
    }

    // Notifications
    fun getNotifications(): Flow<List<NotificationEntity>> {
        return socialDao.getNotifications()
    }

    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {
        socialDao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        socialDao.markAllNotificationsAsRead()
    }

    private fun WatchlistEntity.toDomain() = WatchlistItem(
        animeId = animeId,
        animeTitle = animeTitle,
        posterUrl = posterUrl,
        rating = rating,
        episodeCount = episodeCount,
        status = runCatching { WatchStatus.valueOf(status) }.getOrDefault(WatchStatus.PLAN_TO_WATCH),
        isFavorite = isFavorite,
        updatedAt = updatedAt
    )

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
