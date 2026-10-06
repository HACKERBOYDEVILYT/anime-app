package com.example.data.repository

import com.example.data.model.Anime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.Calendar

data class ScheduledAnime(
    val anime: Anime,
    val episodeNumber: Int,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val airTime: String,   // e.g. "19:30 JST"
    val statusLabel: String = "Simulcast Airing Soon",
    val targetAirEpochMs: Long,
    val isReminderSet: Boolean = false
)

class ScheduleRepository(
    private val animeRepository: AnimeRepository,
    private val watchRepository: WatchRepository? = null
) {
    private val _personalReminders = MutableStateFlow(setOf("anime_1", "anime_2", "anime_3"))
    val personalReminders: StateFlow<Set<String>> = _personalReminders.asStateFlow()

    fun togglePersonalReminder(anime: Anime, episodeNumber: Int): Boolean {
        val current = _personalReminders.value
        val newState = anime.id !in current
        _personalReminders.update { if (newState) it + anime.id else it - anime.id }
        return newState
    }

    suspend fun getWeeklySchedule(): Map<String, List<ScheduledAnime>> = withContext(Dispatchers.IO) {
        val allAnime = animeRepository.getTrending() + animeRepository.getPopular() + animeRepository.getSeasonal()
        val distinct = allAnime.distinctBy { it.id }

        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val now = System.currentTimeMillis()
        val reminderIds = _personalReminders.value

        val scheduleMap = mutableMapOf<String, MutableList<ScheduledAnime>>()
        days.forEach { scheduleMap[it] = mutableListOf() }

        distinct.forEachIndexed { index, anime ->
            val day = days[index % days.size]
            val epNum = ((index * 3 + 4) % (anime.episodesCount.coerceAtLeast(12))) + 1
            val hoursOffset = ((index * 2) % 9) + 1
            val targetTime = now + (hoursOffset * 3600 * 1000L) + (14 * 60 * 1000L) + (36 * 1000L)

            val hour = (12 + (index * 2) % 11)
            val airTimeStr = String.format("%02d:30 JST", hour)
            val statusLabel = when {
                day == getCurrentDayName() -> "Airing Today • Simulcast"
                index % 4 == 0 -> "Countdown Active"
                else -> "Scheduled Simulcast"
            }

            scheduleMap[day]?.add(
                ScheduledAnime(
                    anime = anime,
                    episodeNumber = epNum,
                    dayOfWeek = day,
                    airTime = airTimeStr,
                    statusLabel = statusLabel,
                    targetAirEpochMs = targetTime,
                    isReminderSet = anime.id in reminderIds || index % 3 == 0
                )
            )
        }

        scheduleMap
    }

    fun getCurrentDayName(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Monday"
        }
    }
}
