package com.gows.animwatch.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// AnimWatch Dynamic Multi-Theme Engine (Dark & Light Modes)
// 7 Curated Accent Color Themes — NO Gold/Yellow Palette
// ============================================================

enum class AppAppearanceMode(val label: String, val code: String, val subtitle: String) {
    DARK("Mode Gelap", "DARK", "Nyaman ditonton malam hari"),
    LIGHT("Mode Terang", "LIGHT", "Bersih dan tajam di siang hari")
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
        code = "SAPPHIRE",
        tagline = "Modern & fokus sinematik",
        darkPrimary = Color(0xFF4C7CF5),
        darkBright = Color(0xFF7CA4FF),
        darkDeep = Color(0xFF2B54C7),
        lightPrimary = Color(0xFF2855D6),
        lightBright = Color(0xFF4C7CF5),
        lightDeep = Color(0xFF1C3EA6),
        onAccentDark = Color(0xFFF6F8FF),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    CRIMSON_RUBY(
        displayName = "Merah Rubi",
        code = "CRIMSON",
        tagline = "Tegas & penuh energi aksi",
        darkPrimary = Color(0xFFE0455A),
        darkBright = Color(0xFFFF7588),
        darkDeep = Color(0xFFB3263B),
        lightPrimary = Color(0xFFC92A42),
        lightBright = Color(0xFFE54860),
        lightDeep = Color(0xFF991B2E),
        onAccentDark = Color(0xFFFFF5F6),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    ELECTRIC_CYAN(
        displayName = "Biru Sian",
        code = "CYAN",
        tagline = "Futuristik ala terminal sci-fi",
        darkPrimary = Color(0xFF22B8CF),
        darkBright = Color(0xFF66D9E8),
        darkDeep = Color(0xFF108A9E),
        lightPrimary = Color(0xFF0C8599),
        lightBright = Color(0xFF1AA3B8),
        lightDeep = Color(0xFF096373),
        onAccentDark = Color(0xFF071619),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    EMERALD_MINT(
        displayName = "Hijau Zamrud",
        code = "EMERALD",
        tagline = "Segar & seimbang di mata",
        darkPrimary = Color(0xFF2FC480),
        darkBright = Color(0xFF68E0A8),
        darkDeep = Color(0xFF1C945D),
        lightPrimary = Color(0xFF1A9960),
        lightBright = Color(0xFF2FC480),
        lightDeep = Color(0xFF127347),
        onAccentDark = Color(0xFF07170F),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    VIOLET_AMETHYST(
        displayName = "Ungu Ametis",
        code = "AMETHYST",
        tagline = "Elegan untuk dunia fantasi",
        darkPrimary = Color(0xFF8B6CE8),
        darkBright = Color(0xFFB197FC),
        darkDeep = Color(0xFF6546C4),
        lightPrimary = Color(0xFF6D48D6),
        lightBright = Color(0xFF8B6CE8),
        lightDeep = Color(0xFF5130B0),
        onAccentDark = Color(0xFFF8F5FF),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    CORAL_SUNSET(
        displayName = "Oranye Koral",
        code = "CORAL",
        tagline = "Hangat & penuh semangat",
        darkPrimary = Color(0xFFEB6B40),
        darkBright = Color(0xFFFF926B),
        darkDeep = Color(0xFFBD4924),
        lightPrimary = Color(0xFFD14E24),
        lightBright = Color(0xFFEB6B40),
        lightDeep = Color(0xFFA33917),
        onAccentDark = Color(0xFFFFF6F3),
        onAccentLight = Color(0xFFFFFFFF)
    ),
    ROSE_MAGENTA(
        displayName = "Merah Muda",
        code = "MAGENTA",
        tagline = "Ekspresif & bergaya",
        darkPrimary = Color(0xFFE0569C),
        darkBright = Color(0xFFFF87C2),
        darkDeep = Color(0xFFB33274),
        lightPrimary = Color(0xFFC2367D),
        lightBright = Color(0xFFE0569C),
        lightDeep = Color(0xFF94225C),
        onAccentDark = Color(0xFFFFF5FA),
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
)

fun buildPalette(mode: AppAppearanceMode, accent: AppAccentTheme): AnimWatchPalette {
    val isDark = mode == AppAppearanceMode.DARK
    val primary = if (isDark) accent.darkPrimary else accent.lightPrimary
    val bright = if (isDark) accent.darkBright else accent.lightBright
    val deep = if (isDark) accent.darkDeep else accent.lightDeep
    val onAccent = if (isDark) accent.onAccentDark else accent.onAccentLight

    return if (isDark) {
        AnimWatchPalette(
            isDark = true,
            surfaceVoid = Color(0xFF0D0F14),
            surfaceBase = Color(0xFF13161E),
            surfaceElevated = Color(0xFF191D28),
            surfaceCard = Color(0xFF202533),
            surfaceCardAlt = Color(0xFF282E3F),
            surfaceHover = Color(0xFF323A4E),
            surfaceGlass = Color(0xF0151923),
            accentBright = bright,
            accentPrimary = primary,
            accentWarm = primary,
            accentDeep = deep,
            accentMuted = deep.copy(alpha = 0.7f),
            accentTint = primary.copy(alpha = 0.16f),
            accentTintStrong = primary.copy(alpha = 0.28f),
            accentBorder = primary.copy(alpha = 0.55f),
            accentSubtleBorder = primary.copy(alpha = 0.24f),
            textPrimary = Color(0xFFF1F3F8),
            textSecondary = Color(0xFFB4BCCB),
            textTertiary = Color(0xFF7D869A),
            textOnAccent = onAccent,
            dividerSubtle = Color(0x1FF1F3F8)
        )
    } else {
        AnimWatchPalette(
            isDark = false,
            surfaceVoid = Color(0xFFF3F5FA),
            surfaceBase = Color(0xFFEAEFF7),
            surfaceElevated = Color(0xFFFFFFFF),
            surfaceCard = Color(0xFFFFFFFF),
            surfaceCardAlt = Color(0xFFE6EBF4),
            surfaceHover = Color(0xFFD9E0EE),
            surfaceGlass = Color(0xF5FFFFFF),
            accentBright = primary,
            accentPrimary = primary,
            accentWarm = bright,
            accentDeep = deep,
            accentMuted = deep.copy(alpha = 0.65f),
            accentTint = primary.copy(alpha = 0.11f),
            accentTintStrong = primary.copy(alpha = 0.20f),
            accentBorder = primary.copy(alpha = 0.45f),
            accentSubtleBorder = Color(0x26141824),
            textPrimary = Color(0xFF141824),
            textSecondary = Color(0xFF4B556B),
            textTertiary = Color(0xFF758096),
            textOnAccent = onAccent,
            dividerSubtle = Color(0x1F141824)
        )
    }
}

/**
 * Snapshot-backed live theme state so Compose recomposes and redraws
 * every screen and Canvas immediately when the user switches Dark/Light or accent color.
 */
object AppThemeState {
    var current by mutableStateOf(
        buildPalette(AppAppearanceMode.DARK, AppAccentTheme.COBALT_SAPPHIRE)
    )

    fun apply(mode: AppAppearanceMode, accent: AppAccentTheme) {
        current = buildPalette(mode, accent)
    }
}

// Reactive Theme Tokens (automatically switch across Dark/Light & 7 accent colors)
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

// Always-Dark Media Overlay Tokens (high legibility on top of posters & video streams even in Light Mode)
val MediaScrimDark = Color(0xFF0D0F14)
val MediaTextLight = Color(0xFFF4F6FA)
val MediaTextSecondary = Color(0xFFC8CFD9)

// Semantic Status Colors (Non-Gold)
val StatusLive = Color(0xFFD63946)
val StatusEmerald = Color(0xFF28A76E)
val StatusAmber = Color(0xFFEB6B40)

// Reactive Brushes
val GoldGradientBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(GoldBright, GoldPrimary, GoldDeep),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

val GoldHorizontalBrush: Brush
    get() = Brush.horizontalGradient(
        colors = listOf(GoldBright, GoldPrimary, GoldDeep)
    )

val GoldBorderBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(
            GoldBright.copy(alpha = 0.72f),
            GoldPrimary.copy(alpha = 0.38f),
            GoldDeep.copy(alpha = 0.52f)
        )
    )

val ObsidianGoldCardBrush: Brush
    get() = if (IsDarkTheme) {
        Brush.linearGradient(
            colors = listOf(
                GoldPrimary.copy(alpha = 0.22f),
                ObsidianCard,
                ObsidianBase
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                GoldPrimary.copy(alpha = 0.14f),
                ObsidianCard,
                ObsidianVoid
            )
        )
    }
