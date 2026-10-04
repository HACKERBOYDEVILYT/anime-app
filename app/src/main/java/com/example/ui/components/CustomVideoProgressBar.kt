package com.example.ui.components

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Custom Progress Bar UI Component for the Video Player integrated with Media3 ExoPlayer.
 *
 * Features:
 * - Direct integration with Media3 [Player] / ExoPlayer for live playback position, total duration,
 *   and buffered position updates.
 * - Custom Canvas-rendered multi-layer timeline:
 *   1. Inactive track background
 *   2. Media3 network buffer progress bar
 *   3. Opening (OP) and Ending (ED) chapter markers on the timeline
 *   4. Active neon gradient playback progress bar
 *   5. Animated glowing scrubber thumb
 * - Interactive tap-to-seek and smooth horizontal drag scrubbing with a floating timestamp preview bubble.
 * - Displays formatted current playback time, total duration, buffered percentage, and toggleable remaining time.
 */
@OptIn(UnstableApi::class)
@Composable
fun CustomVideoProgressBar(
    exoPlayer: Player,
    currentPositionMs: Long,
    totalDurationMs: Long,
    bufferedPositionMs: Long = 0L,
    introStartSec: Long = 0L,
    introEndSec: Long = 0L,
    outroStartSec: Long = 0L,
    outroEndSec: Long = 0L,
    onPositionChanged: (currentMs: Long, durationMs: Long, bufferedMs: Long) -> Unit = { _, _, _ -> },
    onSeekCommitted: (targetPositionMs: Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var livePositionMs by remember { mutableLongStateOf(currentPositionMs.coerceAtLeast(0L)) }
    var liveDurationMs by remember { mutableLongStateOf(totalDurationMs.coerceAtLeast(1L)) }
    var liveBufferedMs by remember { mutableLongStateOf(bufferedPositionMs.coerceAtLeast(0L)) }

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubRatio by remember { mutableFloatStateOf(0f) }
    var showRemainingTime by remember { mutableStateOf(false) }
    var barWidthPx by remember { mutableFloatStateOf(1f) }

    // Keep state synced when external props update and user is not actively scrubbing
    LaunchedEffect(currentPositionMs, totalDurationMs, bufferedPositionMs) {
        if (!isScrubbing) {
            livePositionMs = currentPositionMs.coerceAtLeast(0L)
        }
        if (totalDurationMs > 0L && totalDurationMs != C.TIME_UNSET) {
            liveDurationMs = totalDurationMs
        }
        if (bufferedPositionMs >= 0L) {
            liveBufferedMs = bufferedPositionMs
        }
    }

    // Listen to Media3 ExoPlayer events directly for instant duration / seek / buffer updates
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                val playerDur = player.duration
                val resolvedDur = if (playerDur != C.TIME_UNSET && playerDur > 0L) {
                    playerDur
                } else {
                    liveDurationMs.coerceAtLeast(1L)
                }
                val resolvedPos = player.currentPosition.coerceIn(0L, resolvedDur)
                val resolvedBuf = player.bufferedPosition.coerceIn(0L, resolvedDur)

                if (!isScrubbing) {
                    livePositionMs = resolvedPos
                }
                liveDurationMs = resolvedDur
                liveBufferedMs = resolvedBuf
                onPositionChanged(resolvedPos, resolvedDur, resolvedBuf)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // High-frequency 200ms ticker for smooth progress bar movement while Media3 ExoPlayer is playing
    LaunchedEffect(exoPlayer, isScrubbing) {
        while (true) {
            val playerDur = exoPlayer.duration
            val resolvedDur = if (playerDur != C.TIME_UNSET && playerDur > 0L) {
                playerDur
            } else {
                totalDurationMs.takeIf { it > 0L } ?: liveDurationMs.coerceAtLeast(1L)
            }
            val resolvedPos = exoPlayer.currentPosition.coerceIn(0L, resolvedDur)
            val resolvedBuf = exoPlayer.bufferedPosition.coerceIn(0L, resolvedDur)

            if (!isScrubbing) {
                livePositionMs = resolvedPos
            }
            liveDurationMs = resolvedDur
            liveBufferedMs = resolvedBuf
            onPositionChanged(resolvedPos, resolvedDur, resolvedBuf)
            delay(200L)
        }
    }

    val safeDurationMs = liveDurationMs.coerceAtLeast(1L)
    val playbackRatio = (livePositionMs.toFloat() / safeDurationMs.toFloat()).coerceIn(0f, 1f)
    val activeRatio = if (isScrubbing) scrubRatio.coerceIn(0f, 1f) else playbackRatio
    val bufferedRatio = (liveBufferedMs.toFloat() / safeDurationMs.toFloat()).coerceIn(0f, 1f)

    val displayedPositionMs = if (isScrubbing) {
        (activeRatio * safeDurationMs).toLong().coerceIn(0L, safeDurationMs)
    } else {
        livePositionMs.coerceIn(0L, safeDurationMs)
    }

    val remainingMs = (safeDurationMs - displayedPositionMs).coerceAtLeast(0L)
    val bufferedPercent = (bufferedRatio * 100f).roundToInt().coerceIn(0, 100)

    // Determine if current/scrub position is inside Opening (OP) or Ending (ED)
    val displayedSec = displayedPositionMs / 1000L
    val activeChapterLabel = when {
        introEndSec > introStartSec && displayedSec in introStartSec..introEndSec -> "🎵 Opening (OP)"
        outroEndSec > outroStartSec && displayedSec in outroStartSec..outroEndSec -> "🎬 Ending (ED)"
        else -> null
    }

    val trackHeight by animateDpAsState(
        targetValue = if (isScrubbing) 8.dp else 5.dp,
        animationSpec = tween(durationMillis = 150),
        label = "trackHeight"
    )
    val thumbRadius by animateDpAsState(
        targetValue = if (isScrubbing) 10.dp else 7.dp,
        animationSpec = tween(durationMillis = 150),
        label = "thumbRadius"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("custom_video_progress_container")
    ) {
        // Floating Scrub Preview Bubble (Visible while dragging/scrubbing the progress bar)
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val density = LocalDensity.current
            val maxW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val bubbleWidthPx = with(density) { 130.dp.toPx() }
            val thumbCenterX = (activeRatio * maxW).coerceIn(bubbleWidthPx / 2f, (maxW - bubbleWidthPx / 2f).coerceAtLeast(bubbleWidthPx / 2f))

            this@Column.AnimatedVisibility(
                visible = isScrubbing,
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(120))
            ) {
                val deltaSec = ((displayedPositionMs - livePositionMs) / 1000L)
                val deltaText = if (deltaSec >= 0) "+${deltaSec}s" else "${deltaSec}s"

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (thumbCenterX - bubbleWidthPx / 2f).roundToInt(),
                                y = 0
                            )
                        }
                        .background(SurfaceDark.copy(alpha = 0.95f), RoundedCornerShape(10.dp))
                        .border(1.dp, CrimsonNeon.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("video_scrub_preview_tooltip"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = formatPlaybackDuration(displayedPositionMs),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "($deltaText)",
                                color = CyanGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (activeChapterLabel != null) {
                            Text(
                                text = activeChapterLabel,
                                color = StarAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Interactive Custom Progress Bar Canvas (Touch & Drag Scrubbing)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .onSizeChanged { size ->
                    if (size.width > 0) {
                        barWidthPx = size.width.toFloat()
                    }
                }
                .pointerInput(safeDurationMs) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        val ratio = (tapOffset.x / width).coerceIn(0f, 1f)
                        val targetMs = (ratio * safeDurationMs).toLong().coerceIn(0L, safeDurationMs)
                        livePositionMs = targetMs
                        exoPlayer.seekTo(targetMs)
                        onSeekCommitted(targetMs)
                    }
                }
                .pointerInput(safeDurationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { startOffset ->
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            isScrubbing = true
                            scrubRatio = (startOffset.x / width).coerceIn(0f, 1f)
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            scrubRatio = (change.position.x / width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            val targetMs = (scrubRatio * safeDurationMs).toLong().coerceIn(0L, safeDurationMs)
                            livePositionMs = targetMs
                            isScrubbing = false
                            exoPlayer.seekTo(targetMs)
                            onSeekCommitted(targetMs)
                        },
                        onDragCancel = {
                            isScrubbing = false
                        }
                    )
                }
                .testTag("custom_video_progress_bar"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barH = trackHeight.toPx()
                val topY = (canvasHeight - barH) / 2f
                val cornerRadius = CornerRadius(barH / 2f, barH / 2f)

                // 1. Unplayed Background Track
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.22f),
                    topLeft = Offset(0f, topY),
                    size = Size(canvasWidth, barH),
                    cornerRadius = cornerRadius
                )

                // 2. Buffered Track (Media3 ExoPlayer bufferedPosition)
                val bufferedWidth = (canvasWidth * bufferedRatio).coerceIn(0f, canvasWidth)
                if (bufferedWidth > 0f) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.45f),
                        topLeft = Offset(0f, topY),
                        size = Size(bufferedWidth, barH),
                        cornerRadius = cornerRadius
                    )
                }

                // 3. Opening (OP) & Ending (ED) Chapter Segment Markers
                val totalSec = (safeDurationMs / 1000L).coerceAtLeast(1L).toFloat()
                if (introEndSec > introStartSec && introStartSec >= 0L) {
                    val startX = ((introStartSec.toFloat() / totalSec) * canvasWidth).coerceIn(0f, canvasWidth)
                    val endX = ((introEndSec.toFloat() / totalSec) * canvasWidth).coerceIn(startX, canvasWidth)
                    if (endX > startX) {
                        drawRoundRect(
                            color = StarAmber.copy(alpha = 0.65f),
                            topLeft = Offset(startX, topY),
                            size = Size(endX - startX, barH),
                            cornerRadius = cornerRadius
                        )
                    }
                }
                if (outroEndSec > outroStartSec && outroStartSec >= 0L) {
                    val startX = ((outroStartSec.toFloat() / totalSec) * canvasWidth).coerceIn(0f, canvasWidth)
                    val endX = ((outroEndSec.toFloat() / totalSec) * canvasWidth).coerceIn(startX, canvasWidth)
                    if (endX > startX) {
                        drawRoundRect(
                            color = CyanGlow.copy(alpha = 0.65f),
                            topLeft = Offset(startX, topY),
                            size = Size(endX - startX, barH),
                            cornerRadius = cornerRadius
                        )
                    }
                }

                // 4. Active Playback Progress Track (Neon Gradient)
                val activeWidth = (canvasWidth * activeRatio).coerceIn(0f, canvasWidth)
                if (activeWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(CrimsonNeon, VioletAccent, CyanGlow),
                            startX = 0f,
                            endX = canvasWidth.coerceAtLeast(1f)
                        ),
                        topLeft = Offset(0f, topY),
                        size = Size(activeWidth, barH),
                        cornerRadius = cornerRadius
                    )
                }

                // 5. Scrubber Thumb (Outer Halo + Solid Core)
                val thumbX = activeWidth.coerceIn(thumbRadius.toPx(), (canvasWidth - thumbRadius.toPx()).coerceAtLeast(thumbRadius.toPx()))
                val centerY = canvasHeight / 2f
                val rPx = thumbRadius.toPx()

                // Outer Neon Glow Halo
                drawCircle(
                    color = CrimsonNeon.copy(alpha = if (isScrubbing) 0.45f else 0.28f),
                    radius = rPx * 1.65f,
                    center = Offset(thumbX, centerY)
                )
                // Main Thumb Circle
                drawCircle(
                    color = CrimsonNeon,
                    radius = rPx,
                    center = Offset(thumbX, centerY)
                )
                // Inner White Core
                drawCircle(
                    color = Color.White,
                    radius = rPx * 0.45f,
                    center = Offset(thumbX, centerY)
                )
            }
        }

        // Time Labels & Playback Telemetry Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Current Playback Time / Total Duration
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatPlaybackDuration(displayedPositionMs),
                    color = if (isScrubbing) CyanGlow else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.testTag("video_current_time_text")
                )
                Text(
                    text = " / ",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatPlaybackDuration(safeDurationMs),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.testTag("video_total_duration_text")
                )

                if (activeChapterLabel != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StarAmber.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = activeChapterLabel,
                            color = StarAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Right: Buffered % Badge & Remaining Time / Quick Seek Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Buffer Status Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceDark.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = "Buffer $bufferedPercent%",
                        color = CyanGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Remaining Time Toggle Pill (Tap to toggle remaining vs percentage played)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceDark.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clickable { showRemainingTime = !showRemainingTime }
                        .testTag("video_remaining_time_toggle")
                ) {
                    val progressPct = (activeRatio * 100f).roundToInt().coerceIn(0, 100)
                    Text(
                        text = if (showRemainingTime) {
                            "-${formatPlaybackDuration(remainingMs)} left"
                        } else {
                            "-${formatPlaybackDuration(remainingMs)} ($progressPct%)"
                        },
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Formats milliseconds into `MM:SS` or `HH:MM:SS` if duration is 1 hour or longer.
 */
fun formatPlaybackDuration(ms: Long): String {
    if (ms <= 0L || ms == C.TIME_UNSET) return "00:00"
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
