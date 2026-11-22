package com.gows.animwatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Api
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.ApiConnectionStatus
import com.gows.animwatch.data.model.PlaybackSettings
import com.gows.animwatch.data.model.SubtitleSizeOption
import com.gows.animwatch.ui.components.CassetteLinearShimmer
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.LedStatusDot
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBorderBrush
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.MonoFont
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianGoldCardBrush
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusAmber
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

@Composable
fun ProfileScreen(
    uiState: AnimWatchUiState,
    bottomInsetPadding: Dp,
    onUpdateProfile: (String, String, String) -> Unit,
    onUpdateSettings: ((PlaybackSettings) -> PlaybackSettings) -> Unit,
    onUpdateSubtitleSize: (SubtitleSizeOption) -> Unit,
    onUpdateApiBaseUrl: (String) -> Unit,
    onSetUseLiveApi: (Boolean) -> Unit,
    onSyncApi: () -> Unit,
    onOpenThemeEditor: () -> Unit,
    onClearCache: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.userProfile
    val settings = uiState.settings
    val apiConfig = uiState.apiConfig

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showApiUrlDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var showSubtitleLangDialog by remember { mutableStateOf(false) }
    var showSubtitleSizeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = bottomInsetPadding
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "PROFIL // KONFIGURASI",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        // Luxury Royal Black & Gold Member Cassette Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianGoldCardBrush)
                    .border(1.5.dp, GoldBorderBrush, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldGradientBrush)
                            .border(1.5.dp, GoldBright, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.avatarEmblem,
                            fontFamily = MonoFont,
                            color = TextOnGold,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        GoldTagChip(text = profile.memberTier, highlighted = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = profile.handle,
                            fontFamily = MonoFont,
                            fontSize = 11.sp,
                            color = GoldPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldTint)
                            .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                            .clickable { showEditProfileDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Profil",
                            tint = GoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (profile.bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                HorizontalDivider(
                    color = GoldSubtleBorder,
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileStatItem("${profile.watchedHours}J", "JAM TONTON")
                    ProfileStatItem("${uiState.watchlist.size}", "KOLEKSI")
                    ProfileStatItem("${uiState.downloads.size}", "UNDUHAN")
                    ProfileStatItem("${profile.completedAnimeCount}", "TAMAT")
                }
            }
        }

        // Theme & Appearance Quick Access Card
        item {
            SettingsGroupCard(title = "TEMA & TAMPILAN") {
                SettingRowItem(
                    icon = Icons.Rounded.Palette,
                    title = "Ubah Mode & Warna Tema",
                    value = "${uiState.appearanceMode.code} • ${uiState.accentTheme.displayName}",
                    onClick = onOpenThemeEditor
                )
            }
        }

        // Playback Preferences
        item {
            SettingsGroupCard(title = "PREFERENSI STREAMING") {
                SettingRowItem(
                    icon = Icons.Rounded.HighQuality,
                    title = "Kualitas Video Default",
                    value = settings.defaultQuality,
                    onClick = { showQualityDialog = true }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Dns,
                    title = "Server Mirror Utama",
                    value = settings.defaultServer,
                    onClick = { showServerDialog = true }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Subtitles,
                    title = "Bahasa Subtitle",
                    value = settings.defaultSubtitle,
                    onClick = { showSubtitleLangDialog = true }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.ClosedCaption,
                    title = "Ukuran Teks Subtitle",
                    value = settings.subtitleSize.label,
                    onClick = { showSubtitleSizeDialog = true }
                )
                SettingDivider()
                SettingSwitchItem(
                    icon = Icons.Rounded.PlayCircleOutline,
                    title = "Putar Episode Berikutnya Otomatis",
                    subtitle = "Hitung mundur 5 detik di akhir episode",
                    checked = settings.autoPlayNext,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(autoPlayNext = checked) }
                    }
                )
                SettingDivider()
                SettingSwitchItem(
                    icon = Icons.Rounded.FastForward,
                    title = "Lewati Intro Otomatis",
                    subtitle = "Langsung lewati lagu pembuka episode",
                    checked = settings.autoSkipIntro,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(autoSkipIntro = checked) }
                    }
                )
            }
        }

        // Downloads & Notifications
        item {
            SettingsGroupCard(title = "UNDUHAN & NOTIFIKASI") {
                SettingSwitchItem(
                    icon = Icons.Rounded.Wifi,
                    title = "Unduh Hanya via Wi-Fi",
                    subtitle = "Hemat kuota data seluler saat mengunduh",
                    checked = settings.wifiOnlyDownloads,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(wifiOnlyDownloads = checked) }
                    }
                )
                SettingDivider()
                SettingSwitchItem(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Notifikasi Rilis Episode Baru",
                    subtitle = "Beri tahu saat anime di Daftar Tonton rilis",
                    checked = settings.episodeNotifications,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(episodeNotifications = checked) }
                    }
                )
            }
        }

        // Interactive Kumanime API Terminal Card
        item {
            val ledColor = when (apiConfig.status) {
                ApiConnectionStatus.CONNECTED -> StatusEmerald
                ApiConnectionStatus.SYNCING -> GoldBright
                ApiConnectionStatus.OFFLINE_FALLBACK -> StatusAmber
                ApiConnectionStatus.DEMO_LOCAL -> TextSecondary
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Api,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TERMINAL KUMANIME API",
                            fontFamily = MonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LedStatusDot(
                            color = ledColor,
                            pulsing = apiConfig.status == ApiConnectionStatus.SYNCING ||
                                apiConfig.status == ApiConnectionStatus.CONNECTED,
                            size = 7.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = apiConfig.status.code,
                            fontFamily = MonoFont,
                            color = ledColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianVoid)
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "> BASE_URL : ${apiConfig.baseUrl}",
                        fontFamily = MonoFont,
                        fontSize = 10.sp,
                        color = GoldBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "> ADAPTER  : MastayY/kumanime-api (Retrofit)",
                        fontFamily = MonoFont,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "> STATUS   : ${apiConfig.lastSyncMessage}",
                        fontFamily = MonoFont,
                        fontSize = 10.sp,
                        color = TextTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (apiConfig.status == ApiConnectionStatus.SYNCING) {
                        Spacer(modifier = Modifier.height(4.dp))
                        CassetteLinearShimmer(modifier = Modifier.fillMaxWidth())
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Aktifkan Kumanime Live API",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Switch(
                        checked = apiConfig.useLiveApi,
                        onCheckedChange = onSetUseLiveApi,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextOnGold,
                            checkedTrackColor = GoldBright,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = ObsidianVoid,
                            uncheckedBorderColor = GoldBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoldOutlinedButton(
                        text = "Ubah URL",
                        icon = Icons.Rounded.Edit,
                        onClick = { showApiUrlDialog = true },
                        modifier = Modifier.weight(1f),
                        height = 40.dp
                    )
                    GoldGradientButton(
                        text = "Sinkronkan",
                        icon = Icons.Rounded.Refresh,
                        onClick = onSyncApi,
                        modifier = Modifier.weight(1f),
                        height = 40.dp
                    )
                }
            }
        }

        // Storage & About
        item {
            SettingsGroupCard(title = "SISTEM & PENYIMPANAN") {
                SettingRowItem(
                    icon = Icons.Rounded.CleaningServices,
                    title = "Bersihkan Cache Buffer",
                    value = "${uiState.cacheSizeMb} MB",
                    onClick = onClearCache
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Info,
                    title = "Tentang AnimWatch",
                    value = "v1.0.0 DECK",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        // Logout / Switch Account Action
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusLive.copy(alpha = 0.14f))
                    .border(1.2.dp, StatusLive.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .clickable {
                        if (uiState.isGuestSession) onLogout() else showLogoutDialog = true
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    tint = StatusLive,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isGuestSession) "MASUK / DAFTAR AKUN" else "KELUAR DARI AKUN",
                    fontFamily = MonoFont,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusLive,
                    letterSpacing = 0.7.sp
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = ObsidianElevated,
            shape = RoundedCornerShape(10.dp),
            titleContentColor = GoldBright,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = "KONFIRMASI KELUAR",
                    fontFamily = MonoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Text("Kamu akan kembali ke halaman Masuk / Daftar. Daftar tonton dan riwayat lokal tetap tersimpan di sesi ini.")
            },
            confirmButton = {
                GoldGradientButton(
                    text = "Keluar",
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    height = 38.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("BATAL", fontFamily = MonoFont, color = TextSecondary)
                }
            }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = profile.displayName,
            currentBio = profile.bio,
            currentEmblem = profile.avatarEmblem,
            onSave = onUpdateProfile,
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (showApiUrlDialog) {
        EditApiBaseUrlDialog(
            currentUrl = apiConfig.baseUrl,
            onSave = onUpdateApiBaseUrl,
            onDismiss = { showApiUrlDialog = false }
        )
    }

    if (showQualityDialog) {
        OptionPickerDialog(
            title = "KUALITAS DEFAULT",
            options = listOf("1080p", "720p", "480p", "360p"),
            selected = settings.defaultQuality,
            onSelect = { q -> onUpdateSettings { it.copy(defaultQuality = q) } },
            onDismiss = { showQualityDialog = false }
        )
    }

    if (showServerDialog) {
        OptionPickerDialog(
            title = "SERVER DEFAULT",
            options = listOf("SankaStream", "KuroCDN", "DriveMirror", "VidFast HLS"),
            selected = settings.defaultServer,
            onSelect = { srv -> onUpdateSettings { it.copy(defaultServer = srv) } },
            onDismiss = { showServerDialog = false }
        )
    }

    if (showSubtitleLangDialog) {
        OptionPickerDialog(
            title = "BAHASA SUBTITLE",
            options = listOf("Indonesia", "English", "Nonaktif"),
            selected = settings.defaultSubtitle,
            onSelect = { sub -> onUpdateSettings { it.copy(defaultSubtitle = sub) } },
            onDismiss = { showSubtitleLangDialog = false }
        )
    }

    if (showSubtitleSizeDialog) {
        OptionPickerDialog(
            title = "UKURAN SUBTITLE",
            options = SubtitleSizeOption.entries.map { it.label },
            selected = settings.subtitleSize.label,
            onSelect = { label ->
                SubtitleSizeOption.entries.find { it.label == label }?.let(onUpdateSubtitleSize)
            },
            onDismiss = { showSubtitleSizeDialog = false }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = ObsidianElevated,
            shape = RoundedCornerShape(10.dp),
            titleContentColor = GoldBright,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = GoldBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "ANIMWATCH // STREAM DECK",
                        fontFamily = MonoFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    "AnimWatch adalah aplikasi streaming anime bergaya Cassette Futurism dengan dukungan Mode Gelap & Terang serta pilihan warna tema personal. Terintegrasi dengan adapter REST API kumanime-api."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("TUTUP", fontFamily = MonoFont, color = GoldBright)
                }
            }
        )
    }
}

