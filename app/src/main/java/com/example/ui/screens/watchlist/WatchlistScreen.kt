package com.example.ui.screens.watchlist

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.ui.components.ContinueWatchingRow
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WatchlistSortMode
import com.example.viewmodel.WatchlistViewModel

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onAnimeIdClick: (String) -> Unit,
    onWatchEpisodeClick: (String, Int) -> Unit = { animeId, _ -> onAnimeIdClick(animeId) },
    modifier: Modifier = Modifier
) {
    val allItems by viewModel.allWatchlist.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val selectedCollectionId by viewModel.selectedCollectionId.collectAsStateWithLifecycle()
    val customCollections by viewModel.customCollections.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()

    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var newColName by remember { mutableStateOf("") }
    var newColDesc by remember { mutableStateOf("") }
    var showSortDropdown by remember { mutableStateOf(false) }

    val selectedCollection = customCollections.find { it.id == selectedCollectionId }

    val filteredItems = remember(
        allItems,
        selectedStatus,
        showFavoritesOnly,
        selectedCollection,
        searchQuery,
        sortMode
    ) {
        val statusFiltered = when {
            showFavoritesOnly -> allItems.filter { it.isFavorite }
            selectedCollection != null -> allItems.filter { it.animeId in selectedCollection.animeIds }
            selectedStatus != null -> allItems.filter { it.status == selectedStatus }
            else -> allItems
        }
        val queryFiltered = if (searchQuery.isBlank()) {
            statusFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            statusFiltered.filter { it.animeTitle.lowercase().contains(q) }
        }
        when (sortMode) {
            WatchlistSortMode.RECENT -> queryFiltered.sortedByDescending { it.addedAt }
            WatchlistSortMode.RATING_DESC -> queryFiltered.sortedByDescending { it.rating }
            WatchlistSortMode.TITLE_ASC -> queryFiltered.sortedBy { it.animeTitle.lowercase() }
            WatchlistSortMode.EPISODES_DESC -> queryFiltered.sortedByDescending { it.episodeCount }
        }
    }

    if (showCreateCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCollectionDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Create Custom Collection", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        label = { Text("Collection Name (e.g. Peak Fantasy)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newColDesc,
                        onValueChange = { newColDesc = it },
                        label = { Text("Description") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newColName.isNotBlank()) {
                            viewModel.createCustomCollection(newColName, newColDesc)
                            newColName = ""
                            newColDesc = ""
                            showCreateCollectionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCollectionDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Header with Grid/List Toggle, Sort Dropdown, and New Collection Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "❤️ My Watchlist & Collections",
                    color = TextPrimary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${allItems.size} Tracked • ${customCollections.size} Collections • Cloud Synced",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showCreateCollectionDialog = true }) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Collection", tint = CyanAccent)
                }
                Box {
                    IconButton(onClick = { showSortDropdown = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Watchlist", tint = TextPrimary)
                    }
                    DropdownMenu(
                        expanded = showSortDropdown,
                        onDismissRequest = { showSortDropdown = false },
                        modifier = Modifier.background(SurfaceVariantDark)
                    ) {
                        WatchlistSortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = mode.label,
                                        color = if (sortMode == mode) CrimsonNeon else TextPrimary,
                                        fontWeight = if (sortMode == mode) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setSortMode(mode)
                                    showSortDropdown = false
                                }
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { viewModel.toggleGridListView() },
                    modifier = Modifier.testTag("watchlist_view_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid or List View",
                        tint = StarAmber
                    )
                }
            }
        }

        // Search Within Watchlist Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            placeholder = { Text("Search within your watchlist...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CrimsonNeon,
                unfocusedBorderColor = CardBorder,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("watchlist_search_input")
        )

        // Custom Collections Bar (My Top 10, Shounen, Romance, Comedy, Action, Rewatch List + Custom)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(customCollections, key = { it.id }) { col ->
                val isSelected = selectedCollectionId == col.id
                Surface(
                    onClick = { viewModel.selectCollectionFilter(col.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CyanAccent.copy(alpha = 0.22f) else SurfaceDark,
                    modifier = Modifier.border(
                        1.dp,
                        if (isSelected) CyanAccent else CardBorder,
                        RoundedCornerShape(12.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${col.iconEmoji} ${col.name}", color = if (isSelected) CyanAccent else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("(${col.animeIds.size})", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Status Tabs (All, Favorites, Watching, Plan to Watch, Completed, On Hold, Dropped)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isAll = selectedStatus == null && !showFavoritesOnly && selectedCollectionId == null
                FilterChip(
                    selected = isAll,
                    onClick = { viewModel.selectStatusFilter(null) },
                    label = { Text("All (${allItems.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonNeon,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceVariantDark,
                        labelColor = TextSecondary
                    )
                )
            }

            item {
                FilterChip(
                    selected = showFavoritesOnly,
                    onClick = { viewModel.toggleFavoritesFilter() },
                    label = { Text("Favorites (${allItems.count { it.isFavorite }})") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (showFavoritesOnly) Color.White else CrimsonNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonNeon,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceVariantDark,
                        labelColor = TextSecondary
                    )
                )
            }

            items(WatchStatus.entries) { status ->
                val count = allItems.count { it.status == status }
                val isSelected = selectedStatus == status && !showFavoritesOnly && selectedCollectionId == null
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectStatusFilter(status) },
                    label = { Text("${status.displayName} ($count)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonNeon,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceVariantDark,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BookmarkRemove,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Watchlist Filter is Empty",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Save your favorite anime or add titles to custom collections like 'My Top 10' and 'Rewatch List'.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (continueWatching.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ContinueWatchingRow(
                            items = continueWatching,
                            onResumeClick = { item -> onWatchEpisodeClick(item.animeId, item.episodeNumber) },
                            onRemoveClick = { item -> viewModel.removeContinueWatching(item) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                items(filteredItems, key = { it.animeId }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAnimeIdClick(item.animeId) }
                    ) {
                        Column {
                            AsyncImage(
                                model = item.posterUrl,
                                contentDescription = item.animeTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(185.dp)
                            )
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(item.animeTitle, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${item.status.displayName} • ⭐ ${item.rating}", color = CyanAccent, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (continueWatching.isNotEmpty()) {
                    item {
                        ContinueWatchingRow(
                            items = continueWatching,
                            onResumeClick = { item -> onWatchEpisodeClick(item.animeId, item.episodeNumber) },
                            onRemoveClick = { item -> viewModel.removeContinueWatching(item) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                items(filteredItems, key = { it.animeId }) { item ->
                    WatchlistCardItem(
                        item = item,
                        collections = customCollections,
                        onClick = { onAnimeIdClick(item.animeId) },
                        onStatusChange = { newStatus -> viewModel.updateStatus(item, newStatus) },
                        onFavoriteToggle = { viewModel.toggleFavorite(item) },
                        onAddToCollection = { colId -> viewModel.addAnimeToCollection(colId, item.animeId) },
                        onRemove = { viewModel.removeFromWatchlist(item.animeId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchlistCardItem(
    item: WatchlistItem,
    collections: List<com.example.data.model.WatchlistCollection>,
    onClick: () -> Unit,
    onStatusChange: (WatchStatus) -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToCollection: (String) -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showCollectionMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("watchlist_item_${item.animeId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.animeTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(72.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.animeTitle,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f • %d Eps", item.rating, item.episodeCount),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Surface(
                            color = CrimsonNeon.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { showMenu = true }
                        ) {
                            Text(
                                text = item.status.displayName,
                                color = CrimsonNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceVariantDark)
                        ) {
                            WatchStatus.entries.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.displayName, color = TextPrimary) },
                                    onClick = {
                                        onStatusChange(st)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        Surface(
                            color = CyanAccent.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { showCollectionMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                                Text("Collection", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        DropdownMenu(
                            expanded = showCollectionMenu,
                            onDismissRequest = { showCollectionMenu = false },
                            modifier = Modifier.background(SurfaceVariantDark)
                        ) {
                            collections.forEach { col ->
                                DropdownMenuItem(
                                    text = { Text("${col.iconEmoji} ${col.name}", color = TextPrimary) },
                                    onClick = {
                                        onAddToCollection(col.id)
                                        showCollectionMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) CrimsonNeon else TextSecondary
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove from watchlist",
                        tint = TextMuted
                    )
                }
            }
        }
    }
}
