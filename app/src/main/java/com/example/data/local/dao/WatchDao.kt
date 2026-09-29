package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE progressMs < (durationMs * 0.95) ORDER BY lastWatchedAt DESC LIMIT 10")
    fun getContinueWatching(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE episodeId = :episodeId LIMIT 1")
    suspend fun getProgressForEpisode(episodeId: String): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history WHERE animeId = :animeId ORDER BY lastWatchedAt DESC LIMIT 1")
    suspend fun getLastWatchedEpisodeForAnime(animeId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE episodeId = :episodeId")
    suspend fun deleteHistoryItem(episodeId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}
