package com.example.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Anime
import com.example.data.repository.AdminRepository
import com.example.data.sync.CloudSyncStatus
import com.example.ui.components.AnimeRow
import com.example.ui.components.CatalogNetworkNotificationBanner
import com.example.ui.components.ContinueWatchingRow
import com.example.ui.components.HeroCarousel
import com.example.ui.components.HomeShimmerScreen
import com.example.ui.components.NavigatorOnlineStatusPill
import com.example.ui.components.RobiulBrandHeader
import com.example.ui.components.SecretAdminDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.HomeViewModel

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
    onTierListClick: () -> Unit = {},
    onQuotesClick: () -> Unit = {},
    onWebPortalClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val syncState by viewModel.cloudSyncManager.syncState.collectAsStateWithLifecycle()
    val networkState by viewModel.catalogNetworkMonitor.state.collectAsStateWithLifecycle()
    val watchQueue by viewModel.gamificationRepository.watchQueue.collectAsStateWithLifecycle()
    val challenges by viewModel.gamificationRepository.challenges.collectAsStateWithLifecycle()
    val adMobConfig by AdminRepository.globalAdMobConfig.collectAsStateWithLifecycle()

    val unreadNotifsCount = notifications.count { !it.isRead }
    val context = LocalContext.current

    var showAdminAuthDialog by remember { mutableStateOf(false) }
    var showUniverseHubDialog by remember { mutableStateOf(false) }
    var universeHubInitialTab by remember { mutableIntStateOf(0) }
    var logoTapCount by remember { mutableIntStateOf(0) }
    var lastLogoTapTime by remember { mutableLongStateOf(0L) }

    val allCatalog = remember(uiState.trending, uiState.popular, uiState.seasonal, uiState.topRated) {
        (uiState.trending + uiState.popular + uiState.seasonal + uiState.topRated).distinctBy { it.id }
    }

    if (showAdminAuthDialog) {
        SecretAdminDialog(
            onDismiss = { showAdminAuthDialog = false },
            onSuccess = {
                showAdminAuthDialog = false
                onAdminClick()
            }
        )
    }

    if (showUniverseHubDialog) {
        AnimeInteractiveHubDialog(
            initialTabIndex = universeHubInitialTab,
            catalog = allCatalog,
            gamificationRepository = viewModel.gamificationRepository,
            userRepository = viewModel.userRepository,
            onDismiss = { showUniverseHubDialog = false },
            onAnimeClick = onAnimeClick,
            onPlayEpisode = onWatchEpisodeClick
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NavigatorOnlineStatusPill(
                                networkState = networkState,
                                onToggleOfflineTest = { viewModel.toggleSimulatedOfflineMode() },
                                modifier = Modifier.padding(end = 6.dp)
                            )

                            // Cloud Sync Status Badge (SYNCED / SYNCING / OFFLINE / SYNC ERROR)
                            val syncBadgeColor = when (syncState.status) {
                                CloudSyncStatus.SYNCED -> EmeraldSuccess
                                CloudSyncStatus.SYNCING -> CyanAccent
                                CloudSyncStatus.OFFLINE -> StarAmber
                                CloudSyncStatus.SYNC_ERROR -> CrimsonNeon
                            }
                            Surface(
                                color = syncBadgeColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clickable { viewModel.cloudSyncManager.performInitialSync() }
                                    .testTag("cloud_sync_status_badge")
                            ) {
                                Text(
                                    text = "☁️ ${syncState.status.displayLabel}",
                                    color = syncBadgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(onClick = onScheduleClick) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule", tint = TextPrimary)
                            }

                            IconButton(onClick = onDownloadsClick) {
                                Icon(Icons.Default.Download, contentDescription = "Downloads", tint = TextPrimary)
                            }

                            IconButton(
                                onClick = onSearchClick,
                                modifier = Modifier.testTag("header_search_btn")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
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

                // Offline Mode Indicator & Automatic Catalog Fetch Failure Notification with Retry Button
                item {
                    CatalogNetworkNotificationBanner(
                        networkState = networkState,
                        externalErrorMessage = uiState.error,
                        onRetryClick = { viewModel.retryCatalogConnection() },
                        onDismissError = { viewModel.dismissFetchError() }
                    )
                }

                // Continue Watching Horizontal Scroll Section at Top (Based on Local Watch History)
                if (continueWatching.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        ContinueWatchingRow(
                            items = continueWatching,
                            onResumeClick = { item -> onWatchEpisodeClick(item.animeId, item.episodeNumber) },
                            onRemoveClick = { item -> viewModel.removeContinueWatching(item) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                        QuickHubButton("Schedule", "📅", Icons.Default.CalendarMonth, onScheduleClick, Modifier.weight(1f))
                        QuickHubButton("Downloads", "📥", Icons.Default.Download, onDownloadsClick, Modifier.weight(1f))
                        QuickHubButton("Find Anime", "🎯", Icons.Default.AutoAwesome, onQuizClick, Modifier.weight(1f))
                        QuickHubButton("MAL Sync", "🔄", Icons.Default.CloudSync, onMalSyncClick, Modifier.weight(1f))
                        QuickHubButton("Web Live", "🌐", Icons.Default.Language, onWebPortalClick, Modifier.weight(1f))
                    }
                }

                // Secondary Community & Media Hub
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickHubButton("Watch Party", "🎉", Icons.Default.Group, onPartyClick, Modifier.weight(1f))
                        QuickHubButton("Tier Maker", "🏆", Icons.Default.FormatListNumbered, onTierListClick, Modifier.weight(1f))
                        QuickHubButton("Quotes", "💬", Icons.Default.FormatQuote, onQuotesClick, Modifier.weight(1f))
                    }
                }

                // 💎 Anime Universe Interactive Hub Launcher (Characters, Studios, Trailers, News, Polls, Battles, Roulette, Wallpapers, Trivia, Widgets)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💎 Anime Universe Hub",
                                color = StarAmber,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Open All 10 Modules →",
                                color = CyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        universeHubInitialTab = 0
                                        showUniverseHubDialog = true
                                    }
                                    .testTag("open_universe_hub_btn")
                            )
                        }
                        val hubShortcuts = listOf(
                            0 to "🎭 Characters",
                            1 to "🏢 Studios & Staff",
                            2 to "🎬 Trailers & Clips",
                            3 to "📰 Anime News",
                            4 to "🗳️ Polls & ⚔️ Battles",
                            5 to "🎡 Anime Roulette",
                            6 to "🖼️ 4K Wallpapers",
                            7 to "🎁 Events & 🏆 Ranks",
                            8 to "🎮 Trivia & Games",
                            9 to "📱 Widgets & Links"
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(hubShortcuts) { (tabIdx, label) ->
                                Surface(
                                    onClick = {
                                        universeHubInitialTab = tabIdx
                                        showUniverseHubDialog = true
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = SurfaceDark,
                                    modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                                ) {
                                    Text(
                                        text = label,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 💎 PERSONALIZED ANIME DASHBOARD (Welcome back, Robiul • 12 Day Streak • Smart Continue Watching • Next Episode Countdown • Today's Challenge • Watch Stats • Watch Queue)
                item {
                    PersonalizedAnimeDashboardCard(
                        continueWatchingItem = continueWatching.firstOrNull(),
                        watchQueue = watchQueue,
                        dailyChallenge = challenges.firstOrNull(),
                        favoritesCount = favorites.size.coerceAtLeast(9),
                        onResumeSmartClick = { animeId, epNum -> onWatchEpisodeClick(animeId, epNum) },
                        onOpenRoulette = {
                            universeHubInitialTab = 5
                            showUniverseHubDialog = true
                        },
                        onOpenTrivia = {
                            universeHubInitialTab = 8
                            showUniverseHubDialog = true
                        },
                        onPlayNextQueueItem = {
                            val nextItem = watchQueue.firstOrNull()
                            if (nextItem != null) {
                                onWatchEpisodeClick(nextItem.animeId, nextItem.nextEpisodeNumber)
                            }
                        }
                    )
                }

                // Embedded Website Portal Card
                item {
                    val websiteAddress = "http://127.0.0.1:8080"
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, CrimsonNeon.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF00E676), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🌐 ROBIUL WEB STREAMING PORTAL",
                                        color = Color(0xFF00E676),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Surface(
                                    onClick = onWebPortalClick,
                                    shape = RoundedCornerShape(6.dp),
                                    color = CrimsonNeon.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "Open In-App Portal →",
                                        color = CrimsonNeon,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { onWebPortalClick() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SelectionContainer(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$websiteAddress  (Tap to open inside app)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Robiul Website Link", websiteAddress))
                                        Toast.makeText(context, "Copied: $websiteAddress", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Website Address",
                                        tint = CrimsonNeon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Google AdMob Live Banner Placement
                if (adMobConfig.adsEnabled && adMobConfig.bannerAdsEnabled) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
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

                // =========================================================
                // 🤖 AI ANIME RECOMMENDATION SYSTEM (All 7 Personalized Rows)
                // =========================================================
                val recs = uiState.recommendations
                if (recs.aiPicksForYou.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        AnimeRow(
                            title = "🤖 AI Picks For You",
                            animeList = recs.aiPicksForYou,
                            isLoading = false,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }
                if (recs.becauseYouWatched.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        AnimeRow(
                            title = "💡 Because You Watched ${recs.sourceWatchedTitle}",
                            animeList = recs.becauseYouWatched,
                            isLoading = false,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }
                if (recs.yourNextAnime.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        AnimeRow(
                            title = "🎯 Your Next Anime",
                            animeList = recs.yourNextAnime,
                            isLoading = false,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }
                if (recs.recommendedForYou.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        AnimeRow(
                            title = "✨ Recommended For You",
                            animeList = recs.recommendedForYou,
                            isLoading = false,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }
                if (recs.similarAnime.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        AnimeRow(
                            title = "🔗 Similar Anime",
                            animeList = recs.similarAnime,
                            isLoading = false,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }
                if (recs.hiddenGems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        AnimeRow(
                            title = "💎 Hidden Gems",
                            animeList = recs.hiddenGems,
                            isLoading = false,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }
                if (recs.trendingForYou.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        AnimeRow(
                            title = "🚀 Trending For You",
                            animeList = recs.trendingForYou,
                            isLoading = false,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }

                // 🔥 Real-Time Trending Heatmap Section
                if (allCatalog.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        TrendingHeatmapSection(
                            catalog = allCatalog,
                            selectedPeriod = uiState.selectedHeatmapPeriod,
                            gamificationRepository = viewModel.gamificationRepository,
                            onPeriodSelect = { viewModel.setHeatmapPeriod(it) },
                            onAnimeClick = onAnimeClick
                        )
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
private fun PersonalizedAnimeDashboardCard(
    continueWatchingItem: com.example.data.model.WatchHistoryItem?,
    watchQueue: List<com.example.data.model.WatchQueueItem>,
    dailyChallenge: com.example.data.model.ChallengeQuest?,
    favoritesCount: Int,
    onResumeSmartClick: (String, Int) -> Unit,
    onOpenRoulette: () -> Unit,
    onOpenTrivia: () -> Unit,
    onPlayNextQueueItem: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, CrimsonNeon.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .testTag("personalized_anime_dashboard")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Greeting + Watch Streak + Title Rank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👋 Welcome back, Robiul",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "🎖️ Title: Otaku (Lv.25 • 2,850 XP) • Next: Elite Otaku",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = StarAmber.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🔥 12 Day Streak",
                        color = StarAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Smart Continue Watching + Release Countdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = {
                        val targetId = continueWatchingItem?.animeId ?: "anime_1"
                        val targetEp = continueWatchingItem?.episodeNumber ?: 7
                        onResumeSmartClick(targetId, targetEp)
                    },
                    color = SurfaceVariantDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("▶️ Smart Continue Watching", color = CrimsonNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val epNum = continueWatchingItem?.episodeNumber ?: 7
                        val remSec = continueWatchingItem?.let {
                            ((it.totalDurationMs - it.watchedPositionMs).coerceAtLeast(272_000L)) / 1000L
                        } ?: 272L
                        val remStr = String.format("%d:%02d", remSec / 60, remSec % 60)
                        Text(
                            text = "Continue Episode $epNum — $remStr remaining",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = SurfaceVariantDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("📅 Your Next Episodes", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "New Episode in 02 : 14 : 36",
                            color = StarAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Today's Challenge + Your Watch Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = SurfaceVariantDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🏆 Today’s Challenge", color = StarAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = dailyChallenge?.title ?: "Watch 1 Episode (+50 XP)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Surface(
                    color = SurfaceVariantDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("📊 Your Watch Stats", color = EmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "142 Eps • 56.8h • ❤️ $favoritesCount Favs",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Smart Watch Queue (1. One Piece, 2. Solo Leveling, 3. Demon Slayer, 4. JJK)
            if (watchQueue.isNotEmpty()) {
                Surface(
                    color = SurfaceVariantDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🎯 Your Smart Watch Queue (Auto-Advances)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = watchQueue.take(4).joinToString("  •  ") { "${it.orderIndex}. ${it.animeTitle}" },
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onPlayNextQueueItem,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Play #1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Interactive Buttons: Anime Roulette & Daily Anime Trivia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onOpenRoulette,
                    color = CrimsonNeon.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🎡 Spin Tonight's Roulette",
                        color = CrimsonNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                    )
                }
                Surface(
                    onClick = onOpenTrivia,
                    color = CyanAccent.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🧩 Daily Anime Trivia (+XP)",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingHeatmapSection(
    catalog: List<Anime>,
    selectedPeriod: String,
    gamificationRepository: com.example.data.repository.GamificationAndSocialRepository,
    onPeriodSelect: (String) -> Unit,
    onAnimeClick: (Anime) -> Unit
) {
    val heatItems = remember(catalog, selectedPeriod) {
        gamificationRepository.getTrendingHeatmap(catalog, selectedPeriod).take(5)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥 Real-Time Trending Heatmap",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("LIVE", "24H", "7D", "30D").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelect(period) },
                        label = { Text(period, fontSize = 10.sp) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        heatItems.forEach { item ->
            Surface(
                onClick = { onAnimeClick(item.anime) },
                color = SurfaceDark,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "#${item.rank} ${item.anime.titleEnglish}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🔥 ${item.heatScorePercent}° Heat • ${item.activeViewers} watching",
                            color = StarAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.heatScorePercent / 100f },
                        color = CrimsonNeon,
                        trackColor = SurfaceVariantDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
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
        "Cloudflare R2 + CDN (✅ HLS)",
        "AWS S3 + CloudFront (✅ HLS/DASH)",
        "Bunny.net Storage + CDN (✅ HLS)",
        "Cloudflare Stream (✅ HLS)",
        "Mux Video Platform (✅ HLS)",
        "Self-hosted VPS + Nginx (✅ HLS)"
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
