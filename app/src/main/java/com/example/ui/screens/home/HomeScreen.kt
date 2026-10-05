package com.example.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Anime
import com.example.data.repository.AdminRepository
import com.example.ui.components.AnimeRow
import com.example.ui.components.ContinueWatchingRow
import com.example.ui.components.HeroCarousel
import com.example.ui.components.HomeShimmerScreen
import com.example.ui.components.RobiulBrandHeader
import com.example.ui.components.SecretAdminDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.HomeViewModel
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import coil.compose.AsyncImage

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAnimeClick: (Anime) -> Unit,
    onWatchEpisodeClick: (String, Int) -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onGenreClick: (String) -> Unit,
    onScheduleClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onQuizClick: () -> Unit = {},
    onMalSyncClick: () -> Unit = {},
    onPartyClick: () -> Unit = {},
    onRadioClick: () -> Unit = {},
    onTierListClick: () -> Unit = {},
    onQuotesClick: () -> Unit = {},
    onWebPortalClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val adMobConfig by AdminRepository.globalAdMobConfig.collectAsStateWithLifecycle()
    val unreadNotifsCount = notifications.count { !it.isRead }
    val context = LocalContext.current

    var showAdminAuthDialog by remember { mutableStateOf(false) }
    var logoTapCount by remember { mutableIntStateOf(0) }
    var lastLogoTapTime by remember { mutableLongStateOf(0L) }

    if (showAdminAuthDialog) {
        SecretAdminDialog(
            onDismiss = { showAdminAuthDialog = false },
            onSuccess = {
                showAdminAuthDialog = false
                onAdminClick()
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (uiState.isLoading && uiState.featuredAnime == null) {
            HomeShimmerScreen(
                onLogoTap = {
                    val now = System.currentTimeMillis()
                    if (now - lastLogoTapTime < 1500) {
                        logoTapCount++
                        if (logoTapCount >= 5) {
                            logoTapCount = 0
                            showAdminAuthDialog = true
                        }
                    } else {
                        logoTapCount = 1
                    }
                    lastLogoTapTime = now
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Top Header Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Logo & Title (Exclusive Hidden Admin Trigger: tap 5 times on ROBIUL [RS] logo)
                        RobiulBrandHeader(
                            modifier = Modifier.testTag("app_brand_logo"),
                            onClick = {
                                val now = System.currentTimeMillis()
                                if (now - lastLogoTapTime < 1500) {
                                    logoTapCount++
                                    if (logoTapCount >= 5) {
                                        logoTapCount = 0
                                        showAdminAuthDialog = true
                                    }
                                } else {
                                    logoTapCount = 1
                                }
                                lastLogoTapTime = now
                            }
                        )

                        // Actions: Schedule, Downloads, Search, Notifications
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onScheduleClick) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Schedule",
                                    tint = TextPrimary
                                )
                            }

                            IconButton(onClick = onDownloadsClick) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Downloads",
                                    tint = TextPrimary
                                )
                            }

                            IconButton(
                                onClick = onSearchClick,
                                modifier = Modifier.testTag("header_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = TextPrimary
                                )
                            }

                            IconButton(
                                onClick = onNotificationsClick,
                                modifier = Modifier.testTag("header_notif_btn")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifsCount > 0) {
                                            Badge(
                                                containerColor = CrimsonNeon,
                                                contentColor = Color.White
                                            ) {
                                                Text(text = "$unreadNotifsCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Navigation Hub
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickHubButton(
                            title = "Schedule",
                            emoji = "📅",
                            icon = Icons.Default.CalendarMonth,
                            onClick = onScheduleClick,
                            modifier = Modifier.weight(1f)
                        )
                        QuickHubButton(
                            title = "Downloads",
                            emoji = "📥",
                            icon = Icons.Default.Download,
                            onClick = onDownloadsClick,
                            modifier = Modifier.weight(1f)
                        )
                        QuickHubButton(
                            title = "Find Anime",
                            emoji = "🎯",
                            icon = Icons.Default.AutoAwesome,
                            onClick = onQuizClick,
                            modifier = Modifier.weight(1f)
                        )
                        QuickHubButton(
                            title = "MAL Sync",
                            emoji = "🔄",
                            icon = Icons.Default.CloudSync,
                            onClick = onMalSyncClick,
                            modifier = Modifier.weight(1f)
                        )
                        QuickHubButton(
                            title = "Web Live",
                            emoji = "🌐",
                            icon = Icons.Default.Language,
                            onClick = onWebPortalClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Direct Live Website Link Card on Home Screen (Copy Link & Open Live Website)
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .border(1.dp, Color(0xFF00FF66).copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .clickable { onWebPortalClick() }
                            .testTag("home_website_link_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(Color(0xFF00FF66), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🌐 ROBIUL [RS] LIVE WEBSITE LINK",
                                        color = Color(0xFF00FF66),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                SelectionContainer {
                                    Text(
                                        text = "https://ais-pre-nsac4yo6gxe4t4rioushj5-531708784674.asia-southeast1.run.app",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF00FF66),
                                modifier = Modifier
                                    .clickable {
                                        val link = "https://ais-pre-nsac4yo6gxe4t4rioushj5-531708784674.asia-southeast1.run.app"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Robiul Website Link", link))
                                        Toast.makeText(context, "Website Link Copied: $link", Toast.LENGTH_LONG).show()
                                    }
                                    .testTag("home_copy_website_link_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Website Link",
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copy Link",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Google AdMob Banner Slot (Controlled from Admin Panel -> Google AdMob Tab)
                if (adMobConfig.adsEnabled && adMobConfig.bannerAdsEnabled) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .border(1.dp, CrimsonNeon.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFFB300), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "Ad • Google AdMob",
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (adMobConfig.testModeEnabled) "Test Mode Banner" else "Live AdMob Banner",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Unit: ${adMobConfig.bannerAdUnitId} • ${adMobConfig.publisherId}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Featured Hero Banner
                uiState.featuredAnime?.let { featured ->
                    item {
                        HeroCarousel(
                            anime = featured,
                            onWatchClick = { onWatchEpisodeClick(featured.id, 1) },
                            onDetailsClick = { onAnimeClick(featured) },
                            onWatchlistToggle = { viewModel.toggleWatchlist(featured) }
                        )
                    }
                }

                // Live Server Video Streams (Direct 1080p Play from Home Screen)
                if (uiState.trending.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        LiveServerVideosRow(
                            animeList = uiState.trending.take(8),
                            onPlayVideoClick = { anime: Anime -> onWatchEpisodeClick(anime.id, 1) }
                        )
                    }
                }

                // Continue Watching
                if (continueWatching.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        ContinueWatchingRow(
                            items = continueWatching,
                            onResumeClick = { item -> onWatchEpisodeClick(item.animeId, item.episodeNumber) },
                            onRemoveClick = { item -> viewModel.removeContinueWatching(item) }
                        )
                    }
                }

                // Genre Explorer Chips
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Explore Genres",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.genres.filter { it != "All" }) { genre ->
                                Box(
                                    modifier = Modifier
                                        .background(SurfaceVariantDark, RoundedCornerShape(20.dp))
                                        .clickable { onGenreClick(genre) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("genre_chip_$genre")
                                ) {
                                    Text(
                                        text = genre,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Trending Now
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "🔥 Trending Now",
                        animeList = uiState.trending,
                        isLoading = uiState.isLoading,
                        onAnimeClick = onAnimeClick
                    )
                }

                // Seasonal Anime
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "❄️ Seasonal Anime - Winter 2026",
                        animeList = uiState.seasonal,
                        isLoading = uiState.isLoading,
                        onAnimeClick = onAnimeClick
                    )
                }

                // Top Rated Anime
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "⭐ Top Rated Anime",
                        animeList = uiState.topRated,
                        isLoading = uiState.isLoading,
                        onAnimeClick = onAnimeClick
                    )
                }

                // Popular Anime
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "👑 Most Popular",
                        animeList = uiState.popular,
                        isLoading = uiState.isLoading,
                        onAnimeClick = onAnimeClick
                    )
                }

                // Recently Added
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "✨ Recently Updated",
                        animeList = uiState.recentlyAdded,
                        isLoading = uiState.isLoading,
                        onAnimeClick = onAnimeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickHubButton(
    title: String,
    emoji: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        modifier = modifier
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LiveServerVideosRow(
    animeList: List<Anime>,
    onPlayVideoClick: (Anime) -> Unit
) {
    val serverLabels = listOf(
        "HD-1 • VidStreaming (1080p HLS)",
        "HD-2 • MegaCloud (1080p MP4)",
        "VidCloud • Multi-Bitrate HLS",
        "StreamTape • Direct 1080p"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(Color(0xFF00E676), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎬 Live Video Streams (Real Server)",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "1080p HLS / MP4",
                color = Color(0xFF00E676),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animeList.size) { index ->
                val anime = animeList[index]
                val serverTag = serverLabels[index % serverLabels.size]
                Surface(
                    onClick = { onPlayVideoClick(anime) },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDark,
                    modifier = Modifier
                        .width(250.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .testTag("home_live_video_card_${anime.id}")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(136.dp)
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = anime.bannerUrl.ifBlank { anime.posterUrl },
                                contentDescription = anime.titleEnglish,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.45f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )

                            // Top Server Badge
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = serverTag,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Center Play Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .align(Alignment.Center)
                                    .background(CrimsonNeon.copy(alpha = 0.92f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Episode",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Bottom Episode & Duration
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "EPISODE 1 • SUB/DUB",
                                    color = CrimsonNeon,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${anime.durationMinutes}m • 1080p",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = anime.titleEnglish,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap to stream immediately • ${anime.studio}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}


