package com.example.data.model

data class DailyViewStat(
    val day: String,
    val views: Int
)

data class MonthlyMetric(
    val month: String,
    val watchHours: Float,
    val episodesCount: Int
)

data class AdminStats(
    val totalUsers: Int = 18450,
    val activeUsers: Int = 4210,
    val dau: Int = 4210,
    val wau: Int = 11840,
    val mau: Int = 18450,
    val retentionRatePercent: Float = 78.4f,
    val newUsersToday: Int = 342,
    val totalAnime: Int = 124,
    val totalEpisodes: Int = 2890,
    val totalWatchSessions: Int = 94320,
    val totalWatchTimeHours: Long = 38400L,
    val totalDownloads: Int = 15640,
    val activeStreamsCount: Int = 17,
    val streamErrorsCount: Int = 0,
    val playbackFailuresCount: Int = 0,
    val averageServerLatencyMs: Long = 29L,
    val engagementScorePercent: Float = 94.2f,
    val estimatedMonthlyRevenueUsd: Double = 4280.50,
    val mostWatchedAnime: List<Pair<String, Int>> = listOf(
        "Frieren: Beyond Journey's End" to 28400,
        "Jujutsu Kaisen Season 2" to 25100,
        "Solo Leveling" to 22900,
        "One Piece" to 21400,
        "Demon Slayer: Hashira Training" to 18750
    ),
    val mostCompletedAnime: List<Pair<String, Int>> = listOf(
        "Frieren: Beyond Journey's End" to 19200,
        "Solo Leveling" to 17800,
        "Cyberpunk: Edgerunners" to 16400
    ),
    val mostDroppedAnime: List<Pair<String, Int>> = listOf(
        "Mecha Chronicles" to 640,
        "Filler Arc Special" to 510
    ),
    val mostSkippedAnime: List<Pair<String, Int>> = listOf(
        "Recap Special Episode" to 1420,
        "Preview Digest" to 980
    ),
    val mostActiveHours: List<Pair<String, Int>> = listOf(
        "20:00 - 22:00" to 38,
        "22:00 - 00:00" to 31,
        "18:00 - 20:00" to 19,
        "14:00 - 18:00" to 12
    ),
    val geographicStats: Map<String, Int> = mapOf(
        "Bangladesh" to 38,
        "India" to 24,
        "United States" to 16,
        "Japan" to 12,
        "Global / Other" to 10
    ),
    val dailyViews: List<DailyViewStat> = listOf(
        DailyViewStat("Mon", 14200),
        DailyViewStat("Tue", 15800),
        DailyViewStat("Wed", 17400),
        DailyViewStat("Thu", 16900),
        DailyViewStat("Fri", 22500),
        DailyViewStat("Sat", 28900),
        DailyViewStat("Sun", 26400)
    ),
    val deviceStats: Map<String, Int> = mapOf(
        "Android Mobile" to 58,
        "Android TV / Tablet" to 24,
        "Desktop Web" to 18
    )
)

data class VideoJob(
    val id: String,
    val animeTitle: String,
    val episodeNumber: Int,
    val sourceResolution: String,
    val status: String, // "Queued", "Processing", "Completed", "Failed"
    val progressPercent: Int,
    val hlsStreamReady: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

data class ModeratedUser(
    val id: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val status: String, // "Active", "Warned", "Banned"
    val reportsCount: Int = 0,
    val activeSessionsCount: Int = 1,
    val lastActivity: String = "Watched Frieren Ep 1 • 4m ago"
)

data class AuditLog(
    val id: String,
    val adminName: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis()
)
