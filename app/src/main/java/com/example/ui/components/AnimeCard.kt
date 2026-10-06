package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.data.model.Anime
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun AnimeCard(
    anime: Anime,
    onClick: () -> Unit,
    onFavoriteToggle: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier,
    cardWidth: Int = 150,
    cardHeight: Int = 220
) {
    var isPosterLoading by remember(anime.posterUrl) { mutableStateOf(true) }
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .clickable(onClick = onClick)
            .testTag("anime_card_${anime.id}")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .width(cardWidth.dp)
                .height(cardHeight.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isPosterLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(shimmerBrush)
                    )
                }

                AsyncImage(
                    model = anime.posterUrl,
                    contentDescription = anime.titleEnglish,
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        isPosterLoading = state is AsyncImagePainter.State.Loading
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Cinematic gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top badges: Rating & Favorite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBadge(rating = anime.rating)

                    if (onFavoriteToggle != null) {
                        IconButton(
                            onClick = onFavoriteToggle,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) CrimsonNeon else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Bottom badges on image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QualityBadge(label = "HD")
                        Spacer(modifier = Modifier.width(4.dp))
                        SubDubBadges()
                    }
                    Text(
                        text = "${anime.episodesCount} Eps • ${anime.releaseYear}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Title and Japanese Subtitle below card
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = anime.titleEnglish,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (anime.titleJapanese.isNotBlank()) {
            Text(
                text = anime.titleJapanese,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AnimeGridCard(
    anime: Anime,
    onClick: () -> Unit,
    onFavoriteToggle: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimeCard(
        anime = anime,
        onClick = onClick,
        onFavoriteToggle = onFavoriteToggle,
        isFavorite = isFavorite,
        modifier = modifier.fillMaxWidth(),
        cardWidth = 158,
        cardHeight = 224
    )
}

