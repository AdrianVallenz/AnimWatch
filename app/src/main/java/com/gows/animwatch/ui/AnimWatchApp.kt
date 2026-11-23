package com.gows.animwatch.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gows.animwatch.R
import com.gows.animwatch.data.local.AndroidDeviceStorageManager
import com.gows.animwatch.data.local.AppNotificationHelper
import com.gows.animwatch.data.local.NotificationDeepLinkBus
import com.gows.animwatch.data.local.SharedPreferencesSessionStore
import com.gows.animwatch.data.model.CatalogPreset
import com.gows.animwatch.data.remote.GoogleAuthDeepLinkBus
import com.gows.animwatch.data.remote.GoogleAuthManager
import com.gows.animwatch.data.remote.GoogleSignInOutcome
import kotlinx.coroutines.launch
import com.gows.animwatch.ui.components.CassetteLinearShimmer
import com.gows.animwatch.ui.components.FloatingGoldBottomNav
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.MiniPlayerBar
import com.gows.animwatch.ui.components.NotificationsBottomSheet
import com.gows.animwatch.ui.screens.AdminPanelScreen
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
import com.gows.animwatch.ui.state.LibraryTab
import com.gows.animwatch.ui.state.MainTab
import com.gows.animwatch.ui.theme.AnimWatchTheme
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

