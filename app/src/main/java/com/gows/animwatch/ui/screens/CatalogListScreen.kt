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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.GridView
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.SortOption
import com.gows.animwatch.ui.components.AnimePosterCard
import com.gows.animwatch.ui.components.CompactAnimeRowCard
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.MonoFont
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary

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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary
                )
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
                    text = "${subtitle.uppercase()} // ${sortedAnime.size} JUDUL",
                    fontFamily = MonoFont,
                    fontSize = 10.sp,
                    color = GoldPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                    contentDescription = "Ubah Tampilan",
                    tint = GoldBright
                )
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
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (selected) GoldGradientBrush
                            else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
                        )
                        .border(1.dp, if (selected) GoldBright else GoldSubtleBorder, RoundedCornerShape(6.dp))
                        .clickable { sortOption = opt }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = opt.label.uppercase(),
                        fontFamily = MonoFont,
                        color = if (selected) TextOnGold else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isGridView) {
            // Asymmetric 2-column Bento layout (no 3-equal-columns)
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
                            trailingBadge = "PILIHAN"
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
            }
        }
    }
}
