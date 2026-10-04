package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.model.EpisodeAudio
import com.example.data.model.EpisodeSource
import com.example.data.model.EpisodeSubtitle
import com.example.data.network.HlsStreamService
import com.example.data.repository.AnimeRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WatchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isLoading: Boolean = true,
    val anime: Anime? = null,
    val currentEpisode: Episode? = null,
    val allEpisodes: List<Episode> = emptyList(),
    val currentSource: EpisodeSource? = null,
    val selectedQuality: String = "1080p",
    val selectedSubtitle: EpisodeSubtitle? = null,
    val selectedAudio: EpisodeAudio? = null,
    val playbackSpeed: Float = 1.0f,
    val isPlaying: Boolean = true,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 1440000L,
    val bufferedPositionMs: Long = 0L,
    val showControls: Boolean = true,
    val isLocked: Boolean = false,
    val showQualitySheet: Boolean = false,
    val showSubtitleSheet: Boolean = false,
    val showAudioSheet: Boolean = false,
    val showSpeedSheet: Boolean = false,
    val showEpisodeListSheet: Boolean = false,
    val showCommentsSheet: Boolean = false,
    val isBackgroundAudioEnabled: Boolean = false,
    val isDubMode: Boolean = false,
    val gestureOverlayIcon: String? = null, // "BRIGHTNESS", "VOLUME", "FORWARD", "REWIND"
    val gestureOverlayText: String? = null,
    val brightnessPercent: Int = 70,
    val volumePercent: Int = 65,
    val isInIntro: Boolean = false,
    val isInOutro: Boolean = false,
    val autoNextCountdown: Int? = null,
    val error: String? = null
)

