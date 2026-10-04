package com.example.data.repository

import com.example.data.model.Anime
import com.example.data.model.Episode

interface MetadataProvider {
    fun getInitialCatalogSnapshot(): List<Anime> = emptyList()
    suspend fun getTrendingAnime(): List<Anime>
    suspend fun getPopularAnime(): List<Anime>
    suspend fun getTopRatedAnime(): List<Anime>
    suspend fun getSeasonalAnime(): List<Anime>
    suspend fun getRecentlyAdded(): List<Anime>
    suspend fun getAnimeById(id: String): Anime?
    suspend fun searchAnime(
        query: String,
        genre: String? = null,
        year: Int? = null,
        type: String? = null,
        status: String? = null,
        sortBy: String = "POPULARITY"
    ): List<Anime>
    suspend fun getEpisodesForAnime(animeId: String): List<Episode>
    suspend fun getRecommendations(animeId: String): List<Anime>
    suspend fun getAllGenres(): List<String>
    suspend fun getAllStudios(): List<String>
}
