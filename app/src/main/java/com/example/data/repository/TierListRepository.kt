package com.example.data.repository

import com.example.data.model.Anime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TierListRepository(
    private val animeRepository: AnimeRepository
) {
    // Tiers: S, A, B, C, D
    private val _tierAssignments = MutableStateFlow<Map<String, List<Anime>>>(
        mapOf(
            "S" to emptyList(),
            "A" to emptyList(),
            "B" to emptyList(),
            "C" to emptyList(),
            "D" to emptyList()
        )
    )
    val tierAssignments: StateFlow<Map<String, List<Anime>>> = _tierAssignments.asStateFlow()

    suspend fun getAvailableAnime(): List<Anime> {
        val all = animeRepository.getTrending() + animeRepository.getPopular() + animeRepository.getTopRated()
        return all.distinctBy { it.id }
    }

    fun assignAnimeToTier(tier: String, anime: Anime) {
        _tierAssignments.update { current ->
            // Remove from any previous tier
            val cleaned = current.mapValues { (_, list) -> list.filter { it.id != anime.id } }.toMutableMap()
            val existingInTier = cleaned[tier] ?: emptyList()
            cleaned[tier] = existingInTier + anime
            cleaned
        }
    }

    fun removeAnimeFromTier(animeId: String) {
        _tierAssignments.update { current ->
            current.mapValues { (_, list) -> list.filter { it.id != animeId } }
        }
    }

    fun resetTiers() {
        _tierAssignments.value = mapOf(
            "S" to emptyList(),
            "A" to emptyList(),
            "B" to emptyList(),
            "C" to emptyList(),
            "D" to emptyList()
        )
    }
}
