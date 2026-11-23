package com.gows.animwatch.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.PlaybackSettings
import com.gows.animwatch.ui.components.AnalogMeterGauge
import com.gows.animwatch.ui.components.AnimWatchCrestLogo
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.LedStatusDot
import com.gows.animwatch.ui.theme.AnimWatchTheme
import com.gows.animwatch.ui.theme.AppAccentTheme
import com.gows.animwatch.ui.theme.AppAppearanceMode
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianGoldCardBrush
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeOnboardingScreen(
    userName: String,
    appearanceMode: AppAppearanceMode,
    accentTheme: AppAccentTheme,
    preferredGenres: Set<String>,
    availableGenres: List<String>,
    settings: PlaybackSettings,
    isGridCatalogPreferred: Boolean,
    isFromProfile: Boolean,
    onSelectAppearance: (AppAppearanceMode) -> Unit,
    onSelectAccent: (AppAccentTheme) -> Unit,
    onTogglePreferredGenre: (String) -> Unit,
    onUpdateSettings: ((PlaybackSettings) -> PlaybackSettings) -> Unit,
    onSetGridCatalogPreferred: (Boolean) -> Unit,
    onComplete: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val genreChoices = availableGenres.take(12)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // Gentle top accent gradient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        0.0f to GoldPrimary.copy(alpha = if (IsDarkTheme) 0.14f else 0.10f),
                        1.0f to Color.Transparent
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Header & Greeting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFromProfile && onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ObsidianCard)
                                .border(0.75.dp, GoldSubtleBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Kembali",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFromProfile) "Tema & Preferensi" else "Halo, $userName",
                            style = MaterialTheme.typography.displaySmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pilih suasana layar, warna favorit, dan gaya menonton yang paling nyaman untukmu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Live Preview Card
                LiveThemePreviewCard(
                    appearanceMode = appearanceMode,
                    accentTheme = accentTheme,
                    quality = settings.defaultQuality,
                    subtitle = settings.defaultSubtitle
                )

                // ============================
                // SECTION 1: MODE GELAP / TERANG
                // ============================
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ThemeSectionTitle(
                        title = "Suasana Layar",
                        subtitle = "Pilih antara tampilan gelap yang tenang atau terang yang cerah"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppAppearanceMode.entries.forEach { mode ->
                            val selected = mode == appearanceMode
                            val previewBg = if (mode == AppAppearanceMode.DARK) Color(0xFF0F1117) else Color(0xFFF6F7FB)
                            val previewCard = if (mode == AppAppearanceMode.DARK) Color(0xFF212635) else Color(0xFFFFFFFF)
                            val previewAccent = if (mode == AppAppearanceMode.DARK) accentTheme.darkPrimary else accentTheme.lightPrimary

                            val cardBg by animateColorAsState(
                                targetValue = if (selected) GoldTintStrong else ObsidianCard,
                                animationSpec = tween(220),
                                label = "mode_bg"
                            )

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectAppearance(mode) },
                                shape = RoundedCornerShape(20.dp),
                                color = cardBg,
                                shadowElevation = if (IsDarkTheme || selected) 0.dp else 2.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .border(
                                            width = if (selected) 1.5.dp else 0.75.dp,
                                            color = if (selected) GoldPrimary else GoldSubtleBorder,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(previewBg)
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(previewAccent)
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.75f)
                                                    .height(5.dp)
                                                    .clip(CircleShape)
                                                    .background(previewCard)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.45f)
                                                    .height(4.dp)
                                                    .clip(CircleShape)
                                                    .background(previewAccent.copy(alpha = 0.65f))
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (mode == AppAppearanceMode.DARK) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                                contentDescription = null,
                                                tint = if (selected) GoldPrimary else TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = mode.label,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (selected) GoldPrimary else TextPrimary
                                            )
                                        }
                                        if (selected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(GoldPrimary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = TextOnGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = mode.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }

                // ============================
                // SECTION 2: WARNA TEMA (7 PILIHAN, TANPA EMAS)
                // ============================
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ThemeSectionTitle(
                        title = "Warna Tema Kesukaan",
                        subtitle = "Pilih palet warna aksen yang terasa paling pas di matamu"
                    )

                    val rows = AppAccentTheme.entries.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        rows.forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { theme ->
                                    val selected = theme == accentTheme
                                    val swatchPrimary = if (appearanceMode == AppAppearanceMode.DARK) {
                                        theme.darkPrimary
                                    } else {
                                        theme.lightPrimary
                                    }
                                    val swatchBright = if (appearanceMode == AppAppearanceMode.DARK) {
                                        theme.darkBright
                                    } else {
                                        theme.lightBright
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectAccent(theme) },
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (selected) swatchPrimary.copy(alpha = 0.14f) else ObsidianCard,
                                        shadowElevation = if (IsDarkTheme || selected) 0.dp else 2.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .border(
                                                    width = if (selected) 1.5.dp else 0.75.dp,
                                                    color = if (selected) swatchPrimary else GoldSubtleBorder,
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(listOf(swatchBright, swatchPrimary))
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (selected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = if (appearanceMode == AppAppearanceMode.DARK) theme.onAccentDark else theme.onAccentLight,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = theme.displayName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (selected) swatchPrimary else TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = theme.tagline,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextTertiary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // ============================
                // SECTION 3: PREFERENSI MENONTON
                // ============================
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ThemeSectionTitle(
                        title = "Preferensi Menonton",
                        subtitle = "Atur genre favorit dan pengaturan pemutaran bawaan"
                    )

                    // Genre Favorit Multi-Select
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianCard,
                        shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Genre Favorit",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${preferredGenres.size} dipilih",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                genreChoices.forEach { genre ->
                                    SelectablePrefChip(
                                        label = genre,
                                        selected = genre in preferredGenres,
                                        onClick = { onTogglePreferredGenre(genre) }
                                    )
                                }
                            }
                        }
                    }

                    // Quality, Subtitle, & Catalog View
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianCard,
                        shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Kualitas Video Default",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("1080p", "720p", "480p", "360p").forEach { q ->
                                        SelectablePrefChip(
                                            label = q,
                                            selected = settings.defaultQuality == q,
                                            onClick = { onUpdateSettings { it.copy(defaultQuality = q) } }
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = DividerSubtle)

                            Column {
                                Text(
                                    text = "Bahasa Subtitle Default",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Indonesia", "English", "Nonaktif").forEach { sub ->
                                        SelectablePrefChip(
                                            label = sub,
                                            selected = settings.defaultSubtitle == sub,
                                            onClick = { onUpdateSettings { it.copy(defaultSubtitle = sub) } }
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = DividerSubtle)

                            Column {
                                Text(
                                    text = "Gaya Katalog Jelajah",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SelectablePrefChip(
                                        label = "Grid Kartu",
                                        selected = isGridCatalogPreferred,
                                        onClick = { onSetGridCatalogPreferred(true) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SelectablePrefChip(
                                        label = "Daftar Ringkas",
                                        selected = !isGridCatalogPreferred,
                                        onClick = { onSetGridCatalogPreferred(false) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Smart Toggles
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianCard,
                        shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
                    ) {
                        Column(
                            modifier = Modifier.border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                        ) {
                            OnboardingToggleRow(
                                icon = Icons.Rounded.PlayCircleOutline,
                                title = "Putar Episode Berikutnya Otomatis",
                                subtitle = "Lanjut otomatis setelah episode selesai",
                                checked = settings.autoPlayNext,
                                onCheckedChange = { checked ->
                                    onUpdateSettings { it.copy(autoPlayNext = checked) }
                                }
                            )
                            HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            OnboardingToggleRow(
                                icon = Icons.Rounded.FastForward,
                                title = "Lewati Intro Otomatis",
                                subtitle = "Langsung lewati lagu pembuka episode",
                                checked = settings.autoSkipIntro,
                                onCheckedChange = { checked ->
                                    onUpdateSettings { it.copy(autoSkipIntro = checked) }
                                }
                            )
                            HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            OnboardingToggleRow(
                                icon = Icons.Rounded.NotificationsActive,
                                title = "Notifikasi Episode Baru",
                                subtitle = "Dapatkan info rilis terbaru dari daftar tontonmu",
                                checked = settings.episodeNotifications,
                                onCheckedChange = { checked ->
                                    onUpdateSettings { it.copy(episodeNotifications = checked) }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // Soft Elevated Bottom Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ObsidianElevated,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isFromProfile) {
                        GoldOutlinedButton(
                            text = "Lewati",
                            onClick = onComplete,
                            modifier = Modifier.weight(0.65f),
                            height = 48.dp
                        )
                    }
                    GoldGradientButton(
                        text = if (isFromProfile) "Simpan Tema" else "Mulai Menonton",
                        icon = Icons.AutoMirrored.Rounded.ArrowForward,
                        onClick = onComplete,
                        modifier = Modifier.weight(1.35f),
                        height = 48.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveThemePreviewCard(
    appearanceMode: AppAppearanceMode,
    accentTheme: AppAccentTheme,
    quality: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ObsidianCard,
        shadowElevation = if (IsDarkTheme) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(ObsidianGoldCardBrush)
                .border(1.dp, GoldBorder, RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimWatchCrestLogo(size = 34.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pratinjau Langsung",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldPrimary
                        )
                        Text(
                            text = "${appearanceMode.label} • ${accentTheme.displayName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
                GoldTagChip(text = "$quality • $subtitle", highlighted = true)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoldGradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = TextOnGold,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LedStatusDot(color = GoldPrimary, pulsing = true, size = 6.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Siap ditonton",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Kage no Ou • Episode 18",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AnalogMeterGauge(
                        progress = 0.65f,
                        height = 5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun SelectablePrefChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (selected) GoldGradientBrush
                else Brush.linearGradient(listOf(ObsidianCardAlt, ObsidianCardAlt))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) TextOnGold else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OnboardingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextOnGold,
                checkedTrackColor = GoldPrimary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = ObsidianCardAlt,
                uncheckedBorderColor = GoldSubtleBorder
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeOnboardingPreview() {
    AnimWatchTheme(
        appearanceMode = AppAppearanceMode.LIGHT,
        accentTheme = AppAccentTheme.COBALT_SAPPHIRE
    ) {
        ThemeOnboardingScreen(
            userName = "Adrian Vallenz",
            appearanceMode = AppAppearanceMode.LIGHT,
            accentTheme = AppAccentTheme.COBALT_SAPPHIRE,
            preferredGenres = setOf("Action", "Sci-Fi", "Fantasy"),
            availableGenres = listOf("Action", "Fantasy", "Sci-Fi", "Romance", "Isekai", "Mystery"),
            settings = PlaybackSettings(),
            isGridCatalogPreferred = true,
            isFromProfile = false,
            onSelectAppearance = {},
            onSelectAccent = {},
            onTogglePreferredGenre = {},
            onUpdateSettings = {},
            onSetGridCatalogPreferred = {},
            onComplete = {}
        )
    }
}
