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
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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

private val HiAnimePink = Color(0xFFFFBADE)
private val HiAnimeDarkBg = Color(0xFF14151A)
private val HiAnimePanelBg = Color(0xFF1C1E26)
private val HiAnimeEpisodeIdle = Color(0xFF252833)

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
    val isWebEmbedOrTrailer = remember(currentStreamUrl) {
        currentStreamUrl.contains("youtube.com/embed", ignoreCase = true) ||
            currentStreamUrl.contains("youtube.com/watch", ignoreCase = true) ||
            currentStreamUrl.contains("youtu.be/", ignoreCase = true) ||
            currentStreamUrl.contains("/embed/", ignoreCase = true)
    }

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
        } else if (isWebEmbedOrTrailer) {
            exoPlayer.pause()
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

    val currentEpNum = uiState.currentEpisode?.episodeNumber ?: 1
    val currentEpTitle = uiState.currentEpisode?.title ?: "Episode $currentEpNum"
    val totalEpisodesCount = uiState.allEpisodes.size.coerceAtLeast(1)

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
        // 1. HiAnime Top Navigation Header (Visible in normal mode)
        if (!isFullscreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background( Color(0xFF191B24))
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("player_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // HiAnime Brand Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF252836)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "h!",
                            color = HiAnimePink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "anime",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.anime?.titleEnglish ?: "Streaming Anime",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Episode $currentEpNum • $currentEpTitle",
                        color = HiAnimePink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("player_header_episode_number")
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Prominent Episode Number Pill Badge in Header
                Surface(
                    onClick = { viewModel.setShowEpisodeListSheet(true) },
                    shape = RoundedCornerShape(8.dp),
                    color = CrimsonNeon,
                    modifier = Modifier.testTag("player_ep_badge_btn")
                ) {
                    Text(
                        text = "EP $currentEpNum / $totalEpisodesCount",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
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
            if (isWebEmbedOrTrailer) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setBackgroundColor(android.graphics.Color.BLACK)
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            loadUrl(currentStreamUrl)
                        }
                    },
                    update = { webView ->
                        if (webView.url != currentStreamUrl && currentStreamUrl.isNotBlank()) {
                            webView.loadUrl(currentStreamUrl)
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

            // Video Player Overlay Controls
            this@Column.AnimatedVisibility(
                visible = uiState.showControls && !uiState.isLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.52f))
                ) {
                    // Overlay Top Bar inside Video Stage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isFullscreen) Modifier.statusBarsPadding() else Modifier)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFullscreen) {
                            IconButton(
                                onClick = { toggleFullscreenMode() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Exit Fullscreen",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Episode Number Badge inside Player Overlay
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HiAnimePink
                        ) {
                            Text(
                                text = "EPISODE $currentEpNum",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = currentEpTitle,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // SUB / DUB quick toggle pill
                        Surface(
                            onClick = { viewModel.toggleDubSub() },
                            shape = RoundedCornerShape(14.dp),
                            color = if (uiState.isDubMode) CrimsonNeon else SurfaceDark.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = if (uiState.isDubMode) "🎙️ DUB" else "🏳️ SUB",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Picture-in-Picture (PiP) guarded for all Android versions
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
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPictureAlt,
                                    contentDescription = "PiP Mode",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Episodes Playlist Sheet Shortcut
                        IconButton(
                            onClick = { viewModel.setShowEpisodeListSheet(true) },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("player_episodes_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistPlay,
                                contentDescription = "Episodes",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Center Play/Pause & Seek Controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.playPreviousEpisode() },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Episode",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = {
                                val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(newPos)
                                viewModel.showSeekGestureIndicator(isForward = false, deltaSec = 10)
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(CrimsonNeon, CircleShape)
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
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = {
                                val maxDur = if (exoPlayer.duration != C.TIME_UNSET && exoPlayer.duration > 0L) {
                                    exoPlayer.duration
                                } else {
                                    uiState.totalDurationMs.coerceAtLeast(exoPlayer.currentPosition + 10000L)
                                }
                                val newPos = (exoPlayer.currentPosition + 10000L).coerceIn(0L, maxDur)
                                exoPlayer.seekTo(newPos)
                                viewModel.showSeekGestureIndicator(isForward = true, deltaSec = 10)
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = { viewModel.playNextEpisode() },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Episode",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Bottom Timeline & Settings Strip inside Video Stage
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .then(if (isFullscreen) Modifier.navigationBarsPadding() else Modifier)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
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
                            },
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quality & Server Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(SurfaceDark.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setShowQualitySheet(true) }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                    .testTag("player_quality_btn")
                            ) {
                                Icon(Icons.Default.HighQuality, contentDescription = null, tint = HiAnimePink, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.currentSource?.cdnNode?.substringBefore(" (") ?: "HD-1",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            // Subtitles Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(SurfaceDark.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setShowSubtitleSheet(true) }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                    .testTag("player_subtitles_btn")
                            ) {
                                Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = HiAnimePink, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.selectedSubtitle?.label ?: "Sub: Off",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Audio Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(SurfaceDark.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setShowAudioSheet(true) }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                    .testTag("player_audio_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = HiAnimePink, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.selectedAudio?.language?.uppercase(Locale.US) ?: "JA",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Speed Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(SurfaceDark.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setShowSpeedSheet(true) }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                    .testTag("player_speed_btn")
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = HiAnimePink, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.playbackSpeed}x",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Fullscreen Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        if (isFullscreen) CrimsonNeon else SurfaceDark.copy(alpha = 0.85f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { toggleFullscreenMode() }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                    .testTag("player_fullscreen_btn")
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Toggle Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isFullscreen) "Exit" else "Full",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. HiAnime Below-Player Scrollable Dashboard (Controls Bar + "You are watching Episode X" + SUB/DUB Server Switcher + Numbered Episode Grid)
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
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            ) {
                // 3A. HiAnime Quick Action & Auto-Controls Bar directly under Video Player
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF181A22))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    item {
                        HiAnimeActionChip(
                            label = "💬 Comments",
                            onClick = { viewModel.setShowCommentsSheet(true) }
                        )
                    }
                    item {
                        HiAnimeActionChip(
                            label = "🔖 Bookmark",
                            onClick = { viewModel.setShowBookmarksNotesSheet(true) },
                            testTag = "player_bookmarks_notes_btn"
                        )
                    }
                    if (downloadsRepository != null && uiState.anime != null && uiState.currentEpisode != null) {
                        item {
                            HiAnimeActionChip(
                                label = "⬇ Download Ep $currentEpNum",
                                onClick = {
                                    scope.launch {
                                        downloadsRepository.startDownload(uiState.anime!!, uiState.currentEpisode!!)
                                        snackbarHostState.showSnackbar("Downloading Ep $currentEpNum for offline watching...")
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3B. HiAnime Signature "You are watching Episode X" & SUB / DUB Server Switcher Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HiAnimePanelBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .testTag("player_quick_server_row")
                ) {
                    // Top Pink/Dark Banner: "You are watching Episode X"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF2B1C2C), Color(0xFF1F222D))
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "You are watching ",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Episode $currentEpNum",
                                    color = HiAnimePink,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = currentEpTitle,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "If current server doesn't work please try other servers below.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HiAnimePink
                        ) {
                            Text(
                                text = "EP $currentEpNum",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorder)

                    // SUB Servers Row (HiAnime Style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(62.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = null,
                                tint = HiAnimePink,
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
                                    color = if (isSelected) HiAnimePink else HiAnimeEpisodeIdle,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = serverSource.cdnNode.substringBefore(" ("),
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                    // DUB Servers Row (HiAnime Style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(62.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = CyanGlow,
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
                                    color = if (isSelected) HiAnimePink else HiAnimeEpisodeIdle,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = serverSource.cdnNode.replace(" DUB", "").substringBefore(" ("),
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3C. HiAnime "List of episodes:" Section with Numbered Episode Grid (1, 2, 3...)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HiAnimePanelBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("hianime_episodes_section")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "List of episodes:",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Playing Episode $currentEpNum of $totalEpisodesCount",
                                color = HiAnimePink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Grid vs List View Toggle
                            Surface(
                                onClick = { isEpisodeGridMode = true },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isEpisodeGridMode) HiAnimePink else HiAnimeEpisodeIdle
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Number Grid",
                                        tint = if (isEpisodeGridMode) Color.Black else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "1..$totalEpisodesCount",
                                        color = if (isEpisodeGridMode) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                onClick = { isEpisodeGridMode = false },
                                shape = RoundedCornerShape(6.dp),
                                color = if (!isEpisodeGridMode) HiAnimePink else HiAnimeEpisodeIdle
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                        contentDescription = "Episode List",
                                        tint = if (!isEpisodeGridMode) Color.Black else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Titles",
                                        color = if (!isEpisodeGridMode) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Episode Number Search Filter ("Number of Ep")
                    OutlinedTextField(
                        value = episodeSearchQuery,
                        onValueChange = { episodeSearchQuery = it },
                        placeholder = {
                            Text("Number of Ep (e.g. 1, 5, 12)...", color = TextMuted, fontSize = 12.sp)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HiAnimePink,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = HiAnimeDarkBg,
                            unfocusedContainerColor = HiAnimeDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredEpisodes = remember(uiState.allEpisodes, episodeSearchQuery) {
                        val q = episodeSearchQuery.trim()
                        if (q.isEmpty()) {
                            uiState.allEpisodes
                        } else {
                            uiState.allEpisodes.filter { ep ->
                                ep.episodeNumber.toString().contains(q) ||
                                    ep.title.contains(q, ignoreCase = true)
                            }
                        }
                    }

                    if (isEpisodeGridMode) {
                        // HiAnime Numbered Episode Grid (Buttons showing 1, 2, 3, 4, 5...)
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
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isCurrent) HiAnimePink else HiAnimeEpisodeIdle,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .border(
                                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                                    color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .testTag("episode_grid_btn_${ep.episodeNumber}")
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = if (isCurrent) "▶ ${ep.episodeNumber}" else "${ep.episodeNumber}",
                                                    color = if (isCurrent) Color.Black else Color.White,
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
                        // HiAnime Numbered Episode List with Episode Number + Title
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filteredEpisodes.forEach { ep ->
                                val isCurrent = ep.episodeNumber == currentEpNum
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCurrent) HiAnimePink.copy(alpha = 0.18f) else HiAnimeEpisodeIdle)
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrent) HiAnimePink else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            viewModel.selectEpisode(ep.episodeNumber)
                                            kbdShortcutToast = "▶ Playing Episode ${ep.episodeNumber}"
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCurrent) HiAnimePink else Color(0xFF191B24)
                                    ) {
                                        Text(
                                            text = "${ep.episodeNumber}",
                                            color = if (isCurrent) Color.Black else HiAnimePink,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = ep.title,
                                        color = if (isCurrent) HiAnimePink else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isCurrent) {
                                        Text(
                                            text = "▶ PLAYING",
                                            color = HiAnimePink,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
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
private fun HiAnimeToggleChip(
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF252833)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isEnabled) "ON" else "OFF",
                color = if (isEnabled) HiAnimePink else TextMuted,
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
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF252833),
        modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
