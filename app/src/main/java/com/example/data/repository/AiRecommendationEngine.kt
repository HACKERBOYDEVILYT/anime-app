package com.example.data.repository

import com.example.data.model.Anime
import com.example.data.model.ContentControlPreferences
import com.example.data.model.UserPreferences
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem

data class PersonalizedRecommendationsBundle(
    val sourceWatchedTitle: String = "Frieren & Jujutsu Kaisen",
    val becauseYouWatched: List<Anime> = emptyList(),
    val aiPicksForYou: List<Anime> = emptyList(),
    val yourNextAnime: List<Anime> = emptyList(),
    val recommendedForYou: List<Anime> = emptyList(),
    val similarAnime: List<Anime> = emptyList(),
    val hiddenGems: List<Anime> = emptyList(),
    val trendingForYou: List<Anime> = emptyList()
)

/**
 * Intelligent Personalized AI Anime Recommendation Engine (Section 3):
 * Evaluates:
 * - Watch history & Watch completion percentage
 * - Completed anime & Dropped anime (avoids repeatedly recommending completed/dropped titles)
 * - Preferred genres, ratings, favorites, episode length, anime type, language, and content controls
 */
object AiRecommendationEngine {

    fun generateRecommendations(
        catalog: List<Anime>,
        watchHistory: List<WatchHistoryItem>,
        watchlist: List<WatchlistItem>,
        preferences: UserPreferences = UserPreferences()
    ): PersonalizedRecommendationsBundle {
        if (catalog.isEmpty()) return PersonalizedRecommendationsBundle()

        val controls: ContentControlPreferences = preferences.contentControls
        val kidsActive = preferences.kidsMode.isKidsModeActive

        // Apply Content Controls & Kids Mode filtering first
        val filteredCatalog = catalog.filter { anime ->
            val genreBlocked = anime.genres.any { g -> controls.blockedGenres.any { it.equals(g, ignoreCase = true) } }
            val studioBlocked = controls.blockedStudios.any { it.equals(anime.studio, ignoreCase = true) }
            val tagBlocked = anime.tags.any { t -> controls.blockedTags.any { it.equals(t, ignoreCase = true) } }
            val matureRestricted = (controls.restrictMatureContent || kidsActive) &&
                anime.genres.any { it.equals("Horror", ignoreCase = true) || it.equals("Psychological", ignoreCase = true) }
            !genreBlocked && !studioBlocked && !tagBlocked && !matureRestricted
        }.ifEmpty { catalog }

        val completedIds = watchlist.filter { it.status == WatchStatus.COMPLETED }.map { it.animeId }.toSet()
        val droppedIds = watchlist.filter { it.status == WatchStatus.DROPPED }.map { it.animeId }.toSet()
        val favoriteIds = watchlist.filter { it.isFavorite }.map { it.animeId }.toSet()
        val excludedFromDiscovery = completedIds + droppedIds

        // Candidate pool that avoids already completed or dropped anime unless catalog is small
        val candidatePool = filteredCatalog.filter { it.id !in excludedFromDiscovery }.ifEmpty { filteredCatalog }

        // Determine user's top genres from watch history (weighted by completion %) and favorites
        val genreWeights = mutableMapOf<String, Float>()
        watchHistory.forEach { history ->
            val matchedAnime = filteredCatalog.find { it.id == history.animeId }
            val weight = 0.5f + history.percentage
            matchedAnime?.genres?.forEach { genre ->
                genreWeights[genre] = (genreWeights[genre] ?: 0f) + weight
            }
        }
        watchlist.filter { it.isFavorite || it.status == WatchStatus.WATCHING }.forEach { item ->
            val matchedAnime = filteredCatalog.find { it.id == item.animeId }
            val bonus = if (item.isFavorite) 2.0f else 1.0f
            matchedAnime?.genres?.forEach { genre ->
                genreWeights[genre] = (genreWeights[genre] ?: 0f) + bonus
            }
        }
        if (genreWeights.isEmpty()) {
            genreWeights["Action"] = 2.0f
            genreWeights["Fantasy"] = 1.8f
            genreWeights["Adventure"] = 1.5f
            genreWeights["Sci-Fi"] = 1.2f
        }

        // Score every candidate anime based on multi-factor affinity
        fun scoreAnime(anime: Anime): Float {
            val genreAffinity = anime.genres.sumOf { (genreWeights[it] ?: 0.3f).toDouble() }.toFloat()
            val ratingBoost = anime.rating * 1.4f
            val scoreBoost = (anime.score / 25f)
            val durationMatch = if (anime.durationMinutes in 20..28) 0.6f else 0.2f
            val favoriteBonus = if (anime.id in favoriteIds) 0.5f else 0f
            return genreAffinity + ratingBoost + scoreBoost + durationMatch + favoriteBonus
        }

        val rankedCandidates = candidatePool.sortedByDescending { scoreAnime(it) }

        val mostRecentWatched = watchHistory.maxByOrNull { it.lastWatchedAt }
        val anchorAnime = mostRecentWatched?.let { h -> filteredCatalog.find { it.id == h.animeId } }
            ?: watchlist.firstOrNull { it.isFavorite }?.let { f -> filteredCatalog.find { it.id == f.animeId } }
            ?: filteredCatalog.first()

        val becauseYouWatched = candidatePool
            .filter { it.id != anchorAnime.id && it.genres.any { g -> g in anchorAnime.genres } }
            .sortedByDescending { scoreAnime(it) }
            .ifEmpty { rankedCandidates }

        val aiPicksForYou = rankedCandidates

        val yourNextAnime = candidatePool
            .sortedWith(compareByDescending<Anime> { it.rating }.thenBy { it.episodesCount })
            .ifEmpty { rankedCandidates }

        val recommendedForYou = candidatePool
            .sortedByDescending { (it.score * 0.7f) + (scoreAnime(it) * 4f) }
            .ifEmpty { rankedCandidates }

        val similarAnime = filteredCatalog
            .filter { it.id != anchorAnime.id && (it.studio == anchorAnime.studio || it.genres.intersect(anchorAnime.genres.toSet()).isNotEmpty()) }
            .ifEmpty { rankedCandidates }

        val hiddenGems = candidatePool
            .filter { it.rating >= 4.6f && !it.isTrending }
            .ifEmpty { candidatePool.sortedBy { it.episodesCount } }

        val trendingForYou = candidatePool
            .filter { it.isTrending || it.isPopular }
            .sortedByDescending { scoreAnime(it) }
            .ifEmpty { rankedCandidates }

        return PersonalizedRecommendationsBundle(
            sourceWatchedTitle = anchorAnime.titleEnglish,
            becauseYouWatched = becauseYouWatched,
            aiPicksForYou = aiPicksForYou,
            yourNextAnime = yourNextAnime,
            recommendedForYou = recommendedForYou,
            similarAnime = similarAnime,
            hiddenGems = hiddenGems,
            trendingForYou = trendingForYou
        )
    }
}
