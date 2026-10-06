package com.example.data.model

enum class AnimeType(val displayName: String) {
    TV("TV Series"),
    MOVIE("Movie"),
    OVA("OVA"),
    ONA("ONA"),
    SPECIAL("Special")
}

enum class AnimeStatus(val displayName: String) {
    RELEASING("Ongoing"),
    FINISHED("Completed"),
    NOT_YET_RELEASED("Upcoming")
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
