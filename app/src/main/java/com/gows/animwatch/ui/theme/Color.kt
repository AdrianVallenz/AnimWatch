package com.gows.animwatch.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// AnimWatch Cassette Futurism — Charcoal Black & Royal Gold
// Single cohesive dark analog-digital theme (no pure #000000)
// ============================================================

// Charcoal & Off-Black Layered Surfaces
val ObsidianVoid = Color(0xFF0C0C10)       // Deepest chassis off-black
val ObsidianBase = Color(0xFF121217)       // Primary deck surface
val ObsidianElevated = Color(0xFF18181F)   // Elevated panel & sheets
val ObsidianCard = Color(0xFF1E1E26)       // Tactile module card
val ObsidianCardAlt = Color(0xFF262630)    // Recessed meter track / inset
val ObsidianHover = Color(0xFF30303C)      // Pressed / active chassis
val ObsidianGlass = Color(0xF015151C)      // Translucent console bar

// Royal Gold & Phosphor Amber Accents (saturation <= 80%)
val GoldBright = Color(0xFFF5CE62)         // Phosphor gold highlight
val GoldPrimary = Color(0xFFE2B23B)        // Primary gold button & active indicator
val GoldWarm = Color(0xFFC99728)           // Phosphor amber-gold
val GoldDeep = Color(0xFF966D19)           // Burnished deep gold
val GoldBronze = Color(0xFF6B4E14)         // Subtle dark bronze
val GoldTint = Color(0x22E2B23B)           // 13% gold translucent surface
val GoldTintStrong = Color(0x38E2B23B)     // 22% gold active chip fill
val GoldBorder = Color(0x66E2B23B)         // Crisp 1dp/1.5dp bezel border
val GoldSubtleBorder = Color(0x2BE2B23B)   // Hairline cassette panel stroke

// Warm Off-White & Beige Typography (Cassette Futurism paper/phosphor)
val TextPrimary = Color(0xFFEDE6D6)        // Warm off-white (#E8E0D0 family)
val TextSecondary = Color(0xFFC2B59B)      // Warm beige (#D2B48C family)
val TextTertiary = Color(0xFF8A8273)       // Static warm grey
val TextOnGold = Color(0xFF100E0A)         // Deep charcoal ink on gold plates

// Semantic Accents (Tape Red & Phosphor Status)
val StatusLive = Color(0xFFCC2E28)         // Tape Red (REC / Error / Destructive)
val StatusEmerald = Color(0xFF4CAF6E)      // Phosphor green (Ready / Offline OK)
val StatusAmber = Color(0xFFE5A93B)        // Warning / Standby LED
val DividerSubtle = Color(0x22EDE6D6)      // Warm panel seam line

// Signature Brushes
val GoldGradientBrush = Brush.linearGradient(
    colors = listOf(GoldBright, GoldPrimary, GoldWarm),
    start = Offset.Zero,
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val GoldHorizontalBrush = Brush.horizontalGradient(
    colors = listOf(GoldBright, GoldPrimary, GoldWarm)
)

val GoldBorderBrush = Brush.linearGradient(
    colors = listOf(
        GoldBright.copy(alpha = 0.72f),
        GoldPrimary.copy(alpha = 0.35f),
        GoldDeep.copy(alpha = 0.55f)
    )
)

val ObsidianGoldCardBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF231E13),
        Color(0xFF17171E),
        Color(0xFF101015)
    )
)
