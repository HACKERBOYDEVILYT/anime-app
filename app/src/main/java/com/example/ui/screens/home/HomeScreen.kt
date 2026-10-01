package com.example.ui.screens.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Anime
import com.example.ui.components.AnimeRow
import com.example.ui.components.ContinueWatchingRow
import com.example.ui.components.HeroCarousel
import com.example.ui.components.SecretAdminDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifsCount = notifications.count { !it.isRead }

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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CrimsonNeon)
            }
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
                        // Brand Logo & Title (Hidden Admin Trigger: tap 5 times)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .testTag("app_brand_logo")
                                .clickable {
                                    val now = System.currentTimeMillis()
                                    if (now - lastLogoTapTime < 800) {
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
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(CrimsonNeon, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "黒",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KURO",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "STREAM",
                                color = CrimsonNeon,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }

                        // Actions: Search, Notifications
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                        onAnimeClick = onAnimeClick
                    )
                }

                // Seasonal Anime
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "❄️ Seasonal Anime - Winter 2026",
                        animeList = uiState.seasonal,
                        onAnimeClick = onAnimeClick
                    )
                }

                // Top Rated Anime
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "⭐ Top Rated Anime",
                        animeList = uiState.topRated,
                        onAnimeClick = onAnimeClick
                    )
                }

                // Popular Anime
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "👑 Most Popular",
                        animeList = uiState.popular,
                        onAnimeClick = onAnimeClick
                    )
                }

                // Recently Added
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimeRow(
                        title = "✨ Recently Updated",
                        animeList = uiState.recentlyAdded,
                        onAnimeClick = onAnimeClick
                    )
                }
            }
        }
    }
}
