package com.example.ui.screens.watchlist

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchlistItem
import com.example.ui.components.RatingBadge
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WatchlistViewModel

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onAnimeClick: (String) -> Unit,
    onResumeEpisode: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allList by viewModel.allWatchlist.collectAsStateWithLifecycle()
    val watchingList by viewModel.watchingList.collectAsStateWithLifecycle()
    val planList by viewModel.planToWatchList.collectAsStateWithLifecycle()
    val completedList by viewModel.completedList.collectAsStateWithLifecycle()
    val favoritesList by viewModel.favoritesList.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()

    val tabs = listOf("All", "Watching", "Plan to Watch", "Completed", "Favorites", "History")

    val activeList = when (uiState.selectedTab) {
        0 -> allList
        1 -> watchingList
        2 -> planList
        3 -> completedList
        4 -> favoritesList
        else -> emptyList()
    }

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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Library",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            if (uiState.selectedTab == 5 && historyList.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.setShowClearHistoryDialog(true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = CrimsonNeon)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
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
                val count = when (index) {
                    0 -> allList.size
                    1 -> watchingList.size
                    2 -> planList.size
                    3 -> completedList.size
                    4 -> favoritesList.size
                    5 -> historyList.size
                    else -> 0
                }
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            text = "$title ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.selectedTab == index) CrimsonNeon else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History Tab View
        if (uiState.selectedTab == 5) {
            if (historyList.isEmpty()) {
                EmptyLibraryView(message = "No watch history yet", subtitle = "Start watching anime to track your progress")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyList, key = { it.episodeId }) { item ->
                        HistoryListItem(
                            item = item,
                            onResume = { onResumeEpisode(item.animeId, item.episodeNumber) },
                            onDelete = { viewModel.deleteHistoryItem(item.episodeId) }
                        )
                    }
                }
            }
        } else {
            // Watchlist Grid View
            if (activeList.isEmpty()) {
                EmptyLibraryView(message = "No anime in this section", subtitle = "Discover anime and add them to your watchlist")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeList, key = { it.animeId }) { item ->
                        WatchlistGridCard(
                            item = item,
                            onClick = { onAnimeClick(item.animeId) },
                            onRemove = { viewModel.removeFromWatchlist(item.animeId) }
                        )
                    }
                }
            }
        }

        // Clear History Dialog
        if (uiState.showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowClearHistoryDialog(false) },
                containerColor = SurfaceDark,
                title = { Text(text = "Clear Watch History?", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text(text = "This will remove all your saved episodes and playback progress.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.clearAllHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowClearHistoryDialog(false) }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun WatchlistGridCard(
    item: WatchlistItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("watchlist_card_${item.animeId}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.animeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                }

                RatingBadge(
                    rating = item.rating,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.animeTitle,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.status.displayName} • ${item.episodeCount} Eps",
                    color = CrimsonNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryListItem(
    item: WatchHistoryItem,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onResume)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(65.dp)
            ) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.animeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                        .background(CrimsonNeon.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.animeTitle,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ep ${item.episodeNumber} • ${item.episodeTitle}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { item.percentage },
                    color = CrimsonNeon,
                    trackColor = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun EmptyLibraryView(message: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = message, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subtitle, color = TextMuted, fontSize = 12.sp)
    }
}
