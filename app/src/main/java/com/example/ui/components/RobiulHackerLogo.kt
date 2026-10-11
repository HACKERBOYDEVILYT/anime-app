package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.repository.AdminRepository

const val ROBIUL_BUNNY_CDN_BASE_URL = "https://robiulislam.b-cdn.net"
const val ROBIUL_BUNNY_CDN_LOGO_URL = "https://robiulislam.b-cdn.net/images/logo.png"

val HackerMatrixGreen = Color(0xFF00FF66)
val HackerCyberCyan = Color(0xFF00E5FF)
val HackerTerminalDark = Color(0xFF070B10)

/**
 * Clean Logo Emblem with Bunny.net CDN logo (https://robiulislam.b-cdn.net/images/logo.png)
 * and clean cyber emblem fallback without overlaid RS / RS HACKER text.
 */
@Composable
fun RsHackerEmblem(
    size: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    val activeBunnyLogoUrl by AdminRepository.globalBunnyCdnLogoUrl.collectAsStateWithLifecycle()
    val infiniteTransition = rememberInfiniteTransition(label = "robiul_logo_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse_alpha"
    )

    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(HackerTerminalDark)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        HackerMatrixGreen.copy(alpha = pulseAlpha),
                        HackerCyberCyan.copy(alpha = pulseAlpha),
                        HackerMatrixGreen.copy(alpha = pulseAlpha)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Clean graphic logo emblem fallback (no RS / RS HACKER text)
        Image(
            painter = painterResource(id = R.drawable.img_robiul_logo_clean_1791390770574),
            contentDescription = "Robiul Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bunny.net CDN Official Logo (https://robiulislam.b-cdn.net/images/logo.png or custom Admin CDN logo)
        AsyncImage(
            model = activeBunnyLogoUrl.ifBlank { ROBIUL_BUNNY_CDN_LOGO_URL },
            contentDescription = "Robiul Bunny.net CDN Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle HUD corner accents only (no text overlay)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val bracketLen = w * 0.20f
            val strokeW = 1.4.dp.toPx()

            drawLine(
                color = HackerMatrixGreen,
                start = Offset(strokeW, strokeW),
                end = Offset(bracketLen, strokeW),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = HackerMatrixGreen,
                start = Offset(strokeW, strokeW),
                end = Offset(strokeW, bracketLen),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = HackerCyberCyan,
                start = Offset(w - strokeW, h - strokeW),
                end = Offset(w - bracketLen, h - strokeW),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = HackerCyberCyan,
                start = Offset(w - strokeW, h - strokeW),
                end = Offset(w - strokeW, h - bracketLen),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Clean "Robiul" Brand Header with Logo Emblem (without RS / RS_HACKER text).
 * Used in HomeScreen, HomeShimmerScreen, and top navigation headers.
 */
@Composable
fun RobiulBrandHeader(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier
    ) {
        RsHackerEmblem(size = 38.dp)

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "ROBIUL",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.6.sp,
            maxLines = 1
        )
    }
}
