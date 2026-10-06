package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.model.EpisodeNote
import com.example.data.model.StreamSource
import com.example.data.model.SubtitleTrack
import com.example.data.model.VideoBookmark
import com.example.data.repository.AdminRepository
import com.example.data.repository.AnimeRepository
import com.example.data.repository.DownloadsRepository
import com.example.data.repository.GamificationAndSocialRepository
import com.example.data.repository.MalSyncRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isLoading: Boolean = true,
    val anime: Anime? = null,
    val episodes: List<Episode> = emptyList(),
    val currentEpisode: Episode? = null,
    val selectedSource: StreamSource? = null,
    val selectedQuality: String = "1080p",
    val selectedAudio: String = "Japanese (Sub)",
    val selectedSubtitle: SubtitleTrack? = null,
    val playbackSpeed: Float = 1.0f, // 0.5x, 0.75x, 1x, 1.25x, 1.5x, 1.75x, 2x
    val isScreenLocked: Boolean = false,
    val sleepTimerMinutes: Int? = null,
    val autoPlayNextEnabled: Boolean = true,
    val autoSkipIntro: Boolean = false,
    val autoSkipOutro: Boolean = false,
    val backgroundPlaybackEnabled: Boolean = false,
    val isCastingToTv: Boolean = false,
    val castDeviceName: String = "Living Room 4K Smart TV",
    // Subtitle styling & synchronization (Section 7)
    val subtitleFontSizeSp: Int = 16,
    val subtitleColorHex: String = "#FFEE00", // Yellow, White, Cyan, Green
    val subtitleBackgroundColorHex: String = "#000000",
    val subtitleBackgroundOpacity: Float = 0.75f,
    val subtitleBottomPaddingDp: Int = 34,
    val subtitleDelayMs: Long = 0L,
    // Network & Failover telemetry
    val networkQualityLabel: String = "Excellent • 24.8 Mbps (1080p Ready)",
    val initialSeekPositionMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 1440_000L,
    val showSkipIntro: Boolean = false,
    val showSkipOutro: Boolean = false,
    val nextEpisodeCountdownSec: Int? = null,
    val spoilerFreeMode: Boolean = false,
    val error: String? = null,
    val failoverNotice: String? = null,
    val failedSourceUrls: Set<String> = emptySet(),
    val autoFailoverCount: Int = 0
)

