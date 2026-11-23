package com.gows.animwatch.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.gows.animwatch.ui.components.SectionHeader
import com.gows.animwatch.ui.components.TopRankedCard
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.MediaScrimDark
import com.gows.animwatch.ui.theme.MediaTextLight
import com.gows.animwatch.ui.theme.MediaTextSecondary
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusAmber
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary
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
    val animeById = remember(uiState.allAnime) { uiState.allAnime.associateBy { it.id } }
    val featuredList = remember(uiState.allAnime) {
        uiState.allAnime
            .sortedWith(
                compareByDescending<Anime> { it.isOfficialFree && it.rating >= 8.3 }
                    .thenByDescending { it.isFeatured }
                    .thenByDescending { it.rating }
            )
            .take(6)
    }
    val latestReleases = remember(uiState.allAnime) {
        uiState.allAnime.filter { it.isNewRelease || it.status == AnimeStatus.ONGOING }
    }
    val top10List = remember(uiState.allAnime) {
        uiState.allAnime.sortedByDescending { it.rating }.take(10)
    }
    val trendingList = remember(uiState.allAnime) {
        uiState.allAnime
            .filter { it.isTrending || it.isOfficialFree }
            .sortedWith(compareByDescending<Anime> { it.isTrending }.thenBy { it.popularityRank })
            .take(18)
    }
    val completedMarathon = remember(uiState.allAnime) {
        uiState.allAnime
            .filter { it.status == AnimeStatus.COMPLETED && it.type == AnimeType.TV_SERIES }
            .sortedWith(compareByDescending<Anime> { it.isOfficialFree }.thenByDescending { it.rating })
    }
    val moviesList = remember(uiState.allAnime) {
        uiState.allAnime.filter { it.type == AnimeType.MOVIE || it.type == AnimeType.OVA }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid),
        contentPadding = PaddingValues(bottom = bottomInsetPadding)
    ) {
        // 1. Anime Streaming Portal Header Bar
        item(key = "home_top_bar") {
            HomeTopBar(
                unreadNotifications = uiState.unreadNotificationCount,
                avatarEmblem = uiState.userProfile.avatarEmblem,
                onSearchClick = onOpenSearch,
                onNotificationClick = onOpenNotifications,
                onProfileClick = onOpenProfile
            )
        }

        // 2. Streaming Portal Quick Search Pill + Optional Sync Notice + Cinematic Hero Banner
        item(key = "home_hero") {
            // Compact search bar like an anime streaming website
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(CircleShape)
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                    .clickable(onClick = onOpenSearch)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Cari Anime",
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Cari Mushoku Tensei, Frieren, SPY×FAMILY...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GoldTint)
                        .padding(horizontal = 8.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "AniList",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }
            }

            val apiStatus = uiState.apiConfig.status
            if (apiStatus == ApiConnectionStatus.SYNCING || apiStatus == ApiConnectionStatus.OFFLINE_FALLBACK) {
                Spacer(modifier = Modifier.height(6.dp))
                CassetteTelemetryStrip(
                    code = if (apiStatus == ApiConnectionStatus.SYNCING) "Memuat katalog AniList..." else "Mode Katalog Offline",
                    statusText = "${uiState.allAnime.size} judul",
                    ledColor = if (apiStatus == ApiConnectionStatus.SYNCING) GoldBright else StatusAmber,
                    pulsing = apiStatus == ApiConnectionStatus.SYNCING,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable(onClick = onOpenProfile)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HeroCarouselSection(
                featuredAnime = featuredList,
                watchlist = uiState.watchlist.keys,
                onAnimeClick = onAnimeClick,
                onPlayClick = { anime -> onPlayAnime(anime.id, null) },
                onBookmarkClick = onBookmarkClick
            )
        }

        // 3. Streaming Category Navigation Strip
        item(key = "home_shortcuts") {
            Spacer(modifier = Modifier.height(16.dp))
            QuickShortcutStrip(
                onScheduleClick = onOpenSchedule,
                onOngoingClick = { onOpenCatalog(CatalogPreset.ONGOING, "Sedang Tayang") },
                onRankingClick = onOpenRanking,
                onMoviesClick = { onOpenCatalog(CatalogPreset.MOVIES, "Anime Movie & OVA") },
                onRandomClick = onOpenRandom
            )
        }

        // 4. Continue Watching
        if (uiState.watchHistory.isNotEmpty()) {
            item(key = "home_continue") {
                Spacer(modifier = Modifier.height(26.dp))
                SectionHeader(
                    title = "Lanjutkan Menonton",
                    subtitle = "Lanjutkan tepat dari menit terakhir",
                    actionLabel = "Riwayat",
                    onActionClick = { onOpenCatalog(CatalogPreset.LATEST, "Baru Ditonton") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.watchHistory, key = { it.animeId }) { progress ->
                        val anime = animeById[progress.animeId]
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
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
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
                title = "Siap Maraton",
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
            AnimWatchCrestLogo(size = 38.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                            append("Anim")
                        }
                        withStyle(SpanStyle(color = GoldPrimary, fontWeight = FontWeight.Bold)) {
                            append("Watch")
                        }
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 20.sp
                )
                Text(
                    text = "Streaming Sub Indo",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ObsidianCard)
                        .border(0.75.dp, GoldSubtleBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = "Notifikasi",
                        tint = if (unreadNotifications > 0) GoldPrimary else TextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
                if (unreadNotifications > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(17.dp)
                            .clip(CircleShape)
                            .background(StatusLive),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$unreadNotifications",
                            color = MediaTextLight,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoldGradientBrush)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarEmblem,
                    color = TextOnGold,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
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
    val heroScrimBrush = remember {
        Brush.verticalGradient(
            0.0f to MediaScrimDark.copy(alpha = 0.14f),
            0.38f to MediaScrimDark.copy(alpha = 0.24f),
            0.70f to MediaScrimDark.copy(alpha = 0.86f),
            1.0f to MediaScrimDark
        )
    }

    LaunchedEffect(featuredAnime.size) {
        if (featuredAnime.size > 1) {
            while (true) {
                delay(6200L)
                if (!pagerState.isScrollInProgress) {
                    val next = (pagerState.currentPage + 1) % featuredAnime.size
                    pagerState.animateScrollToPage(next)
                }
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
                    .height(380.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(24.dp))
                    .clickable { onAnimeClick(anime.id) }
            ) {
                AnimePosterArtwork(
                    artTheme = anime.artTheme,
                    title = anime.title,
                    posterUrl = anime.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                    showVignette = false,
                    decodeSizePx = 720
                )

                // Smooth cinematic vignette (cached brush)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(heroScrimBrush)
                )

                    // Top floating streaming portal badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GoldGradientBrush)
                                .padding(horizontal = 11.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = TextOnGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (anime.isOfficialFree) {
                                    "SUB INDO • ${anime.officialChannel ?: "Muse ID"}"
                                } else {
                                    anime.posterTag
                                },
                                color = TextOnGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MediaScrimDark.copy(alpha = 0.72f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${anime.rating} • Ep ${anime.releasedEpisodes}/${anime.totalEpisodes}",
                                color = MediaTextLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Bottom hero info & smooth actions
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            anime.genres.take(3).forEach { genre ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MediaScrimDark.copy(alpha = 0.62f))
                                        .border(0.75.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 3.5.dp)
                                ) {
                                    Text(
                                        text = genre,
                                        color = MediaTextLight,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
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
                                text = "Tonton Sekarang",
                                icon = Icons.Rounded.PlayArrow,
                                onClick = { onPlayClick(anime) },
                                modifier = Modifier.weight(1.35f),
                                height = 44.dp
                            )
                            GoldOutlinedButton(
                                text = if (isSaved) "Tersimpan" else "Koleksi",
                                icon = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                                isActive = isSaved,
                                onClick = { onBookmarkClick(anime.id) },
                                modifier = Modifier.weight(1f),
                                height = 44.dp
                            )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Smooth fluid capsule pager indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(featuredAnime.size) { index ->
                val selected = pagerState.currentPage == index
                val indicatorWidth by animateDpAsState(
                    targetValue = if (selected) 24.dp else 7.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "pager_dot"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.5.dp)
                        .height(6.dp)
                        .width(indicatorWidth)
                        .clip(CircleShape)
                        .background(if (selected) GoldPrimary else GoldPrimary.copy(alpha = 0.25f))
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
        QuickActionPill(Icons.Rounded.CalendarMonth, "Jadwal", onScheduleClick)
        QuickActionPill(Icons.Rounded.LiveTv, "Ongoing", onOngoingClick)
        QuickActionPill(Icons.Rounded.EmojiEvents, "Top 10", onRankingClick)
        QuickActionPill(Icons.Rounded.Movie, "Movie", onMoviesClick)
        QuickActionPill(Icons.Rounded.Shuffle, "Acak", onRandomClick)
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
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(GoldTint)
                .border(0.75.dp, GoldSubtleBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GoldPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            maxLines = 1
        )
    }
}
