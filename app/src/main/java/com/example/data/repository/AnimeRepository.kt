package com.example.data.repository

import com.example.data.model.Anime
import com.example.data.model.Episode

class AnimeRepository(
    private val metadataProvider: MetadataProvider
) {
    fun getInitialSnapshot(): List<Anime> = metadataProvider.getInitialCatalogSnapshot()
    suspend fun getTrending(): List<Anime> = metadataProvider.getTrendingAnime()
    suspend fun getPopular(): List<Anime> = metadataProvider.getPopularAnime()
    suspend fun getTopRated(): List<Anime> = metadataProvider.getTopRatedAnime()
    suspend fun getSeasonal(): List<Anime> = metadataProvider.getSeasonalAnime()
    suspend fun getRecentlyAdded(): List<Anime> = metadataProvider.getRecentlyAdded()
    suspend fun getAnimeById(id: String): Anime? = metadataProvider.getAnimeById(id)
    suspend fun getEpisodes(animeId: String): List<Episode> = metadataProvider.getEpisodesForAnime(animeId)
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
}
