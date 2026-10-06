package com.example.data.model

data class SubtitleStylePreferences(
    val fontSizeSp: Int = 16, // 12..26
    val textColorHex: String = "#FFEE00", // Yellow, White, Cyan, Green
    val backgroundColorHex: String = "#000000",
    val backgroundOpacity: Float = 0.75f, // 0f..1f
    val verticalPositionPercent: Int = 88, // 10..95 (from top)
    val bottomMarginDp: Int = 34,
    val bottomPaddingDp: Int = bottomMarginDp,
    val subtitleDelayMs: Long = 0L, // -5000L..+5000L
    val delayMs: Long = subtitleDelayMs
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
    val spoilerFreeMode: Boolean = false
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
    val autoPlayNext: Boolean = true,
    val autoPlay: Boolean = true,
    val skipIntro: Boolean = true,
    val autoSkipIntro: Boolean = true,
    val skipOutro: Boolean = true,
    val autoSkipOutro: Boolean = true,
    val backgroundPlayback: Boolean = false,
    val backgroundPlaybackEnabled: Boolean = backgroundPlayback,
    val preferDub: Boolean = false,
    val preferredAudio: String = "Japanese [Original]",
    val preferredAudioLanguage: String = "Japanese",
    val preferredSubtitle: String = "English",
    val preferredSubtitleLanguage: String = preferredSubtitle,
    val preferredAnimeType: String = "TV",
    val preferredEpisodeLengthMinutes: Int = 24,
    val appLanguageCode: String = "en", // "en", "bn", "hi", "ar", "ja"
    val darkTheme: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val spoilerFreeMode: Boolean = false,
    val kidsModeEnabled: Boolean = false,
    val kidsModePin: String = "1234",
    val matureContentRestricted: Boolean = false,
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
    val userId: String = "u_default_01",
    val deviceName: String,
    val deviceModel: String = "Android",
    val platform: String = "Android 14",
    val ipAddress: String = "103.112.44.18",
    val location: String = "Dhaka, BD",
    val locationOrIp: String = "Dhaka, BD • 103.112.44.18",
    val lastActiveLabel: String = "Active now",
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val lastActiveEpochMs: Long = System.currentTimeMillis(),
    val isCurrentDevice: Boolean = false,
    val refreshTokenGeneration: Int = 1,
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
)

data class LoginHistoryItem(
    val id: String,
    val userId: String = "u_default_01",
    val deviceName: String,
    val timestampLabel: String = "Just now",
    val ipAddress: String = "103.112.44.18",
    val location: String = "Dhaka, BD",
    val locationOrIp: String = "103.112.44.18",
    val authMethod: String = "Email + Password",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // "SUCCESS", "FAILED", "SUSPICIOUS_CHALLENGED", "2FA_VERIFIED"
    val statusText: String = "Success",
    val isSuspicious: Boolean = false,
    val suspiciousReason: String? = null
) {
    val statusNote: String get() = statusText
}

data class User(
    val id: String = "u_default_01",
    val username: String = "Robiul",
    val email: String = "ayanislam10000@gmail.com",
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
    val bio: String = "Anime enthusiast • 1080p Simulcast Streamer • Otaku",
    val tier: String = "Ultra VIP",
    val role: UserRole = UserRole.SUPER_ADMIN,
    val isLoggedIn: Boolean = true,
    val emailVerified: Boolean = true,
    val isEmailVerified: Boolean = emailVerified,
    val twoFactorEnabled: Boolean = true,
    val isTwoFactorEnabled: Boolean = twoFactorEnabled,
    val authProvider: String = "EMAIL_AND_GOOGLE",
    val memberSince: String = "Jan 2026",
    val joinDate: String = memberSince,
    val episodesWatched: Int = 142,
    val hoursWatched: Float = 56.8f,
    val watchTimeHours: Float = hoursWatched,
    val completedAnimeCount: Int = 18,
    val meanScore: Float = 9.2f,
    val reviewsCount: Int = 14,
    val favoritesCount: Int = 9,
    val xp: Int = 2850,
    val level: Int = 25,
    val titleRank: String = "Otaku",
    val watchStreakDays: Int = 12,
    val watchedToday: Boolean = true,
    val lastWatchedDateIso: String = "2026-10-06",
    val followersCount: Int = 128,
    val followingCount: Int = 46,
    val favoriteGenre: String = "Action",
    val favoriteStudio: String = "MAPPA",
    val favoriteCharacters: List<String> = listOf("Satoru Gojo", "Frieren", "Sung Jinwoo", "Monkey D. Luffy"),
    val badges: List<String> = listOf(
        "First Anime 🎬",
        "100 Episodes 🔥",
        "Shounen Master ⚔️",
        "Action Expert 💥",
        "Night Owl 🦉",
        "Weekend Warrior 🛡️"
    ),
    val activeSessions: List<DeviceSession> = emptyList(),
    val loginHistory: List<LoginHistoryItem> = emptyList(),
    val preferences: UserPreferences = UserPreferences()
) {
    val userTitle: String get() = titleRank
    val currentStreakDays: Int get() = watchStreakDays
    val xpPoints: Int get() = xp
    val episodesWatchedCount: Int get() = episodesWatched
    val completedCount: Int get() = completedAnimeCount

    companion object {
        fun computeLevelFromXp(xp: Int): Int = (1 + (xp / 115)).coerceIn(1, 100)
        fun calculateLevel(xp: Int): Int = computeLevelFromXp(xp)

        fun computeTitleFromLevel(level: Int): String = when {
            level >= 100 -> "Anime Master"
            level >= 50 -> "Elite Otaku"
            level >= 25 -> "Otaku"
            level >= 10 -> "Anime Fan"
            else -> "Newbie"
        }
        fun calculateTitleForLevel(level: Int): String = computeTitleFromLevel(level)

        fun xpForNextLevel(level: Int): Int = (level * 115).coerceAtLeast(115)
    }
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val animeId: String? = null,
    val timestamp: String = "Just now",
    val timestampEpochMs: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "EPISODE"
)
