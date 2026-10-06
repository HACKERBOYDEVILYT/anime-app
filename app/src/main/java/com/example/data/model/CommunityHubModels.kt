package com.example.data.model

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val xpReward: Int,
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val targetProgress: Int,
    val category: String // "WATCH", "SOCIAL", "GENRE", "STREAK", "MASTER"
) {
    val progressFraction: Float
        get() = if (targetProgress > 0) (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f) else 0f
}

data class ChallengeQuest(
    val id: String,
    val title: String,
    val description: String,
    val questType: String, // "DAILY", "WEEKLY", "MONTHLY"
    val currentCount: Int,
    val targetCount: Int,
    val xpReward: Int,
    val badgeReward: String? = null,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
) {
    val progressFraction: Float
        get() = if (targetCount > 0) (currentCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f) else 0f
}

data class SeasonalEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val themeColorHex: String,
    val bannerEmoji: String,
    val activePeriod: String,
    val xpMultiplier: Float,
    val specialBadgeTitle: String,
    val currentPoints: Int,
    val targetPoints: Int,
    val isJoined: Boolean = true
)

data class SocialUserProfile(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val titleRank: String,
    val level: Int,
    val xp: Int,
    val episodesWatched: Int,
    val reviewsCount: Int,
    val favoriteAnime: String,
    val isFollowing: Boolean = false,
    val isFollower: Boolean = false
)

data class SocialActivityItem(
    val id: String,
    val userId: String,
    val username: String,
    val userAvatar: String,
    val actionText: String, // e.g., "completed Naruto", "rated One Piece 5 stars", "added Bleach to favorites"
    val animeId: String? = null,
    val animeTitle: String = "",
    val ratingStars: Int? = null,
    val timestampLabel: String = "Just now",
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val titleRank: String,
    val level: Int,
    val metricValue: String,
    val category: String // "TOP_WATCHERS", "TOP_REVIEWERS", "TOP_CONTRIBUTORS", "WEEKLY_XP", "MONTHLY_XP"
)

data class CharacterProfile(
    val id: String,
    val name: String,
    val japaneseName: String,
    val animeId: String,
    val animeTitle: String,
    val role: String, // "Main", "Supporting", "Antagonist"
    val avatarUrl: String,
    val voiceActor: String,
    val voiceActorLanguage: String = "Japanese",
    val bio: String,
    val relationships: List<String>, // e.g. "Suguru Geto (Best Friend)", "Yuji Itadori (Student)"
    val likesCount: Int = 1420,
    val isFavorite: Boolean = false
)

data class StudioStaffProfile(
    val id: String,
    val studioName: String,
    val foundedYear: Int,
    val headquarters: String,
    val director: String,
    val headWriter: String,
    val leadAnimator: String,
    val featuredVoiceActors: List<String>,
    val notableWorks: List<String>,
    val averageScore: Float,
    val logoUrl: String
)

data class AnimeClipItem(
    val id: String,
    val animeId: String,
    val animeTitle: String,
    val title: String,
    val category: String, // "Trailer", "Teaser", "Opening", "Ending", "Character PV", "News Clip"
    val durationLabel: String,
    val thumbnailUrl: String,
    val streamUrl: String,
    val viewsLabel: String = "142K views"
)

data class AnimeNewsItem(
    val id: String,
    val title: String,
    val category: String, // "New Anime", "Season Announcement", "Release News", "Studio News", "Movie Announcement"
    val summary: String,
    val imageUrl: String,
    val publishedTime: String,
    val relatedAnimeId: String? = null
)

data class PollOption(
    val id: String,
    val label: String,
    val votes: Int
)

data class CommunityPoll(
    val id: String,
    val category: String, // "Best Anime", "Best Character", "Best Opening", "Best Fight", "Weekly Poll", "Monthly Poll"
    val question: String,
    val options: List<PollOption>,
    val selectedOptionId: String? = null
) {
    val totalVotes: Int
        get() = options.sumOf { it.votes }.coerceAtLeast(1)
}

data class AnimeBattle(
    val id: String,
    val title: String,
    val leftFighterName: String,
    val leftFighterAnime: String,
    val leftFighterAvatar: String,
    val leftVotes: Int,
    val rightFighterName: String,
    val rightFighterAnime: String,
    val rightFighterAvatar: String,
    val rightVotes: Int,
    val userVotedSide: String? = null // "LEFT", "RIGHT", or null
) {
    val totalVotes: Int
        get() = (leftVotes + rightVotes).coerceAtLeast(1)
    val leftPercent: Int
        get() = ((leftVotes * 100f) / totalVotes).toInt().coerceIn(0, 100)
    val rightPercent: Int
        get() = (100 - leftPercent).coerceIn(0, 100)
}

data class WallpaperItem(
    val id: String,
    val title: String,
    val animeTitle: String,
    val category: String, // "Lock Screen", "Home Screen", "Character Wallpaper"
    val resolution: String = "4K UHD (2160x3840)",
    val imageUrl: String,
    val downloadsCount: Int = 850,
    val isFavorite: Boolean = false
)

data class TriviaQuestion(
    val id: String,
    val category: String, // "Guess the Character", "Guess the Anime", "Opening Quiz", "Emoji Anime Quiz", "Daily Trivia"
    val promptText: String,
    val hintOrEmoji: String,
    val imageUrl: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val xpReward: Int = 25
)

data class EpisodeNote(
    val id: String,
    val animeId: String,
    val episodeNumber: Int,
    val noteText: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class VideoBookmark(
    val id: String,
    val animeId: String,
    val episodeNumber: Int,
    val positionMs: Long,
    val label: String, // e.g., "Best fight", "Important scene"
    val createdAt: Long = System.currentTimeMillis()
) {
    val timestampMs: Long get() = positionMs
    val formattedTimestamp: String
        get() {
            val totalSeconds = (positionMs / 1000L).coerceAtLeast(0L)
            val mins = totalSeconds / 60
            val secs = totalSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}

data class WatchQueueItem(
    val orderIndex: Int,
    val animeId: String,
    val animeTitle: String,
    val posterUrl: String,
    val nextEpisodeNumber: Int = 1,
    val totalEpisodes: Int = 12,
    val studio: String = ""
)

data class WatchlistCollection(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val animeIds: List<String>,
    val isCustom: Boolean = false
)

data class TrendingHeatItem(
    val rank: Int,
    val anime: Anime,
    val activeViewers: Int,
    val heatScorePercent: Int, // 1..100
    val period: String // "LIVE", "24H", "7D", "30D"
)
