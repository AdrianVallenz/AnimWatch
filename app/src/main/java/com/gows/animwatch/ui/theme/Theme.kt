package com.gows.animwatch.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AnimWatchColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = TextOnGold,
    primaryContainer = GoldTintStrong,
    onPrimaryContainer = GoldBright,
    secondary = GoldWarm,
    onSecondary = TextOnGold,
    secondaryContainer = ObsidianCardAlt,
    onSecondaryContainer = GoldPrimary,
    tertiary = GoldBright,
    onTertiary = TextOnGold,
    background = ObsidianVoid,
    onBackground = TextPrimary,
    surface = ObsidianBase,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianVoid,
    surfaceContainerLow = ObsidianBase,
    surfaceContainer = ObsidianElevated,
    surfaceContainerHigh = ObsidianCard,
    surfaceContainerHighest = ObsidianCardAlt,
    outline = GoldBorder,
    outlineVariant = DividerSubtle,
    error = StatusLive,
    onError = TextPrimary
)

// Cassette Futurism base corner radius: 8dp (chunky tactile bezels)
val AnimWatchShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AnimWatchTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AnimWatchColorScheme,
        typography = Typography,
        shapes = AnimWatchShapes,
        content = content
    )
}
