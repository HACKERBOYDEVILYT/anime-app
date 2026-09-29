package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val episodeId: String,
    val animeId: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val animeTitle: String,
    val posterUrl: String,
    val progressMs: Long,
    val durationMs: Long,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val animeId: String,
    val animeTitle: String,
    val posterUrl: String,
    val rating: Float,
    val episodeCount: Int,
    val status: String, // WATCHING, PLAN_TO_WATCH, COMPLETED, ON_HOLD, DROPPED
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val animeId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val rating: Int,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val hasLiked: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,
    val animeId: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "EPISODE"
)
