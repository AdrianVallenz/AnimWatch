package com.gows.animwatch.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.CommentItem
import com.gows.animwatch.data.model.DownloadStatus
import com.gows.animwatch.data.model.DownloadTask
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.ui.components.AnalogMeterGauge
import com.gows.animwatch.ui.components.AnimePosterArtwork
import com.gows.animwatch.ui.components.AnimePosterCard
import com.gows.animwatch.ui.components.CassetteLinearShimmer
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.RatingDialog
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.components.TapeReelSpinner
import com.gows.animwatch.ui.components.WatchlistCategorySheet
import com.gows.animwatch.ui.components.formatTimeSeconds
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.MediaScrimDark
import com.gows.animwatch.ui.theme.MediaTextLight
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

private enum class DetailTab(val label: String) {
    EPISODES("Episode"),
    CHARACTERS("Karakter"),
    RELATED("Terkait"),
    COMMENTS("Komentar")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailScreen(
    anime: Anime,
    uiState: AnimWatchUiState,
    bottomInsetPadding: Dp,
    onBack: () -> Unit,
    onPlayEpisode: (String, String?) -> Unit,
    onSelectWatchlist: (String, WatchlistCategory?) -> Unit,
    onUpdateWatchedEpisodes: (String, Int) -> Unit,
    onRateAnime: (String, Int) -> Unit,
    onToggleReminder: (String) -> Unit,
    onDownloadEpisode: (Anime, Episode) -> Unit,
    onGenreClick: (String) -> Unit,
    onRelatedAnimeClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onPostComment: (String, Int?, String, Boolean) -> Unit,
    onToggleCommentLike: (String) -> Unit,
    onShowMessage: (String) -> Unit = {},
    onShareClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(DetailTab.EPISODES) }
    var synopsisExpanded by remember { mutableStateOf(false) }
    var sortDescending by remember { mutableStateOf(false) }
    var episodeGridMode by remember { mutableStateOf(false) }
    var showWatchlistSheet by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    var commentDraft by remember { mutableStateOf("") }
    var isSpoilerDraft by remember { mutableStateOf(false) }
    var revealedSpoilers by remember { mutableStateOf(setOf<String>()) }

    val watchlistCategory = uiState.watchlist[anime.id]
    val watchedEpisodesCount = uiState.watchedEpisodes[anime.id] ?: 0
    val maxTrackableEps = maxOf(anime.totalEpisodes, anime.releasedEpisodes, anime.episodes.size, 1)
    val isCompletedAnime = watchlistCategory == WatchlistCategory.COMPLETED ||
        (watchedEpisodesCount > 0 && watchedEpisodesCount >= maxTrackableEps)
    val userRating = uiState.userRatings[anime.id] ?: 0
    val watchProgress = uiState.watchHistory.find { it.animeId == anime.id }
    val hasReminder = anime.id in uiState.reminders

