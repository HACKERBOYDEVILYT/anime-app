package com.example.data.model

data class UserPreferences(
    val defaultQuality: String = "1080p", // "Auto", "1080p", "720p", "480p"
    val autoNextEpisode: Boolean = true,
    val autoPlay: Boolean = true,
    val skipIntro: Boolean = true,
    val preferredAudio: String = "Japanese [Original]",
    val preferredSubtitle: String = "English",
    val darkTheme: Boolean = true
)

enum class UserRole {
    SUPER_ADMIN,
    ADMIN,
    CONTENT_MANAGER,
    MODERATOR,
    USER
}

data class User(
    val id: String = "u_default_01",
    val username: String = "OtakuStreamer",
    val email: String = "ayanislam10000@gmail.com",
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
    val tier: String = "Ultra VIP",
    val role: UserRole = UserRole.ADMIN, // Admin enabled for platform control
    val joinDate: String = "Jan 2026",
    val episodesWatched: Int = 142,
    val watchTimeHours: Float = 56.8f,
    val preferences: UserPreferences = UserPreferences()
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val animeId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "EPISODE" // "EPISODE", "WATCHLIST", "ANNOUNCEMENT"
)
