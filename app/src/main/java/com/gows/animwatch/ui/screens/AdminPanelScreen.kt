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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeArtTheme
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.remote.AdminAnimeDto
import com.gows.animwatch.data.remote.AdminEpisodeDto
import com.gows.animwatch.data.remote.AdminPlaylistDto
import com.gows.animwatch.data.remote.AdminSaveAnimeRequestDto
import com.gows.animwatch.data.remote.AdminSavePlaylistRequestDto
import com.gows.animwatch.ui.components.AnimePosterArtwork
import com.gows.animwatch.ui.components.CassetteLinearShimmer
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
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

private enum class AdminSectionTab(val label: String) {
    ANIME("Kelola Anime"),
    PLAYLIST("Playlist"),
    CONTROL("Kontrol")
}

private enum class AdminAnimeFilter(val label: String) {
    ALL("Semua"),
    CMS("CMS DB"),
    CATALOG("Katalog"),
    HIDDEN("Tersembunyi")
}

private data class EditableAnimeDraft(
    val isNew: Boolean,
    val existingSlug: String?,
    val title: String,
    val japaneseTitle: String,
    val poster: String,
    val banner: String,
    val synopsis: String,
    val genresCsv: String,
    val rating: String,
    val studio: String,
    val status: String,
    val type: String,
    val year: String,
    val season: String,
    val totalEpisodes: String,
    val releasedEpisodes: String,
    val officialChannel: String,
    val playlistId: String,
    val anilistId: String,
    val isOfficialFree: Boolean,
    val isDubIndo: Boolean,
    val isFeatured: Boolean,
    val isHidden: Boolean,
    val episodes: List<AdminEpisodeDto>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    uiState: AnimWatchUiState,
    bottomInsetPadding: Dp,
    onBack: () -> Unit,
    onVerifyAdmin: (String) -> Unit,
    onRefreshOverview: () -> Unit,
    onSaveAnime: (existingSlug: String?, request: AdminSaveAnimeRequestDto, onDone: (Boolean) -> Unit) -> Unit,
    onQuickToggleHide: (Anime, Boolean) -> Unit,
    onDeleteAnime: (String) -> Unit,
    onSavePlaylist: (existingPlaylistId: String?, request: AdminSavePlaylistRequestDto, onDone: (Boolean) -> Unit) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onUpdateUserRole: (userId: String, role: String, memberTier: String) -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AdminSectionTab.ANIME) }
    var animeFilter by remember { mutableStateOf(AdminAnimeFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var secretInput by remember { mutableStateOf("") }

    var editingDraft by remember { mutableStateOf<EditableAnimeDraft?>(null) }
    var pendingDeleteSlug by remember { mutableStateOf<Pair<String, String>?>(null) }

    var editingPlaylist by remember { mutableStateOf<AdminPlaylistDto?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var pendingDeletePlaylist by remember { mutableStateOf<AdminPlaylistDto?>(null) }

    var serverUrlDraft by remember(uiState.apiConfig.baseUrl) { mutableStateOf(uiState.apiConfig.baseUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ObsidianCard)
                        .border(0.75.dp, GoldSubtleBorder, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Panel Admin CMS",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.isAdmin) "Akses Penuh • CRUD Database Aktif" else "Verifikasi Diperlukan",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.isAdmin) StatusEmerald else GoldPrimary
                    )
                }
            }

            if (uiState.isAdmin) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onRefreshOverview,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ObsidianCard)
                            .border(0.75.dp, GoldSubtleBorder, CircleShape)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Segarkan", tint = GoldPrimary, modifier = Modifier.size(19.dp))
                    }
                    IconButton(
                        onClick = { editingDraft = emptyAnimeDraft() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GoldGradientBrush)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Tambah Anime", tint = TextOnGold, modifier = Modifier.size(21.dp))
                    }
                }
            }
        }

        if (uiState.isLoadingAdmin || uiState.isSavingAdmin) {
            CassetteLinearShimmer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (!uiState.isAdmin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = ObsidianCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GoldTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = GoldPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Kunci Akses Admin", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("Khusus pengelola katalog & database", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }

                        TactileInputField(
                            label = "Kunci Rahasia Admin",
                            value = secretInput,
                            onValueChange = { secretInput = it },
                            placeholder = "Masukkan Admin Secret Key...",
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )

                        GoldGradientButton(
                            text = "Verifikasi & Buka Panel Admin",
                            icon = Icons.Rounded.AdminPanelSettings,
                            onClick = { onVerifyAdmin(secretInput) },
                            modifier = Modifier.fillMaxWidth(),
                            height = 46.dp
                        )
                    }
                }
            }
            return
        }

        // Section Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(CircleShape)
                .background(ObsidianCard)
                .border(0.75.dp, GoldSubtleBorder, CircleShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AdminSectionTab.entries.forEach { tab ->
                val selected = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(
                            if (selected) GoldGradientBrush
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { activeTab = tab }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) TextOnGold else TextSecondary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        when (activeTab) {
            AdminSectionTab.ANIME -> {
                val adminBySlug = remember(uiState.adminAnimes) { uiState.adminAnimes.associateBy { it.slug } }
                val q = searchQuery.trim().lowercase()

                val filteredCms = remember(uiState.adminAnimes, animeFilter, q) {
                    uiState.adminAnimes.filter { item ->
                        val matchesFilter = when (animeFilter) {
                            AdminAnimeFilter.ALL, AdminAnimeFilter.CMS -> true
                            AdminAnimeFilter.HIDDEN -> item.isHidden
                            AdminAnimeFilter.CATALOG -> false
                        }
                        val matchesQ = q.isEmpty() || item.title.lowercase().contains(q) || item.slug.lowercase().contains(q)
                        matchesFilter && matchesQ
                    }
                }

                val filteredCatalog = remember(uiState.allAnime, adminBySlug, animeFilter, q) {
                    if (animeFilter == AdminAnimeFilter.CMS || animeFilter == AdminAnimeFilter.HIDDEN) {
                        emptyList()
                    } else {
                        uiState.allAnime.filter { a ->
                            if (a.slug in adminBySlug || a.id in adminBySlug) return@filter false
                            q.isEmpty() || a.title.lowercase().contains(q) || a.slug.lowercase().contains(q)
                        }.take(80)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = bottomInsetPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        AdminStatsStrip(
                            cmsCount = uiState.adminAnimes.size,
                            catalogCount = uiState.allAnime.size,
                            playlistCount = uiState.adminPlaylists.size,
                            commentCount = uiState.adminStats?.totalComments ?: uiState.adminComments.size
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TactileInputField(
                                label = "Cari Anime",
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "Cari judul atau slug anime...",
                                leadingIcon = {
                                    Icon(Icons.Rounded.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AdminAnimeFilter.entries.forEach { f ->
                                    val sel = animeFilter == f
                                    GoldTagChip(
                                        text = f.label,
                                        highlighted = sel,
                                        modifier = Modifier.clickable { animeFilter = f }
                                    )
                                }
                            }
                            GoldGradientButton(
                                text = "Tambah Anime Baru",
                                icon = Icons.Rounded.Add,
                                onClick = { editingDraft = emptyAnimeDraft() },
                                modifier = Modifier.fillMaxWidth(),
                                height = 44.dp
                            )
                        }
                    }

                    if (filteredCms.isNotEmpty()) {
                        item {
                            Text(
                                text = "Data CMS Database (${filteredCms.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(filteredCms, key = { "cms_${it.slug}" }) { item ->
                            AdminCmsAnimeRow(
                                item = item,
                                onEdit = { editingDraft = item.toDraft() },
                                onToggleHide = {
                                    onSaveAnime(
                                        item.slug,
                                        AdminSaveAnimeRequestDto(
                                            slug = item.slug,
                                            title = item.title,
                                            isHidden = !item.isHidden
                                        )
                                    ) {}
                                },
                                onDelete = { pendingDeleteSlug = item.slug to item.title }
                            )
                        }
                    }

                    if (filteredCatalog.isNotEmpty()) {
                        item {
                            Text(
                                text = "Katalog Live (${filteredCatalog.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(filteredCatalog, key = { "cat_${it.id}" }) { anime ->
                            AdminCatalogAnimeRow(
                                anime = anime,
                                onEdit = { editingDraft = anime.toDraft() },
                                onHide = { onQuickToggleHide(anime, true) }
                            )
                        }
                    }
                }
            }

            AdminSectionTab.PLAYLIST -> {
                val q = searchQuery.trim().lowercase()
                val playlists = remember(uiState.adminPlaylists, q) {
                    uiState.adminPlaylists.filter {
                        q.isEmpty() || it.cleanTitle.lowercase().contains(q) ||
                            it.title.lowercase().contains(q) ||
                            it.playlistId.lowercase().contains(q)
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = bottomInsetPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TactileInputField(
                                label = "Cari Playlist",
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "Cari playlist ID atau judul anime...",
                                leadingIcon = {
                                    Icon(Icons.Rounded.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true
                            )
                            GoldGradientButton(
                                text = "Tambah Playlist YouTube",
                                icon = Icons.Rounded.PlaylistAdd,
                                onClick = { showCreatePlaylistDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                height = 44.dp
                            )
                            uiState.adminStats?.let { st ->
                                Text(
                                    text = "${st.curatedRulesCount} rule terverifikasi • ${st.splitPartRulesCount} aturan split 2-Part/Cour aktif",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }

                    items(playlists, key = { it.playlistId }) { pl ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ObsidianCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = pl.cleanTitle.ifBlank { pl.title },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${pl.channelName} • ${pl.audioLang} • ${pl.episodeHint} Ep • AL#${pl.anilistId ?: "-"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = pl.playlistId,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { editingPlaylist = pl }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = GoldPrimary, modifier = Modifier.size(19.dp))
                                }
                                IconButton(onClick = { pendingDeletePlaylist = pl }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = StatusLive, modifier = Modifier.size(19.dp))
                                }
                            }
                        }
                    }
                }
            }

            AdminSectionTab.CONTROL -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = bottomInsetPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Surface(shape = RoundedCornerShape(20.dp), color = ObsidianCard) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Endpoint Server API", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(
                                    text = "Status: ${uiState.apiConfig.status.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary
                                )
                                TactileInputField(
                                    label = "URL Server API",
                                    value = serverUrlDraft,
                                    onValueChange = { serverUrlDraft = it },
                                    placeholder = "https://kumanime-api-production.up.railway.app/api/",
                                    singleLine = true
                                )
                                GoldGradientButton(
                                    text = "Simpan URL & Sinkronkan",
                                    onClick = { onUpdateServerUrl(serverUrlDraft) },
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 42.dp
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Moderasi Komentar Terbaru (${uiState.adminComments.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(uiState.adminComments, key = { "adm_c_${it.id}" }) { c ->
                        Surface(shape = RoundedCornerShape(16.dp), color = ObsidianCard) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = "${c.authorName} • ${c.animeId} (${c.timeLabel})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = c.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { onDeleteComment(c.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Hapus komentar", tint = StatusLive, modifier = Modifier.size(19.dp))
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Pengguna Terdaftar (${uiState.adminUsers.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(uiState.adminUsers, key = { "adm_u_${it.id ?: it.email}" }) { u ->
                        val uid = u.id?.toString().orEmpty()
                        val isAdminUser = u.role.equals("admin", ignoreCase = true)
                        Surface(shape = RoundedCornerShape(16.dp), color = ObsidianCard) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = u.displayName ?: "User",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${u.email.orEmpty()} • ${u.memberTier ?: "Member"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                GoldTagChip(
                                    text = if (isAdminUser) "ADMIN" else "USER",
                                    highlighted = isAdminUser,
                                    modifier = Modifier.clickable(enabled = uid.isNotBlank()) {
                                        val nextRole = if (isAdminUser) "user" else "admin"
                                        val nextTier = if (isAdminUser) "Member" else "ADMIN"
                                        onUpdateUserRole(uid, nextRole, nextTier)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editingDraft?.let { draft ->
        AdminAnimeEditorSheet(
            initialDraft = draft,
            isSaving = uiState.isSavingAdmin,
            onSave = { existingSlug, req ->
                onSaveAnime(existingSlug, req) { ok ->
                    if (ok) editingDraft = null
                }
            },
            onDismiss = { editingDraft = null }
        )
    }

    pendingDeleteSlug?.let { (slug, title) ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSlug = null },
            containerColor = ObsidianElevated,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Hapus Data Anime?") },
            text = { Text("'$title' akan dihapus dari tabel CMS admin.") },
            confirmButton = {
                GoldGradientButton(
                    text = "Hapus",
                    onClick = {
                        pendingDeleteSlug = null
                        onDeleteAnime(slug)
                    },
                    height = 40.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSlug = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    if (showCreatePlaylistDialog || editingPlaylist != null) {
        AdminPlaylistEditorDialog(
            initial = editingPlaylist,
            onSave = { existingId, req ->
                onSavePlaylist(existingId, req) { ok ->
                    if (ok) {
                        showCreatePlaylistDialog = false
                        editingPlaylist = null
                    }
                }
            },
            onDismiss = {
                showCreatePlaylistDialog = false
                editingPlaylist = null
            }
        )
    }

    pendingDeletePlaylist?.let { pl ->
        AlertDialog(
            onDismissRequest = { pendingDeletePlaylist = null },
            containerColor = ObsidianElevated,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Hapus Playlist?") },
            text = { Text("Playlist '${pl.cleanTitle.ifBlank { pl.title }}' (${pl.playlistId}) akan dihapus.") },
            confirmButton = {
                GoldGradientButton(
                    text = "Hapus",
                    onClick = {
                        val id = pl.playlistId
                        pendingDeletePlaylist = null
                        onDeletePlaylist(id)
                    },
                    height = 40.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePlaylist = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

private fun emptyAnimeDraft() = EditableAnimeDraft(
    isNew = true,
    existingSlug = null,
    title = "",
    japaneseTitle = "",
    poster = "",
    banner = "",
    synopsis = "",
    genresCsv = "Action, Fantasy, Adventure",
    rating = "8.7",
    studio = "AnimWatch Admin",
    status = "ONGOING",
    type = "TV Series",
    year = "2026",
    season = "2026",
    totalEpisodes = "12",
    releasedEpisodes = "1",
    officialChannel = "Muse Indonesia",
    playlistId = "",
    anilistId = "",
    isOfficialFree = true,
    isDubIndo = false,
    isFeatured = true,
    isHidden = false,
    episodes = emptyList()
)

@Composable
private fun AdminStatsStrip(
    cmsCount: Int,
    catalogCount: Int,
    playlistCount: Int,
    commentCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianCard)
            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatBox("$cmsCount", "CMS DB")
        StatBox("$catalogCount", "Katalog")
        StatBox("$playlistCount", "Playlist")
        StatBox("$commentCount", "Komentar")
    }
}

@Composable
private fun StatBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = GoldPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

@Composable
private fun AdminCmsAnimeRow(
    item: AdminAnimeDto,
    onEdit: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp), color = ObsidianCard, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .border(0.75.dp, if (item.isHidden) StatusLive.copy(alpha = 0.4f) else GoldSubtleBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimePosterArtwork(
                artTheme = AnimeArtTheme.CELESTIAL_ASTRAL,
                title = item.title,
                posterUrl = item.poster,
                modifier = Modifier
                    .size(width = 52.dp, height = 72.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${item.status} • ${item.releasedEpisodes}/${item.totalEpisodes} Ep • ★ ${item.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.isHidden) GoldTagChip("TERSEMBUNYI")
                    if (item.isDubIndo) GoldTagChip("DUB INDO", highlighted = true)
                    if (item.isFeatured && !item.isHidden) GoldTagChip("UNGGULAN", highlighted = true)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = GoldPrimary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onToggleHide) {
                Icon(
                    imageVector = if (item.isHidden) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = if (item.isHidden) "Tampilkan" else "Sembunyikan",
                    tint = TextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = StatusLive, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun AdminCatalogAnimeRow(
    anime: Anime,
    onEdit: () -> Unit,
    onHide: () -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp), color = ObsidianCardAlt, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimePosterArtwork(
                artTheme = anime.artTheme,
                title = anime.title,
                posterUrl = anime.posterUrl,
                modifier = Modifier
                    .size(width = 48.dp, height = 66.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(anime.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${if (anime.isDubIndo) "Dub Indo" else "Sub Indo"} • ${anime.releasedEpisodes} Ep • ★ ${anime.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(anime.slug, style = MaterialTheme.typography.labelSmall, color = TextTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = GoldPrimary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onHide) {
                Icon(Icons.Rounded.VisibilityOff, contentDescription = "Sembunyikan", tint = TextSecondary, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAnimeEditorSheet(
    initialDraft: EditableAnimeDraft,
    isSaving: Boolean,
    onSave: (existingSlug: String?, AdminSaveAnimeRequestDto) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialDraft.title) }
    val japaneseTitle = remember { initialDraft.japaneseTitle }
    var poster by remember { mutableStateOf(initialDraft.poster) }
    val banner = remember { initialDraft.banner }
    var synopsis by remember { mutableStateOf(initialDraft.synopsis) }
    var genresCsv by remember { mutableStateOf(initialDraft.genresCsv) }
    var rating by remember { mutableStateOf(initialDraft.rating) }
    var studio by remember { mutableStateOf(initialDraft.studio) }
    var status by remember { mutableStateOf(initialDraft.status) }
    val type = remember { initialDraft.type }
    var year by remember { mutableStateOf(initialDraft.year) }
    var totalEpisodes by remember { mutableStateOf(initialDraft.totalEpisodes) }
    var releasedEpisodes by remember { mutableStateOf(initialDraft.releasedEpisodes) }
    var channel by remember { mutableStateOf(initialDraft.officialChannel) }
    var playlistId by remember { mutableStateOf(initialDraft.playlistId) }
    var isOfficialFree by remember { mutableStateOf(initialDraft.isOfficialFree) }
    var isDubIndo by remember { mutableStateOf(initialDraft.isDubIndo) }
    var isFeatured by remember { mutableStateOf(initialDraft.isFeatured) }
    var isHidden by remember { mutableStateOf(initialDraft.isHidden) }

    val episodesState = remember { mutableStateListOf<AdminEpisodeDto>().apply { addAll(initialDraft.episodes) } }
    var newEpNum by remember(episodesState.size) { mutableStateOf("${episodesState.size + 1}") }
    var newEpTitle by remember { mutableStateOf("") }
    var newEpUrl by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ObsidianElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialDraft.isNew) "Tambah Anime Baru" else "Edit Anime CMS",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = TextSecondary)
                }
            }

            TactileInputField(
                label = "Judul Anime",
                value = title,
                onValueChange = { title = it },
                placeholder = "Contoh: Mushoku Tensei Season 3",
                singleLine = true
            )

            TactileInputField(
                label = "URL Poster (Opsional)",
                value = poster,
                onValueChange = { poster = it },
                placeholder = "https://...",
                singleLine = true
            )

            TactileInputField(
                label = "Genre (pisahkan koma)",
                value = genresCsv,
                onValueChange = { genresCsv = it },
                placeholder = "Action, Fantasy, Drama",
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TactileInputField(
                    label = "Total Ep",
                    value = totalEpisodes,
                    onValueChange = { totalEpisodes = it.filter(Char::isDigit) },
                    placeholder = "12",
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TactileInputField(
                    label = "Rilis Ep",
                    value = releasedEpisodes,
                    onValueChange = { releasedEpisodes = it.filter(Char::isDigit) },
                    placeholder = "1",
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TactileInputField(
                    label = "Rating",
                    value = rating,
                    onValueChange = { rating = it },
                    placeholder = "8.7",
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TactileInputField(
                    label = "Status",
                    value = status,
                    onValueChange = { status = it },
                    placeholder = "ONGOING / COMPLETED",
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TactileInputField(
                    label = "Tahun",
                    value = year,
                    onValueChange = { year = it.filter(Char::isDigit) },
                    placeholder = "2026",
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            TactileInputField(
                label = "YouTube Playlist ID (Opsional)",
                value = playlistId,
                onValueChange = { playlistId = it },
                placeholder = "PLPanbgyTozt...",
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TactileInputField(
                    label = "Channel",
                    value = channel,
                    onValueChange = { channel = it },
                    placeholder = "Muse Indonesia",
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TactileInputField(
                    label = "Studio",
                    value = studio,
                    onValueChange = { studio = it },
                    placeholder = "Studio Bind",
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            TactileInputField(
                label = "Sinopsis",
                value = synopsis,
                onValueChange = { synopsis = it },
                placeholder = "Tulis deskripsi singkat anime...",
                singleLine = false,
                maxLines = 4
            )

            AdminSwitchRow("Siap Tonton Gratis", isOfficialFree) { isOfficialFree = it }
            AdminSwitchRow("Kategori Dub Indo", isDubIndo) { isDubIndo = it }
            AdminSwitchRow("Tampilkan di Unggulan", isFeatured) { isFeatured = it }
            AdminSwitchRow("Sembunyikan dari Katalog User", isHidden) { isHidden = it }

            HorizontalDivider(color = DividerSubtle)
            Text(
                text = "Daftar Episode Kustom (${episodesState.size})",
                style = MaterialTheme.typography.titleSmall,
                color = GoldPrimary,
                fontWeight = FontWeight.SemiBold
            )

            episodesState.forEachIndexed { idx, ep ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianCard)
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ep ${ep.number ?: (idx + 1)} • ${ep.epsTitle.orEmpty()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = ep.streamUrl.orEmpty().ifBlank { "Info saja (tanpa URL stream)" },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { episodesState.removeAt(idx) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Hapus Episode", tint = StatusLive, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Surface(shape = RoundedCornerShape(14.dp), color = ObsidianCard) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Tambah Episode Baru", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TactileInputField(
                            label = "No",
                            value = newEpNum,
                            onValueChange = { newEpNum = it.filter(Char::isDigit) },
                            placeholder = "1",
                            singleLine = true,
                            modifier = Modifier.width(72.dp)
                        )
                        TactileInputField(
                            label = "Judul Episode",
                            value = newEpTitle,
                            onValueChange = { newEpTitle = it },
                            placeholder = "Episode Baru",
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    TactileInputField(
                        label = "URL YouTube / Stream .mp4",
                        value = newEpUrl,
                        onValueChange = { newEpUrl = it },
                        placeholder = "https://youtube.com/watch?v=... atau video ID",
                        singleLine = true
                    )
                    GoldOutlinedButton(
                        text = "+ Tambah ke Daftar Episode",
                        onClick = {
                            val num = newEpNum.toIntOrNull()?.coerceAtLeast(1) ?: (episodesState.size + 1)
                            val titleFinal = newEpTitle.trim().ifBlank { "Episode $num" }
                            episodesState.add(
                                AdminEpisodeDto(
                                    epsTitle = titleFinal,
                                    epsSlug = "custom-ep-$num",
                                    number = num,
                                    streamUrl = newEpUrl.trim(),
                                    streamType = if (newEpUrl.isNotBlank()) "official_episode" else "info_only",
                                    thumbnail = poster.trim()
                                )
                            )
                            newEpTitle = ""
                            newEpUrl = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        height = 38.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            GoldGradientButton(
                text = if (isSaving) "Menyimpan..." else "Simpan ke Database",
                onClick = {
                    val cleanTitle = title.trim()
                    if (isSaving || cleanTitle.isEmpty()) return@GoldGradientButton
                    val genres = genresCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("Anime") }
                    val relEp = releasedEpisodes.toIntOrNull() ?: maxOf(episodesState.size, 1)
                    val totEp = maxOf(totalEpisodes.toIntOrNull() ?: 12, relEp, 1)
                    onSave(
                        initialDraft.existingSlug,
                        AdminSaveAnimeRequestDto(
                            slug = initialDraft.existingSlug,
                            title = cleanTitle,
                            japaneseTitle = japaneseTitle.trim(),
                            poster = poster.trim(),
                            banner = banner.trim().ifBlank { poster.trim() },
                            synopsis = synopsis.trim(),
                            genres = genres,
                            rating = rating.toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 8.5,
                            studio = studio.trim().ifBlank { "AnimWatch Admin" },
                            status = if (status.contains("COMPLET", true)) "COMPLETED" else "ONGOING",
                            type = type.trim().ifBlank { "TV Series" },
                            year = year.toIntOrNull() ?: 2026,
                            season = initialDraft.season,
                            totalEpisodes = totEp,
                            releasedEpisodes = relEp,
                            isOfficialFree = isOfficialFree,
                            isDubIndo = isDubIndo,
                            officialChannel = channel.trim().ifBlank { "AnimWatch Admin" },
                            playlistId = playlistId.trim(),
                            anilistId = initialDraft.anilistId.toIntOrNull(),
                            episodes = episodesState.toList(),
                            isHidden = isHidden,
                            isFeatured = isFeatured
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AdminPlaylistEditorDialog(
    initial: AdminPlaylistDto?,
    onSave: (existingPlaylistId: String?, AdminSavePlaylistRequestDto) -> Unit,
    onDismiss: () -> Unit
) {
    var playlistId by remember { mutableStateOf(initial?.playlistId.orEmpty()) }
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var cleanTitle by remember { mutableStateOf(initial?.cleanTitle.orEmpty()) }
    var channel by remember { mutableStateOf(initial?.channelName ?: "Muse Indonesia") }
    var audioLang by remember { mutableStateOf(initial?.audioLang ?: "SUB") }
    var epHint by remember { mutableStateOf("${initial?.episodeHint ?: 12}") }
    var anilistId by remember { mutableStateOf(initial?.anilistId?.toString().orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text(if (initial == null) "Tambah Playlist" else "Edit Playlist") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TactileInputField(label = "Playlist ID", value = playlistId, onValueChange = { playlistId = it }, placeholder = "PL...", singleLine = true)
                TactileInputField(label = "Judul Playlist Asli", value = title, onValueChange = { title = it }, placeholder = "Mushoku Tensei...", singleLine = true)
                TactileInputField(label = "Judul Bersih", value = cleanTitle, onValueChange = { cleanTitle = it }, placeholder = "Mushoku Tensei", singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TactileInputField(label = "Channel", value = channel, onValueChange = { channel = it }, placeholder = "Muse Indonesia", singleLine = true, modifier = Modifier.weight(1f))
                    TactileInputField(label = "Audio", value = audioLang, onValueChange = { audioLang = it }, placeholder = "SUB / DUB_ID", singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TactileInputField(label = "Jml Ep", value = epHint, onValueChange = { epHint = it.filter(Char::isDigit) }, placeholder = "12", singleLine = true, modifier = Modifier.weight(1f))
                    TactileInputField(label = "AniList ID", value = anilistId, onValueChange = { anilistId = it.filter(Char::isDigit) }, placeholder = "108465", singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            GoldGradientButton(
                text = "Simpan",
                onClick = {
                    if (playlistId.isBlank() || title.isBlank()) return@GoldGradientButton
                    onSave(
                        initial?.playlistId,
                        AdminSavePlaylistRequestDto(
                            playlistId = playlistId.trim(),
                            title = title.trim(),
                            cleanTitle = cleanTitle.trim().ifBlank { null },
                            channelName = channel.trim().ifBlank { "Muse Indonesia" },
                            episodeHint = epHint.toIntOrNull()?.coerceAtLeast(1) ?: 12,
                            audioLang = if (audioLang.contains("DUB", true)) "DUB_ID" else "SUB",
                            anilistId = anilistId.toIntOrNull()
                        )
                    )
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
private fun AdminSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianCard)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextOnGold,
                checkedTrackColor = GoldPrimary
            )
        )
    }
}

private fun AdminAnimeDto.toDraft(): EditableAnimeDraft = EditableAnimeDraft(
    isNew = false,
    existingSlug = slug,
    title = title,
    japaneseTitle = japaneseTitle,
    poster = poster,
    banner = banner,
    synopsis = synopsis,
    genresCsv = genres.joinToString(", "),
    rating = rating.toString(),
    studio = studio,
    status = status,
    type = type,
    year = year.toString(),
    season = season,
    totalEpisodes = totalEpisodes.toString(),
    releasedEpisodes = releasedEpisodes.toString(),
    officialChannel = officialChannel,
    playlistId = playlistId,
    anilistId = anilistId?.toString().orEmpty(),
    isOfficialFree = isOfficialFree,
    isDubIndo = isDubIndo,
    isFeatured = isFeatured,
    isHidden = isHidden,
    episodes = episodes
)

private fun Anime.toDraft(): EditableAnimeDraft = EditableAnimeDraft(
    isNew = false,
    existingSlug = slug.ifBlank { id },
    title = title,
    japaneseTitle = japaneseTitle,
    poster = posterUrl,
    banner = bannerUrl,
    synopsis = synopsis,
    genresCsv = genres.joinToString(", "),
    rating = rating.toString(),
    studio = studio,
    status = if (status == AnimeStatus.COMPLETED) "COMPLETED" else "ONGOING",
    type = type.label,
    year = year.toString(),
    season = season,
    totalEpisodes = totalEpisodes.toString(),
    releasedEpisodes = releasedEpisodes.toString(),
    officialChannel = officialChannel ?: "AnimWatch Admin",
    playlistId = externalLinks.firstOrNull { it.url.contains("list=") }?.url?.substringAfter("list=")?.substringBefore("&").orEmpty(),
    anilistId = Regex("-(\\d{2,8})$").find(slug)?.groupValues?.getOrNull(1).orEmpty(),
    isOfficialFree = isOfficialFree,
    isDubIndo = isDubIndo,
    isFeatured = isFeatured,
    isHidden = false,
    episodes = episodes.map { ep ->
        AdminEpisodeDto(
            epsTitle = ep.title,
            epsSlug = ep.slug.ifBlank { ep.id },
            number = ep.number,
            streamUrl = ep.streamUrl,
            streamType = ep.streamType,
            thumbnail = ep.thumbnailUrl
        )
    }
)
