package com.gows.animwatch.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.gows.animwatch.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size as CoilSize
import com.gows.animwatch.data.model.AnimeArtTheme
import com.gows.animwatch.data.model.ArtMotif
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldDeep
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.MediaScrimDark
import com.gows.animwatch.ui.theme.MediaTextLight
import com.gows.animwatch.ui.theme.ObsidianBase
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimWatchCrestLogo(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.animwatch_logo),
        contentDescription = "Logo AnimWatch",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
    )
}

private val PosterVignetteBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color.Transparent,
        0.58f to Color.Transparent,
        1.0f to MediaScrimDark.copy(alpha = 0.88f)
    )
)

@Composable
fun AnimePosterArtwork(
    artTheme: AnimeArtTheme,
    title: String,
    modifier: Modifier = Modifier,
    posterUrl: String = "",
    showKanjiWatermark: Boolean = true,
    showVignette: Boolean = true,
    decodeSizePx: Int = 420
) {
    val context = LocalContext.current
    val imageLoadFailedState = androidx.compose.runtime.remember(posterUrl) {
        androidx.compose.runtime.mutableStateOf(false)
    }
    Box(
        modifier = modifier.background(artTheme.secondaryBg)
    ) {
        if (posterUrl.isNotBlank() && !imageLoadFailedState.value) {
            // Fast path: skip heavy Canvas overdraw when loading real remote poster
            val request = remember(posterUrl, decodeSizePx) {
                ImageRequest.Builder(context)
                    .data(posterUrl)
                    .size(CoilSize(decodeSizePx, decodeSizePx))
                    .precision(Precision.INEXACT)
                    .crossfade(160)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onError = { imageLoadFailedState.value = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Procedural fallback artwork only when offline / no remote URL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(artTheme.primaryBg, artTheme.secondaryBg, MediaScrimDark)
                        )
                    )
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArtMotif(
                    motif = artTheme.motif,
                    glow = artTheme.accentGlow,
                    phase = 0f,
                    isWide = size.width > size.height * 1.15f
                )
            }
            if (showKanjiWatermark) {
                Text(
                    text = artTheme.kanjiCrest,
                    color = artTheme.accentGlow.copy(alpha = 0.14f),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                )
            }
        }

        if (showVignette) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PosterVignetteBrush)
            )
        }
    }
}

