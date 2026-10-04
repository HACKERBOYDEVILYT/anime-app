package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary

/**
 * Creates an animated diagonal shimmer brush tailored for the dark-neon KuroStream aesthetic.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val shimmerColors = listOf(
        SurfaceDark,
        SurfaceVariantDark.copy(alpha = 0.95f),
        CrimsonNeon.copy(alpha = 0.14f),
        SurfaceVariantDark.copy(alpha = 0.95f),
        SurfaceDark
    )

    val transition = rememberInfiniteTransition(label = "kuro_shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1250,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "kuro_shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 500f, y = translateAnim - 500f),
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

/**
 * Reusable Modifier extension that applies a rounded shimmer skeleton effect.
 */
fun Modifier.shimmerEffect(cornerRadius: Dp = 8.dp): Modifier = composed {
    val brush = rememberShimmerBrush()
    this
        .clip(RoundedCornerShape(cornerRadius))
        .background(brush)
}

/**
 * Skeleton placeholder matching [AnimeCard] dimensions and visual hierarchy.
 */
@Composable
fun AnimeCardSkeleton(
    modifier: Modifier = Modifier,
    cardWidth: Int = 150,
    cardHeight: Int = 220
) {
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .testTag("anime_card_skeleton")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .width(cardWidth.dp)
                .height(cardHeight.dp)
                .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(shimmerBrush)
            ) {
                // Top badges placeholder (Rating & Favorite icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                }

                // Bottom badges placeholder (HD, SUB/DUB, Episode count)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(15.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.09f))
                        )
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(15.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.09f))
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(76.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                }
            }
        }

        // Title and Subtitle skeleton bars below poster
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .height(13.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
    }
}

/**
 * Horizontal row of [AnimeCardSkeleton] cards matching [AnimeRow].
 */
@Composable
fun AnimeRowSkeleton(
    itemCount: Int = 5,
    showHeader: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("anime_row_skeleton")
    ) {
        if (showHeader) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(155.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmerBrush)
                )
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(itemCount) {
                AnimeCardSkeleton()
            }
        }
    }
}

/**
 * Hero Banner skeleton placeholder matching [HeroCarousel].
 */
@Composable
fun HeroCarouselSkeleton(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(410.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(shimmerBrush)
            .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("hero_carousel_skeleton")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Featured spotlight pill placeholder
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.09f))
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Hero Anime Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Metadata row placeholder
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(58.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons placeholder (Watch Now + Details)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                )
            }
        }
    }
}

/**
 * Full-screen shimmer skeleton layout for [com.example.ui.screens.home.HomeScreen] while fetching initial catalog data.
 */
@Composable
fun HomeShimmerScreen(
    onLogoTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shimmerBrush = rememberShimmerBrush()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("home_shimmer_screen"),
        contentPadding = PaddingValues(bottom = 90.dp),
        userScrollEnabled = false
    ) {
        // Keep the top Brand Header interactive so 5-tap ROBIUL [RS] always works
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RobiulBrandHeader(
                    modifier = Modifier.testTag("app_brand_logo"),
                    onClick = { onLogoTap() }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(shimmerBrush)
                        )
                    }
                }
            }
        }

        // Quick Navigation Hub Skeleton
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }

        // Hero Banner Skeleton
        item {
            HeroCarouselSkeleton()
        }

        // Genre Chips Skeleton Row
        item {
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(6) {
                    Box(
                        modifier = Modifier
                            .width(84.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }

        // Multiple Anime Card List Row Skeletons
        items(3) {
            Spacer(modifier = Modifier.height(18.dp))
            AnimeRowSkeleton(itemCount = 5)
        }
    }
}

/**
 * Grid of [AnimeCardSkeleton] placeholders for [com.example.ui.screens.search.SearchScreen] while searching APIs.
 */
@Composable
fun SearchGridSkeleton(
    itemCount: Int = 8,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false,
        modifier = modifier
            .fillMaxSize()
            .testTag("search_grid_skeleton")
    ) {
        items(itemCount) {
            AnimeCardSkeleton(
                cardWidth = 160,
                cardHeight = 230
            )
        }
    }
}

/**
 * Episode row skeleton placeholder for [com.example.ui.screens.details.AnimeDetailsScreen].
 */
@Composable
fun EpisodeRowSkeleton(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, CardBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(110.dp, 64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(shimmerBrush)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

/**
 * Full-screen skeleton placeholder for [com.example.ui.screens.details.AnimeDetailsScreen].
 */
@Composable
fun AnimeDetailsSkeleton(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .testTag("anime_details_skeleton"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner + Poster Header Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(shimmerBrush)
        )

        // Title & Studio Lines
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(shimmerBrush)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(shimmerBrush)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(shimmerBrush)
            )
        }

        // Episode List Skeletons
        repeat(4) {
            EpisodeRowSkeleton()
        }
    }
}
