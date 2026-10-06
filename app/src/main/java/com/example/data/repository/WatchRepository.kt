package com.example.data.repository

import com.example.data.local.dao.ReviewDao
import com.example.data.local.dao.WatchHistoryDao
import com.example.data.local.dao.WatchlistDao
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WatchRepository(
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val reviewDao: ReviewDao,
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
        return watchlistDao.observeWatchlistItem(animeId).map { it?.toDomain() }
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
            addedAt = now
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
        val current = watchlistDao.observeWatchlistItem(animeId).firstOrNull()
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
            addedAt = current?.addedAt ?: now
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

    // Watch History & Continue Watching (with Smart Continue Watching remaining time support)
    fun getContinueWatching(): Flow<List<WatchHistoryItem>> {
        return watchHistoryDao.getRecentHistory(15).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getEpisodeProgress(animeId: String, episodeNumber: Int): WatchHistoryItem? = withContext(Dispatchers.IO) {
        watchHistoryDao.getEpisodeProgress(animeId, episodeNumber)?.toDomain()
    }

    suspend fun saveWatchProgress(
        animeId: String,
        animeTitle: String,
        episodeId: String,
        episodeNumber: Int,
        episodeTitle: String,
        thumbnailUrl: String,
        watchedPositionMs: Long,
        totalDurationMs: Long
    ) = withContext(Dispatchers.IO) {
        val safeDuration = if (totalDurationMs <= 0L) 1440_000L else totalDurationMs
        val now = System.currentTimeMillis()
        val entity = WatchHistoryEntity(
            id = "${animeId}_ep_$episodeNumber",
            animeId = animeId,
            animeTitle = animeTitle,
            episodeId = episodeId,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            thumbnailUrl = thumbnailUrl,
            watchedPositionMs = watchedPositionMs.coerceAtLeast(0L),
            totalDurationMs = safeDuration,
            lastWatchedAt = now
        )
        watchHistoryDao.saveProgress(entity)
        val remainingSec = ((safeDuration - watchedPositionMs).coerceAtLeast(0L)) / 1000L
        val mins = remainingSec / 60
        val secs = remainingSec % 60
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "CONTINUE_WATCHING",
            entityId = entity.id,
            operation = "UPSERT",
            payloadSummary = "$animeTitle Ep $episodeNumber (${String.format("%d:%02d", mins, secs)} remaining)",
            updatedAtEpochMs = now
        )
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        watchHistoryDao.clearAllHistory()
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "WATCH_HISTORY",
            entityId = "all",
            operation = "DELETE",
            payloadSummary = "Cleared watch history"
        )
    }

    // Reviews & Ratings
    fun getReviewsForAnime(animeId: String): Flow<List<Review>> {
        return reviewDao.getReviewsForAnime(animeId).map { list ->
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
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val now = System.currentTimeMillis()
        val reviewId = UUID.randomUUID().toString()
        val entity = ReviewEntity(
            id = reviewId,
            animeId = animeId,
            userName = userName,
            userAvatar = avatarUrl,
            rating = rating.coerceIn(1, 5),
            content = content.trim(),
            likesCount = 1,
            createdAt = dateFormat.format(Date(now))
        )
        reviewDao.insertReview(entity)
        cloudSyncManager?.enqueueIncrementalSync(
            entityType = "REVIEWS",
            entityId = reviewId,
            operation = "UPSERT",
            payloadSummary = "Rated $animeId ${rating}★ & posted review",
            updatedAtEpochMs = now
        )
    }

    suspend fun likeReview(reviewId: String) = withContext(Dispatchers.IO) {
        reviewDao.incrementReviewLikes(reviewId)
    }

    private fun WatchlistEntity.toDomain() = WatchlistItem(
        animeId = animeId,
        animeTitle = animeTitle,
        posterUrl = posterUrl,
        rating = rating,
        episodeCount = episodeCount,
        status = runCatching { WatchStatus.valueOf(status) }.getOrDefault(WatchStatus.PLAN_TO_WATCH),
        isFavorite = isFavorite,
        addedAt = addedAt
    )

    private fun WatchHistoryEntity.toDomain() = WatchHistoryItem(
        id = id,
        animeId = animeId,
        animeTitle = animeTitle,
        episodeId = episodeId,
        episodeNumber = episodeNumber,
        episodeTitle = episodeTitle,
        thumbnailUrl = thumbnailUrl,
        watchedPositionMs = watchedPositionMs,
        totalDurationMs = totalDurationMs,
        lastWatchedAt = lastWatchedAt
    )

    private fun ReviewEntity.toDomain() = Review(
        id = id,
        animeId = animeId,
        userName = userName,
        userAvatar = userAvatar,
        rating = rating,
        content = content,
        likesCount = likesCount,
        createdAt = createdAt
    )
}
