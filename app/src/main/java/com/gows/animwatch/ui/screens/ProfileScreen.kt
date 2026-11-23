package com.gows.animwatch.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Api
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.local.AndroidDeviceStorageManager
import com.gows.animwatch.data.model.ApiConnectionStatus
import com.gows.animwatch.data.model.PlaybackSettings
import com.gows.animwatch.data.model.SubtitleSizeOption
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.ui.components.CassetteLinearShimmer
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.LedStatusDot
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
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
    onRefreshDeviceState: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile = uiState.userProfile
    val settings = uiState.settings
    val apiConfig = uiState.apiConfig
    val deviceStorage = uiState.deviceStorage

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshDeviceState()
    }

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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        // Smooth Member Profile Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = ObsidianCard,
                shadowElevation = if (IsDarkTheme) 0.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(ObsidianGoldCardBrush)
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(GoldGradientBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.avatarEmblem,
                                color = TextOnGold,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
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
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldTint)
                                .clickable { showEditProfileDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Profil",
                                tint = GoldPrimary,
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
                        color = DividerSubtle,
                        modifier = Modifier.padding(vertical = 15.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val totalTrackedEps = uiState.watchedEpisodes.values.sum()
                        val completedCount = uiState.watchlist.values.count { it == WatchlistCategory.COMPLETED }
                        ProfileStatItem("$totalTrackedEps", "Ep Ditonton")
                        ProfileStatItem("${uiState.watchlist.size}", "Koleksi")
                        ProfileStatItem("${uiState.downloads.size}", "Offline")
                        ProfileStatItem("$completedCount", "Tamat")
                    }
                }
            }
        }

        // Theme & Appearance Quick Access Card
        item {
            SettingsGroupCard(title = "Tema & Tampilan") {
                SettingRowItem(
                    icon = Icons.Rounded.Palette,
                    title = "Mode & Warna Tema",
                    value = "${uiState.appearanceMode.label} • ${uiState.accentTheme.displayName}",
                    onClick = onOpenThemeEditor
                )
            }
        }

        // Playback Preferences
        item {
            SettingsGroupCard(title = "Preferensi Streaming") {
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
            SettingsGroupCard(title = "Unduhan & Notifikasi") {
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

        // Clean Organic Kumanime API Card
        item {
            val ledColor = when (apiConfig.status) {
                ApiConnectionStatus.CONNECTED -> StatusEmerald
                ApiConnectionStatus.SYNCING -> GoldBright
                ApiConnectionStatus.OFFLINE_FALLBACK -> StatusAmber
                ApiConnectionStatus.DEMO_LOCAL -> TextSecondary
            }
            val friendlyStatus = when (apiConfig.status) {
                ApiConnectionStatus.CONNECTED -> "Terhubung"
                ApiConnectionStatus.SYNCING -> "Menyinkronkan"
                ApiConnectionStatus.OFFLINE_FALLBACK -> "Offline"
                ApiConnectionStatus.DEMO_LOCAL -> "Lokal"
            }

            Column {
                Text(
                    text = "Sumber Streaming",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(GoldTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Api,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Kumanime API",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ledColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LedStatusDot(
                                    color = ledColor,
                                    pulsing = apiConfig.status == ApiConnectionStatus.SYNCING ||
                                        apiConfig.status == ApiConnectionStatus.CONNECTED,
                                    size = 6.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = friendlyStatus,
                                    color = ledColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ObsidianCardAlt.copy(alpha = if (IsDarkTheme) 0.65f else 0.75f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Server: ${apiConfig.baseUrl}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Backend: Railway PostgreSQL + Kumanime API",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = apiConfig.lastSyncMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (apiConfig.status == ApiConnectionStatus.SYNCING) {
                                Spacer(modifier = Modifier.height(4.dp))
                                CassetteLinearShimmer(modifier = Modifier.fillMaxWidth())
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = ObsidianCardAlt,
                                    uncheckedBorderColor = GoldSubtleBorder
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GoldOutlinedButton(
                                text = "Ubah Server",
                                icon = Icons.Rounded.Edit,
                                onClick = { showApiUrlDialog = true },
                                modifier = Modifier.weight(1f),
                                height = 42.dp
                            )
                            GoldGradientButton(
                                text = "Sinkronkan",
                                icon = Icons.Rounded.Refresh,
                                onClick = onSyncApi,
                                modifier = Modifier.weight(1f),
                                height = 42.dp
                            )
                        }
                    }
                }
            }
        }

        // App Permissions & Runtime Access Control
        if (uiState.appPermissions.isNotEmpty()) {
            item {
                SettingsGroupCard(title = "Izin Penggunaan Aplikasi") {
                    uiState.appPermissions.forEachIndexed { idx, perm ->
                        if (idx > 0) SettingDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = perm.canRequestRuntime && perm.androidPermission != null) {
                                    perm.androidPermission?.let { permissionLauncher.launch(it) }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (perm.isGranted) StatusEmerald.copy(alpha = 0.14f) else GoldTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (perm.id) {
                                        "notifications" -> Icons.Rounded.NotificationsActive
                                        "storage" -> Icons.Rounded.Storage
                                        "network" -> Icons.Rounded.Wifi
                                        else -> Icons.Rounded.Security
                                    },
                                    contentDescription = null,
                                    tint = if (perm.isGranted) StatusEmerald else GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = perm.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = perm.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (perm.isGranted) StatusEmerald.copy(alpha = 0.14f)
                                        else GoldTintStrong
                                    )
                                    .clickable(enabled = perm.canRequestRuntime && perm.androidPermission != null) {
                                        perm.androidPermission?.let { permissionLauncher.launch(it) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (perm.isGranted) "Diizinkan" else "Izinkan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (perm.isGranted) StatusEmerald else GoldPrimary
                                )
                            }
                        }
                    }
                    SettingDivider()
                    SettingRowItem(
                        icon = Icons.Rounded.Settings,
                        title = "Pengaturan Izin Sistem Android",
                        value = "Buka",
                        onClick = { AndroidDeviceStorageManager.openAppPermissionSettings(context) }
                    )
                }
            }
        }

        // Storage & About
        item {
            SettingsGroupCard(title = "Aplikasi & Penyimpanan Perangkat") {
                SettingRowItem(
                    icon = Icons.Rounded.Storage,
                    title = "Ruang Kosong Perangkat",
                    value = if (deviceStorage.totalDeviceBytes > 0L) {
                        "${deviceStorage.availableLabel} / ${deviceStorage.totalLabel}"
                    } else "Memindai...",
                    onClick = onRefreshDeviceState
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.CleaningServices,
                    title = "Bersihkan Cache",
                    value = deviceStorage.cacheLabel,
                    onClick = onClearCache
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Info,
                    title = "Tentang AnimWatch",
                    value = "v1.1.0",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        // Logout / Switch Account Action
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StatusLive.copy(alpha = 0.11f))
                    .border(0.75.dp, StatusLive.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
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
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isGuestSession) "Masuk atau Daftar Akun" else "Keluar dari Akun",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusLive
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = ObsidianElevated,
            shape = RoundedCornerShape(24.dp),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = "Keluar dari Akun?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Kamu akan kembali ke halaman Masuk / Daftar. Daftar tonton dan riwayat lokal tetap tersimpan di sesi ini.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                GoldGradientButton(
                    text = "Keluar",
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    height = 40.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = TextSecondary)
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
            title = "Kualitas Default",
            options = listOf("1080p", "720p", "480p", "360p"),
            selected = settings.defaultQuality,
            onSelect = { q -> onUpdateSettings { it.copy(defaultQuality = q) } },
            onDismiss = { showQualityDialog = false }
        )
    }

    if (showServerDialog) {
        OptionPickerDialog(
            title = "Server Mirror Utama",
            options = listOf("SankaStream", "KuroCDN", "DriveMirror", "VidFast HLS"),
            selected = settings.defaultServer,
            onSelect = { srv -> onUpdateSettings { it.copy(defaultServer = srv) } },
            onDismiss = { showServerDialog = false }
        )
    }

    if (showSubtitleLangDialog) {
        OptionPickerDialog(
            title = "Bahasa Subtitle",
            options = listOf("Indonesia", "English", "Nonaktif"),
            selected = settings.defaultSubtitle,
            onSelect = { sub -> onUpdateSettings { it.copy(defaultSubtitle = sub) } },
            onDismiss = { showSubtitleLangDialog = false }
        )
    }

    if (showSubtitleSizeDialog) {
        OptionPickerDialog(
            title = "Ukuran Subtitle",
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
            shape = RoundedCornerShape(24.dp),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AnimWatch",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                Text(
                    "AnimWatch adalah aplikasi streaming anime modern dengan antarmuka lembut, dukungan Mode Gelap & Terang, pilihan warna tema personal, serta integrasi REST API kumanime-api.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup", color = GoldPrimary, fontWeight = FontWeight.SemiBold)
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
            color = GoldPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
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
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = ObsidianCard,
            shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
        ) {
            Column(
                modifier = Modifier.border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
            ) {
                content()
            }
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(GoldTint),
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
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = GoldPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(2.dp))
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(GoldTint),
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
                checkedTrackColor = GoldPrimary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = ObsidianCardAlt,
                uncheckedBorderColor = GoldSubtleBorder
            )
        )
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(horizontal = 16.dp))
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
        shape = RoundedCornerShape(24.dp),
        titleContentColor = TextPrimary,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) GoldTintStrong else ObsidianCard)
                            .border(
                                if (isSelected) 1.dp else 0.75.dp,
                                if (isSelected) GoldPrimary else GoldSubtleBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onSelect(option)
                                onDismiss()
                            }
                            .padding(horizontal = 15.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) GoldPrimary else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = TextSecondary)
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
        shape = RoundedCornerShape(24.dp),
        titleContentColor = TextPrimary,
        title = {
            Text(
                "Edit Profil",
                style = MaterialTheme.typography.titleLarge
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
                    label = "Inisial Avatar (Maks 2 huruf)",
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
                Text("Batal", color = TextSecondary)
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
        shape = RoundedCornerShape(24.dp),
        titleContentColor = TextPrimary,
        title = {
            Text(
                "Server Kumanime API",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Masukkan Base URL server kumanime-api (untuk Emulator gunakan 10.0.2.2:3000 atau URL deploy Vercel/Railway).",
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
                    placeholder = "https://kumanime-api-production.up.railway.app/api/",
                    errorText = urlError
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GoldTint)
                            .clickable {
                                urlInput = "https://kumanime-api-production.up.railway.app/api/"
                                urlError = null
                            }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Railway Live",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ObsidianCard)
                            .border(0.75.dp, GoldSubtleBorder, CircleShape)
                            .clickable {
                                urlInput = "http://10.0.2.2:3000/api/"
                                urlError = null
                            }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Emulator Lokal",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
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
                Text("Batal", color = TextSecondary)
            }
        }
    )
}