@Composable
fun AnimatedStreamingSceneCanvas(
    artTheme: AnimeArtTheme,
    isPlaying: Boolean,
    positionSeconds: Int,
    playbackSpeed: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stream_scene")
    val ambientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (6500 / playbackSpeed).toInt().coerceIn(2200, 12000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_phase"
    )

    val effectivePhase = if (isPlaying) {
        (ambientPhase + (positionSeconds % 30) / 30f) % 1f
    } else {
        (positionSeconds % 30) / 30f
    }

    Box(
        modifier = modifier.background(
            Brush.radialGradient(
                colors = listOf(artTheme.primaryBg, artTheme.secondaryBg, MediaScrimDark),
                radius = 900f
            )
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cinematic horizontal camera pan accent lines
            val barCount = 14
            for (i in 0 until barCount) {
                val seed = (i * 0.173f + effectivePhase) % 1f
                val y = ((i + 0.5f) / barCount) * h
                val xStart = (1f - seed) * w * 1.2f - w * 0.2f
                val lineLen = 40f + (i % 4) * 38f
                drawLine(
                    color = artTheme.accentGlow.copy(alpha = if (isPlaying) 0.14f else 0.05f),
                    start = Offset(xStart, y),
                    end = Offset(xStart + lineLen, y),
                    strokeWidth = if (i % 3 == 0) 2.5f else 1.2f,
                    cap = StrokeCap.Round
                )
            }

            // Main motif animated gently
            drawArtMotif(
                motif = artTheme.motif,
                glow = artTheme.accentGlow,
                phase = effectivePhase,
                isWide = true
            )

            // Floating golden dust / energy motes
            for (p in 0 until 18) {
                val px = ((p * 0.237f + effectivePhase * 0.4f) % 1f) * w
                val py = ((p * 0.413f - effectivePhase * 0.25f + 1f) % 1f) * h
                val radius = 1.6f + (p % 3) * 1.3f
                drawCircle(
                    color = GoldBright.copy(alpha = 0.30f + (p % 4) * 0.10f),
                    radius = radius,
                    center = Offset(px, py)
                )
            }
        }

        // Subtle widescreen letterbox feel & dark contrast edge
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MediaScrimDark.copy(alpha = 0.35f),
                        0.2f to Color.Transparent,
                        0.8f to Color.Transparent,
                        1f to MediaScrimDark.copy(alpha = 0.55f)
                    )
                )
        )

        // CRT Scanline texture across live video viewport
        CrtScanlineOverlay(
            modifier = Modifier.fillMaxSize(),
            lineAlpha = 0.10f,
            stepPx = 5f
        )

        // Soft ambient broadcast watermark
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LedStatusDot(
                color = if (isPlaying) GoldBright else MediaTextLight.copy(alpha = 0.35f),
                pulsing = isPlaying,
                size = 6.dp
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "AnimWatch",
                color = MediaTextLight.copy(alpha = 0.38f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

private fun DrawScope.drawArtMotif(
    motif: ArtMotif,
    glow: Color,
    phase: Float,
    isWide: Boolean
) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return
    val cx = w * 0.5f
    val cy = h * if (isWide) 0.46f else 0.40f
    val minDim = minOf(w, h)
    val pulse = 1f + 0.05f * sin(phase * 2f * PI.toFloat())
    val haloRadius = (minDim * 0.68f * pulse).coerceAtLeast(1f)

    // Ambient central radial halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glow.copy(alpha = 0.26f),
                glow.copy(alpha = 0.06f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = haloRadius
        ),
        radius = haloRadius,
        center = Offset(cx, cy)
    )

    when (motif) {
        ArtMotif.KATANA_SLASH -> {
            // Sweeping crimson/gold slash arcs
            for (i in 0..2) {
                val offset = i * 16f
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, glow, GoldBright, Color.Transparent),
                        start = Offset(w * 0.1f, h * 0.72f - offset),
                        end = Offset(w * 0.92f, h * 0.22f - offset)
                    ),
                    start = Offset(w * 0.1f, h * 0.72f - offset),
                    end = Offset(w * 0.92f, h * 0.22f - offset),
                    strokeWidth = (4.5f - i * 1.1f),
                    cap = StrokeCap.Round
                )
            }
            drawCircle(
                color = glow.copy(alpha = 0.35f),
                radius = minDim * 0.22f * pulse,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )
        }

        ArtMotif.MAGIC_RING -> {
            val r = minDim * 0.30f * pulse
            rotate(degrees = phase * 360f, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = glow.copy(alpha = 0.45f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.2f)
                )
                drawCircle(
                    color = glow.copy(alpha = 0.22f),
                    radius = r * 0.72f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.4f)
                )
                val starPoints = 6
                for (i in 0 until starPoints) {
                    val a1 = (i * 60f) * (PI / 180f).toFloat()
                    val a2 = ((i + 2) * 60f) * (PI / 180f).toFloat()
                    drawLine(
                        color = glow.copy(alpha = 0.38f),
                        start = Offset(cx + cos(a1) * r, cy + sin(a1) * r),
                        end = Offset(cx + cos(a2) * r, cy + sin(a2) * r),
                        strokeWidth = 1.5f
                    )
                }
            }
        }

        ArtMotif.CROWN_MONOLITH -> {
            // Obsidian monolith pillars + hovering royal gold crown arc
            val pillarW = w * 0.12f
            drawRect(
                brush = Brush.verticalGradient(listOf(glow.copy(alpha = 0.22f), Color.Transparent)),
                topLeft = Offset(cx - pillarW * 1.4f, h * 0.28f),
                size = Size(pillarW * 0.7f, h * 0.52f)
            )
            drawRect(
                brush = Brush.verticalGradient(listOf(glow.copy(alpha = 0.36f), Color.Transparent)),
                topLeft = Offset(cx - pillarW * 0.5f, h * 0.18f),
                size = Size(pillarW, h * 0.65f)
            )
            drawRect(
                brush = Brush.verticalGradient(listOf(glow.copy(alpha = 0.20f), Color.Transparent)),
                topLeft = Offset(cx + pillarW * 0.7f, h * 0.32f),
                size = Size(pillarW * 0.7f, h * 0.48f)
            )
            drawCircle(
                color = GoldBright.copy(alpha = 0.5f),
                radius = minDim * 0.18f * pulse,
                center = Offset(cx, cy * 0.85f),
                style = Stroke(width = 2.4f)
            )
        }

        ArtMotif.MECHA_GRID -> {
            val step = minDim * 0.14f
            for (i in -3..3) {
                drawLine(
                    color = glow.copy(alpha = 0.12f),
                    start = Offset(cx + i * step, 0f),
                    end = Offset(cx + i * step * 1.6f, h),
                    strokeWidth = 1f
                )
            }
            val hexR = minDim * 0.26f * pulse
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val angle = (i * 60f - 30f) * (PI / 180f).toFloat()
                    val x = cx + cos(angle) * hexR
                    val y = cy + sin(angle) * hexR
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(hex, color = glow.copy(alpha = 0.55f), style = Stroke(width = 2.5f))
            drawCircle(color = glow.copy(alpha = 0.35f), radius = hexR * 0.32f, center = Offset(cx, cy))
        }

        ArtMotif.TORII_MOON -> {
            // Crescent/full golden moon + stylized gate silhouette
            val moonBrushRadius = (minDim * 0.28f).coerceAtLeast(1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldBright.copy(alpha = 0.55f), glow.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(cx, cy * 0.92f),
                    radius = moonBrushRadius
                ),
                radius = (minDim * 0.24f * pulse).coerceAtLeast(1f),
                center = Offset(cx, cy * 0.92f)
            )
            // Gate lintels
            drawLine(
                color = glow.copy(alpha = 0.55f),
                start = Offset(cx - minDim * 0.28f, cy + minDim * 0.08f),
                end = Offset(cx + minDim * 0.28f, cy + minDim * 0.08f),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = glow.copy(alpha = 0.40f),
                start = Offset(cx - minDim * 0.20f, cy + minDim * 0.16f),
                end = Offset(cx + minDim * 0.20f, cy + minDim * 0.16f),
                strokeWidth = 2.5f
            )
        }

        ArtMotif.SOLAR_ECLIPSE -> {
            val r = minDim * 0.25f * pulse
            drawCircle(
                color = glow.copy(alpha = 0.65f),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 3.5f)
            )
            drawCircle(
                color = MediaScrimDark,
                radius = r * 0.88f,
                center = Offset(cx, cy)
            )
            // Alchemy triangle
            val tri = Path().apply {
                moveTo(cx, cy - r * 0.72f)
                lineTo(cx + r * 0.62f, cy + r * 0.42f)
                lineTo(cx - r * 0.62f, cy + r * 0.42f)
                close()
            }
            drawPath(tri, color = glow.copy(alpha = 0.45f), style = Stroke(width = 1.8f))
        }

        ArtMotif.COMPASS_STAR -> {
            val r = minDim * 0.28f * pulse
            rotate(degrees = sin(phase * 2f * PI.toFloat()) * 14f, pivot = Offset(cx, cy)) {
                val compass = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r * 0.22f, cy - r * 0.22f)
                    lineTo(cx + r, cy)
                    lineTo(cx + r * 0.22f, cy + r * 0.22f)
                    lineTo(cx, cy + r)
                    lineTo(cx - r * 0.22f, cy + r * 0.22f)
                    lineTo(cx - r, cy)
                    lineTo(cx - r * 0.22f, cy - r * 0.22f)
                    close()
                }
                drawPath(compass, color = glow.copy(alpha = 0.55f), style = Stroke(width = 2.2f))
                drawCircle(color = glow.copy(alpha = 0.25f), radius = r * 0.62f, center = Offset(cx, cy), style = Stroke(1.4f))
            }
        }

        ArtMotif.HOURGLASS_SIGIL -> {
            val r = minDim * 0.26f * pulse
            val hourglass = Path().apply {
                moveTo(cx - r * 0.65f, cy - r * 0.8f)
                lineTo(cx + r * 0.65f, cy - r * 0.8f)
                lineTo(cx - r * 0.65f, cy + r * 0.8f)
                lineTo(cx + r * 0.65f, cy + r * 0.8f)
                close()
            }
            drawPath(hourglass, color = glow.copy(alpha = 0.50f), style = Stroke(width = 2.2f))
            drawCircle(
                color = glow.copy(alpha = 0.24f),
                radius = r * 0.95f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
        }
    }
}
