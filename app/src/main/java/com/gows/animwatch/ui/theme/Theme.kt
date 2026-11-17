package com.gows.animwatch.ui.theme

import android.app.Activity
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

val AnimWatchShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AnimWatchTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
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
