package com.gows.animwatch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldHorizontalBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

/**
 * Soft organic ambient sheen overlay (replaces harsh CRT scanlines for a clean, silky finish).
 */
@Composable
fun CrtScanlineOverlay(
    modifier: Modifier = Modifier,
    lineAlpha: Float = 0.04f,
    stepPx: Float = 6f
) {
    // Keep parameter signature compatible; renders a subtle soft atmospheric glow instead of rigid scanlines
    val sheenAlpha = if (IsDarkTheme) (lineAlpha * 0.45f).coerceAtMost(0.05f) else 0.0f
    if (sheenAlpha > 0f) {
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = sheenAlpha),
                    0.35f to Color.Transparent,
                    1.0f to Color.Transparent
                )
            )
        )
    }
}

/**
 * Soft breathing status indicator dot.
 */
@Composable
fun LedStatusDot(
    color: Color = GoldBright,
    pulsing: Boolean = true,
    size: Dp = 7.dp,
    modifier: Modifier = Modifier
) {
    val alpha = if (pulsing) {
        val transition = rememberInfiniteTransition(label = "status_pulse")
        val anim by transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "status_alpha"
        )
        anim
    } else 1.0f

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

/**
 * Smooth organic animated equalizer wave (replaces stiff mechanical reel).
 */
@Composable
fun TapeReelSpinner(
    isSpinning: Boolean,
    size: Dp = 22.dp,
    tint: Color = GoldBright,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "wave_anim")
    val bar1 by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(580, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(640, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val barWidth = w * 0.20f
        val gap = w * 0.12f
        val startX = (w - (barWidth * 3 + gap * 2)) / 2f
        val heights = if (isSpinning) listOf(bar1, bar2, bar3) else listOf(0.45f, 0.70f, 0.45f)

        heights.forEachIndexed { idx, frac ->
            val bh = (h * frac).coerceAtLeast(barWidth)
            val x = startX + idx * (barWidth + gap)
            val y = (h - bh) / 2f
            drawRoundRect(
                color = tint.copy(alpha = if (isSpinning) 0.95f else 0.55f),
                topLeft = Offset(x, y),
                size = Size(barWidth, bh),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

/**
 * Silky smooth capsule progress bar with fluid animated fill.
 */
@Composable
fun AnalogMeterGauge(
    progress: Float,
    segments: Int = 18,
    height: Dp = 6.dp,
    activeColor: Color = GoldPrimary,
    trackColor: Color = ObsidianCardAlt,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "smooth_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceAtLeast(0.03f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(activeColor, GoldBright)
                        )
                    )
            )
        }
    }
}

/**
 * Soft organic pill status banner.
 */
@Composable
fun CassetteTelemetryStrip(
    code: String,
    statusText: String,
    ledColor: Color = GoldBright,
    pulsing: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LedStatusDot(color = ledColor, pulsing = pulsing, size = 7.dp)
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = code,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontSize = 12.5.sp
            )
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            fontSize = 11.5.sp
        )
    }
}

/**
 * Soft, smooth, organic rounded form input field.
 */
@Composable
fun TactileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    errorText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = when {
            errorText != null -> StatusLive
            isFocused -> GoldPrimary
            else -> GoldSubtleBorder
        },
        animationSpec = tween(220),
        label = "input_border"
    )
    val labelColor by animateColorAsState(
        targetValue = when {
            errorText != null -> StatusLive
            isFocused -> GoldPrimary
            else -> TextSecondary
        },
        animationSpec = tween(220),
        label = "input_label"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianCardAlt.copy(alpha = if (IsDarkTheme) 0.72f else 0.85f))
                .border(
                    width = if (isFocused || errorText != null) 1.5.dp else 0.75.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    maxLines = maxLines,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    cursorBrush = SolidColor(GoldPrimary),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                )
            }
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = StatusLive,
                modifier = Modifier.padding(start = 4.dp, top = 5.dp)
            )
        }
    }
}

/**
 * Silky rounded shimmer bar for loading states.
 */
@Composable
fun CassetteLinearShimmer(
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    if (progress != null) {
        AnalogMeterGauge(
            progress = progress,
            height = 6.dp,
            modifier = modifier
        )
    } else {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val pos by transition.animateFloat(
            initialValue = 0.22f,
            targetValue = 0.90f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmer_pos"
        )
        Box(
            modifier = modifier
                .height(6.dp)
                .clip(CircleShape)
                .background(ObsidianCardAlt)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pos)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(GoldHorizontalBrush)
            )
        }
    }
}
