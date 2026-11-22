package com.gows.animwatch.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldDeep
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.MonoFont
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Subtle CRT horizontal scanline overlay for Cassette Futurism monitors & artwork decks.
 */
@Composable
fun CrtScanlineOverlay(
    modifier: Modifier = Modifier,
    lineAlpha: Float = 0.08f,
    stepPx: Float = 6f
) {
    val effectiveAlpha = if (IsDarkTheme) lineAlpha else (lineAlpha * 0.35f)
    Canvas(modifier = modifier) {
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color(0xFF0C0F14).copy(alpha = effectiveAlpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.6f
            )
            y += stepPx
        }
    }
}

/**
 * Hardware phosphor LED indicator with optional pulse animation.
 */
@Composable
fun LedStatusDot(
    color: Color = GoldBright,
    pulsing: Boolean = true,
    size: Dp = 7.dp,
    modifier: Modifier = Modifier
) {
    val alpha = if (pulsing) {
        val transition = rememberInfiniteTransition(label = "led_pulse")
        val anim by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "led_alpha"
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
 * Animated magnetic cassette tape reel in Gold & Charcoal.
 */
@Composable
fun TapeReelSpinner(
    isSpinning: Boolean,
    size: Dp = 24.dp,
    tint: Color = GoldBright,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "tape_reel")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reel_angle"
    )
    val rotation = if (isSpinning) angle else 0f

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = minOf(cx, cy) - 2f

        // Outer magnetic reel rim
        drawCircle(
            color = tint.copy(alpha = 0.35f),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 1.6f)
        )
        // Inner spool hub
        drawCircle(
            color = tint,
            radius = r * 0.30f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )
        // 3 rotating sprocket teeth
        rotate(degrees = rotation, pivot = Offset(cx, cy)) {
            for (i in 0 until 3) {
                val rad = (i * 120f) * (PI / 180f).toFloat()
                drawLine(
                    color = tint,
                    start = Offset(cx + cos(rad) * (r * 0.34f), cy + sin(rad) * (r * 0.34f)),
                    end = Offset(cx + cos(rad) * (r * 0.85f), cy + sin(rad) * (r * 0.85f)),
                    strokeWidth = 2.0f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Segmented Phosphor LED / Analog Meter Bar Gauge.
 * Replaces smooth/generic progress bars with authentic hardware metering.
 */
@Composable
fun AnalogMeterGauge(
    progress: Float,
    segments: Int = 18,
    height: Dp = 8.dp,
    activeColor: Color = GoldBright,
    trackColor: Color = ObsidianCardAlt,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0f, 1f)
    val activeCount = (clamped * segments).toInt().coerceIn(0, segments)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(ObsidianVoid)
            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(4.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until segments) {
            val isLit = i < activeCount || (clamped > 0f && i == 0)
            val segColor = when {
                !isLit -> trackColor
                i >= segments - 2 -> GoldBright
                else -> activeColor
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(segColor)
            )
        }
    }
}

/**
 * Hardware telemetry strip header for deck sections.
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
            .clip(RoundedCornerShape(6.dp))
            .background(ObsidianCard)
            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LedStatusDot(color = ledColor, pulsing = pulsing, size = 6.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = code,
                fontFamily = MonoFont,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBright,
                letterSpacing = 0.8.sp
            )
        }
        Text(
            text = statusText,
            fontFamily = MonoFont,
            fontSize = 10.sp,
            color = TextTertiary,
            letterSpacing = 0.6.sp
        )
    }
}

/**
 * Cassette Futurism Form Input:
 * - Label placed strictly ABOVE the input box (no floating label)
 * - 1dp border unfocused, 2dp gold border when focused
 * - 8dp corner radius
 * - Optional error line below in Tape Red
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
    val borderWidth = if (isFocused) 2.dp else 1.dp
    val borderColor = when {
        errorText != null -> StatusLive
        isFocused -> GoldBright
        else -> GoldBorder
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontFamily = MonoFont,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) GoldBright else TextSecondary,
            letterSpacing = 0.9.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianVoid)
                .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
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
                    cursorBrush = SolidColor(GoldBright),
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
                fontFamily = MonoFont,
                fontSize = 10.5.sp,
                color = StatusLive,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Cassette shimmer placeholder bar (replaces circular spinners where needed).
 */
@Composable
fun CassetteLinearShimmer(
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    if (progress != null) {
        AnalogMeterGauge(
            progress = progress,
            segments = 10,
            height = 7.dp,
            modifier = modifier
        )
    } else {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val pos by transition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmer_pos"
        )
        Box(
            modifier = modifier
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ObsidianCardAlt)
                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pos)
                    .fillMaxHeight()
                    .background(GoldDeep)
            )
        }
    }
}
