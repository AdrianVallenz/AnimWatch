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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.DownloadStatus
import com.gows.animwatch.data.model.DownloadTask
import com.gows.animwatch.data.model.WatchProgress
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.ui.components.AnalogMeterGauge
import com.gows.animwatch.ui.components.AnimePosterArtwork
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.TapeReelSpinner
import com.gows.animwatch.ui.components.WatchlistCategorySheet
import com.gows.animwatch.ui.components.formatTimeSeconds
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.state.LibraryTab
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.MonoFont
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

@Composable
fun LibraryScreen(
    uiState: AnimWatchUiState,
    bottomInsetPadding: Dp,
    onSelectLibraryTab: (LibraryTab) -> Unit,
    onSelectWatchlistFilter: (WatchlistCategory?) -> Unit,
    onUpdateWatchlistCategory: (String, WatchlistCategory?) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onPauseDownload: (String) -> Unit,
    onResumeDownload: (String) -> Unit,
    onRemoveDownload: (String) -> Unit,
    onAnimeClick: (String) -> Unit,
    onPlayAnime: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingWatchlistAnime by remember { mutableStateOf<Anime?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = "KOLEKSI // ARSIP TAPE",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianElevated)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LibraryTab.entries.forEach { tab ->
                    val selected = uiState.libraryTab == tab
                    val count = when (tab) {
                        LibraryTab.WATCHLIST -> uiState.watchlist.size
                        LibraryTab.HISTORY -> uiState.watchHistory.size
                        LibraryTab.DOWNLOADS -> uiState.downloads.size
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (selected) GoldGradientBrush
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { onSelectLibraryTab(tab) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tab.label.uppercase()} ($count)",
                            fontFamily = MonoFont,
                            color = if (selected) TextOnGold else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        when (uiState.libraryTab) {
            LibraryTab.WATCHLIST -> WatchlistContent(
                uiState = uiState,
                bottomInsetPadding = bottomInsetPadding,
                onSelectFilter = onSelectWatchlistFilter,
                onEditCategory = { editingWatchlistAnime = it },
                onAnimeClick = onAnimeClick,
                onPlayClick = { onPlayAnime(it.id, null) }
            )

            LibraryTab.HISTORY -> HistoryContent(
                allAnime = uiState.allAnime,
                history = uiState.watchHistory,
                bottomInsetPadding = bottomInsetPadding,
                onClearAll = onClearAllHistory,
                onRemoveItem = onRemoveHistory,
                onAnimeClick = onAnimeClick,
                onResumeClick = { progress -> onPlayAnime(progress.animeId, progress.episodeId) }
            )

            LibraryTab.DOWNLOADS -> DownloadsContent(
                downloads = uiState.downloads,
                bottomInsetPadding = bottomInsetPadding,
                onPause = onPauseDownload,
                onResume = onResumeDownload,
                onRemove = onRemoveDownload,
                onPlayOffline = { task -> onPlayAnime(task.animeId, task.episodeId) },
                onAnimeClick = onAnimeClick
            )
        }
    }

    editingWatchlistAnime?.let { anime ->
        WatchlistCategorySheet(
            animeTitle = anime.title,
            currentCategory = uiState.watchlist[anime.id],
            onSelectCategory = { cat -> onUpdateWatchlistCategory(anime.id, cat) },
            onDismiss = { editingWatchlistAnime = null }
        )
    }
}

@Composable
private fun WatchlistContent(
    uiState: AnimWatchUiState,
    bottomInsetPadding: Dp,
    onSelectFilter: (WatchlistCategory?) -> Unit,
    onEditCategory: (Anime) -> Unit,
    onAnimeClick: (String) -> Unit,
    onPlayClick: (Anime) -> Unit
) {
    val savedEntries = remember(uiState.allAnime, uiState.watchlist, uiState.selectedWatchlistFilter) {
        uiState.allAnime.mapNotNull { anime ->
            val category = uiState.watchlist[anime.id] ?: return@mapNotNull null
            if (uiState.selectedWatchlistFilter != null && category != uiState.selectedWatchlistFilter) {
                return@mapNotNull null
            }
            anime to category
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipPill(
                    text = "SEMUA (${uiState.watchlist.size})",
                    selected = uiState.selectedWatchlistFilter == null,
                    onClick = { onSelectFilter(null) }
                )
            }
            items(WatchlistCategory.entries) { cat ->
                val count = uiState.watchlist.values.count { it == cat }
                FilterChipPill(
                    text = "${cat.label.uppercase()} ($count)",
                    selected = uiState.selectedWatchlistFilter == cat,
                    onClick = { onSelectFilter(cat) }
                )
            }
        }

        if (savedEntries.isEmpty()) {
            EmptyLibraryState(
                icon = Icons.Rounded.BookmarkBorder,
                title = "Daftar tonton masih kosong",
                subtitle = "Simpan anime favorit dari Beranda atau Detail untuk melacak progres tontonan."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = bottomInsetPadding
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedEntries, key = { it.first.id }) { (anime, category) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                            .clickable { onAnimeClick(anime.id) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .aspectRatio(0.72f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
                        ) {
                            AnimePosterArtwork(
                                artTheme = anime.artTheme,
                                title = anime.title,
                                modifier = Modifier.fillMaxSize(),
                                showKanjiWatermark = false
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GoldTint)
                                        .border(1.dp, GoldBorder, RoundedCornerShape(4.dp))
                                        .clickable { onEditCategory(anime) }
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${category.label.uppercase()} [UBAH]",
                                        fontFamily = MonoFont,
                                        color = GoldBright,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = GoldBright,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${anime.rating}",
                                        fontFamily = MonoFont,
                                        color = GoldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = anime.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "EP ${anime.releasedEpisodes}/${anime.totalEpisodes} // ${anime.studio}",
                                fontFamily = MonoFont,
                                fontSize = 10.sp,
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onPlayClick(anime) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldGradientBrush)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Tonton",
                                tint = TextOnGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryContent(
    allAnime: List<Anime>,
    history: List<WatchProgress>,
    bottomInsetPadding: Dp,
    onClearAll: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onAnimeClick: (String) -> Unit,
    onResumeClick: (WatchProgress) -> Unit
) {
    if (history.isEmpty()) {
        EmptyLibraryState(
            icon = Icons.Rounded.History,
            title = "Belum ada riwayat tonton",
            subtitle = "Episode yang kamu putar akan tercatat di sini agar mudah dilanjutkan."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 6.dp,
                bottom = bottomInsetPadding
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${history.size} ANIME TERAKHIR DITONTON",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    TextButton(onClick = onClearAll) {
                        Text(
                            "BERSIHKAN SEMUA",
                            fontFamily = MonoFont,
                            color = StatusLive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            items(history, key = { it.animeId }) { progress ->
                val anime = allAnime.find { it.id == progress.animeId } ?: return@items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianCard)
                        .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                        .clickable { onResumeClick(progress) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(104.dp)
                            .aspectRatio(16f / 10f)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
                    ) {
                        AnimePosterArtwork(
                            artTheme = anime.artTheme,
                            title = anime.title,
                            modifier = Modifier.fillMaxSize(),
                            showKanjiWatermark = false
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianVoid.copy(alpha = 0.85f))
                                .border(1.dp, GoldBright, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Lanjut",
                                tint = GoldBright,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onAnimeClick(anime.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GoldTagChip(text = "EP ${progress.episodeNumber}", highlighted = true)
                            Text(
                                text = progress.updatedAtLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = anime.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AnalogMeterGauge(
                            progress = progress.progressFraction.coerceAtLeast(0.06f),
                            segments = 14,
                            height = 6.dp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${formatTimeSeconds(progress.positionSeconds)} / ${formatTimeSeconds(progress.durationSeconds)}",
                            fontFamily = MonoFont,
                            fontSize = 10.5.sp,
                            color = GoldPrimary
                        )
                    }

                    IconButton(onClick = { onRemoveItem(anime.id) }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Hapus",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsContent(
    downloads: List<DownloadTask>,
    bottomInsetPadding: Dp,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onRemove: (String) -> Unit,
    onPlayOffline: (DownloadTask) -> Unit,
    onAnimeClick: (String) -> Unit
) {
    val totalUsedMb = downloads.sumOf { it.downloadedMb }
    val maxBudgetMb = 4096

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = bottomInsetPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Storage,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PENYIMPANAN TAPE OFFLINE",
                            fontFamily = MonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "$totalUsedMb MB / 4 GB",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldBright
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                AnalogMeterGauge(
                    progress = (totalUsedMb.toFloat() / maxBudgetMb).coerceIn(0.02f, 1f),
                    segments = 24,
                    height = 9.dp
                )
            }
        }

        if (downloads.isEmpty()) {
            item {
                EmptyLibraryState(
                    icon = Icons.Rounded.DownloadDone,
                    title = "Belum ada unduhan offline",
                    subtitle = "Unduh episode dari halaman Detail atau Player untuk ditonton tanpa internet."
                )
            }
        } else {
            items(downloads, key = { it.episodeId }) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianCard)
                        .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            if (task.status == DownloadStatus.COMPLETED) onPlayOffline(task)
                            else onAnimeClick(task.animeId)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .aspectRatio(0.76f)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
                    ) {
                        AnimePosterArtwork(
                            artTheme = task.artTheme,
                            title = task.animeTitle,
                            modifier = Modifier.fillMaxSize(),
                            showKanjiWatermark = false
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GoldTagChip(
                                text = "EP ${task.episodeNumber} // ${task.qualityLabel}",
                                highlighted = task.status == DownloadStatus.COMPLETED
                            )
                            Text(
                                text = when (task.status) {
                                    DownloadStatus.COMPLETED -> "${task.totalMb} MB"
                                    DownloadStatus.DOWNLOADING -> "${(task.progress * 100).toInt()}%"
                                    DownloadStatus.PAUSED -> "PAUSE ${(task.progress * 100).toInt()}%"
                                },
                                fontFamily = MonoFont,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.status == DownloadStatus.COMPLETED) StatusEmerald else GoldBright
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.animeTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = task.episodeTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (task.status != DownloadStatus.COMPLETED) {
                            Spacer(modifier = Modifier.height(7.dp))
                            AnalogMeterGauge(
                                progress = task.progress.coerceIn(0.02f, 1f),
                                segments = 14,
                                height = 6.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    when (task.status) {
                        DownloadStatus.COMPLETED -> {
                            IconButton(
                                onClick = { onPlayOffline(task) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldGradientBrush)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Putar Offline",
                                    tint = TextOnGold
                                )
                            }
                        }

                        DownloadStatus.DOWNLOADING -> {
                            IconButton(
                                onClick = { onPause(task.episodeId) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldTint)
                                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                            ) {
                                TapeReelSpinner(isSpinning = true, size = 18.dp, tint = GoldBright)
                            }
                        }

                        DownloadStatus.PAUSED -> {
                            IconButton(
                                onClick = { onResume(task.episodeId) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldTint)
                                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Downloading,
                                    contentDescription = "Lanjutkan",
                                    tint = GoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { onRemove(task.episodeId) }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Hapus Unduhan",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (selected) GoldGradientBrush
                else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
            )
            .border(1.dp, if (selected) GoldBright else GoldSubtleBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            fontFamily = MonoFont,
            color = if (selected) TextOnGold else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyLibraryState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldTint)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldBright,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