@Composable
private fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = MonoFont,
            color = GoldBright,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            fontFamily = MonoFont,
            color = TextSecondary,
            fontSize = 9.5.sp
        )
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = MonoFont,
            color = GoldPrimary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianCard)
                .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingRowItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontFamily = MonoFont,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBright
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingSwitchItem(
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
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                style = MaterialTheme.typography.bodyLarge,
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
                checkedTrackColor = GoldBright,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = ObsidianVoid,
                uncheckedBorderColor = GoldBorder
            )
        )
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(horizontal = 14.dp))
}

@Composable
private fun OptionPickerDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        titleContentColor = GoldBright,
        title = {
            Text(
                title,
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GoldTintStrong else ObsidianCard)
                            .border(
                                1.dp,
                                if (isSelected) GoldBright else GoldSubtleBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelect(option)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            fontFamily = MonoFont,
                            color = if (isSelected) GoldBright else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            LedStatusDot(color = GoldBright, pulsing = false, size = 7.dp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("TUTUP", fontFamily = MonoFont, color = TextSecondary)
            }
        }
    )
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    currentEmblem: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var emblem by remember { mutableStateOf(currentEmblem) }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        titleContentColor = GoldBright,
        title = {
            Text(
                "EDIT PROFIL",
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TactileInputField(
                    label = "Nama Tampilan",
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isBlank()) "Nama tampilan wajib diisi" else null
                    },
                    placeholder = "Masukkan nama...",
                    errorText = nameError
                )
                TactileInputField(
                    label = "Inisial Avatar (Maks 2 Huruf)",
                    value = emblem,
                    onValueChange = { if (it.length <= 2) emblem = it.uppercase() },
                    placeholder = "AV"
                )
                TactileInputField(
                    label = "Bio Singkat",
                    value = bio,
                    onValueChange = { bio = it },
                    placeholder = "Tuliskan deskripsi singkat...",
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            GoldGradientButton(
                text = "Simpan",
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Nama tampilan wajib diisi"
                    } else {
                        onSave(name, bio, emblem)
                        onDismiss()
                    }
                },
                height = 40.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("BATAL", fontFamily = MonoFont, color = TextSecondary)
            }
        }
    )
}

