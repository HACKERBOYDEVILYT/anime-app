package com.example.data.model

data class SubtitleStylePreferences(
    val fontSizeSp: Int = 16, // 12..26
    val textColorHex: String = "#FFEE00", // Yellow, White, Cyan, Green
    val backgroundColorHex: String = "#000000",
    val backgroundOpacity: Float = 0.75f, // 0f..1f
    val verticalPositionPercent: Int = 88, // 10..95 (from top)
    val subtitleDelayMs: Long = 0L // -5000L..+5000L
)

data class NotificationCategoryPreferences(
    val newEpisodes: Boolean = true,
    val watchlistUpdates: Boolean = true,
    val comments: Boolean = true,
    val replies: Boolean = true,
    val social: Boolean = true,
    val watchParty: Boolean = true,
    val achievements: Boolean = true,
    val system: Boolean = true,
    val marketing: Boolean = false
)

data class ContentControlPreferences(
    val blockedGenres: Set<String> = emptySet(),
    val blockedStudios: Set<String> = emptySet(),
    val blockedTags: Set<String> = emptySet(),
    val restrictMatureContent: Boolean = false,
    val spoilerFreeMode: Boolean = true
)

data class KidsModeConfig(
    val isKidsModeActive: Boolean = false,
    val pinCode: String = "1234",
    val maxAgeRating: Int = 13,
    val restrictSearch: Boolean = true
)

data class UserPreferences(
    val defaultQuality: String = "1080p", // "Auto", "1080p", "720p", "480p"
    val autoNextEpisode: Boolean = true,
    val autoPlay: Boolean = true,
    val skipIntro: Boolean = true,
    val skipOutro: Boolean = true,
    val preferredAudio: String = "Japanese [Original]",
    val preferredSubtitle: String = "English",
    val appLanguageCode: String = "en", // "en", "bn", "hi", "ar", "ja"
    val darkTheme: Boolean = true,
    val wifiOnlyDownloads: Boolean = true,
    val mobileDataDownloads: Boolean = false,
    val autoDeleteWatchedDownloads: Boolean = false,
    val downloadQuality: String = "1080p",
    val publicProfile: Boolean = true,
    val showWatchActivity: Boolean = true,
    val allowFollowers: Boolean = true,
    val subtitleStyle: SubtitleStylePreferences = SubtitleStylePreferences(),
    val notificationPrefs: NotificationCategoryPreferences = NotificationCategoryPreferences(),
    val contentControls: ContentControlPreferences = ContentControlPreferences(),
    val kidsMode: KidsModeConfig = KidsModeConfig()
)

enum class UserRole {
    SUPER_ADMIN,
    ADMIN,
    CONTENT_MANAGER,
    MODERATOR,
    SUPPORT,
    ANALYST,
    USER
}

data class DeviceSession(
    val sessionId: String,
    val userId: String,
    val deviceName: String,
    val deviceModel: String,
    val ipAddress: String,
    val location: String,
    val lastActiveLabel: String,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val isCurrentDevice: Boolean = false,
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
)

data class LoginHistoryItem(
    val id: String,
    val userId: String,
    val deviceName: String,
    val ipAddress: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // "SUCCESS", "FAILED", "SUSPICIOUS_CHALLENGED", "2FA_VERIFIED"
    val isSuspicious: Boolean = false,
    val suspiciousReason: String? = null
)

data class User(
    val id: String = "u_default_01",
    val username: String = "Robiul",
    val email: String = "ayanislam10000@gmail.com",
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
    val bio: String = "Anime enthusiast • 1080p Simulcast Streamer • Otaku",
    val tier: String = "Ultra VIP",
    val role: UserRole = UserRole.ADMIN,
    val joinDate: String = "Jan 2026",
    val episodesWatched: Int = 142,
    val watchTimeHours: Float = 56.8f,
    val completedAnimeCount: Int = 18,
    val xp: Int = 2850,
    val level: Int = 25,
    val titleRank: String = "Otaku",
    val watchStreakDays: Int = 12,
    val watchedToday: Boolean = true,
    val followersCount: Int = 128,
    val followingCount: Int = 46,
    val isEmailVerified: Boolean = true,
    val isTwoFactorEnabled: Boolean = false,
    val favoriteGenre: String = "Action",
    val favoriteStudio: String = "MAPPA",
    val favoriteCharacters: List<String> = listOf("Satoru Gojo", "Frieren", "Sung Jinwoo", "Monkey D. Luffy"),
    val preferences: UserPreferences = UserPreferences()
) {
    companion object {
        fun computeLevelFromXp(xp: Int): Int = (1 + (xp / 115)).coerceIn(1, 100)

        fun computeTitleFromLevel(level: Int): String = when {
            level >= 100 -> "Anime Master"
            level >= 50 -> "Elite Otaku"
            level >= 25 -> "Otaku"
            level >= 10 -> "Anime Fan"
            else -> "Newbie"
        }

        fun xpForNextLevel(level: Int): Int = (level * 115).coerceAtLeast(115)
    }
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val animeId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "EPISODE" // "EPISODE", "NEW_SEASON", "WATCHLIST", "RELEASE", "COMMENT_REPLY", "COMMENT_LIKE", "FOLLOW", "WATCH_PARTY", "ACHIEVEMENT", "CHALLENGE", "SYSTEM", "MARKETING"
)
