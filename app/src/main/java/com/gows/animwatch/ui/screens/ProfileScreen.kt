package com.gows.animwatch.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gows.animwatch.data.local.AndroidDeviceStorageManager
import com.gows.animwatch.data.local.AppPermissionStatus
import com.gows.animwatch.data.local.DeviceStorageInfo
import com.gows.animwatch.data.model.PlaybackSettings
import com.gows.animwatch.data.model.SubtitleSizeOption
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.data.model.formatWatchDuration
import com.gows.animwatch.ui.components.AnalogMeterGauge
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.theme.DividerSubtle
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
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

private data class AnimeAvatarPreset(
    val label: String,
    val url: String
)

private val ANIME_AVATAR_PRESETS = listOf(
    AnimeAvatarPreset("Frieren", "https://s4.anilist.co/file/anilistcdn/character/large/b176754-PCnpqIOkjhFk.png"),
    AnimeAvatarPreset("Gojo Satoru", "https://s4.anilist.co/file/anilistcdn/character/large/b127691-9zqh1xpIubn7.png"),
    AnimeAvatarPreset("Sung Jinwoo", "https://s4.anilist.co/file/anilistcdn/character/large/b129928-BCEjVaP0AQSw.png"),
    AnimeAvatarPreset("Anya Forger", "https://s4.anilist.co/file/anilistcdn/character/large/b138100-4Li0tWRCa5bQ.png"),
    AnimeAvatarPreset("Rimuru", "https://s4.anilist.co/file/anilistcdn/character/large/b123962-eL9yGV0NLMF7.png"),
    AnimeAvatarPreset("Ayanokoji", "https://s4.anilist.co/file/anilistcdn/character/large/b123212-ewZgUQr9vvEM.png"),
    AnimeAvatarPreset("Maomao", "https://s4.anilist.co/file/anilistcdn/character/large/b126824-MqsCncTO1qpv.png"),
    AnimeAvatarPreset("Killua", "https://s4.anilist.co/file/anilistcdn/character/large/b27-Z5O02kQUydpT.jpg"),
    AnimeAvatarPreset("Luffy", "https://s4.anilist.co/file/anilistcdn/character/large/b40-MNypXsxSRb1R.png"),
    AnimeAvatarPreset("Levi", "https://s4.anilist.co/file/anilistcdn/character/large/b45627-CR68RyZmddGG.png"),
    AnimeAvatarPreset("Tanjiro", "https://s4.anilist.co/file/anilistcdn/character/large/b126071-BTNEc1nRIv68.png"),
    AnimeAvatarPreset("Fern", "https://s4.anilist.co/file/anilistcdn/character/large/b183965-uGFohBjlFoTp.png")
)

private val LEGACY_AVATAR_URL_FIXES = mapOf(
    "https://s4.anilist.co/file/anilistcdn/character/large/b176754-Ya46QWtQuXzQ.png" to "https://s4.anilist.co/file/anilistcdn/character/large/b176754-PCnpqIOkjhFk.png",
    "https://s4.anilist.co/file/anilistcdn/character/large/b127691-UmP85236325F.png" to "https://s4.anilist.co/file/anilistcdn/character/large/b127691-9zqh1xpIubn7.png",
    "https://s4.anilist.co/file/anilistcdn/character/large/b129928-BCEj21b2217k.png" to "https://s4.anilist.co/file/anilistcdn/character/large/b129928-BCEjVaP0AQSw.png",
    "https://s4.anilist.co/file/anilistcdn/character/large/b138100-3B2V93T1R48u.png" to "https://s4.anilist.co/file/anilistcdn/character/large/b138100-4Li0tWRCa5bQ.png",
    "https://s4.anilist.co/file/anilistcdn/character/large/b124051-C4pM2T7kL13W.png" to "https://s4.anilist.co/file/anilistcdn/character/large/b123962-eL9yGV0NLMF7.png",
    "https://s4.anilist.co/file/anilistcdn/character/large/b88750-24136x8dO04r.png" to "https://s4.anilist.co/file/anilistcdn/character/large/b123212-ewZgUQr9vvEM.png",
    "https://s4.anilist.co/file/anilistcdn/character/large/b160602-H5y11C9nO35K.png" to "https://s4.anilist.co/file/anilistcdn/character/large/b126824-MqsCncTO1qpv.png",
    "https://s4.anilist.co/file/anilistcdn/character/large/b27-Z91v6776118z.png" to "https://s4.anilist.co/file/anilistcdn/character/large/b27-Z5O02kQUydpT.jpg"
)

