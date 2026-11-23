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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.GridView
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.SortOption
import com.gows.animwatch.ui.components.AnimePosterCard
import com.gows.animwatch.ui.components.CassetteLinearShimmer
import com.gows.animwatch.ui.components.CompactAnimeRowCard
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

@Composable
fun CatalogListScreen(
    title: String,
    subtitle: String,
    animeList: List<Anime>,
    watchlist: Set<String>,
    bottomInsetPadding: Dp,
    onBack: () -> Unit,
    onAnimeClick: (String) -> Unit,
    onPlayAnime: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    hasMore: Boolean = false,
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var sortOption by remember { mutableStateOf(SortOption.POPULAR) }
    var isGridView by remember { mutableStateOf(true) }

    val sortedAnime = remember(animeList, sortOption) {
        when (sortOption) {
            SortOption.POPULAR -> animeList.sortedBy { it.popularityRank }
            SortOption.TOP_RATING -> animeList.sortedByDescending { it.rating }
            SortOption.LATEST -> animeList.sortedByDescending { it.year }
            SortOption.TITLE_AZ -> animeList.sortedBy { it.title }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = ObsidianCard,
                shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .border(0.75.dp, GoldSubtleBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$subtitle • ${sortedAnime.size} judul",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = CircleShape,
                color = ObsidianCard,
                shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
            ) {
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier
                        .size(40.dp)
                        .border(0.75.dp, GoldSubtleBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                        contentDescription = "Ubah Tampilan",
                        tint = GoldPrimary
                    )
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SortOption.entries) { opt ->
                val selected = sortOption == opt
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
                        .clickable { sortOption = opt }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = opt.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) TextOnGold else TextSecondary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (sortedAnime.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Belum ada anime di kategori ini",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tunggu sinkronisasi katalog selesai atau coba kategori lainnya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = bottomInsetPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(
                    items = sortedAnime,
                    key = { _, anime -> anime.id },
                    span = { index, _ -> GridItemSpan(if (index % 5 == 0) 2 else 1) }
                ) { index, anime ->
                    if (index % 5 == 0) {
                        CompactAnimeRowCard(
                            anime = anime,
                            isBookmarked = anime.id in watchlist,
                            onClick = { onAnimeClick(anime.id) },
                            onPlayClick = { onPlayAnime(anime.id) },
                            onBookmarkClick = { onBookmarkClick(anime.id) },
                            trailingBadge = "Pilihan"
                        )
                    } else {
                        AnimePosterCard(
                            anime = anime,
                            isBookmarked = anime.id in watchlist,
                            onClick = { onAnimeClick(anime.id) },
                            onBookmarkClick = { onBookmarkClick(anime.id) },
                            cardWidth = null
                        )
                    }
                }
                if (hasMore) {
                    item(span = { GridItemSpan(2) }) {
                        CatalogLoadMoreFooter(isLoadingMore = isLoadingMore, onLoadMore = onLoadMore)
                    }
                }
            }
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
                items(sortedAnime, key = { it.id }) { anime ->
                    CompactAnimeRowCard(
                        anime = anime,
                        isBookmarked = anime.id in watchlist,
                        onClick = { onAnimeClick(anime.id) },
                        onPlayClick = { onPlayAnime(anime.id) },
                        onBookmarkClick = { onBookmarkClick(anime.id) }
                    )
                }
                if (hasMore) {
                    item {
                        CatalogLoadMoreFooter(isLoadingMore = isLoadingMore, onLoadMore = onLoadMore)
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogLoadMoreFooter(
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoadingMore) {
            Text(
                text = "Memuat lebih banyak anime...",
                style = MaterialTheme.typography.labelMedium,
                color = GoldPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            CassetteLinearShimmer(modifier = Modifier.fillMaxWidth())
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
                    .clickable(onClick = onLoadMore)
                    .padding(vertical = 13.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Muat Koleksi Berikutnya",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
