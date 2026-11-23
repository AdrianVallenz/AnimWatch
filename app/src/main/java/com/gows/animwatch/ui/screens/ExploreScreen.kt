package com.gows.animwatch.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
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
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Jelajah",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Smooth capsule segmented switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExploreMode.entries.forEach { mode ->
                    val selected = uiState.exploreMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (selected) GoldGradientBrush
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) TextOnGold else TextSecondary
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
    val searchBorderColor by animateColorAsState(
        targetValue = if (searchFocused) GoldPrimary else GoldSubtleBorder,
        animationSpec = tween(220),
        label = "search_border"
    )

    Column(modifier = Modifier.fillMaxSize()) {
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
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(ObsidianCard)
                    .border(
                        width = if (searchFocused) 1.5.dp else 0.75.dp,
                        color = searchBorderColor,
                        shape = CircleShape
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = if (searchFocused) GoldPrimary else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
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
                        cursorBrush = SolidColor(GoldPrimary),
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (filter.activeFilterCount > 0) GoldTintStrong else ObsidianCard)
                        .border(
                            if (filter.activeFilterCount > 0) 1.dp else 0.75.dp,
                            if (filter.activeFilterCount > 0) GoldPrimary else GoldSubtleBorder,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Filter",
                        tint = if (filter.activeFilterCount > 0) GoldPrimary else TextPrimary
                    )
                }
                if (filter.activeFilterCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${filter.activeFilterCount}",
                            color = TextOnGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleGridView,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
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
                    onClick = { if (!allSelected) onResetFilters() }
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
                            text = "Pencarian terakhir",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Hapus semua",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onClearSearchHistory)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
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
                                .clip(CircleShape)
                                .background(ObsidianCard)
                                .border(0.75.dp, GoldSubtleBorder, CircleShape)
                                .clickable { onQueryChange(term) }
                                .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = term,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(5.dp))
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
                text = "${filteredAnime.size} judul • ${filter.sortOption.label}",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )
            if (filter.activeFilterCount > 0 || filter.query.isNotEmpty()) {
                Text(
                    text = "Reset filter",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldPrimary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onResetFilters)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        if (uiState.isSearchingRemote) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "Mencari di Kumanime...",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(5.dp))
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
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(GoldTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Anime tidak ditemukan",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Coba ubah kata kunci pencarian atau reset filter genre yang aktif.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
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
                            trailingBadge = "Sorotan"
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
                        .width(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (selected) GoldGradientBrush
                            else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
                        )
                        .border(
                            if (selected || isToday) 1.dp else 0.75.dp,
                            if (selected || isToday) GoldPrimary else GoldSubtleBorder,
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { onSelectDay(day) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.shortLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) TextOnGold else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isToday) "Hari ini" else "${allAnime.count { it.releaseDay == day }} ep",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) TextOnGold.copy(alpha = 0.88f) else if (isToday) GoldPrimary else TextTertiary,
                        fontSize = 10.sp
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
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GoldTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada jadwal hari ${selectedDay.label}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih hari lain di atas untuk melihat siaran mingguan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
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
                        text = "Jadwal rilis hari ${selectedDay.label}",
                        style = MaterialTheme.typography.titleSmall,
                        color = GoldPrimary
                    )
                }
                items(dayAnime, key = { it.id }) { anime ->
                    val hasReminder = anime.id in reminders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(ObsidianCard)
                            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                            .clickable { onAnimeClick(anime.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(54.dp)
                        ) {
                            Text(
                                text = (anime.releaseTimeWib ?: "20:00").substringBefore(" "),
                                color = GoldPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "WIB",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                fontSize = 10.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(DividerSubtle)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(width = 52.dp, height = 72.dp)
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
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (anime.status == AnimeStatus.ONGOING) {
                                    LedStatusDot(color = StatusLive, pulsing = false, size = 6.dp)
                                }
                                GoldTagChip(
                                    text = if (anime.status == AnimeStatus.ONGOING)
                                        "Ep ${anime.releasedEpisodes + 1} segera"
                                    else "Tamat",
                                    highlighted = anime.status == AnimeStatus.ONGOING
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = anime.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = anime.genres.take(2).joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
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
                                    .border(
                                        0.75.dp,
                                        if (hasReminder) GoldPrimary else GoldSubtleBorder,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                                    contentDescription = "Pengingat",
                                    tint = if (hasReminder) GoldPrimary else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            IconButton(
                                onClick = { onPlayAnime(anime.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldGradientBrush)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Tonton",
                                    tint = TextOnGold,
                                    modifier = Modifier.size(19.dp)
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
                text = "Peringkat Skor Tertinggi",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
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
            .clip(CircleShape)
            .background(
                if (selected) GoldGradientBrush
                else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
            )
            .border(
                if (selected) 0.dp else 0.75.dp,
                if (selected) Color.Transparent else GoldSubtleBorder,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) TextOnGold else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                TextButton(onClick = onReset) {
                    Text(
                        "Reset",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldPrimary
                    )
                }
            }

            HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(vertical = 8.dp))

            Text("Urutkan", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
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

            Text("Status Tayang", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("Semua", filter.statusFilter == null) { onStatusChange(null) }
                AnimeStatus.entries.forEach { status ->
                    GenrePillChip(status.label, filter.statusFilter == status) { onStatusChange(status) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Format Rilis", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("Semua", filter.typeFilter == null) { onTypeChange(null) }
                AnimeType.entries.forEach { type ->
                    GenrePillChip(type.label, filter.typeFilter == type) { onTypeChange(type) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tahun Rilis", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GenrePillChip("Semua", filter.yearFilter == null) { onYearChange(null) }
                listOf(2026, 2025, 2024).forEach { yr ->
                    GenrePillChip("$yr", filter.yearFilter == yr) { onYearChange(yr) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Genre", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
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
