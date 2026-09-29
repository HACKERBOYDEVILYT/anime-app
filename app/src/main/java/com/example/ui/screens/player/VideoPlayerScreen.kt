package com.example.ui.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun VideoPlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler { onBack() }

    // ExoPlayer Instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // Set Media Item when currentSource changes
    LaunchedEffect(uiState.currentSource?.streamUrl) {
        val streamUrl = uiState.currentSource?.streamUrl
        if (!streamUrl.isNullOrBlank()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
            exoPlayer.setMediaItem(mediaItem)
            if (uiState.currentPositionMs > 0) {
                exoPlayer.seekTo(uiState.currentPositionMs)
            }
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    // Track playback parameters (speed)
    LaunchedEffect(uiState.playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(uiState.playbackSpeed)
    }

    // Keep state in sync with ExoPlayer position
    LaunchedEffect(exoPlayer) {
        while (true) {
            val currentPos = exoPlayer.currentPosition
            val totalDur = exoPlayer.duration.coerceAtLeast(1L)
            viewModel.updatePosition(currentPos, totalDur)
            viewModel.setPlaying(exoPlayer.isPlaying)
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(uiState.showControls, uiState.isPlaying) {
        if (uiState.showControls && uiState.isPlaying && !uiState.isLocked) {
            delay(4000)
            viewModel.toggleControls()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_container")
    ) {
        // ExoPlayer View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // We render our own premium custom UI controls
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.toggleControls()
                }
        )

        // Loading Indicator
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CrimsonNeon)
            }
        }

        // Skip Intro Button Prompt (Overlay on right side)
        if (uiState.isInIntro && !uiState.isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 20.dp)
            ) {
                Button(
                    onClick = {
                        val targetMs = ((uiState.currentEpisode?.introEndSec ?: 180L) + 1) * 1000L
                        exoPlayer.seekTo(targetMs)
                        viewModel.skipIntro()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.85f),
                        contentColor = CrimsonNeon
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("skip_intro_btn")
                ) {
                    Text(
                        text = "Skip Intro ⏩",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Lock Toggle Floating Button (Visible when controls shown or when locked)
        if (uiState.showControls || uiState.isLocked) {
            IconButton(
                onClick = { viewModel.toggleLock() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .testTag("player_lock_btn")
            ) {
                Icon(
                    imageVector = if (uiState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Lock",
                    tint = if (uiState.isLocked) CrimsonNeon else Color.White
                )
            }
        }

        // Player Controls Overlay
        AnimatedVisibility(
            visible = uiState.showControls && !uiState.isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("player_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.anime?.titleEnglish ?: "Streaming Anime",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        uiState.currentEpisode?.let { ep ->
                            Text(
                                text = "Ep ${ep.episodeNumber}: ${ep.title}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Episodes Playlist Shortcut
                    IconButton(
                        onClick = { viewModel.setShowEpisodeListSheet(true) },
                        modifier = Modifier.testTag("player_episodes_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = "Episodes",
                            tint = Color.White
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
                    // Previous Episode
                    IconButton(
                        onClick = { viewModel.playPreviousEpisode() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Rewind 10s
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Play / Pause Main Button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
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
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Fast Forward 10s
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration)
                            exoPlayer.seekTo(newPos)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Next Episode
                    IconButton(
                        onClick = { viewModel.playNextEpisode() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Timeline & Settings Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Timeline Slider & Timestamps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(uiState.currentPositionMs),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Slider(
                            value = if (uiState.totalDurationMs > 0) uiState.currentPositionMs.toFloat() / uiState.totalDurationMs.toFloat() else 0f,
                            onValueChange = { ratio ->
                                val target = (ratio * uiState.totalDurationMs).toLong()
                                exoPlayer.seekTo(target)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = CrimsonNeon,
                                activeTrackColor = CrimsonNeon,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        )

                        Text(
                            text = formatTime(uiState.totalDurationMs),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Settings & Selectors Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quality Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(SurfaceDark.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .clickable { viewModel.setShowQualitySheet(true) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("player_quality_btn")
                        ) {
                            Icon(Icons.Default.HighQuality, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = uiState.selectedQuality, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Subtitle Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(SurfaceDark.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .clickable { viewModel.setShowSubtitleSheet(true) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("player_subtitles_btn")
                        ) {
                            Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.selectedSubtitle?.label ?: "Sub: Off",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Audio Dub Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(SurfaceDark.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .clickable { viewModel.setShowAudioSheet(true) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("player_audio_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.selectedAudio?.language?.uppercase() ?: "JA",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Speed Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(SurfaceDark.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .clickable { viewModel.setShowSpeedSheet(true) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("player_speed_btn")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${uiState.playbackSpeed}x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quality Selection Sheet
        if (uiState.showQualitySheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowQualitySheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Select Stream Quality", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            Text(text = src.quality, color = if (isSelected) CrimsonNeon else TextPrimary, fontWeight = FontWeight.SemiBold)
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
                    // Off option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSubtitle(null) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(text = "Off", color = if (uiState.selectedSubtitle == null) CrimsonNeon else TextPrimary)
                    }
                    uiState.currentEpisode?.subtitles?.forEach { sub ->
                        val isSelected = uiState.selectedSubtitle?.id == sub.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSubtitle(sub) }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(text = sub.label, color = if (isSelected) CrimsonNeon else TextPrimary, fontWeight = FontWeight.SemiBold)
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
                                .padding(vertical = 10.dp)
                        ) {
                            Text(text = audio.label, color = if (isSelected) CrimsonNeon else TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Speed Selection Sheet
        if (uiState.showSpeedSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowSpeedSheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Playback Speed", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val isSelected = uiState.playbackSpeed == speed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setPlaybackSpeed(speed) }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(text = "${speed}x", color = if (isSelected) CrimsonNeon else TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Episode List Sheet
        if (uiState.showEpisodeListSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowEpisodeListSheet(false) },
                containerColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Select Episode", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.height(350.dp)) {
                        items(uiState.allEpisodes) { ep ->
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
                                Text(
                                    text = "Ep ${ep.episodeNumber}",
                                    color = if (isCurrent) CrimsonNeon else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(48.dp)
                                )
                                Text(
                                    text = ep.title,
                                    color = if (isCurrent) CrimsonNeon else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000L
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
