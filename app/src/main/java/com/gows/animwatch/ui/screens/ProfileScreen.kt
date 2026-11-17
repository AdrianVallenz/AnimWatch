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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.PlaybackSettings
import com.gows.animwatch.data.model.SubtitleSizeOption
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldTagChip
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
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianGoldCardBrush
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusEmerald
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
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.userProfile
    val settings = uiState.settings

    var showEditProfileDialog by remember { mutableStateOf(false) }
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
                text = "Profil & Pengaturan",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        // Luxury Royal Black & Gold Member Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(ObsidianGoldCardBrush)
                    .border(1.2.dp, GoldBorderBrush, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GoldGradientBrush)
                            .border(2.dp, ObsidianVoid, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.avatarEmblem,
                            color = TextOnGold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GoldTagChip(text = profile.memberTier, highlighted = true)
                        }
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldTint)
                            .border(1.dp, GoldBorder, CircleShape)
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
                    ProfileStatItem("${profile.watchedHours}j", "Jam Tonton")
                    ProfileStatItem("${uiState.watchlist.size}", "Koleksi")
                    ProfileStatItem("${uiState.downloads.size}", "Unduhan")
                    ProfileStatItem("${profile.completedAnimeCount}", "Tamat")
                }
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

        // API Readiness Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Koneksi API Streaming",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusEmerald)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "UI Preview",
                            color = StatusEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saat ini menggunakan MockAnimeRepository untuk pratinjau penuh UI/UX. Arsitektur repository sudah siap disambungkan ke REST API animeindo-api.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Storage & About
        item {
            SettingsGroupCard(title = "Aplikasi & Penyimpanan") {
                SettingRowItem(
                    icon = Icons.Rounded.CleaningServices,
                    title = "Bersihkan Cache Gambar & Buffer",
                    value = "${uiState.cacheSizeMb} MB",
                    onClick = onClearCache
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Info,
                    title = "Tentang AnimWatch",
                    value = "v1.0.0 Royal",
                    onClick = { showAboutDialog = true }
                )
            }
        }
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

    if (showQualityDialog) {
        OptionPickerDialog(
            title = "Pilih Kualitas Default",
            options = listOf("1080p", "720p", "480p", "360p"),
            selected = settings.defaultQuality,
            onSelect = { q -> onUpdateSettings { it.copy(defaultQuality = q) } },
            onDismiss = { showQualityDialog = false }
        )
    }

    if (showServerDialog) {
        OptionPickerDialog(
            title = "Pilih Server Default",
            options = listOf("SankaStream", "KuroCDN", "DriveMirror", "VidFast HLS"),
            selected = settings.defaultServer,
            onSelect = { srv -> onUpdateSettings { it.copy(defaultServer = srv) } },
            onDismiss = { showServerDialog = false }
        )
    }

    if (showSubtitleLangDialog) {
        OptionPickerDialog(
            title = "Bahasa Subtitle Default",
            options = listOf("Indonesia", "English", "Nonaktif"),
            selected = settings.defaultSubtitle,
            onSelect = { sub -> onUpdateSettings { it.copy(defaultSubtitle = sub) } },
            onDismiss = { showSubtitleLangDialog = false }
        )
    }

    if (showSubtitleSizeDialog) {
        OptionPickerDialog(
            title = "Ukuran Teks Subtitle",
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
            titleContentColor = GoldBright,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = GoldBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AnimWatch Royal Edition")
                }
            },
            text = {
                Text(
                    "AnimWatch adalah aplikasi streaming anime modern bertema Obsidian Black & Champagne Gold. Dibangun dengan Jetpack Compose dan arsitektur modular yang siap diintegrasikan dengan backend REST API."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup", color = GoldBright)
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
            color = GoldBright,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp
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
            text = title.uppercase(),
            color = GoldPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(ObsidianCard)
                .border(1.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(20.dp)
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
            style = MaterialTheme.typography.labelLarge,
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(20.dp)
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
        titleContentColor = GoldBright,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) GoldTintStrong else ObsidianCard)
                            .border(
                                1.dp,
                                if (isSelected) GoldBright else GoldSubtleBorder,
                                RoundedCornerShape(12.dp)
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
                            color = if (isSelected) GoldBright else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GoldBright)
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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldBright,
        unfocusedBorderColor = GoldBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = GoldBright,
        unfocusedLabelColor = TextSecondary,
        cursorColor = GoldBright
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        titleContentColor = GoldBright,
        title = { Text("Edit Profil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Tampilan") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emblem,
                    onValueChange = { if (it.length <= 2) emblem = it.uppercase() },
                    label = { Text("Inisial Avatar (2 huruf)") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio Singkat") },
                    maxLines = 3,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GoldGradientButton(
                text = "Simpan",
                onClick = {
                    onSave(name, bio, emblem)
                    onDismiss()
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
