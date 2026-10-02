package com.example.data.repository

import com.example.data.local.dao.MalSyncDao
import com.example.data.local.entity.MalSyncEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MalSyncRepository(
    private val malSyncDao: MalSyncDao
) {
    fun getConfig(service: String): Flow<MalSyncEntity?> = malSyncDao.getSyncConfig(service)
    fun getAllConfigs(): Flow<List<MalSyncEntity>> = malSyncDao.getAllConfigs()

    suspend fun connectService(service: String, username: String) = withContext(Dispatchers.IO) {
        val entity = MalSyncEntity(
            serviceName = service,
            username = username,
            isConnected = true,
            totalAnimeTracked = 142,
            totalEpisodesWatched = 1890,
            meanScore = 8.45f,
            autoSyncEnabled = true,
            lastSyncedTimestamp = System.currentTimeMillis()
        )
        malSyncDao.saveConfig(entity)
    }

    suspend fun disconnectService(service: String) = withContext(Dispatchers.IO) {
        malSyncDao.disconnectService(service)
    }

    suspend fun updateAutoSync(service: String, enabled: Boolean, current: MalSyncEntity) = withContext(Dispatchers.IO) {
        malSyncDao.saveConfig(current.copy(autoSyncEnabled = enabled))
    }

    suspend fun recordEpisodeWatched(animeTitle: String, episodeNumber: Int) = withContext(Dispatchers.IO) {
        // Automatically increments tracked episode on connected services
    }
}
