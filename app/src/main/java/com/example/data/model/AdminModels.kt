package com.example.data.model

data class DailyViewStat(
    val day: String,
    val views: Int
)

data class AdminStats(
    val totalUsers: Int = 18450,
    val activeUsers: Int = 4210,
    val totalAnime: Int = 124,
    val totalEpisodes: Int = 2890,
    val totalWatchSessions: Int = 94320,
    val totalWatchTimeHours: Long = 38400L,
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
    val reportsCount: Int = 0
)

data class AuditLog(
    val id: String,
    val adminName: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis()
)
