package com.gows.animwatch.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

val AnimWatchShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AnimWatchTheme(
    appearanceMode: AppAppearanceMode = AppAppearanceMode.DARK,
    accentTheme: AppAccentTheme = AppAccentTheme.COBALT_SAPPHIRE,
    content: @Composable () -> Unit
) {
    // Update reactive palette tokens synchronously before composing content
    AppThemeState.apply(appearanceMode, accentTheme)
    val palette = AppThemeState.current

    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accentPrimary,
            onPrimary = palette.textOnAccent,
            primaryContainer = palette.accentTintStrong,
            onPrimaryContainer = palette.accentBright,
            secondary = palette.accentWarm,
            onSecondary = palette.textOnAccent,
            secondaryContainer = palette.surfaceCardAlt,
            onSecondaryContainer = palette.accentPrimary,
            tertiary = palette.accentBright,
            onTertiary = palette.textOnAccent,
            background = palette.surfaceVoid,
            onBackground = palette.textPrimary,
            surface = palette.surfaceBase,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceCard,
            onSurfaceVariant = palette.textSecondary,
            surfaceContainerLowest = palette.surfaceVoid,
            surfaceContainerLow = palette.surfaceBase,
            surfaceContainer = palette.surfaceElevated,
            surfaceContainerHigh = palette.surfaceCard,
            surfaceContainerHighest = palette.surfaceCardAlt,
            outline = palette.accentBorder,
            outlineVariant = palette.dividerSubtle,
            error = StatusLive,
            onError = MediaTextLight
        )
    } else {
        lightColorScheme(
            primary = palette.accentPrimary,
            onPrimary = palette.textOnAccent,
            primaryContainer = palette.accentTintStrong,
            onPrimaryContainer = palette.accentDeep,
            secondary = palette.accentWarm,
            onSecondary = palette.textOnAccent,
            secondaryContainer = palette.surfaceCardAlt,
            onSecondaryContainer = palette.accentPrimary,
            tertiary = palette.accentBright,
            onTertiary = palette.textOnAccent,
            background = palette.surfaceVoid,
            onBackground = palette.textPrimary,
            surface = palette.surfaceBase,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceCard,
            onSurfaceVariant = palette.textSecondary,
            surfaceContainerLowest = palette.surfaceVoid,
            surfaceContainerLow = palette.surfaceBase,
            surfaceContainer = palette.surfaceElevated,
            surfaceContainerHigh = palette.surfaceCard,
            surfaceContainerHighest = palette.surfaceCardAlt,
            outline = palette.accentBorder,
            outlineVariant = palette.dividerSubtle,
            error = StatusLive,
            onError = MediaTextLight
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                val lightIcons = !palette.isDark
                controller.isAppearanceLightStatusBars = lightIcons
                controller.isAppearanceLightNavigationBars = lightIcons
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AnimWatchShapes,
        content = content
    )
}
