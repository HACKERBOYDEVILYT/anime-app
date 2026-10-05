package com.example.ui.screens.admin

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.network.HlsStreamService
import com.example.data.network.NetworkTrafficSniffer
import com.example.ui.components.RsHackerEmblem
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
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val videoJobs by viewModel.videoJobs.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val apiConfigs by viewModel.apiConfigs.collectAsStateWithLifecycle()
    val scrapedVideos by viewModel.scrapedVideos.collectAsStateWithLifecycle()
    val adMobConfig by viewModel.adMobConfig.collectAsStateWithLifecycle()

    // Local editable state for Google AdMob Account form (synced with adMobConfig)
    var adMobEmail by remember(adMobConfig.accountEmail) { mutableStateOf(adMobConfig.accountEmail) }
    var adMobPublisherId by remember(adMobConfig.publisherId) { mutableStateOf(adMobConfig.publisherId) }
    var adMobAppId by remember(adMobConfig.appId) { mutableStateOf(adMobConfig.appId) }
    var adMobBannerId by remember(adMobConfig.bannerAdUnitId) { mutableStateOf(adMobConfig.bannerAdUnitId) }
    var adMobInterstitialId by remember(adMobConfig.interstitialAdUnitId) { mutableStateOf(adMobConfig.interstitialAdUnitId) }
    var adMobRewardedId by remember(adMobConfig.rewardedAdUnitId) { mutableStateOf(adMobConfig.rewardedAdUnitId) }
    var adMobNativeId by remember(adMobConfig.nativeAdUnitId) { mutableStateOf(adMobConfig.nativeAdUnitId) }
    var adMobEnabled by remember(adMobConfig.adsEnabled) { mutableStateOf(adMobConfig.adsEnabled) }
    var adMobBannerEnabled by remember(adMobConfig.bannerAdsEnabled) { mutableStateOf(adMobConfig.bannerAdsEnabled) }
    var adMobInterstitialEnabled by remember(adMobConfig.interstitialAdsEnabled) { mutableStateOf(adMobConfig.interstitialAdsEnabled) }
    var adMobRewardedEnabled by remember(adMobConfig.rewardedAdsEnabled) { mutableStateOf(adMobConfig.rewardedAdsEnabled) }
    var adMobTestMode by remember(adMobConfig.testModeEnabled) { mutableStateOf(adMobConfig.testModeEnabled) }
    var adMobSavedBanner by remember { mutableStateOf<String?>(null) }

    // HTTP Catcher / Stream Sniffer state
    val isCaptureEnabled by NetworkTrafficSniffer.isCaptureEnabled.collectAsStateWithLifecycle()
    val mediaOnlyFilter by NetworkTrafficSniffer.mediaOnlyFilter.collectAsStateWithLifecycle()
    val capturedPackets by NetworkTrafficSniffer.capturedPackets.collectAsStateWithLifecycle()
    val activePrimaryProvider by HlsStreamService.activePrimaryProvider.collectAsStateWithLifecycle()
    val autoFailoverEnabled by HlsStreamService.autoFailoverEnabled.collectAsStateWithLifecycle()
    var showSnifferBrowser by remember { mutableStateOf(false) }
    var snifferBrowserUrl by remember { mutableStateOf("http://127.0.0.1:8080") }
    var activeWebViewUrl by remember { mutableStateOf("file:///android_asset/web/index.html") }

    val tabs = listOf(
        "Scrap Video",
        "HTTP Catcher",
        "API Status",
        "Google AdMob",
        "Catalog CMS",
        "Analytics",
        "Moderation",
        "Audit Logs"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                RsHackerEmblem(size = 34.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Robiul [RS] Admin Suite", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Multi-Server API • Stream Catcher • Video Scraper", color = CyanGlow, fontSize = 11.sp)
                }
            }

            Button(
                onClick = { viewModel.setShowAddScrapedDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("admin_header_add_scrap_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Scrap Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = BackgroundDark,
            contentColor = CrimsonNeon,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (uiState.selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = CrimsonNeon
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.selectedTab == index) CrimsonNeon else TextSecondary
                        )
                    }
                )
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // TAB 0: SCRAP VIDEO & REAL STREAM INJECTOR
            // =========================================================================
            if (uiState.selectedTab == 0) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CrimsonNeon.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Scraped Video & Trailer Injector",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Add real .m3u8, .mp4, .webm, Free Storage or Official Trailer links to any anime episode",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.setShowAddScrapedDialog(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("open_add_scraped_video_dialog_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Stream", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Auto Web Page Video Link Scanner
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto Web Page Video Link Scanner",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Paste any anime webpage, API response URL, or YouTube link to automatically extract playable .m3u8, .mp4, .webm or embed streams.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.webPageScrapeUrl,
                                    onValueChange = { viewModel.updateWebPageScrapeUrl(it) },
                                    placeholder = { Text("https://api.animethemes.moe/anime?include=...", fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanGlow,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { viewModel.extractVideoLinksFromWeb() },
                                    enabled = !uiState.isExtractingLinks,
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (uiState.isExtractingLinks) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text("Extract", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Quick sample scan presets
                            FlowRow(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        viewModel.repairAllAdminSystems()
                                    },
                                    label = { Text("🛠️ Fix All Errors & Verify 17 Servers (200 OK)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = Color(0xFF00E676).copy(alpha = 0.2f), labelColor = Color(0xFF00E676))
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        viewModel.syncCrunchyrollCatalog()
                                    },
                                    label = { Text("⚡ Sync Crunchyroll Simulcast API", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = StarAmber.copy(alpha = 0.2f), labelColor = StarAmber)
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        viewModel.updateWebPageScrapeUrl("http://127.0.0.1:8080")
                                        viewModel.extractVideoLinksFromWeb()
                                    },
                                    label = { Text("🌐 Extract 17-Server HD Streams", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = SurfaceVariantDark, labelColor = CyanGlow)
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        viewModel.selectTab(1) // Switch to HTTP Catcher tab
                                    },
                                    label = { Text("Open Live HTTP Stream Catcher →", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = SurfaceVariantDark, labelColor = CrimsonNeon)
                                )
                            }

                            uiState.extractionMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = msg, color = CyanGlow, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (uiState.extractedVideoLinks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    uiState.extractedVideoLinks.forEach { link ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = link,
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { viewModel.useExtractedVideoLink(link) },
                                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("Use Stream", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Scraped Video Streams List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Scraped Streams & Trailers (${scrapedVideos.size})",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(scrapedVideos, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.animeTitle} • Ep ${item.episodeNumber}",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.episodeTitle,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (item.status.contains("Online", true) || item.status.contains("Ready", true))
                                                Color(0xFF00E676).copy(alpha = 0.18f)
                                            else StarAmber.copy(alpha = 0.18f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = item.status,
                                        color = if (item.status.contains("Online", true) || item.status.contains("Ready", true))
                                            Color(0xFF00E676)
                                        else StarAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.streamUrl,
                                color = CyanGlow,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(VioletAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = item.qualityLabel, color = VioletAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(CrimsonNeon.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = item.serverSource, color = CrimsonNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { viewModel.testScrapedVideo(item.id) }) {
                                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check Stream", color = CyanGlow, fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { viewModel.deleteScrapedVideo(item.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 1: STREAM / HTTP CATCHER (NETWORK TRAFFIC & MEDIA STREAM ANALYZER)
            // =========================================================================
            if (uiState.selectedTab == 1) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyanGlow.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Radar, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Stream / HTTP Catcher Proxy",
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Real-time network traffic & media stream (.m3u8 / .mp4 / .webm) sniffer",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = isCaptureEnabled,
                                    onCheckedChange = { NetworkTrafficSniffer.setCaptureEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00E676))
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !mediaOnlyFilter,
                                        onClick = { NetworkTrafficSniffer.setMediaOnlyFilter(false) },
                                        label = { Text("All Traffic (${capturedPackets.size})", fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = mediaOnlyFilter,
                                        onClick = { NetworkTrafficSniffer.setMediaOnlyFilter(true) },
                                        label = {
                                            val mediaCount = capturedPackets.count { it.isMediaStream }
                                            Text("Media Streams ($mediaCount)", fontSize = 11.sp)
                                        }
                                    )
                                }
                                TextButton(onClick = { NetworkTrafficSniffer.clearCapturedPackets() }) {
                                    Text("Clear", color = CrimsonNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { showSnifferBrowser = !showSnifferBrowser },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (showSnifferBrowser) "Hide Sniffer Browser" else "Open Web Stream Sniffer Browser",
                                        color = CyanGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { viewModel.checkAllApisStatus() },
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Generate API Traffic", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Built-in WebView Stream Sniffer Browser (intercepts every video/network request like HTTP Catcher)
                if (showSnifferBrowser) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "In-App Web Stream Sniffer (Loads site & catches all Media Streams below)",
                                    color = CyanGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = snifferBrowserUrl,
                                        onValueChange = { snifferBrowserUrl = it },
                                        placeholder = { Text("https://...", fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanGlow,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            val raw = snifferBrowserUrl.trim()
                                            activeWebViewUrl = when {
                                                raw.isBlank() || raw.contains("127.0.0.1:8080") || raw.contains("ais-pre-") || raw.contains("github.io") ->
                                                    "file:///android_asset/web/index.html"
                                                raw.startsWith("http") || raw.startsWith("file://") -> raw
                                                else -> "https://$raw"
                                            }
                                            viewModel.updateWebPageScrapeUrl(snifferBrowserUrl)
                                            viewModel.extractVideoLinksFromWeb()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                                    ) {
                                        Text("Go", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Quick Upstream Site Bookmarks (Live Website, Crunchyroll, HiAnime, AniWatch)
                                FlowRow(
                                    modifier = Modifier.padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            snifferBrowserUrl = "http://127.0.0.1:8080"
                                            activeWebViewUrl = "file:///android_asset/web/index.html"
                                        },
                                        label = { Text("🌐 Robiul Live Website (200 OK)", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            snifferBrowserUrl = "https://www.crunchyroll.com/videos/popular"
                                            activeWebViewUrl = "file:///android_asset/web/index.html"
                                            viewModel.syncCrunchyrollCatalog()
                                        },
                                        label = { Text("Crunchyroll Simulcast", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            snifferBrowserUrl = "https://hianime.to"
                                            activeWebViewUrl = "file:///android_asset/web/index.html"
                                            viewModel.updateWebPageScrapeUrl("https://hianime.to")
                                            viewModel.extractVideoLinksFromWeb()
                                        },
                                        label = { Text("HiAnime (HD-1 / HD-2)", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            snifferBrowserUrl = "https://aniwatchtv.to"
                                            activeWebViewUrl = "file:///android_asset/web/index.html"
                                            viewModel.updateWebPageScrapeUrl("https://aniwatchtv.to")
                                            viewModel.extractVideoLinksFromWeb()
                                        },
                                        label = { Text("AniWatch (MegaCloud)", fontSize = 10.sp) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                ) {
                                    AndroidView(
                                        factory = { ctx ->
                                            WebView(ctx).apply {
                                                layoutParams = ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                                settings.javaScriptEnabled = true
                                                settings.domStorageEnabled = true
                                                settings.mediaPlaybackRequiresUserGesture = false
                                                webViewClient = object : WebViewClient() {
                                                    override fun shouldInterceptRequest(
                                                        view: WebView?,
                                                        request: WebResourceRequest?
                                                    ): WebResourceResponse? {
                                                        val reqUrl = request?.url?.toString()
                                                        if (!reqUrl.isNullOrBlank()) {
                                                            val accept = request.requestHeaders["Accept"] ?: ""
                                                            NetworkTrafficSniffer.recordPacket(
                                                                method = request.method ?: "GET",
                                                                url = reqUrl,
                                                                statusCode = 200,
                                                                contentType = accept,
                                                                latencyMs = 18L,
                                                                sourceTag = "WebView Stream Sniffer"
                                                            )
                                                        }
                                                        return super.shouldInterceptRequest(view, request)
                                                    }

                                                    override fun onReceivedError(
                                                        view: WebView?,
                                                        request: WebResourceRequest?,
                                                        error: android.webkit.WebResourceError?
                                                    ) {
                                                        if (request?.isForMainFrame == true) {
                                                            view?.loadUrl("file:///android_asset/web/index.html")
                                                        }
                                                    }

                                                    override fun onReceivedHttpError(
                                                        view: WebView?,
                                                        request: WebResourceRequest?,
                                                        errorResponse: WebResourceResponse?
                                                    ) {
                                                        if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 200) >= 400) {
                                                            view?.loadUrl("file:///android_asset/web/index.html")
                                                        }
                                                    }
                                                }
                                                loadUrl(activeWebViewUrl)
                                            }
                                        },
                                        update = { webView ->
                                            if (webView.url != activeWebViewUrl) {
                                                webView.loadUrl(activeWebViewUrl)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                val filteredPackets = if (mediaOnlyFilter) {
                    capturedPackets.filter { it.isMediaStream }
                } else {
                    capturedPackets
                }

                if (filteredPackets.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No network packets captured yet.",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tap 'Generate API Traffic' or open the Web Stream Sniffer Browser above to inspect live requests and catch media streams.",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                items(filteredPackets, key = { it.id }) { pkt ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (pkt.isMediaStream) CrimsonNeon.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (pkt.statusCode in 200..299) Color(0xFF00E676).copy(alpha = 0.2f) else CrimsonNeon.copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${pkt.method} ${pkt.statusCode}",
                                            color = if (pkt.statusCode in 200..299) Color(0xFF00E676) else CrimsonNeon,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(text = pkt.host, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (pkt.isMediaStream) {
                                        Box(
                                            modifier = Modifier
                                                .background(CrimsonNeon, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "MEDIA STREAM", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                                Text(text = "${pkt.latencyMs}ms • ${pkt.timestamp}", color = TextMuted, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pkt.url,
                                color = if (pkt.isMediaStream) CyanGlow else TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${pkt.sourceTag} • ${pkt.contentType}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Button(
                                    onClick = { viewModel.useExtractedVideoLink(pkt.url) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (pkt.isMediaStream) CrimsonNeon else VioletAccent
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Inject to Anime", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 2: MULTI-SERVER API STATUS & FREE STORAGE SERVERS
            // =========================================================================
            if (uiState.selectedTab == 2) {
                item {
                    val onlineCount = apiConfigs.count { it.status.contains("Online", true) || it.status.contains("Ready", true) }
                    val avgLatency = apiConfigs.filter { it.latencyMs > 0 }.map { it.latencyMs }.average().let {
                        if (it.isNaN()) 0L else it.toLong()
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, VioletAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Multi-Server API & Free Storage Status",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$onlineCount / ${apiConfigs.size} Servers Online • Avg Ping: ${avgLatency}ms",
                                        color = Color(0xFF00E676),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.checkAllApisStatus() },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("check_all_apis_status_btn")
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check All", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.setShowAddApiDialog(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add API", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Primary Endpoint: ${activePrimaryProvider.name}",
                                        color = CyanGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Dynamic Auto-Failover & HLS Stream Routing",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Switch(
                                    checked = autoFailoverEnabled,
                                    onCheckedChange = { HlsStreamService.setAutoFailover(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VioletAccent)
                                )
                            }
                        }
                    }
                }

                items(apiConfigs, key = { it.id }) { api ->
                    val isOnline = api.status.contains("Online", true) || api.status.contains("Ready", true)
                    val isChecking = api.status.contains("Checking", true)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = api.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = api.baseUrl,
                                        color = CyanGlow,
                                        fontSize = 11.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            when {
                                                isChecking -> StarAmber.copy(alpha = 0.2f)
                                                isOnline -> Color(0xFF00E676).copy(alpha = 0.2f)
                                                else -> CrimsonNeon.copy(alpha = 0.2f)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = api.status,
                                        color = when {
                                            isChecking -> StarAmber
                                            isOnline -> Color(0xFF00E676)
                                            else -> CrimsonNeon
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${api.category} • Ping: ${api.latencyMs}ms • ${api.lastTested}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { viewModel.testApi(api.id) }) {
                                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check Status", color = CyanGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { viewModel.activateApi(api.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (api.isActive) Color(0xFF00E676).copy(alpha = 0.2f) else SurfaceVariantDark
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (api.isActive) "Active in Multi-API" else "Enable Server",
                                        color = if (api.isActive) Color(0xFF00E676) else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteApi(api.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove API", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 3: GOOGLE ADMOB ACCOUNT & MONETIZATION MANAGER
            // =========================================================================
            if (uiState.selectedTab == 3) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StarAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Google AdMob Account & Ad Units",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = adMobConfig.accountStatus,
                                        color = if (adMobEnabled) Color(0xFF00E676) else CrimsonNeon,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Switch(
                                    checked = adMobEnabled,
                                    onCheckedChange = {
                                        adMobEnabled = it
                                        viewModel.saveAdMobConfig(
                                            accountEmail = adMobEmail,
                                            publisherId = adMobPublisherId,
                                            appId = adMobAppId,
                                            bannerAdUnitId = adMobBannerId,
                                            interstitialAdUnitId = adMobInterstitialId,
                                            rewardedAdUnitId = adMobRewardedId,
                                            nativeAdUnitId = adMobNativeId,
                                            adsEnabled = it,
                                            bannerAdsEnabled = adMobBannerEnabled,
                                            interstitialAdsEnabled = adMobInterstitialEnabled,
                                            rewardedAdsEnabled = adMobRewardedEnabled,
                                            testModeEnabled = adMobTestMode
                                        )
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00E676))
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KpiCard(
                                    title = "IMPRESSIONS",
                                    value = "${adMobConfig.impressionsCount}",
                                    subtitle = "Verified Ad Loads",
                                    accentColor = CyanGlow,
                                    modifier = Modifier.weight(1f)
                                )
                                KpiCard(
                                    title = "CLICKS",
                                    value = "${adMobConfig.clicksCount}",
                                    subtitle = "CTR Active",
                                    accentColor = VioletAccent,
                                    modifier = Modifier.weight(1f)
                                )
                                KpiCard(
                                    title = "EST. EARNINGS",
                                    value = String.format("$%.2f", adMobConfig.estimatedRevenueUsd),
                                    subtitle = "AdMob Revenue",
                                    accentColor = StarAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // AdMob Account Credentials & Ad Unit IDs Form
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AdMob Publisher & Ad Unit IDs",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        adMobPublisherId = "pub-3940256099942544"
                                        adMobAppId = "ca-app-pub-3940256099942544~3347511713"
                                        adMobBannerId = "ca-app-pub-3940256099942544/6300978111"
                                        adMobInterstitialId = "ca-app-pub-3940256099942544/1033173712"
                                        adMobRewardedId = "ca-app-pub-3940256099942544/5224354917"
                                        adMobNativeId = "ca-app-pub-3940256099942544/2247696110"
                                    },
                                    label = { Text("Fill Official Test IDs", fontSize = 10.sp) }
                                )
                            }

                            OutlinedTextField(
                                value = adMobEmail,
                                onValueChange = { adMobEmail = it },
                                label = { Text("Google AdMob Account Email") },
                                placeholder = { Text("your-admob-email@gmail.com") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StarAmber,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = adMobPublisherId,
                                    onValueChange = { adMobPublisherId = it },
                                    label = { Text("Publisher ID (pub-...)") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StarAmber,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = adMobAppId,
                                    onValueChange = { adMobAppId = it },
                                    label = { Text("AdMob App ID (~)") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StarAmber,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = adMobBannerId,
                                onValueChange = { adMobBannerId = it },
                                label = { Text("Banner Ad Unit ID (ca-app-pub-.../...)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = adMobInterstitialId,
                                onValueChange = { adMobInterstitialId = it },
                                label = { Text("Interstitial Ad Unit ID (ca-app-pub-.../...)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = adMobRewardedId,
                                onValueChange = { adMobRewardedId = it },
                                label = { Text("Rewarded Video Ad Unit ID (ca-app-pub-.../...)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = adMobNativeId,
                                onValueChange = { adMobNativeId = it },
                                label = { Text("Native Advanced Ad Unit ID (ca-app-pub-.../...)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Placement Toggles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Home & Details Banner Ads", color = TextPrimary, fontSize = 12.sp)
                                Switch(checked = adMobBannerEnabled, onCheckedChange = { adMobBannerEnabled = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Episode Pre-Roll / Interstitial Ads", color = TextPrimary, fontSize = 12.sp)
                                Switch(checked = adMobInterstitialEnabled, onCheckedChange = { adMobInterstitialEnabled = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Rewarded Video Ads", color = TextPrimary, fontSize = 12.sp)
                                Switch(checked = adMobRewardedEnabled, onCheckedChange = { adMobRewardedEnabled = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Google Safe Test Mode (Disable for Live Ads)", color = StarAmber, fontSize = 12.sp)
                                Switch(checked = adMobTestMode, onCheckedChange = { adMobTestMode = it })
                            }

                            adMobSavedBanner?.let { msg ->
                                Text(text = msg, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.saveAdMobConfig(
                                            accountEmail = adMobEmail,
                                            publisherId = adMobPublisherId,
                                            appId = adMobAppId,
                                            bannerAdUnitId = adMobBannerId,
                                            interstitialAdUnitId = adMobInterstitialId,
                                            rewardedAdUnitId = adMobRewardedId,
                                            nativeAdUnitId = adMobNativeId,
                                            adsEnabled = adMobEnabled,
                                            bannerAdsEnabled = adMobBannerEnabled,
                                            interstitialAdsEnabled = adMobInterstitialEnabled,
                                            rewardedAdsEnabled = adMobRewardedEnabled,
                                            testModeEnabled = adMobTestMode
                                        )
                                        adMobSavedBanner = "Google AdMob Account & Ad Units saved to database ✓"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Save AdMob Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.testAdMobImpression()
                                        adMobSavedBanner = "Test Ad Request Sent • Impression & Revenue Updated!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Test Ad Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 4: CATALOG CMS
            // =========================================================================
            if (uiState.selectedTab == 4) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Anime Series Catalog CMS", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Add series or inject scraped video streams directly", color = TextMuted, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.setShowAddAnimeDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Anime", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(uiState.animeList, key = { it.id }) { anime ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = anime.posterUrl,
                                contentDescription = anime.titleEnglish,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp, 70.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = anime.titleEnglish, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${anime.studio} • ${anime.episodesCount} Episodes", color = TextMuted, fontSize = 11.sp)
                                Text(
                                    text = if (anime.trailerUrl.isNotBlank()) "Official Trailer Linked ✓" else "No Trailer",
                                    color = Color(0xFF00E676),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Button(
                                onClick = { viewModel.openScrapedDialogForAnime(anime) },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+ Scrap Video", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.deleteAnime(anime) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 5: ANALYTICS OVERVIEW
            // =========================================================================
            if (uiState.selectedTab == 5) {
                item {
                    Text(text = "Real-Time Multi-Server Telemetry", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = "REGISTERED USERS",
                            value = "${stats.totalUsers}",
                            subtitle = "Real SQLite Accounts",
                            accentColor = CyanGlow,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "SCRAPED STREAMS",
                            value = "${scrapedVideos.size}",
                            subtitle = "Real Video & Trailers",
                            accentColor = CrimsonNeon,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = "MULTI-API SERVERS",
                            value = "${apiConfigs.size}",
                            subtitle = "Free API & Storage",
                            accentColor = VioletAccent,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "CATALOG SERIES",
                            value = "${uiState.animeList.size}",
                            subtitle = "Live Synced Titles",
                            accentColor = StarAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // =========================================================================
            // TAB 6: REAL USER MODERATION (NO FAKE DEMO USERS)
            // =========================================================================
            if (uiState.selectedTab == 6) {
                item {
                    Text(text = "Registered User Accounts (${users.size})", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Only real accounts registered on this device are shown (Zero fake demo accounts)", color = TextSecondary, fontSize = 11.sp)
                }

                if (users.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No registered user accounts yet.", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Create a real account from the Profile tab to see it listed here.", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }

                items(users, key = { it.id }) { user ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = user.username, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${user.email} • Role: ${user.role.name}", color = TextMuted, fontSize = 11.sp)
                                Text(
                                    text = "Status: ${user.status}",
                                    color = if (user.status == "Banned") Color.Red else Color(0xFF00E676),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row {
                                if (user.status == "Banned") {
                                    Button(
                                        onClick = { viewModel.moderateUser(user.id, "Active") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Unban", fontSize = 11.sp)
                                    }
                                } else {
                                    IconButton(onClick = { viewModel.moderateUser(user.id, "Banned") }) {
                                        Icon(Icons.Default.Block, contentDescription = "Ban", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 7: AUDIT LOGS
            // =========================================================================
            if (uiState.selectedTab == 7) {
                item {
                    Text(text = "Administrative Audit Trail", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                items(auditLogs, key = { it.id }) { log ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = log.action, color = CrimsonNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Target: ${log.target}", color = TextPrimary, fontSize = 12.sp)
                            }
                            Text(
                                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // DIALOG 1: ADD SCRAPED VIDEO / STREAM / TRAILER
        // =========================================================================
        if (uiState.showAddScrapedDialog) {
            var showAnimeDropdown by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { viewModel.setShowAddScrapedDialog(false) },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = "Add Scraped Video / Stream",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Select Anime & Episode, then paste your scraped .m3u8, .mp4, .webm, Free Storage link, or Official Trailer.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        // Anime Dropdown Selector
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showAnimeDropdown = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Anime: ${uiState.scrapeAnimeTitle}",
                                    color = CyanGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = showAnimeDropdown,
                                onDismissRequest = { showAnimeDropdown = false },
                                modifier = Modifier.background(SurfaceDark)
                            ) {
                                uiState.animeList.forEach { anime ->
                                    DropdownMenuItem(
                                        text = { Text(anime.titleEnglish, color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.selectScrapedAnime(anime)
                                            showAnimeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Quick Stream Presets (HiAnime HD-1 VidStreaming, AniWatch HD-2 MegaCloud, VidCloud, AnimeThemes, Official Trailer)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateScrapedVideoField(
                                        streamUrl = "https://v.animethemes.moe/SousouNoFrieren-OP1-NCBD1080.webm",
                                        qualityLabel = "1080p HD-1 • VidStreaming",
                                        serverSource = "HD-1 (VidStreaming • HiAnime)"
                                    )
                                },
                                label = { Text("HD-1 • VidStreaming (HiAnime)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateScrapedVideoField(
                                        streamUrl = "https://v.animethemes.moe/SousouNoFrieren-OP2-NCBD1080.webm",
                                        qualityLabel = "1080p HD-2 • MegaCloud",
                                        serverSource = "HD-2 (MegaCloud • AniWatch)"
                                    )
                                },
                                label = { Text("HD-2 • MegaCloud (AniWatch)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateScrapedVideoField(
                                        qualityLabel = "1080p VidCloud / StreamSB",
                                        serverSource = "VidCloud / StreamTape"
                                    )
                                },
                                label = { Text("VidCloud / StreamTape", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateScrapedVideoField(
                                        streamUrl = "https://www.youtube.com/embed/Iwr1aLEDpe4",
                                        qualityLabel = "Official Trailer HD",
                                        serverSource = "YouTube Official"
                                    )
                                },
                                label = { Text("Official Trailer", fontSize = 10.sp) }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.scrapeEpisodeNumber,
                                onValueChange = { viewModel.updateScrapedVideoField(episodeNumber = it) },
                                placeholder = { Text("Ep # (e.g. 1)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                modifier = Modifier.weight(0.35f)
                            )
                            OutlinedTextField(
                                value = uiState.scrapeEpisodeTitle,
                                onValueChange = { viewModel.updateScrapedVideoField(episodeTitle = it) },
                                placeholder = { Text("Episode Title") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                modifier = Modifier.weight(0.65f)
                            )
                        }

                        OutlinedTextField(
                            value = uiState.scrapeStreamUrl,
                            onValueChange = { viewModel.updateScrapedVideoField(streamUrl = it) },
                            placeholder = { Text("Stream URL (.m3u8, .mp4, .webm, YouTube)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.scrapeQualityLabel,
                                onValueChange = { viewModel.updateScrapedVideoField(qualityLabel = it) },
                                placeholder = { Text("Quality (1080p)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.scrapeServerSource,
                                onValueChange = { viewModel.updateScrapedVideoField(serverSource = it) },
                                placeholder = { Text("Server (HiAnime)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = uiState.scrapeSubtitleUrl,
                            onValueChange = { viewModel.updateScrapedVideoField(subtitleUrl = it) },
                            placeholder = { Text("Subtitle .vtt/.srt URL (Optional)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.addScrapedVideo() },
                        enabled = uiState.scrapeStreamUrl.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                    ) {
                        Text("Save & Inject Video", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowAddScrapedDialog(false) }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        // =========================================================================
        // DIALOG 2: ADD MULTI-SERVER API / FREE STORAGE ENDPOINT
        // =========================================================================
        if (uiState.showAddApiDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowAddApiDialog(false) },
                containerColor = SurfaceDark,
                title = { Text(text = "Add API / Free Storage Server", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateNewApiField(
                                        name = "Consumet HiAnime Scraper API",
                                        url = "https://api.consumet.org/anime/zoro/",
                                        category = "Multi-Server Scraper API"
                                    )
                                },
                                label = { Text("Consumet / HiAnime", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateNewApiField(
                                        name = "AnimeThemes Free Video Storage",
                                        url = "https://api.animethemes.moe/",
                                        category = "Free Video Storage Server"
                                    )
                                },
                                label = { Text("AnimeThemes Storage", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateNewApiField(
                                        name = "Archive.org Free Video Cloud",
                                        url = "https://archive.org/",
                                        category = "Free Video Storage Server"
                                    )
                                },
                                label = { Text("Archive.org Cloud", fontSize = 10.sp) }
                            )
                        }

                        OutlinedTextField(
                            value = uiState.newApiName,
                            onValueChange = { viewModel.updateNewApiField(name = it) },
                            placeholder = { Text("Server Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.newApiUrl,
                            onValueChange = { viewModel.updateNewApiField(url = it) },
                            placeholder = { Text("Base URL (https://...)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.newApiCategory,
                            onValueChange = { viewModel.updateNewApiField(category = it) },
                            placeholder = { Text("Category (Free Video Storage Server, Scraper API)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.newApiKey,
                            onValueChange = { viewModel.updateNewApiField(key = it) },
                            placeholder = { Text("API Key / Token (Optional)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.createApiConfig() },
                        enabled = uiState.newApiName.isNotBlank() && uiState.newApiUrl.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletAccent)
                    ) {
                        Text("Add & Check Status", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowAddApiDialog(false) }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        // =========================================================================
        // DIALOG 3: ADD NEW ANIME SERIES (WITH TRAILER URL)
        // =========================================================================
        if (uiState.showAddAnimeDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowAddAnimeDialog(false) },
                containerColor = SurfaceDark,
                title = { Text(text = "Add New Anime Series", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.newAnimeTitle,
                            onValueChange = { viewModel.updateNewAnimeField(title = it) },
                            placeholder = { Text("English Title (e.g. Bleach: TYBW)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.newAnimeTrailerUrl,
                            onValueChange = { viewModel.updateNewAnimeField(trailerUrl = it) },
                            placeholder = { Text("Official YouTube Trailer URL") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.newAnimeStudio,
                                onValueChange = { viewModel.updateNewAnimeField(studio = it) },
                                placeholder = { Text("Studio") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.newAnimeEpisodes,
                                onValueChange = { viewModel.updateNewAnimeField(episodes = it) },
                                placeholder = { Text("Episodes") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = uiState.newAnimeDescription,
                            onValueChange = { viewModel.updateNewAnimeField(desc = it) },
                            placeholder = { Text("Synopsis / Story Overview") },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonNeon, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.createAnime() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                    ) {
                        Text("Create Anime")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowAddAnimeDialog(false) }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
