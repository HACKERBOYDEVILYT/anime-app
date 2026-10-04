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

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val downloadId: String, // animeId_epNumber
    val animeId: String,
    val episodeNumber: Int,
    val animeTitle: String,
    val episodeTitle: String,
    val posterUrl: String,
    val videoUrl: String,
    val localFilePath: String,
    val fileSizeBytes: Long = 210000000L, // ~210 MB default
    val downloadedBytes: Long = 210000000L,
    val status: String = "COMPLETED", // DOWNLOADING, PAUSED, COMPLETED, FAILED
    val progressPercent: Int = 100,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "episode_comments")
data class EpisodeCommentEntity(
    @PrimaryKey
    val id: String,
    val animeId: String,
    val episodeNumber: Int,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val isSpoiler: Boolean = false,
    val fireReactions: Int = 12,
    val cryReactions: Int = 3,
    val shockReactions: Int = 8,
    val loveReactions: Int = 24,
    val userReaction: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mal_sync")
data class MalSyncEntity(
    @PrimaryKey
    val serviceName: String, // "MAL" or "ANILIST"
    val username: String,
    val isConnected: Boolean,
    val totalAnimeTracked: Int = 0,
    val totalEpisodesWatched: Int = 0,
    val meanScore: Float = 0.0f,
    val autoSyncEnabled: Boolean = true,
    val lastSyncedTimestamp: Long = 0L
)

@Entity(tableName = "scraped_videos")
data class ScrapedVideoEntity(
    @PrimaryKey
    val id: String,
    val animeId: String,
    val animeTitle: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val streamUrl: String,
    val qualityLabel: String = "1080p FHD",
    val isHls: Boolean = true,
    val isWebEmbed: Boolean = false,
    val subtitleUrl: String? = null,
    val subtitleLanguage: String? = "Bangla",
    val audioLanguage: String? = "Japanese [Original]",
    val serverSource: String = "Free Storage Server", // e.g., "HiAnime", "AnimeThemes", "Archive.org Storage", "YouTube Trailer"
    val status: String = "Online",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey
    val email: String,
    val userId: String,
    val username: String,
    val salt: String,
    val passwordHash: String,
    val avatarUrl: String,
    val tier: String = "Standard Member",
    val watchTimeHours: Float = 0f,
    val episodesWatched: Int = 0,
    val joinDate: String,
    val isActiveSession: Boolean = false
)

@Entity(tableName = "api_endpoints")
data class ApiEndpointEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val baseUrl: String,
    val category: String,
    val apiKey: String? = null,
    val isActive: Boolean = true,
    val status: String = "Online",
    val httpCode: Int = 200,
    val latencyMs: Long = 0L,
    val lastTested: String = "Not checked"
)


