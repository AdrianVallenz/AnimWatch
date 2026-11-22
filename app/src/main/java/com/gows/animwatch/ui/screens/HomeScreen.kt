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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
import com.gows.animwatch.data.model.ApiConnectionStatus
import com.gows.animwatch.data.model.CatalogPreset
import com.gows.animwatch.ui.components.AnimWatchCrestLogo
import com.gows.animwatch.ui.components.AnimePosterArtwork
import com.gows.animwatch.ui.components.AnimePosterCard
import com.gows.animwatch.ui.components.CassetteTelemetryStrip
import com.gows.animwatch.ui.components.ContinueWatchingCard
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.SectionHeader
import com.gows.animwatch.ui.components.TopRankedCard
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.MediaScrimDark
import com.gows.animwatch.ui.theme.MediaTextLight
import com.gows.animwatch.ui.theme.MediaTextSecondary
import com.gows.animwatch.ui.theme.MonoFont
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusAmber
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    uiState: AnimWatchUiState,
    bottomInsetPadding: Dp,
    onAnimeClick: (String) -> Unit,
    onPlayAnime: (String, String?) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onOpenCatalog: (CatalogPreset, String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenRanking: () -> Unit,
    onOpenRandom: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredList = uiState.allAnime.filter { it.isFeatured }.ifEmpty { uiState.allAnime.take(4) }
    val latestReleases = uiState.allAnime.filter { it.isNewRelease || it.status == AnimeStatus.ONGOING }
    val top10List = uiState.allAnime.sortedByDescending { it.rating }.take(10)
    val trendingList = uiState.allAnime.filter { it.isTrending }.sortedBy { it.popularityRank }
    val completedMarathon = uiState.allAnime.filter { it.status == AnimeStatus.COMPLETED && it.type == AnimeType.TV_SERIES }
    val moviesList = uiState.allAnime.filter { it.type == AnimeType.MOVIE || it.type == AnimeType.OVA }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid),
        contentPadding = PaddingValues(bottom = bottomInsetPadding)
    ) {
        // 1. Top Hardware Header Bar
        item {
            HomeTopBar(
                unreadNotifications = uiState.unreadNotificationCount,
                avatarEmblem = uiState.userProfile.avatarEmblem,
                onSearchClick = onOpenSearch,
                onNotificationClick = onOpenNotifications,
                onProfileClick = onOpenProfile
            )
        }

        // 2. Telemetry Strip + Hero Featured Carousel
        item {
            val apiStatus = uiState.apiConfig.status
            val ledColor = when (apiStatus) {
                ApiConnectionStatus.CONNECTED -> StatusEmerald
                ApiConnectionStatus.SYNCING -> GoldBright
                ApiConnectionStatus.OFFLINE_FALLBACK -> StatusAmber
                ApiConnectionStatus.DEMO_LOCAL -> GoldPrimary
            }
            CassetteTelemetryStrip(
                code = "CH-01 // ${apiStatus.code}",
                statusText = if (apiStatus == ApiConnectionStatus.CONNECTED) {
                    "${uiState.apiConfig.remoteCount} LIVE // KUMANIME API"
                } else {
                    "${uiState.allAnime.size} ANIME // SUB INDO"
                },
                ledColor = ledColor,
                pulsing = true,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable(onClick = onOpenProfile)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HeroCarouselSection(
                featuredAnime = featuredList,
                watchlist = uiState.watchlist.keys,
                onAnimeClick = onAnimeClick,
                onPlayClick = { anime -> onPlayAnime(anime.id, null) },
                onBookmarkClick = onBookmarkClick
            )
        }

        // 3. Quick Discovery Tactile Keys
        item {
            Spacer(modifier = Modifier.height(16.dp))
            QuickShortcutStrip(
                onScheduleClick = onOpenSchedule,
                onOngoingClick = { onOpenCatalog(CatalogPreset.ONGOING, "Sedang Tayang") },
                onRankingClick = onOpenRanking,
                onMoviesClick = { onOpenCatalog(CatalogPreset.MOVIES, "Anime Movie & OVA") },
                onRandomClick = onOpenRandom
            )
        }

        // 4. Continue Watching Deck
        if (uiState.watchHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    channelCode = "CH-02 // TAPE DECK",
                    title = "Lanjutkan Menonton",
                    subtitle = "Lanjutkan tepat dari menit terakhir",
                    actionLabel = "RIWAYAT",
                    onActionClick = { onOpenCatalog(CatalogPreset.LATEST, "Baru Ditonton") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.watchHistory, key = { it.animeId }) { progress ->
                        val anime = uiState.allAnime.find { it.id == progress.animeId }
                        if (anime != null) {
                            ContinueWatchingCard(
                                anime = anime,
                                progress = progress,
                                onPlayClick = { onPlayAnime(anime.id, progress.episodeId) },
                                onDetailClick = { onAnimeClick(anime.id) },
                                onRemoveClick = { onRemoveHistory(anime.id) }
                            )
                        }
                    }
                }
            }
        }

        // 5. Rilis Terbaru Hari Ini
        item {
            Spacer(modifier = Modifier.height(26.dp))
            SectionHeader(
                channelCode = "CH-03 // SIARAN BARU",
                title = "Rilis Episode Terbaru",
                subtitle = "Update Sub Indo terbaru minggu ini",
                onActionClick = { onOpenCatalog(CatalogPreset.LATEST, "Rilis Episode Terbaru") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(latestReleases, key = { "latest_${it.id}" }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isBookmarked = anime.id in uiState.watchlist,
                        onClick = { onAnimeClick(anime.id) },
                        onBookmarkClick = { onBookmarkClick(anime.id) }
                    )
                }
            }
        }

        // 6. Top 10 Populer Minggu Ini
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                channelCode = "CH-04 // PERINGKAT",
                title = "Top 10 Peringkat",
                subtitle = "Koleksi terbaik dengan skor tertinggi",
                onActionClick = onOpenRanking
            )
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(top10List, key = { _, anime -> "top_${anime.id}" }) { index, anime ->
                    TopRankedCard(
                        rank = index + 1,
                        anime = anime,
                        onClick = { onAnimeClick(anime.id) }
                    )
                }
            }
        }

        // 7. Sedang Hangat / Trending
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                channelCode = "CH-05 // POPULER",
                title = "Sedang Hangat",
                subtitle = "Paling sering diputar di AnimWatch",
                onActionClick = { onOpenCatalog(CatalogPreset.TRENDING, "Sedang Hangat") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trendingList, key = { "trend_${it.id}" }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isBookmarked = anime.id in uiState.watchlist,
                        onClick = { onAnimeClick(anime.id) },
                        onBookmarkClick = { onBookmarkClick(anime.id) }
                    )
                }
            }
        }

        // 8. Maraton Anime Tamat
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                channelCode = "CH-06 // ARSIP LENGKAP",
                title = "Siap Maraton (Tamat)",
                subtitle = "Semua episode lengkap dari awal hingga akhir",
                onActionClick = { onOpenCatalog(CatalogPreset.COMPLETED, "Anime Tamat — Siap Maraton") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(completedMarathon, key = { "comp_${it.id}" }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isBookmarked = anime.id in uiState.watchlist,
                        onClick = { onAnimeClick(anime.id) },
                        onBookmarkClick = { onBookmarkClick(anime.id) }
                    )
                }
            }
        }

        // 9. Anime Movie & OVA
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                channelCode = "CH-07 // LAYAR LEBAR",
                title = "Film & Spesial Pilihan",
                subtitle = "Tontonan layar lebar kualitas sinematik",
                onActionClick = { onOpenCatalog(CatalogPreset.MOVIES, "Film & OVA Pilihan") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(moviesList, key = { "mov_${it.id}" }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isBookmarked = anime.id in uiState.watchlist,
                        onClick = { onAnimeClick(anime.id) },
                        onBookmarkClick = { onBookmarkClick(anime.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    unreadNotifications: Int,
    avatarEmblem: String,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimWatchCrestLogo(size = 36.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.ExtraBold)) {
                            append("ANIM")
                        }
                        withStyle(SpanStyle(color = GoldBright, fontWeight = FontWeight.ExtraBold)) {
                            append("WATCH")
                        }
                    },
                    fontFamily = MonoFont,
                    fontSize = 18.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "CASSETTE DECK // STREAM",
                    fontFamily = MonoFont,
                    color = GoldPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Cari Anime",
                    tint = TextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }

            Box {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianCard)
                        .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = "Notifikasi",
                        tint = if (unreadNotifications > 0) GoldBright else TextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
                if (unreadNotifications > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(StatusLive),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$unreadNotifications",
                            fontFamily = MonoFont,
                            color = TextPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldGradientBrush)
                    .border(1.dp, GoldBright, RoundedCornerShape(8.dp))
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarEmblem,
                    fontFamily = MonoFont,
                    color = TextOnGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun HeroCarouselSection(
    featuredAnime: List<Anime>,
    watchlist: Set<String>,
    onAnimeClick: (String) -> Unit,
    onPlayClick: (Anime) -> Unit,
    onBookmarkClick: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { featuredAnime.size })

    LaunchedEffect(featuredAnime.size) {
        if (featuredAnime.size > 1) {
            while (true) {
                delay(5800L)
                val next = (pagerState.currentPage + 1) % featuredAnime.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val anime = featuredAnime[page]
            val isSaved = anime.id in watchlist

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(352.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, GoldBorder, RoundedCornerShape(8.dp))
                    .clickable { onAnimeClick(anime.id) }
            ) {
                AnimePosterArtwork(
                    artTheme = anime.artTheme,
                    title = anime.title,
                    posterUrl = anime.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                    showVignette = false
                )

                // Deep editorial vignette (always dark over hero artwork for crisp legibility in Dark & Light modes)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.38f to MediaScrimDark.copy(alpha = 0.25f),
                                0.68f to MediaScrimDark.copy(alpha = 0.88f),
                                1.0f to MediaScrimDark
                            )
                        )
                )

                // Top bezel badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldGradientBrush)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = TextOnGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = anime.posterTag,
                            fontFamily = MonoFont,
                            color = TextOnGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MediaScrimDark.copy(alpha = 0.88f))
                            .border(1.dp, GoldBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${anime.rating} // EP ${anime.releasedEpisodes}/${anime.totalEpisodes}",
                            fontFamily = MonoFont,
                            color = MediaTextLight,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom hero info & tactile buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        anime.genres.take(3).forEach { genre ->
                            GoldTagChip(text = genre)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = anime.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MediaTextLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = anime.synopsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MediaTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoldGradientButton(
                            text = "Tonton",
                            icon = Icons.Rounded.PlayArrow,
                            onClick = { onPlayClick(anime) },
                            modifier = Modifier.weight(1.25f)
                        )
                        GoldOutlinedButton(
                            text = if (isSaved) "Tersimpan" else "Koleksi",
                            icon = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                            isActive = isSaved,
                            onClick = { onBookmarkClick(anime.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LED Segment Pager Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(featuredAnime.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(5.dp)
                        .width(if (selected) 26.dp else 10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (selected) GoldBright else GoldPrimary.copy(alpha = 0.28f))
                )
            }
        }
    }
}

@Composable
private fun QuickShortcutStrip(
    onScheduleClick: () -> Unit,
    onOngoingClick: () -> Unit,
    onRankingClick: () -> Unit,
    onMoviesClick: () -> Unit,
    onRandomClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionPill(Icons.Rounded.CalendarMonth, "JADWAL", onScheduleClick)
        QuickActionPill(Icons.Rounded.LiveTv, "ONGOING", onOngoingClick)
        QuickActionPill(Icons.Rounded.EmojiEvents, "TOP 10", onRankingClick)
        QuickActionPill(Icons.Rounded.Movie, "MOVIE", onMoviesClick)
        QuickActionPill(Icons.Rounded.Shuffle, "ACAK", onRandomClick)
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GoldTint)
                .border(1.dp, GoldBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GoldBright,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontFamily = MonoFont,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            maxLines = 1
        )
    }
}
