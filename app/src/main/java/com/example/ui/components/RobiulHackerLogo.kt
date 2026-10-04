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
import com.example.R

val HackerMatrixGreen = Color(0xFF00FF66)
val HackerCyberCyan = Color(0xFF00E5FF)
val HackerTerminalDark = Color(0xFF070B10)

/**
 * Hacker-style "RS" Logo Emblem with generated cyber-hacker artwork,
 * glowing Matrix Green (#00FF66) & Cyber Cyan (#00E5FF) shield frame,
 * digital HUD corner brackets, and high-contrast "RS" hacker monogram.
 */
@Composable
fun RsHackerEmblem(
    size: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rs_hacker_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rs_pulse_alpha"
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
        // Generated Hacker RS Logo Background Art
        Image(
            painter = painterResource(id = R.drawable.img_rs_hacker_logo_1791100219170),
            contentDescription = "Robiul RS Hacker Logo",
            contentScale = ContentScale.Crop,
            alpha = 0.45f,
            modifier = Modifier.fillMaxSize()
        )

        // Dark cyber vignette + HUD corner brackets & hood shield vector
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val bracketLen = w * 0.22f
            val strokeW = 1.6.dp.toPx()

            // Top-left & bottom-right HUD terminal corner brackets
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

            // Subtle hacker hood / shield crest silhouette at top
            val crestPath = Path().apply {
                moveTo(w * 0.5f, h * 0.09f)
                lineTo(w * 0.84f, h * 0.24f)
                lineTo(w * 0.84f, h * 0.68f)
                lineTo(w * 0.5f, h * 0.91f)
                lineTo(w * 0.16f, h * 0.68f)
                lineTo(w * 0.16f, h * 0.24f)
                close()
            }
            drawPath(
                path = crestPath,
                color = HackerMatrixGreen.copy(alpha = 0.28f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Bold Hacker Monogram "RS" in the center
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "R",
                color = HackerMatrixGreen,
                fontSize = (size.value * 0.44f).sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "S",
                color = HackerCyberCyan,
                fontSize = (size.value * 0.44f).sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

/**
 * Full "Robiul" Brand Header with the Hacker-style "RS" Logo Emblem.
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

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ROBIUL",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.8.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = HackerMatrixGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = HackerMatrixGreen.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(5.dp)
                    )
                ) {
                    Text(
                        text = "[RS]",
                        color = HackerMatrixGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = ">_ RS_HACKER // ROOT",
                color = HackerMatrixGreen.copy(alpha = 0.85f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
        }
    }
}
