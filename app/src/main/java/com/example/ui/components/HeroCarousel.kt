package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.Anime
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@kotlin.OptIn(ExperimentalLayoutApi::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HeroCarousel(
    anime: Anime,
    onWatchClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onWatchlistToggle: () -> Unit,
    isInWatchlist: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Keep preview off by default on startup to avoid background Codec2 hardware decoder contention
    var isVideoPreviewEnabled by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(true) }
    var currentServerIdx by remember { mutableIntStateOf(0) }

    val liveServerStreams = remember(anime.id, anime.titleEnglish) {
        val titleLower = "${anime.titleEnglish} ${anime.titleRomaji}".lowercase()
        val primaryUrl = when {
            anime.id == "anime_1" || titleLower.contains("frieren") -> "https://v.animethemes.moe/SousouNoFrieren-OP1.webm"
            anime.id == "anime_2" || titleLower.contains("jujutsu") -> "https://v.animethemes.moe/JujutsuKaisenS2-OP1.webm"
            anime.id == "anime_3" || titleLower.contains("solo leveling") -> "https://v.animethemes.moe/SoloLeveling-OP1.webm"
            anime.id == "anime_4" || titleLower.contains("demon slayer") || titleLower.contains("kimetsu") -> "https://v.animethemes.moe/KimetsuNoYaiba-OP1.webm"
            anime.id == "anime_5" || titleLower.contains("chainsaw") -> "https://v.animethemes.moe/ChainsawMan-OP1.webm"
            anime.id == "anime_6" || titleLower.contains("attack on titan") || titleLower.contains("shingeki") -> "https://v.animethemes.moe/ShingekiNoKyojin-OP1.webm"
            anime.id == "anime_7" || titleLower.contains("cyberpunk") -> "https://v.animethemes.moe/CyberpunkEdgerunners-OP1.webm"
            anime.id == "anime_8" || titleLower.contains("spy") -> "https://v.animethemes.moe/SpyXFamily-OP1.webm"
            else -> "https://v.animethemes.moe/SousouNoFrieren-OP1.webm"
        }
        listOf(
            primaryUrl to "HD-1 • VidStreaming (SUB)",
            primaryUrl to "HD-2 • MegaCloud (SUB)",
            primaryUrl to "Bunny.net CDN • robiulislam.b-cdn.net",
            primaryUrl to "Cloudflare R2 + CDN • 1080p"
        )
    }

    val activeStreamPair = liveServerStreams[currentServerIdx % liveServerStreams.size]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .clickable(
                onClick = {
                    isVideoPreviewEnabled = false
                    onDetailsClick()
                }
            )
            .testTag("hero_banner")
    ) {
        // High-res Backdrop Hero Image
        AsyncImage(
            model = anime.bannerUrl.ifBlank { anime.posterUrl },
            contentDescription = anime.titleEnglish,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Lazily instantiate ExoPlayer ONLY when the user explicitly enables Live Video Preview
        if (isVideoPreviewEnabled) {
            val heroPlayer = remember(context) {
                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36")
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(8000)
                    .setReadTimeoutMs(12000)

                val renderersFactory = DefaultRenderersFactory(context)
                    .setEnableDecoderFallback(true)

                val trackSelector = DefaultTrackSelector(context).apply {
                    setParameters(
                        buildUponParameters()
                            .setMaxVideoSize(854, 480)
                            .setMaxVideoBitrate(1_200_000)
                    )
                }

                ExoPlayer.Builder(context, renderersFactory)
                    .setTrackSelector(trackSelector)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory))
                    .build()
                    .apply {
                        volume = if (isMuted) 0f else 1f
                        repeatMode = Player.REPEAT_MODE_ONE
                        playWhenReady = true
                    }
            }

            DisposableEffect(heroPlayer) {
                val listener = object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        if (currentServerIdx < liveServerStreams.size - 1) {
                            currentServerIdx++
                        } else {
                            isVideoPreviewEnabled = false
                        }
                    }
                }
                heroPlayer.addListener(listener)
                onDispose {
                    heroPlayer.removeListener(listener)
                    heroPlayer.stop()
                    heroPlayer.release()
                }
            }

            LaunchedEffect(activeStreamPair.first) {
                heroPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(activeStreamPair.first)))
                heroPlayer.prepare()
                heroPlayer.play()
            }

            LaunchedEffect(isMuted) {
                heroPlayer.volume = if (isMuted) 0f else 1f
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = heroPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Triple Gradient Overlay: Top subtle, bottom full blend to BackgroundDark
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            BackgroundDark.copy(alpha = 0.65f),
                            BackgroundDark
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Top Live Video Server Controls Row on Home Hero Banner
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark.copy(alpha = 0.85f),
                modifier = Modifier
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable {
                        if (!isVideoPreviewEnabled) {
                            isVideoPreviewEnabled = true
                        } else {
                            currentServerIdx = (currentServerIdx + 1) % liveServerStreams.size
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isVideoPreviewEnabled) Color(0xFF00E676) else CrimsonNeon,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isVideoPreviewEnabled) {
                            "LIVE • ${activeStreamPair.second}"
                        } else {
                            "TAP FOR LIVE PREVIEW • ${activeStreamPair.second}"
                        },
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isVideoPreviewEnabled) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(34.dp)
                            .background(SurfaceDark.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute Live Video" else "Mute Live Video",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { isVideoPreviewEnabled = !isVideoPreviewEnabled },
                    modifier = Modifier
                        .size(34.dp)
                        .background(SurfaceDark.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isVideoPreviewEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isVideoPreviewEnabled) "Pause Video Preview" else "Play Video Preview",
                        tint = CrimsonNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Japanese Kanji subtitle
            if (anime.titleJapanese.isNotBlank()) {
                Text(
                    text = anime.titleJapanese,
                    color = CrimsonNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Main Title
            Text(
                text = anime.titleEnglish,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Metadata Row: Rating, Score %, Type, Year, Ep Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format("%.1f", anime.rating),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "•",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Text(
                    text = "${anime.score}% Match",
                    color = Color(0xFF00E676),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "•",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Text(
                    text = "${anime.episodesCount} Episodes",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                QualityBadge("1080p HLS")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Genre chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                anime.genres.take(3).forEach { genre ->
                    Box(
                        modifier = Modifier
                            .background(SurfaceVariantDark.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = genre,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        isVideoPreviewEnabled = false
                        onWatchClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonNeon,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("hero_watch_now_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Watch Ep 1",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onWatchlistToggle,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("hero_watchlist_btn")
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isInWatchlist) "In Watchlist" else "Watchlist",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