@Composable
fun AnimWatchApp(
    viewModel: AnimWatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingGoogleCallback by GoogleAuthDeepLinkBus.pendingCallback.collectAsStateWithLifecycle()
    val pendingNotificationTarget by NotificationDeepLinkBus.pendingTarget.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var hasDismissedPermissionPrompt by remember { mutableStateOf(false) }
    val localContext = LocalContext.current
    val appContext = localContext.applicationContext
    val coroutineScope = rememberCoroutineScope()

    val multiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasDismissedPermissionPrompt = true
        viewModel.refreshDeviceStorageAndPermissions()
        viewModel.onNotificationPermissionGranted()
    }

    LaunchedEffect(Unit) {
        viewModel.attachSessionStore(SharedPreferencesSessionStore(appContext))
        viewModel.attachStorageGateway(AndroidDeviceStorageManager(appContext))
        viewModel.fetchGoogleAuthConfig()
    }

    LaunchedEffect(pendingGoogleCallback) {
        val payload = pendingGoogleCallback ?: return@LaunchedEffect
        GoogleAuthDeepLinkBus.consume()
        viewModel.completeGoogleOAuthCallback(payload.token, payload.error)
    }

    LaunchedEffect(
        pendingNotificationTarget,
        uiState.isAuthenticated,
        uiState.hasCompletedThemeSetup,
        uiState.isEditingTheme
    ) {
        val target = pendingNotificationTarget ?: return@LaunchedEffect
        if (uiState.isAuthenticated && uiState.hasCompletedThemeSetup && !uiState.isEditingTheme) {
            NotificationDeepLinkBus.consume()
            target.notificationId?.let(viewModel::markNotificationRead)
            viewModel.openAnimeDetail(target.animeId)
        }
    }

    LaunchedEffect(uiState.currentTab) {
        if (uiState.currentTab == MainTab.LIBRARY || uiState.currentTab == MainTab.PROFILE) {
            viewModel.refreshDeviceStorageAndPermissions()
        }
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

        LaunchedEffect(Unit) {
            viewModel.systemNotificationBatch.collect { items ->
                if (viewModel.uiState.value.settings.episodeNotifications) {
                    val posted = AppNotificationHelper.postEpisodeUpdate(appContext, items)
                    if (posted) {
                        viewModel.markSystemNotificationDispatched(items)
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.systemNotificationCancelEvent.collect {
                AppNotificationHelper.cancelEpisodeNotifications(appContext)
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
                val localClientId = runCatching {
                    localContext.getString(R.string.google_web_client_id).trim()
                }.getOrDefault("")
                val resolvedClientId = uiState.googleWebClientId.ifBlank { localClientId }
                val hasCloudOAuthConfigured = resolvedClientId.isNotBlank()

                var triggerManualGooglePrompt by remember { mutableStateOf(false) }

                val googleAccountPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == android.app.Activity.RESULT_OK) {
                        val selectedEmail = result.data
                            ?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
                            ?.trim()
                            .orEmpty()
                        if (selectedEmail.isNotBlank()) {
                            val derivedName = selectedEmail
                                .substringBefore("@")
                                .replace(Regex("[._-]+"), " ")
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                                .ifEmpty { "Google User" }
                            viewModel.loginWithGoogleProfile(selectedEmail, derivedName)
                        } else {
                            triggerManualGooglePrompt = true
                        }
                    } else {
                        viewModel.setGoogleAuthLoading(false)
                    }
                }

                AuthScreen(
                    authError = uiState.authError,
                    isGoogleLoading = uiState.isGoogleAuthLoading,
                    hasNativeGoogleOAuth = hasCloudOAuthConfigured,
                    forceOpenManualDialog = triggerManualGooglePrompt,
                    onConsumeForceOpenManualDialog = { triggerManualGooglePrompt = false },
                    onClearAuthError = viewModel::clearAuthError,
                    onGoogleSignIn = {
                        val activity = localContext.findHostActivity()
                        if (activity == null) {
                            viewModel.onGoogleAuthError("Tidak dapat membuka jendela login Google di perangkat ini.")
                        } else {
                            coroutineScope.launch {
                                viewModel.setGoogleAuthLoading(true)
                                when (
                                    val outcome = GoogleAuthManager.signIn(
                                        activity = activity,
                                        webClientId = resolvedClientId,
                                        webOAuthEnabled = uiState.googleWebOAuthEnabled,
                                        apiBaseUrl = uiState.apiConfig.baseUrl
                                    )
                                ) {
                                    is GoogleSignInOutcome.IdToken -> {
                                        viewModel.loginWithGoogleIdToken(outcome.idToken)
                                    }
                                    GoogleSignInOutcome.LaunchedBrowserOAuth -> {
                                        viewModel.setGoogleAuthLoading(false)
                                        viewModel.emitMessage("Selesaikan login Google di browser...")
                                    }
                                    GoogleSignInOutcome.OpenInAppGooglePrompt -> {
                                        viewModel.setGoogleAuthLoading(false)
                                        runCatching {
                                            @Suppress("DEPRECATION")
                                            val pickerIntent = android.accounts.AccountManager.newChooseAccountIntent(
                                                null,
                                                null,
                                                arrayOf("com.google"),
                                                true,
                                                null,
                                                null,
                                                null,
                                                null
                                            )
                                            if (pickerIntent.resolveActivity(localContext.packageManager) != null) {
                                                googleAccountPickerLauncher.launch(pickerIntent)
                                            } else {
                                                triggerManualGooglePrompt = true
                                            }
                                        }.onFailure {
                                            triggerManualGooglePrompt = true
                                        }
                                    }
                                    GoogleSignInOutcome.Cancelled -> {
                                        viewModel.setGoogleAuthLoading(false)
                                    }
                                    is GoogleSignInOutcome.Failure -> {
                                        viewModel.onGoogleAuthError(outcome.message)
                                    }
                                }
                            }
                        }
                    },
                    onGoogleDirectSignIn = { email, displayName ->
                        viewModel.loginWithGoogleProfile(email, displayName)
                    }
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
                                onOpenGenre = viewModel::openExploreWithGenre,
                                onOpenHistory = {
                                    viewModel.selectTab(MainTab.LIBRARY)
                                    viewModel.setLibraryTab(LibraryTab.HISTORY)
                                },
                                onLoadMoreCatalog = { viewModel.loadMoreCatalog(silent = true) },
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
                                onToggleOnlyPlayable = viewModel::toggleOnlyPlayableFilter,
                                onSelectAudioFilter = viewModel::setAudioFilter,
                                onStatusChange = viewModel::setStatusFilter,
                                onTypeChange = viewModel::setTypeFilter,
                                onYearChange = viewModel::setYearFilter,
                                onSortChange = viewModel::setSortOption,
                                onToggleGridView = viewModel::toggleExploreGridMode,
                                onResetFilters = viewModel::resetExploreFilters,
                                onResetGenreFilters = viewModel::resetExploreGenreFilter,
                                onLoadMoreCatalog = { viewModel.loadMoreCatalog(silent = true) },
                                onRetryLoadMoreCatalog = viewModel::retryLoadMoreCatalog,
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
                                onUpdateWatchlistCategory = viewModel::setWatchlistCategory,
                                onRemoveHistory = viewModel::removeHistoryItem,
                                onClearAllHistory = viewModel::clearAllHistory,
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
                                onRefreshDeviceState = viewModel::refreshDeviceStorageAndPermissions,
                                onOpenAdminPanel = viewModel::openAdminPanel,
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
                            onUpdateWatchedEpisodes = viewModel::updateWatchedEpisodes,
                            onRateAnime = viewModel::rateAnime,
                            onToggleReminder = viewModel::toggleAnimeReminder,
                            onGenreClick = viewModel::openExploreWithGenre,
                            onRelatedAnimeClick = viewModel::openAnimeDetail,
                            onBookmarkClick = viewModel::toggleQuickBookmark,
                            onPostComment = viewModel::postComment,
                            onToggleCommentLike = viewModel::toggleCommentLike,
                            onDeleteComment = viewModel::deleteComment,
                            onShowMessage = viewModel::emitMessage,
                            onShareClick = { title ->
                                val shareUrl = "https://animwatch.id/anime/${anime.slug}"
                                val shareText = "Tonton $title di AnimWatch: $shareUrl"
                                val clipboard = localContext.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("AnimWatch", shareUrl))
                                val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                runCatching {
                                    localContext.startActivity(
                                        android.content.Intent.createChooser(sendIntent, "Bagikan Anime")
                                            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }.onFailure {
                                    viewModel.emitMessage("Tautan '$title' disalin ke papan klip")
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ObsidianVoid)
                                .statusBarsPadding()
                        ) {
                            IconButton(
                                onClick = { viewModel.navigateBack() },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                                    .size(40.dp)
                                    .background(ObsidianCard, CircleShape)
                                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = TextPrimary
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (uiState.isLoadingDetail) "Memuat detail anime..." else "Anime belum tersedia",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                if (uiState.isLoadingDetail) {
                                    CassetteLinearShimmer(modifier = Modifier.fillMaxWidth(0.7f))
                                } else {
                                    GoldGradientButton(
                                        text = "Kembali",
                                        onClick = { viewModel.navigateBack() },
                                        height = 42.dp
                                    )
                                }
                            }
                        }
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
                        onBookmarkClick = viewModel::toggleQuickBookmark,
                        hasMore = uiState.hasMoreCatalog &&
                            route.preset != CatalogPreset.DUB_INDO &&
                            route.preset != CatalogPreset.TOP_RATED,
                        isLoadingMore = uiState.isLoadingMoreCatalog,
                        catalogPageLoaded = uiState.catalogPageLoaded,
                        hasLoadError = uiState.catalogLoadError &&
                            route.preset != CatalogPreset.DUB_INDO &&
                            route.preset != CatalogPreset.TOP_RATED,
                        onLoadMore = { viewModel.loadMoreCatalog(silent = true) },
                        onRetryLoadMore = viewModel::retryLoadMoreCatalog
                    )
                }

                is AppRoute.AdminPanel -> {
                    AdminPanelScreen(
                        uiState = uiState,
                        bottomInsetPadding = bottomContentPadding,
                        onBack = { viewModel.navigateBack() },
                        onVerifyAdmin = viewModel::verifyAdminAccess,
                        onRefreshOverview = { viewModel.loadAdminOverview(silent = false) },
                        onSaveAnime = viewModel::saveAdminAnime,
                        onQuickToggleHide = viewModel::quickToggleCatalogAnimeHidden,
                        onDeleteAnime = viewModel::deleteAdminAnime,
                        onSavePlaylist = viewModel::saveAdminPlaylist,
                        onDeletePlaylist = viewModel::deleteAdminPlaylist,
                        onDeleteComment = viewModel::adminDeleteComment,
                        onUpdateUserRole = viewModel::adminUpdateUserRole,
                        onUpdateServerUrl = viewModel::updateApiBaseUrl
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
                    onSelectEpisode = { ep ->
                        viewModel.playAnime(
                            animeId = playerAnime.id,
                            episodeId = ep.id,
                            startFromZero = true,
                            preserveFullscreen = activeSession.isFullscreen
                        )
                    },
                    onSelectQuality = viewModel::setPlayerQuality,
                    onSelectServer = viewModel::setPlayerServer,
                    onSelectSubtitle = viewModel::setPlayerSubtitle,
                    onSelectSubtitleSize = viewModel::updateSubtitleSize,
                    onSelectSpeed = viewModel::setPlaybackSpeed,
                    onCycleAspect = viewModel::cycleAspectMode,
                    onToggleLock = viewModel::togglePlayerLock,
                    onToggleFullscreen = { viewModel.setPlayerFullscreen(!activeSession.isFullscreen) },
                    onToggleWebStreamView = viewModel::togglePlayerWebStreamView,
                    onWebProgressUpdate = viewModel::updateWebPlaybackProgress,
                    onWebEpisodeEnded = viewModel::onWebEpisodeEnded,
                    onEpisodeCompleted = viewModel::markEpisodeCompleted,
                    onMarkAnimeCompleted = viewModel::markAnimeCompleted,
                    onToggleLike = viewModel::toggleEpisodeLike,
                    onSelectWatchlist = viewModel::setWatchlistCategory,
                    onPostComment = viewModel::postComment,
                    onToggleCommentLike = viewModel::toggleCommentLike,
                    onDeleteComment = viewModel::deleteComment,
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
        val snackBottomPadding = when {
            uiState.isAuthenticated && uiState.hasCompletedThemeSetup && !uiState.isEditingTheme && isTabsRoot && hasMiniPlayer -> 168.dp
            uiState.isAuthenticated && uiState.hasCompletedThemeSetup && !uiState.isEditingTheme && isTabsRoot -> 88.dp
            hasMiniPlayer -> 88.dp
            else -> 20.dp
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    bottom = snackBottomPadding,
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
            onRefreshUpdates = viewModel::refreshLatestUpdates,
            onDismiss = { showNotificationsSheet = false }
        )
    }

    val pendingRuntimePermissions = remember(uiState.appPermissions) {
        uiState.appPermissions.filter { it.id != "storage" && it.canRequestRuntime && it.androidPermission != null }
    }
    val showPermissionsWelcome = uiState.isAuthenticated &&
        uiState.hasCompletedThemeSetup &&
        !uiState.isEditingTheme &&
        !hasDismissedPermissionPrompt &&
        pendingRuntimePermissions.isNotEmpty()

    if (showPermissionsWelcome) {
        AlertDialog(
            onDismissRequest = { hasDismissedPermissionPrompt = true },
            containerColor = ObsidianElevated,
            shape = RoundedCornerShape(24.dp),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Izin Penggunaan Aplikasi",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "AnimWatch memerlukan izin berikut agar notifikasi rilis episode anime terbaru berjalan lancar:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    uiState.appPermissions.filterNot { it.id == "storage" }.forEach { perm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ObsidianCard, RoundedCornerShape(12.dp))
                                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 11.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = perm.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = perm.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (perm.isGranted) "Aktif" else "Perlu Izin",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (perm.isGranted) StatusEmerald else GoldPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                GoldGradientButton(
                    text = "Izinkan Semua",
                    onClick = {
                        val toRequest = pendingRuntimePermissions
                            .mapNotNull { it.androidPermission }
                            .toTypedArray()
                        if (toRequest.isNotEmpty()) {
                            multiPermissionLauncher.launch(toRequest)
                        } else {
                            hasDismissedPermissionPrompt = true
                        }
                    },
                    height = 40.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { hasDismissedPermissionPrompt = true }) {
                    Text("Nanti Saja", color = TextSecondary)
                }
            }
        )
    }
    } // end AnimWatchTheme
}

@Preview(showBackground = true)
@Composable
fun AnimWatchAppPreview() {
    AnimWatchApp()
}
