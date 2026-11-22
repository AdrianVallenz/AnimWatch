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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
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
import com.gows.animwatch.ui.components.CassetteLinearShimmer
import com.gows.animwatch.ui.components.CompactAnimeRowCard
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.LedStatusDot
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.state.ExploreFilterState
import com.gows.animwatch.ui.state.ExploreMode
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.MonoFont
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusLive
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
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = "JELAJAH // DATABASE",
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
                ExploreMode.entries.forEach { mode ->
                    val selected = uiState.exploreMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (selected) GoldGradientBrush
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label.uppercase(),
                            fontFamily = MonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (selected) TextOnGold else TextSecondary,
                            letterSpacing = 0.5.sp
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
    var searchFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Static label above search input (Cassette Futurism input spec)
        Text(
            text = "KATA KUNCI PENCARIAN",
            fontFamily = MonoFont,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (searchFocused) GoldBright else TextTertiary,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

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
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(
                        width = if (searchFocused) 2.dp else 1.dp,
                        color = if (searchFocused) GoldBright else GoldBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (filter.query.isEmpty()) {
                        Text(
                            text = "Judul, studio, atau genre...",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { searchFocused = it.isFocused }
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

            Box {
                IconButton(
                    onClick = onOpenFilterSheet,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (filter.activeFilterCount > 0) GoldTintStrong else ObsidianCard)
                        .border(
                            1.dp,
                            if (filter.activeFilterCount > 0) GoldBright else GoldBorder,
                            RoundedCornerShape(8.dp)
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
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldBright),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${filter.activeFilterCount}",
                            fontFamily = MonoFont,
                            color = TextOnGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleGridView,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
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
                    label = "SEMUA",
                    selected = allSelected,
                    onClick = { if (!allSelected) onResetFilters() }
                )
            }
            items(uiState.genres) { genre ->
                GenrePillChip(
                    label = genre.uppercase(),
                    selected = genre in filter.selectedGenres,
                    onClick = { onToggleGenre(genre) }
                )
            }
        }

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
                            text = "RIWAYAT CARI",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "HAPUS SEMUA",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianCard)
                                .border(1.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
                                .clickable { onQueryChange(term) }
                                .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = term,
                                fontFamily = MonoFont,
                                fontSize = 11.sp,
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredAnime.size} HASIL // ${filter.sortOption.label.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )
            if (filter.activeFilterCount > 0 || filter.query.isNotEmpty()) {
                Text(
                    text = "RESET FILTER",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldBright,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onResetFilters)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (uiState.isSearchingRemote) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "MENCARI DI KUMANIME API...",
                    fontFamily = MonoFont,
                    fontSize = 10.sp,
                    color = GoldBright,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                CassetteLinearShimmer(modifier = Modifier.fillMaxWidth())
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
                        text = "ANIME TIDAK DITEMUKAN",
                        fontFamily = MonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ubah kata kunci pencarian atau reset filter genre yang sedang aktif.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GoldGradientButton(
                        text = "Reset Filter",
                        icon = Icons.Rounded.RestartAlt,
                        onClick = onResetFilters
                    )
                }
            }
        } else if (filter.isGridView) {
            // Asymmetric 2-column Bento layout (every 5th item spans 2 cols as wide spotlight deck; no 3-equal-columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = bottomInsetPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(
                    items = filteredAnime,
                    key = { _, anime -> anime.id },
                    span = { index, _ ->
                        GridItemSpan(if (index % 5 == 0) 2 else 1)
                    }
                ) { index, anime ->
                    if (index % 5 == 0) {
                        CompactAnimeRowCard(
                            anime = anime,
                            isBookmarked = anime.id in uiState.watchlist,
                            onClick = { onAnimeClick(anime.id) },
                            onPlayClick = { onPlayAnime(anime.id) },
                            onBookmarkClick = { onBookmarkClick(anime.id) },
                            trailingBadge = "SOROTAN"
                        )
                    } else {
                        AnimePosterCard(
                            anime = anime,
                            isBookmarked = anime.id in uiState.watchlist,
                            onClick = { onAnimeClick(anime.id) },
                            onBookmarkClick = { onBookmarkClick(anime.id) },
                            cardWidth = null
                        )
                    }
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
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ReleaseDay.entries) { day ->
                val selected = day == selectedDay
                val isToday = day == today
                Column(
                    modifier = Modifier
                        .width(66.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) GoldGradientBrush
                            else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
                        )
                        .border(
                            1.dp,
                            if (selected) GoldBright else if (isToday) GoldBright else GoldSubtleBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectDay(day) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.shortLabel.uppercase(),
                        fontFamily = MonoFont,
                        color = if (selected) TextOnGold else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isToday) "TODAY" else "${allAnime.count { it.releaseDay == day }} EP",
                        fontFamily = MonoFont,
                        color = if (selected) TextOnGold.copy(alpha = 0.85f) else if (isToday) GoldBright else TextTertiary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
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
                        text = "Pilih hari lain di atas untuk melihat siaran mingguan.",
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
                        text = "SIARAN HARI ${selectedDay.label.uppercase()}",
                        fontFamily = MonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldBright,
                        letterSpacing = 0.8.sp
                    )
                }
                items(dayAnime, key = { it.id }) { anime ->
                    val hasReminder = anime.id in reminders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                            .clickable { onAnimeClick(anime.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(56.dp)
                        ) {
                            Text(
                                text = (anime.releaseTimeWib ?: "20:00").substringBefore(" "),
                                fontFamily = MonoFont,
                                color = GoldBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "WIB",
                                fontFamily = MonoFont,
                                color = TextTertiary,
                                fontSize = 9.5.sp,
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
                                .size(width = 50.dp, height = 70.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
                        ) {
                            AnimePosterArtwork(
                                artTheme = anime.artTheme,
                                title = anime.title,
                                posterUrl = anime.posterUrl,
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
                                if (anime.status == AnimeStatus.ONGOING) {
                                    LedStatusDot(color = StatusLive, pulsing = true, size = 6.dp)
                                }
                                GoldTagChip(
                                    text = if (anime.status == AnimeStatus.ONGOING)
                                        "EP ${anime.releasedEpisodes + 1} SEGERA"
                                    else "TAMAT",
                                    highlighted = anime.status == AnimeStatus.ONGOING
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = anime.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = anime.genres.take(2).joinToString(" / "),
                                fontFamily = MonoFont,
                                fontSize = 10.sp,
                                color = TextTertiary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(
                                onClick = { onToggleReminder(anime.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (hasReminder) GoldTintStrong else ObsidianCardAlt)
                                    .border(
                                        1.dp,
                                        if (hasReminder) GoldBright else GoldSubtleBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                                    contentDescription = "Pengingat",
                                    tint = if (hasReminder) GoldBright else TextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            IconButton(
                                onClick = { onPlayAnime(anime.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
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
                text = "PERINGKAT // SKOR TERTINGGI",
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = GoldBright,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Berdasarkan rating komunitas, konsistensi rilis, dan total tayangan",
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
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (selected) GoldGradientBrush
                else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
            )
            .border(
                1.dp,
                if (selected) GoldBright else GoldSubtleBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontFamily = MonoFont,
            color = if (selected) TextOnGold else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
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
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
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
                    text = "FILTER & URUTKAN",
                    fontFamily = MonoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GoldBright
                )
                TextButton(onClick = onReset) {
                    Text("RESET", fontFamily = MonoFont, color = GoldPrimary)
                }
            }

            HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(vertical = 8.dp))

            Text("URUTKAN", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOption.entries.forEach { sort ->
                    GenrePillChip(
                        label = sort.label.uppercase(),
                        selected = filter.sortOption == sort,
                        onClick = { onSortChange(sort) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("STATUS TAYANG", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("SEMUA", filter.statusFilter == null) { onStatusChange(null) }
                AnimeStatus.entries.forEach { status ->
                    GenrePillChip(status.label.uppercase(), filter.statusFilter == status) { onStatusChange(status) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("FORMAT RILIS", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("SEMUA", filter.typeFilter == null) { onTypeChange(null) }
                AnimeType.entries.forEach { type ->
                    GenrePillChip(type.label.uppercase(), filter.typeFilter == type) { onTypeChange(type) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("TAHUN RILIS", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("SEMUA", filter.yearFilter == null) { onYearChange(null) }
                listOf(2026, 2025, 2024).forEach { yr ->
                    GenrePillChip("$yr", filter.yearFilter == yr) { onYearChange(yr) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("GENRE (MULTI-PILIH)", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                genres.forEach { g ->
                    GenrePillChip(g.uppercase(), g in filter.selectedGenres) { onToggleGenre(g) }
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
