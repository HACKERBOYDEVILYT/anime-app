package com.example.ui.screens.downloads

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entity.DownloadItemEntity
import com.example.data.repository.DownloadRuntimeTelemetry
import com.example.data.repository.DownloadsRepository
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
import kotlinx.coroutines.launch

@Composable
fun DownloadsScreen(
    downloadsRepository: DownloadsRepository,
    onBack: () -> Unit,
    onPlayOfflineEpisode: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val downloads by downloadsRepository.allDownloads.collectAsStateWithLifecycle(initialValue = emptyList())
    val settings by downloadsRepository.settings.collectAsStateWithLifecycle()
    val telemetryMap by downloadsRepository.telemetryById.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var selectedStatusTab by remember { mutableStateOf("ALL") } // "ALL", "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED", "WAITING"
    var showSettingsPanel by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        downloadsRepository.seedInitialDownloadsIfEmpty()
    }

    val storageSummary = remember(downloads) {
        downloadsRepository.getDeviceStorageSummary(downloads)
    }

    val filteredDownloads = remember(downloads, selectedStatusTab) {
        if (selectedStatusTab == "ALL") downloads
        else downloads.filter { it.status.equals(selectedStatusTab, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("downloads_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "📥 Offline Downloads Manager",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${downloads.size} items • Queue, Pause/Resume & Auto-Recovery",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showSettingsPanel = !showSettingsPanel }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Download Settings",
                        tint = if (showSettingsPanel) CrimsonNeon else TextPrimary
                    )
                }
                if (downloads.isNotEmpty()) {
                    IconButton(
                        onClick = { scope.launch { downloadsRepository.clearAll() } },
                        modifier = Modifier.testTag("downloads_clear_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All Downloads",
                            tint = CrimsonNeon
                        )
                    }
                }
            }
        }

        // Active Background Download Notification Banner
        settings.activeNotificationBanner?.let { banner ->
            Surface(
                color = CyanAccent.copy(alpha = 0.14f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = banner,
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (downloads.any { it.status == "FAILED" || it.status == "PAUSED" }) {
                        Text(
                            text = "Recover All",
                            color = StarAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clickable { downloadsRepository.recoverAllFailedDownloads() }
                        )
                    }
                }
            }
        }

        // Storage Information Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = CrimsonNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Device & App Storage",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${storageSummary.appDownloadsUsedMb} MB App • ${String.format("%.1f", storageSummary.freeDeviceStorageGb)} GB Free",
                        color = EmeraldSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { storageSummary.usedStorageFraction },
                    color = CrimsonNeon,
                    trackColor = SurfaceVariantDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
        }

        // Collapsible Download Preferences Panel (Wi-Fi Only, Mobile Data, Default Quality, Auto-Delete Watched)
        if (showSettingsPanel) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚙️ Professional Download Settings", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wi-Fi Only Downloads", color = TextSecondary, fontSize = 12.sp)
                        Switch(
                            checked = settings.wifiOnly,
                            onCheckedChange = { downloadsRepository.updateSettings(wifiOnly = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonNeon)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Allow Mobile Data", color = TextSecondary, fontSize = 12.sp)
                        Switch(
                            checked = settings.allowMobileData,
                            onCheckedChange = { downloadsRepository.updateSettings(allowMobileData = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CyanAccent)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-Delete Watched Episodes", color = TextSecondary, fontSize = 12.sp)
                        Switch(
                            checked = settings.autoDeleteWatchedEpisodes,
                            onCheckedChange = { downloadsRepository.updateSettings(autoDeleteWatched = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldSuccess)
                        )
                    }
                    Text("Default Download Quality:", color = TextSecondary, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("360p", "480p", "720p", "1080p").forEach { q ->
                            FilterChip(
                                selected = settings.defaultQuality == q,
                                onClick = { downloadsRepository.updateSettings(defaultQuality = q) },
                                label = { Text(q, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Download Status Filter Tabs (All, Downloading, Paused, Completed, Failed, Waiting)
        val statusTabs = listOf(
            "ALL" to "All (${downloads.size})",
            "DOWNLOADING" to "Downloading",
            "PAUSED" to "Paused",
            "COMPLETED" to "Completed",
            "FAILED" to "Failed",
            "WAITING" to "Waiting"
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statusTabs) { (code, label) ->
                FilterChip(
                    selected = selectedStatusTab == code,
                    onClick = { selectedStatusTab = code },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
        }

        if (filteredDownloads.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Offline Downloads in '$selectedStatusTab'",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Open any anime details page to download individual episodes or an entire season.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDownloads, key = { it.id }) { item ->
                    DownloadEpisodeCard(
                        item = item,
                        telemetry = telemetryMap[item.id],
                        onPlay = { onPlayOfflineEpisode(item.animeId, item.episodeNumber) },
                        onPause = { downloadsRepository.pauseDownload(item.id) },
                        onResume = { downloadsRepository.resumeDownload(item.id) },
                        onRetry = { downloadsRepository.retryFailedDownload(item.id) },
                        onDelete = { scope.launch { downloadsRepository.removeDownload(item.id) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadEpisodeCard(
    item: DownloadItemEntity,
    telemetry: DownloadRuntimeTelemetry?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = item.status == "COMPLETED"
    val isPaused = item.status == "PAUSED"
    val isFailed = item.status == "FAILED"
    val isWaiting = item.status == "WAITING"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onPlay() }
            .testTag("download_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(106.dp)
                    .height(66.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.episodeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CrimsonNeon, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Offline",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.animeTitle,
                    color = CrimsonNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ep ${item.episodeNumber} - ${item.episodeTitle}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SurfaceVariantDark,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.quality,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${item.sizeMb} MB",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    when {
                        isCompleted -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Completed", color = EmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        isPaused -> Text("Paused (${item.progressPercent}%)", color = StarAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        isFailed -> Text("Failed • Tap Retry", color = CrimsonNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        isWaiting -> Text("Waiting in Queue", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        else -> Text(
                            text = "${item.progressPercent}% • ${telemetry?.speedLabel ?: "8.4 MB/s"} • ${telemetry?.remainingTimeLabel ?: "00:15"}",
                            color = StarAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.progressPercent / 100f },
                        color = if (isFailed) CrimsonNeon else StarAmber,
                        trackColor = SurfaceVariantDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }

            // Pause / Resume / Retry / Delete controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    isFailed -> {
                        IconButton(onClick = onRetry) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry Download", tint = StarAmber)
                        }
                    }
                    isPaused || isWaiting -> {
                        IconButton(onClick = onResume) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume Download", tint = CyanAccent)
                        }
                    }
                    !isCompleted -> {
                        IconButton(onClick = onPause) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause Download", tint = StarAmber)
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Download",
                        tint = TextMuted
                    )
                }
            }
        }
    }
}
