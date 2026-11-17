package com.gows.animwatch.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
import com.gows.animwatch.data.model.ReleaseDay
import com.gows.animwatch.data.model.SortOption
import com.gows.animwatch.ui.components.AnimePosterArtwork
import com.gows.animwatch.ui.components.AnimePosterCard
import com.gows.animwatch.ui.components.CompactAnimeRowCard
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.state.ExploreFilterState
import com.gows.animwatch.ui.state.ExploreMode
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    uiState: AnimWatchUiState,
    filteredAnime: List<Anime>,
    bottomInsetPadding: Dp,
    onSelectMode: (ExploreMode) -> Unit,
    onQueryChange: (String) -> Unit,
    onCommitSearch: (String) -> Unit,
    onRemoveSearchHistory: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onToggleGenre: (String) -> Unit,
    onStatusChange: (AnimeStatus?) -> Unit,
    onTypeChange: (AnimeType?) -> Unit,
    onYearChange: (Int?) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onToggleGridView: () -> Unit,
    onResetFilters: () -> Unit,
    onSelectScheduleDay: (ReleaseDay) -> Unit,
    onToggleReminder: (String) -> Unit,
    onAnimeClick: (String) -> Unit,
    onPlayAnime: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val filter = uiState.exploreFilter

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
    ) {
        // Header Title + Mode Switcher
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = "Jelajah Anime",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Mode Segmented Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianElevated)
                    .border(1.dp, GoldSubtleBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExploreMode.entries.forEach { mode ->
                    val selected = uiState.exploreMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) GoldGradientBrush
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            color = if (selected) TextOnGold else TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        when (uiState.exploreMode) {
            ExploreMode.CATALOG -> {
                CatalogExploreContent(
                    uiState = uiState,
                    filteredAnime = filteredAnime,
                    bottomInsetPadding = bottomInsetPadding,
                    onQueryChange = onQueryChange,
                    onCommitSearch = onCommitSearch,
                    onRemoveSearchHistory = onRemoveSearchHistory,
                    onClearSearchHistory = onClearSearchHistory,
                    onToggleGenre = onToggleGenre,
                    onOpenFilterSheet = { showFilterSheet = true },
                    onToggleGridView = onToggleGridView,
                    onResetFilters = onResetFilters,
                    onAnimeClick = onAnimeClick,
                    onPlayAnime = onPlayAnime,
                    onBookmarkClick = onBookmarkClick
                )
            }

            ExploreMode.SCHEDULE -> {
                ScheduleExploreContent(
                    allAnime = uiState.allAnime,
                    selectedDay = uiState.selectedScheduleDay,
                    reminders = uiState.reminders,
                    bottomInsetPadding = bottomInsetPadding,
                    onSelectDay = onSelectScheduleDay,
                    onToggleReminder = onToggleReminder,
                    onAnimeClick = onAnimeClick,
                    onPlayAnime = onPlayAnime
                )
            }

            ExploreMode.RANKING -> {
                RankingExploreContent(
                    allAnime = uiState.allAnime,
                    watchlist = uiState.watchlist.keys,
                    bottomInsetPadding = bottomInsetPadding,
                    onAnimeClick = onAnimeClick,
                    onPlayAnime = onPlayAnime,
                    onBookmarkClick = onBookmarkClick
                )
            }
        }
    }

    if (showFilterSheet) {
        ExploreFilterBottomSheet(
            filter = filter,
            genres = uiState.genres,
            onToggleGenre = onToggleGenre,
            onStatusChange = onStatusChange,
            onTypeChange = onTypeChange,
            onYearChange = onYearChange,
            onSortChange = onSortChange,
            onReset = onResetFilters,
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogExploreContent(
    uiState: AnimWatchUiState,
    filteredAnime: List<Anime>,
    bottomInsetPadding: Dp,
    onQueryChange: (String) -> Unit,
    onCommitSearch: (String) -> Unit,
    onRemoveSearchHistory: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onToggleGenre: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onToggleGridView: () -> Unit,
    onResetFilters: () -> Unit,
    onAnimeClick: (String) -> Unit,
    onPlayAnime: (String) -> Unit,
    onBookmarkClick: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val filter = uiState.exploreFilter

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Box + Filter & Layout Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldSubtleBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (filter.query.isEmpty()) {
                        Text(
                            text = "Cari judul, studio, atau genre...",
                            color = TextTertiary,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = filter.query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        cursorBrush = SolidColor(GoldBright),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onCommitSearch(filter.query)
                                focusManager.clearFocus()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (filter.query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Hapus pencarian",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Filter Button with active badge
            Box {
                IconButton(
                    onClick = onOpenFilterSheet,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (filter.activeFilterCount > 0) GoldTintStrong else ObsidianCard)
                        .border(
                            1.dp,
                            if (filter.activeFilterCount > 0) GoldBright else GoldSubtleBorder,
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Filter",
                        tint = if (filter.activeFilterCount > 0) GoldBright else TextPrimary
                    )
                }
                if (filter.activeFilterCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(GoldBright),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${filter.activeFilterCount}",
                            color = TextOnGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Grid/List Switch
            IconButton(
                onClick = onToggleGridView,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldSubtleBorder, RoundedCornerShape(14.dp))
            ) {
                Icon(
                    imageVector = if (filter.isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                    contentDescription = "Ubah Tampilan",
                    tint = GoldPrimary
                )
            }
        }

        // Quick Genre Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val allSelected = filter.selectedGenres.isEmpty()
                GenrePillChip(
                    label = "Semua",
                    selected = allSelected,
                    onClick = {
                        if (!allSelected) onResetFilters()
                    }
                )
            }
            items(uiState.genres) { genre ->
                GenrePillChip(
                    label = genre,
                    selected = genre in filter.selectedGenres,
                    onClick = { onToggleGenre(genre) }
                )
            }
        }

        // Search History Strip when query is empty and filters inactive
        if (filter.query.isEmpty() && filter.activeFilterCount == 0 && uiState.searchHistory.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pencarian Terakhir",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Hapus Semua",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClearSearchHistory)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.searchHistory.forEach { term ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(ObsidianCard)
                                .border(0.75.dp, DividerSubtle, RoundedCornerShape(20.dp))
                                .clickable { onQueryChange(term) }
                                .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = term,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Hapus",
                                tint = TextTertiary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onRemoveSearchHistory(term) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // Result summary bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredAnime.size} Judul Ditemukan • ${filter.sortOption.label}",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )
            if (filter.activeFilterCount > 0 || filter.query.isNotEmpty()) {
                Text(
                    text = "Reset Filter",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldBright,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onResetFilters)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (filteredAnime.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = GoldPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Anime tidak ditemukan",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Coba ubah kata kunci pencarian atau reset filter genre yang aktif.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GoldGradientButton(
                        text = "Reset Semua Filter",
                        icon = Icons.Rounded.RestartAlt,
                        onClick = onResetFilters
                    )
                }
            }
        } else if (filter.isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = bottomInsetPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredAnime, key = { it.id }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isBookmarked = anime.id in uiState.watchlist,
                        onClick = { onAnimeClick(anime.id) },
                        onBookmarkClick = { onBookmarkClick(anime.id) },
                        cardWidth = null
                    )
                }
            }
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
                items(filteredAnime, key = { it.id }) { anime ->
                    CompactAnimeRowCard(
                        anime = anime,
                        isBookmarked = anime.id in uiState.watchlist,
                        onClick = { onAnimeClick(anime.id) },
                        onPlayClick = { onPlayAnime(anime.id) },
                        onBookmarkClick = { onBookmarkClick(anime.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleExploreContent(
    allAnime: List<Anime>,
    selectedDay: ReleaseDay,
    reminders: Set<String>,
    bottomInsetPadding: Dp,
    onSelectDay: (ReleaseDay) -> Unit,
    onToggleReminder: (String) -> Unit,
    onAnimeClick: (String) -> Unit,
    onPlayAnime: (String) -> Unit
) {
    val today = remember { AnimWatchUiState.currentReleaseDay() }
    val dayAnime = remember(allAnime, selectedDay) {
        allAnime.filter { it.releaseDay == selectedDay }
            .sortedBy { it.releaseTimeWib ?: "23:59" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Day Strip Selector
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ReleaseDay.entries) { day ->
                val selected = day == selectedDay
                val isToday = day == today
                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) GoldGradientBrush
                            else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
                        )
                        .border(
                            1.dp,
                            if (selected) Color.Transparent else if (isToday) GoldBright else GoldSubtleBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectDay(day) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.shortLabel.uppercase(),
                        color = if (selected) TextOnGold else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isToday) "Hari Ini" else "${allAnime.count { it.releaseDay == day }} Ep",
                        color = if (selected) TextOnGold.copy(alpha = 0.85f) else if (isToday) GoldBright else TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (dayAnime.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tidak ada jadwal rilis hari ${selectedDay.label}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pilih hari lain di atas untuk melihat anime mingguan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
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
                    Text(
                        text = "Jadwal Rilis Hari ${selectedDay.label}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldBright
                    )
                }
                items(dayAnime, key = { it.id }) { anime ->
                    val hasReminder = anime.id in reminders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
                            .clickable { onAnimeClick(anime.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(58.dp)
                        ) {
                            Text(
                                text = (anime.releaseTimeWib ?: "20:00").substringBefore(" "),
                                color = GoldBright,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "WIB",
                                color = TextTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(54.dp)
                                .background(GoldBorder)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(width = 52.dp, height = 72.dp)
                                .clip(RoundedCornerShape(10.dp))
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
                            GoldTagChip(
                                text = if (anime.status == AnimeStatus.ONGOING)
                                    "EP ${anime.releasedEpisodes + 1} SEGERA"
                                else "TAMAT",
                                highlighted = anime.status == AnimeStatus.ONGOING
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = anime.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = anime.genres.take(2).joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(
                                onClick = { onToggleReminder(anime.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (hasReminder) GoldTintStrong else ObsidianCardAlt)
                            ) {
                                Icon(
                                    imageVector = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                                    contentDescription = "Pengingat",
                                    tint = if (hasReminder) GoldBright else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            IconButton(
                                onClick = { onPlayAnime(anime.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(GoldGradientBrush)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Tonton",
                                    tint = TextOnGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingExploreContent(
    allAnime: List<Anime>,
    watchlist: Set<String>,
    bottomInsetPadding: Dp,
    onAnimeClick: (String) -> Unit,
    onPlayAnime: (String) -> Unit,
    onBookmarkClick: (String) -> Unit
) {
    val ranked = remember(allAnime) {
        allAnime.sortedByDescending { it.rating }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = bottomInsetPadding
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Peringkat Teratas Komunitas",
                style = MaterialTheme.typography.titleMedium,
                color = GoldBright
            )
            Text(
                text = "Berdasarkan skor ulasan, konsistensi episode, dan total tayangan",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        itemsIndexed(ranked, key = { _, anime -> anime.id }) { index, anime ->
            CompactAnimeRowCard(
                anime = anime,
                isBookmarked = anime.id in watchlist,
                onClick = { onAnimeClick(anime.id) },
                onPlayClick = { onPlayAnime(anime.id) },
                onBookmarkClick = { onBookmarkClick(anime.id) },
                rankNumber = index + 1
            )
        }
    }
}

@Composable
private fun GenrePillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) GoldGradientBrush
                else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
            )
            .border(
                1.dp,
                if (selected) Color.Transparent else GoldSubtleBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (selected) TextOnGold else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExploreFilterBottomSheet(
    filter: ExploreFilterState,
    genres: List<String>,
    onToggleGenre: (String) -> Unit,
    onStatusChange: (AnimeStatus?) -> Unit,
    onTypeChange: (AnimeType?) -> Unit,
    onYearChange: (Int?) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianElevated,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Urutkan",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoldBright
                )
                TextButton(onClick = onReset) {
                    Text("Reset", color = GoldPrimary)
                }
            }

            HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(vertical = 8.dp))

            // Sort
            Text("Urutkan Berdasarkan", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOption.entries.forEach { sort ->
                    GenrePillChip(
                        label = sort.label,
                        selected = filter.sortOption == sort,
                        onClick = { onSortChange(sort) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status
            Text("Status Tayang", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("Semua", filter.statusFilter == null) { onStatusChange(null) }
                AnimeStatus.entries.forEach { status ->
                    GenrePillChip(status.label, filter.statusFilter == status) { onStatusChange(status) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type
            Text("Format Rilis", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("Semua", filter.typeFilter == null) { onTypeChange(null) }
                AnimeType.entries.forEach { type ->
                    GenrePillChip(type.label, filter.typeFilter == type) { onTypeChange(type) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Year
            Text("Tahun Rilis", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("Semua", filter.yearFilter == null) { onYearChange(null) }
                listOf(2026, 2025, 2024).forEach { yr ->
                    GenrePillChip("$yr", filter.yearFilter == yr) { onYearChange(yr) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Genres
            Text("Genre (Multi-pilih)", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                genres.forEach { g ->
                    GenrePillChip(g, g in filter.selectedGenres) { onToggleGenre(g) }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            GoldGradientButton(
                text = "Terapkan Filter",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
