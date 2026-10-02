package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MalSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MalSyncDao {
    @Query("SELECT * FROM mal_sync WHERE serviceName = :service")
    fun getSyncConfig(service: String): Flow<MalSyncEntity?>

    @Query("SELECT * FROM mal_sync")
    fun getAllConfigs(): Flow<List<MalSyncEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: MalSyncEntity)

    @Query("DELETE FROM mal_sync WHERE serviceName = :service")
    suspend fun disconnectService(service: String)
}
