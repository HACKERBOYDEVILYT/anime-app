package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.model.EpisodeAudio
import com.example.data.model.EpisodeNote
import com.example.data.model.EpisodeSource
import com.example.data.model.EpisodeSubtitle
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
    val currentSource: EpisodeSource? = null,
    val currentQuality: String = "1080p",
    val currentAudio: EpisodeAudio? = null,
    val currentSubtitle: EpisodeSubtitle? = null,
    val playbackSpeed: Float = 1.0f, // 0.5x, 0.75x, 1x, 1.25x, 1.5x, 1.75x, 2x
    val isPlaying: Boolean = true,
    val showControls: Boolean = true,
    val isLocked: Boolean = false,
    val sleepTimerMinutes: Int = 0, // 0 = off
    val autoNextEpisode: Boolean = true,
    val autoSkipIntro: Boolean = true,
    val autoSkipOutro: Boolean = true,
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
    val bufferedPositionMs: Long = 0L,
    val gestureOverlayIcon: String? = null,
    val gestureOverlayText: String? = null,
    val brightnessPercent: Int = 75,
    val volumePercent: Int = 80,
    val showSkipIntro: Boolean = false,
    val showSkipOutro: Boolean = false,
    val nextEpisodeCountdownSec: Int? = null,
    val spoilerFreeMode: Boolean = false,
    val showCommentsSheet: Boolean = false,
    val showQualitySheet: Boolean = false,
    val showSubtitleSheet: Boolean = false,
    val showSubtitleStyleSheet: Boolean = false,
    val showAudioSheet: Boolean = false,
    val showSpeedSheet: Boolean = false,
    val showEpisodeListSheet: Boolean = false,
    val showBookmarksNotesSheet: Boolean = false,
    val error: String? = null,
    val failoverStatusMessage: String? = null,
    val failedSourceUrls: Set<String> = emptySet(),
    val autoFailoverCount: Int = 0
) {
    val selectedSource: EpisodeSource? get() = currentSource
    val selectedQuality: String get() = currentQuality
    val selectedSubtitle: EpisodeSubtitle? get() = currentSubtitle
    val selectedAudio: EpisodeAudio? get() = currentAudio
    val allEpisodes: List<Episode> get() = episodes
    val isScreenLocked: Boolean get() = isLocked
    val autoPlayNextEnabled: Boolean get() = autoNextEpisode
    val isBackgroundAudioEnabled: Boolean get() = backgroundPlaybackEnabled
    val isDubMode: Boolean
        get() = currentAudio?.language?.equals("en", ignoreCase = true) == true ||
            currentSource?.audioLang?.contains("dub", ignoreCase = true) == true
    val isInIntro: Boolean get() = showSkipIntro
    val isInOutro: Boolean get() = showSkipOutro
    val failoverNotice: String? get() = failoverStatusMessage
}

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

    fun loadPlaybackSession(episodeNumber: Int) {
        loadPlayerData(episodeNumber)
    }

    fun loadPlayerData(episodeNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val anime = animeRepository.getAnimeById(animeId)
                val episodes = animeRepository.getEpisodes(animeId)
                val targetEp = episodes.find { it.episodeNumber == episodeNumber }
                    ?: episodes.firstOrNull()

                val prefs = userRepository.getCurrentUserSnapshot().preferences
                val preferredLangCode = when (prefs.preferredAudioLanguage.lowercase()) {
                    "english", "en" -> "en"
                    "hindi", "hi" -> "hi"
                    "bengali", "bn" -> "bn"
                    else -> if (prefs.preferDub) "en" else "ja"
                }

                val defaultAudio = targetEp?.audioTracks?.find { it.language == preferredLangCode }
                    ?: targetEp?.audioTracks?.firstOrNull()

                val defaultSubLangCode = when (prefs.preferredSubtitleLanguage.lowercase()) {
                    "bengali", "bn" -> "bn"
                    "hindi", "hi" -> "hi"
                    "off", "none" -> "off"
                    else -> "en"
                }
                val defaultSub = if (defaultSubLangCode == "off") {
                    null
                } else {
                    targetEp?.subtitles?.find { it.language == defaultSubLangCode }
                        ?: targetEp?.subtitles?.firstOrNull()
                }

                val defaultSource = targetEp?.sources?.find { it.quality == prefs.defaultQuality }
                    ?: targetEp?.sources?.firstOrNull()

                val savedProgress = watchRepository.getEpisodeProgress(animeId, episodeNumber)
                val initialSeek = if (savedProgress != null && savedProgress.progressPositionMs > 5_000L &&
                    savedProgress.progressPositionMs < (savedProgress.totalDurationMs * 0.95)
                ) {
                    savedProgress.progressPositionMs
                } else {
                    0L
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        anime = anime,
                        episodes = episodes,
                        currentEpisode = targetEp,
                        currentSource = defaultSource,
                        currentQuality = defaultSource?.quality ?: "1080p",
                        currentAudio = defaultAudio,
                        currentSubtitle = defaultSub,
                        autoNextEpisode = prefs.autoPlayNext,
                        autoSkipIntro = prefs.autoSkipIntro,
                        autoSkipOutro = prefs.autoSkipOutro,
                        backgroundPlaybackEnabled = prefs.backgroundPlaybackEnabled,
                        subtitleFontSizeSp = prefs.subtitleStyle.fontSizeSp,
                        subtitleColorHex = prefs.subtitleStyle.textColorHex,
                        subtitleBackgroundColorHex = prefs.subtitleStyle.backgroundColorHex,
                        subtitleBackgroundOpacity = prefs.subtitleStyle.backgroundOpacity,
                        subtitleBottomPaddingDp = prefs.subtitleStyle.bottomPaddingDp,
                        subtitleDelayMs = prefs.subtitleStyle.delayMs,
                        spoilerFreeMode = prefs.spoilerFreeMode,
                        initialSeekPositionMs = initialSeek,
                        currentPositionMs = initialSeek,
                        totalDurationMs = (targetEp?.durationSec ?: 1440L) * 1000L,
                        failoverStatusMessage = null,
                        failedSourceUrls = emptySet(),
                        autoFailoverCount = 0
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load episode stream: ${e.localizedMessage}")
                }
            }
        }
    }

    fun selectEpisode(episodeNumber: Int) {
        loadPlayerData(episodeNumber)
    }

    fun playNextEpisode() {
        val currentEpNum = _uiState.value.currentEpisode?.episodeNumber ?: return
        val nextEp = _uiState.value.episodes.find { it.episodeNumber == currentEpNum + 1 }
        if (nextEp != null) {
            loadPlayerData(nextEp.episodeNumber)
        }
    }

    fun playPreviousEpisode() {
        val currentEpNum = _uiState.value.currentEpisode?.episodeNumber ?: return
        val prevEp = _uiState.value.episodes.find { it.episodeNumber == currentEpNum - 1 }
        if (prevEp != null) {
            loadPlayerData(prevEp.episodeNumber)
        }
    }

    fun selectQuality(quality: String) {
        val source = _uiState.value.currentEpisode?.sources?.find { it.quality == quality }
        if (source != null) {
            _uiState.update {
                it.copy(
                    currentSource = source,
                    currentQuality = quality,
                    showQualitySheet = false,
                    failoverStatusMessage = "Switched to $quality (${source.cdnNode})"
                )
            }
        }
    }

    fun selectQuality(source: EpisodeSource) {
        selectSource(source)
        _uiState.update { it.copy(showQualitySheet = false) }
    }

    fun selectSource(source: EpisodeSource) {
        _uiState.update {
            it.copy(
                currentSource = source,
                currentQuality = source.quality,
                failoverStatusMessage = "Switched to ${source.cdnNode} (${source.quality})"
            )
        }
    }

    fun fallbackToNextWorkingSource(failedUrl: String): EpisodeSource? {
        val ep = _uiState.value.currentEpisode ?: return null
        val updatedFailed = _uiState.value.failedSourceUrls + failedUrl
        val nextCandidate = ep.sources.firstOrNull { src ->
            src.url.isNotBlank() && src.url !in updatedFailed
        } ?: ep.sources.firstOrNull { src -> src.url != failedUrl }

        if (nextCandidate != null) {
            _uiState.update {
                it.copy(
                    currentSource = nextCandidate,
                    currentQuality = nextCandidate.quality,
                    failedSourceUrls = updatedFailed,
                    autoFailoverCount = it.autoFailoverCount + 1,
                    failoverStatusMessage = "⚡ Auto-Switched to ${nextCandidate.cdnNode} (${nextCandidate.quality})"
                )
            }
        }
        return nextCandidate
    }

    fun selectAudio(audio: EpisodeAudio) {
        _uiState.update { it.copy(currentAudio = audio, showAudioSheet = false) }
    }

    fun toggleDubSub() {
        val tracks = _uiState.value.currentEpisode?.audioTracks.orEmpty()
        val isCurrentlyDub = _uiState.value.isDubMode
        val nextAudio = if (isCurrentlyDub) {
            tracks.find { it.language.equals("ja", ignoreCase = true) } ?: tracks.firstOrNull()
        } else {
            tracks.find { it.language.equals("en", ignoreCase = true) } ?: tracks.lastOrNull()
        }
        if (nextAudio != null) {
            _uiState.update {
                it.copy(
                    currentAudio = nextAudio,
                    failoverStatusMessage = "Switched Audio to ${nextAudio.label}"
                )
            }
        }
    }

    fun selectSubtitle(subtitle: EpisodeSubtitle?) {
        _uiState.update { it.copy(currentSubtitle = subtitle, showSubtitleSheet = false) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed.coerceIn(0.5f, 2.0f), showSpeedSheet = false) }
    }

    fun setPlaying(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }

    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
    }

    fun toggleLock() {
        _uiState.update { it.copy(isLocked = !it.isLocked) }
    }

    fun setSleepTimer(minutes: Int) {
        _uiState.update { it.copy(sleepTimerMinutes = minutes) }
    }

    fun toggleAutoNext() {
        _uiState.update { it.copy(autoNextEpisode = !it.autoNextEpisode) }
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

    fun toggleBackgroundPlayback() {
        val next = !_uiState.value.backgroundPlaybackEnabled
        _uiState.update { it.copy(backgroundPlaybackEnabled = next) }
        userRepository.updateAdvancedPreferences { it.copy(backgroundPlaybackEnabled = next) }
    }

    fun toggleBackgroundAudio() {
        toggleBackgroundPlayback()
    }

    fun toggleCastToTv() {
        _uiState.update {
            val casting = !it.isCastingToTv
            it.copy(
                isCastingToTv = casting,
                failoverStatusMessage = if (casting) "📺 Casting to ${it.castDeviceName}" else "📱 Returned playback to device"
            )
        }
    }

    fun updateSubtitleStyling(
        fontSizeSp: Int = _uiState.value.subtitleFontSizeSp,
        colorHex: String = _uiState.value.subtitleColorHex,
        bgOpacity: Float = _uiState.value.subtitleBackgroundOpacity,
        bottomPaddingDp: Int = _uiState.value.subtitleBottomPaddingDp,
        delayMs: Long = _uiState.value.subtitleDelayMs
    ) {
        _uiState.update {
            it.copy(
                subtitleFontSizeSp = fontSizeSp.coerceIn(12, 26),
                subtitleColorHex = colorHex,
                subtitleBackgroundOpacity = bgOpacity.coerceIn(0f, 1f),
                subtitleBottomPaddingDp = bottomPaddingDp.coerceIn(12, 80),
                subtitleDelayMs = delayMs
            )
        }
        userRepository.updateAdvancedPreferences { prefs ->
            prefs.copy(
                subtitleStyle = prefs.subtitleStyle.copy(
                    fontSizeSp = fontSizeSp.coerceIn(12, 26),
                    textColorHex = colorHex,
                    backgroundOpacity = bgOpacity.coerceIn(0f, 1f),
                    bottomPaddingDp = bottomPaddingDp.coerceIn(12, 80),
                    delayMs = delayMs
                )
            )
        }
    }

    fun addTimestampBookmark(label: String) {
        val ep = _uiState.value.currentEpisode ?: return
        gamificationRepository.addVideoBookmark(
            animeId = animeId,
            episodeNumber = ep.episodeNumber,
            positionMs = _uiState.value.currentPositionMs,
            label = label
        )
        _uiState.update {
            it.copy(failoverStatusMessage = "🔖 Saved bookmark '${label.ifBlank { "Scene" }}'")
        }
    }

    fun removeBookmark(bookmarkId: String) {
        gamificationRepository.deleteVideoBookmark(bookmarkId)
    }

    fun addPersonalEpisodeNote(noteText: String) {
        val anime = _uiState.value.anime ?: return
        val ep = _uiState.value.currentEpisode ?: return
        gamificationRepository.addEpisodeNote(
            animeId = anime.id,
            episodeNumber = ep.episodeNumber,
            noteText = noteText
        )
        _uiState.update {
            it.copy(failoverStatusMessage = "📌 Saved personal note for Episode ${ep.episodeNumber}")
        }
    }

    fun showSeekGestureIndicator(isForward: Boolean, deltaSec: Int = 10) {
        _uiState.update {
            it.copy(
                gestureOverlayIcon = if (isForward) "FORWARD" else "REWIND",
                gestureOverlayText = if (isForward) "+${deltaSec}s" else "-${deltaSec}s"
            )
        }
    }

    fun setBrightnessPercent(percent: Int) {
        val clamped = percent.coerceIn(5, 100)
        _uiState.update {
            it.copy(
                brightnessPercent = clamped,
                gestureOverlayIcon = "BRIGHTNESS",
                gestureOverlayText = "Brightness $clamped%"
            )
        }
    }

    fun setVolumePercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _uiState.update {
            it.copy(
                volumePercent = clamped,
                gestureOverlayIcon = "VOLUME",
                gestureOverlayText = "Volume $clamped%"
            )
        }
    }

    fun clearGestureIndicator() {
        _uiState.update { it.copy(gestureOverlayIcon = null, gestureOverlayText = null) }
    }

    fun setShowCommentsSheet(show: Boolean) {
        _uiState.update { it.copy(showCommentsSheet = show) }
    }

    fun setShowQualitySheet(show: Boolean) {
        _uiState.update { it.copy(showQualitySheet = show) }
    }

    fun setShowSubtitleSheet(show: Boolean) {
        _uiState.update { it.copy(showSubtitleSheet = show) }
    }

    fun setShowSubtitleStyleSheet(show: Boolean) {
        _uiState.update { it.copy(showSubtitleStyleSheet = show) }
    }

    fun setShowAudioSheet(show: Boolean) {
        _uiState.update { it.copy(showAudioSheet = show) }
    }

    fun setShowSpeedSheet(show: Boolean) {
        _uiState.update { it.copy(showSpeedSheet = show) }
    }

    fun setShowEpisodeListSheet(show: Boolean) {
        _uiState.update { it.copy(showEpisodeListSheet = show) }
    }

    fun setShowBookmarksNotesSheet(show: Boolean) {
        _uiState.update { it.copy(showBookmarksNotesSheet = show) }
    }

    fun skipIntro(): Long {
        val ep = _uiState.value.currentEpisode ?: return 0L
        val targetMs = (ep.introEndSec + 1L) * 1000L
        _uiState.update {
            it.copy(
                currentPositionMs = targetMs,
                showSkipIntro = false,
                failoverStatusMessage = "⏩ Intro Skipped"
            )
        }
        return targetMs
    }

    fun skipOutro(): Long {
        val ep = _uiState.value.currentEpisode ?: return 0L
        val targetMs = (ep.outroEndSec) * 1000L
        _uiState.update {
            it.copy(
                currentPositionMs = targetMs,
                showSkipOutro = false
            )
        }
        playNextEpisode()
        return targetMs
    }

    fun updatePosition(
        positionMs: Long,
        durationMs: Long = _uiState.value.totalDurationMs,
        bufferedMs: Long = _uiState.value.bufferedPositionMs
    ) {
        val ep = _uiState.value.currentEpisode ?: return
        val anime = _uiState.value.anime ?: return
        val posSec = positionMs / 1000L

        val inIntro = posSec in ep.introStartSec..ep.introEndSec
        val inOutro = posSec in ep.outroStartSec..ep.outroEndSec
        val remainingSec = ((durationMs - positionMs) / 1000L).toInt().coerceAtLeast(0)
        val countdown = if (_uiState.value.autoNextEpisode && remainingSec in 1..10) remainingSec else null

        _uiState.update {
            it.copy(
                currentPositionMs = positionMs,
                totalDurationMs = durationMs.coerceAtLeast(1000L),
                bufferedPositionMs = bufferedMs.coerceAtLeast(positionMs),
                showSkipIntro = inIntro,
                showSkipOutro = inOutro,
                nextEpisodeCountdownSec = countdown
            )
        }

        // Save progress every ~5 seconds
        if (posSec > 0 && posSec % 5L == 0L) {
            viewModelScope.launch {
                watchRepository.saveWatchProgress(
                    animeId = anime.id,
                    animeTitle = anime.titleEnglish,
                    episodeId = ep.id,
                    episodeNumber = ep.episodeNumber,
                    episodeTitle = ep.title,
                    thumbnailUrl = ep.thumbnailUrl,
                    posterUrl = anime.posterUrl,
                    progressMs = positionMs,
                    durationMs = durationMs
                )
                if (durationMs > 0 && positionMs.toFloat() / durationMs.toFloat() >= 0.85f) {
                    userRepository.recordEpisodeWatchedAndStreak()
                    downloadsRepository?.autoDeleteWatchedEpisodeIfNeeded(anime.id, ep.episodeNumber)
                    malSyncRepository?.recordEpisodeWatched(anime.titleEnglish, ep.episodeNumber)
                }
            }
        }
    }
}
