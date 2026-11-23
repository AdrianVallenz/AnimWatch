package com.gows.animwatch.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// AnimWatch Organic Multi-Theme Palette (Dark & Light Modes)
// 7 Curated Non-Gold Accent Palettes with Soft Layered Depth
// ============================================================

enum class AppAppearanceMode(val label: String, val code: String, val subtitle: String) {
    DARK("Mode Gelap", "Gelap", "Tenang dan nyaman di malam hari"),
    LIGHT("Mode Terang", "Terang", "Cerah, lapang, dan bersih di siang hari")
}

enum class AppAccentTheme(
    val displayName: String,
    val code: String,
    val tagline: String,
    val darkPrimary: Color,
    val darkBright: Color,
    val darkDeep: Color,
    val lightPrimary: Color,
    val lightBright: Color,
    val lightDeep: Color,
    val onAccentDark: Color,
    val onAccentLight: Color
) {
    COBALT_SAPPHIRE(
        displayName = "Biru Safir",
        code = "Safir",
        tagline = "Tenang & sinematik",
        darkPrimary = Color(0xFF5682F5),
        darkBright = Color(0xFF84A9FF),
        darkDeep = Color(0xFF335ED4),
        lightPrimary = Color(0xFF3461DE),
        lightBright = Color(0xFF5682F5),
        lightDeep = Color(0xFF2349B5),
        onAccentDark = Color(0xFFFFFFFF),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    CRIMSON_RUBY(
        displayName = "Merah Rubi",
        code = "Rubi",
        tagline = "Hangat & penuh energi",
        darkPrimary = Color(0xFFE55367),
        darkBright = Color(0xFFFF8293),
        darkDeep = Color(0xFFBF2E43),
        lightPrimary = Color(0xFFD1324A),
        lightBright = Color(0xFFE8566C),
        lightDeep = Color(0xFFA62035),
        onAccentDark = Color(0xFFFFFFFF),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    ELECTRIC_CYAN(
        displayName = "Biru Sian",
        code = "Sian",
        tagline = "Segar & modern",
        darkPrimary = Color(0xFF2CC2D6),
        darkBright = Color(0xFF70DFED),
        darkDeep = Color(0xFF1594A6),
        lightPrimary = Color(0xFF0E8FA3),
        lightBright = Color(0xFF22ABC0),
        lightDeep = Color(0xFF0A6B7A),
        onAccentDark = Color(0xFF061A1E),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    EMERALD_MINT(
        displayName = "Hijau Zamrud",
        code = "Zamrud",
        tagline = "Sejuk & alami di mata",
        darkPrimary = Color(0xFF38C988),
        darkBright = Color(0xFF72E3AE),
        darkDeep = Color(0xFF1F9E65),
        lightPrimary = Color(0xFF1E9E65),
        lightBright = Color(0xFF35BD80),
        lightDeep = Color(0xFF147A4D),
        onAccentDark = Color(0xFF061A11),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    VIOLET_AMETHYST(
        displayName = "Ungu Ametis",
        code = "Ametis",
        tagline = "Lembut & elegan",
        darkPrimary = Color(0xFF9376ED),
        darkBright = Color(0xFFB7A1FA),
        darkDeep = Color(0xFF6E4FD1),
        lightPrimary = Color(0xFF724EDB),
        lightBright = Color(0xFF9172EB),
        lightDeep = Color(0xFF5634B8),
        onAccentDark = Color(0xFFFFFFFF),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    CORAL_SUNSET(
        displayName = "Oranye Koral",
        code = "Koral",
        tagline = "Ceria & hangat",
        darkPrimary = Color(0xFFEF754B),
        darkBright = Color(0xFFFF9B78),
        darkDeep = Color(0xFFC95028),
        lightPrimary = Color(0xFFD95529),
        lightBright = Color(0xFFEF754B),
        lightDeep = Color(0xFFAD3D18),
        onAccentDark = Color(0xFFFFFFFF),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    ROSE_MAGENTA(
        displayName = "Merah Muda",
        code = "Rose",
        tagline = "Manis & ekspresif",
        darkPrimary = Color(0xFFE660A4),
        darkBright = Color(0xFFFF91C8),
        darkDeep = Color(0xFFBD3A7E),
        lightPrimary = Color(0xFFC93C84),
        lightBright = Color(0xFFE35EA2),
        lightDeep = Color(0xFF9E2864),
        onAccentDark = Color(0xFFFFFFFF),
        onAccentLight = Color(0xFFFFFFFF)
    )
}

data class AnimWatchPalette(
    val isDark: Boolean,
    val surfaceVoid: Color,
    val surfaceBase: Color,
    val surfaceElevated: Color,
    val surfaceCard: Color,
    val surfaceCardAlt: Color,
    val surfaceHover: Color,
    val surfaceGlass: Color,
    val accentBright: Color,
    val accentPrimary: Color,
    val accentWarm: Color,
    val accentDeep: Color,
    val accentMuted: Color,
    val accentTint: Color,
    val accentTintStrong: Color,
    val accentBorder: Color,
    val accentSubtleBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,
    val dividerSubtle: Color
) {
    // Pre-computed brushes cached per palette instance (zero per-frame allocations during scroll)
    val gradientBrush: Brush = Brush.linearGradient(
        colors = listOf(accentBright, accentPrimary),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    val horizontalBrush: Brush = Brush.horizontalGradient(
        colors = listOf(accentPrimary, accentBright)
    )
    val borderBrush: Brush = Brush.linearGradient(
        colors = listOf(
            accentBright.copy(alpha = 0.42f),
            accentPrimary.copy(alpha = 0.22f),
            accentDeep.copy(alpha = 0.30f)
        )
    )
    val cardBrush: Brush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                accentPrimary.copy(alpha = 0.18f),
                surfaceCard,
                surfaceElevated
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                accentPrimary.copy(alpha = 0.12f),
                surfaceCard,
                surfaceVoid
            )
        )
    }
}

fun buildPalette(mode: AppAppearanceMode, accent: AppAccentTheme): AnimWatchPalette {
    val isDark = mode == AppAppearanceMode.DARK
    val primary = if (isDark) accent.darkPrimary else accent.lightPrimary
    val bright = if (isDark) accent.darkBright else accent.lightBright
    val deep = if (isDark) accent.darkDeep else accent.lightDeep
    val onAccent = if (isDark) accent.onAccentDark else accent.onAccentLight

    return if (isDark) {
        AnimWatchPalette(
            isDark = true,
            surfaceVoid = Color(0xFF0F1117),
            surfaceBase = Color(0xFF151821),
            surfaceElevated = Color(0xFF1B1F2B),
            surfaceCard = Color(0xFF212635),
            surfaceCardAlt = Color(0xFF2A3042),
            surfaceHover = Color(0xFF343B50),
            surfaceGlass = Color(0xEB171B26),
            accentBright = bright,
            accentPrimary = primary,
            accentWarm = primary,
            accentDeep = deep,
            accentMuted = deep.copy(alpha = 0.65f),
            accentTint = primary.copy(alpha = 0.13f),
            accentTintStrong = primary.copy(alpha = 0.22f),
            accentBorder = primary.copy(alpha = 0.34f),
            accentSubtleBorder = Color(0x1AE8EDF7),
            textPrimary = Color(0xFFF3F5FA),
            textSecondary = Color(0xFFB5BDCF),
            textTertiary = Color(0xFF818A9E),
            textOnAccent = onAccent,
            dividerSubtle = Color(0x16F3F5FA)
        )
    } else {
        AnimWatchPalette(
            isDark = false,
            surfaceVoid = Color(0xFFF6F7FB),
            surfaceBase = Color(0xFFEEF1F7),
            surfaceElevated = Color(0xFFFFFFFF),
            surfaceCard = Color(0xFFFFFFFF),
            surfaceCardAlt = Color(0xFFEDF0F7),
            surfaceHover = Color(0xFFE1E6F0),
            surfaceGlass = Color(0xF5FFFFFF),
            accentBright = primary,
            accentPrimary = primary,
            accentWarm = bright,
            accentDeep = deep,
            accentMuted = deep.copy(alpha = 0.60f),
            accentTint = primary.copy(alpha = 0.10f),
            accentTintStrong = primary.copy(alpha = 0.17f),
            accentBorder = primary.copy(alpha = 0.32f),
            accentSubtleBorder = Color(0x17141824),
            textPrimary = Color(0xFF171C28),
            textSecondary = Color(0xFF525D73),
            textTertiary = Color(0xFF7F8A9E),
            textOnAccent = onAccent,
            dividerSubtle = Color(0x14171C28)
        )
    }
}

object AppThemeState {
    private var lastMode: AppAppearanceMode = AppAppearanceMode.DARK
    private var lastAccent: AppAccentTheme = AppAccentTheme.COBALT_SAPPHIRE

    var current by mutableStateOf(
        buildPalette(AppAppearanceMode.DARK, AppAccentTheme.COBALT_SAPPHIRE)
    )

    fun apply(mode: AppAppearanceMode, accent: AppAccentTheme) {
        // Avoid redundant snapshot writes that trigger full-tree recomposition storms
        if (lastMode == mode && lastAccent == accent) return
        lastMode = mode
        lastAccent = accent
        current = buildPalette(mode, accent)
    }
}

// Reactive Theme Tokens
val IsDarkTheme: Boolean get() = AppThemeState.current.isDark
val ObsidianVoid: Color get() = AppThemeState.current.surfaceVoid
val ObsidianBase: Color get() = AppThemeState.current.surfaceBase
val ObsidianElevated: Color get() = AppThemeState.current.surfaceElevated
val ObsidianCard: Color get() = AppThemeState.current.surfaceCard
val ObsidianCardAlt: Color get() = AppThemeState.current.surfaceCardAlt
val ObsidianHover: Color get() = AppThemeState.current.surfaceHover
val ObsidianGlass: Color get() = AppThemeState.current.surfaceGlass

val GoldBright: Color get() = AppThemeState.current.accentBright
val GoldPrimary: Color get() = AppThemeState.current.accentPrimary
val GoldWarm: Color get() = AppThemeState.current.accentWarm
val GoldDeep: Color get() = AppThemeState.current.accentDeep
val GoldBronze: Color get() = AppThemeState.current.accentMuted
val GoldTint: Color get() = AppThemeState.current.accentTint
val GoldTintStrong: Color get() = AppThemeState.current.accentTintStrong
val GoldBorder: Color get() = AppThemeState.current.accentBorder
val GoldSubtleBorder: Color get() = AppThemeState.current.accentSubtleBorder

val TextPrimary: Color get() = AppThemeState.current.textPrimary
val TextSecondary: Color get() = AppThemeState.current.textSecondary
val TextTertiary: Color get() = AppThemeState.current.textTertiary
val TextOnGold: Color get() = AppThemeState.current.textOnAccent
val DividerSubtle: Color get() = AppThemeState.current.dividerSubtle

// Always-Dark Media Overlay Tokens (legibility on top of posters & video streams in Dark & Light modes)
val MediaScrimDark = Color(0xFF0E1016)
val MediaTextLight = Color(0xFFF6F7FB)
val MediaTextSecondary = Color(0xFFCCD2DE)
val MediaAccentBright: Color get() = AppThemeState.current.let { pal ->
    if (pal.isDark) pal.accentBright else pal.accentWarm
}

// Semantic Status Colors
val StatusLive = Color(0xFFE04856)
val StatusEmerald = Color(0xFF2EB377)
val StatusAmber = Color(0xFFEF754B)

// Soft Organic Brushes (cached on active palette)
val GoldGradientBrush: Brush get() = AppThemeState.current.gradientBrush
val GoldHorizontalBrush: Brush get() = AppThemeState.current.horizontalBrush
val GoldBorderBrush: Brush get() = AppThemeState.current.borderBrush
val ObsidianGoldCardBrush: Brush get() = AppThemeState.current.cardBrush
