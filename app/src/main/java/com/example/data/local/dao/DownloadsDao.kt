package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadsDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE downloadId = :downloadId")
    suspend fun getDownload(downloadId: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE animeId = :animeId")
    fun getDownloadsForAnime(animeId: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE downloadId = :downloadId")
    suspend fun deleteDownload(downloadId: String)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()

    @Query("SELECT SUM(fileSizeBytes) FROM downloads WHERE status = 'COMPLETED'")
    fun getTotalStorageUsedBytes(): Flow<Long?>
}
