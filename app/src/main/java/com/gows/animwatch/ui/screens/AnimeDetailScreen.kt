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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.RatingDialog
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.components.TapeReelSpinner
import com.gows.animwatch.ui.components.WatchlistCategorySheet
import com.gows.animwatch.ui.components.formatTimeSeconds
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.MonoFont
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
    onRateAnime: (String, Int) -> Unit,
    onToggleReminder: (String) -> Unit,
    onDownloadEpisode: (Anime, Episode) -> Unit,
    onGenreClick: (String) -> Unit,
    onRelatedAnimeClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onPostComment: (String, Int?, String, Boolean) -> Unit,
    onToggleCommentLike: (String) -> Unit,
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
        // 1. CRT Monitor Backdrop Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            ) {
                AnimePosterArtwork(
                    artTheme = anime.artTheme,
                    title = anime.title,
                    modifier = Modifier.fillMaxSize(),
                    showVignette = false
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to ObsidianVoid.copy(alpha = 0.58f),
                                0.45f to ObsidianVoid.copy(alpha = 0.35f),
                                0.80f to ObsidianVoid.copy(alpha = 0.90f),
                                1.0f to ObsidianVoid
                            )
                        )
                )

                // Top Nav Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianVoid.copy(alpha = 0.82f))
                            .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (anime.status == AnimeStatus.ONGOING) {
                            IconButton(
                                onClick = { onToggleReminder(anime.id) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianVoid.copy(alpha = 0.82f))
                                    .border(1.dp, if (hasReminder) GoldBright else GoldBorder, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                                    contentDescription = "Pengingat Episode",
                                    tint = if (hasReminder) GoldBright else TextPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = { onShareClick(anime.title) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianVoid.copy(alpha = 0.82f))
                                .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Bagikan",
                                tint = TextPrimary
                            )
                        }
                    }
                }

                // Poster + Primary Info Row anchored at bottom of backdrop
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(106.dp)
                            .aspectRatio(0.70f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, GoldBorder, RoundedCornerShape(8.dp))
                    ) {
                        AnimePosterArtwork(
                            artTheme = anime.artTheme,
                            title = anime.title,
                            modifier = Modifier.fillMaxSize()
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
                            GoldTagChip(text = anime.type.label)
                            GoldTagChip(text = anime.ageRating)
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
                            color = GoldPrimary,
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
                                    tint = GoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${anime.rating}",
                                    fontFamily = MonoFont,
                                    color = GoldBright,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = " (${anime.ratingCountLabel})",
                                    fontFamily = MonoFont,
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                            if (userRating > 0) {
                                Text(
                                    text = "// KAMU: $userRating",
                                    fontFamily = MonoFont,
                                    color = GoldBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
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
                    text = "${anime.studio.uppercase()} // ${anime.season.uppercase()} // ${anime.releasedEpisodes}/${anime.totalEpisodes} EP (${anime.durationMinutes}M)",
                    fontFamily = MonoFont,
                    fontSize = 10.5.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                val playLabel = if (watchProgress != null) {
                    "Lanjutkan Ep ${watchProgress.episodeNumber} (${formatTimeSeconds(watchProgress.positionSeconds)})"
                } else {
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
                    DetailQuickAction(
                        icon = Icons.Rounded.Download,
                        label = "Unduh Ep.${anime.episodes.lastOrNull()?.number ?: 1}",
                        active = false,
                        onClick = {
                            anime.episodes.lastOrNull()?.let { ep ->
                                onDownloadEpisode(anime, ep)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Synopsis Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianCard)
                        .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
                        .clickable { synopsisExpanded = !synopsisExpanded }
                        .animateContentSize()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "RINGKASAN CERITA",
                        fontFamily = MonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GoldBright,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = anime.synopsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = if (synopsisExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (synopsisExpanded) "[ TUTUP SINOPSIS ]" else "[ BACA SELENGKAPNYA ]",
                        fontFamily = MonoFont,
                        color = GoldPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    anime.genres.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTint)
                                .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
                                .clickable { onGenreClick(genre) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = genre.uppercase(),
                                fontFamily = MonoFont,
                                color = GoldBright,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Detail Tabs Selector
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (selected) GoldGradientBrush
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tab.label.uppercase()}$suffix",
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
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Tab Content
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
                            text = "DAFTAR EPISODE (${anime.episodes.size})",
                            fontFamily = MonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ObsidianCard)
                                    .border(1.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
                                    .clickable { sortDescending = !sortDescending }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (sortDescending) "TERBARU" else "EP 1->N",
                                    fontFamily = MonoFont,
                                    color = TextSecondary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ObsidianCard)
                                    .border(1.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
                                    .clickable { episodeGridMode = !episodeGridMode }
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (episodeGridMode) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                    contentDescription = "Ubah Tampilan Episode",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
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
                                        .width(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isWatched) GoldGradientBrush else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard)))
                                        .border(
                                            1.dp,
                                            if (isWatched) GoldBright else GoldSubtleBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onPlayEpisode(anime.id, ep.id) }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "EP ${ep.number}",
                                        fontFamily = MonoFont,
                                        color = if (isWatched) TextOnGold else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
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
                            Text("Data karakter akan diperbarui dari API.", color = TextTertiary)
                        }
                    }
                } else {
                    items(anime.characters) { charRole ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianCard)
                                .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldGradientBrush),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = charRole.emblem,
                                    fontFamily = MonoFont,
                                    color = TextOnGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
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
                                    fontFamily = MonoFont,
                                    fontSize = 11.sp,
                                    color = GoldPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "SEIYUU",
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

            DetailTab.RELATED -> {
                item {
                    if (relatedAnime.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada rekomendasi terkait.", color = TextTertiary)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        TactileInputField(
                            label = "Tulis Komentar",
                            value = commentDraft,
                            onValueChange = { commentDraft = it },
                            placeholder = "Tulis pendapatmu tentang anime ini...",
                            singleLine = false,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(6.dp))
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
                                        checkedColor = GoldBright,
                                        uncheckedColor = TextTertiary,
                                        checkmarkColor = TextOnGold
                                    )
                                )
                                Text(
                                    "TANDAI SPOILER",
                                    fontFamily = MonoFont,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            GoldGradientButton(
                                text = "Kirim",
                                icon = Icons.AutoMirrored.Rounded.Send,
                                height = 38.dp,
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
                            Text("Jadilah yang pertama berkomentar!", color = TextTertiary)
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
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) GoldTintStrong else ObsidianCard)
            .border(1.dp, if (active) GoldBright else GoldSubtleBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) GoldBright else GoldPrimary,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            fontFamily = MonoFont,
            color = if (active) GoldBright else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
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
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianCard)
            .border(1.dp, if (watchFraction > 0f) GoldBorder else GoldSubtleBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onPlayClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(102.dp)
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
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
                    contentDescription = "Putar",
                    tint = GoldBright,
                    modifier = Modifier.size(17.dp)
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
                    text = "EPISODE ${episode.number}",
                    fontFamily = MonoFont,
                    color = GoldBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (episode.isFiller) {
                    GoldTagChip(text = "FILLER")
                }
                Text(
                    text = "// ${episode.durationSeconds / 60}M",
                    fontFamily = MonoFont,
                    color = TextTertiary,
                    fontSize = 10.sp
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
                Spacer(modifier = Modifier.height(5.dp))
                AnalogMeterGauge(
                    progress = watchFraction.coerceIn(0.06f, 1f),
                    segments = 12,
                    height = 5.dp
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = episode.releaseDate.uppercase(),
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
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onDownloadClick)
                        .padding(4.dp)
                ) {
                    TapeReelSpinner(isSpinning = true, size = 22.dp, tint = GoldBright)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${(downloadTask.progress * 100).toInt()}%",
                        fontFamily = MonoFont,
                        fontSize = 9.sp,
                        color = GoldBright,
                        fontWeight = FontWeight.Bold
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
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianCard)
            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldTintStrong)
                        .border(0.75.dp, GoldBorder, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comment.author.take(1).uppercase(),
                        fontFamily = MonoFont,
                        color = GoldBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.author,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                GoldTagChip(text = comment.authorBadge, highlighted = comment.authorBadge == "GOLD VIP")
            }
            Text(
                text = comment.timeAgo,
                fontFamily = MonoFont,
                fontSize = 10.sp,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (comment.isSpoiler && !spoilerRevealed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianCardAlt)
                    .border(1.dp, StatusLive.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .clickable(onClick = onRevealSpoiler)
                    .padding(10.dp),
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
                    text = "MENGANDUNG SPOILER // KETUK UNTUK BUKA",
                    fontFamily = MonoFont,
                    color = StatusLive,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (comment.episodeNumber != null) {
                Text(
                    text = "EPISODE ${comment.episodeNumber}",
                    fontFamily = MonoFont,
                    fontSize = 10.sp,
                    color = GoldPrimary
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (comment.isLiked) GoldTintStrong else ObsidianCardAlt)
                    .border(
                        0.75.dp,
                        if (comment.isLiked) GoldBright else GoldSubtleBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(onClick = onLikeClick)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ThumbUp,
                    contentDescription = "Suka",
                    tint = if (comment.isLiked) GoldBright else TextTertiary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${comment.likes}",
                    fontFamily = MonoFont,
                    color = if (comment.isLiked) GoldBright else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
