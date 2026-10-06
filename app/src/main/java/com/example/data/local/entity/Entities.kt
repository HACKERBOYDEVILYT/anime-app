package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

typealias DownloadItemEntity = DownloadEntity

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
    val status: String = "COMPLETED", // DOWNLOADING, PAUSED, COMPLETED, FAILED, WAITING
    val progressPercent: Int = 100,
    val createdAt: Long = System.currentTimeMillis()
) {
    val id: String
        get() = downloadId

    val thumbnailUrl: String
        get() = posterUrl

    val streamUrl: String
        get() = videoUrl

    val quality: String
        get() = when {
            fileSizeBytes >= 300_000_000L -> "1080p"
            fileSizeBytes >= 180_000_000L -> "720p"
            fileSizeBytes >= 120_000_000L -> "480p"
            else -> "360p"
        }

    val sizeMb: Int
        get() = (fileSizeBytes / (1024L * 1024L)).toInt().coerceAtLeast(85)

    val timestamp: Long
        get() = createdAt
}

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
    val serverSource: String = "Free Storage Server",
    val status: String = "Online",
    val createdAt: Long = System.currentTimeMillis()
) {
    val quality: String
        get() = qualityLabel

    val sourceProvider: String
        get() = serverSource
}

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

@Entity(tableName = "admob_config")
data class AdMobConfigEntity(
    @PrimaryKey
    val id: String = "primary_admob_account",
    val accountEmail: String = "",
    val publisherId: String = "pub-3940256099942544",
    val appId: String = "ca-app-pub-3940256099942544~3347511713",
    val bannerAdUnitId: String = "ca-app-pub-3940256099942544/6300978111",
    val interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712",
    val rewardedAdUnitId: String = "ca-app-pub-3940256099942544/5224354917",
    val nativeAdUnitId: String = "ca-app-pub-3940256099942544/2247696110",
    val adsEnabled: Boolean = true,
    val bannerAdsEnabled: Boolean = true,
    val interstitialAdsEnabled: Boolean = true,
    val rewardedAdsEnabled: Boolean = true,
    val testModeEnabled: Boolean = true,
    val impressionsCount: Int = 0,
    val clicksCount: Int = 0,
    val estimatedRevenueUsd: Double = 0.0,
    val accountStatus: String = "Connected (Google AdMob)",
    val updatedAt: Long = System.currentTimeMillis()
)
