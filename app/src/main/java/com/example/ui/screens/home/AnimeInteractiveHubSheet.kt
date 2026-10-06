package com.example.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Anime
import com.example.data.repository.GamificationAndSocialRepository
import com.example.data.repository.UserRepository
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeInteractiveHubDialog(
    initialTabIndex: Int = 0,
    catalog: List<Anime>,
    gamificationRepository: GamificationAndSocialRepository,
    userRepository: UserRepository?,
    onDismiss: () -> Unit,
    onAnimeClick: (Anime) -> Unit,
    onPlayEpisode: (String, Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTabIndex) }
    val tabs = listOf(
        "🎭 Characters",
        "🏢 Studios & Staff",
        "🎬 Trailers & Clips",
        "📰 Anime News",
        "🗳️ Polls & ⚔️ Battles",
        "🎡 Roulette & Picker",
        "🖼️ Wallpapers",
        "🎁 Events & 🏆 Ranks",
        "🎮 Mini Games & Trivia",
        "📱 Widgets & Links"
    )

    val characters by gamificationRepository.characters.collectAsStateWithLifecycle()
    val polls by gamificationRepository.communityPolls.collectAsStateWithLifecycle()
    val battles by gamificationRepository.animeBattles.collectAsStateWithLifecycle()
    val wallpapers by gamificationRepository.wallpapers.collectAsStateWithLifecycle()
    val seasonalEvents by gamificationRepository.seasonalEvents.collectAsStateWithLifecycle()

    var charSearchQuery by remember { mutableStateOf("") }
    var favCharactersOnly by remember { mutableStateOf(false) }
    var selectedClipFilter by remember { mutableStateOf("All") }
    var selectedNewsFilter by remember { mutableStateOf("All") }
    var selectedWallpaperFilter by remember { mutableStateOf("All") }
    var selectedLeaderboardCategory by remember { mutableStateOf("TOP_WATCHERS") }
    var randomGenreFilter by remember { mutableStateOf("All") }
    var pickedRandomAnime by remember { mutableStateOf(catalog.firstOrNull()) }
    var rouletteSpinCount by remember { mutableIntStateOf(0) }
    var rouletteWinner by remember { mutableStateOf(catalog.lastOrNull()) }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }

    val wheelRotation by animateFloatAsState(
        targetValue = rouletteSpinCount * 1080f,
        animationSpec = tween(durationMillis = 900),
        label = "roulette_wheel_spin"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💎 Robiul Anime Universe Hub",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Characters • Studios • Clips • News • Polls • Battles • Roulette • Trivia",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Hub", tint = TextPrimary)
                    }
                }

                if (feedbackBanner != null) {
                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.16f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = feedbackBanner.orEmpty(),
                                color = EmeraldSuccess,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Dismiss",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { feedbackBanner = null }
                            )
                        }
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceDark,
                    contentColor = CrimsonNeon,
                    edgePadding = 12.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) CrimsonNeon else TextSecondary
                                )
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        // 1. CHARACTER DATABASE
                        0 -> {
                            item {
                                OutlinedTextField(
                                    value = charSearchQuery,
                                    onValueChange = { charSearchQuery = it },
                                    placeholder = { Text("Search characters, voice actors, or anime...", color = TextMuted, fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CrimsonNeon,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !favCharactersOnly,
                                        onClick = { favCharactersOnly = false },
                                        label = { Text("All Characters (${characters.size})") }
                                    )
                                    FilterChip(
                                        selected = favCharactersOnly,
                                        onClick = { favCharactersOnly = true },
                                        label = { Text("❤️ Favorites (${characters.count { it.isFavorite }})") }
                                    )
                                }
                            }
                            val filteredChars = characters.filter { ch ->
                                val favMatch = !favCharactersOnly || ch.isFavorite
                                val q = charSearchQuery.trim().lowercase()
                                val qMatch = q.isEmpty() ||
                                    ch.name.lowercase().contains(q) ||
                                    ch.japaneseName.lowercase().contains(q) ||
                                    ch.voiceActor.lowercase().contains(q) ||
                                    ch.animeTitle.lowercase().contains(q)
                                favMatch && qMatch
                            }
                            items(filteredChars, key = { it.id }) { ch ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AsyncImage(
                                                model = ch.avatarUrl,
                                                contentDescription = ch.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, CrimsonNeon, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${ch.name} (${ch.japaneseName})", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                Text("${ch.animeTitle} • ${ch.role}", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Text("🎙️ VA: ${ch.voiceActor}", color = StarAmber, fontSize = 11.sp)
                                            }
                                            IconButton(
                                                onClick = {
                                                    gamificationRepository.toggleFavoriteCharacter(ch.id)
                                                    userRepository?.awardUserXp(5, "Favorited character ${ch.name}")
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (ch.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Favorite Character",
                                                    tint = if (ch.isFavorite) CrimsonNeon else TextSecondary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(ch.bio, color = TextSecondary, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Character Relationships:", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            ch.relationships.forEach { rel ->
                                                Surface(
                                                    color = SurfaceVariantDark,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = rel,
                                                        color = TextSecondary,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. STUDIO & STAFF PAGES
                        1 -> {
                            items(gamificationRepository.studiosAndStaff, key = { it.id }) { studio ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AsyncImage(
                                                model = studio.logoUrl,
                                                contentDescription = studio.studioName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("🏢 ${studio.studioName}", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                                Text("Founded ${studio.foundedYear} • ${studio.headquarters}", color = TextSecondary, fontSize = 12.sp)
                                            }
                                            Surface(color = StarAmber.copy(alpha = 0.16f), shape = RoundedCornerShape(8.dp)) {
                                                Text(
                                                    text = "⭐ ${studio.averageScore}",
                                                    color = StarAmber,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("🎬 Director: ${studio.director}", color = TextPrimary, fontSize = 12.sp)
                                        Text("✍️ Series Composition / Writer: ${studio.headWriter}", color = TextPrimary, fontSize = 12.sp)
                                        Text("✨ Lead Key Animator: ${studio.leadAnimator}", color = CyanAccent, fontSize = 12.sp)
                                        Text("🎙️ Featured Voice Actors: ${studio.featuredVoiceActors.joinToString(", ")}", color = TextSecondary, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Notable Works:", color = StarAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            studio.notableWorks.forEach { work ->
                                                Surface(color = CrimsonNeon.copy(alpha = 0.14f), shape = RoundedCornerShape(6.dp)) {
                                                    Text(work, color = CrimsonNeon, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. ANIME TRAILERS & CLIPS
                        2 -> {
                            item {
                                val clipCategories = listOf("All", "Trailer", "Teaser", "Opening", "Ending", "Character PV", "News Clip")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(clipCategories) { cat ->
                                        FilterChip(
                                            selected = selectedClipFilter == cat,
                                            onClick = { selectedClipFilter = cat },
                                            label = { Text(cat, fontSize = 12.sp) }
                                        )
                                    }
                                }
                            }
                            val clips = gamificationRepository.animeClips.filter {
                                selectedClipFilter == "All" || it.category.equals(selectedClipFilter, ignoreCase = true)
                            }
                            items(clips, key = { it.id }) { clip ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            onDismiss()
                                            onPlayEpisode(clip.animeId, 1)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(width = 110.dp, height = 66.dp)) {
                                            AsyncImage(
                                                model = clip.thumbnailUrl,
                                                contentDescription = clip.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.7f),
                                                shape = CircleShape,
                                                modifier = Modifier.align(Alignment.Center)
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play Clip", tint = CrimsonNeon, modifier = Modifier.padding(4.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Surface(color = CyanAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                Text(clip.category, color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(clip.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${clip.animeTitle} • ${clip.durationLabel} • ${clip.viewsLabel}", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // 4. ANIME NEWS CENTER
                        3 -> {
                            item {
                                val newsCats = listOf("All", "New Anime", "Season Announcement", "Release News", "Studio News", "Movie Announcement")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(newsCats) { cat ->
                                        FilterChip(
                                            selected = selectedNewsFilter == cat,
                                            onClick = { selectedNewsFilter = cat },
                                            label = { Text(cat, fontSize = 12.sp) }
                                        )
                                    }
                                }
                            }
                            val filteredNews = gamificationRepository.animeNews.filter {
                                selectedNewsFilter == "All" || it.category.equals(selectedNewsFilter, ignoreCase = true)
                            }
                            items(filteredNews, key = { it.id }) { news ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Surface(color = CrimsonNeon.copy(alpha = 0.16f), shape = RoundedCornerShape(6.dp)) {
                                                Text(news.category, color = CrimsonNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                            }
                                            Text(news.publishedTime, color = TextMuted, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(news.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(news.summary, color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // 5. COMMUNITY POLLS & ANIME BATTLES
                        4 -> {
                            item {
                                Text("⚔️ Live Anime Battles (Tap fighter to vote)", color = StarAmber, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            items(battles, key = { it.id }) { battle ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(battle.title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    gamificationRepository.voteInBattle(battle.id, "LEFT")
                                                    userRepository?.awardUserXp(15, "Voted in ${battle.leftFighterName} vs ${battle.rightFighterName}")
                                                    feedbackBanner = "⚔️ Voted for ${battle.leftFighterName}! (+15 XP)"
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("${battle.leftFighterName} (${battle.leftPercent}%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Text(" 🆚 ", color = StarAmber, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                            Button(
                                                onClick = {
                                                    gamificationRepository.voteInBattle(battle.id, "RIGHT")
                                                    userRepository?.awardUserXp(15, "Voted in ${battle.leftFighterName} vs ${battle.rightFighterName}")
                                                    feedbackBanner = "⚔️ Voted for ${battle.rightFighterName}! (+15 XP)"
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("${battle.rightFighterName} (${battle.rightPercent}%)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { battle.leftPercent / 100f },
                                            color = CrimsonNeon,
                                            trackColor = CyanAccent,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${battle.totalVotes} total community votes", color = TextMuted, fontSize = 11.sp)
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("🗳️ Community Polls (Weekly & Monthly)", color = CyanAccent, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            items(polls, key = { it.id }) { poll ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(poll.category, color = CrimsonNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(poll.question, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        poll.options.forEach { opt ->
                                            val pct = ((opt.votes * 100f) / poll.totalVotes).toInt()
                                            val isSelected = poll.selectedOptionId == opt.id
                                            Surface(
                                                color = if (isSelected) CrimsonNeon.copy(alpha = 0.22f) else SurfaceVariantDark,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        gamificationRepository.voteInPoll(poll.id, opt.id)
                                                        userRepository?.awardUserXp(10, "Voted in Community Poll")
                                                        feedbackBanner = "🗳️ Poll vote recorded for '${opt.label}' (+10 XP)"
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(opt.label, color = TextPrimary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                    Text("$pct% (${opt.votes})", color = StarAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. RANDOM ANIME PICKER & ANIME ROULETTE
                        5 -> {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CrimsonNeon.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🎡 Anime Roulette Wheel", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("“What should I watch tonight?”", color = TextSecondary, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .rotate(wheelRotation)
                                                .background(CrimsonNeon.copy(alpha = 0.18f), CircleShape)
                                                .border(3.dp, StarAmber, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎡", fontSize = 44.sp)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        rouletteWinner?.let { winner ->
                                            Text("Tonight's Spin Winner: ${winner.titleEnglish}", color = StarAmber, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text("${winner.studio} • ⭐ ${winner.rating} • ${winner.episodesCount} Eps", color = TextSecondary, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Button(
                                                    onClick = {
                                                        rouletteSpinCount++
                                                        if (catalog.isNotEmpty()) {
                                                            rouletteWinner = catalog.random()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                                                ) {
                                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Spin Wheel")
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onDismiss()
                                                        onAnimeClick(winner)
                                                    }
                                                ) {
                                                    Text("Watch Now", color = CyanAccent)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("🎰 Smart Random Anime Picker", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Text("Select a genre filter and get an instant recommendation:", color = TextSecondary, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val genres = listOf("All", "Action", "Fantasy", "Adventure", "Sci-Fi", "Comedy", "Supernatural")
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(genres) { g ->
                                                FilterChip(
                                                    selected = randomGenreFilter == g,
                                                    onClick = {
                                                        randomGenreFilter = g
                                                        val pool = if (g == "All") catalog else catalog.filter { a -> a.genres.any { it.equals(g, true) } }.ifEmpty { catalog }
                                                        pickedRandomAnime = pool.randomOrNull()
                                                    },
                                                    label = { Text(g) }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        pickedRandomAnime?.let { picked ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(
                                                    model = picked.posterUrl,
                                                    contentDescription = picked.titleEnglish,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(width = 70.dp, height = 96.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(picked.titleEnglish, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                                    Text(picked.genres.joinToString(" • "), color = CyanAccent, fontSize = 11.sp)
                                                    Text(picked.synopsis, color = TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = {
                                                                val pool = if (randomGenreFilter == "All") catalog else catalog.filter { a -> a.genres.any { it.equals(randomGenreFilter, true) } }.ifEmpty { catalog }
                                                                pickedRandomAnime = pool.randomOrNull()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
                                                        ) {
                                                            Text("Pick Another", fontSize = 11.sp)
                                                        }
                                                        Button(
                                                            onClick = {
                                                                onDismiss()
                                                                onAnimeClick(picked)
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                                                        ) {
                                                            Text("Open Anime", fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 7. WALLPAPER CENTER
                        6 -> {
                            item {
                                val wpCats = listOf("All", "Lock Screen", "Home Screen", "Character Wallpaper")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(wpCats) { cat ->
                                        FilterChip(
                                            selected = selectedWallpaperFilter == cat,
                                            onClick = { selectedWallpaperFilter = cat },
                                            label = { Text(cat) }
                                        )
                                    }
                                }
                            }
                            val filteredWallpapers = wallpapers.filter {
                                selectedWallpaperFilter == "All" || it.category.equals(selectedWallpaperFilter, ignoreCase = true)
                            }
                            items(filteredWallpapers, key = { it.id }) { wp ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = wp.imageUrl,
                                            contentDescription = wp.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 80.dp, height = 115.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Surface(color = CrimsonNeon.copy(alpha = 0.16f), shape = RoundedCornerShape(6.dp)) {
                                                Text(wp.category, color = CrimsonNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(wp.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text("${wp.animeTitle} • ${wp.resolution}", color = TextSecondary, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        feedbackBanner = "🖼️ Applied '${wp.title}' as ${wp.category}!"
                                                        userRepository?.awardUserXp(10, "Downloaded 4K Anime Wallpaper")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                                                ) {
                                                    Text("Set ${wp.category}", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 8. SEASONAL EVENTS & GLOBAL LEADERBOARDS
                        7 -> {
                            item {
                                Text("🎁 Active Seasonal Events (Bonus XP & Badges)", color = StarAmber, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            items(seasonalEvents, key = { it.id }) { evt ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(evt.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text("${evt.xpMultiplier}x XP", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Text(evt.subtitle, color = TextSecondary, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("🎖️ Event Badge: ${evt.specialBadgeTitle} (${evt.currentPoints}/${evt.targetPoints} pts • ${evt.activePeriod})", color = CyanAccent, fontSize = 11.sp)
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("🏆 Global Leaderboards", color = CyanAccent, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                val lbCats = listOf("TOP_WATCHERS", "TOP_REVIEWERS", "TOP_CONTRIBUTORS", "WEEKLY_XP", "MONTHLY_XP")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(lbCats) { cat ->
                                        FilterChip(
                                            selected = selectedLeaderboardCategory == cat,
                                            onClick = { selectedLeaderboardCategory = cat },
                                            label = { Text(cat.replace("_", " "), fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                            val entries = gamificationRepository.leaderboards.filter { it.category == selectedLeaderboardCategory }
                            items(entries, key = { "${it.category}_${it.rank}_${it.userId}" }) { entry ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#${entry.rank}", color = StarAmber, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(36.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${entry.username} • ${entry.titleRank} (Lv.${entry.level})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(entry.metricValue, color = CyanAccent, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // 9. DAILY ANIME TRIVIA & MINI GAMES
                        8 -> {
                            items(gamificationRepository.triviaAndMiniGames, key = { it.id }) { q ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("🎮 ${q.category}", color = CrimsonNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("+${q.xpReward} XP", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(q.promptText, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Clue: ${q.hintOrEmoji}", color = StarAmber, fontSize = 18.sp, modifier = Modifier.padding(vertical = 6.dp))
                                        q.options.forEachIndexed { idx, opt ->
                                            Surface(
                                                color = SurfaceVariantDark,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clickable {
                                                        if (idx == q.correctIndex) {
                                                            userRepository?.awardUserXp(q.xpReward, "Won ${q.category}")
                                                            feedbackBanner = "🎉 Correct! '$opt' — Awarded +${q.xpReward} XP!"
                                                        } else {
                                                            feedbackBanner = "❌ Not quite! Try another option."
                                                        }
                                                    }
                                            ) {
                                                Text(
                                                    text = opt,
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 10. HOME SCREEN WIDGETS & DEEP LINKS PREVIEW
                        9 -> {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("📱 Android Home Screen Widgets (4 Active Widgets)", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("Pin these interactive widgets to your Android launcher:", color = TextSecondary, fontSize = 12.sp)
                                        Surface(color = SurfaceVariantDark, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("1️⃣ Continue Watching Widget (4x2)", color = CrimsonNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Frieren: Beyond Journey's End • Ep 7 — 04:32 remaining", color = TextPrimary, fontSize = 12.sp)
                                            }
                                        }
                                        Surface(color = SurfaceVariantDark, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("2️⃣ Next Episode Countdown Widget (4x1)", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Solo Leveling Episode 13 airs in 02 : 14 : 36", color = TextPrimary, fontSize = 12.sp)
                                            }
                                        }
                                        Surface(color = SurfaceVariantDark, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("3️⃣ Daily Anime Pick Widget (2x2)", color = StarAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Jujutsu Kaisen • ⭐ 4.9 • 1080p Simulcast", color = TextPrimary, fontSize = 12.sp)
                                            }
                                        }
                                        Surface(color = SurfaceVariantDark, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("4️⃣ Watch Streak Flame Widget (2x1)", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("🔥 12 Day Watch Streak Active — Watch 1 ep today to keep it alive!", color = TextPrimary, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("🔗 Deep Links Router (`robiul://anime/{id}`)", color = CyanAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("Tap any shared deep link below to jump straight to that anime page:", color = TextSecondary, fontSize = 12.sp)
                                        catalog.take(4).forEach { anime ->
                                            OutlinedButton(
                                                onClick = {
                                                    onDismiss()
                                                    onAnimeClick(anime)
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("🔗 https://robiul.anime/watch/${anime.id} (${anime.titleEnglish})", color = TextPrimary, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
