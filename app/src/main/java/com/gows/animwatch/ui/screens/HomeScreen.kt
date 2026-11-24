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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.gows.animwatch.ui.components.CassetteLinearShimmer
import com.gows.animwatch.ui.components.CassetteTelemetryStrip
import com.gows.animwatch.ui.components.CompactAnimeRowCard
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
    onOpenGenre: (String) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onLoadMoreCatalog: () -> Unit = {},
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHomeGenre by remember { mutableStateOf<String?>(null) }

    val animeById = remember(uiState.allAnime) { uiState.allAnime.associateBy { it.id } }
    val visibleHistory = remember(uiState.watchHistory, animeById) {
        uiState.watchHistory.filter { it.animeId in animeById }.distinctBy { it.animeId }
    }
    val subIndoAnime = remember(uiState.allAnime) { uiState.allAnime.filter { !it.isDubIndo && !it.isSubEng } }
    val subEngList = remember(uiState.allAnime) {
        uiState.allAnime
            .filter { it.isSubEng }
            .sortedWith(compareByDescending<Anime> { it.rating }.thenBy { it.popularityRank })
    }
    val dubIndoList = remember(uiState.allAnime) {
        uiState.allAnime
            .filter { it.isDubIndo }
            .sortedWith(compareByDescending<Anime> { it.rating }.thenBy { it.popularityRank })
    }
    val playableList = remember(subIndoAnime) {
        subIndoAnime
            .filter { it.isOfficialFree }
            .sortedWith(compareByDescending<Anime> { it.rating }.thenBy { it.popularityRank })
    }
    val featuredList = remember(subIndoAnime) {
        subIndoAnime
            .sortedWith(
                compareByDescending<Anime> { it.isOfficialFree && it.rating >= 8.3 }
                    .thenByDescending { it.isFeatured }
                    .thenByDescending { it.rating }
            )
            .take(7)
    }
    val todayReleaseDay = remember { AnimWatchUiState.currentReleaseDay() }
    val latestReleases = remember(subIndoAnime, todayReleaseDay) {
        subIndoAnime
            .filter { it.isNewRelease || it.status == AnimeStatus.ONGOING || it.lastAiredAtEpochSec > 0L }
            .sortedWith(
                compareByDescending<Anime> { it.lastAiredAtEpochSec }
                    .thenByDescending { it.releaseDay == todayReleaseDay }
                    .thenByDescending { it.isOfficialFree }
                    .thenBy { it.popularityRank }
            )
            .take(24)
    }
    val top10List = remember(subIndoAnime) {
        subIndoAnime.sortedByDescending { it.rating }.take(10)
    }
    val trendingList = remember(subIndoAnime) {
        subIndoAnime
            .filter { it.isTrending || it.isOfficialFree }
            .sortedWith(compareByDescending<Anime> { it.isTrending }.thenBy { it.popularityRank })
            .take(24)
    }
    val actionList = remember(subIndoAnime) {
        subIndoAnime
            .filter { a -> a.genres.any { it.equals("Action", ignoreCase = true) } }
            .sortedWith(compareByDescending<Anime> { it.isOfficialFree }.thenByDescending { it.rating })
            .take(20)
    }
    val fantasyList = remember(subIndoAnime) {
        subIndoAnime
            .filter { a -> a.genres.any { it.equals("Fantasy", ignoreCase = true) } }
            .sortedWith(compareByDescending<Anime> { it.isOfficialFree }.thenByDescending { it.rating })
            .take(20)
    }
    val completedMarathon = remember(subIndoAnime) {
        subIndoAnime
            .filter { it.status == AnimeStatus.COMPLETED && it.type == AnimeType.TV_SERIES }
            .sortedWith(compareByDescending<Anime> { it.isOfficialFree }.thenByDescending { it.rating })
            .take(24)
    }
    val moviesList = remember(subIndoAnime) {
        subIndoAnime
            .filter { it.type == AnimeType.MOVIE || it.type == AnimeType.OVA }
            .sortedByDescending { it.rating }
            .take(24)
    }
    val quickGenres = remember(uiState.genres) {
        val preferredOrder = listOf(
            "Action", "Fantasy", "Adventure", "Comedy", "Romance",
            "Sci-Fi", "Drama", "Sports", "Supernatural", "Slice of Life"
        )
        preferredOrder.filter { p -> uiState.genres.any { it.equals(p, ignoreCase = true) } }
            .ifEmpty {
                uiState.genres.filterNot {
                    it.equals("Dub Indo", ignoreCase = true) || it.equals("Sub Eng", ignoreCase = true)
                }.take(8)
            }
    }
    val genreFilteredAnime = remember(subIndoAnime, selectedHomeGenre) {
        val g = selectedHomeGenre ?: return@remember emptyList()
        subIndoAnime
            .filter { a -> a.genres.any { it.equals(g, ignoreCase = true) } }
            .sortedWith(compareByDescending<Anime> { it.isOfficialFree }.thenByDescending { it.rating })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid),
        contentPadding = PaddingValues(bottom = bottomInsetPadding)
    ) {
        // 1. Anime Streaming Portal Header Bar (Hanya logo & lonceng notifikasi — tanpa tombol navigasi ganda)
        item(key = "home_top_bar") {
            HomeTopBar(
                totalAnimeCount = uiState.allAnime.size,
                unreadNotifications = uiState.unreadNotificationCount,
                onNotificationClick = onOpenNotifications
            )
        }

        // 2. Quick Search Pill + Optional Sync Notice + Cinematic Hero Banner
        item(key = "home_hero") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(CircleShape)
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                    .clickable(onClick = onOpenSearch)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
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
                    text = "Cari Mushoku Tensei, Frieren, Jujutsu Kaisen...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            val apiStatus = uiState.apiConfig.status
            if (apiStatus == ApiConnectionStatus.SYNCING || apiStatus == ApiConnectionStatus.OFFLINE_FALLBACK) {
                Spacer(modifier = Modifier.height(6.dp))
                CassetteTelemetryStrip(
                    code = if (apiStatus == ApiConnectionStatus.SYNCING) "Memuat katalog..." else "Mode Katalog Offline",
                    statusText = "${uiState.allAnime.size} judul",
                    ledColor = if (apiStatus == ApiConnectionStatus.SYNCING) GoldBright else StatusAmber,
                    pulsing = apiStatus == ApiConnectionStatus.SYNCING,
                    modifier = Modifier.padding(horizontal = 16.dp)
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

        // 3. Streaming Category Navigation Strip + Quick Genre Filter Pills
        item(key = "home_shortcuts") {
            Spacer(modifier = Modifier.height(16.dp))
            QuickShortcutStrip(
                onDubIndoClick = { onOpenCatalog(CatalogPreset.DUB_INDO, "Kategori Dub Indo") },
                onScheduleClick = onOpenSchedule,
                onOngoingClick = { onOpenCatalog(CatalogPreset.ONGOING, "Sedang Tayang") },
                onRankingClick = onOpenRanking,
                onMoviesClick = { onOpenCatalog(CatalogPreset.MOVIES, "Anime Movie & OVA") }
            )
            if (quickGenres.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        HomeGenreChip(
                            label = "Semua",
                            selected = selectedHomeGenre == null,
                            onClick = { selectedHomeGenre = null }
                        )
                    }
                    items(quickGenres) { genre ->
                        HomeGenreChip(
                            label = genre,
                            selected = selectedHomeGenre == genre,
                            onClick = {
                                selectedHomeGenre = if (selectedHomeGenre == genre) null else genre
                            }
                        )
                    }
                }
            }
        }

        // If a Home genre pill is active, display an instant filtered genre view right on Home
        if (selectedHomeGenre != null) {
            val activeGenre = selectedHomeGenre!!
            item(key = "home_genre_header") {
                Spacer(modifier = Modifier.height(22.dp))
                SectionHeader(
                    title = "Genre $activeGenre",
                    subtitle = "${genreFilteredAnime.size} judul ditemukan",
                    actionLabel = "Filter Lengkap",
                    onActionClick = { onOpenGenre(activeGenre) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            if (genreFilteredAnime.isEmpty()) {
                item(key = "home_genre_empty") {
                    Text(
                        text = "Belum ada anime di kategori \"$activeGenre\" saat ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(genreFilteredAnime.take(30), key = { "hg_${it.id}" }) { anime ->
                    CompactAnimeRowCard(
                        anime = anime,
                        isBookmarked = anime.id in uiState.watchlist,
                        onClick = { onAnimeClick(anime.id) },
                        onPlayClick = { onPlayAnime(anime.id, null) },
                        onBookmarkClick = { onBookmarkClick(anime.id) },
                        trailingBadge = if (anime.isOfficialFree) "Siap Putar" else null,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        } else {
            // 4. Continue Watching
            if (visibleHistory.isNotEmpty()) {
                item(key = "home_continue") {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = "Lanjutkan Menonton",
                        subtitle = "Lanjutkan tepat dari menit terakhir",
                        actionLabel = "Riwayat",
                        onActionClick = onOpenHistory
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleHistory, key = { it.animeId }) { progress ->
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

            // 5. Koleksi Siap Tonton Sub Indo
            if (playableList.isNotEmpty()) {
                item(key = "home_playable") {
                    Spacer(modifier = Modifier.height(26.dp))
                    SectionHeader(
                        title = "Siap Tonton • Sub Indo",
                        subtitle = "${playableList.size} serial Sub Indo langsung putar",
                        onActionClick = { onOpenCatalog(CatalogPreset.OFFICIAL_FREE, "Siap Tonton • Sub Indo") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(playableList.take(24), key = { "play_${it.id}" }) { anime ->
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

            // 5b. Kategori Tersendiri: Sub Eng (Subtitle Bahasa Inggris Resmi)
            if (subEngList.isNotEmpty()) {
                item(key = "home_sub_eng") {
                    Spacer(modifier = Modifier.height(26.dp))
                    SectionHeader(
                        title = "Koleksi Sub Eng",
                        subtitle = "${subEngList.size} playlist subtitle Bahasa Inggris (Muse Asia)",
                        onActionClick = { onOpenCatalog(CatalogPreset.SUB_ENG, "Koleksi Sub Eng") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(subEngList.take(24), key = { "suben_${it.id}" }) { anime ->
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

            // 5c. Kategori Tersendiri: Dub Indo (Sulih Suara Bahasa Indonesia)
            if (dubIndoList.isNotEmpty()) {
                item(key = "home_dub_indo") {
                    Spacer(modifier = Modifier.height(26.dp))
                    SectionHeader(
                        title = "Kategori Dub Indo",
                        subtitle = "${dubIndoList.size} serial sulih suara Bahasa Indonesia",
                        onActionClick = { onOpenCatalog(CatalogPreset.DUB_INDO, "Kategori Dub Indo") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dubIndoList.take(24), key = { "dub_${it.id}" }) { anime ->
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

            // 6. Rilis Episode Terbaru
            item(key = "home_latest") {
                Spacer(modifier = Modifier.height(26.dp))
                SectionHeader(
                    title = "Rilis Episode Terbaru",
                    subtitle = "Update episode terbaru minggu ini",
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

            // 7. Top 10 Peringkat
            item(key = "home_top10") {
                Spacer(modifier = Modifier.height(26.dp))
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

            // 8. Sedang Hangat / Trending
            item(key = "home_trending") {
                Spacer(modifier = Modifier.height(26.dp))
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

            // 9. Aksi & Pertarungan
            if (actionList.isNotEmpty()) {
                item(key = "home_action") {
                    Spacer(modifier = Modifier.height(26.dp))
                    SectionHeader(
                        title = "Aksi & Pertarungan",
                        subtitle = "Pertempuran epik dan adrenalin tinggi",
                        onActionClick = { onOpenGenre("Action") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(actionList, key = { "act_${it.id}" }) { anime ->
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

            // 10. Fantasi & Dunia Lain
            if (fantasyList.isNotEmpty()) {
                item(key = "home_fantasy") {
                    Spacer(modifier = Modifier.height(26.dp))
                    SectionHeader(
                        title = "Fantasi & Dunia Lain",
                        subtitle = "Petualangan sihir, isekai, dan dunia baru",
                        onActionClick = { onOpenGenre("Fantasy") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(fantasyList, key = { "fan_${it.id}" }) { anime ->
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

            // 11. Maraton Anime Tamat
            item(key = "home_completed") {
                Spacer(modifier = Modifier.height(26.dp))
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

            // 12. Anime Movie & OVA
            item(key = "home_movies") {
                Spacer(modifier = Modifier.height(26.dp))
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

        // 13. Auto-loaded Catalog Summary Footer
        item(key = "home_load_more") {
            if (uiState.hasMoreCatalog && !uiState.catalogLoadError && !uiState.isLoadingMoreCatalog) {
                LaunchedEffect(uiState.catalogPageLoaded) {
                    onLoadMoreCatalog()
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${subIndoAnime.size} Sub Indo • ${subEngList.size} Sub Eng • ${dubIndoList.size} Dub Indo",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        uiState.catalogLoadError -> "Sebagian katalog gagal dimuat. Periksa koneksi lalu coba lagi."
                        uiState.isLoadingMoreCatalog -> "Sedang memuat sisa koleksi anime secara otomatis..."
                        else -> "Seluruh katalog & playlist anime otomatis diperbarui."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
                if (uiState.isLoadingMoreCatalog) {
                    Spacer(modifier = Modifier.height(10.dp))
                    CassetteLinearShimmer(modifier = Modifier.fillMaxWidth())
                }
                if (uiState.catalogLoadError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    GoldOutlinedButton(
                        text = "Muat Ulang Katalog",
                        onClick = onLoadMoreCatalog,
                        height = 38.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeGenreChip(
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
                width = if (selected) 0.dp else 0.75.dp,
                color = if (selected) Color.Transparent else GoldSubtleBorder,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) TextOnGold else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun HomeTopBar(
    totalAnimeCount: Int,
    unreadNotifications: Int,
    onNotificationClick: () -> Unit
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
                    text = "$totalAnimeCount+ Anime Streaming",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
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
    if (featuredAnime.isEmpty()) return
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
                if (!pagerState.isScrollInProgress && featuredAnime.size > 1) {
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
            val anime = featuredAnime.getOrNull(page) ?: return@HorizontalPager
            val isSaved = anime.id in watchlist

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(386.dp)
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
                            imageVector = if (anime.isOfficialFree) Icons.Rounded.PlayArrow else Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = TextOnGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = anime.officialChannel ?: anime.posterTag,
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
                            tint = com.gows.animwatch.ui.theme.MediaAccentBright,
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

                // Bottom hero info & 3-action streaming bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoldGradientButton(
                            text = "Tonton Sekarang",
                            icon = Icons.Rounded.PlayArrow,
                            onClick = { onPlayClick(anime) },
                            modifier = Modifier.weight(1.4f),
                            height = 44.dp
                        )
                        GoldOutlinedButton(
                            text = "Detail",
                            icon = Icons.Rounded.Info,
                            onClick = { onAnimeClick(anime.id) },
                            modifier = Modifier.weight(0.85f),
                            height = 44.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSaved) GoldTint else MediaScrimDark.copy(alpha = 0.65f))
                                .border(
                                    1.dp,
                                    if (isSaved) GoldPrimary else Color.White.copy(alpha = 0.24f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onBookmarkClick(anime.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                                contentDescription = "Simpan ke Koleksi",
                                tint = if (isSaved) com.gows.animwatch.ui.theme.MediaAccentBright else MediaTextLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
    onDubIndoClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onOngoingClick: () -> Unit,
    onRankingClick: () -> Unit,
    onMoviesClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionPill(Icons.Rounded.RecordVoiceOver, "Dub Indo", onDubIndoClick)
        QuickActionPill(Icons.Rounded.CalendarMonth, "Jadwal", onScheduleClick)
        QuickActionPill(Icons.Rounded.LiveTv, "Ongoing", onOngoingClick)
        QuickActionPill(Icons.Rounded.EmojiEvents, "Top 10", onRankingClick)
        QuickActionPill(Icons.Rounded.Movie, "Movie", onMoviesClick)
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
