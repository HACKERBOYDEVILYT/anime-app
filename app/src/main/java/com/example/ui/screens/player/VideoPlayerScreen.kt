package com.example.ui.screens.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.Episode
import com.example.data.repository.CommentsRepository
import com.example.data.repository.DownloadsRepository
import com.example.ui.components.CustomVideoProgressBar
import com.example.ui.components.EpisodeCommentsSheet
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private val HiAnimePink = Color(0xFFFF0000) // YouTube Signature Red
private val HiAnimeDarkBg = Color(0xFF0F0F0F) // YouTube Dark Background
private val HiAnimePanelBg = Color(0xFF1E1E1E) // YouTube Elevated Card Surface
private val HiAnimeEpisodeIdle = Color(0xFF272727) // YouTube Pill / Chip Background
private val YouTubeRed = Color(0xFFFF0000)
private val YouTubeDarkBg = Color(0xFF0F0F0F)
private val YouTubeSurfaceBg = Color(0xFF1E1E1E)
private val YouTubeChipBg = Color(0xFF272727)
private val YouTubeSubtleText = Color(0xFFAAAAAA)

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun VideoPlayerScreen(
    viewModel: PlayerViewModel,
    commentsRepository: CommentsRepository? = null,
    downloadsRepository: DownloadsRepository? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isFullscreen by remember { mutableStateOf(false) }
    var isEpisodeGridMode by remember { mutableStateOf(true) }
    var episodeSearchQuery by remember { mutableStateOf("") }
    var isSubscribedToChannel by remember { mutableStateOf(false) }
    var isChannelSubscribed by remember { mutableStateOf(false) }
    var isVideoLiked by remember { mutableStateOf(false) }
    var isVideoDisliked by remember { mutableStateOf(false) }
    var likeCount by remember(uiState.anime?.id, uiState.currentEpisode?.episodeNumber) {
        mutableIntStateOf(48200 + ((uiState.currentEpisode?.episodeNumber ?: 1) * 340))
    }
    var likeCountValue by remember(uiState.anime?.id, uiState.currentEpisode?.episodeNumber) {
        mutableIntStateOf(48200 + ((uiState.currentEpisode?.episodeNumber ?: 1) * 340))
    }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var showDescriptionExpanded by remember { mutableStateOf(false) }

    val toggleFullscreenMode: () -> Unit = {
        isFullscreen = !isFullscreen
        runCatching {
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (isFullscreen) {
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    BackHandler {
        if (isFullscreen) {
            toggleFullscreenMode()
        } else {
            onBack()
        }
    }

    val audioManager = remember {
        runCatching { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }.getOrNull()
    }
    val maxVolume = remember {
        runCatching { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15 }.getOrDefault(15)
    }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var currentBrightness by remember { mutableFloatStateOf(0.75f) }
    var currentVolumePercent by remember {
        val currentVol = runCatching { audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10 }.getOrDefault(10)
        mutableIntStateOf(((currentVol.toFloat() / maxVolume.toFloat()) * 100).toInt().coerceIn(0, 100))
    }
    var kbdShortcutToast by remember { mutableStateOf<String?>(null) }
    val playerFocusRequester = remember { FocusRequester() }

    LaunchedEffect(kbdShortcutToast) {
        if (kbdShortcutToast != null) {
            delay(1500)
            kbdShortcutToast = null
        }
    }

    LaunchedEffect(Unit) {
        runCatching { playerFocusRequester.requestFocus() }
    }

    // ExoPlayer Instance compatible across all Android versions (API 24+ through API 36)
    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(12000)
            .setReadTimeoutMs(18000)

        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setExceedVideoConstraintsIfNecessary(true)
                    .setExceedRendererCapabilitiesIfNecessary(true)
            )
        }

        ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory))
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    val currentStreamUrl = uiState.currentSource?.streamUrl.orEmpty()
    val isBlockedYoutubeTrailer = remember(currentStreamUrl) {
        currentStreamUrl.contains("youtube.com", ignoreCase = true) ||
            currentStreamUrl.contains("youtu.be", ignoreCase = true)
    }
    val isPaidServerHtmlOrEmbed = remember(currentStreamUrl, isBlockedYoutubeTrailer) {
        if (isBlockedYoutubeTrailer || currentStreamUrl.isBlank()) {
            false
        } else {
            val trimmed = currentStreamUrl.trim()
            trimmed.startsWith("<") ||
                trimmed.contains("<iframe", ignoreCase = true) ||
                trimmed.contains("<video", ignoreCase = true) ||
                trimmed.contains("<mux-player", ignoreCase = true) ||
                trimmed.contains("iframe.mediadelivery.net", ignoreCase = true) ||
                (trimmed.contains("cloudflarestream.com", ignoreCase = true) && trimmed.contains("/iframe", ignoreCase = true)) ||
                trimmed.contains("player.vimeo.com", ignoreCase = true) ||
                trimmed.contains("jwplayer.com/players", ignoreCase = true) ||
                trimmed.contains("filemoon.", ignoreCase = true) ||
                trimmed.contains("streamtape.", ignoreCase = true) ||
                trimmed.contains("dood", ignoreCase = true) ||
                trimmed.contains("vidguard", ignoreCase = true) ||
                trimmed.contains("listeamed", ignoreCase = true) ||
                trimmed.contains("abyss.to", ignoreCase = true) ||
                trimmed.contains("streamwish", ignoreCase = true) ||
                trimmed.contains("voe.sx", ignoreCase = true) ||
                trimmed.contains("/embed/", ignoreCase = true) ||
                trimmed.endsWith(".html", ignoreCase = true)
        }
    }
    val isWebEmbedOrTrailer = isBlockedYoutubeTrailer || isPaidServerHtmlOrEmbed

    // Automatic Multi-Server Failover if a stream URL fails or codec is unsupported on device
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                val failedUrl = uiState.currentSource?.streamUrl.orEmpty()
                val nextSource = viewModel.fallbackToNextWorkingSource(failedUrl)
                if (nextSource != null) {
                    kbdShortcutToast = "⚡ Auto-Switched → ${nextSource.cdnNode}"
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    exoPlayer.play()
                    viewModel.setPlaying(true)
                } else if (playbackState == Player.STATE_ENDED && uiState.autoNextEpisode) {
                    viewModel.playNextEpisode()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Watchdog Timer: If any server fails to initialize after 8.5 seconds, switch to next server
    LaunchedEffect(currentStreamUrl) {
        if (currentStreamUrl.isNotBlank() && !isWebEmbedOrTrailer) {
            delay(8500)
            if (!exoPlayer.isPlaying &&
                exoPlayer.playbackState != Player.STATE_READY &&
                exoPlayer.playbackState != Player.STATE_BUFFERING
            ) {
                val nextSource = viewModel.fallbackToNextWorkingSource(currentStreamUrl)
                if (nextSource != null) {
                    kbdShortcutToast = "⚡ Server Timeout → Auto-Switched to ${nextSource.cdnNode}"
                }
            }
        }
    }

    // Set Media Item when currentSource or currentEpisode changes
    LaunchedEffect(currentStreamUrl, uiState.currentEpisode?.id, isWebEmbedOrTrailer) {
        if (currentStreamUrl.isNotBlank() && !isWebEmbedOrTrailer) {
            val mediaItem = if (currentStreamUrl.contains(".m3u8", ignoreCase = true)) {
                MediaItem.Builder()
                    .setUri(Uri.parse(currentStreamUrl))
                    .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
                    .build()
            } else {
                MediaItem.fromUri(Uri.parse(currentStreamUrl))
            }
            exoPlayer.setMediaItem(mediaItem)
            if (uiState.currentPositionMs > 5000L && uiState.currentPositionMs < 60_000L) {
                exoPlayer.seekTo(uiState.currentPositionMs)
            }
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            viewModel.setPlaying(false)
        }
    }

    // Playback Speed
    LaunchedEffect(uiState.playbackSpeed) {
        runCatching {
            exoPlayer.playbackParameters = PlaybackParameters(uiState.playbackSpeed)
        }
    }

    // Sync ExoPlayer Position with ViewModel safely
    LaunchedEffect(exoPlayer) {
        while (true) {
            val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
            val playerDur = exoPlayer.duration
            val totalDur = if (playerDur != C.TIME_UNSET && playerDur > 0L) {
                playerDur
            } else {
                uiState.totalDurationMs.coerceAtLeast(1000L)
            }
            val bufferedPos = exoPlayer.bufferedPosition.coerceIn(0L, totalDur)
            viewModel.updatePosition(currentPos, totalDur, bufferedPos)
            viewModel.setPlaying(exoPlayer.isPlaying)
            delay(500)
        }
    }

    // Clear gesture indicator after 1.5 seconds
    LaunchedEffect(uiState.gestureOverlayIcon) {
        if (uiState.gestureOverlayIcon != null) {
            delay(1500)
            viewModel.clearGestureIndicator()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            runCatching {
                exoPlayer.stop()
                exoPlayer.release()
            }
        }
    }

    // Auto-hide controls after 4.5s while playing
    LaunchedEffect(uiState.showControls, uiState.isPlaying) {
        if (uiState.showControls && uiState.isPlaying && !uiState.isLocked) {
            delay(4500)
            viewModel.toggleControls()
        }
    }

    val animeTitle = uiState.anime?.titleEnglish?.ifBlank { "Anime" } ?: "Anime"
    val currentEpNum = uiState.currentEpisode?.episodeNumber ?: 1
    val currentEpTitle = uiState.currentEpisode?.title ?: "Episode $currentEpNum"
    val guaranteedEpisodes = remember(uiState.allEpisodes, uiState.anime) {
        val targetTotal = maxOf(
            uiState.allEpisodes.size,
            (uiState.anime?.episodesCount ?: 12).coerceIn(12, 28),
            12
        )
        val existingByNum = uiState.allEpisodes.associateBy { it.episodeNumber }
        val template = uiState.allEpisodes.firstOrNull()
        (1..targetTotal).map { epNum ->
            existingByNum[epNum] ?: Episode(
                id = "${uiState.anime?.id ?: "anime"}_ep_$epNum",
                animeId = uiState.anime?.id ?: "anime",
                episodeNumber = epNum,
                title = "Episode $epNum",
                thumbnail = template?.thumbnail ?: (uiState.anime?.bannerUrl ?: ""),
                durationSeconds = template?.durationSeconds ?: 1440L,
                airDate = template?.airDate ?: "2025",
                sources = template?.sources.orEmpty()
            )
        }
    }
    val totalEpisodesCount = guaranteedEpisodes.size.coerceAtLeast(12)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HiAnimeDarkBg)
            .focusRequester(playerFocusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && !uiState.isLocked) {
                    when (keyEvent.key) {
                        Key.Spacebar, Key.MediaPlayPause -> {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                                viewModel.setPlaying(false)
                                kbdShortcutToast = "⏸ Paused [Space]"
                            } else {
                                exoPlayer.play()
                                viewModel.setPlaying(true)
                                kbdShortcutToast = "▶ Playing [Space]"
                            }
                            true
                        }
                        Key.DirectionLeft -> {
                            val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            viewModel.showSeekGestureIndicator(isForward = false, deltaSec = 10)
                            kbdShortcutToast = "⏪ Rewind -10s [←]"
                            true
                        }
                        Key.DirectionRight -> {
                            val maxDur = if (exoPlayer.duration != C.TIME_UNSET && exoPlayer.duration > 0L) {
                                exoPlayer.duration
                            } else {
                                uiState.totalDurationMs.coerceAtLeast(exoPlayer.currentPosition + 10000L)
                            }
                            val newPos = (exoPlayer.currentPosition + 10000L).coerceIn(0L, maxDur)
                            exoPlayer.seekTo(newPos)
                            viewModel.showSeekGestureIndicator(isForward = true, deltaSec = 10)
                            kbdShortcutToast = "⏩ Forward +10s [→]"
                            true
                        }
                        Key.DirectionUp -> {
                            val newVolPercent = (currentVolumePercent + 10).coerceIn(0, 100)
                            currentVolumePercent = newVolPercent
                            val targetStreamVol = ((newVolPercent / 100f) * maxVolume).toInt()
                            runCatching { audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetStreamVol, 0) }
                            viewModel.setVolumePercent(newVolPercent)
                            kbdShortcutToast = "🔊 Volume $newVolPercent% [↑]"
                            true
                        }
                        Key.DirectionDown -> {
                            val newVolPercent = (currentVolumePercent - 10).coerceIn(0, 100)
                            currentVolumePercent = newVolPercent
                            val targetStreamVol = ((newVolPercent / 100f) * maxVolume).toInt()
                            runCatching { audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetStreamVol, 0) }
                            viewModel.setVolumePercent(newVolPercent)
                            kbdShortcutToast = "🔉 Volume $newVolPercent% [↓]"
                            true
                        }
                        Key.F -> {
                            toggleFullscreenMode()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .testTag("video_player_container")
    ) {
        // 1. YouTube Top Navigation Header (Visible in normal mode)
        if (!isFullscreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0F0F))
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("player_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Authentic YouTube Play Badge + Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = YouTubeRed,
                        modifier = Modifier.size(width = 28.dp, height = 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "YouTube",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.anime?.titleEnglish ?: "Streaming Anime",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Episode $currentEpNum • $currentEpTitle",
                        color = YouTubeSubtleText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("player_header_episode_number")
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Prominent Episode Number Pill Badge in Header
                Surface(
                    onClick = { viewModel.setShowEpisodeListSheet(true) },
                    shape = RoundedCornerShape(16.dp),
                    color = YouTubeChipBg,
                    modifier = Modifier
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                        .testTag("player_ep_badge_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EP $currentEpNum/$totalEpisodesCount",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Video Player Stage (16:9 in Portrait/Normal Mode, FillMaxSize in Fullscreen Mode)
        Box(
            modifier = (if (isFullscreen) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            })
                .background(Color.Black)
                .onGloballyPositioned { containerSize = it.size }
                .pointerInput(uiState.isLocked) {
                    if (!uiState.isLocked) {
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                val width = containerSize.width.toFloat()
                                if (width > 0) {
                                    if (offset.x < width * 0.35f) {
                                        val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                        exoPlayer.seekTo(newPos)
                                        viewModel.showSeekGestureIndicator(isForward = false, deltaSec = 10)
                                    } else if (offset.x > width * 0.65f) {
                                        val maxDur = if (exoPlayer.duration != C.TIME_UNSET && exoPlayer.duration > 0L) {
                                            exoPlayer.duration
                                        } else {
                                            uiState.totalDurationMs.coerceAtLeast(exoPlayer.currentPosition + 10000L)
                                        }
                                        val newPos = (exoPlayer.currentPosition + 10000L).coerceIn(0L, maxDur)
                                        exoPlayer.seekTo(newPos)
                                        viewModel.showSeekGestureIndicator(isForward = true, deltaSec = 10)
                                    } else {
                                        viewModel.toggleControls()
                                    }
                                }
                            },
                            onTap = {
                                viewModel.toggleControls()
                            }
                        )
                    }
                }
                .pointerInput(uiState.isLocked) {
                    if (!uiState.isLocked) {
                        var dragStartX = 0f
                        var dragStartY = 0f
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStartX = offset.x
                                dragStartY = offset.y
                            },
                            onDrag = { change, dragAmount ->
                                val width = containerSize.width.toFloat()
                                val height = containerSize.height.toFloat()

                                if (width > 0 && height > 0 && dragStartY in (height * 0.16f)..(height * 0.72f)) {
                                    change.consume()
                                    val deltaRatio = -dragAmount.y / height
                                    if (dragStartX < width * 0.5f) {
                                        currentBrightness = (currentBrightness + deltaRatio * 1.5f).coerceIn(0.05f, 1f)
                                        runCatching {
                                            activity?.window?.attributes = activity?.window?.attributes?.apply {
                                                screenBrightness = currentBrightness
                                            }
                                        }
                                        val percent = (currentBrightness * 100).toInt()
                                        viewModel.setBrightnessPercent(percent)
                                    } else {
                                        val deltaPercent = (deltaRatio * 150).toInt()
                                        val newVolPercent = (currentVolumePercent + deltaPercent).coerceIn(0, 100)
                                        currentVolumePercent = newVolPercent
                                        val targetStreamVol = ((newVolPercent / 100f) * maxVolume).toInt()
                                        runCatching {
                                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetStreamVol, 0)
                                        }
                                        viewModel.setVolumePercent(newVolPercent)
                                    }
                                }
                            }
                        )
                    }
                }
        ) {
            if (currentStreamUrl.isBlank() || isBlockedYoutubeTrailer || uiState.currentEpisode?.sources.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0E1017))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Video not available right now",
                            tint = HiAnimePink,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Video not available right now",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.anime?.titleEnglish ?: "This anime"} is not available on the streaming server yet.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else if (isPaidServerHtmlOrEmbed) {
                val htmlPayload = remember(currentStreamUrl) {
                    val trimmed = currentStreamUrl.trim()
                    val bodyMarkup = if (trimmed.startsWith("<") || trimmed.contains("<iframe", true) || trimmed.contains("<video", true)) {
                        trimmed
                    } else {
                        """<iframe src="$trimmed" allow="accelerometer; gyroscope; autoplay; encrypted-media; picture-in-picture; fullscreen" allowfullscreen="true" frameborder="0" scrolling="no" style="width:100%;height:100%;border:none;"></iframe>"""
                    }
                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                      * { margin: 0; padding: 0; box-sizing: border-box; background: #000; }
                      html, body { width: 100%; height: 100%; overflow: hidden; background: #000; display: flex; align-items: center; justify-content: center; }
                      iframe, video, mux-player, div { width: 100% !important; height: 100% !important; border: none !important; }
                    </style>
                    </head>
                    <body>$bodyMarkup</body>
                    </html>
                    """.trimIndent()
                }
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setBackgroundColor(android.graphics.Color.BLACK)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL("https://robiulislam.b-cdn.net/", htmlPayload, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        if (webView.tag != htmlPayload) {
                            webView.tag = htmlPayload
                            webView.loadDataWithBaseURL("https://robiulislam.b-cdn.net/", htmlPayload, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AndroidView(
                    factory = { ctx ->
                        (LayoutInflater.from(ctx).inflate(
                            com.example.R.layout.exo_texture_player_view,
                            null,
                            false
                        ) as PlayerView).apply {
                            player = exoPlayer
                            useController = false
                            keepScreenOn = true
                            resizeMode = if (isFullscreen) {
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            } else {
                                AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { playerView ->
                        if (playerView.player !== exoPlayer) {
                            playerView.player = exoPlayer
                        }
                        playerView.resizeMode = if (isFullscreen) {
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        } else {
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Always-Visible Corner Episode Number & Server Pill inside the Video Player
            if (!uiState.showControls) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = if (isFullscreen) 16.dp else 10.dp)
                        .background(Color.Black.copy(alpha = 0.68f), RoundedCornerShape(6.dp))
                        .border(1.dp, HiAnimePink.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EP $currentEpNum",
                        color = HiAnimePink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " • ${if (uiState.isDubMode) "DUB" else "SUB"} • ${uiState.currentSource?.cdnNode?.substringBefore(" (") ?: "HD-1"}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Keyboard Shortcut / Server Switch Toast Overlay
            if (kbdShortcutToast != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(Color(0xFF070B10).copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                        .border(1.dp, HiAnimePink, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = kbdShortcutToast ?: "",
                        color = HiAnimePink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Gesture HUD Overlay (Brightness, Volume, Seek Rewind/Forward)
            if (uiState.gestureOverlayIcon != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
                        .border(1.dp, CrimsonNeon.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (uiState.gestureOverlayIcon) {
                            "BRIGHTNESS" -> {
                                Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(30.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = uiState.gestureOverlayText ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { uiState.brightnessPercent / 100f },
                                    color = CrimsonNeon,
                                    trackColor = Color.DarkGray,
                                    modifier = Modifier.width(100.dp).height(5.dp).clip(RoundedCornerShape(3.dp))
                                )
                            }
                            "VOLUME" -> {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(30.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = uiState.gestureOverlayText ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { uiState.volumePercent / 100f },
                                    color = CrimsonNeon,
                                    trackColor = Color.DarkGray,
                                    modifier = Modifier.width(100.dp).height(5.dp).clip(RoundedCornerShape(3.dp))
                                )
                            }
                            "FORWARD" -> {
                                Icon(Icons.Default.FastForward, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = uiState.gestureOverlayText ?: "+10s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            "REWIND" -> {
                                Icon(Icons.Default.FastRewind, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = uiState.gestureOverlayText ?: "-10s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Loading Indicator
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HiAnimePink)
                }
            }

            // Skip Intro / Outro Floating Button
            if ((uiState.isInIntro || uiState.isInOutro) && !uiState.isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (uiState.showControls) 82.dp else 18.dp, end = 16.dp)
                ) {
                    Button(
                        onClick = {
                            if (uiState.isInIntro) {
                                val targetMs = ((uiState.currentEpisode?.introEndSec ?: 89L) + 1) * 1000L
                                exoPlayer.seekTo(targetMs)
                                viewModel.skipIntro()
                            } else {
                                viewModel.skipOutro()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HiAnimePink,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("skip_intro_btn")
                    ) {
                        Text(
                            text = if (uiState.isInIntro) "Skip Intro ⏩" else "Next Episode ⏭",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Lock Toggle Floating Button
            if (uiState.showControls || uiState.isLocked) {
                IconButton(
                    onClick = { viewModel.toggleLock() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .testTag("player_lock_btn")
                ) {
                    Icon(
                        imageVector = if (uiState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock",
                        tint = if (uiState.isLocked) HiAnimePink else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // YouTube Video Player Overlay Controls
            this@Column.AnimatedVisibility(
                visible = uiState.showControls && !uiState.isLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.72f),
                                    Color.Black.copy(alpha = 0.32f),
                                    Color.Black.copy(alpha = 0.78f)
                                )
                            )
                        )
                ) {
                    // YouTube Top Control Strip (Minimize/Back, Video Title, Autoplay Toggle, Cast, CC, Audio, Speed, Settings Gear)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isFullscreen) Modifier.statusBarsPadding() else Modifier)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isFullscreen) {
                            IconButton(
                                onClick = { toggleFullscreenMode() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Exit Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }

                        Text(
                            text = "Ep $currentEpNum • $currentEpTitle",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // YouTube Autoplay Toggle Pill
                        Surface(
                            onClick = { viewModel.toggleAutoNext() },
                            shape = RoundedCornerShape(999.dp),
                            color = if (uiState.autoNextEpisode) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(if (uiState.autoNextEpisode) Color.White else Color.Gray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.autoNextEpisode) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = "Autoplay",
                                        tint = Color.Black,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.autoNextEpisode) "Auto" else "Off",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // YouTube CC (Subtitles) Button
                        Surface(
                            onClick = { viewModel.setShowSubtitleSheet(true) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (uiState.selectedSubtitle != null) Color.White.copy(alpha = 0.22f) else Color.Transparent,
                            modifier = Modifier.testTag("player_subtitles_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ClosedCaption,
                                    contentDescription = "Subtitles (CC)",
                                    tint = if (uiState.selectedSubtitle != null) YouTubeRed else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // YouTube Audio / Dub Track Button
                        Surface(
                            onClick = { viewModel.setShowAudioSheet(true) },
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.testTag("player_audio_btn")
                        ) {
                            Text(
                                text = uiState.selectedAudio?.language?.uppercase(Locale.US) ?: "JA",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        // YouTube Speed Button
                        Surface(
                            onClick = { viewModel.setShowSpeedSheet(true) },
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.testTag("player_speed_btn")
                        ) {
                            Text(
                                text = "${uiState.playbackSpeed}x",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        // YouTube Settings Gear (Quality & Server Selection)
                        Surface(
                            onClick = { viewModel.setShowQualitySheet(true) },
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier.testTag("player_quality_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Quality & Server Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "HD",
                                    color = YouTubeRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // YouTube Center Play/Pause & Seek Circular Controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                .clickable { viewModel.playPreviousEpisode() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Episode",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                .clickable {
                                    val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    viewModel.showSeekGestureIndicator(isForward = false, deltaSec = 10)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // YouTube Center Play/Pause Circle
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .clickable {
                                    if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                        viewModel.setPlaying(false)
                                    } else {
                                        exoPlayer.play()
                                        viewModel.setPlaying(true)
                                    }
                                }
                                .testTag("player_play_pause_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                .clickable {
                                    val maxDur = if (exoPlayer.duration != C.TIME_UNSET && exoPlayer.duration > 0L) {
                                        exoPlayer.duration
                                    } else {
                                        uiState.totalDurationMs.coerceAtLeast(exoPlayer.currentPosition + 10000L)
                                    }
                                    val newPos = (exoPlayer.currentPosition + 10000L).coerceIn(0L, maxDur)
                                    exoPlayer.seekTo(newPos)
                                    viewModel.showSeekGestureIndicator(isForward = true, deltaSec = 10)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                .clickable { viewModel.playNextEpisode() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Episode",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // YouTube Bottom Bar (Timestamps + Episodes Pill + PiP + Fullscreen + Red Progress Scrubber)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .then(if (isFullscreen) Modifier.navigationBarsPadding() else Modifier)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    onClick = { viewModel.toggleDubSub() },
                                    shape = RoundedCornerShape(4.dp),
                                    color = YouTubeRed
                                ) {
                                    Text(
                                        text = if (uiState.isDubMode) "DUB" else "SUB",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "• Ep $currentEpNum (${uiState.currentSource?.cdnNode?.substringBefore(" (") ?: "HD-1"})",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Episodes Playlist Sheet Shortcut
                                Surface(
                                    onClick = { viewModel.setShowEpisodeListSheet(true) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.55f),
                                    modifier = Modifier.testTag("player_episodes_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistPlay,
                                            contentDescription = "Episodes",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Episodes",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Picture-in-Picture (PiP)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    IconButton(
                                        onClick = {
                                            runCatching {
                                                val hasPip = context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                                                if (hasPip) {
                                                    val params = PictureInPictureParams.Builder()
                                                        .setAspectRatio(Rational(16, 9))
                                                        .build()
                                                    activity?.enterPictureInPictureMode(params)
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureInPictureAlt,
                                            contentDescription = "PiP Mode",
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                // YouTube Fullscreen Corner Icon
                                IconButton(
                                    onClick = { toggleFullscreenMode() },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .testTag("player_fullscreen_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Toggle Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        CustomVideoProgressBar(
                            exoPlayer = exoPlayer,
                            currentPositionMs = uiState.currentPositionMs,
                            totalDurationMs = uiState.totalDurationMs,
                            bufferedPositionMs = uiState.bufferedPositionMs,
                            introStartSec = uiState.currentEpisode?.introStartSec ?: 0L,
                            introEndSec = uiState.currentEpisode?.introEndSec ?: 0L,
                            outroStartSec = uiState.currentEpisode?.outroStartSec ?: 0L,
                            outroEndSec = uiState.currentEpisode?.outroEndSec ?: 0L,
                            onPositionChanged = { currentMs, durationMs, bufferedMs ->
                                viewModel.updatePosition(currentMs, durationMs, bufferedMs)
                            },
                            onSeekCommitted = { targetPositionMs ->
                                viewModel.updatePosition(targetPositionMs, uiState.totalDurationMs, uiState.bufferedPositionMs)
                            }
                        )
                    }
                }
            }
        }

        // 3. YouTube Watch Page Below-Player Scrollable Layout (Title, Channel + Subscribe, Like/Share/Download/Save Pills, Comments Teaser, Server Switcher & Episodes Playlist)
        if (!isFullscreen) {
            val availableServers = uiState.currentEpisode?.sources.orEmpty()
            val subServers = remember(availableServers) {
                availableServers.filter { !it.audioLang.equals("dub", ignoreCase = true) }
                    .ifEmpty { availableServers }
            }
            val dubServers = remember(availableServers) {
                availableServers.filter { it.audioLang.equals("dub", ignoreCase = true) }
                    .ifEmpty { availableServers.take(3) }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(YouTubeDarkBg)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp)
            ) {
                // 3A. YouTube Video Title, Views, Relative Time, Hashtags & Expandable Description
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDescriptionExpanded = !showDescriptionExpanded }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "$animeTitle — Episode $currentEpNum: $currentEpTitle",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        maxLines = if (showDescriptionExpanded) 4 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${120 + currentEpNum * 17}K views • 2 hr ago",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "#Episode$currentEpNum #Anime #1080p",
                            color = Color(0xFF3EA6FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (showDescriptionExpanded) "Show less" else "...more",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showDescriptionExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = YouTubeSurfaceBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = uiState.anime?.description?.ifBlank {
                                        "Official 1080p HD Simulcast of $animeTitle Episode $currentEpNum ($currentEpTitle). Streamed via Bunny.net Global Edge CDN with multi-language subtitles and dual audio."
                                    } ?: "Official 1080p HD Simulcast of $animeTitle Episode $currentEpNum.",
                                    color = Color(0xFFE5E5E5),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Studio: ${uiState.anime?.studio ?: "Official Studio"} • Year: ${uiState.anime?.releaseYear ?: 2025} • Audio: ${if (uiState.isDubMode) "English Dub" else "Japanese Sub"}",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // 3B. YouTube Channel Row + Official Subscribe Pill Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = uiState.anime?.posterUrl ?: "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
                            contentDescription = animeTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(YouTubeChipBg)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.anime?.studio?.ifBlank { "Robiul Anime Official" } ?: "Robiul Anime Official",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Channel",
                                    tint = Color(0xFFAAAAAA),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = "1.84M subscribers",
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            isChannelSubscribed = !isChannelSubscribed
                            kbdShortcutToast = if (isChannelSubscribed) "🔔 Subscribed to channel" else "Unsubscribed"
                        },
                        shape = RoundedCornerShape(999.dp),
                        color = if (isChannelSubscribed) YouTubeChipBg else Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isChannelSubscribed) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (isChannelSubscribed) "Subscribed" else "Subscribe",
                                color = if (isChannelSubscribed) Color.White else Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3C. YouTube Horizontal Action Pills Row (Like/Dislike, Share, Download, Save/Bookmark, Auto Next, Skip Intro)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Segmented Like | Dislike Pill
                    item {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = YouTubeChipBg
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        if (isVideoLiked) {
                                            isVideoLiked = false
                                            likeCountValue -= 1
                                        } else {
                                            isVideoLiked = true
                                            isVideoDisliked = false
                                            likeCountValue += 1
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isVideoLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Like",
                                        tint = if (isVideoLiked) YouTubeRed else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", likeCountValue / 1000.0)}K",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = "  |  ",
                                    color = Color.White.copy(alpha = 0.25f),
                                    fontSize = 12.sp
                                )

                                Icon(
                                    imageVector = if (isVideoDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                                    contentDescription = "Dislike",
                                    tint = if (isVideoDisliked) YouTubeRed else Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            isVideoDisliked = !isVideoDisliked
                                            if (isVideoDisliked && isVideoLiked) {
                                                isVideoLiked = false
                                                likeCountValue -= 1
                                            }
                                        }
                                )
                            }
                        }
                    }

                    // YouTube Share Pill
                    item {
                        YouTubeActionPill(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Link copied: $animeTitle • Episode $currentEpNum")
                                }
                            }
                        )
                    }

                    // YouTube Download Pill
                    if (downloadsRepository != null && uiState.anime != null && uiState.currentEpisode != null) {
                        item {
                            YouTubeActionPill(
                                icon = Icons.Default.FileDownload,
                                label = "Download Ep $currentEpNum",
                                onClick = {
                                    scope.launch {
                                        downloadsRepository.startDownload(uiState.anime!!, uiState.currentEpisode!!)
                                        snackbarHostState.showSnackbar("Downloading Ep $currentEpNum for offline watching...")
                                    }
                                }
                            )
                        }
                    }

                    // YouTube Save / Bookmark Pill (preserves testTag)
                    item {
                        YouTubeActionPill(
                            icon = Icons.Default.BookmarkBorder,
                            label = "Save",
                            onClick = { viewModel.setShowBookmarksNotesSheet(true) },
                            testTag = "player_bookmarks_notes_btn"
                        )
                    }

                    // Prev / Next Episode Pills
                    item {
                        HiAnimeActionChip(
                            label = "⏮ Prev Ep",
                            onClick = { viewModel.playPreviousEpisode() }
                        )
                    }
                    item {
                        HiAnimeActionChip(
                            label = "Next Ep ⏭",
                            onClick = { viewModel.playNextEpisode() }
                        )
                    }

                    // Auto Next & Skip Intro Pills
                    item {
                        HiAnimeToggleChip(
                            label = "Auto Next",
                            isEnabled = uiState.autoNextEpisode,
                            onClick = { viewModel.toggleAutoNext() }
                        )
                    }
                    item {
                        HiAnimeToggleChip(
                            label = "Skip Intro",
                            isEnabled = uiState.autoSkipIntro,
                            onClick = { viewModel.toggleAutoSkipIntro() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3D. YouTube Comments Preview Teaser Card
                Surface(
                    onClick = { viewModel.setShowCommentsSheet(true) },
                    shape = RoundedCornerShape(12.dp),
                    color = YouTubeSurfaceBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Comments",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${140 + currentEpNum * 9}",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "Tap to view all ▾",
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = YouTubeRed,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("A", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔥 Episode $currentEpNum in 1080p looks incredible! Smooth playback & zero buffering.",
                                color = Color(0xFFE5E5E5),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3E. YouTube Stream Server & Audio Source Card (SUB / DUB Server Switcher)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(YouTubeSurfaceBg)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .testTag("player_quick_server_row")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222222))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Now Playing • ",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Episode $currentEpNum",
                                    color = YouTubeRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "Switch stream server or audio language (SUB / DUB) instantly below:",
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = YouTubeRed
                        ) {
                            Text(
                                text = "EP $currentEpNum",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    if (availableServers.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Video not available right now",
                                color = YouTubeRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        // SUB Servers Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(62.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SUB:",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(subServers, key = { "sub_${it.id}" }) { serverSource ->
                                    val isSelected = uiState.currentSource?.id == serverSource.id
                                    Surface(
                                        onClick = {
                                            viewModel.selectSource(serverSource)
                                            kbdShortcutToast = "⚡ SUB Server → ${serverSource.cdnNode}"
                                        },
                                        color = if (isSelected) YouTubeRed else YouTubeChipBg,
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Text(
                                            text = serverSource.cdnNode.substringBefore(" ("),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                        // DUB Servers Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(62.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = Color(0xFF3EA6FF),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DUB:",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(dubServers, key = { "dub_${it.id}" }) { serverSource ->
                                    val isSelected = uiState.currentSource?.id == serverSource.id
                                    Surface(
                                        onClick = {
                                            viewModel.selectSource(serverSource)
                                            kbdShortcutToast = "🎙️ DUB Server → ${serverSource.cdnNode}"
                                        },
                                        color = if (isSelected) YouTubeRed else YouTubeChipBg,
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Text(
                                            text = serverSource.cdnNode.replace(" DUB", "").substringBefore(" ("),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3F. YouTube Playlist & Up Next Episodes Section with Numbered Episode Grid (1, 2, 3...)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(YouTubeSurfaceBg)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("hianime_episodes_section")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Episodes Playlist",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "Playing Episode $currentEpNum of $totalEpisodesCount • $animeTitle",
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Grid vs YouTube Up Next Feed Toggle
                            Surface(
                                onClick = { isEpisodeGridMode = true },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isEpisodeGridMode) YouTubeRed else YouTubeChipBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Number Grid",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "1..$totalEpisodesCount",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                onClick = { isEpisodeGridMode = false },
                                shape = RoundedCornerShape(999.dp),
                                color = if (!isEpisodeGridMode) YouTubeRed else YouTubeChipBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                        contentDescription = "Up Next List",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Up Next",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Episode Number Search Filter ("Number of Ep") with Enter-to-Play support
                    val submitEpisodeJump: () -> Unit = {
                        val trimmed = episodeSearchQuery.trim()
                        val parsedEp = trimmed.filter { it.isDigit() }.toIntOrNull()
                        if (parsedEp != null && parsedEp in 1..totalEpisodesCount) {
                            viewModel.selectEpisode(parsedEp)
                            kbdShortcutToast = "▶ Playing Episode $parsedEp"
                        } else {
                            val matchedEp = guaranteedEpisodes.firstOrNull {
                                it.episodeNumber.toString() == trimmed ||
                                    it.title.contains(trimmed, ignoreCase = true)
                            }
                            if (matchedEp != null) {
                                viewModel.selectEpisode(matchedEp.episodeNumber)
                                kbdShortcutToast = "▶ Playing Episode ${matchedEp.episodeNumber}"
                            }
                        }
                    }

                    OutlinedTextField(
                        value = episodeSearchQuery,
                        onValueChange = { episodeSearchQuery = it },
                        placeholder = {
                            Text("Search Episode 1, 2, 3... (Press Enter to play)", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                        },
                        trailingIcon = {
                            Button(
                                onClick = { submitEpisodeJump() },
                                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                shape = RoundedCornerShape(999.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .height(30.dp)
                            ) {
                                Text("Play ↵", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { submitEpisodeJump() },
                            onDone = { submitEpisodeJump() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YouTubeRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = YouTubeDarkBg,
                            unfocusedContainerColor = YouTubeDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown &&
                                    (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter)
                                ) {
                                    submitEpisodeJump()
                                    true
                                } else {
                                    false
                                }
                            }
                            .testTag("player_episode_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredEpisodes = remember(guaranteedEpisodes, episodeSearchQuery) {
                        val q = episodeSearchQuery.trim()
                        if (q.isEmpty()) {
                            guaranteedEpisodes
                        } else {
                            guaranteedEpisodes.filter { ep ->
                                ep.episodeNumber.toString().contains(q) ||
                                    ep.title.contains(q, ignoreCase = true)
                            }.ifEmpty { guaranteedEpisodes }
                        }
                    }

                    if (isEpisodeGridMode) {
                        // Numbered Episode Grid (Buttons showing 1, 2, 3, 4, 5...) with YouTube Red active state
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredEpisodes.chunked(5).forEach { rowEpisodes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowEpisodes.forEach { ep ->
                                        val isCurrent = ep.episodeNumber == currentEpNum
                                        Surface(
                                            onClick = {
                                                viewModel.selectEpisode(ep.episodeNumber)
                                                kbdShortcutToast = "▶ Playing Episode ${ep.episodeNumber}"
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCurrent) YouTubeRed else YouTubeChipBg,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .border(
                                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                                    color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .testTag("episode_grid_btn_${ep.episodeNumber}")
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = if (isCurrent) "▶ ${ep.episodeNumber}" else "${ep.episodeNumber}",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    repeat(5 - rowEpisodes.size) {
                                        Spacer(modifier = Modifier.weight(1f).height(40.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        // YouTube "Up Next" Horizontal Thumbnail Episode Cards
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filteredEpisodes.forEach { ep ->
                                val isCurrent = ep.episodeNumber == currentEpNum
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isCurrent) YouTubeRed.copy(alpha = 0.16f) else YouTubeDarkBg)
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrent) YouTubeRed else Color.White.copy(alpha = 0.06f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            viewModel.selectEpisode(ep.episodeNumber)
                                            kbdShortcutToast = "▶ Playing Episode ${ep.episodeNumber}"
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 16:9 YouTube Episode Thumbnail with Bottom-Right Duration Badge
                                    Box(
                                        modifier = Modifier
                                            .width(126.dp)
                                            .height(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                    ) {
                                        AsyncImage(
                                            model = ep.thumbnailUrl.ifBlank {
                                                uiState.anime?.bannerUrl ?: "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg"
                                            },
                                            contentDescription = ep.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = if (isCurrent) YouTubeRed else Color.Black.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = "EP ${ep.episodeNumber}",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = if (isCurrent) "PLAYING" else "24:00",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Episode ${ep.episodeNumber} — ${ep.title}",
                                            color = if (isCurrent) YouTubeRed else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${uiState.anime?.studio ?: "Robiul Anime"} • ${85 + ep.episodeNumber * 12}K views",
                                            color = Color(0xFFAAAAAA),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))

        // Comments Bottom Sheet
        if (uiState.showCommentsSheet && commentsRepository != null && uiState.anime != null && uiState.currentEpisode != null) {
            EpisodeCommentsSheet(
                animeId = uiState.anime!!.id,
                episodeNumber = uiState.currentEpisode!!.episodeNumber,
                commentsRepository = commentsRepository,
                onDismiss = { viewModel.setShowCommentsSheet(false) }
            )
        }

        // Quality & Server Selection Sheet
        if (uiState.showQualitySheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowQualitySheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Select Stream Server & Quality", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    uiState.currentEpisode?.sources?.forEach { src ->
                        val isSelected = uiState.currentSource?.id == src.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectQuality(src) }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = src.quality, color = if (isSelected) HiAnimePink else TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = src.cdnNode, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Subtitle Selection Sheet
        if (uiState.showSubtitleSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowSubtitleSheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Select Subtitles", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSubtitle(null) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(text = "Off", color = if (uiState.selectedSubtitle == null) HiAnimePink else TextPrimary)
                    }

                    uiState.currentEpisode?.subtitles?.forEach { sub ->
                        val isSelected = uiState.selectedSubtitle?.id == sub.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSubtitle(sub) }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.label,
                                color = if (isSelected) HiAnimePink else TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isSelected) {
                                Text(text = "✓ Active", color = HiAnimePink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🎨 Subtitle Styling & Delay (${uiState.subtitleFontSizeSp}sp • Delay ${uiState.subtitleDelayMs}ms)",
                        color = HiAnimePink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(14, 16, 20, 24).forEach { sz ->
                            Surface(
                                onClick = { viewModel.updateSubtitleStyling(fontSizeSp = sz) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (uiState.subtitleFontSizeSp == sz) CrimsonNeon else SurfaceDark
                            ) {
                                Text(
                                    text = "${sz}sp",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.updateSubtitleStyling(delayMs = uiState.subtitleDelayMs - 250L) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                        ) {
                            Text("-250ms Delay", color = TextPrimary, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.updateSubtitleStyling(delayMs = 0L) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                        ) {
                            Text("Reset 0ms", color = TextPrimary, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.updateSubtitleStyling(delayMs = uiState.subtitleDelayMs + 250L) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                        ) {
                            Text("+250ms Delay", color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Audio Dub Selection Sheet
        if (uiState.showAudioSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowAudioSheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Select Audio Track", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    uiState.currentEpisode?.audioTracks?.forEach { audio ->
                        val isSelected = uiState.selectedAudio?.id == audio.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectAudio(audio) }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = audio.label, color = if (isSelected) HiAnimePink else TextPrimary, fontWeight = FontWeight.SemiBold)
                            if (isSelected) {
                                Text(text = "✓ Selected", color = HiAnimePink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Speed Selection & Sleep Timer Sheet
        if (uiState.showSpeedSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowSpeedSheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Playback Speed", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                        val isSelected = uiState.playbackSpeed == speed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setPlaybackSpeed(speed) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${speed}x", color = if (isSelected) HiAnimePink else TextPrimary, fontWeight = FontWeight.SemiBold)
                            if (isSelected) {
                                Text(text = "✓ Active", color = HiAnimePink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "⏲️ Sleep Timer", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Off", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "60m").forEach { (mins, label) ->
                            Surface(
                                onClick = { viewModel.setSleepTimer(mins) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (uiState.sleepTimerMinutes == mins) CrimsonNeon else SurfaceDark
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Bookmarks & Personal Episode Notes Sheet
        if (uiState.showBookmarksNotesSheet) {
            val bookmarks by viewModel.videoBookmarks.collectAsStateWithLifecycle()
            val notes by viewModel.episodeNotes.collectAsStateWithLifecycle()
            var bookmarkLabelInput by remember { mutableStateOf("") }
            var noteTextInput by remember { mutableStateOf("") }

            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowBookmarksNotesSheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔖 Custom Timestamp Bookmarks & 📌 Episode Notes",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                viewModel.addTimestampBookmark(bookmarkLabelInput.ifBlank { "Key Scene (Ep $currentEpNum)" })
                                bookmarkLabelInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                        ) {
                            Text("🔖 Bookmark ${formatTime(uiState.currentPositionMs)}", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.addPersonalEpisodeNote(noteTextInput.ifBlank { "Great scene in Episode $currentEpNum!" })
                                noteTextInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21293A))
                        ) {
                            Text("📌 Save Quick Note", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Saved Scene Bookmarks:", color = HiAnimePink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    bookmarks.take(4).forEach { bm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    exoPlayer.seekTo(bm.timestampMs)
                                    viewModel.updatePosition(bm.timestampMs)
                                    viewModel.setShowBookmarksNotesSheet(false)
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${bm.formattedTimestamp} — ${bm.label}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Jump ⏩", color = HiAnimePink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Personal Episode Notes:", color = HiAnimePink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    notes.take(3).forEach { note ->
                        Text(
                            text = "• Ep ${note.episodeNumber}: “${note.noteText}”",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Episode List Modal Sheet (also accessible from Fullscreen mode)
        if (uiState.showEpisodeListSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowEpisodeListSheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Episode (1 - $totalEpisodesCount)",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.height(360.dp)) {
                        items(uiState.allEpisodes, key = { it.id }) { ep ->
                            val isCurrent = uiState.currentEpisode?.id == ep.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.loadPlaybackSession(ep.episodeNumber)
                                        viewModel.setShowEpisodeListSheet(false)
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCurrent) HiAnimePink else HiAnimeEpisodeIdle,
                                    modifier = Modifier.width(54.dp)
                                ) {
                                    Text(
                                        text = "Ep ${ep.episodeNumber}",
                                        color = if (isCurrent) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = ep.title,
                                    color = if (isCurrent) HiAnimePink else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isCurrent) {
                                    Text(text = "NOW PLAYING", color = HiAnimePink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YouTubeActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = YouTubeChipBg,
        modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HiAnimeToggleChip(
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = YouTubeChipBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                color = Color(0xFFAAAAAA),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isEnabled) "ON" else "OFF",
                color = if (isEnabled) YouTubeRed else Color(0xFFAAAAAA),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun HiAnimeActionChip(
    label: String,
    onClick: () -> Unit,
    testTag: String? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = YouTubeChipBg,
        modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