@Composable
private fun EditApiBaseUrlDialog(
    currentUrl: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }
    var urlError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        titleContentColor = GoldBright,
        title = {
            Text(
                "KONFIGURASI KUMANIME API",
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Masukkan Base URL server kumanime-api (lokal Emulator gunakan 10.0.2.2:3000 atau URL deploy Vercel/Railway).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                TactileInputField(
                    label = "Base URL Server",
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        urlError = if (it.isBlank()) "Base URL tidak boleh kosong" else null
                    },
                    placeholder = "http://10.0.2.2:3000/api/",
                    errorText = urlError
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldTint)
                            .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                urlInput = "http://10.0.2.2:3000/api/"
                                urlError = null
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "EMULATOR LOKAL",
                            fontFamily = MonoFont,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldBright
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                urlInput = "http://192.168.1.10:3000/api/"
                                urlError = null
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "IP WI-FI LAN",
                            fontFamily = MonoFont,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            GoldGradientButton(
                text = "Hubungkan",
                onClick = {
                    if (urlInput.isBlank()) {
                        urlError = "Base URL tidak boleh kosong"
                    } else {
                        onSave(urlInput)
                        onDismiss()
                    }
                },
                height = 40.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("BATAL", fontFamily = MonoFont, color = TextSecondary)
            }
        }
    )
}

