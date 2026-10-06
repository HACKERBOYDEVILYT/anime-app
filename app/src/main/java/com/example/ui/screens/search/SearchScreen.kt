package com.example.ui.screens.search

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Anime
import com.example.data.model.AnimeSortOption
import com.example.data.model.AnimeStatus
import com.example.data.model.AnimeType
import com.example.ui.components.AnimeGridCard
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
import com.example.viewmodel.SearchViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showAdvancedFilters by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Search Header & Input Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🔎 Advanced Search & Filters",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Search Anime • Characters • Studios • Genres • Years • VAs • Episodes",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { showAdvancedFilters = !showAdvancedFilters },
                    modifier = Modifier
                        .background(if (showAdvancedFilters) CrimsonNeon.copy(alpha = 0.2f) else SurfaceVariantDark, CircleShape)
                        .testTag("toggle_filters_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Toggle Advanced Filters",
                        tint = if (showAdvancedFilters) CrimsonNeon else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = {
                    Text(
                        text = "Search anime, character (Gojo), VA, studio (MAPPA), year...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CrimsonNeon
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onQueryChange("") },
                            modifier = Modifier.testTag("clear_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.onSubmitQuery(uiState.query)
                        focusManager.clearFocus()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonNeon,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )
        }

        // Categorized Search Suggestions (Anime, Characters, Episodes, Genres) when typing
        if (uiState.query.isNotBlank() && uiState.suggestions.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.suggestions) { suggestion ->
                    Surface(
                        onClick = { viewModel.onSubmitQuery(suggestion.targetQuery) },
                        color = SurfaceDark,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${suggestion.category}] ",
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = suggestion.label,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Trending Searches & Recent Search History when query is empty
        if (uiState.query.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🔥 Trending Searches",
                    color = StarAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(uiState.trendingSearches) { trend ->
                        Surface(
                            onClick = { viewModel.onSubmitQuery(trend) },
                            color = SurfaceVariantDark,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "🔥 $trend",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                if (recentSearches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕒 Recent Searches",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Clear All", color = CrimsonNeon, fontSize = 11.sp)
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentSearches) { historyQuery ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.onQueryChange(historyQuery) },
                                label = { Text(historyQuery, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceVariantDark,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Quick Genre + Sub/Dub + Ongoing/Completed Filter Bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.subOnly,
                    onClick = { viewModel.toggleSubOnly() },
                    label = { Text("SUB", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent.copy(alpha = 0.22f),
                        selectedLabelColor = CyanAccent
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.dubOnly,
                    onClick = { viewModel.toggleDubOnly() },
                    label = { Text("DUB", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StarAmber.copy(alpha = 0.22f),
                        selectedLabelColor = StarAmber
                    )
                )
            }
            items(uiState.availableGenres.filter { it != "All" }) { genre ->
                val isSelected = uiState.selectedGenre == genre
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectGenre(genre) },
                    label = { Text(genre, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonNeon,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceVariantDark,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Expandable Comprehensive Filter Drawer (Year, Season, Status, Type, Studio, Score, Episode Count, Duration, Language, Sort)
        if (showAdvancedFilters) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(SurfaceDark, RoundedCornerShape(14.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Multi-Attribute Catalog Filters",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.clearAllFilters() }) {
                            Text("Reset All", color = CrimsonNeon, fontSize = 12.sp)
                        }
                    }
                }

                // Sort By
                item {
                    Text("Sort By", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AnimeSortOption.entries) { sort ->
                            FilterChip(
                                selected = uiState.sortOption == sort,
                                onClick = { viewModel.selectSort(sort) },
                                label = { Text(sort.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Status (Ongoing / Airing, Completed, Upcoming) & Type (TV, Movie, OVA, ONA)
                item {
                    Text("Status & Format Type", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AnimeStatus.entries) { status ->
                            FilterChip(
                                selected = uiState.selectedStatus == status,
                                onClick = { viewModel.selectStatus(status) },
                                label = { Text(status.displayName, fontSize = 11.sp) }
                            )
                        }
                        items(AnimeType.entries) { type ->
                            FilterChip(
                                selected = uiState.selectedType == type,
                                onClick = { viewModel.selectType(type) },
                                label = { Text(type.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Studio Filter
                item {
                    Text("Animation Studio", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.availableStudios) { studio ->
                            FilterChip(
                                selected = uiState.selectedStudio == studio,
                                onClick = { viewModel.selectStudio(studio) },
                                label = { Text(studio, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Release Year & Season
                item {
                    Text("Year & Season", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val years = listOf(2026, 2025, 2024, 2023, 2022, 2021, 2020, 2019)
                    val seasons = listOf("Winter", "Spring", "Summer", "Fall")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(seasons) { season ->
                            FilterChip(
                                selected = uiState.selectedSeason == season,
                                onClick = { viewModel.selectSeason(season) },
                                label = { Text(season, fontSize = 11.sp) }
                            )
                        }
                        items(years) { yr ->
                            FilterChip(
                                selected = uiState.selectedYear == yr,
                                onClick = { viewModel.selectYear(yr) },
                                label = { Text("$yr", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Score, Episode Count, Duration & Language
                item {
                    Text("Score, Episode Count, Duration & Audio Language", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(90 to "Score 90+", 80 to "Score 80+", 70 to "Score 70+").forEach { (sc, label) ->
                            item {
                                FilterChip(
                                    selected = uiState.minScoreFilter == sc,
                                    onClick = { viewModel.selectMinScore(sc) },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                        listOf("1-12" to "1–12 Eps", "13-24" to "13–24 Eps", "25+" to "25+ Eps").forEach { (rng, label) ->
                            item {
                                FilterChip(
                                    selected = uiState.episodeCountFilter == rng,
                                    onClick = { viewModel.selectEpisodeCountRange(rng) },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                        listOf("<20m" to "<20 min", "20-30m" to "20–30 min", "30m+" to "30+ min").forEach { (dur, label) ->
                            item {
                                FilterChip(
                                    selected = uiState.durationFilter == dur,
                                    onClick = { viewModel.selectDurationRange(dur) },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                        listOf("Japanese", "English", "Hindi", "Bengali").forEach { lang ->
                            item {
                                FilterChip(
                                    selected = uiState.languageFilter == lang,
                                    onClick = { viewModel.selectLanguageFilter(lang) },
                                    label = { Text("🎙️ $lang", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Matched Characters & Episode Results Strip (when searching)
        if (uiState.matchedCharacters.isNotEmpty() || uiState.matchedEpisodesSummary.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (uiState.matchedCharacters.isNotEmpty()) {
                    Text(
                        text = "🎭 Matched Characters & Voice Actors (${uiState.matchedCharacters.size})",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.matchedCharacters, key = { it.id }) { ch ->
                            Surface(
                                color = SurfaceDark,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ch.avatarUrl,
                                        contentDescription = ch.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(ch.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("VA: ${ch.voiceActor}", color = TextSecondary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Results Count Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${uiState.results.size} Anime Found",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            val hasActiveFilters = uiState.selectedGenre != null ||
                uiState.selectedYear != null ||
                uiState.selectedSeason != null ||
                uiState.selectedType != null ||
                uiState.selectedStatus != null ||
                uiState.selectedStudio != null ||
                uiState.minScoreFilter != null ||
                uiState.subOnly ||
                uiState.dubOnly
            if (hasActiveFilters) {
                Text(
                    text = "Clear Active Filters",
                    color = CrimsonNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.clearAllFilters() }
                )
            }
        }

        // Results Grid or Empty-State UI with No-Result Suggestions
        if (uiState.results.isEmpty() && !uiState.isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = CrimsonNeon,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Exact Anime Found for '${uiState.query}'",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try one of these popular anime suggestions or reset your filters:",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.noResultSuggestions.forEach { suggestion ->
                            Surface(
                                onClick = { viewModel.onSubmitQuery(suggestion) },
                                color = SurfaceDark,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.border(1.dp, CrimsonNeon.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = "✨ $suggestion",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.clearAllFilters() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                    ) {
                        Text("Reset Search & Filters")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 148.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.results, key = { it.id }) { anime ->
                    AnimeGridCard(
                        anime = anime,
                        onClick = { onAnimeClick(anime) }
                    )
                }
            }
        }
    }
}
