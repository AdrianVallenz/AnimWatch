package com.gows.animwatch.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gows.animwatch.data.local.SharedPreferencesSessionStore
import com.gows.animwatch.ui.components.FloatingGoldBottomNav
import com.gows.animwatch.ui.components.MiniPlayerBar
import com.gows.animwatch.ui.components.NotificationsBottomSheet
import com.gows.animwatch.ui.screens.AnimeDetailScreen
import com.gows.animwatch.ui.screens.AuthScreen
import com.gows.animwatch.ui.screens.CatalogListScreen
import com.gows.animwatch.ui.screens.ExploreScreen
import com.gows.animwatch.ui.screens.HomeScreen
import com.gows.animwatch.ui.screens.LibraryScreen
import com.gows.animwatch.ui.screens.PlayerScreen
import com.gows.animwatch.ui.screens.ProfileScreen
import com.gows.animwatch.ui.screens.ThemeOnboardingScreen
import com.gows.animwatch.ui.state.AnimWatchViewModel
import com.gows.animwatch.ui.state.AppRoute
import com.gows.animwatch.ui.state.ExploreMode
import com.gows.animwatch.ui.state.MainTab
import com.gows.animwatch.ui.theme.AnimWatchTheme
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.TextPrimary

@Composable
fun AnimWatchApp(
    viewModel: AnimWatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    val appContext = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        viewModel.attachSessionStore(SharedPreferencesSessionStore(appContext))
    }

    AnimWatchTheme(
        appearanceMode = uiState.appearanceMode,
        accentTheme = uiState.accentTheme
    ) {
        LaunchedEffect(uiState.isAuthenticated, uiState.hasCompletedThemeSetup) {
            if (uiState.isAuthenticated && uiState.hasCompletedThemeSetup) {
                viewModel.syncKumanimeCatalog(silentIfOffline = true)
            }
        }

        LaunchedEffect(Unit) {
            viewModel.toastMessages.collect { msg ->
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
            }
        }

        val shouldHandleBack = uiState.isEditingTheme || (
            uiState.isAuthenticated && uiState.hasCompletedThemeSetup && (
                (uiState.playerSession?.isExpanded == true) ||
                    uiState.backStack.size > 1 ||
                    uiState.currentTab != MainTab.HOME
                )
            )

        BackHandler(enabled = shouldHandleBack) {
            viewModel.navigateBack()
        }

        val isTabsRoot = uiState.currentRoute is AppRoute.TabsRoot
        val hasMiniPlayer = uiState.playerSession != null && uiState.playerSession?.isExpanded == false

        val bottomContentPadding = when {
            isTabsRoot && hasMiniPlayer -> 178.dp
            isTabsRoot -> 106.dp
            hasMiniPlayer -> 96.dp
            else -> 28.dp
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianVoid)
        ) {
            if (!uiState.isAuthenticated) {
                AuthScreen(
                    authError = uiState.authError,
                    onClearAuthError = viewModel::clearAuthError,
                    onLogin = viewModel::login,
                    onRegister = viewModel::register,
                    onContinueAsGuest = viewModel::continueAsGuest,
                    onDemoVipLogin = viewModel::loginDemoVip,
                    onResetPasswordRequested = viewModel::requestPasswordReset
                )
            } else if (!uiState.hasCompletedThemeSetup || uiState.isEditingTheme) {
                ThemeOnboardingScreen(
                    userName = uiState.userProfile.displayName,
                    appearanceMode = uiState.appearanceMode,
                    accentTheme = uiState.accentTheme,
                    preferredGenres = uiState.preferredGenres,
                    availableGenres = uiState.genres,
                    settings = uiState.settings,
                    isGridCatalogPreferred = uiState.exploreFilter.isGridView,
                    isFromProfile = uiState.isEditingTheme,
                    onSelectAppearance = viewModel::setAppearanceMode,
                    onSelectAccent = viewModel::setAccentTheme,
                    onTogglePreferredGenre = viewModel::togglePreferredGenre,
                    onUpdateSettings = viewModel::updateSettings,
                    onSetGridCatalogPreferred = viewModel::setGridCatalogPreferred,
                    onComplete = viewModel::completeThemeOnboarding,
                    onBack = if (uiState.isEditingTheme) viewModel::closeThemeEditor else null
                )
            } else {
        // 1. Main Route Content
        AnimatedContent(
            targetState = uiState.currentRoute,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 28 }
                )) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "app_route"
        ) { route ->
            when (route) {
                is AppRoute.TabsRoot -> {
                    when (uiState.currentTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                bottomInsetPadding = bottomContentPadding,
                                onAnimeClick = viewModel::openAnimeDetail,
                                onPlayAnime = { id, epId -> viewModel.playAnime(id, epId) },
                                onBookmarkClick = viewModel::toggleQuickBookmark,
                                onRemoveHistory = viewModel::removeHistoryItem,
                                onOpenCatalog = { preset, title ->
                                    viewModel.openCatalog(preset, title)
                                },
                                onOpenSearch = {
                                    viewModel.selectTab(MainTab.EXPLORE)
                                    viewModel.setExploreMode(ExploreMode.CATALOG)
                                },
                                onOpenSchedule = viewModel::openExploreSchedule,
                                onOpenRanking = viewModel::openExploreRanking,
                                onOpenRandom = viewModel::openRandomAnime,
                                onOpenNotifications = { showNotificationsSheet = true },
                                onOpenProfile = { viewModel.selectTab(MainTab.PROFILE) }
                            )
                        }

                        MainTab.EXPLORE -> {
                            val filtered = remember(uiState.allAnime, uiState.exploreFilter) {
                                viewModel.getFilteredExploreAnime()
                            }
                            ExploreScreen(
                                uiState = uiState,
                                filteredAnime = filtered,
                                bottomInsetPadding = bottomContentPadding,
                                onSelectMode = viewModel::setExploreMode,
                                onQueryChange = viewModel::updateSearchQuery,
                                onCommitSearch = viewModel::commitSearchQuery,
                                onRemoveSearchHistory = viewModel::removeSearchHistoryItem,
                                onClearSearchHistory = viewModel::clearSearchHistory,
                                onToggleGenre = viewModel::toggleGenreFilter,
                                onStatusChange = viewModel::setStatusFilter,
                                onTypeChange = viewModel::setTypeFilter,
                                onYearChange = viewModel::setYearFilter,
                                onSortChange = viewModel::setSortOption,
                                onToggleGridView = viewModel::toggleExploreGridMode,
                                onResetFilters = viewModel::resetExploreFilters,
                                onSelectScheduleDay = viewModel::selectScheduleDay,
                                onToggleReminder = viewModel::toggleAnimeReminder,
                                onAnimeClick = viewModel::openAnimeDetail,
                                onPlayAnime = { id -> viewModel.playAnime(id) },
                                onBookmarkClick = viewModel::toggleQuickBookmark
                            )
                        }

                        MainTab.LIBRARY -> {
                            LibraryScreen(
                                uiState = uiState,
                                bottomInsetPadding = bottomContentPadding,
                                onSelectLibraryTab = viewModel::setLibraryTab,
                                onSelectWatchlistFilter = viewModel::setWatchlistCategoryFilter,
                                onUpdateWatchlistCategory = viewModel::setWatchlistCategory,
                                onRemoveHistory = viewModel::removeHistoryItem,
                                onClearAllHistory = viewModel::clearAllHistory,
                                onPauseDownload = viewModel::pauseDownload,
                                onResumeDownload = viewModel::resumeDownload,
                                onRemoveDownload = viewModel::removeDownload,
                                onAnimeClick = viewModel::openAnimeDetail,
                                onPlayAnime = { id, epId -> viewModel.playAnime(id, epId) }
                            )
                        }

                        MainTab.PROFILE -> {
                            ProfileScreen(
                                uiState = uiState,
                                bottomInsetPadding = bottomContentPadding,
                                onUpdateProfile = viewModel::updateProfile,
                                onUpdateSettings = viewModel::updateSettings,
                                onUpdateSubtitleSize = viewModel::updateSubtitleSize,
                                onUpdateApiBaseUrl = viewModel::updateApiBaseUrl,
                                onSetUseLiveApi = viewModel::setUseLiveApi,
                                onSyncApi = { viewModel.syncKumanimeCatalog(silentIfOffline = false) },
                                onOpenThemeEditor = viewModel::openThemeEditorFromProfile,
                                onClearCache = viewModel::clearAppCache,
                                onLogout = viewModel::logout
                            )
                        }
                    }
                }

                is AppRoute.AnimeDetail -> {
                    val anime = uiState.allAnime.find { it.id == route.animeId }
                    if (anime != null) {
                        AnimeDetailScreen(
                            anime = anime,
                            uiState = uiState,
                            bottomInsetPadding = bottomContentPadding,
                            onBack = { viewModel.navigateBack() },
                            onPlayEpisode = { id, epId -> viewModel.playAnime(id, epId) },
                            onSelectWatchlist = viewModel::setWatchlistCategory,
                            onRateAnime = viewModel::rateAnime,
                            onToggleReminder = viewModel::toggleAnimeReminder,
                            onDownloadEpisode = { a, ep -> viewModel.handleEpisodeDownloadAction(a, ep) },
                            onGenreClick = viewModel::openExploreWithGenre,
                            onRelatedAnimeClick = viewModel::openAnimeDetail,
                            onBookmarkClick = viewModel::toggleQuickBookmark,
                            onPostComment = viewModel::postComment,
                            onToggleCommentLike = viewModel::toggleCommentLike,
                            onShareClick = { title ->
                                viewModel.emitMessage("Tautan '$title' disalin ke papan klip")
                            }
                        )
                    }
                }

                is AppRoute.Catalog -> {
                    val list = remember(uiState.allAnime, route.preset, route.initialGenre) {
                        viewModel.getCatalogAnime(route.preset, route.initialGenre)
                    }
                    CatalogListScreen(
                        title = route.title,
                        subtitle = route.preset.subtitle,
                        animeList = list,
                        watchlist = uiState.watchlist.keys,
                        bottomInsetPadding = bottomContentPadding,
                        onBack = { viewModel.navigateBack() },
                        onAnimeClick = viewModel::openAnimeDetail,
                        onPlayAnime = { id -> viewModel.playAnime(id) },
                        onBookmarkClick = viewModel::toggleQuickBookmark
                    )
                }
            }
        }

        // 2. Persistent Floating Mini-Player & Bottom Navigation
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            val session = uiState.playerSession
            if (session != null && !session.isExpanded) {
                val playingAnime = uiState.allAnime.find { it.id == session.animeId }
                if (playingAnime != null) {
                    MiniPlayerBar(
                        anime = playingAnime,
                        session = session,
                        onExpand = viewModel::expandPlayer,
                        onTogglePlay = viewModel::togglePlayPause,
                        onNextEpisode = viewModel::playNextEpisode,
                        onClose = viewModel::closePlayer,
                        modifier = Modifier.padding(
                            bottom = if (isTabsRoot) 2.dp else 14.dp
                        )
                    )
                    if (!isTabsRoot) {
                        Box(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }

            if (isTabsRoot) {
                FloatingGoldBottomNav(
                    currentTab = uiState.currentTab,
                    libraryBadgeCount = uiState.activeDownloadCount,
                    onSelectTab = viewModel::selectTab
                )
            }
        }

        // 3. Expanded Video Player Overlay
        val activeSession = uiState.playerSession
        val playerAnime = activeSession?.let { sess ->
            uiState.allAnime.find { it.id == sess.animeId }
        }
        val playerEpisode = activeSession?.let { sess ->
            playerAnime?.episodes?.find { it.id == sess.episodeId }
        }

        AnimatedVisibility(
            visible = activeSession != null && activeSession.isExpanded && playerAnime != null && playerEpisode != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            if (activeSession != null && playerAnime != null && playerEpisode != null) {
                PlayerScreen(
                    anime = playerAnime,
                    episode = playerEpisode,
                    session = activeSession,
                    uiState = uiState,
                    subtitleText = viewModel.getCurrentSubtitleText(activeSession),
                    onMinimize = viewModel::minimizePlayer,
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onSeekTo = viewModel::seekTo,
                    onSeekBy = viewModel::seekBy,
                    onSkipIntro = viewModel::skipIntro,
                    onNextEpisode = viewModel::playNextEpisode,
                    onPreviousEpisode = viewModel::playPreviousEpisode,
                    onCancelAutoNext = viewModel::cancelAutoNext,
                    onSelectEpisode = { ep -> viewModel.playAnime(playerAnime.id, ep.id, startFromZero = true) },
                    onSelectQuality = viewModel::setPlayerQuality,
                    onSelectServer = viewModel::setPlayerServer,
                    onSelectSubtitle = viewModel::setPlayerSubtitle,
                    onSelectSubtitleSize = viewModel::updateSubtitleSize,
                    onSelectSpeed = viewModel::setPlaybackSpeed,
                    onCycleAspect = viewModel::cycleAspectMode,
                    onToggleLock = viewModel::togglePlayerLock,
                    onToggleFullscreen = { viewModel.setPlayerFullscreen(!activeSession.isFullscreen) },
                    onToggleWebStreamView = viewModel::togglePlayerWebStreamView,
                    onToggleLike = viewModel::toggleEpisodeLike,
                    onSelectWatchlist = viewModel::setWatchlistCategory,
                    onDownloadEpisode = viewModel::handleEpisodeDownloadAction,
                    onPostComment = viewModel::postComment,
                    onToggleCommentLike = viewModel::toggleCommentLike,
                    onOpenAnimeDetail = { animeId ->
                        viewModel.minimizePlayer()
                        viewModel.openAnimeDetail(animeId)
                    },
                    onShowMessage = viewModel::emitMessage
                )
            }
        }
        } // end else (uiState.isAuthenticated)

        // 4. Global Smooth Snackbar Toast
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    bottom = if (uiState.isAuthenticated && uiState.hasCompletedThemeSetup && !uiState.isEditingTheme && isTabsRoot) 88.dp else 20.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) { data ->
            Snackbar(
                shape = RoundedCornerShape(16.dp),
                containerColor = ObsidianElevated,
                contentColor = TextPrimary,
                modifier = Modifier.border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            notifications = uiState.notifications,
            onNotificationClick = { item ->
                viewModel.markNotificationRead(item.id)
                showNotificationsSheet = false
                item.animeId?.let(viewModel::openAnimeDetail)
            },
            onMarkAllRead = viewModel::markAllNotificationsRead,
            onDismiss = { showNotificationsSheet = false }
        )
    }
    } // end AnimWatchTheme
}

@Preview(showBackground = true)
@Composable
fun AnimWatchAppPreview() {
    AnimWatchApp()
}
