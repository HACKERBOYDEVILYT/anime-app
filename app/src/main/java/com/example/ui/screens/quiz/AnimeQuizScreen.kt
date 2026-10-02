package com.example.ui.screens.quiz

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Anime
import com.example.data.repository.AnimeRepository
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QuizOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val associatedGenres: List<String>
)

data class MatchedAnime(
    val anime: Anime,
    val matchScorePercent: Int,
    val reason: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeQuizScreen(
    animeRepository: AnimeRepository,
    onBack: () -> Unit,
    onAnimeClick: (String) -> Unit,
    onWatchEpisode: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) } // 1: Mood, 2: Length, 3: Audio/Format, 4: Results
    var selectedMood by remember { mutableStateOf<QuizOption?>(null) }
    var selectedLength by remember { mutableStateOf<String>("Quick 12-ep Binge") }
    var selectedAudioPref by remember { mutableStateOf<String>("Japanese with Bangla/English Sub") }

    var allAnime by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var matchedResults by remember { mutableStateOf<List<MatchedAnime>>(emptyList()) }

    LaunchedEffect(Unit) {
        val trending = animeRepository.getTrending()
        val popular = animeRepository.getPopular()
        val seasonal = animeRepository.getSeasonal()
        allAnime = (trending + popular + seasonal).distinctBy { it.id }
    }

    val moodOptions = listOf(
        QuizOption(
            id = "hyped",
            title = "Hyped & High Adrenaline",
            subtitle = "Epic fights, high stakes, god-tier animation",
            emoji = "🔥",
            associatedGenres = listOf("Action", "Fantasy", "Supernatural")
        ),
        QuizOption(
            id = "emotional",
            title = "Emotional & Deep Feels",
            subtitle = "Beautiful storytelling, nostalgic, tears guaranteed",
            emoji = "😭",
            associatedGenres = listOf("Drama", "Adventure", "Fantasy", "Slice of Life")
        ),
        QuizOption(
            id = "chill",
            title = "Chill & Cozy Vibe",
            subtitle = "Warm comedy, relaxing afternoons, wholesome",
            emoji = "☕",
            associatedGenres = listOf("Slice of Life", "Comedy")
        ),
        QuizOption(
            id = "mindblowing",
            title = "Mind-Bending & Psychological",
            subtitle = "Plot twists, deep mysteries, dark themes",
            emoji = "🧠",
            associatedGenres = listOf("Psychological", "Mystery", "Dark Fantasy", "Horror")
        ),
        QuizOption(
            id = "comedy",
            title = "Unstoppable Laughs",
            subtitle = "Parody, chaos, funny relatable antics",
            emoji = "😂",
            associatedGenres = listOf("Comedy", "Slice of Life")
        )
    )

    fun calculateMatches() {
        val targetGenres = selectedMood?.associatedGenres ?: emptyList()
        val matches = allAnime.map { anime ->
            val genreOverlap = anime.genres.count { it in targetGenres }
            val baseScore = 75 + (genreOverlap * 8) + (anime.rating * 3).toInt()
            val finalScore = baseScore.coerceIn(84, 99)
            val reason = when {
                genreOverlap >= 2 -> "Matches your exact mood with top ${anime.genres.take(2).joinToString(" & ")} elements"
                anime.rating >= 4.8f -> "Critically acclaimed masterpiece loved by fans"
                else -> "Great pick for your current pacing and theme"
            }
            MatchedAnime(anime, finalScore, reason)
        }.sortedByDescending { it.matchScorePercent }.take(5)

        matchedResults = matches
        step = 4
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CrimsonNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (step < 4) "Find My Anime Quiz" else "Your Perfect Matches",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (step > 1) step-- else onBack()
                        },
                        modifier = Modifier.testTag("quiz_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (step == 4) {
                        IconButton(onClick = { step = 1 }) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Retake Quiz",
                                tint = CrimsonNeon
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step Progress Bar
            if (step < 4) {
                LinearProgressIndicator(
                    progress = { step / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = CrimsonNeon,
                    trackColor = SurfaceVariantDark
                )
            }

            when (step) {
                1 -> {
                    // Step 1: Mood
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Question 1 of 3",
                            color = CrimsonNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "How do you want to feel right now?",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pick the vibe that matches your energy today.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(moodOptions) { mood ->
                                val isSelected = selectedMood?.id == mood.id
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMood = mood }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) CrimsonNeon else CardBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) SurfaceVariantDark else SurfaceDark
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = mood.emoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mood.title,
                                                color = TextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = mood.subtitle,
                                                color = TextMuted,
                                                fontSize = 12.sp
                                            )
                                        }
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(CrimsonNeon, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { step = 2 },
                            enabled = selectedMood != null,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Next Question", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> {
                    // Step 2: Time Available
                    val lengths = listOf(
                        Triple("Quick 12-ep Binge", "Finished in a weekend (12-13 episodes)", "⚡"),
                        Triple("Movie Experience", "Complete story in 2 hours (Feature Film)", "🍿"),
                        Triple("Long Epic Saga", "Deep world-building (24+ episodes)", "🛡️")
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Question 2 of 3",
                            color = CrimsonNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "How much time do you have?",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "We will tailor the episode length accordingly.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        lengths.forEach { (title, subtitle, emoji) ->
                            val isSelected = selectedLength == title
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { selectedLength = title }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) CrimsonNeon else CardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SurfaceVariantDark else SurfaceDark
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = emoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = subtitle,
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { step = 3 },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Next Question", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                3 -> {
                    // Step 3: Audio preference
                    val prefs = listOf(
                        Pair("Japanese with Bangla/English Sub", "🇯🇵 Original voice acting with localized subs"),
                        Pair("English / Bangla Dub", "🎙️ Listen in English or Bangla dub voiceover"),
                        Pair("Either is fine!", "✨ Both options work for me")
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Question 3 of 3",
                            color = CrimsonNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Dub or Sub?",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select your preferred audio experience.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        prefs.forEach { (title, subtitle) ->
                            val isSelected = selectedAudioPref == title
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { selectedAudioPref = title }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) CrimsonNeon else CardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SurfaceVariantDark else SurfaceDark
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = title,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = subtitle,
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { calculateMatches() },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Reveal My Anime Matches", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                4 -> {
                    // Step 4: Results
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CrimsonNeon.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceDark
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = selectedMood?.emoji ?: "🎯", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "Based on: ${selectedMood?.title}",
                                            color = CrimsonNeon,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "We found ${matchedResults.size} perfect titles matching your taste!",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        items(matchedResults, key = { it.anime.id }) { match ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                    .clickable { onAnimeClick(match.anime.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceDark
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(match.anime.posterUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = match.anime.titleEnglish,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 72.dp, height = 100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(CrimsonNeon.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${match.matchScorePercent}% MATCH",
                                                        color = CrimsonNeon,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFB300),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "${match.anime.rating}",
                                                        color = TextPrimary,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = match.anime.titleEnglish,
                                                color = TextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = match.anime.genres.joinToString(" • "),
                                                color = TextMuted,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = match.reason,
                                                color = TextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { onWatchEpisode(match.anime.id, 1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Start Watching Episode 1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
