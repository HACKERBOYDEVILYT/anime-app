package com.example.ui.screens.details

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Anime
import com.example.data.model.AnimeCharacter
import com.example.data.model.Episode
import com.example.data.model.Review
import com.example.data.model.WatchStatus
import com.example.data.repository.DownloadsRepository
import com.example.ui.components.AnimeCard
import com.example.ui.components.CharacterDetailDialog
import com.example.ui.components.QualityBadge
import com.example.ui.components.RatingBadge
import com.example.ui.components.SubDubBadges
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DetailsViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailsScreen(
    viewModel: DetailsViewModel,
    downloadsRepository: DownloadsRepository? = null,
    onBack: () -> Unit,
    onPlayEpisode: (String, Int) -> Unit,
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var selectedCharacter by remember { mutableStateOf<AnimeCharacter?>(null) }
    var episodeAudioFilter by remember { mutableStateOf("ALL") } // "ALL", "SUB", "DUB"

    val anime = uiState.anime

    if (anime == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CrimsonNeon)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Backdrop Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = anime.bannerUrl.ifBlank { anime.posterUrl },
                        contentDescription = anime.titleEnglish,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient fade into body
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        BackgroundDark.copy(alpha = 0.8f),
                                        BackgroundDark
                                    )
                                )
                            )
                    )

                    // Top Bar Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .testTag("details_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Watch ${anime.titleEnglish} on KuroStream! High Quality Anime Streaming.")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Anime"))
                                },
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            val isFav = uiState.watchlistItem?.isFavorite == true
                            IconButton(
                                onClick = { viewModel.toggleFavorite() },
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .testTag("details_fav_btn")
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) CrimsonNeon else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Anime Header Overview Info
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Poster Thumbnail
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .width(115.dp)
                            .height(165.dp)
                    ) {
                        AsyncImage(
                            model = anime.posterUrl,
                            contentDescription = anime.titleEnglish,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Metadata details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = anime.titleEnglish,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )

                        if (anime.titleJapanese.isNotBlank()) {
                            Text(
                                text = anime.titleJapanese,
                                color = CrimsonNeon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RatingBadge(rating = anime.rating)
                            QualityBadge("HD")
                            SubDubBadges()
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${anime.type.displayName} • ${anime.releaseYear} • ${anime.episodesCount} Eps",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "Studio: ${anime.studio}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Text(
                            text = "Status: ${anime.status.displayName}",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Primary Action Buttons: Play Now & Watchlist Status
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onPlayEpisode(anime.id, 1) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonNeon,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("details_play_ep1_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Play Episode 1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showStatusDropdown = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("details_watchlist_dropdown_btn")
                        ) {
                            Icon(
                                imageVector = if (uiState.watchlistItem != null) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.watchlistItem?.status?.displayName ?: "Add to Watchlist",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            WatchStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = status.displayName,
                                            color = if (uiState.watchlistItem?.status == status) CrimsonNeon else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateWatchStatus(status)
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Genre Pills
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    anime.genres.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .background(SurfaceVariantDark, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = genre, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Navigation Tabs
            item {
                Spacer(modifier = Modifier.height(12.dp))
                val tabs = listOf("Episodes", "Overview & Cast", "Reviews", "Related")
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
                                    fontSize = 13.sp,
                                    fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.selectedTab == index) CrimsonNeon else TextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Tab 0: Episodes List
            if (uiState.selectedTab == 0) {
                // Search & Sort bar for episodes
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.episodeSearchQuery,
                            onValueChange = { viewModel.onEpisodeSearchChange(it) },
                            placeholder = { Text("Filter episodes…", color = TextMuted, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                            trailingIcon = {
                                if (uiState.episodeSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onEpisodeSearchChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonNeon,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = { viewModel.toggleEpisodeSort() },
                            modifier = Modifier.background(SurfaceDark, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                tint = if (uiState.isEpisodeSortAsc) CrimsonNeon else TextSecondary
                            )
                        }
                    }
                }

                // Audio Format filter chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = episodeAudioFilter == "ALL",
                            onClick = { episodeAudioFilter = "ALL" },
                            label = { Text("All", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonNeon,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = episodeAudioFilter == "SUB",
                            onClick = { episodeAudioFilter = "SUB" },
                            label = { Text("🇯🇵 Sub", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonNeon,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = episodeAudioFilter == "DUB",
                            onClick = { episodeAudioFilter = "DUB" },
                            label = { Text("🎙️ Dub", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonNeon,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                val filteredEpisodes = uiState.episodes
                    .filter {
                        val matchesSearch = if (uiState.episodeSearchQuery.isBlank()) true
                        else it.title.contains(uiState.episodeSearchQuery, ignoreCase = true) ||
                             it.episodeNumber.toString() == uiState.episodeSearchQuery

                        val matchesAudio = when (episodeAudioFilter) {
                            "SUB" -> it.subtitles.isNotEmpty()
                            "DUB" -> it.audioTracks.any { aud -> aud.language != "ja" }
                            else -> true
                        }
                        matchesSearch && matchesAudio
                    }
                    .let { list ->
                        if (uiState.isEpisodeSortAsc) list.sortedBy { it.episodeNumber }
                        else list.sortedByDescending { it.episodeNumber }
                    }

                items(filteredEpisodes, key = { it.id }) { ep ->
                    EpisodeListItem(
                        episode = ep,
                        onPlay = { onPlayEpisode(anime.id, ep.episodeNumber) },
                        onDownload = {
                            scope.launch {
                                downloadsRepository?.startDownload(anime, ep)
                                snackbarHostState.showSnackbar("Downloading Ep ${ep.episodeNumber} for offline viewing...")
                            }
                        }
                    )
                }
            }

            // Tab 1: Overview & Cast
            if (uiState.selectedTab == 1) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Synopsis",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = anime.description,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Characters & Voice Cast",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        anime.characters.forEach { character ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedCharacter = character }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = character.avatarUrl,
                                    contentDescription = character.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = character.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = character.role, color = TextMuted, fontSize = 11.sp)
                                }
                                if (character.voiceActor.isNotBlank()) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = character.voiceActor, color = CrimsonNeon, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "VA (Tap for bio)", color = TextMuted, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 2: Reviews
            if (uiState.selectedTab == 2) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Community Reviews (${reviews.size})",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { viewModel.showReviewDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Write Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (reviews.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No reviews yet!", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Be the first to share your thoughts on this series.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(reviews, key = { it.id }) { review ->
                        ReviewItem(
                            review = review,
                            onLike = { viewModel.likeReview(review.id) }
                        )
                    }
                }
            }

            // Tab 3: Related & Recommendations
            if (uiState.selectedTab == 3) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "More Like This",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            uiState.recommendations.forEach { rec ->
                                AnimeCard(
                                    anime = rec,
                                    onClick = { onAnimeClick(rec) },
                                    cardWidth = 150,
                                    cardHeight = 220
                                )
                            }
                        }
                    }
                }
            }
        }

        // Review Submission Dialog
        if (uiState.showReviewDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showReviewDialog(false) },
                containerColor = SurfaceDark,
                title = { Text(text = "Rate & Review ${anime.titleEnglish}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Select your rating:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= uiState.userSelectedRating) StarAmber else Color.DarkGray,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { viewModel.setRating(star) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = uiState.reviewTextInput,
                            onValueChange = { viewModel.setReviewText(it) },
                            placeholder = { Text("What did you think of the animation, plot and characters?", color = TextMuted, fontSize = 12.sp) },
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonNeon,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.submitReview() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                    ) {
                        Text("Post Review")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showReviewDialog(false) }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        if (selectedCharacter != null) {
            CharacterDetailDialog(
                character = selectedCharacter!!,
                onDismiss = { selectedCharacter = null }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun EpisodeListItem(
    episode: Episode,
    onPlay: () -> Unit,
    onDownload: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onPlay)
            .testTag("episode_item_${episode.episodeNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(68.dp)
            ) {
                AsyncImage(
                    model = episode.thumbnail,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Episode ${episode.episodeNumber}",
                    color = CrimsonNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = episode.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${episode.durationSeconds / 60}m • ${episode.airDate}",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (onDownload != null) {
                IconButton(onClick = onDownload) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Episode",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewItem(
    review: Review,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = review.userAvatar,
                        contentDescription = review.userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = review.userName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    (1..review.rating).forEach {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarAmber, modifier = Modifier.size(13.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.content,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = onLike)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Like",
                        tint = if (review.hasLiked) CrimsonNeon else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${review.likesCount}",
                        color = if (review.hasLiked) CrimsonNeon else TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
