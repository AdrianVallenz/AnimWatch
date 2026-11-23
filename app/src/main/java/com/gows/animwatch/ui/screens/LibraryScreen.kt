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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.WatchProgress
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.ui.components.AnalogMeterGauge
import com.gows.animwatch.ui.components.AnimePosterArtwork
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.formatTimeSeconds
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.state.LibraryTab
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.MediaScrimDark
import com.gows.animwatch.ui.theme.MediaTextLight
import com.gows.animwatch.ui.theme.ObsidianCard
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
    onUpdateWatchlistCategory: (String, WatchlistCategory?) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onAnimeClick: (String) -> Unit,
    onPlayAnime: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val animeById = remember(uiState.allAnime) { uiState.allAnime.associateBy { it.id } }
    val visibleSavedCount = remember(uiState.watchlist, animeById) {
        uiState.watchlist.keys.count { it in animeById }
    }
    val visibleHistoryCount = remember(uiState.watchHistory, animeById) {
        uiState.watchHistory.count { it.animeId in animeById }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Koleksiku",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LibraryTab.entries.forEach { tab ->
                    val selected = uiState.libraryTab == tab
                    val count = when (tab) {
                        LibraryTab.WATCHLIST -> visibleSavedCount
                        LibraryTab.HISTORY -> visibleHistoryCount
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (selected) GoldGradientBrush
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { onSelectLibraryTab(tab) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tab.label} ($count)",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) TextOnGold else TextSecondary,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
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
                onRemoveSaved = { animeId -> onUpdateWatchlistCategory(animeId, null) },
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
        }
    }
}

@Composable
private fun WatchlistContent(
    uiState: AnimWatchUiState,
    bottomInsetPadding: Dp,
    onRemoveSaved: (String) -> Unit,
    onAnimeClick: (String) -> Unit,
    onPlayClick: (Anime) -> Unit
) {
    val animeById = remember(uiState.allAnime) { uiState.allAnime.associateBy { it.id } }
    val savedEntries = remember(uiState.watchlist, animeById) {
        uiState.watchlist.entries.mapNotNull { (animeId, category) ->
            val anime = animeById[animeId] ?: return@mapNotNull null
            anime to category
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (savedEntries.isEmpty()) {
            EmptyLibraryState(
                icon = Icons.Rounded.BookmarkBorder,
                title = "Belum ada anime disimpan",
                subtitle = "Ketuk tombol Simpan pada anime favoritmu untuk memasukkannya ke daftar Disimpan."
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
                    Text(
                        text = "${savedEntries.size} anime dalam kategori Disimpan",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                items(savedEntries, key = { it.first.id }) { (anime, category) ->
                    val watchedCount = uiState.watchedEpisodes[anime.id] ?: 0
                    val maxTrackable = maxOf(anime.totalEpisodes, anime.releasedEpisodes, 1)
                    val isCompleted = category == WatchlistCategory.COMPLETED ||
                        (watchedCount > 0 && watchedCount >= maxTrackable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianCard)
                            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
                            .clickable { onAnimeClick(anime.id) }
                            .padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .aspectRatio(0.72f)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AnimePosterArtwork(
                                artTheme = anime.artTheme,
                                title = anime.title,
                                posterUrl = anime.posterUrl,
                                modifier = Modifier.fillMaxSize(),
                                showKanjiWatermark = false,
                                decodeSizePx = 240
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
                                        .clip(CircleShape)
                                        .background(GoldTint)
                                        .padding(horizontal = 9.dp, vertical = 3.5.dp)
                                ) {
                                    Text(
                                        text = if (isCompleted) "Disimpan • Tamat" else "Disimpan",
                                        color = GoldPrimary,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${anime.rating}",
                                        color = TextPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = anime.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            val shownWatched = if (watchedCount > 0) watchedCount else maxTrackable
                            Text(
                                text = if (isCompleted) {
                                    "Tamat • $shownWatched/$maxTrackable Ep • ${anime.studio}"
                                } else {
                                    "${anime.studio} • ${anime.releasedEpisodes}/${anime.totalEpisodes} Ep"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = if (isCompleted) StatusEmerald else TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isCompleted) {
                                Spacer(modifier = Modifier.height(5.dp))
                                AnalogMeterGauge(
                                    progress = (shownWatched.toFloat() / maxTrackable.toFloat()).coerceIn(0.05f, 1f),
                                    height = 4.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { onRemoveSaved(anime.id) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Hapus dari Disimpan",
                                tint = TextTertiary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { onPlayClick(anime) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
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
    val animeById = remember(allAnime) { allAnime.associateBy { it.id } }
    val visibleHistory = remember(history, animeById) {
        history.filter { it.animeId in animeById }
    }
    if (visibleHistory.isEmpty()) {
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
                        text = "${visibleHistory.size} anime terakhir ditonton",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    TextButton(onClick = onClearAll) {
                        Text(
                            "Bersihkan semua",
                            style = MaterialTheme.typography.labelMedium,
                            color = StatusLive,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            items(visibleHistory, key = { it.animeId }) { progress ->
                val anime = animeById[progress.animeId] ?: return@items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ObsidianCard)
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
                        .clickable { onResumeClick(progress) }
                        .padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(104.dp)
                            .aspectRatio(16f / 10f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AnimePosterArtwork(
                            artTheme = anime.artTheme,
                            title = anime.title,
                            posterUrl = anime.posterUrl,
                            modifier = Modifier.fillMaxSize(),
                            showKanjiWatermark = false,
                            decodeSizePx = 280
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MediaScrimDark.copy(alpha = 0.72f))
                                .border(1.dp, GoldBright.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Lanjut",
                                tint = MediaTextLight,
                                modifier = Modifier.size(18.dp)
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
                            GoldTagChip(text = "Ep ${progress.episodeNumber}", highlighted = true)
                            Text(
                                text = progress.updatedAtLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = anime.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        AnalogMeterGauge(
                            progress = progress.progressFraction.coerceAtLeast(0.06f),
                            height = 5.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatTimeSeconds(progress.positionSeconds)} / ${formatTimeSeconds(progress.durationSeconds)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
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
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(GoldTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
