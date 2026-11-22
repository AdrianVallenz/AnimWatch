package com.gows.animwatch.ui.screens

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
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.gows.animwatch.ui.components.CassetteTelemetryStrip
import com.gows.animwatch.ui.components.CrtScanlineOverlay
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.LedStatusDot
import com.gows.animwatch.ui.theme.AnimWatchTheme
import com.gows.animwatch.ui.theme.AppAccentTheme
import com.gows.animwatch.ui.theme.AppAppearanceMode
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBorderBrush
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.MonoFont
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
        CrtScanlineOverlay(
            modifier = Modifier.fillMaxSize(),
            lineAlpha = 0.06f,
            stepPx = 6f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Scrollable Customization Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFromProfile && onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianCard)
                                .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Kembali",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    CassetteTelemetryStrip(
                        code = if (isFromProfile) "SYS.THEME // UBAH TAMPILAN" else "SYS.SETUP // TEMA & PREFERENSI",
                        statusText = "${appearanceMode.code} • ${accentTheme.code}",
                        ledColor = GoldBright,
                        pulsing = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Greeting Title
                Column {
                    Text(
                        text = if (isFromProfile) "Atur Tema & Preferensi" else "Halo, $userName",
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih mode gelap atau terang, warna tema kesukaanmu, serta preferensi menonton sebelum masuk ke Beranda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Live Theme Preview Mini-Deck
                LiveThemePreviewCard(
                    appearanceMode = appearanceMode,
                    accentTheme = accentTheme,
                    quality = settings.defaultQuality,
                    subtitle = settings.defaultSubtitle
                )

                // ============================
                // SECTION 1: GELAP / TERANG
                // ============================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeSectionTitle(
                        code = "01 // MODE LAYAR",
                        title = "Pilih Mode Gelap atau Terang"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppAppearanceMode.entries.forEach { mode ->
                            val selected = mode == appearanceMode
                            val previewBg = if (mode == AppAppearanceMode.DARK) Color(0xFF0D0F14) else Color(0xFFF3F5FA)
                            val previewCard = if (mode == AppAppearanceMode.DARK) Color(0xFF202533) else Color(0xFFFFFFFF)
                            val previewAccent = if (mode == AppAppearanceMode.DARK) accentTheme.darkPrimary else accentTheme.lightPrimary

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) GoldTintStrong else ObsidianCard)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) GoldPrimary else GoldSubtleBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectAppearance(mode) }
                                    .padding(12.dp)
                            ) {
                                // Mini Dark/Light visual mock bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(previewBg)
                                        .border(1.dp, previewAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(previewAccent)
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.75f)
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(previewCard)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(previewAccent.copy(alpha = 0.6f))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

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
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.titleSmall,
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

                // ============================
                // SECTION 2: WARNA TEMA (7 PILIHAN, TANPA EMAS)
                // ============================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeSectionTitle(
                        code = "02 // WARNA AKSEN",
                        title = "Pilih Warna Tema Kesukaanmu"
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

                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) swatchPrimary.copy(alpha = 0.16f) else ObsidianCard)
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) swatchPrimary else GoldSubtleBorder,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSelectAccent(theme) }
                                            .padding(11.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    Brush.linearGradient(listOf(swatchBright, swatchPrimary))
                                                )
                                                .border(1.dp, swatchBright.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
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
                                                color = if (selected) swatchPrimary else TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = theme.tagline,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextTertiary,
                                                fontSize = 10.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
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
                        code = "03 // PREFERENSI",
                        title = "Sesuaikan Pengalaman Menonton"
                    )

                    // Genre Favorit Multi-Select
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GENRE FAVORIT",
                                fontFamily = MonoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldPrimary
                            )
                            Text(
                                text = "${preferredGenres.size} DIPILIH",
                                fontFamily = MonoFont,
                                fontSize = 10.sp,
                                color = TextTertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            genreChoices.forEach { genre ->
                                val selected = genre in preferredGenres
                                SelectablePrefChip(
                                    label = genre,
                                    selected = selected,
                                    onClick = { onTogglePreferredGenre(genre) }
                                )
                            }
                        }
                    }

                    // Default Quality, Subtitle, & Layout Mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "KUALITAS VIDEO DEFAULT",
                                fontFamily = MonoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.height(7.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("1080p", "720p", "480p", "360p").forEach { q ->
                                    SelectablePrefChip(
                                        label = q,
                                        selected = settings.defaultQuality == q,
                                        onClick = { onUpdateSettings { it.copy(defaultQuality = q) } },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = DividerSubtle)

                        Column {
                            Text(
                                text = "BAHASA SUBTITLE DEFAULT",
                                fontFamily = MonoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.height(7.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Indonesia", "English", "Nonaktif").forEach { sub ->
                                    SelectablePrefChip(
                                        label = sub,
                                        selected = settings.defaultSubtitle == sub,
                                        onClick = { onUpdateSettings { it.copy(defaultSubtitle = sub) } },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = DividerSubtle)

                        Column {
                            Text(
                                text = "TAMPILAN KATALOG JELAJAH",
                                fontFamily = MonoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.height(7.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SelectablePrefChip(
                                    label = "Grid Asimetris",
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

                    // Smart Playback Toggles
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
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
                        HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(horizontal = 14.dp))
                        OnboardingToggleRow(
                            icon = Icons.Rounded.FastForward,
                            title = "Lewati Intro Otomatis",
                            subtitle = "Langsung lewati lagu pembuka episode",
                            checked = settings.autoSkipIntro,
                            onCheckedChange = { checked ->
                                onUpdateSettings { it.copy(autoSkipIntro = checked) }
                            }
                        )
                        HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(horizontal = 14.dp))
                        OnboardingToggleRow(
                            icon = Icons.Rounded.Wifi,
                            title = "Unduh Hanya via Wi-Fi",
                            subtitle = "Hemat kuota data seluler saat mengunduh",
                            checked = settings.wifiOnlyDownloads,
                            onCheckedChange = { checked ->
                                onUpdateSettings { it.copy(wifiOnlyDownloads = checked) }
                            }
                        )
                        HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(horizontal = 14.dp))
                        OnboardingToggleRow(
                            icon = Icons.Rounded.NotificationsActive,
                            title = "Notifikasi Episode Baru",
                            subtitle = "Info rilis terbaru dari daftar tontonmu",
                            checked = settings.episodeNotifications,
                            onCheckedChange = { checked ->
                                onUpdateSettings { it.copy(episodeNotifications = checked) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // Sticky Bottom CTA Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianElevated)
                    .border(1.dp, GoldSubtleBorder)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isFromProfile) {
                    GoldOutlinedButton(
                        text = "Lewati",
                        onClick = onComplete,
                        modifier = Modifier.weight(0.65f),
                        height = 46.dp
                    )
                }
                GoldGradientButton(
                    text = if (isFromProfile) "Simpan Tema" else "Mulai Menonton",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = onComplete,
                    modifier = Modifier.weight(1.35f),
                    height = 46.dp
                )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianGoldCardBrush)
            .border(1.5.dp, GoldBorderBrush, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimWatchCrestLogo(size = 30.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PRATINJAU LANGSUNG",
                        fontFamily = MonoFont,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = "${appearanceMode.label} • ${accentTheme.displayName}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }
            }
            GoldTagChip(text = "$quality • $subtitle", highlighted = true)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianCard)
                .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
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
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LedStatusDot(color = GoldPrimary, pulsing = true, size = 6.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SIAP STREAMING",
                        fontFamily = MonoFont,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
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
                Spacer(modifier = Modifier.height(5.dp))
                AnalogMeterGauge(
                    progress = 0.65f,
                    segments = 16,
                    height = 6.dp
                )
            }
        }
    }
}

@Composable
private fun ThemeSectionTitle(
    code: String,
    title: String
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = code,
                fontFamily = MonoFont,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                letterSpacing = 0.9.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
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
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (selected) GoldGradientBrush
                else Brush.linearGradient(listOf(ObsidianCardAlt, ObsidianCardAlt))
            )
            .border(
                1.dp,
                if (selected) GoldPrimary else GoldSubtleBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = MonoFont,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
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
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
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
                uncheckedTrackColor = ObsidianVoid,
                uncheckedBorderColor = GoldBorder
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeOnboardingPreview() {
    AnimWatchTheme(
        appearanceMode = AppAppearanceMode.DARK,
        accentTheme = AppAccentTheme.COBALT_SAPPHIRE
    ) {
        ThemeOnboardingScreen(
            userName = "Adrian Vallenz",
            appearanceMode = AppAppearanceMode.DARK,
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