    val displayedEpisodes = remember(anime.episodes, sortDescending) {
        if (sortDescending) anime.episodes.sortedByDescending { it.number }
        else anime.episodes.sortedBy { it.number }
    }
    val relatedAnime = remember(anime.relatedIds, uiState.allAnime) {
        uiState.allAnime.filter { it.id in anime.relatedIds }
    }
    val animeComments = remember(uiState.comments, anime.id) {
        uiState.comments.filter { it.animeId == anime.id }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid),
        contentPadding = PaddingValues(bottom = bottomInsetPadding)
    ) {
        // 1. Soft Editorial Backdrop Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(318.dp)
            ) {
                AnimePosterArtwork(
                    artTheme = anime.artTheme,
                    title = anime.title,
                    posterUrl = anime.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                    showVignette = false,
                    decodeSizePx = 640
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to MediaScrimDark.copy(alpha = 0.48f),
                                0.42f to ObsidianVoid.copy(alpha = 0.30f),
                                0.78f to ObsidianVoid.copy(alpha = 0.92f),
                                1.0f to ObsidianVoid
                            )
                        )
                )

                // Soft circular top controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MediaScrimDark.copy(alpha = 0.62f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MediaTextLight
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (anime.status == AnimeStatus.ONGOING) {
                            IconButton(
                                onClick = { onToggleReminder(anime.id) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MediaScrimDark.copy(alpha = 0.62f))
                            ) {
                                Icon(
                                    imageVector = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                                    contentDescription = "Pengingat Episode",
                                    tint = if (hasReminder) GoldBright else MediaTextLight
                                )
                            }
                        }

                        IconButton(
                            onClick = { onShareClick(anime.title) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MediaScrimDark.copy(alpha = 0.62f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Bagikan",
                                tint = MediaTextLight
                            )
                        }
                    }
                }

                // Poster + Primary Info Row
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(108.dp)
                            .aspectRatio(0.70f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianCard)
                            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
                    ) {
                        AnimePosterArtwork(
                            artTheme = anime.artTheme,
                            title = anime.title,
                            posterUrl = anime.posterUrl,
                            modifier = Modifier.fillMaxSize(),
                            decodeSizePx = 320
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GoldTagChip(
                                text = anime.status.label,
                                highlighted = anime.status == AnimeStatus.ONGOING
                            )
                            GoldTagChip(
                                text = if (anime.isDubIndo) "Dub Indo" else "Sub Indo",
                                highlighted = anime.isDubIndo
                            )
                            GoldTagChip(text = anime.type.label)
                            GoldTagChip(text = anime.ageRating)
                            if (anime.isOfficialFree && !anime.officialChannel.isNullOrBlank()) {
                                GoldTagChip(text = anime.officialChannel)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = anime.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = anime.japaneseTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${anime.rating}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = " (${anime.ratingCountLabel})",
                                    color = TextTertiary,
                                    fontSize = 11.5.sp
                                )
                            }
                            if (userRating > 0) {
                                Text(
                                    text = "• Nilaimu: $userRating",
                                    color = GoldPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Metadata Strip & Primary Actions
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "${anime.studio} • ${anime.season} • ${anime.releasedEpisodes}/${anime.totalEpisodes} Ep (${anime.durationMinutes} mnt)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                val playLabel = when {
                    watchProgress != null ->
                        "Lanjutkan Ep ${watchProgress.episodeNumber} (${formatTimeSeconds(watchProgress.positionSeconds)})"
                    else ->
                        "Tonton Episode 1"
                }

                GoldGradientButton(
                    text = playLabel,
                    icon = Icons.Rounded.PlayArrow,
                    onClick = { onPlayEpisode(anime.id, watchProgress?.episodeId) },
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailQuickAction(
                        icon = if (watchlistCategory != null) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                        label = watchlistCategory?.label ?: "Simpan",
                        active = watchlistCategory != null,
                        onClick = { showWatchlistSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                    DetailQuickAction(
                        icon = Icons.Rounded.Star,
                        label = if (userRating > 0) "Nilai: $userRating/10" else "Beri Nilai",
                        active = userRating > 0,
                        onClick = { showRatingDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    val latestEp = anime.episodes.lastOrNull()
                    val latestDownload = latestEp?.let { ep -> uiState.downloads.find { it.episodeId == ep.id } }
                    DetailQuickAction(
                        icon = if (latestDownload?.status == DownloadStatus.COMPLETED) Icons.Rounded.CheckCircle else Icons.Rounded.Download,
                        label = when (latestDownload?.status) {
                            DownloadStatus.COMPLETED -> "Ep ${latestEp.number} Offline"
                            DownloadStatus.DOWNLOADING -> "Unduh ${(latestDownload.progress * 100).toInt()}%"
                            else -> "Unduh Ep ${latestEp?.number ?: 1}"
                        },
                        active = latestDownload != null,
                        onClick = {
                            if (latestEp != null) {
                                onDownloadEpisode(anime, latestEp)
                            } else {
                                onShowMessage("Daftar episode sedang dimuat, coba sesaat lagi")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pelacak Tontonan hanya aktif setelah menamatkan anime
                if (isCompletedAnime) {
                    Spacer(modifier = Modifier.height(14.dp))
                    val effectiveWatched = if (watchedEpisodesCount > 0) watchedEpisodesCount else maxTrackableEps
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(ObsidianCard)
                            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Column {
                                    Text(
                                        text = "Pelacak Tontonan",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Anime telah ditamatkan • Sinkron otomatis",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusEmerald
                                    )
                                }
                            }
                            Text(
                                text = "$effectiveWatched / $maxTrackableEps Ep",
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AnalogMeterGauge(
                            progress = (effectiveWatched.toFloat() / maxTrackableEps.toFloat()).coerceIn(0f, 1f),
                            height = 6.dp
                        )

                        Spacer(modifier = Modifier.height(11.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(GoldTint)
                                    .clickable { showWatchlistSheet = true }
                                    .padding(horizontal = 11.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = watchlistCategory?.label ?: "Tamat",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(ObsidianCardAlt)
                                        .border(0.75.dp, GoldSubtleBorder, CircleShape)
                                        .clickable(enabled = effectiveWatched > 0) {
                                            onUpdateWatchedEpisodes(anime.id, effectiveWatched - 1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Remove,
                                        contentDescription = "Kurangi Episode",
                                        tint = if (effectiveWatched > 0) TextPrimary else TextTertiary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(GoldGradientBrush)
                                        .clickable(enabled = effectiveWatched < maxTrackableEps) {
                                            onUpdateWatchedEpisodes(anime.id, effectiveWatched + 1)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Tambah Episode",
                                        tint = TextOnGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (effectiveWatched >= maxTrackableEps) "Tamat" else "1 Ep",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextOnGold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Soft Synopsis Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(ObsidianCard)
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                        .clickable { synopsisExpanded = !synopsisExpanded }
                        .animateContentSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sinopsis",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = anime.synopsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = if (synopsisExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (synopsisExpanded) "Sembunyikan" else "Baca selengkapnya",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    anime.genres.filterNot { it.equals("Dub Indo", ignoreCase = true) }.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GoldTint)
                                .clickable { onGenreClick(genre) }
                                .padding(horizontal = 13.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 3. Soft Capsule Detail Tabs
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DetailTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    val suffix = when (tab) {
                        DetailTab.EPISODES -> " (${anime.episodes.size})"
                        DetailTab.COMMENTS -> " (${animeComments.size})"
                        else -> ""
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (selected) GoldGradientBrush
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tab.label}$suffix",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) TextOnGold else TextSecondary,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Tab Content
        if (uiState.isLoadingDetail) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Memuat detail & daftar episode...",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CassetteLinearShimmer(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        when (selectedTab) {
            DetailTab.EPISODES -> {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daftar Episode (${anime.episodes.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ObsidianCard)
                                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                                    .clickable { sortDescending = !sortDescending }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (sortDescending) "Terbaru" else "Ep 1 → ${anime.episodes.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianCard)
                                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                                    .clickable { episodeGridMode = !episodeGridMode },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (episodeGridMode) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                    contentDescription = "Ubah Tampilan Episode",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (episodeGridMode) {
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displayedEpisodes.forEach { ep ->
                                val isWatched = watchProgress?.episodeId == ep.id
                                Box(
                                    modifier = Modifier
                                        .width(74.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isWatched) GoldGradientBrush else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard)))
                                        .border(
                                            if (isWatched) 0.dp else 0.75.dp,
                                            if (isWatched) Color.Transparent else GoldSubtleBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onPlayEpisode(anime.id, ep.id) }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Ep ${ep.number}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isWatched) TextOnGold else TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(displayedEpisodes, key = { it.id }) { ep ->
                        val downloadTask = uiState.downloads.find { it.episodeId == ep.id }
                        val epProgress = if (watchProgress?.episodeId == ep.id) watchProgress.progressFraction else 0f
                        EpisodeRowItem(
                            anime = anime,
                            episode = ep,
                            watchFraction = epProgress,
                            downloadTask = downloadTask,
                            onPlayClick = { onPlayEpisode(anime.id, ep.id) },
                            onDownloadClick = { onDownloadEpisode(anime, ep) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            DetailTab.CHARACTERS -> {
                if (anime.characters.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Data karakter akan segera diperbarui.", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                        }
                    }
                } else {
                    items(anime.characters) { charRole ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = ObsidianCard,
                            shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
                                    .padding(13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(GoldGradientBrush),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = charRole.emblem,
                                        color = TextOnGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = charRole.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = charRole.role,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Pengisi Suara",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                    Text(
                                        text = charRole.voiceActor,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            DetailTab.RELATED -> {
                item {
                    if (relatedAnime.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada rekomendasi terkait.", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(relatedAnime, key = { it.id }) { rel ->
                                AnimePosterCard(
                                    anime = rel,
                                    isBookmarked = rel.id in uiState.watchlist,
                                    onClick = { onRelatedAnimeClick(rel.id) },
                                    onBookmarkClick = { onBookmarkClick(rel.id) }
                                )
                            }
                        }
                    }
                }
            }

            DetailTab.COMMENTS -> {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianCard,
                        shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            TactileInputField(
                                label = "Tulis Komentar",
                                value = commentDraft,
                                onValueChange = { commentDraft = it },
                                placeholder = "Bagikan pendapatmu tentang anime ini...",
                                singleLine = false,
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { isSpoilerDraft = !isSpoilerDraft }
                                ) {
                                    Checkbox(
                                        checked = isSpoilerDraft,
                                        onCheckedChange = { isSpoilerDraft = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = GoldPrimary,
                                            uncheckedColor = TextTertiary,
                                            checkmarkColor = TextOnGold
                                        )
                                    )
                                    Text(
                                        "Tandai spoiler",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                }
                                GoldGradientButton(
                                    text = "Kirim",
                                    icon = Icons.AutoMirrored.Rounded.Send,
                                    height = 40.dp,
                                    onClick = {
                                        if (commentDraft.isNotBlank()) {
                                            onPostComment(anime.id, null, commentDraft, isSpoilerDraft)
                                            commentDraft = ""
                                            isSpoilerDraft = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (animeComments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Jadilah yang pertama berkomentar!", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                        }
                    }
                } else {
                    items(animeComments, key = { it.id }) { comment ->
                        CommentCardItem(
                            comment = comment,
                            spoilerRevealed = comment.id in revealedSpoilers,
                            onRevealSpoiler = { revealedSpoilers = revealedSpoilers + comment.id },
                            onLikeClick = { onToggleCommentLike(comment.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }

    if (showWatchlistSheet) {
        WatchlistCategorySheet(
            animeTitle = anime.title,
            currentCategory = watchlistCategory,
            onSelectCategory = { cat -> onSelectWatchlist(anime.id, cat) },
            onDismiss = { showWatchlistSheet = false }
        )
    }

    if (showRatingDialog) {
        RatingDialog(
            animeTitle = anime.title,
            initialScore = userRating,
            onConfirm = { score -> onRateAnime(anime.id, score) },
            onDismiss = { showRatingDialog = false }
        )
    }
}

@Composable
private fun DetailQuickAction(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) GoldTintStrong else ObsidianCard)
            .border(
                if (active) 1.dp else 0.75.dp,
                if (active) GoldPrimary else GoldSubtleBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) GoldBright else GoldPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) GoldBright else TextSecondary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EpisodeRowItem(
    anime: Anime,
    episode: Episode,
    watchFraction: Float,
    downloadTask: DownloadTask?,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianCard)
            .border(
                0.75.dp,
                if (watchFraction > 0f) GoldPrimary.copy(alpha = 0.45f) else GoldSubtleBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onPlayClick)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(102.dp)
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AnimePosterArtwork(
                artTheme = anime.artTheme,
                title = anime.title,
                posterUrl = episode.thumbnailUrl.ifBlank { anime.posterUrl },
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
                    contentDescription = "Putar",
                    tint = MediaTextLight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Episode ${episode.number}",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (episode.isFiller) {
                    GoldTagChip(text = "Filler")
                }
                Text(
                    text = "• ${episode.durationSeconds / 60} mnt",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (watchFraction > 0f) {
                Spacer(modifier = Modifier.height(6.dp))
                AnalogMeterGauge(
                    progress = watchFraction.coerceIn(0.06f, 1f),
                    height = 5.dp
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = episode.releaseDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        when (downloadTask?.status) {
            DownloadStatus.COMPLETED -> {
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Tersimpan",
                        tint = StatusEmerald
                    )
                }
            }

            DownloadStatus.DOWNLOADING -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDownloadClick)
                        .padding(4.dp)
                ) {
                    TapeReelSpinner(isSpinning = true, size = 20.dp, tint = GoldPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${(downloadTask.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.5.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            else -> {
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Unduh Episode",
                        tint = GoldPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun CommentCardItem(
    comment: CommentItem,
    spoilerRevealed: Boolean,
    onRevealSpoiler: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianCard)
            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(GoldTintStrong),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.author.take(1).uppercase(),
                            color = GoldPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = comment.author,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    GoldTagChip(text = comment.authorBadge, highlighted = comment.authorBadge == "PRO VIP")
                }
                Text(
                    text = comment.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            if (comment.isSpoiler && !spoilerRevealed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(StatusLive.copy(alpha = 0.08f))
                        .border(0.75.dp, StatusLive.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onRevealSpoiler)
                        .padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        tint = StatusLive,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mengandung spoiler — ketuk untuk membuka",
                        style = MaterialTheme.typography.labelMedium,
                        color = StatusLive,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (comment.episodeNumber != null) {
                    Text(
                        text = "Episode ${comment.episodeNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (comment.isLiked) GoldTintStrong else ObsidianCardAlt)
                        .clickable(onClick = onLikeClick)
                        .padding(horizontal = 11.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbUp,
                        contentDescription = "Suka",
                        tint = if (comment.isLiked) GoldPrimary else TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${comment.likes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (comment.isLiked) GoldPrimary else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
