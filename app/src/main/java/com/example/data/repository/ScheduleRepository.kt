package com.example.data.repository

import com.example.data.model.Anime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

data class ScheduledAnime(
    val anime: Anime,
    val episodeNumber: Int,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val airTime: String,   // e.g. "07:30 PM"
    val targetAirEpochMs: Long,
    val isReminderSet: Boolean = false
)

class ScheduleRepository(
    private val animeRepository: AnimeRepository
) {
    suspend fun getWeeklySchedule(): Map<String, List<ScheduledAnime>> = withContext(Dispatchers.IO) {
        val allAnime = animeRepository.getTrending() + animeRepository.getPopular() + animeRepository.getSeasonal()
        val distinct = allAnime.distinctBy { it.id }

        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val now = System.currentTimeMillis()

        val scheduleMap = mutableMapOf<String, MutableList<ScheduledAnime>>()
        days.forEach { scheduleMap[it] = mutableListOf() }

        distinct.forEachIndexed { index, anime ->
            val day = days[index % days.size]
            val epNum = ((index * 3 + 4) % (anime.episodesCount.coerceAtLeast(12))) + 1
            val hoursOffset = ((index * 5) % 24) + 1
            val targetTime = now + (hoursOffset * 3600 * 1000L) + (index * 15 * 60 * 1000L)

            val hour = (12 + (index * 2) % 11)
            val airTimeStr = String.format("%02d:30 JST", hour)

            scheduleMap[day]?.add(
                ScheduledAnime(
                    anime = anime,
                    episodeNumber = epNum,
                    dayOfWeek = day,
                    airTime = airTimeStr,
                    targetAirEpochMs = targetTime,
                    isReminderSet = index % 3 == 0
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
