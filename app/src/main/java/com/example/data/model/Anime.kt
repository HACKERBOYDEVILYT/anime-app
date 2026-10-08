package com.example.data.model

enum class AnimeType(val displayName: String) {
    TV("TV Series"),
    MOVIE("Movie"),
    OVA("OVA"),
    ONA("ONA"),
    SPECIAL("Special"),
    MUSIC("Music");

    companion object {
        fun fromApiString(raw: String?): AnimeType {
            return when (raw?.trim()?.uppercase()) {
                "TV", "TV_SHORT" -> TV
                "MOVIE" -> MOVIE
                "OVA" -> OVA
                "ONA" -> ONA
                "SPECIAL", "TV SPECIAL", "TV_SPECIAL", "CM", "PV" -> SPECIAL
                "MUSIC" -> MUSIC
                else -> TV
            }
        }
    }
}

enum class AnimeStatus(val displayName: String) {
    RELEASING("Ongoing"),
    FINISHED("Completed"),
    NOT_YET_RELEASED("Upcoming");

    companion object {
        fun fromApiString(raw: String?, isAiring: Boolean = false): AnimeStatus {
            if (isAiring) return RELEASING
            val normalized = raw?.trim()?.uppercase() ?: return FINISHED
            return when {
                normalized.contains("AIRING") && !normalized.contains("FINISHED") && !normalized.contains("NOT YET") -> RELEASING
                normalized == "RELEASING" || normalized == "ONGOING" || normalized == "HIATUS" -> RELEASING
                normalized.contains("NOT YET") || normalized == "NOT_YET_RELEASED" || normalized == "UPCOMING" || normalized == "UNRELEASED" -> NOT_YET_RELEASED
                else -> FINISHED
            }
        }
    }
}

enum class AnimeSortOption(val displayName: String) {
    POPULARITY("Most Popular"),
    RATING("Highest Rated"),
    NEWEST("Newest Releases"),
    TITLE_AZ("Title (A-Z)")
}

data class AnimeCharacter(
    val name: String,
    val role: String,
    val avatarUrl: String,
    val voiceActor: String = ""
)

data class Anime(
    val id: String,
    val slug: String,
    val titleEnglish: String,
    val titleRomaji: String,
    val titleJapanese: String,
    val description: String,
    val posterUrl: String,
    val bannerUrl: String,
    val rating: Float, // e.g. 4.85
    val score: Int, // e.g. 92%
    val type: AnimeType,
    val status: AnimeStatus,
    val episodesCount: Int,
    val releaseYear: Int,
    val season: String, // e.g. "Winter 2026", "Fall 2025"
    val durationMinutes: Int,
    val studio: String,
    val producers: List<String> = emptyList(),
    val genres: List<String>,
    val tags: List<String> = emptyList(),
    val trailerUrl: String = "",
    val characters: List<AnimeCharacter> = emptyList(),
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isPopular: Boolean = false,
    val isSeasonal: Boolean = false,
    val nextEpisodeAirDate: String? = null,
    val hasSub: Boolean = true,
    val hasDub: Boolean = true
) {
    val synopsis: String
        get() = description
}