fun normalizeProfileAvatarUrl(raw: String): String {
    val trimmed = raw.trim()
    return LEGACY_AVATAR_URL_FIXES[trimmed] ?: trimmed
}

private val MEMBER_BADGE_OPTIONS = listOf(
    "Google Member",
    "Wibu Sejati",
    "Otaku Legend",
    "Marathoner",
    "Sub Indo Enthusiast",
    "Dub Indo Enjoyer",
    "Reviewer Anime"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: AnimWatchUiState,
    bottomInsetPadding: Dp,
    onUpdateProfile: (
        displayName: String,
        handle: String,
        bio: String,
        emblem: String,
        avatarUrl: String,
        memberTier: String,
        favoriteGenre: String
    ) -> Unit,
    onUpdateSettings: ((PlaybackSettings) -> PlaybackSettings) -> Unit,
    onUpdateSubtitleSize: (SubtitleSizeOption) -> Unit,
    onUpdateApiBaseUrl: (String) -> Unit,
    onSetUseLiveApi: (Boolean) -> Unit,
    onSyncApi: () -> Unit,
    onOpenThemeEditor: () -> Unit,
    onClearCache: () -> Unit,
    onRefreshDeviceState: () -> Unit,
    onOpenAdminPanel: () -> Unit = {},
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

    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showPlaybackSheet by remember { mutableStateOf(false) }
    var showStoragePermissionsSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showApiUrlDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showSubtitleLangDialog by remember { mutableStateOf(false) }
    var showSubtitleSizeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = bottomInsetPadding
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        // 1. Clean Member Summary Card (single Edit button, clear 3-stat strip)
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
                        UserAvatarBadge(
                            avatarUrl = profile.avatarUrl,
                            emblem = profile.avatarEmblem,
                            size = 68.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = profile.handle,
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GoldTagChip(text = profile.memberTier, highlighted = true)
                                if (uiState.isAdmin) {
                                    GoldTagChip(text = "ADMIN", highlighted = true)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        GoldOutlinedButton(
                            text = "Edit",
                            icon = Icons.Rounded.Edit,
                            onClick = { showEditProfileSheet = true },
                            height = 38.dp
                        )
                    }

                    if (profile.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val animeById = remember(uiState.allAnime) { uiState.allAnime.associateBy { it.id } }
                    val totalTrackedEps = uiState.watchedEpisodes.values.sum()
                    val visibleSavedCount = uiState.watchlist.keys.count { it in animeById }
                    val completedCount = uiState.watchlist.entries.count { (id, cat) ->
                        id in animeById && cat == WatchlistCategory.COMPLETED
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ObsidianCard.copy(alpha = 0.65f))
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(formatWatchDuration(profile.watchedSecondsTotal), "Durasi")
                        ProfileStatItem("$totalTrackedEps", "Episode")
                        ProfileStatItem("$visibleSavedCount", "Disimpan")
                        ProfileStatItem("$completedCount", "Tamat")
                    }
                }
            }
        }

        // 2. Simple Grouped Settings
        item {
            SettingsGroupCard(title = "Pengaturan") {
                SettingRowItem(
                    icon = Icons.Rounded.Palette,
                    title = "Tema & Warna",
                    value = "${uiState.appearanceMode.label} • ${uiState.accentTheme.displayName}",
                    onClick = onOpenThemeEditor
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Tune,
                    title = "Pemutar Video",
                    value = "${settings.defaultQuality} • ${settings.defaultSubtitle}",
                    onClick = { showPlaybackSheet = true }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Storage,
                    title = "Penyimpanan & Izin",
                    value = deviceStorage.cacheLabel,
                    onClick = {
                        onRefreshDeviceState()
                        showStoragePermissionsSheet = true
                    }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Security,
                    title = "Panel Admin CMS",
                    value = if (uiState.isAdmin) "Aktif" else "Kelola",
                    onClick = onOpenAdminPanel
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Rounded.Info,
                    title = "Tentang Aplikasi",
                    value = "v1.3.0",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        // 3. Account Sign Out Action
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StatusLive.copy(alpha = 0.11f))
                    .border(0.75.dp, StatusLive.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .clickable { showLogoutDialog = true }
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
                    text = if (uiState.isGuestSession) "Keluar dari Mode Tamu" else "Keluar dari Akun",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusLive
                )
            }
        }
    }

    // Bottom Sheet: Custom Profile Editor
    if (showEditProfileSheet) {
        CustomProfileEditorSheet(
            currentName = profile.displayName,
            currentHandle = profile.handle,
            currentBio = profile.bio,
            currentEmblem = profile.avatarEmblem,
            currentAvatarUrl = profile.avatarUrl,
            currentTier = profile.memberTier,
            currentFavoriteGenre = profile.favoriteGenre,
            availableGenres = uiState.genres.ifEmpty {
                listOf("Action", "Adventure", "Comedy", "Drama", "Fantasy", "Romance", "Sci-Fi", "Slice of Life")
            },
            onSave = { name, handle, bio, emblem, avatarUrl, tier, favGenre ->
                onUpdateProfile(name, handle, bio, emblem, avatarUrl, tier, favGenre)
                showEditProfileSheet = false
            },
            onDismiss = { showEditProfileSheet = false }
        )
    }

    // Bottom Sheet: Preferensi Pemutar Video
    if (showPlaybackSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlaybackSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ObsidianElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Preferensi Pemutar Video",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianCard
                ) {
                    Column(
                        modifier = Modifier.border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                    ) {
                        SettingRowItem(
                            icon = Icons.Rounded.HighQuality,
                            title = "Kualitas Default",
                            value = settings.defaultQuality,
                            onClick = { showQualityDialog = true }
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
                            title = "Ukuran Subtitle",
                            value = settings.subtitleSize.label,
                            onClick = { showSubtitleSizeDialog = true }
                        )
                        SettingDivider()
                        SettingSwitchItem(
                            icon = Icons.Rounded.PlayCircleOutline,
                            title = "Putar Episode Berikutnya",
                            subtitle = "Lanjut otomatis saat episode selesai",
                            checked = settings.autoPlayNext,
                            onCheckedChange = { checked ->
                                onUpdateSettings { it.copy(autoPlayNext = checked) }
                            }
                        )
                        SettingDivider()
                        SettingSwitchItem(
                            icon = Icons.Rounded.FastForward,
                            title = "Lewati Intro Otomatis",
                            subtitle = "Langsung lewati lagu pembuka",
                            checked = settings.autoSkipIntro,
                            onCheckedChange = { checked ->
                                onUpdateSettings { it.copy(autoSkipIntro = checked) }
                            }
                        )
                        SettingDivider()
                        SettingSwitchItem(
                            icon = Icons.Rounded.NotificationsActive,
                            title = "Notifikasi Rilis Episode",
                            subtitle = "Ingatkan episode baru di daftar Disimpan",
                            checked = settings.episodeNotifications,
                            onCheckedChange = { checked ->
                                onUpdateSettings { it.copy(episodeNotifications = checked) }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Bottom Sheet: Cache & Izin Aplikasi
    if (showStoragePermissionsSheet) {
        StorageAndPermissionsSheet(
            deviceStorage = deviceStorage,
            permissions = uiState.appPermissions,
            onClearCache = onClearCache,
            onRequestPermission = { perm -> permissionLauncher.launch(perm) },
            onOpenSystemSettings = { AndroidDeviceStorageManager.openAppPermissionSettings(context) },
            onDismiss = { showStoragePermissionsSheet = false }
        )
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
                    text = "Keluar dari Akun Google?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Kamu akan kembali ke layar Masuk dengan Google. Profil kustom, daftar Disimpan, riwayat tonton, dan komentar kamu tetap aman tersimpan.",
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

    if (showQualityDialog) {
        OptionPickerDialog(
            title = "Kualitas Default",
            options = listOf("1080p", "720p", "480p", "360p"),
            selected = settings.defaultQuality,
            onSelect = { q -> onUpdateSettings { it.copy(defaultQuality = q) } },
            onDismiss = { showQualityDialog = false }
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
                    Text("AnimWatch v1.3.0", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Aplikasi streaming anime Sub Indo, Sub Eng & Dub Indo resmi dengan Google Sign-In, Custom Profile, multi-season switcher, dan diskusi komunitas.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${uiState.allAnime.size} judul anime termuat saat ini.",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoldGradientButton(
                            text = "Segarkan Katalog",
                            icon = Icons.Rounded.Refresh,
                            onClick = {
                                showAboutDialog = false
                                if (!apiConfig.useLiveApi) onSetUseLiveApi(true) else onSyncApi()
                            },
                            modifier = Modifier.weight(1f),
                            height = 40.dp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup", color = GoldPrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAboutDialog = false
                        showApiUrlDialog = true
                    }
                ) {
                    Text("Server", color = TextTertiary)
                }
            }
        )
    }

    if (showApiUrlDialog) {
        EditApiBaseUrlDialog(
            currentUrl = apiConfig.baseUrl,
            onSave = onUpdateApiBaseUrl,
            onDismiss = { showApiUrlDialog = false }
        )
    }
}

@Composable
fun UserAvatarBadge(
    avatarUrl: String,
    emblem: String,
    size: Dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cleanUrl = remember(avatarUrl) { normalizeProfileAvatarUrl(avatarUrl) }
    val cleanEmblem = emblem.trim().take(2).uppercase().ifEmpty { "AW" }
    val hasHttpUrl = cleanUrl.startsWith("http://", ignoreCase = true) ||
        cleanUrl.startsWith("https://", ignoreCase = true)
    var isLoading by remember(cleanUrl) { mutableStateOf(hasHttpUrl) }
    var loadFailed by remember(cleanUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(GoldGradientBrush)
            .border(1.2.dp, GoldPrimary, CircleShape)
            .let { mod -> if (onClick != null) mod.clickable(onClick = onClick) else mod },
        contentAlignment = Alignment.Center
    ) {
        if (hasHttpUrl && !loadFailed) {
            AsyncImage(
                model = cleanUrl,
                contentDescription = cleanEmblem,
                contentScale = ContentScale.Crop,
                onLoading = { isLoading = true },
                onSuccess = {
                    isLoading = false
                    loadFailed = false
                },
                onError = {
                    isLoading = false
                    loadFailed = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
        if (!hasHttpUrl || isLoading || loadFailed) {
            Text(
                text = cleanEmblem,
                color = TextOnGold,
                fontSize = (size.value * 0.34f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CustomProfileEditorSheet(
    currentName: String,
    currentHandle: String,
    currentBio: String,
    currentEmblem: String,
    currentAvatarUrl: String,
    currentTier: String,
    currentFavoriteGenre: String,
    availableGenres: List<String>,
    onSave: (
        displayName: String,
        handle: String,
        bio: String,
        emblem: String,
        avatarUrl: String,
        memberTier: String,
        favoriteGenre: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var handle by remember { mutableStateOf(currentHandle.removePrefix("@")) }
    var bio by remember { mutableStateOf(currentBio) }
    var emblem by remember { mutableStateOf(currentEmblem) }
    var avatarUrl by remember { mutableStateOf(normalizeProfileAvatarUrl(currentAvatarUrl)) }
    var selectedTier by remember { mutableStateOf(currentTier.ifBlank { "Google Member" }) }
    var selectedGenre by remember { mutableStateOf(currentFavoriteGenre.ifBlank { "Action" }) }
    var nameError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ObsidianElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Kustomisasi Profil",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            Text(
                text = "Personalisasi tampilan profilmu yang akan dilihat pengguna lain di kolom komentar.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            // Live Preview Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard
            ) {
                Row(
                    modifier = Modifier
                        .background(ObsidianGoldCardBrush)
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatarBadge(
                        avatarUrl = avatarUrl,
                        emblem = emblem.ifBlank { name.take(2).uppercase() },
                        size = 56.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GoldTagChip(text = selectedTier, highlighted = true)
                            GoldTagChip(text = selectedGenre)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name.ifBlank { "Nama Tampilan" },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "@${handle.ifBlank { "username" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary
                        )
                    }
                }
            }

            // Preset Anime Character Avatars
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Avatar Karakter Anime",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    if (avatarUrl.isNotBlank()) {
                        Text(
                            text = "Pakai Inisial Saja",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { avatarUrl = "" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ANIME_AVATAR_PRESETS) { preset ->
                        val isSelected = avatarUrl == preset.url
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GoldTintStrong else ObsidianCard)
                                .border(
                                    if (isSelected) 1.5.dp else 0.75.dp,
                                    if (isSelected) GoldPrimary else GoldSubtleBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { avatarUrl = preset.url }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            UserAvatarBadge(
                                avatarUrl = preset.url,
                                emblem = preset.label.take(2),
                                size = 46.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) GoldPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            TactileInputField(
                label = "Nama Tampilan",
                value = name,
                onValueChange = {
                    name = it
                    nameError = if (it.isBlank()) "Nama tampilan wajib diisi" else null
                },
                placeholder = "Masukkan nama tampilan...",
                errorText = nameError
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TactileInputField(
                    label = "Username (@handle)",
                    value = handle,
                    onValueChange = { raw ->
                        handle = raw.removePrefix("@").lowercase().replace(Regex("[^a-z0-9._]"), "").take(22)
                    },
                    placeholder = "username",
                    modifier = Modifier.weight(1.4f)
                )
                TactileInputField(
                    label = "Inisial (2 Huruf)",
                    value = emblem,
                    onValueChange = { if (it.length <= 2) emblem = it.uppercase() },
                    placeholder = "AW",
                    modifier = Modifier.weight(0.9f)
                )
            }

            TactileInputField(
                label = "URL Foto Profil Kustom (Opsional)",
                value = avatarUrl,
                onValueChange = { avatarUrl = it },
                placeholder = "https://..."
            )

            TactileInputField(
                label = "Bio Profil",
                value = bio,
                onValueChange = { bio = it },
                placeholder = "Ceritakan anime favorit atau kutipan kesukaanmu...",
                singleLine = false,
                maxLines = 3
            )

            // Badge / Gelar Komunitas Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gelar / Badge Profil",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MEMBER_BADGE_OPTIONS.forEach { badge ->
                        val selected = selectedTier.equals(badge, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) GoldGradientBrush else androidx.compose.ui.graphics.Brush.linearGradient(listOf(ObsidianCard, ObsidianCard)))
                                .border(
                                    0.75.dp,
                                    if (selected) Color.Transparent else GoldSubtleBorder,
                                    CircleShape
                                )
                                .clickable { selectedTier = badge }
                                .padding(horizontal = 13.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) TextOnGold else TextSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Genre Favorit Utama Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Genre Favorit Utama",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableGenres.filterNot { it.equals("Dub Indo", ignoreCase = true) }.take(12).forEach { g ->
                        val selected = selectedGenre.equals(g, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) GoldTintStrong else ObsidianCard)
                                .border(
                                    0.75.dp,
                                    if (selected) GoldPrimary else GoldSubtleBorder,
                                    CircleShape
                                )
                                .clickable { selectedGenre = g }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = g,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) GoldPrimary else TextSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GoldOutlinedButton(
                    text = "Batal",
                    onClick = onDismiss,
                    modifier = Modifier.weight(0.8f),
                    height = 46.dp
                )
                GoldGradientButton(
                    text = "Simpan Profil",
                    onClick = {
                        if (name.isBlank()) {
                            nameError = "Nama tampilan wajib diisi"
                        } else {
                            val cleanHandle = "@${handle.ifBlank { name.lowercase().replace(Regex("[^a-z0-9]"), "").take(16).ifEmpty { "watcher" } }}"
                            val cleanEmblem = emblem.trim().ifEmpty {
                                name.trim().take(2).uppercase()
                            }
                            onSave(
                                name.trim(),
                                cleanHandle,
                                bio.trim(),
                                cleanEmblem,
                                avatarUrl.trim(),
                                selectedTier,
                                selectedGenre
                            )
                        }
                    },
                    modifier = Modifier.weight(1.2f),
                    height = 46.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorageAndPermissionsSheet(
    deviceStorage: DeviceStorageInfo,
    permissions: List<AppPermissionStatus>,
    onClearCache: () -> Unit,
    onRequestPermission: (String) -> Unit,
    onOpenSystemSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ObsidianElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Cache & Izin Aplikasi",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Real Device Storage & Cache Cleaner Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard
            ) {
                Column(
                    modifier = Modifier
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cache Gambar & Streaming",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = deviceStorage.cacheLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    AnalogMeterGauge(
                        progress = deviceStorage.deviceUsedFraction.coerceIn(0.03f, 1f),
                        height = 6.dp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tersedia: ${deviceStorage.availableLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Total Perangkat: ${deviceStorage.totalLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                    GoldOutlinedButton(
                        text = "Bersihkan Cache (${deviceStorage.cacheLabel})",
                        icon = Icons.Rounded.CleaningServices,
                        onClick = onClearCache,
                        modifier = Modifier.fillMaxWidth(),
                        height = 42.dp
                    )
                }
            }

            // Permissions Card (excluding offline storage permission)
            val relevantPermissions = permissions.filterNot { it.id == "storage" }
            if (relevantPermissions.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianCard
                ) {
                    Column(
                        modifier = Modifier.border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                    ) {
                        relevantPermissions.forEachIndexed { idx, perm ->
                            if (idx > 0) SettingDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = perm.canRequestRuntime && perm.androidPermission != null) {
                                        perm.androidPermission?.let(onRequestPermission)
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
                                            perm.androidPermission?.let(onRequestPermission)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (perm.isGranted) "Aktif" else "Izinkan",
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
                            title = "Pengaturan Izin Sistem",
                            value = "Buka",
                            onClick = onOpenSystemSettings
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
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
            modifier = Modifier.weight(1f, fill = true),
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
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 156.dp)
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
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
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
        title = { Text("Pengaturan Server", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TactileInputField(
                    label = "URL Server",
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        urlError = if (it.isBlank()) "URL server tidak boleh kosong" else null
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
                            text = "Server Utama",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            GoldGradientButton(
                text = "Simpan",
                onClick = {
                    if (urlInput.isBlank()) {
                        urlError = "URL server tidak boleh kosong"
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
