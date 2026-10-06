package com.example.data.repository

import android.os.Environment
import android.os.StatFs
import com.example.data.local.dao.DownloadsDao
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.DownloadItemEntity
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.sync.CloudSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class DownloadRuntimeTelemetry(
    val itemId: String,
    val speedLabel: String = "8.4 MB/s",
    val remainingTimeLabel: String = "00:18 remaining",
    val notificationTitle: String = "Downloading episode..."
)

data class DownloadSettingsState(
    val wifiOnly: Boolean = true,
    val allowMobileData: Boolean = false,
    val defaultQuality: String = "1080p", // "360p", "480p", "720p", "1080p"
    val autoDeleteWatchedEpisodes: Boolean = false,
    val backgroundDownloadNotifications: Boolean = true,
    val activeNotificationBanner: String? = null
)

data class DeviceStorageSummary(
    val appDownloadsUsedMb: Int = 0,
    val freeDeviceStorageGb: Float = 42.6f,
    val totalDeviceStorageGb: Float = 128.0f
) {
    val usedStorageFraction: Float
        get() = if (totalDeviceStorageGb > 0f) {
            ((totalDeviceStorageGb - freeDeviceStorageGb) / totalDeviceStorageGb).coerceIn(0.05f, 0.98f)
        } else 0.35f
}

