package com.gows.animwatch.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// AnimWatch Luxury Black & Gold Color System
// Single cohesive theme across the entire app
// ==========================================

// Layered Obsidian Blacks
val ObsidianVoid = Color(0xFF070709)
val ObsidianBase = Color(0xFF0D0D11)
val ObsidianElevated = Color(0xFF14141B)
val ObsidianCard = Color(0xFF1A1A23)
val ObsidianCardAlt = Color(0xFF20202B)
val ObsidianHover = Color(0xFF282835)
val ObsidianGlass = Color(0xE6111117)

// Royal & Champagne Golds
val GoldBright = Color(0xFFF8D477)
val GoldPrimary = Color(0xFFE5B84B)
val GoldWarm = Color(0xFFD4A02E)
val GoldDeep = Color(0xFFA67819)
val GoldBronze = Color(0xFF6E4E12)
val GoldTint = Color(0x26E5B84B)
val GoldTintStrong = Color(0x3DE5B84B)
val GoldBorder = Color(0x4DE5B84B)
val GoldSubtleBorder = Color(0x22E5B84B)

// Typography Neutrals
val TextPrimary = Color(0xFFF7F4EC)
val TextSecondary = Color(0xFFB8B1A3)
val TextTertiary = Color(0xFF7D766A)
val TextOnGold = Color(0xFF0A0907)

// Semantic Accents (tuned for Obsidian & Gold harmony)
val StatusLive = Color(0xFFE85D4A)
val StatusEmerald = Color(0xFF52C478)
val StatusInfo = Color(0xFF59A5E8)
val DividerSubtle = Color(0x1FF7F4EC)

// Signature Brushes
val GoldGradientBrush = Brush.linearGradient(
    colors = listOf(GoldBright, GoldPrimary, GoldDeep),
    start = Offset.Zero,
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val GoldHorizontalBrush = Brush.horizontalGradient(
    colors = listOf(GoldBright, GoldPrimary, GoldWarm)
)

val GoldBorderBrush = Brush.linearGradient(
    colors = listOf(
        GoldBright.copy(alpha = 0.65f),
        GoldPrimary.copy(alpha = 0.25f),
        GoldDeep.copy(alpha = 0.45f)
    )
)

val ObsidianGoldCardBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1F1B12),
        Color(0xFF14131A),
        Color(0xFF0B0B0F)
    )
)
