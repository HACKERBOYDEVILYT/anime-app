package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY updatedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE status = :status ORDER BY updatedAt DESC")
    fun getWatchlistByStatus(status: String): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavorites(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE animeId = :animeId LIMIT 1")
    fun getWatchlistItem(animeId: String): Flow<WatchlistEntity?>

    @Query("SELECT * FROM watchlist WHERE animeId = :animeId LIMIT 1")
    suspend fun getWatchlistItemSync(animeId: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE animeId = :animeId")
    suspend fun removeFromWatchlist(animeId: String)
}