class DownloadsRepository(
    private val downloadsDao: DownloadsDao,
    private val cloudSyncManager: CloudSyncManager? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeDownloadJobs = mutableMapOf<String, Job>()

    private val _settings = MutableStateFlow(DownloadSettingsState())
    val settings: StateFlow<DownloadSettingsState> = _settings.asStateFlow()

    private val _telemetryById = MutableStateFlow<Map<String, DownloadRuntimeTelemetry>>(emptyMap())
    val telemetryById: StateFlow<Map<String, DownloadRuntimeTelemetry>> = _telemetryById.asStateFlow()

    val allDownloads: Flow<List<DownloadEntity>> = downloadsDao.getAllDownloads()
    val totalStorageBytes: Flow<Long?> = downloadsDao.getTotalStorageUsedBytes()

    fun getDownloadsForAnime(animeId: String): Flow<List<DownloadEntity>> =
        downloadsDao.getDownloadsForAnime(animeId)

    suspend fun seedInitialDownloadsIfEmpty() = withContext(Dispatchers.IO) {
        // Keep downloads authentic
    }

    fun updateSettings(
        wifiOnly: Boolean? = null,
        allowMobileData: Boolean? = null,
        defaultQuality: String? = null,
        autoDeleteWatched: Boolean? = null,
        backgroundNotifications: Boolean? = null
    ) {
        _settings.update { current ->
            current.copy(
                wifiOnly = wifiOnly ?: current.wifiOnly,
                allowMobileData = allowMobileData ?: current.allowMobileData,
                defaultQuality = defaultQuality ?: current.defaultQuality,
                autoDeleteWatchedEpisodes = autoDeleteWatched ?: current.autoDeleteWatchedEpisodes,
                backgroundDownloadNotifications = backgroundNotifications ?: current.backgroundDownloadNotifications
            )
        }
        cloudSyncManager?.enqueueIncrementalSync("DOWNLOAD_METADATA", "download_settings", "UPSERT", "Updated download preferences")
    }

    private fun bytesForQuality(quality: String): Long = when (quality) {
        "360p" -> 95L * 1024L * 1024L
        "480p" -> 145L * 1024L * 1024L
        "720p" -> 230L * 1024L * 1024L
        "1080p" -> 340L * 1024L * 1024L
        else -> 240L * 1024L * 1024L
    }

    suspend fun startDownload(anime: Anime, episode: Episode) {
        enqueueDownload(anime, episode, _settings.value.defaultQuality)
    }

    suspend fun enqueueDownload(
        anime: Anime,
        episode: Episode,
        quality: String = _settings.value.defaultQuality
    ) = withContext(Dispatchers.IO) {
        val itemId = "${anime.id}_ep_${episode.episodeNumber}"
        val streamUrl = episode.sources.firstOrNull()?.streamUrl
            ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val totalBytes = bytesForQuality(quality)
        val item = DownloadEntity(
            downloadId = itemId,
            animeId = anime.id,
            episodeNumber = episode.episodeNumber,
            animeTitle = anime.titleEnglish,
            episodeTitle = episode.title,
            posterUrl = episode.thumbnail.ifBlank { anime.posterUrl },
            videoUrl = streamUrl,
            localFilePath = "/storage/emulated/0/Android/data/com.example/files/Downloads/$itemId.mp4",
            fileSizeBytes = totalBytes,
            downloadedBytes = 0L,
            status = "WAITING",
            progressPercent = 0,
            createdAt = System.currentTimeMillis()
        )
        downloadsDao.insertOrUpdate(item)
        cloudSyncManager?.enqueueIncrementalSync("DOWNLOAD_METADATA", itemId, "UPSERT", "Queued ${anime.titleEnglish} Ep ${episode.episodeNumber} ($quality)")
        startOrResumeWorker(item)
    }

    suspend fun enqueueEntireSeason(
        anime: Anime,
        episodes: List<Episode>,
        quality: String = _settings.value.defaultQuality
    ) = withContext(Dispatchers.IO) {
        val totalBytes = bytesForQuality(quality)
        val items = episodes.mapIndexed { idx, episode ->
            val itemId = "${anime.id}_ep_${episode.episodeNumber}"
            val streamUrl = episode.sources.firstOrNull()?.streamUrl
                ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            DownloadEntity(
                downloadId = itemId,
                animeId = anime.id,
                episodeNumber = episode.episodeNumber,
                animeTitle = anime.titleEnglish,
                episodeTitle = episode.title,
                posterUrl = episode.thumbnail.ifBlank { anime.posterUrl },
                videoUrl = streamUrl,
                localFilePath = "/storage/emulated/0/Android/data/com.example/files/Downloads/$itemId.mp4",
                fileSizeBytes = totalBytes,
                downloadedBytes = 0L,
                status = if (idx == 0) "DOWNLOADING" else "WAITING",
                progressPercent = 0,
                createdAt = System.currentTimeMillis() + idx
            )
        }
        items.forEach { downloadsDao.insertOrUpdate(it) }
        cloudSyncManager?.enqueueIncrementalSync("DOWNLOAD_METADATA", "season_${anime.id}", "UPSERT", "Queued ${items.size} episodes of ${anime.titleEnglish}")
        items.forEach { startOrResumeWorker(it) }
    }

    fun pauseDownload(id: String) {
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)
        scope.launch {
            val current = downloadsDao.getDownload(id) ?: return@launch
            val progress = current.progressPercent.coerceIn(5, 95)
            downloadsDao.insertOrUpdate(current.copy(progressPercent = progress, status = "PAUSED"))
            _telemetryById.update { map ->
                map + (id to DownloadRuntimeTelemetry(id, "0.0 MB/s (Paused)", "Paused by user", "Paused: ${current.animeTitle}"))
            }
            _settings.update { it.copy(activeNotificationBanner = "⏸️ Download paused: ${current.animeTitle} Ep ${current.episodeNumber}") }
        }
    }

    fun resumeDownload(id: String) {
        scope.launch {
            val current = downloadsDao.getDownload(id) ?: return@launch
            startOrResumeWorker(current)
        }
    }

    fun retryFailedDownload(id: String) {
        scope.launch {
            val current = downloadsDao.getDownload(id) ?: return@launch
            val reset = current.copy(progressPercent = 0, downloadedBytes = 0L, status = "WAITING")
            downloadsDao.insertOrUpdate(reset)
            startOrResumeWorker(reset)
        }
    }

    fun recoverAllFailedDownloads() {
        scope.launch {
            val all = downloadsDao.getAllDownloads().firstOrNull().orEmpty()
            all.filter { it.status == "FAILED" || it.status == "PAUSED" }.forEach { item ->
                startOrResumeWorker(item)
            }
        }
    }

    fun simulateNetworkInterruption(id: String) {
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)
        scope.launch {
            val current = downloadsDao.getDownload(id) ?: return@launch
            downloadsDao.insertOrUpdate(current.copy(progressPercent = current.progressPercent.coerceAtLeast(20), status = "FAILED"))
            _telemetryById.update { map ->
                map + (id to DownloadRuntimeTelemetry(id, "0.0 MB/s • Network Error", "Tap Retry to recover", "Failed: ${current.animeTitle}"))
            }
        }
    }

    suspend fun deleteDownload(downloadId: String) = removeDownload(downloadId)

    suspend fun removeDownload(id: String) = withContext(Dispatchers.IO) {
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)
        downloadsDao.deleteDownload(id)
        _telemetryById.update { it - id }
        cloudSyncManager?.enqueueIncrementalSync("DOWNLOAD_METADATA", id, "DELETE", "Deleted offline download $id")
    }

    suspend fun clearAllDownloads() = clearAll()

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        activeDownloadJobs.values.forEach { it.cancel() }
        activeDownloadJobs.clear()
        downloadsDao.clearAllDownloads()
        _telemetryById.value = emptyMap()
        _settings.update { it.copy(activeNotificationBanner = null) }
    }

    suspend fun onEpisodeCompleted(animeId: String, episodeNumber: Int) = withContext(Dispatchers.IO) {
        if (!_settings.value.autoDeleteWatchedEpisodes) return@withContext
        val itemId = "${animeId}_ep_$episodeNumber"
        downloadsDao.deleteDownload(itemId)
        _telemetryById.update { it - itemId }
    }

    fun getDeviceStorageSummary(downloads: List<DownloadItemEntity>): DeviceStorageSummary {
        val usedMb = downloads.filter { it.status == "COMPLETED" || it.status == "DOWNLOADING" }.sumOf {
            if (it.status == "COMPLETED") it.sizeMb else (it.sizeMb * it.progressPercent) / 100
        }
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val freeGb = (freeBytes / (1024f * 1024f * 1024f)).coerceAtLeast(1.0f)
            val totalGb = (totalBytes / (1024f * 1024f * 1024f)).coerceAtLeast(16.0f)
            DeviceStorageSummary(
                appDownloadsUsedMb = usedMb,
                freeDeviceStorageGb = freeGb,
                totalDeviceStorageGb = totalGb
            )
        } catch (_: Exception) {
            DeviceStorageSummary(appDownloadsUsedMb = usedMb)
        }
    }

    private fun startOrResumeWorker(item: DownloadEntity) {
        activeDownloadJobs[item.downloadId]?.cancel()
        val job = scope.launch {
            var p = item.progressPercent.coerceIn(0, 95)
            downloadsDao.insertOrUpdate(item.copy(progressPercent = p, status = "DOWNLOADING"))
            while (p < 100) {
                delay(280)
                p = (p + 15).coerceAtMost(100)
                val status = if (p >= 100) "COMPLETED" else "DOWNLOADING"
                val dlBytes = (item.fileSizeBytes * p) / 100L
                downloadsDao.insertOrUpdate(
                    item.copy(
                        progressPercent = p,
                        downloadedBytes = dlBytes,
                        status = status
                    )
                )
                val remainingMb = ((100 - p) * item.sizeMb) / 100f
                val speed = 7.8f + ((p % 4) * 1.1f)
                val etaSec = (remainingMb / speed).toInt().coerceAtLeast(1)
                _telemetryById.update { map ->
                    map + (
                        item.downloadId to DownloadRuntimeTelemetry(
                            itemId = item.downloadId,
                            speedLabel = if (p >= 100) "Completed • Offline Ready" else String.format(Locale.US, "%.1f MB/s", speed),
                            remainingTimeLabel = if (p >= 100) "00:00 remaining" else String.format(Locale.US, "00:%02d remaining", etaSec),
                            notificationTitle = if (p >= 100) "Download Complete: ${item.animeTitle} Ep ${item.episodeNumber}"
                            else "Downloading ${item.animeTitle} Ep ${item.episodeNumber} ($p%)"
                        )
                    )
                }
                if (_settings.value.backgroundDownloadNotifications) {
                    _settings.update {
                        it.copy(
                            activeNotificationBanner = if (p >= 100) "✅ Offline Ready: ${item.animeTitle} Ep ${item.episodeNumber} (${item.quality})"
                            else "⬇️ Downloading ${item.animeTitle} Ep ${item.episodeNumber} • $p% (${String.format(Locale.US, "%.1f MB/s", speed)})"
                        )
                    }
                }
            }
            activeDownloadJobs.remove(item.downloadId)
        }
        activeDownloadJobs[item.downloadId] = job
    }
}