class PlayerViewModel(
    private val animeId: String,
    private val initialEpisodeNumber: Int,
    private val animeRepository: AnimeRepository,
    private val watchRepository: WatchRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository? = null,
    private val malSyncRepository: MalSyncRepository? = null,
    private val downloadsRepository: DownloadsRepository? = null,
    val gamificationRepository: GamificationAndSocialRepository = GamificationAndSocialRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val episodeNotes: StateFlow<List<EpisodeNote>> = gamificationRepository.episodeNotes
    val videoBookmarks: StateFlow<List<VideoBookmark>> = gamificationRepository.videoBookmarks

    init {
        loadPlayerData(initialEpisodeNumber)
    }

    fun loadPlayerData(episodeNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val anime = animeRepository.getAnimeById(animeId)
                val baseEpisodes = animeRepository.getEpisodes(animeId)

                val episodes = baseEpisodes.map { ep ->
                    val adminStreamSources = adminRepository?.getScrapedStreamsForEpisode(animeId, ep.episodeNumber)
                        .orEmpty()
                        .mapIndexed { idx, scraped ->
                            StreamSource(
                                serverName = "Server ${idx + 1} • ${scraped.sourceProvider}",
                                quality = scraped.quality.ifBlank { "1080p" },
                                url = scraped.streamUrl,
                                isHls = scraped.isHls || scraped.streamUrl.contains(".m3u8", ignoreCase = true),
                                audioTrack = "Japanese / Multi-Audio"
                            )
                        }
                    val combined = (adminStreamSources + ep.sources).distinctBy { "${it.serverName}_${it.url}" }
                    if (combined.isNotEmpty()) ep.copy(sources = combined) else ep
                }

                val targetEpisode = episodes.find { it.episodeNumber == episodeNumber }
                    ?: episodes.firstOrNull()

                val savedProgress = watchRepository.getEpisodeProgress(animeId, episodeNumber)
                val startPos = savedProgress?.watchedPositionMs ?: 0L

                val defaultSource = targetEpisode?.sources?.firstOrNull()
                val defaultSub = targetEpisode?.subtitles?.firstOrNull { it.isDefault }
                    ?: targetEpisode?.subtitles?.firstOrNull()

                val prefs = userRepository.preferences.value

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        anime = anime,
                        episodes = episodes,
                        currentEpisode = targetEpisode,
                        selectedSource = defaultSource,
                        selectedQuality = defaultSource?.quality ?: prefs.defaultQuality,
                        selectedAudio = defaultSource?.audioTrack ?: prefs.preferredAudioLanguage,
                        selectedSubtitle = defaultSub,
                        autoPlayNextEnabled = prefs.autoPlayNext,
                        autoSkipIntro = prefs.autoSkipIntro,
                        autoSkipOutro = prefs.autoSkipOutro,
                        backgroundPlaybackEnabled = prefs.backgroundPlayback,
                        spoilerFreeMode = prefs.spoilerFreeMode,
                        subtitleFontSizeSp = prefs.subtitleStyle.fontSizeSp,
                        subtitleColorHex = prefs.subtitleStyle.textColorHex,
                        subtitleBackgroundColorHex = prefs.subtitleStyle.backgroundColorHex,
                        subtitleBackgroundOpacity = prefs.subtitleStyle.backgroundOpacity,
                        subtitleBottomPaddingDp = prefs.subtitleStyle.bottomMarginDp,
                        subtitleDelayMs = prefs.subtitleStyle.subtitleDelayMs,
                        initialSeekPositionMs = startPos,
                        currentPositionMs = startPos,
                        failedSourceUrls = emptySet(),
                        failoverNotice = null
                    )
                }

                if (anime != null) {
                    userRepository.incrementWatchStats(anime.durationMinutes)
                    malSyncRepository?.recordEpisodeWatched(anime.titleEnglish, episodeNumber)
                    gamificationRepository.recordEpisodeWatchedProgress()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to initialize video stream"
                    )
                }
            }
        }
    }

    fun selectEpisode(episodeNumber: Int) {
        saveProgressNow()
        loadPlayerData(episodeNumber)
    }

    fun selectSource(source: StreamSource) {
        _uiState.update {
            it.copy(
                selectedSource = source,
                selectedQuality = source.quality,
                selectedAudio = source.audioTrack,
                initialSeekPositionMs = it.currentPositionMs,
                error = null,
                failoverNotice = "Switched to ${source.serverName} (${source.quality})"
            )
        }
    }

    /**
     * Automatic Multi-Server Failover across all 17 servers.
     */
    fun switchToNextWorkingServer(reason: String = "Stream error") {
        val state = _uiState.value
        val ep = state.currentEpisode ?: return
        val currentUrl = state.selectedSource?.url.orEmpty()
        val currentServer = state.selectedSource?.serverName ?: "Current Server"

        val updatedFailedUrls = state.failedSourceUrls + setOfNotNull(currentUrl.takeIf { it.isNotBlank() })
        val allSources = ep.sources

        val nextCandidate = allSources.firstOrNull { src ->
            src.url !in updatedFailedUrls && src.serverName != currentServer
        } ?: allSources.firstOrNull { src ->
            src.url !in updatedFailedUrls
        }

        if (nextCandidate != null) {
            _uiState.update {
                it.copy(
                    selectedSource = nextCandidate,
                    selectedQuality = nextCandidate.quality,
                    selectedAudio = nextCandidate.audioTrack,
                    initialSeekPositionMs = it.currentPositionMs,
                    failedSourceUrls = updatedFailedUrls,
                    autoFailoverCount = it.autoFailoverCount + 1,
                    error = null,
                    failoverNotice = "Auto-switched to ${nextCandidate.serverName} ($reason)"
                )
            }
        } else {
            val firstSource = allSources.firstOrNull()
            _uiState.update {
                it.copy(
                    selectedSource = firstSource,
                    failedSourceUrls = emptySet(),
                    error = "All ${allSources.size} servers timed out. Tap Retry to reconnect.",
                    failoverNotice = null
                )
            }
        }
    }

    fun clearFailoverNotice() {
        _uiState.update { it.copy(failoverNotice = null) }
    }

    fun selectQuality(quality: String) {
        val ep = _uiState.value.currentEpisode ?: return
        val matchingSource = ep.sources.find { it.quality == quality } ?: _uiState.value.selectedSource
        _uiState.update {
            it.copy(
                selectedQuality = quality,
                selectedSource = matchingSource,
                initialSeekPositionMs = it.currentPositionMs
            )
        }
    }

    fun selectAudioTrack(audioLabel: String) {
        val ep = _uiState.value.currentEpisode ?: return
        val matchingSource = ep.sources.find { it.audioTrack.contains(audioLabel, ignoreCase = true) }
            ?: _uiState.value.selectedSource
        _uiState.update {
            it.copy(
                selectedAudio = audioLabel,
                selectedSource = matchingSource,
                initialSeekPositionMs = it.currentPositionMs,
                failoverNotice = "Audio Track: $audioLabel"
            )
        }
        userRepository.updateAdvancedPreferences { prefs ->
            prefs.copy(preferredAudioLanguage = audioLabel)
        }
    }

    fun selectSubtitle(track: SubtitleTrack?) {
        _uiState.update { it.copy(selectedSubtitle = track) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed.coerceIn(0.5f, 2.0f)) }
    }

    fun toggleScreenLock() {
        _uiState.update { it.copy(isScreenLocked = !it.isScreenLocked) }
    }

    fun setSleepTimer(minutes: Int?) {
        _uiState.update { it.copy(sleepTimerMinutes = minutes) }
    }

    fun toggleAutoSkipIntro() {
        val next = !_uiState.value.autoSkipIntro
        _uiState.update { it.copy(autoSkipIntro = next) }
        userRepository.updateAdvancedPreferences { it.copy(autoSkipIntro = next) }
    }

    fun toggleAutoSkipOutro() {
        val next = !_uiState.value.autoSkipOutro
        _uiState.update { it.copy(autoSkipOutro = next) }
        userRepository.updateAdvancedPreferences { it.copy(autoSkipOutro = next) }
    }

    fun toggleAutoPlayNext() {
        val next = !_uiState.value.autoPlayNextEnabled
        _uiState.update { it.copy(autoPlayNextEnabled = next) }
        viewModelScope.launch {
            userRepository.updatePreference(autoPlayNext = next)
        }
    }

    fun toggleBackgroundPlayback() {
        val next = !_uiState.value.backgroundPlaybackEnabled
        _uiState.update { it.copy(backgroundPlaybackEnabled = next) }
        userRepository.updateAdvancedPreferences { it.copy(backgroundPlayback = next) }
    }

    fun toggleCastToSmartTv() {
        val next = !_uiState.value.isCastingToTv
        _uiState.update {
            it.copy(
                isCastingToTv = next,
                failoverNotice = if (next) "📺 Connected to ${it.castDeviceName}" else "Disconnected from Smart TV Cast"
            )
        }
    }

    fun updateSubtitleStyling(
        fontSizeSp: Int? = null,
        colorHex: String? = null,
        bgOpacity: Float? = null,
        bottomPaddingDp: Int? = null,
        delayMs: Long? = null
    ) {
        _uiState.update { state ->
            state.copy(
                subtitleFontSizeSp = (fontSizeSp ?: state.subtitleFontSizeSp).coerceIn(12, 28),
                subtitleColorHex = colorHex ?: state.subtitleColorHex,
                subtitleBackgroundOpacity = (bgOpacity ?: state.subtitleBackgroundOpacity).coerceIn(0f, 1f),
                subtitleBottomPaddingDp = (bottomPaddingDp ?: state.subtitleBottomPaddingDp).coerceIn(12, 84),
                subtitleDelayMs = (delayMs ?: state.subtitleDelayMs).coerceIn(-5000L, 5000L)
            )
        }
    }

    fun addNoteForCurrentEpisode(text: String) {
        val ep = _uiState.value.currentEpisode ?: return
        gamificationRepository.addEpisodeNote(animeId, ep.episodeNumber, text)
        userRepository.awardUserXp(10, "Saved Episode Note")
    }

    fun deleteEpisodeNote(noteId: String) {
        gamificationRepository.deleteEpisodeNote(noteId)
    }

    fun addBookmarkAtCurrentPosition(label: String) {
        val state = _uiState.value
        val ep = state.currentEpisode ?: return
        gamificationRepository.addVideoBookmark(
            animeId = animeId,
            episodeNumber = ep.episodeNumber,
            positionMs = state.currentPositionMs,
            label = label
        )
        userRepository.awardUserXp(10, "Created Timestamp Bookmark")
    }

    fun deleteBookmark(bookmarkId: String) {
        gamificationRepository.deleteVideoBookmark(bookmarkId)
    }

    fun updatePlaybackPosition(positionMs: Long, durationMs: Long) {
        val ep = _uiState.value.currentEpisode ?: return
        val safeDuration = if (durationMs > 0) durationMs else 1440_000L

        val inIntro = positionMs in ep.introStartMs..ep.introEndMs
        val inOutro = positionMs in ep.outroStartMs..ep.outroEndMs

        val remainingSec = ((safeDuration - positionMs) / 1000L).toInt()
        val countdown = if (stateHasNextEpisode() && _uiState.value.autoPlayNextEnabled && remainingSec in 1..10) {
            remainingSec
        } else null

        _uiState.update {
            it.copy(
                currentPositionMs = positionMs,
                totalDurationMs = safeDuration,
                showSkipIntro = inIntro,
                showSkipOutro = inOutro,
                nextEpisodeCountdownSec = countdown
            )
        }

        if (positionMs > 0 && (positionMs / 1000) % 5L == 0L) {
            saveProgressNow()
        }

        if (remainingSec <= 1 && safeDuration > 10_000L) {
            viewModelScope.launch {
                downloadsRepository?.onEpisodeCompleted(animeId, ep.episodeNumber)
            }
        }
    }

    fun getIntroEndPositionMs(): Long {
        return _uiState.value.currentEpisode?.introEndMs ?: 90_000L
    }

    fun getOutroEndPositionMs(): Long {
        return _uiState.value.currentEpisode?.outroEndMs ?: 1400_000L
    }

    private fun stateHasNextEpisode(): Boolean {
        val state = _uiState.value
        val currentEpNum = state.currentEpisode?.episodeNumber ?: return false
        return state.episodes.any { it.episodeNumber == currentEpNum + 1 }
    }

    fun hasNextEpisode(): Boolean = stateHasNextEpisode()

    fun hasPreviousEpisode(): Boolean {
        val state = _uiState.value
        val currentEpNum = state.currentEpisode?.episodeNumber ?: return false
        return state.episodes.any { it.episodeNumber == currentEpNum - 1 }
    }

    fun playNextEpisode() {
        val currentEpNum = _uiState.value.currentEpisode?.episodeNumber ?: return
        if (hasNextEpisode()) {
            selectEpisode(currentEpNum + 1)
        }
    }

    fun playPreviousEpisode() {
        val currentEpNum = _uiState.value.currentEpisode?.episodeNumber ?: return
        if (hasPreviousEpisode()) {
            selectEpisode(currentEpNum - 1)
        }
    }

    fun onPlayerError(message: String) {
        switchToNextWorkingServer(message)
    }

    fun retryPlayback() {
        val currentEp = _uiState.value.currentEpisode?.episodeNumber ?: initialEpisodeNumber
        loadPlayerData(currentEp)
    }

    fun saveProgressNow() {
        val state = _uiState.value
        val anime = state.anime ?: return
        val ep = state.currentEpisode ?: return
        if (state.currentPositionMs <= 0L) return

        viewModelScope.launch {
            watchRepository.saveWatchProgress(
                animeId = anime.id,
                animeTitle = anime.titleEnglish,
                episodeId = ep.id,
                episodeNumber = ep.episodeNumber,
                episodeTitle = ep.title,
                thumbnailUrl = ep.thumbnailUrl.ifBlank { anime.posterUrl },
                watchedPositionMs = state.currentPositionMs,
                totalDurationMs = state.totalDurationMs
            )
        }
    }
}
