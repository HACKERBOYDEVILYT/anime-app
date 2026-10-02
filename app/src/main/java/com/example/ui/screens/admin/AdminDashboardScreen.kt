package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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

    val tabs = listOf("Analytics", "Catalog CMS", "Video Pipeline", "Moderation", "Audit Logs", "API Manager")

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
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(text = "Admin Control Suite", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "KuroStream Master Console", color = VioletAccent, fontSize = 11.sp)
                }
            }

            Box(
                modifier = Modifier
                    .background(VioletAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "SuperAdmin", color = VioletAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = BackgroundDark,
            contentColor = CrimsonNeon,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                    color = CrimsonNeon
                )
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
            // Tab 0: Analytics Overview
            if (uiState.selectedTab == 0) {
                // Key KPI Metrics Grid
                item {
                    Text(text = "Platform Telemetry", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard("Active Users", "${stats.activeUsers}", "/ ${stats.totalUsers}", CyanGlow, Modifier.weight(1f))
                        KpiCard("Watch Time", "${stats.totalWatchTimeHours}h", "Total Streamed", StarAmber, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard("Catalog Size", "${stats.totalAnime} Anime", "${stats.totalEpisodes} Episodes", CrimsonNeon, Modifier.weight(1f))
                        KpiCard("Stream Sessions", "${stats.totalWatchSessions}", "HLS Manifest hits", VioletAccent, Modifier.weight(1f))
                    }
                }

                // Daily Views Bar Visualizer
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Weekly Stream Views (Mon - Sun)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val maxViews = stats.dailyViews.maxOfOrNull { it.views } ?: 30000
                                stats.dailyViews.forEach { stat ->
                                    val heightFraction = (stat.views.toFloat() / maxViews.toFloat()).coerceIn(0.1f, 1f)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.fillMaxHeight()
                                    ) {
                                        Text(text = "${stat.views / 1000}k", color = TextMuted, fontSize = 9.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height((100 * heightFraction).dp)
                                                .background(
                                                    if (stat.day == "Sat" || stat.day == "Sun") CrimsonNeon else VioletAccent,
                                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = stat.day, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Device Distribution
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Client Device Distribution", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            stats.deviceStats.forEach { (device, percent) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = device, color = TextSecondary, fontSize = 12.sp)
                                    Text(text = "$percent%", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { percent / 100f },
                                    color = CrimsonNeon,
                                    trackColor = SurfaceVariantDark,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            // Tab 1: Catalog CMS
            if (uiState.selectedTab == 1) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Anime Catalog (${uiState.animeList.size})", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                                Text(text = "${anime.studio} • ${anime.episodesCount} Episodes • ${anime.type.name}", color = TextMuted, fontSize = 11.sp)
                                Text(text = "Rating: ${anime.rating} ★ (${anime.score}%)", color = StarAmber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            IconButton(onClick = { viewModel.deleteAnime(anime) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // Tab 2: Video Transcoding Pipeline
            if (uiState.selectedTab == 2) {
                item {
                    Text(text = "HLS Transcoding Pipeline", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "FFmpeg Master → 1080p, 720p, 480p Multi-bitrate HLS", color = TextMuted, fontSize = 11.sp)
                }

                items(videoJobs, key = { it.id }) { job ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${job.animeTitle} - Episode ${job.episodeNumber}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (job.status) {
                                                "Completed" -> Color(0xFF00E676).copy(alpha = 0.2f)
                                                "Processing" -> CrimsonNeon.copy(alpha = 0.2f)
                                                else -> Color.Gray.copy(alpha = 0.2f)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = job.status,
                                        color = when (job.status) {
                                            "Completed" -> Color(0xFF00E676)
                                            "Processing" -> CrimsonNeon
                                            else -> TextSecondary
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Source: ${job.sourceResolution} | Target: master.m3u8", color = TextMuted, fontSize = 11.sp)

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { job.progressPercent / 100f },
                                color = if (job.status == "Completed") Color(0xFF00E676) else CrimsonNeon,
                                trackColor = SurfaceVariantDark,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                            )

                            if (job.status != "Completed") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { viewModel.triggerTranscode(job.animeTitle, job.episodeNumber) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Restart Transcode", fontSize = 11.sp, color = CrimsonNeon)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 3: Moderation
            if (uiState.selectedTab == 3) {
                item {
                    Text(text = "User Moderation & Access Control", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                                    text = "Status: ${user.status} • Reports: ${user.reportsCount}",
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
                                    IconButton(onClick = { viewModel.moderateUser(user.id, "Warned") }) {
                                        Icon(Icons.Default.Warning, contentDescription = "Warn", tint = StarAmber)
                                    }
                                    IconButton(onClick = { viewModel.moderateUser(user.id, "Banned") }) {
                                        Icon(Icons.Default.Block, contentDescription = "Ban", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 4: Audit Logs
            if (uiState.selectedTab == 4) {
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
                                Text(text = "By: ${log.adminName}", color = TextMuted, fontSize = 10.sp)
                            }
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp)),
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Tab 5: Dynamic API Endpoint Manager
            if (uiState.selectedTab == 5) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Dynamic API Endpoints", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Add, test and switch network services in real time", color = TextSecondary, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.setShowAddApiDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add API", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Active Live Route Banner
                item {
                    val activeApi = apiConfigs.firstOrNull { it.isActive }
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(0xFF00E676), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ACTIVE LIVE ROUTE",
                                        color = Color(0xFF00E676),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${activeApi?.latencyMs ?: 35}ms latency",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeApi?.name ?: "KuroStream Master Cluster",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeApi?.baseUrl ?: "https://api.kurostream.app/",
                                color = CyanGlow,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // List of all configured API endpoints
                items(apiConfigs, key = { it.id }) { api ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (api.isActive) SurfaceVariantDark else SurfaceDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = api.name,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (api.isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF00E676).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = "CURRENT ACTIVE", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = api.baseUrl,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(VioletAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = api.category, color = VioletAccent, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (api.status == "Online") Color(0xFF00E676) else StarAmber,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${api.status} • ${api.latencyMs}ms",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.testApi(api.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Ping", modifier = Modifier.size(12.dp), tint = TextSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test Ping", fontSize = 10.sp, color = TextSecondary)
                                    }

                                    if (!api.isActive) {
                                        Button(
                                            onClick = { viewModel.activateApi(api.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Set Active", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (api.id.startsWith("api_") && !api.id.startsWith("api_main") && !api.id.startsWith("api_cdn")) {
                                        IconButton(
                                            onClick = { viewModel.deleteApi(api.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Custom API Modal Dialog
        if (uiState.showAddApiDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowAddApiDialog(false) },
                containerColor = SurfaceDark,
                title = { Text(text = "Add Custom API Endpoint", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Quick Presets (Click to autofill):",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateNewApiField(
                                        name = "AniList GraphQL API",
                                        url = "https://graphql.anilist.co",
                                        category = "Metadata & Catalog"
                                    )
                                },
                                label = { Text("AniList", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceVariantDark,
                                    labelColor = TextPrimary
                                )
                            )

                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateNewApiField(
                                        name = "Jikan MyAnimeList API",
                                        url = "https://api.jikan.moe/v4",
                                        category = "MAL Sync & Info"
                                    )
                                },
                                label = { Text("Jikan MAL", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceVariantDark,
                                    labelColor = TextPrimary
                                )
                            )

                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateNewApiField(
                                        name = "Kitsu Public API",
                                        url = "https://kitsu.io/api/edge",
                                        category = "Catalog Discovery"
                                    )
                                },
                                label = { Text("Kitsu", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceVariantDark,
                                    labelColor = TextPrimary
                                )
                            )

                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.updateNewApiField(
                                        name = "Custom Streaming CDN",
                                        url = "https://cdn.anime-node.com/api/v1",
                                        category = "Streaming HLS"
                                    )
                                },
                                label = { Text("Custom CDN", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceVariantDark,
                                    labelColor = TextPrimary
                                )
                            )
                        }

                        OutlinedTextField(
                            value = uiState.newApiName,
                            onValueChange = { viewModel.updateNewApiField(name = it) },
                            placeholder = { Text("API Name (e.g. Frankfurt Mirror)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.newApiUrl,
                            onValueChange = { viewModel.updateNewApiField(url = it) },
                            placeholder = { Text("Base URL (e.g. https://node.example.com/)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.newApiCategory,
                            onValueChange = { viewModel.updateNewApiField(category = it) },
                            placeholder = { Text("Category (Streaming HLS, Catalog, Backup)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.newApiKey,
                            onValueChange = { viewModel.updateNewApiField(key = it) },
                            placeholder = { Text("API Key / Bearer Token (Optional)") },
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
                        Text("Add Endpoint", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowAddApiDialog(false) }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        // Add Anime Modal Dialog
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
                            value = uiState.newAnimeJapanese,
                            onValueChange = { viewModel.updateNewAnimeField(japanese = it) },
                            placeholder = { Text("Japanese Title (e.g. BLEACH 千年血戦篇)") },
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
