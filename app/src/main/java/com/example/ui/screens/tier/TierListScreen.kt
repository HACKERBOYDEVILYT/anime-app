package com.example.ui.screens.tier

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Anime
import com.example.data.repository.TierListRepository
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierListScreen(
    tierListRepository: TierListRepository,
    onBack: () -> Unit,
    onAnimeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tierAssignments by tierListRepository.tierAssignments.collectAsStateWithLifecycle()
    var availableAnime by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var selectedAnimeForAssignment by remember { mutableStateOf<Anime?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        availableAnime = tierListRepository.getAvailableAnime()
        // Pre-seed some anime to tiers for instant satisfying preview
        if (tierAssignments.values.all { it.isEmpty() } && availableAnime.isNotEmpty()) {
            tierListRepository.assignAnimeToTier("S", availableAnime[0])
            if (availableAnime.size > 1) tierListRepository.assignAnimeToTier("A", availableAnime[1])
            if (availableAnime.size > 2) tierListRepository.assignAnimeToTier("S", availableAnime[2])
        }
    }

    val tiers = listOf(
        Pair("S", Color(0xFFFF4D4D)),
        Pair("A", Color(0xFFFF9933)),
        Pair("B", Color(0xFFFFD11A)),
        Pair("C", Color(0xFF66CC66)),
        Pair("D", Color(0xFF4D94FF))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = CrimsonNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Anime Tier List Maker",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tier_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { tierListRepository.resetTiers() }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Tiers", tint = TextMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tier Rows List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tiers) { (tierLabel, tierColor) ->
                    val assignedAnime = tierAssignments[tierLabel] ?: emptyList()

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceDark
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tier Tag Badge
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(90.dp)
                                    .background(tierColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tierLabel,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                )
                            }

                            // Horizontal Anime Cards in Tier
                            if (assignedAnime.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "Tap anime below to rank in $tierLabel Tier",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            } else {
                                LazyRow(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    items(assignedAnime, key = { it.id }) { anime ->
                                        Box(
                                            modifier = Modifier
                                                .size(width = 54.dp, height = 76.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    tierListRepository.removeAnimeFromTier(anime.id)
                                                    scope.launch { snackbarHostState.showSnackbar("Removed ${anime.titleEnglish} from $tierLabel Tier") }
                                                }
                                        ) {
                                            AsyncImage(
                                                model = anime.posterUrl,
                                                contentDescription = anime.titleEnglish,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Anime Bank at Bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceDark,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Anime Bank (Tap to Rank)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(availableAnime, key = { it.id }) { anime ->
                            Column(
                                modifier = Modifier
                                    .width(70.dp)
                                    .clickable { selectedAnimeForAssignment = anime },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = anime.posterUrl,
                                    contentDescription = anime.titleEnglish,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 70.dp, height = 98.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = anime.titleEnglish,
                                    color = TextPrimary,
                                    fontSize = 10.sp,
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

    if (selectedAnimeForAssignment != null) {
        val targetAnime = selectedAnimeForAssignment!!
        AlertDialog(
            onDismissRequest = { selectedAnimeForAssignment = null },
            title = { Text("Rank: ${targetAnime.titleEnglish}", color = TextPrimary, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Select a tier for this anime:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        tiers.forEach { (label, color) ->
                            Surface(
                                onClick = {
                                    tierListRepository.assignAnimeToTier(label, targetAnime)
                                    selectedAnimeForAssignment = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = color,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = label, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedAnimeForAssignment = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
