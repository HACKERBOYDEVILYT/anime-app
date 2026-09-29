package com.example.data.model

enum class WatchStatus(val displayName: String) {
    WATCHING("Watching"),
    PLAN_TO_WATCH("Plan to Watch"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped")
}

data class WatchlistItem(
    val animeId: String,
    val animeTitle: String,
    val posterUrl: String,
    val rating: Float,
    val episodeCount: Int,
    val status: WatchStatus = WatchStatus.WATCHING,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class WatchHistoryItem(
    val animeId: String,
    val episodeId: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val animeTitle: String,
    val posterUrl: String,
    val progressMs: Long,
    val durationMs: Long,
    val lastWatchedAt: Long = System.currentTimeMillis()
) {
    val percentage: Float
        get() = if (durationMs > 0) (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val remainingMinutes: Int
        get() = (((durationMs - progressMs) / 1000) / 60).toInt().coerceAtLeast(0)
}

data class Review(
    val id: String,
    val animeId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val rating: Int, // 1 to 5 stars
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val hasLiked: Boolean = false
)

data class Comment(
    val id: String,
    val episodeId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val hasLiked: Boolean = false
)
