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
import com.gows.animwatch.data.local.AppPermissionStatus
import com.gows.animwatch.data.local.DeviceStorageInfo
import com.gows.animwatch.data.local.InMemorySessionStore
import com.gows.animwatch.data.local.NoOpOfflineStorageGateway
import com.gows.animwatch.data.local.OfflineStorageGateway
import com.gows.animwatch.data.local.SessionStore
import com.gows.animwatch.data.local.formatStorageBytes
import com.gows.animwatch.data.remote.AuthResponseDto
import com.gows.animwatch.data.remote.HistoryUpsertRequestDto
import com.gows.animwatch.data.remote.KumanimeApiClient
import com.gows.animwatch.data.remote.KumanimeRemoteDataSource
import com.gows.animwatch.data.remote.UpdatePreferencesRequestDto
import com.gows.animwatch.data.repository.AnimeRepository
import com.gows.animwatch.data.repository.MockAnimeRepository
import com.gows.animwatch.ui.theme.AppAccentTheme
import com.gows.animwatch.ui.theme.AppAppearanceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
    val streamType: String = "official_trailer",
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
    val watchedEpisodes: Map<String, Int> = emptyMap(),
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
    val cacheSizeMb: Int = 0,
    val deviceStorage: DeviceStorageInfo = DeviceStorageInfo(),
    val appPermissions: List<AppPermissionStatus> = emptyList(),
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

    private var sessionStore: SessionStore = InMemorySessionStore()
    private var storageGateway: OfflineStorageGateway = NoOpOfflineStorageGateway()
    private var storageAttached: Boolean = false
    private var cloudSyncEnabled: Boolean = false
    private var authToken: String? = null
    private var authJob: Job? = null
    private var commentsFetchJob: Job? = null
    private var lastHistorySyncAnimeId: String? = null
    private var lastHistorySyncEpisodeId: String? = null
    private var lastHistorySyncAtMs: Long = 0L

    private var playerTickerJob: Job? = null
    private var apiSyncJob: Job? = null
    private var detailFetchJob: Job? = null
    private var streamFetchJob: Job? = null
    private var remoteSearchJob: Job? = null
    private val downloadJobs = mutableMapOf<String, Job>()

    fun attachStorageGateway(gateway: OfflineStorageGateway) {
        if (storageAttached) return
        storageGateway = gateway
        storageAttached = true
        viewModelScope.launch(Dispatchers.IO) {
            val storage = gateway.inspectStorage()
            val perms = gateway.readPermissions()
            val persistedDownloads = gateway.loadPersistedDownloads()
            _uiState.update { state ->
                state.copy(
                    downloads = persistedDownloads,
                    deviceStorage = storage,
                    appPermissions = perms,
                    cacheSizeMb = (storage.appCacheBytes / (1024L * 1024L)).toInt()
                )
            }
        }
    }

    fun refreshDeviceStorageAndPermissions() {
        if (!storageAttached) return
        viewModelScope.launch(Dispatchers.IO) {
            val storage = storageGateway.inspectStorage()
            val perms = storageGateway.readPermissions()
            _uiState.update { state ->
                state.copy(
                    deviceStorage = storage,
                    appPermissions = perms,
                    cacheSizeMb = (storage.appCacheBytes / (1024L * 1024L)).toInt()
                )
            }
        }
    }

    fun attachSessionStore(store: SessionStore) {
        if (cloudSyncEnabled) return
        sessionStore = store
        cloudSyncEnabled = true

        // Restore saved theme & API URL immediately
        val savedUrl = store.getApiBaseUrl()
        val savedMode = store.getAppearanceMode()?.let { name ->
            AppAppearanceMode.entries.find { it.name == name }
        }
        val savedAccent = store.getAccentTheme()?.let { name ->
            AppAccentTheme.entries.find { it.name == name }
        }
        _uiState.update { state ->
            state.copy(
                appearanceMode = savedMode ?: state.appearanceMode,
                accentTheme = savedAccent ?: state.accentTheme,
                apiConfig = if (savedUrl != null) {
                    state.apiConfig.copy(baseUrl = KumanimeApiClient.normalizeBaseUrl(savedUrl))
                } else state.apiConfig
            )
        }

        val savedToken = store.getAuthToken() ?: return
        authToken = savedToken
        val baseUrl = _uiState.value.apiConfig.baseUrl
        authJob?.cancel()
        authJob = viewModelScope.launch {
            val res = remoteDataSource.fetchCurrentUser(baseUrl, savedToken)
            res.onSuccess { snapshot ->
                applyCloudAuthSnapshot(snapshot, savedToken, showWelcomeToast = false)
            }.onFailure {
                authToken = null
                sessionStore.saveAuthToken(null)
            }
        }
    }

    private fun applyCloudAuthSnapshot(
        dto: AuthResponseDto,
        tokenOverride: String? = null,
        showWelcomeToast: Boolean = true
    ) {
        val token = dto.token?.takeIf { it.isNotBlank() } ?: tokenOverride
        if (!token.isNullOrBlank()) {
            authToken = token
            sessionStore.saveAuthToken(token)
        }

        val u = dto.user ?: return
        val mode = AppAppearanceMode.entries.find { it.name.equals(u.appearanceMode, ignoreCase = true) }
            ?: _uiState.value.appearanceMode
        val accent = AppAccentTheme.entries.find { it.name.equals(u.accentTheme, ignoreCase = true) }
            ?: _uiState.value.accentTheme
        sessionStore.saveAppearanceMode(mode.name)
        sessionStore.saveAccentTheme(accent.name)

        val genres = u.preferredGenres?.filter { it.isNotBlank() }?.toSet()
            ?.ifEmpty { _uiState.value.preferredGenres }
            ?: _uiState.value.preferredGenres

        val rawSettings = u.settings.orEmpty()
        val currentSettings = _uiState.value.settings
        val restoredSubSize = (rawSettings["subtitleSize"] as? String)?.let { label ->
            SubtitleSizeOption.entries.find { it.name.equals(label, true) || it.label.equals(label, true) }
        } ?: currentSettings.subtitleSize

        val mergedSettings = currentSettings.copy(
            defaultQuality = (rawSettings["defaultQuality"] as? String)?.ifBlank { null } ?: currentSettings.defaultQuality,
            defaultServer = (rawSettings["defaultServer"] as? String)?.ifBlank { null } ?: currentSettings.defaultServer,
            defaultSubtitle = (rawSettings["defaultSubtitle"] as? String)?.ifBlank { null } ?: currentSettings.defaultSubtitle,
            subtitleSize = restoredSubSize,
            autoPlayNext = (rawSettings["autoPlayNext"] as? Boolean) ?: currentSettings.autoPlayNext,
            autoSkipIntro = (rawSettings["autoSkipIntro"] as? Boolean) ?: currentSettings.autoSkipIntro,
            wifiOnlyDownloads = (rawSettings["wifiOnlyDownloads"] as? Boolean) ?: currentSettings.wifiOnlyDownloads,
            episodeNotifications = (rawSettings["episodeNotifications"] as? Boolean) ?: currentSettings.episodeNotifications
        )
        val restoredGridPref = (rawSettings["isGridCatalogPreferred"] as? Boolean) ?: _uiState.value.exploreFilter.isGridView

        val restoredWatchlist = dto.watchlist.orEmpty().mapNotNull { (animeId, catName) ->
            val cat = WatchlistCategory.entries.find { it.name.equals(catName, ignoreCase = true) }
            if (cat != null) animeId to cat else null
        }.toMap()

        val restoredHistory = dto.history.orEmpty().mapNotNull { h ->
            val animeId = h.animeId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val epId = h.episodeId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            WatchProgress(
                animeId = animeId,
                episodeId = epId,
                episodeNumber = (h.episodeNumber ?: 1).coerceAtLeast(1),
                episodeTitle = h.episodeTitle?.ifBlank { null } ?: "Episode ${h.episodeNumber ?: 1}",
                positionSeconds = (h.positionSeconds ?: 0).coerceAtLeast(0),
                durationSeconds = (h.durationSeconds ?: 1440).coerceAtLeast(1),
                updatedAtLabel = h.updatedAtLabel ?: "Tersimpan"
            )
        }

        val restoredRatings = dto.ratings.orEmpty()
        val restoredWatchedEpisodes = dto.watchedEpisodes.orEmpty()
        val completedSetup = u.hasCompletedThemeSetup ?: false

        _uiState.update { state ->
            state.copy(
                isAuthenticated = true,
                isGuestSession = false,
                hasCompletedThemeSetup = completedSetup,
                isEditingTheme = false,
                appearanceMode = mode,
                accentTheme = accent,
                preferredGenres = genres,
                authError = null,
                currentTab = MainTab.HOME,
                backStack = listOf(AppRoute.TabsRoot),
                watchlist = restoredWatchlist,
                watchedEpisodes = restoredWatchedEpisodes,
                watchHistory = restoredHistory,
                userRatings = restoredRatings,
                settings = mergedSettings,
                exploreFilter = state.exploreFilter.copy(isGridView = restoredGridPref),
                userProfile = state.userProfile.copy(
                    displayName = u.displayName?.ifBlank { null } ?: state.userProfile.displayName,
                    handle = u.handle?.ifBlank { null } ?: state.userProfile.handle,
                    bio = u.bio ?: state.userProfile.bio,
                    avatarEmblem = u.avatarEmblem?.ifBlank { null } ?: state.userProfile.avatarEmblem,
                    memberTier = u.memberTier?.ifBlank { null } ?: state.userProfile.memberTier,
                    favoriteGenre = genres.firstOrNull() ?: state.userProfile.favoriteGenre
                )
            )
        }

        if (showWelcomeToast) {
            val name = u.displayName ?: "Member"
            emitMessage(
                if (completedSetup) "Selamat datang kembali, $name!"
                else "Berhasil masuk sebagai $name. Pilih temamu!"
            )
        }
    }

    private fun pushPreferencesToCloud() {
        val token = authToken ?: return
        val state = _uiState.value
        if (!cloudSyncEnabled || state.isGuestSession) return
        val baseUrl = state.apiConfig.baseUrl
        val s = state.settings
        val req = UpdatePreferencesRequestDto(
            appearanceMode = state.appearanceMode.name,
            accentTheme = state.accentTheme.name,
            preferredGenres = state.preferredGenres.toList(),
            hasCompletedThemeSetup = state.hasCompletedThemeSetup,
            settings = mapOf(
                "defaultQuality" to s.defaultQuality,
                "defaultServer" to s.defaultServer,
                "defaultSubtitle" to s.defaultSubtitle,
                "subtitleSize" to s.subtitleSize.name,
                "autoPlayNext" to s.autoPlayNext,
                "autoSkipIntro" to s.autoSkipIntro,
                "wifiOnlyDownloads" to s.wifiOnlyDownloads,
                "episodeNotifications" to s.episodeNotifications,
                "isGridCatalogPreferred" to state.exploreFilter.isGridView
            )
        )
        viewModelScope.launch {
            remoteDataSource.updateUserPreferences(baseUrl, token, req)
        }
    }

    private fun loadInitialState(): AnimWatchUiState {
        return AnimWatchUiState(
            allAnime = repository.getAllAnime(),
            genres = repository.getAllGenres(),
            watchlist = repository.getInitialWatchlist(),
            watchHistory = repository.getInitialWatchHistory(),
            downloads = emptyList(),
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
        if (cloudSyncEnabled) {
            val baseUrl = _uiState.value.apiConfig.baseUrl
            _uiState.update { it.copy(authError = null) }
            authJob?.cancel()
            authJob = viewModelScope.launch {
                val res = remoteDataSource.loginUser(baseUrl, key, password)
                res.onSuccess { snapshot ->
                    applyCloudAuthSnapshot(snapshot, showWelcomeToast = true)
                    if (!rememberMe) {
                        sessionStore.saveAuthToken(null)
                    }
                }.onFailure { err ->
                    _uiState.update { it.copy(authError = err.message ?: "Gagal masuk ke akun") }
                }
            }
            return
        }

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
        if (cloudSyncEnabled) {
            val baseUrl = _uiState.value.apiConfig.baseUrl
            _uiState.update { it.copy(authError = null) }
            authJob?.cancel()
            authJob = viewModelScope.launch {
                val res = remoteDataSource.registerUser(baseUrl, cleanName, cleanEmail, password)
                res.onSuccess { snapshot ->
                    applyCloudAuthSnapshot(snapshot, showWelcomeToast = false)
                    emitMessage("Akun $cleanName tersimpan! Pilih tema kesukaanmu.")
                }.onFailure { err ->
                    _uiState.update { it.copy(authError = err.message ?: "Gagal mendaftarkan akun") }
                }
            }
            return
        }

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
                    bio = "Anggota baru AnimWatch."
                )
            )
        }
        emitMessage("Akun ${account.displayName} dibuat! Pilih tema kesukaanmu.")
    }

    fun loginDemoVip() {
        if (cloudSyncEnabled) {
            login("adrian@animwatch.id", "animwatch123", rememberMe = true)
            return
        }
        val demo = registeredAccounts["adrian@animwatch.id"] ?: return
        login(demo.email, demo.password, rememberMe = true)
    }

    fun continueAsGuest() {
        authToken = null
        sessionStore.saveAuthToken(null)
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
                    handle = "@guest",
                    avatarEmblem = "TM",
                    memberTier = "Mode Tamu",
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
        val currentToken = authToken
        val baseUrl = _uiState.value.apiConfig.baseUrl
        authToken = null
        sessionStore.saveAuthToken(null)
        if (cloudSyncEnabled && !currentToken.isNullOrBlank()) {
            viewModelScope.launch {
                remoteDataSource.logoutUser(baseUrl, currentToken)
            }
        }
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
        sessionStore.saveAppearanceMode(mode.name)
        _uiState.update { it.copy(appearanceMode = mode) }
    }

    fun setAccentTheme(accent: AppAccentTheme) {
        sessionStore.saveAccentTheme(accent.name)
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
        pushPreferencesToCloud()
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
        sessionStore.saveApiBaseUrl(normalized)
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
                val mockIds = repository.getAllAnime().map { it.id }.toSet()
                val remoteIdSet = remoteItems.map { it.id }.toSet()
                val dynamicGenres = remoteItems
                    .flatMap { it.genres }
                    .distinct()
                    .ifEmpty { repository.getAllGenres() }

                _uiState.update { state ->
                    val preservedSavedRemote = state.allAnime.filter { existing ->
                        existing.isRemote && existing.id !in remoteIdSet &&
                            (existing.id in state.watchlist || state.watchHistory.any { h -> h.animeId == existing.id })
                    }
                    val finalCatalog = remoteItems + preservedSavedRemote
                    state.copy(
                        allAnime = finalCatalog,
                        genres = dynamicGenres,
                        watchlist = state.watchlist.filterKeys { it !in mockIds },
                        watchedEpisodes = state.watchedEpisodes.filterKeys { it !in mockIds },
                        watchHistory = state.watchHistory.filterNot { it.animeId in mockIds },
                        apiConfig = state.apiConfig.copy(
                            status = ApiConnectionStatus.CONNECTED,
                            lastSyncMessage = "Terhubung (${remoteItems.size} judul AniList & Resmi Gratis)",
                            remoteCount = remoteItems.size
                        )
                    )
                }
                if (!silentIfOffline) {
                    emitMessage("Katalog AniList & Gratis Resmi dimuat (${remoteItems.size} judul)")
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
        val isYtFreeReady = anime?.slug?.startsWith("ytfree-") == true && anime.episodes.isNotEmpty()
        if (anime != null && anime.isRemote && !isYtFreeReady &&
            (anime.episodes.isEmpty() || anime.isEpisodeEntry || (anime.characters.isEmpty() && anime.episodes.size <= 1))
        ) {
            fetchRemoteDetailFor(anime)
        }
        if (cloudSyncEnabled) {
            fetchRemoteCommentsFor(animeId)
        }
    }

    private fun fetchRemoteCommentsFor(animeId: String) {
        val baseUrl = _uiState.value.apiConfig.baseUrl
        val token = authToken
        commentsFetchJob?.cancel()
        commentsFetchJob = viewModelScope.launch {
            remoteDataSource.fetchComments(baseUrl, animeId, token).onSuccess { remoteComments ->
                _uiState.update { state ->
                    val otherComments = state.comments.filterNot { it.animeId == animeId }
                    state.copy(comments = remoteComments + otherComments)
                }
            }
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

        // Jika anime dari AniList belum memuat episode penuh Muse/Ani-One, ambil detailnya terlebih dahulu
        val needsOfficialEpisodeResolve = anime.isRemote && (
            anime.episodes.isEmpty() ||
                (anime.episodes.size == 1 && anime.episodes.first().streamType != "official_episode" && anime.characters.isEmpty())
            )
        if (needsOfficialEpisodeResolve) {
            val baseUrl = state.apiConfig.baseUrl
            emitMessage("Menghubungkan ke episode resmi...")
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
                    emitMessage("Gagal mengambil daftar episode dari server")
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
            streamType = targetEpisode.streamType,
            isLoadingStream = needsRemoteStream,
            useWebStreamView = true
        )

        _uiState.update { it.copy(playerSession = session) }
        recordWatchProgress(anime, targetEpisode, resumePos)
        syncPlayerTickerForSession(session)

        if (needsRemoteStream) {
            fetchRemoteEpisodeStream(anime, targetEpisode)
        }
    }

    private fun syncPlayerTickerForSession(session: PlayerSession?) {
        val showingLiveWebStream = session != null && session.useWebStreamView && session.streamUrl.isNotBlank()
        if (session != null && session.isPlaying && !showingLiveWebStream && !session.isLoadingStream) {
            startPlayerTicker()
        } else {
            playerTickerJob?.cancel()
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
                            streamType = enrichedEp.streamType,
                            isLoadingStream = false
                        )
                    } else curSession
                    cur.copy(allAnime = updatedAnimeList, playerSession = updatedSession)
                }
                syncPlayerTickerForSession(_uiState.value.playerSession)
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
                syncPlayerTickerForSession(_uiState.value.playerSession)
                emitMessage("Gagal memuat link video dari server")
            }
        }
    }

    fun togglePlayerWebStreamView() {
        _uiState.update { state ->
            val session = state.playerSession ?: return@update state
            state.copy(playerSession = session.copy(useWebStreamView = !session.useWebStreamView))
        }
        syncPlayerTickerForSession(_uiState.value.playerSession)
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
        syncPlayerTickerForSession(_uiState.value.playerSession)
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

        val token = authToken
        val state = _uiState.value
        val now = System.currentTimeMillis()
        val shouldSync = lastHistorySyncAnimeId != anime.id ||
            lastHistorySyncEpisodeId != episode.id ||
            (now - lastHistorySyncAtMs) >= 10_000L
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession && shouldSync) {
            lastHistorySyncAnimeId = anime.id
            lastHistorySyncEpisodeId = episode.id
            lastHistorySyncAtMs = now
            val baseUrl = state.apiConfig.baseUrl
            viewModelScope.launch {
                remoteDataSource.syncHistoryItem(
                    baseUrl = baseUrl,
                    token = token,
                    request = HistoryUpsertRequestDto(
                        animeId = anime.id,
                        episodeId = episode.id,
                        episodeNumber = episode.number,
                        episodeTitle = episode.title,
                        positionSeconds = positionSeconds,
                        durationSeconds = episode.durationSeconds
                    )
                )
            }
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
        val trimmed = query.trim()
        remoteSearchJob?.cancel()
        if (trimmed.length < 2) {
            _uiState.update { it.copy(isSearchingRemote = false) }
            return
        }
        val cfg = _uiState.value.apiConfig
        if (cfg.useLiveApi) {
            remoteSearchJob = viewModelScope.launch {
                delay(420L)
                executeRemoteSearch(cfg.baseUrl, trimmed)
            }
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
        if (cfg.useLiveApi) {
            remoteSearchJob?.cancel()
            remoteSearchJob = viewModelScope.launch {
                executeRemoteSearch(cfg.baseUrl, trimmed)
            }
        }
    }

    private suspend fun executeRemoteSearch(baseUrl: String, query: String) {
        _uiState.update { it.copy(isSearchingRemote = true) }
        val res = remoteDataSource.searchAnime(baseUrl, query)
        res.onSuccess { found ->
            if (found.isNotEmpty()) {
                _uiState.update { state ->
                    val merged = LinkedHashMap<String, Anime>()
                    state.allAnime.forEach { merged[it.id] = it }
                    found.forEach { item ->
                        if (!merged.containsKey(item.id)) {
                            merged[item.id] = item
                        }
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
        val anime = _uiState.value.allAnime.find { it.id == animeId }
        val title = anime?.title ?: "Anime"
        var syncedWatched: Int? = null
        _uiState.update { state ->
            val nextMap = state.watchlist.toMutableMap()
            val nextWatched = state.watchedEpisodes.toMutableMap()
            if (category == null) {
                nextMap.remove(animeId)
                nextWatched.remove(animeId)
                emitMessage("$title dihapus dari Daftar Tonton")
            } else {
                nextMap[animeId] = category
                if (category == WatchlistCategory.COMPLETED && anime != null) {
                    val maxEps = maxOf(anime.totalEpisodes, anime.releasedEpisodes, anime.episodes.size, 1)
                    if ((nextWatched[animeId] ?: 0) < maxEps) {
                        nextWatched[animeId] = maxEps
                        syncedWatched = maxEps
                    }
                }
                emitMessage("$title disimpan ke ${category.label}")
            }
            state.copy(watchlist = nextMap, watchedEpisodes = nextWatched)
        }
        val token = authToken
        val state = _uiState.value
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession) {
            viewModelScope.launch {
                remoteDataSource.syncWatchlist(
                    baseUrl = state.apiConfig.baseUrl,
                    token = token,
                    animeId = animeId,
                    category = category?.name,
                    watchedEpisodes = syncedWatched
                )
            }
        }
    }

    fun updateWatchedEpisodes(animeId: String, count: Int) {
        val anime = _uiState.value.allAnime.find { it.id == animeId } ?: return
        val maxEps = maxOf(anime.totalEpisodes, anime.releasedEpisodes, anime.episodes.size, 1)
        val clamped = count.coerceIn(0, maxEps)
        val currentCat = _uiState.value.watchlist[animeId]
        val targetCat = when {
            clamped >= maxEps -> WatchlistCategory.COMPLETED
            clamped > 0 && (currentCat == null || currentCat == WatchlistCategory.PLAN_TO_WATCH || currentCat == WatchlistCategory.COMPLETED) ->
                WatchlistCategory.WATCHING
            else -> currentCat ?: WatchlistCategory.WATCHING
        }
        _uiState.update { state ->
            state.copy(
                watchedEpisodes = state.watchedEpisodes + (animeId to clamped),
                watchlist = state.watchlist + (animeId to targetCat)
            )
        }
        val token = authToken
        val state = _uiState.value
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession) {
            viewModelScope.launch {
                remoteDataSource.syncWatchlist(
                    baseUrl = state.apiConfig.baseUrl,
                    token = token,
                    animeId = animeId,
                    category = targetCat.name,
                    watchedEpisodes = clamped
                )
            }
        }
        emitMessage(
            if (clamped >= maxEps) "Progres ${anime.title.substringBefore(":")} selesai ($clamped/$maxEps Ep)!"
            else "Progres tracker: $clamped/$maxEps Episode"
        )
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
        val token = authToken
        val state = _uiState.value
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession) {
            viewModelScope.launch {
                remoteDataSource.deleteHistoryItem(state.apiConfig.baseUrl, token, animeId)
            }
        }
        emitMessage("Dihapus dari riwayat tonton")
    }

    fun clearAllHistory() {
        _uiState.update { it.copy(watchHistory = emptyList()) }
        val token = authToken
        val state = _uiState.value
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession) {
            viewModelScope.launch {
                remoteDataSource.clearHistory(state.apiConfig.baseUrl, token)
            }
        }
        emitMessage("Seluruh riwayat tonton dibersihkan")
    }

    // ====================
    // Real Device Offline Episode Packages
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
                emitMessage("Episode ${episode.number} sudah tersimpan di penyimpanan perangkat")
            }
            null -> startEpisodeDownload(anime, episode, qualityLabel)
        }
    }

    fun startEpisodeDownload(anime: Anime, episode: Episode, qualityLabel: String = _uiState.value.settings.defaultQuality) {
        val existing = _uiState.value.downloads.find { it.episodeId == episode.id }
        if (existing != null) {
            when (existing.status) {
                DownloadStatus.COMPLETED -> emitMessage("Episode ${episode.number} sudah tersimpan di perangkat")
                DownloadStatus.DOWNLOADING -> emitMessage("Episode ${episode.number} sedang disimpan")
                DownloadStatus.PAUSED -> resumeDownload(episode.id)
            }
            return
        }

        val task = DownloadTask(
            episodeId = episode.id,
            animeId = anime.id,
            animeTitle = anime.title,
            episodeNumber = episode.number,
            episodeTitle = episode.title,
            artTheme = anime.artTheme,
            qualityLabel = qualityLabel,
            totalMb = 1,
            progress = 0.05f,
            status = DownloadStatus.DOWNLOADING,
            sizeBytes = 0L,
            posterUrl = anime.posterUrl
        )

        _uiState.update { state ->
            state.copy(downloads = listOf(task) + state.downloads)
        }
        emitMessage("Menyimpan paket offline ${anime.title.substringBefore(":")} Ep ${episode.number}...")
        launchRealOfflineDownload(anime, episode, qualityLabel)
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
        val task = _uiState.value.downloads.find { it.episodeId == episodeId } ?: return
        val anime = _uiState.value.allAnime.find { it.id == task.animeId }
        val ep = anime?.episodes?.find { it.id == episodeId }
        _uiState.update { state ->
            state.copy(
                downloads = state.downloads.map {
                    if (it.episodeId == episodeId && it.status == DownloadStatus.PAUSED) {
                        it.copy(status = DownloadStatus.DOWNLOADING)
                    } else it
                }
            )
        }
        if (anime != null && ep != null) {
            launchRealOfflineDownload(anime, ep, task.qualityLabel)
        }
    }

    fun removeDownload(episodeId: String) {
        downloadJobs[episodeId]?.cancel()
        downloadJobs.remove(episodeId)
        _uiState.update { state ->
            state.copy(downloads = state.downloads.filterNot { it.episodeId == episodeId })
        }
        viewModelScope.launch(Dispatchers.IO) {
            storageGateway.deleteEpisodePackage(episodeId)
            val refreshed = storageGateway.inspectStorage()
            _uiState.update { it.copy(deviceStorage = refreshed) }
        }
        emitMessage("Paket offline dihapus dari penyimpanan perangkat")
    }

    private fun launchRealOfflineDownload(anime: Anime, episode: Episode, qualityLabel: String) {
        downloadJobs[episode.id]?.cancel()
        downloadJobs[episode.id] = viewModelScope.launch {
            val cues = repository.getSubtitleCues(anime.title, episode.number)
            val result = storageGateway.downloadEpisodePackage(
                anime = anime,
                episode = episode,
                qualityLabel = qualityLabel,
                subtitleCues = cues
            ) { progress, bytesWritten ->
                _uiState.update { state ->
                    state.copy(
                        downloads = state.downloads.map { item ->
                            if (item.episodeId == episode.id) {
                                item.copy(
                                    progress = progress.coerceIn(0.05f, 0.99f),
                                    sizeBytes = bytesWritten,
                                    totalMb = (bytesWritten / (1024L * 1024L)).toInt().coerceAtLeast(1)
                                )
                            } else item
                        }
                    )
                }
            }

            result.onSuccess { saved ->
                val refreshed = withContext(Dispatchers.IO) { storageGateway.inspectStorage() }
                _uiState.update { state ->
                    state.copy(
                        deviceStorage = refreshed,
                        downloads = state.downloads.map { item ->
                            if (item.episodeId == episode.id) {
                                item.copy(
                                    progress = 1.0f,
                                    status = DownloadStatus.COMPLETED,
                                    sizeBytes = saved.bytesWritten,
                                    totalMb = (saved.bytesWritten / (1024L * 1024L)).toInt().coerceAtLeast(1),
                                    localPath = saved.localPath
                                )
                            } else item
                        }
                    )
                }
                emitMessage(
                    "${anime.title.substringBefore(":")} Ep ${episode.number} tersimpan (${formatStorageBytes(saved.bytesWritten)})"
                )
            }.onFailure { err ->
                _uiState.update { state ->
                    state.copy(
                        downloads = state.downloads.map { item ->
                            if (item.episodeId == episode.id) item.copy(status = DownloadStatus.PAUSED) else item
                        }
                    )
                }
                emitMessage("Gagal menyimpan paket offline: ${err.localizedMessage ?: "Periksa koneksi"}")
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
        val token = authToken
        val state = _uiState.value
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession) {
            viewModelScope.launch {
                remoteDataSource.syncRating(state.apiConfig.baseUrl, token, animeId, score)
            }
        }
        emitMessage("Rating $score/10 berhasil disimpan")
    }

    fun postComment(animeId: String, episodeNumber: Int?, text: String, isSpoiler: Boolean) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val token = authToken
        val state = _uiState.value
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession) {
            viewModelScope.launch {
                remoteDataSource.postComment(
                    baseUrl = state.apiConfig.baseUrl,
                    token = token,
                    animeId = animeId,
                    episodeNumber = episodeNumber,
                    content = trimmed,
                    isSpoiler = isSpoiler
                ).onSuccess { savedComment ->
                    _uiState.update { cur ->
                        cur.copy(comments = listOf(savedComment) + cur.comments.filterNot { it.id == savedComment.id })
                    }
                    emitMessage("Komentar terkirim")
                }.onFailure { err ->
                    emitMessage(err.message ?: "Gagal mengirim komentar")
                }
            }
            return
        }

        val profile = state.userProfile
        val newComment = CommentItem(
            id = "user_${System.currentTimeMillis()}",
            animeId = animeId,
            episodeNumber = episodeNumber,
            author = profile.displayName,
            authorBadge = profile.memberTier,
            content = trimmed,
            timeAgo = "Baru saja",
            likes = 0,
            isLiked = false,
            isSpoiler = isSpoiler
        )
        _uiState.update { cur ->
            cur.copy(comments = listOf(newComment) + cur.comments)
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
        val token = authToken
        val state = _uiState.value
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession && commentId.startsWith("db_cmt_")) {
            viewModelScope.launch {
                remoteDataSource.toggleCommentLike(state.apiConfig.baseUrl, token, commentId)
                    .onSuccess { (liked, count) ->
                        _uiState.update { cur ->
                            cur.copy(
                                comments = cur.comments.map { c ->
                                    if (c.id == commentId) c.copy(isLiked = liked, likes = count) else c
                                }
                            )
                        }
                    }
            }
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
        val cleanBio = bio.trim()
        val cleanEmblem = emblem.take(2).uppercase().ifEmpty { "AW" }
        _uiState.update { state ->
            state.copy(
                userProfile = state.userProfile.copy(
                    displayName = cleanName,
                    bio = cleanBio,
                    avatarEmblem = cleanEmblem
                )
            )
        }
        val token = authToken
        val state = _uiState.value
        if (cloudSyncEnabled && !token.isNullOrBlank() && !state.isGuestSession) {
            viewModelScope.launch {
                remoteDataSource.updateUserProfile(
                    baseUrl = state.apiConfig.baseUrl,
                    token = token,
                    displayName = cleanName,
                    bio = cleanBio,
                    avatarEmblem = cleanEmblem
                )
            }
        }
        emitMessage("Profil berhasil diperbarui")
    }

    fun updateSettings(transform: (PlaybackSettings) -> PlaybackSettings) {
        _uiState.update { state ->
            state.copy(settings = transform(state.settings))
        }
        pushPreferencesToCloud()
    }

    fun updateSubtitleSize(option: SubtitleSizeOption) {
        updateSettings { it.copy(subtitleSize = option) }
        emitMessage("Ukuran subtitle: ${option.label}")
    }

    fun clearAppCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val freedBytes = storageGateway.clearAppCache()
            val refreshed = storageGateway.inspectStorage()
            _uiState.update {
                it.copy(
                    deviceStorage = refreshed,
                    cacheSizeMb = (refreshed.appCacheBytes / (1024L * 1024L)).toInt()
                )
            }
            emitMessage("Cache dibersihkan (${formatStorageBytes(freedBytes)} dibebaskan)")
        }
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
            CatalogPreset.OFFICIAL_FREE -> list.filter { it.isOfficialFree }
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
