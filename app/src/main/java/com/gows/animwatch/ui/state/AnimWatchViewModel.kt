package com.gows.animwatch.ui.state

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
import com.gows.animwatch.data.model.ApiConfigState
import com.gows.animwatch.data.model.ApiConnectionStatus
import com.gows.animwatch.data.model.CatalogPreset
import com.gows.animwatch.data.model.CommentItem
import com.gows.animwatch.data.model.DownloadStatus
import com.gows.animwatch.data.model.DownloadTask
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.NotificationItem
import com.gows.animwatch.data.model.PlaybackSettings
import com.gows.animwatch.data.model.ReleaseDay
import com.gows.animwatch.data.model.SortOption
import com.gows.animwatch.data.model.SubtitleCue
import com.gows.animwatch.data.model.SubtitleSizeOption
import com.gows.animwatch.data.model.UserProfile
import com.gows.animwatch.data.model.VideoAspectMode
import com.gows.animwatch.data.model.WatchProgress
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.data.remote.KumanimeApiClient
import com.gows.animwatch.data.remote.KumanimeRemoteDataSource
import com.gows.animwatch.data.repository.AnimeRepository
import com.gows.animwatch.data.repository.MockAnimeRepository
import com.gows.animwatch.ui.theme.AppAccentTheme
import com.gows.animwatch.ui.theme.AppAppearanceMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

enum class MainTab(val label: String) {
    HOME("Beranda"),
    EXPLORE("Jelajah"),
    LIBRARY("Koleksiku"),
    PROFILE("Profil")
}

enum class ExploreMode(val label: String) {
    CATALOG("Katalog"),
    SCHEDULE("Jadwal"),
    RANKING("Peringkat")
}

enum class LibraryTab(val label: String) {
    WATCHLIST("Daftar Tonton"),
    HISTORY("Riwayat"),
    DOWNLOADS("Unduhan")
}

sealed interface AppRoute {
    data object TabsRoot : AppRoute
    data class AnimeDetail(val animeId: String) : AppRoute
    data class Catalog(
        val preset: CatalogPreset,
        val title: String,
        val initialGenre: String? = null
    ) : AppRoute
}

@Immutable
data class ExploreFilterState(
    val query: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val statusFilter: AnimeStatus? = null,
    val typeFilter: AnimeType? = null,
    val yearFilter: Int? = null,
    val sortOption: SortOption = SortOption.POPULAR,
    val isGridView: Boolean = true
) {
    val activeFilterCount: Int
        get() = selectedGenres.size +
            (if (statusFilter != null) 1 else 0) +
            (if (typeFilter != null) 1 else 0) +
            (if (yearFilter != null) 1 else 0)
}

@Immutable
data class PlayerSession(
    val animeId: String,
    val episodeId: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val isExpanded: Boolean = true,
    val isPlaying: Boolean = true,
    val positionSeconds: Int = 0,
    val durationSeconds: Int = 1440,
    val bufferedSeconds: Int = 180,
    val selectedQuality: String = "1080p",
    val selectedServerId: String = "sanka",
    val selectedSubtitleId: String = "id",
    val playbackSpeed: Float = 1.0f,
    val aspectMode: VideoAspectMode = VideoAspectMode.FIT,
    val isLocked: Boolean = false,
    val isFullscreen: Boolean = false,
    val autoNextCountdownSec: Int? = null,
    val isEpisodeLiked: Boolean = false,
    val streamUrl: String = "",
    val isLoadingStream: Boolean = false,
    val useWebStreamView: Boolean = true
)

@Immutable
data class AnimWatchUiState(
    val isAuthenticated: Boolean = false,
    val isGuestSession: Boolean = false,
    val hasCompletedThemeSetup: Boolean = false,
    val isEditingTheme: Boolean = false,
    val appearanceMode: AppAppearanceMode = AppAppearanceMode.DARK,
    val accentTheme: AppAccentTheme = AppAccentTheme.COBALT_SAPPHIRE,
    val preferredGenres: Set<String> = setOf("Action", "Fantasy", "Sci-Fi"),
    val authError: String? = null,
    val currentTab: MainTab = MainTab.HOME,
    val backStack: List<AppRoute> = listOf(AppRoute.TabsRoot),
    val allAnime: List<Anime> = emptyList(),
    val genres: List<String> = emptyList(),
    val watchlist: Map<String, WatchlistCategory> = emptyMap(),
    val userRatings: Map<String, Int> = emptyMap(),
    val watchHistory: List<WatchProgress> = emptyList(),
    val downloads: List<DownloadTask> = emptyList(),
    val comments: List<CommentItem> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val reminders: Set<String> = emptySet(),
    val searchHistory: List<String> = listOf("Kage no Ou", "Cyber", "Isekai", "Movie 4K"),
    val exploreMode: ExploreMode = ExploreMode.CATALOG,
    val exploreFilter: ExploreFilterState = ExploreFilterState(),
    val selectedScheduleDay: ReleaseDay = currentReleaseDay(),
    val libraryTab: LibraryTab = LibraryTab.WATCHLIST,
    val selectedWatchlistFilter: WatchlistCategory? = null,
    val userProfile: UserProfile = UserProfile(
        displayName = "Adrian Vallenz",
        handle = "@adrianvallenz",
        bio = "",
        avatarEmblem = "AV",
        memberTier = "PRO STREAM PASS",
        watchedHours = 0,
        completedAnimeCount = 0,
        favoriteGenre = "Action"
    ),
    val settings: PlaybackSettings = PlaybackSettings(),
    val playerSession: PlayerSession? = null,
    val cacheSizeMb: Int = 184,
    val apiConfig: ApiConfigState = ApiConfigState(),
    val isLoadingDetail: Boolean = false,
    val isSearchingRemote: Boolean = false
) {
    val currentRoute: AppRoute
        get() = backStack.lastOrNull() ?: AppRoute.TabsRoot

    val unreadNotificationCount: Int
        get() = notifications.count { !it.isRead }

    val activeDownloadCount: Int
        get() = downloads.count { it.status == DownloadStatus.DOWNLOADING }

    companion object {
        fun currentReleaseDay(): ReleaseDay {
            return when (LocalDate.now().dayOfWeek) {
                DayOfWeek.MONDAY -> ReleaseDay.SENIN
                DayOfWeek.TUESDAY -> ReleaseDay.SELASA
                DayOfWeek.WEDNESDAY -> ReleaseDay.RABU
                DayOfWeek.THURSDAY -> ReleaseDay.KAMIS
                DayOfWeek.FRIDAY -> ReleaseDay.JUMAT
                DayOfWeek.SATURDAY -> ReleaseDay.SABTU
                DayOfWeek.SUNDAY -> ReleaseDay.MINGGU
            }
        }
    }
}

