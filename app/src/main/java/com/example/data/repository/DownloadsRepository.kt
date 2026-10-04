package com.example.data.repository

import com.example.data.local.dao.DownloadsDao
import com.example.data.local.entity.DownloadEntity
import com.example.data.model.Anime
import com.example.data.model.Episode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadsRepository(
    private val downloadsDao: DownloadsDao
) {
    private val activeDownloadJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getAllDownloads(): Flow<List<DownloadEntity>> = downloadsDao.getAllDownloads()

    suspend fun getDownload(downloadId: String): DownloadEntity? = downloadsDao.getDownload(downloadId)

    fun getDownloadsForAnime(animeId: String): Flow<List<DownloadEntity>> =
        downloadsDao.getDownloadsForAnime(animeId)

    fun getTotalStorageUsedBytes(): Flow<Long?> = downloadsDao.getTotalStorageUsedBytes()

    suspend fun startDownload(anime: Anime, episode: Episode) = withContext(Dispatchers.IO) {
        val downloadId = "${anime.id}_ep_${episode.episodeNumber}"
        val existing = downloadsDao.getDownload(downloadId)
        if (existing?.status == "COMPLETED") return@withContext

        val targetUrl = episode.sources.firstOrNull()?.streamUrl
            ?: anime.trailerUrl.ifBlank { "https://v.animethemes.moe/SousouNoFrieren-OP1-NCBD1080.webm" }
        val totalSize = 220L * 1024L * 1024L // ~220 MB

        val entity = DownloadEntity(
            downloadId = downloadId,
            animeId = anime.id,
            episodeNumber = episode.episodeNumber,
            animeTitle = anime.titleEnglish,
            episodeTitle = episode.title,
            posterUrl = anime.posterUrl,
            videoUrl = targetUrl,
            localFilePath = "/data/user/0/com.aistudio.kurostream/files/downloads/${downloadId}.mp4",
            fileSizeBytes = totalSize,
            downloadedBytes = 0L,
            status = "DOWNLOADING",
            progressPercent = 0
        )
        downloadsDao.insertOrUpdate(entity)

        simulateDownload(downloadId, totalSize)
    }

    private fun simulateDownload(downloadId: String, totalSize: Long) {
        activeDownloadJobs[downloadId]?.cancel()
        val job = scope.launch {
            var currentPercent = 5
            while (currentPercent <= 100) {
                delay(600)
                val downloadedBytes = (totalSize * currentPercent) / 100
                val status = if (currentPercent >= 100) "COMPLETED" else "DOWNLOADING"

                val current = downloadsDao.getDownload(downloadId)
                if (current == null || current.status == "PAUSED") break

                downloadsDao.insertOrUpdate(
                    current.copy(
                        downloadedBytes = downloadedBytes,
                        progressPercent = currentPercent,
                        status = status
                    )
                )
                currentPercent += 15
            }
        }
        activeDownloadJobs[downloadId] = job
    }

    suspend fun pauseDownload(downloadId: String) = withContext(Dispatchers.IO) {
        activeDownloadJobs[downloadId]?.cancel()
        activeDownloadJobs.remove(downloadId)
        val current = downloadsDao.getDownload(downloadId) ?: return@withContext
        downloadsDao.insertOrUpdate(current.copy(status = "PAUSED"))
    }

    suspend fun resumeDownload(downloadId: String) = withContext(Dispatchers.IO) {
        val current = downloadsDao.getDownload(downloadId) ?: return@withContext
        downloadsDao.insertOrUpdate(current.copy(status = "DOWNLOADING"))
        simulateDownload(downloadId, current.fileSizeBytes)
    }

    suspend fun deleteDownload(downloadId: String) = withContext(Dispatchers.IO) {
        activeDownloadJobs[downloadId]?.cancel()
        activeDownloadJobs.remove(downloadId)
        downloadsDao.deleteDownload(downloadId)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        activeDownloadJobs.values.forEach { it.cancel() }
        activeDownloadJobs.clear()
        downloadsDao.clearAllDownloads()
    }
}
