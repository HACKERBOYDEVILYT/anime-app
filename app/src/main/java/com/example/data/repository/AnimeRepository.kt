package com.example.data.repository

import com.example.data.model.Anime
import com.example.data.model.AnimeSortOption
import com.example.data.model.AnimeStatus
import com.example.data.model.AnimeType
import com.example.data.model.Episode

class AnimeRepository(
    private val metadataProvider: MetadataProvider
) {
    fun getInitialSnapshot(): List<Anime> = metadataProvider.getInitialCatalogSnapshot()
    suspend fun getFeaturedAnime(): Anime? = metadataProvider.getTrendingAnime().firstOrNull()
        ?: metadataProvider.getInitialCatalogSnapshot().firstOrNull()
    suspend fun getTrending(): List<Anime> = metadataProvider.getTrendingAnime()
    suspend fun getPopular(): List<Anime> = metadataProvider.getPopularAnime()
    suspend fun getTopRated(): List<Anime> = metadataProvider.getTopRatedAnime()
    suspend fun getSeasonal(): List<Anime> = metadataProvider.getSeasonalAnime()
    suspend fun getRecentlyAdded(): List<Anime> = metadataProvider.getRecentlyAdded()
    suspend fun getAnimeById(id: String): Anime? = metadataProvider.getAnimeById(id)
    suspend fun getEpisodes(animeId: String): List<Episode> = metadataProvider.getEpisodesForAnime(animeId)
    suspend fun getEpisodesForAnime(animeId: String): List<Episode> = metadataProvider.getEpisodesForAnime(animeId)
    suspend fun getRecommendations(animeId: String): List<Anime> = metadataProvider.getRecommendations(animeId)
    suspend fun getGenres(): List<String> = metadataProvider.getAllGenres()
    suspend fun getStudios(): List<String> = metadataProvider.getAllStudios()

    suspend fun search(
        query: String,
        genre: String? = null,
        year: Int? = null,
        type: String? = null,
        status: String? = null,
        sortBy: String = "POPULARITY"
    ): List<Anime> = metadataProvider.searchAnime(query, genre, year, type, status, sortBy)

    suspend fun filterCatalog(
        query: String = "",
        genre: String? = null,
        year: Int? = null,
        season: String? = null,
        type: AnimeType? = null,
        status: AnimeStatus? = null,
        sort: AnimeSortOption = AnimeSortOption.POPULARITY
    ): List<Anime> {
        val base = metadataProvider.searchAnime(
            query = query,
            genre = genre,
            year = year,
            type = type?.name,
            status = status?.name,
            sortBy = sort.name
        )
        val seasonFiltered = if (season.isNullOrBlank()) {
            base
        } else {
            base.filter { it.season.contains(season, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            return seasonFiltered
        }
        return when (sort) {
            AnimeSortOption.POPULARITY -> seasonFiltered.sortedByDescending { it.score }
            AnimeSortOption.RATING -> seasonFiltered.sortedByDescending { it.rating }
            AnimeSortOption.NEWEST -> seasonFiltered.sortedByDescending { it.releaseYear }
            AnimeSortOption.TITLE_AZ -> seasonFiltered.sortedBy { it.titleEnglish }
        }
    }
}