class AnimWatchViewModel(
    private val repository: AnimeRepository = MockAnimeRepository(),
    private val remoteDataSource: KumanimeRemoteDataSource = KumanimeRemoteDataSource()
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<AnimWatchUiState> = _uiState.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    private data class LocalAccount(
        val displayName: String,
        val email: String,
        val password: String,
        val handle: String,
        val emblem: String,
        val tier: String
    )

    private val registeredAccounts = mutableMapOf(
        "adrian@animwatch.id" to LocalAccount(
            displayName = "Adrian Vallenz",
            email = "adrian@animwatch.id",
            password = "123456",
            handle = "@adrianvallenz",
            emblem = "AV",
            tier = "PRO STREAM PASS"
        )
    )

    private var playerTickerJob: Job? = null
    private var apiSyncJob: Job? = null
    private var detailFetchJob: Job? = null
    private var streamFetchJob: Job? = null
    private var remoteSearchJob: Job? = null
    private val downloadJobs = mutableMapOf<String, Job>()

    private fun loadInitialState(): AnimWatchUiState {
        return AnimWatchUiState(
            allAnime = repository.getAllAnime(),
            genres = repository.getAllGenres(),
            watchlist = repository.getInitialWatchlist(),
            watchHistory = repository.getInitialWatchHistory(),
            downloads = repository.getInitialDownloads(),
            comments = repository.getInitialComments(),
            notifications = repository.getInitialNotifications(),
            reminders = repository.getInitialReminders(),
            userProfile = repository.getInitialUserProfile(),
            settings = repository.getInitialPlaybackSettings(),
            apiConfig = ApiConfigState(
                baseUrl = KumanimeApiClient.DEFAULT_BASE_URL,
                useLiveApi = true,
                status = ApiConnectionStatus.DEMO_LOCAL,
                lastSyncMessage = "Menunggu sinkronisasi ke kumanime-api"
            )
        )
    }

    fun emitMessage(message: String) {
        _toastMessages.tryEmit(message)
    }

    // ====================
    // Authentication
    // ====================

    private fun buildEmblem(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "AW"
        }
    }

    private fun buildHandle(nameOrEmail: String): String {
        val base = nameOrEmail.substringBefore("@")
            .lowercase()
            .replace(Regex("[^a-z0-9_]"), "")
            .ifEmpty { "watcher" }
        return "@$base"
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    fun login(identifier: String, password: String, rememberMe: Boolean = true) {
        val key = identifier.trim().lowercase()
        val matched = registeredAccounts[key]
            ?: registeredAccounts.values.find {
                it.handle.removePrefix("@").equals(key.removePrefix("@"), ignoreCase = true)
            }

        if (matched != null) {
            if (matched.password != password) {
                _uiState.update { it.copy(authError = "Kata sandi salah untuk ${matched.email}") }
                return
            }
            _uiState.update { state ->
                state.copy(
                    isAuthenticated = true,
                    isGuestSession = false,
                    hasCompletedThemeSetup = false,
                    isEditingTheme = false,
                    authError = null,
                    currentTab = MainTab.HOME,
                    backStack = listOf(AppRoute.TabsRoot),
                    userProfile = state.userProfile.copy(
                        displayName = matched.displayName,
                        handle = matched.handle,
                        avatarEmblem = matched.emblem,
                        memberTier = matched.tier
                    )
                )
            }
            emitMessage("Selamat datang kembali, ${matched.displayName}. Pilih temamu!")
            return
        }

        // Untuk akun baru / uji cepat tanpa backend auth
        val derivedName = identifier.trim()
            .substringBefore("@")
            .replace(Regex("[._-]+"), " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
            .ifEmpty { "Anime Watcher" }

        val newAccount = LocalAccount(
            displayName = derivedName,
            email = key,
            password = password,
            handle = buildHandle(identifier),
            emblem = buildEmblem(derivedName),
            tier = if (rememberMe) "PRO MEMBER" else "STANDARD PASS"
        )
        registeredAccounts[key] = newAccount

        _uiState.update { state ->
            state.copy(
                isAuthenticated = true,
                isGuestSession = false,
                hasCompletedThemeSetup = false,
                isEditingTheme = false,
                authError = null,
                currentTab = MainTab.HOME,
                backStack = listOf(AppRoute.TabsRoot),
                userProfile = state.userProfile.copy(
                    displayName = newAccount.displayName,
                    handle = newAccount.handle,
                    avatarEmblem = newAccount.emblem,
                    memberTier = newAccount.tier
                )
            )
        }
        emitMessage("Berhasil masuk sebagai ${newAccount.displayName}. Pilih temamu!")
    }

    fun register(displayName: String, email: String, password: String) {
        val cleanName = displayName.trim()
        val cleanEmail = email.trim().lowercase()
        if (registeredAccounts.containsKey(cleanEmail)) {
            _uiState.update { it.copy(authError = "Email $cleanEmail sudah terdaftar. Silakan masuk.") }
            return
        }

        val account = LocalAccount(
            displayName = cleanName,
            email = cleanEmail,
            password = password,
            handle = buildHandle(cleanName),
            emblem = buildEmblem(cleanName),
            tier = "PRO STREAM PASS"
        )
        registeredAccounts[cleanEmail] = account

        _uiState.update { state ->
            state.copy(
                isAuthenticated = true,
                isGuestSession = false,
                hasCompletedThemeSetup = false,
                isEditingTheme = false,
                authError = null,
                currentTab = MainTab.HOME,
                backStack = listOf(AppRoute.TabsRoot),
                userProfile = state.userProfile.copy(
                    displayName = account.displayName,
                    handle = account.handle,
                    avatarEmblem = account.emblem,
                    memberTier = account.tier,
                    bio = "Anggota baru AnimWatch Stream Deck."
                )
            )
        }
        emitMessage("Akun ${account.displayName} dibuat! Pilih tema kesukaanmu.")
    }

    fun loginDemoVip() {
        val demo = registeredAccounts["adrian@animwatch.id"] ?: return
        login(demo.email, demo.password, rememberMe = true)
    }

    fun continueAsGuest() {
        _uiState.update { state ->
            state.copy(
                isAuthenticated = true,
                isGuestSession = true,
                hasCompletedThemeSetup = false,
                isEditingTheme = false,
                authError = null,
                currentTab = MainTab.HOME,
                backStack = listOf(AppRoute.TabsRoot),
                userProfile = state.userProfile.copy(
                    displayName = "Tamu Watcher",
                    handle = "@guest_deck",
                    avatarEmblem = "TM",
                    memberTier = "MODE TAMU",
                    bio = "Menjelajah AnimWatch dalam mode tamu."
                )
            )
        }
        emitMessage("Masuk dalam Mode Tamu. Atur tema tampilanmu!")
    }

    fun requestPasswordReset(email: String) {
        emitMessage("Tautan pemulihan kata sandi dikirim ke $email")
    }

    fun logout() {
        closePlayer()
        _uiState.update { state ->
            state.copy(
                isAuthenticated = false,
                isGuestSession = false,
                hasCompletedThemeSetup = false,
                isEditingTheme = false,
                authError = null,
                currentTab = MainTab.HOME,
                backStack = listOf(AppRoute.TabsRoot)
            )
        }
        emitMessage("Berhasil keluar dari akun")
    }

    // ====================
    // Theme & Preferences
    // ====================

    fun setAppearanceMode(mode: AppAppearanceMode) {
        _uiState.update { it.copy(appearanceMode = mode) }
    }

    fun setAccentTheme(accent: AppAccentTheme) {
        _uiState.update { it.copy(accentTheme = accent) }
    }

    fun togglePreferredGenre(genre: String) {
        _uiState.update { state ->
            val current = state.preferredGenres
            val next = if (genre in current) current - genre else current + genre
            state.copy(preferredGenres = next)
        }
    }

    fun setGridCatalogPreferred(isGrid: Boolean) {
        _uiState.update { state ->
            state.copy(exploreFilter = state.exploreFilter.copy(isGridView = isGrid))
        }
    }

    fun completeThemeOnboarding() {
        val state = _uiState.value
        val favGenre = state.preferredGenres.firstOrNull() ?: state.userProfile.favoriteGenre
        val wasEditing = state.isEditingTheme
        _uiState.update { cur ->
            cur.copy(
                hasCompletedThemeSetup = true,
                isEditingTheme = false,
                userProfile = cur.userProfile.copy(favoriteGenre = favGenre)
            )
        }
        emitMessage(
            if (wasEditing) "Tema ${state.appearanceMode.label} • ${state.accentTheme.displayName} disimpan"
            else "Tema ${state.accentTheme.displayName} diterapkan. Selamat menonton!"
        )
    }

    fun openThemeEditorFromProfile() {
        _uiState.update { it.copy(isEditingTheme = true) }
    }

    fun closeThemeEditor() {
        _uiState.update { it.copy(isEditingTheme = false) }
    }

    // ====================
    // Kumanime API Sync
    // ====================

    fun updateApiBaseUrl(rawUrl: String) {
        val normalized = KumanimeApiClient.normalizeBaseUrl(rawUrl)
        _uiState.update { state ->
            state.copy(
                apiConfig = state.apiConfig.copy(
                    baseUrl = normalized,
                    useLiveApi = true
                )
            )
        }
        emitMessage("Base URL diset: $normalized")
        syncKumanimeCatalog(silentIfOffline = false)
    }

    fun setUseLiveApi(enabled: Boolean) {
        if (!enabled) {
            apiSyncJob?.cancel()
            _uiState.update { state ->
                state.copy(
                    allAnime = repository.getAllAnime(),
                    genres = repository.getAllGenres(),
                    apiConfig = state.apiConfig.copy(
                        useLiveApi = false,
                        status = ApiConnectionStatus.DEMO_LOCAL,
                        lastSyncMessage = "Menggunakan katalog lokal (Mode Demo)",
                        remoteCount = 0
                    )
                )
            }
            emitMessage("Beralih ke Mode Demo Lokal")
        } else {
            _uiState.update { state ->
                state.copy(apiConfig = state.apiConfig.copy(useLiveApi = true))
            }
            syncKumanimeCatalog(silentIfOffline = false)
        }
    }

    fun syncKumanimeCatalog(silentIfOffline: Boolean = false) {
        val config = _uiState.value.apiConfig
        if (!config.useLiveApi) return

        apiSyncJob?.cancel()
        apiSyncJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    apiConfig = state.apiConfig.copy(
                        status = ApiConnectionStatus.SYNCING,
                        lastSyncMessage = "Menghubungi ${config.baseUrl}..."
                    )
                )
            }

            val result = remoteDataSource.fetchInitialCatalog(config.baseUrl)
            result.onSuccess { remoteItems ->
                val remoteIds = remoteItems.map { it.id }.toSet()
                val dynamicGenres = remoteItems
                    .flatMap { it.genres }
                    .distinct()
                    .ifEmpty { repository.getAllGenres() }

                _uiState.update { state ->
                    state.copy(
                        allAnime = remoteItems,
                        genres = dynamicGenres,
                        watchlist = state.watchlist.filterKeys { it in remoteIds },
                        watchHistory = state.watchHistory.filter { it.animeId in remoteIds },
                        apiConfig = state.apiConfig.copy(
                            status = ApiConnectionStatus.CONNECTED,
                            lastSyncMessage = "Terhubung (${remoteItems.size} anime live dimuat)",
                            remoteCount = remoteItems.size
                        )
                    )
                }
                if (!silentIfOffline) {
                    emitMessage("Kumanime API terhubung: ${remoteItems.size} anime dimuat")
                }
            }.onFailure { err ->
                val reason = err.localizedMessage?.take(72) ?: "Server tidak dapat dijangkau"
                _uiState.update { state ->
                    state.copy(
                        apiConfig = state.apiConfig.copy(
                            status = ApiConnectionStatus.OFFLINE_FALLBACK,
                            lastSyncMessage = "Fallback lokal ($reason)",
                            remoteCount = 0
                        )
                    )
                }
                if (!silentIfOffline) {
                    emitMessage("Server Kumanime tidak aktif — memakai katalog lokal")
                }
            }
        }
    }

    // ====================
    // Navigation Controls
    // ====================

    fun selectTab(tab: MainTab) {
        _uiState.update { state ->
            state.copy(
                currentTab = tab,
                backStack = listOf(AppRoute.TabsRoot)
            )
        }
    }

    fun openAnimeDetail(animeId: String) {
        _uiState.update { state ->
            val current = state.currentRoute
            if (current is AppRoute.AnimeDetail && current.animeId == animeId) {
                state
            } else {
                state.copy(backStack = state.backStack + AppRoute.AnimeDetail(animeId))
            }
        }
        val anime = _uiState.value.allAnime.find { it.id == animeId }
        if (anime != null && anime.isRemote && (anime.episodes.isEmpty() || anime.isEpisodeEntry || (anime.characters.isEmpty() && anime.episodes.size <= 1))) {
            fetchRemoteDetailFor(anime)
        }
    }

    private fun fetchRemoteDetailFor(anime: Anime) {
        val baseUrl = _uiState.value.apiConfig.baseUrl
        detailFetchJob?.cancel()
        detailFetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }
            val res = remoteDataSource.fetchAnimeDetail(baseUrl, anime)
            res.onSuccess { detailed ->
                _uiState.update { state ->
                    val updatedList = state.allAnime.map {
                        if (it.id == anime.id) detailed else it
                    }.distinctBy { it.id }
                    val updatedBackStack = state.backStack.map { route ->
                        if (route is AppRoute.AnimeDetail && route.animeId == anime.id) {
                            AppRoute.AnimeDetail(detailed.id)
                        } else route
                    }
                    state.copy(
                        allAnime = updatedList,
                        backStack = updatedBackStack,
                        isLoadingDetail = false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingDetail = false) }
                emitMessage("Gagal memuat detail dari Kumanime API")
            }
        }
    }

    fun openCatalog(preset: CatalogPreset, title: String = preset.title, genre: String? = null) {
        _uiState.update { state ->
            state.copy(
                backStack = state.backStack + AppRoute.Catalog(
                    preset = preset,
                    title = title,
                    initialGenre = genre
                )
            )
        }
    }

    fun openExploreSchedule() {
        _uiState.update { state ->
            state.copy(
                currentTab = MainTab.EXPLORE,
                exploreMode = ExploreMode.SCHEDULE,
                backStack = listOf(AppRoute.TabsRoot)
            )
        }
    }

    fun openExploreRanking() {
        _uiState.update { state ->
            state.copy(
                currentTab = MainTab.EXPLORE,
                exploreMode = ExploreMode.RANKING,
                backStack = listOf(AppRoute.TabsRoot)
            )
        }
    }

    fun openExploreWithGenre(genre: String) {
        _uiState.update { state ->
            state.copy(
                currentTab = MainTab.EXPLORE,
                exploreMode = ExploreMode.CATALOG,
                exploreFilter = ExploreFilterState(selectedGenres = setOf(genre)),
                backStack = listOf(AppRoute.TabsRoot)
            )
        }
    }

    fun openRandomAnime() {
        val randomAnime = _uiState.value.allAnime.randomOrNull() ?: return
        openAnimeDetail(randomAnime.id)
        emitMessage("Rekomendasi acak: ${randomAnime.title}")
    }

    fun navigateBack(): Boolean {
        val state = _uiState.value
        if (state.isEditingTheme) {
            closeThemeEditor()
            return true
        }
        val player = state.playerSession
        if (player != null && player.isExpanded) {
            if (player.isFullscreen) {
                setPlayerFullscreen(false)
                return true
            }
            minimizePlayer()
            return true
        }
        if (state.backStack.size > 1) {
            _uiState.update { it.copy(backStack = it.backStack.dropLast(1)) }
            return true
        }
        if (state.currentTab != MainTab.HOME) {
            _uiState.update { it.copy(currentTab = MainTab.HOME) }
            return true
        }
        return false
    }

    // ====================
    // Player & Playback
    // ====================

    fun playAnime(animeId: String, episodeId: String? = null, startFromZero: Boolean = false) {
        val state = _uiState.value
        val anime = state.allAnime.find { it.id == animeId } ?: return

        // Jika anime dari Kumanime API belum memuat daftar episode, ambil detailnya terlebih dahulu
        if (anime.isRemote && anime.episodes.isEmpty()) {
            val baseUrl = state.apiConfig.baseUrl
            emitMessage("Memuat episode dari Kumanime API...")
            detailFetchJob?.cancel()
            detailFetchJob = viewModelScope.launch {
                val detailRes = remoteDataSource.fetchAnimeDetail(baseUrl, anime)
                detailRes.onSuccess { detailed ->
                    _uiState.update { cur ->
                        cur.copy(
                            allAnime = cur.allAnime.map { if (it.id == anime.id) detailed else it }.distinctBy { it.id }
                        )
                    }
                    if (detailed.episodes.isNotEmpty()) {
                        startPlayerWithEpisode(detailed, detailed.episodes.first(), startFromZero)
                    } else {
                        emitMessage("Belum ada episode tersedia untuk ${detailed.title}")
                    }
                }.onFailure {
                    emitMessage("Gagal mengambil daftar episode dari API")
                }
            }
            return
        }

        val historyForAnime = state.watchHistory.find { it.animeId == animeId }
        val targetEpisode: Episode = when {
            episodeId != null -> anime.episodes.find { it.id == episodeId }
            historyForAnime != null -> anime.episodes.find { it.id == historyForAnime.episodeId }
            else -> anime.episodes.firstOrNull()
        } ?: anime.episodes.firstOrNull() ?: return

        startPlayerWithEpisode(anime, targetEpisode, startFromZero)
    }

    private fun startPlayerWithEpisode(anime: Anime, targetEpisode: Episode, startFromZero: Boolean) {
        val state = _uiState.value
        val historyForAnime = state.watchHistory.find { it.animeId == anime.id }

        val resumePos = if (!startFromZero && historyForAnime?.episodeId == targetEpisode.id) {
            if (historyForAnime.positionSeconds >= targetEpisode.durationSeconds - 15) 0
            else historyForAnime.positionSeconds
        } else {
            0
        }

        val subtitleId = when (state.settings.defaultSubtitle) {
            "English" -> "en"
            "Nonaktif" -> "off"
            else -> "id"
        }
        val serverId = targetEpisode.servers.find {
            it.name.equals(state.settings.defaultServer, ignoreCase = true)
        }?.id ?: targetEpisode.servers.firstOrNull()?.id ?: "sanka"

        val needsRemoteStream = anime.isRemote && targetEpisode.slug.isNotBlank() && targetEpisode.streamUrl.isBlank()

        val session = PlayerSession(
            animeId = anime.id,
            episodeId = targetEpisode.id,
            episodeNumber = targetEpisode.number,
            episodeTitle = targetEpisode.title,
            isExpanded = true,
            isPlaying = true,
            positionSeconds = resumePos,
            durationSeconds = targetEpisode.durationSeconds,
            bufferedSeconds = (resumePos + 220).coerceAtMost(targetEpisode.durationSeconds),
            selectedQuality = state.settings.defaultQuality,
            selectedServerId = serverId,
            selectedSubtitleId = subtitleId,
            streamUrl = targetEpisode.streamUrl,
            isLoadingStream = needsRemoteStream,
            useWebStreamView = true
        )

        _uiState.update { it.copy(playerSession = session) }
        recordWatchProgress(anime, targetEpisode, resumePos)
        startPlayerTicker()

        if (needsRemoteStream) {
            fetchRemoteEpisodeStream(anime, targetEpisode)
        }
    }

    private fun fetchRemoteEpisodeStream(anime: Anime, episode: Episode) {
        val baseUrl = _uiState.value.apiConfig.baseUrl
        streamFetchJob?.cancel()
        streamFetchJob = viewModelScope.launch {
            val result = remoteDataSource.fetchEpisodeStream(baseUrl, episode)
            result.onSuccess { streamResult ->
                val enrichedEp = streamResult.episode
                _uiState.update { cur ->
                    val updatedAnimeList = cur.allAnime.map { item ->
                        if (item.id == anime.id) {
                            item.copy(
                                episodes = item.episodes.map { ep ->
                                    if (ep.id == episode.id) enrichedEp else ep
                                }
                            )
                        } else item
                    }
                    val curSession = cur.playerSession
                    val updatedSession = if (curSession?.episodeId == episode.id) {
                        curSession.copy(
                            episodeTitle = enrichedEp.title,
                            streamUrl = enrichedEp.streamUrl,
                            isLoadingStream = false
                        )
                    } else curSession
                    cur.copy(allAnime = updatedAnimeList, playerSession = updatedSession)
                }
                if (enrichedEp.streamUrl.isBlank()) {
                    emitMessage("Link stream tidak tersedia, memakai mode simulasi")
                }
            }.onFailure {
                _uiState.update { cur ->
                    val curSession = cur.playerSession
                    if (curSession?.episodeId == episode.id) {
                        cur.copy(playerSession = curSession.copy(isLoadingStream = false))
                    } else cur
                }
                emitMessage("Gagal memuat link video dari Kumanime API")
            }
        }
    }

    fun togglePlayerWebStreamView() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(useWebStreamView = !session.useWebStreamView))
        }
    }

    fun togglePlayPause() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            val nextPlaying = !session.isPlaying
            val restartPos = if (nextPlaying && session.positionSeconds >= session.durationSeconds) 0 else session.positionSeconds
            state.copy(
                playerSession = session.copy(
                    isPlaying = nextPlaying,
                    positionSeconds = restartPos,
                    autoNextCountdownSec = null
                )
            )
        }
        if (_uiState.value.playerSession?.isPlaying == true) {
            startPlayerTicker()
        } else {
            playerTickerJob?.cancel()
        }
    }

    fun seekTo(seconds: Int) {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            val clamped = seconds.coerceIn(0, session.durationSeconds)
            val updated = session.copy(
                positionSeconds = clamped,
                bufferedSeconds = maxOf(session.bufferedSeconds, (clamped + 180).coerceAtMost(session.durationSeconds)),
                autoNextCountdownSec = null
            )
            state.copy(playerSession = updated)
        }
        syncCurrentProgressToHistory()
    }

    fun seekBy(deltaSeconds: Int) {
        val session = _uiState.value.playerSession ?: return
        seekTo(session.positionSeconds + deltaSeconds)
    }

    fun skipIntro() {
        val session = _uiState.value.playerSession ?: return
        val anime = _uiState.value.allAnime.find { it.id == session.animeId } ?: return
        val ep = anime.episodes.find { it.id == session.episodeId } ?: return
        seekTo(ep.introEndSec + 1)
        emitMessage("Intro dilewati")
    }

    fun playNextEpisode() {
        val session = _uiState.value.playerSession ?: return
        val anime = _uiState.value.allAnime.find { it.id == session.animeId } ?: return
        val nextEp = anime.episodes.find { it.number == session.episodeNumber + 1 }
        if (nextEp != null) {
            playAnime(anime.id, nextEp.id, startFromZero = true)
        } else {
            emitMessage("Sudah di episode terakhir yang tersedia")
        }
    }

    fun playPreviousEpisode() {
        val session = _uiState.value.playerSession ?: return
        val anime = _uiState.value.allAnime.find { it.id == session.animeId } ?: return
        val prevEp = anime.episodes.find { it.number == session.episodeNumber - 1 }
        if (prevEp != null) {
            playAnime(anime.id, prevEp.id, startFromZero = true)
        } else {
            emitMessage("Sudah di episode pertama")
        }
    }

    fun cancelAutoNext() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(autoNextCountdownSec = null, isPlaying = false))
        }
        playerTickerJob?.cancel()
    }

    fun minimizePlayer() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(isExpanded = false, isFullscreen = false))
        }
    }

    fun expandPlayer() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(isExpanded = true))
        }
    }

    fun closePlayer() {
        playerTickerJob?.cancel()
        _uiState.update { it.copy(playerSession = null) }
    }

    fun setPlayerQuality(quality: String) {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(selectedQuality = quality))
        }
        emitMessage("Kualitas diubah ke $quality")
    }

    fun setPlayerServer(serverId: String, serverName: String) {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(selectedServerId = serverId))
        }
        emitMessage("Beralih ke server $serverName")
    }

    fun setPlayerSubtitle(subtitleId: String, label: String) {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(selectedSubtitleId = subtitleId))
        }
        emitMessage("Subtitle: $label")
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(playbackSpeed = speed))
        }
        emitMessage("Kecepatan putar ${speed}x")
    }

    fun cycleAspectMode() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            val modes = VideoAspectMode.entries
            val nextMode = modes[(modes.indexOf(session.aspectMode) + 1) % modes.size]
            emitMessage("Rasio layar: ${nextMode.label}")
            state.copy(playerSession = session.copy(aspectMode = nextMode))
        }
    }

    fun togglePlayerLock() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(isLocked = !session.isLocked))
        }
    }

    fun setPlayerFullscreen(fullscreen: Boolean) {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(isFullscreen = fullscreen))
        }
    }

    fun toggleEpisodeLike() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            val nextLiked = !session.isEpisodeLiked
            emitMessage(if (nextLiked) "Episode disukai!" else "Batal menyukai episode")
            state.copy(playerSession = session.copy(isEpisodeLiked = nextLiked))
        }
    }

    fun getCurrentSubtitleText(session: PlayerSession): String? {
        if (session.selectedSubtitleId == "off") return null
        val anime = _uiState.value.allAnime.find { it.id == session.animeId } ?: return null
        val cues: List<SubtitleCue> = repository.getSubtitleCues(anime.title, session.episodeNumber)
        val cue = cues.find { session.positionSeconds in it.startSec..it.endSec } ?: cues.lastOrNull()
        return when (session.selectedSubtitleId) {
            "en" -> cue?.englishText
            else -> cue?.indoText
        }
    }

    private fun startPlayerTicker() {
        playerTickerJob?.cancel()
        playerTickerJob = viewModelScope.launch {
            while (true) {
                val delayMs = ((1000f / (_uiState.value.playerSession?.playbackSpeed ?: 1f))).toLong().coerceIn(350L, 2000L)
                delay(delayMs)
                val state = _uiState.value
                val session = state.playerSession ?: break
                if (!session.isPlaying) {
                    val countdown = session.autoNextCountdownSec
                    if (countdown != null) {
                        if (countdown <= 1) {
                            playNextEpisode()
                        } else {
                            _uiState.update { s ->
                                val cur = s.playerSession ?: return@update s
                                s.copy(playerSession = cur.copy(autoNextCountdownSec = countdown - 1))
                            }
                        }
                    }
                    continue
                }

                val anime = state.allAnime.find { it.id == session.animeId }
                val episode = anime?.episodes?.find { it.id == session.episodeId }

                var nextPos = session.positionSeconds + 1
                if (state.settings.autoSkipIntro && episode != null &&
                    nextPos in episode.introStartSec until episode.introEndSec
                ) {
                    nextPos = episode.introEndSec + 1
                    emitMessage("Intro dilewati otomatis")
                }

                if (nextPos >= session.durationSeconds) {
                    val hasNext = anime?.episodes?.any { it.number == session.episodeNumber + 1 } == true
                    if (state.settings.autoPlayNext && hasNext) {
                        _uiState.update { s ->
                            val cur = s.playerSession ?: return@update s
                            s.copy(
                                playerSession = cur.copy(
                                    positionSeconds = cur.durationSeconds,
                                    isPlaying = false,
                                    autoNextCountdownSec = 5
                                )
                            )
                        }
                    } else {
                        _uiState.update { s ->
                            val cur = s.playerSession ?: return@update s
                            s.copy(
                                playerSession = cur.copy(
                                    positionSeconds = cur.durationSeconds,
                                    isPlaying = false
                                )
                            )
                        }
                    }
                } else {
                    _uiState.update { s ->
                        val cur = s.playerSession ?: return@update s
                        val nextBuffer = (cur.bufferedSeconds + 2).coerceAtMost(cur.durationSeconds)
                        s.copy(
                            playerSession = cur.copy(
                                positionSeconds = nextPos,
                                bufferedSeconds = nextBuffer
                            )
                        )
                    }
                    if (nextPos % 4 == 0 && anime != null && episode != null) {
                        recordWatchProgress(anime, episode, nextPos)
                    }
                }
            }
        }
    }

    private fun syncCurrentProgressToHistory() {
        val state = _uiState.value
        val session = state.playerSession ?: return
        val anime = state.allAnime.find { it.id == session.animeId } ?: return
        val ep = anime.episodes.find { it.id == session.episodeId } ?: return
        recordWatchProgress(anime, ep, session.positionSeconds)
    }

    private fun recordWatchProgress(anime: Anime, episode: Episode, positionSeconds: Int) {
        _uiState.update { state ->
            val updatedItem = WatchProgress(
                animeId = anime.id,
                episodeId = episode.id,
                episodeNumber = episode.number,
                episodeTitle = episode.title,
                positionSeconds = positionSeconds,
                durationSeconds = episode.durationSeconds,
                updatedAtLabel = "Baru saja"
            )
            val filtered = state.watchHistory.filterNot { it.animeId == anime.id }
            state.copy(watchHistory = listOf(updatedItem) + filtered)
        }
    }

    // ====================
    // Explore & Filtering
    // ====================

    fun setExploreMode(mode: ExploreMode) {
        _uiState.update { it.copy(exploreMode = mode) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(exploreFilter = state.exploreFilter.copy(query = query))
        }
    }

    fun commitSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { state ->
            val updated = (listOf(trimmed) + state.searchHistory.filterNot {
                it.equals(trimmed, ignoreCase = true)
            }).take(8)
            state.copy(searchHistory = updated)
        }
        val cfg = _uiState.value.apiConfig
        if (cfg.useLiveApi && cfg.status == ApiConnectionStatus.CONNECTED) {
            remoteSearchJob?.cancel()
            remoteSearchJob = viewModelScope.launch {
                _uiState.update { it.copy(isSearchingRemote = true) }
                val res = remoteDataSource.searchAnime(cfg.baseUrl, trimmed)
                res.onSuccess { found ->
                    if (found.isNotEmpty()) {
                        _uiState.update { state ->
                            val merged = LinkedHashMap<String, Anime>()
                            found.forEach { merged[it.id] = it }
                            state.allAnime.forEach { existing ->
                                if (!merged.containsKey(existing.id)) merged[existing.id] = existing
                            }
                            state.copy(allAnime = merged.values.toList(), isSearchingRemote = false)
                        }
                    } else {
                        _uiState.update { it.copy(isSearchingRemote = false) }
                    }
                }.onFailure {
                    _uiState.update { it.copy(isSearchingRemote = false) }
                }
            }
        }
    }

    fun removeSearchHistoryItem(term: String) {
        _uiState.update { state ->
            state.copy(searchHistory = state.searchHistory.filterNot { it == term })
        }
    }

    fun clearSearchHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
    }

    fun toggleGenreFilter(genre: String) {
        _uiState.update { state ->
            val current = state.exploreFilter.selectedGenres
            val next = if (genre in current) current - genre else current + genre
            state.copy(exploreFilter = state.exploreFilter.copy(selectedGenres = next))
        }
    }

    fun setStatusFilter(status: AnimeStatus?) {
        _uiState.update { state ->
            state.copy(exploreFilter = state.exploreFilter.copy(statusFilter = status))
        }
    }

    fun setTypeFilter(type: AnimeType?) {
        _uiState.update { state ->
            state.copy(exploreFilter = state.exploreFilter.copy(typeFilter = type))
        }
    }

    fun setYearFilter(year: Int?) {
        _uiState.update { state ->
            state.copy(exploreFilter = state.exploreFilter.copy(yearFilter = year))
        }
    }

    fun setSortOption(sort: SortOption) {
        _uiState.update { state ->
            state.copy(exploreFilter = state.exploreFilter.copy(sortOption = sort))
        }
    }

    fun toggleExploreGridMode() {
        _uiState.update { state ->
            state.copy(exploreFilter = state.exploreFilter.copy(isGridView = !state.exploreFilter.isGridView))
        }
    }

    fun resetExploreFilters() {
        _uiState.update { state ->
            state.copy(
                exploreFilter = ExploreFilterState(isGridView = state.exploreFilter.isGridView)
            )
        }
    }

    fun selectScheduleDay(day: ReleaseDay) {
        _uiState.update { it.copy(selectedScheduleDay = day) }
    }

    fun toggleAnimeReminder(animeId: String) {
        val animeTitle = _uiState.value.allAnime.find { it.id == animeId }?.title ?: "Anime"
        _uiState.update { state ->
            val enabled = animeId !in state.reminders
            val next = if (enabled) state.reminders + animeId else state.reminders - animeId
            emitMessage(
                if (enabled) "Pengingat rilis diaktifkan untuk $animeTitle"
                else "Pengingat dinonaktifkan"
            )
            state.copy(reminders = next)
        }
    }

    // ====================
    // Library, Watchlist & History
    // ====================

    fun setLibraryTab(tab: LibraryTab) {
        _uiState.update { it.copy(libraryTab = tab) }
    }

    fun setWatchlistCategoryFilter(category: WatchlistCategory?) {
        _uiState.update { it.copy(selectedWatchlistFilter = category) }
    }

    fun setWatchlistCategory(animeId: String, category: WatchlistCategory?) {
        val title = _uiState.value.allAnime.find { it.id == animeId }?.title ?: "Anime"
        _uiState.update { state ->
            val nextMap = state.watchlist.toMutableMap()
            if (category == null) {
                nextMap.remove(animeId)
                emitMessage("$title dihapus dari Daftar Tonton")
            } else {
                nextMap[animeId] = category
                emitMessage("$title disimpan ke ${category.label}")
            }
            state.copy(watchlist = nextMap)
        }
    }

    fun toggleQuickBookmark(animeId: String) {
        val current = _uiState.value.watchlist[animeId]
        if (current == null) {
            setWatchlistCategory(animeId, WatchlistCategory.WATCHING)
        } else {
            setWatchlistCategory(animeId, null)
        }
    }

    fun removeHistoryItem(animeId: String) {
        _uiState.update { state ->
            state.copy(watchHistory = state.watchHistory.filterNot { it.animeId == animeId })
        }
        emitMessage("Dihapus dari riwayat tonton")
    }

    fun clearAllHistory() {
        _uiState.update { it.copy(watchHistory = emptyList()) }
        emitMessage("Seluruh riwayat tonton dibersihkan")
    }

    // ====================
    // Offline Downloads Simulation
    // ====================

    fun handleEpisodeDownloadAction(
        anime: Anime,
        episode: Episode,
        qualityLabel: String = _uiState.value.settings.defaultQuality
    ) {
        val existing = _uiState.value.downloads.find { it.episodeId == episode.id }
        when (existing?.status) {
            DownloadStatus.DOWNLOADING -> {
                pauseDownload(episode.id)
                emitMessage("Unduhan Ep ${episode.number} dijeda")
            }
            DownloadStatus.PAUSED -> {
                resumeDownload(episode.id)
                emitMessage("Melanjutkan unduhan Ep ${episode.number}")
            }
            DownloadStatus.COMPLETED -> {
                emitMessage("Episode ${episode.number} sudah tersimpan offline")
            }
            null -> startEpisodeDownload(anime, episode, qualityLabel)
        }
    }

    fun startEpisodeDownload(anime: Anime, episode: Episode, qualityLabel: String = _uiState.value.settings.defaultQuality) {
        val existing = _uiState.value.downloads.find { it.episodeId == episode.id }
        if (existing != null) {
            when (existing.status) {
                DownloadStatus.COMPLETED -> emitMessage("Episode ${episode.number} sudah tersimpan offline")
                DownloadStatus.DOWNLOADING -> emitMessage("Episode ${episode.number} sedang diunduh")
                DownloadStatus.PAUSED -> resumeDownload(episode.id)
            }
            return
        }

        val sizeMb = episode.qualities.find { it.label == qualityLabel }?.sizeEstimateMb ?: 195
        val task = DownloadTask(
            episodeId = episode.id,
            animeId = anime.id,
            animeTitle = anime.title,
            episodeNumber = episode.number,
            episodeTitle = episode.title,
            artTheme = anime.artTheme,
            qualityLabel = qualityLabel,
            totalMb = sizeMb,
            progress = 0.04f,
            status = DownloadStatus.DOWNLOADING
        )

        _uiState.update { state ->
            state.copy(downloads = listOf(task) + state.downloads)
        }
        emitMessage("Mengunduh ${anime.title.substringBefore(":")} Ep ${episode.number} ($qualityLabel)")
        launchDownloadProgress(episode.id)
    }

    fun pauseDownload(episodeId: String) {
        downloadJobs[episodeId]?.cancel()
        _uiState.update { state ->
            state.copy(
                downloads = state.downloads.map {
                    if (it.episodeId == episodeId && it.status == DownloadStatus.DOWNLOADING) {
                        it.copy(status = DownloadStatus.PAUSED)
                    } else it
                }
            )
        }
    }

    fun resumeDownload(episodeId: String) {
        _uiState.update { state ->
            state.copy(
                downloads = state.downloads.map {
                    if (it.episodeId == episodeId && it.status == DownloadStatus.PAUSED) {
                        it.copy(status = DownloadStatus.DOWNLOADING)
                    } else it
                }
            )
        }
        launchDownloadProgress(episodeId)
    }

    fun removeDownload(episodeId: String) {
        downloadJobs[episodeId]?.cancel()
        downloadJobs.remove(episodeId)
        _uiState.update { state ->
            state.copy(downloads = state.downloads.filterNot { it.episodeId == episodeId })
        }
        emitMessage("Unduhan dihapus dari penyimpanan")
    }

    private fun launchDownloadProgress(episodeId: String) {
        downloadJobs[episodeId]?.cancel()
        downloadJobs[episodeId] = viewModelScope.launch {
            while (true) {
                delay(280L)
                val task = _uiState.value.downloads.find { it.episodeId == episodeId } ?: break
                if (task.status != DownloadStatus.DOWNLOADING) break

                val nextProgress = (task.progress + 0.08f).coerceAtMost(1.0f)
                val completed = nextProgress >= 1.0f

                _uiState.update { state ->
                    state.copy(
                        downloads = state.downloads.map {
                            if (it.episodeId == episodeId) {
                                it.copy(
                                    progress = nextProgress,
                                    status = if (completed) DownloadStatus.COMPLETED else DownloadStatus.DOWNLOADING
                                )
                            } else it
                        }
                    )
                }

                if (completed) {
                    emitMessage("${task.animeTitle.substringBefore(":")} Ep ${task.episodeNumber} selesai diunduh!")
                    break
                }
            }
        }
    }

    // ====================
    // Ratings, Comments & Notifications
    // ====================

    fun rateAnime(animeId: String, score: Int) {
        _uiState.update { state ->
            state.copy(userRatings = state.userRatings + (animeId to score))
        }
        emitMessage("Rating $score/10 berhasil disimpan")
    }

    fun postComment(animeId: String, episodeNumber: Int?, text: String, isSpoiler: Boolean) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val profile = _uiState.value.userProfile
        val newComment = CommentItem(
            id = "user_${System.currentTimeMillis()}",
            animeId = animeId,
            episodeNumber = episodeNumber,
            author = profile.displayName,
            authorBadge = "PRO VIP",
            content = trimmed,
            timeAgo = "Baru saja",
            likes = 0,
            isLiked = false,
            isSpoiler = isSpoiler
        )
        _uiState.update { state ->
            state.copy(comments = listOf(newComment) + state.comments)
        }
        emitMessage("Komentar terkirim")
    }

    fun toggleCommentLike(commentId: String) {
        _uiState.update { state ->
            state.copy(
                comments = state.comments.map { comment ->
                    if (comment.id == commentId) {
                        val nextLiked = !comment.isLiked
                        comment.copy(
                            isLiked = nextLiked,
                            likes = if (nextLiked) comment.likes + 1 else (comment.likes - 1).coerceAtLeast(0)
                        )
                    } else comment
                }
            )
        }
    }

    fun markNotificationRead(notificationId: String) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map {
                    if (it.id == notificationId) it.copy(isRead = true) else it
                }
            )
        }
    }

    fun markAllNotificationsRead() {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
        }
    }

    // ====================
    // Profile & Settings
    // ====================

    fun updateProfile(displayName: String, bio: String, emblem: String) {
        val cleanName = displayName.trim().ifEmpty { "Anime Watcher" }
        _uiState.update { state ->
            state.copy(
                userProfile = state.userProfile.copy(
                    displayName = cleanName,
                    bio = bio.trim(),
                    avatarEmblem = emblem.take(2).uppercase().ifEmpty { "AW" }
                )
            )
        }
        emitMessage("Profil berhasil diperbarui")
    }

    fun updateSettings(transform: (PlaybackSettings) -> PlaybackSettings) {
        _uiState.update { state ->
            state.copy(settings = transform(state.settings))
        }
    }

    fun updateSubtitleSize(option: SubtitleSizeOption) {
        updateSettings { it.copy(subtitleSize = option) }
        emitMessage("Ukuran subtitle: ${option.label}")
    }

    fun clearAppCache() {
        _uiState.update { it.copy(cacheSizeMb = 0) }
        emitMessage("Cache aplikasi berhasil dibersihkan (184 MB dibebaskan)")
    }

    // ====================
    // Query Helpers
    // ====================

    fun getFilteredExploreAnime(): List<Anime> {
        val state = _uiState.value
        val filter = state.exploreFilter
        val q = filter.query.trim().lowercase()

        val filtered = state.allAnime.filter { anime ->
            val matchesQuery = q.isEmpty() ||
                anime.title.lowercase().contains(q) ||
                anime.japaneseTitle.lowercase().contains(q) ||
                anime.studio.lowercase().contains(q) ||
                anime.genres.any { it.lowercase().contains(q) }

            val matchesGenres = filter.selectedGenres.isEmpty() ||
                filter.selectedGenres.all { g -> anime.genres.any { it.equals(g, ignoreCase = true) } }

            val matchesStatus = filter.statusFilter == null || anime.status == filter.statusFilter
            val matchesType = filter.typeFilter == null || anime.type == filter.typeFilter
            val matchesYear = filter.yearFilter == null || anime.year == filter.yearFilter

            matchesQuery && matchesGenres && matchesStatus && matchesType && matchesYear
        }

        return when (filter.sortOption) {
            SortOption.POPULAR -> filtered.sortedBy { it.popularityRank }
            SortOption.TOP_RATING -> filtered.sortedByDescending { it.rating }
            SortOption.LATEST -> filtered.sortedWith(
                compareByDescending<Anime> { it.isNewRelease }
                    .thenByDescending { it.year }
                    .thenBy { it.popularityRank }
            )
            SortOption.TITLE_AZ -> filtered.sortedBy { it.title }
        }
    }

    fun getCatalogAnime(preset: CatalogPreset, genre: String?): List<Anime> {
        val list = _uiState.value.allAnime
        val byPreset = when (preset) {
            CatalogPreset.ALL -> list
            CatalogPreset.ONGOING -> list.filter { it.status == AnimeStatus.ONGOING }
            CatalogPreset.TRENDING -> list.filter { it.isTrending }.sortedBy { it.popularityRank }
            CatalogPreset.TOP_RATED -> list.sortedByDescending { it.rating }.take(10)
            CatalogPreset.LATEST -> list.filter { it.isNewRelease || it.status == AnimeStatus.ONGOING }
            CatalogPreset.COMPLETED -> list.filter { it.status == AnimeStatus.COMPLETED }
            CatalogPreset.MOVIES -> list.filter { it.type == AnimeType.MOVIE || it.type == AnimeType.OVA }
        }
        return if (genre != null) {
            byPreset.filter { a -> a.genres.any { it.equals(genre, ignoreCase = true) } }
        } else byPreset
    }
}