class PlayerViewModel(
    private val animeId: String,
    private val initialEpisodeNumber: Int,
    private val animeRepository: AnimeRepository,
    private val watchRepository: WatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressSaveJob: Job? = null
    private var lastSavedPositionMs = 0L
    private val failedStreamUrls = mutableSetOf<String>()

    init {
        loadPlaybackSession(initialEpisodeNumber)
    }

    fun fallbackToNextWorkingSource(failedUrl: String): EpisodeSource? {
        if (failedUrl.isNotBlank()) {
            failedStreamUrls.add(failedUrl)
        }
        val sources = _uiState.value.currentEpisode?.sources.orEmpty()
        val nextWorking = sources.firstOrNull { it.streamUrl.isNotBlank() && it.streamUrl !in failedStreamUrls }
        if (nextWorking != null) {
            _uiState.update {
                it.copy(
                    currentSource = nextWorking,
                    selectedQuality = nextWorking.quality
                )
            }
        }
        return nextWorking
    }

    fun loadPlaybackSession(episodeNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val anime = animeRepository.getAnimeById(animeId)
                val episodes = animeRepository.getEpisodes(animeId)
                val rawEpisode = episodes.find { it.episodeNumber == episodeNumber } ?: episodes.firstOrNull()

                if (rawEpisode != null && anime != null) {
                    val prioritizedSources = HlsStreamService.prioritizeEpisodeSources(rawEpisode.sources)
                    val episode = rawEpisode.copy(sources = prioritizedSources)
                    val defaultSource = prioritizedSources.firstOrNull()
                    val defaultSub = episode.subtitles.find { it.isDefault }
                        ?: episode.subtitles.firstOrNull()
                    val defaultAud = episode.audioTracks.find { it.isDefault }
                        ?: episode.audioTracks.firstOrNull()

                    // Check if saved progress exists in database to resume!
                    val savedHistory = watchRepository.getEpisodeProgress(episode.id)
                    val resumePos = savedHistory?.progressMs ?: 0L

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            anime = anime,
                            currentEpisode = episode,
                            allEpisodes = episodes,
                            currentSource = defaultSource,
                            selectedQuality = defaultSource?.quality ?: "1080p",
                            selectedSubtitle = defaultSub,
                            selectedAudio = defaultAud,
                            currentPositionMs = resumePos,
                            totalDurationMs = episode.durationSeconds * 1000L
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun setPlaying(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }

    fun toggleControls() {
        if (!_uiState.value.isLocked) {
            _uiState.update { it.copy(showControls = !it.showControls) }
        }
    }

    fun toggleLock() {
        _uiState.update {
            val newLocked = !it.isLocked
            it.copy(isLocked = newLocked, showControls = !newLocked)
        }
    }

    fun updatePosition(posMs: Long, durationMs: Long, bufferedMs: Long = 0L) {
        val currentEp = _uiState.value.currentEpisode ?: return
        val safePos = posMs.coerceAtLeast(0L)
        val currentSec = safePos / 1000L

        val inIntro = currentSec in currentEp.introStartSec..currentEp.introEndSec
        val inOutro = currentSec in currentEp.outroStartSec..currentEp.outroEndSec

        _uiState.update {
            val validDuration = if (durationMs > 1000L) durationMs else it.totalDurationMs.coerceAtLeast(1000L)
            it.copy(
                currentPositionMs = safePos.coerceAtMost(validDuration),
                totalDurationMs = validDuration,
                bufferedPositionMs = bufferedMs.coerceIn(0L, validDuration),
                isInIntro = inIntro,
                isInOutro = inOutro
            )
        }

        // Debounced save watch progress to Room database (save at most once every 5 seconds)
        if (kotlin.math.abs(safePos - lastSavedPositionMs) > 5000L) {
            lastSavedPositionMs = safePos
            saveProgressToDatabase(safePos, _uiState.value.totalDurationMs)
        }
    }

    fun skipIntro() {
        val ep = _uiState.value.currentEpisode ?: return
        val skipToMs = (ep.introEndSec + 1) * 1000L
        _uiState.update { it.copy(currentPositionMs = skipToMs, isInIntro = false) }
    }

    fun skipOutro() {
        playNextEpisode()
    }

    fun playNextEpisode() {
        val currentEpNum = _uiState.value.currentEpisode?.episodeNumber ?: return
        val nextEp = _uiState.value.allEpisodes.find { it.episodeNumber == currentEpNum + 1 }
        if (nextEp != null) {
            loadPlaybackSession(nextEp.episodeNumber)
        }
    }

    fun playPreviousEpisode() {
        val currentEpNum = _uiState.value.currentEpisode?.episodeNumber ?: return
        val prevEp = _uiState.value.allEpisodes.find { it.episodeNumber == currentEpNum - 1 }
        if (prevEp != null) {
            loadPlaybackSession(prevEp.episodeNumber)
        }
    }

    fun selectQuality(source: EpisodeSource) {
        _uiState.update { it.copy(currentSource = source, selectedQuality = source.quality, showQualitySheet = false) }
    }

    fun selectSubtitle(subtitle: EpisodeSubtitle?) {
        _uiState.update { it.copy(selectedSubtitle = subtitle, showSubtitleSheet = false) }
    }

    fun selectAudio(audio: EpisodeAudio) {
        _uiState.update { it.copy(selectedAudio = audio, showAudioSheet = false) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed, showSpeedSheet = false) }
    }

    fun setShowQualitySheet(show: Boolean) = _uiState.update { it.copy(showQualitySheet = show) }
    fun setShowSubtitleSheet(show: Boolean) = _uiState.update { it.copy(showSubtitleSheet = show) }
    fun setShowAudioSheet(show: Boolean) = _uiState.update { it.copy(showAudioSheet = show) }
    fun setShowSpeedSheet(show: Boolean) = _uiState.update { it.copy(showSpeedSheet = show) }
    fun setShowEpisodeListSheet(show: Boolean) = _uiState.update { it.copy(showEpisodeListSheet = show) }
    fun setShowCommentsSheet(show: Boolean) = _uiState.update { it.copy(showCommentsSheet = show) }

    fun toggleBackgroundAudio() {
        _uiState.update { it.copy(isBackgroundAudioEnabled = !it.isBackgroundAudioEnabled) }
    }

    fun toggleDubSub() {
        _uiState.update { current ->
            val newDubMode = !current.isDubMode
            val targetAudio = if (newDubMode) {
                current.currentEpisode?.audioTracks?.find { it.language == "bn" || it.language == "en" }
            } else {
                current.currentEpisode?.audioTracks?.find { it.language == "ja" }
            }
            current.copy(
                isDubMode = newDubMode,
                selectedAudio = targetAudio ?: current.selectedAudio
            )
        }
    }

    fun selectBanglaSubtitle() {
        val bnSub = _uiState.value.currentEpisode?.subtitles?.find { it.language == "bn" }
        if (bnSub != null) {
            selectSubtitle(bnSub)
        }
    }

    fun setBrightnessPercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _uiState.update {
            it.copy(
                brightnessPercent = clamped,
                gestureOverlayIcon = "BRIGHTNESS",
                gestureOverlayText = "Brightness: $clamped%"
            )
        }
    }

    fun setVolumePercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _uiState.update {
            it.copy(
                volumePercent = clamped,
                gestureOverlayIcon = "VOLUME",
                gestureOverlayText = "Volume: $clamped%"
            )
        }
    }

    fun showSeekGestureIndicator(isForward: Boolean, deltaSec: Int) {
        _uiState.update {
            it.copy(
                gestureOverlayIcon = if (isForward) "FORWARD" else "REWIND",
                gestureOverlayText = if (isForward) "+${deltaSec}s" else "-${deltaSec}s"
            )
        }
    }

    fun clearGestureIndicator() {
        _uiState.update { it.copy(gestureOverlayIcon = null, gestureOverlayText = null) }
    }

    private fun saveProgressToDatabase(posMs: Long, durationMs: Long) {
        val state = _uiState.value
        val ep = state.currentEpisode ?: return
        val anime = state.anime ?: return

        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            watchRepository.saveWatchProgress(
                animeId = anime.id,
                episodeId = ep.id,
                episodeNumber = ep.episodeNumber,
                episodeTitle = ep.title,
                animeTitle = anime.titleEnglish,
                posterUrl = anime.posterUrl,
                progressMs = posMs,
                durationMs = durationMs
            )
            userRepository.incrementWatchTime(0.08f) // ~5 seconds added to user watch stats
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Final save on player exit
        val state = _uiState.value
        val ep = state.currentEpisode
        val anime = state.anime
        if (ep != null && anime != null && state.currentPositionMs > 0) {
            viewModelScope.launch {
                watchRepository.saveWatchProgress(
                    animeId = anime.id,
                    episodeId = ep.id,
                    episodeNumber = ep.episodeNumber,
                    episodeTitle = ep.title,
                    animeTitle = anime.titleEnglish,
                    posterUrl = anime.posterUrl,
                    progressMs = state.currentPositionMs,
                    durationMs = state.totalDurationMs
                )
            }
        }
    }
}
