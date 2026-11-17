package com.gows.animwatch.ui.state

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
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
import com.gows.animwatch.data.repository.AnimeRepository
import com.gows.animwatch.data.repository.MockAnimeRepository
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
    val isEpisodeLiked: Boolean = false
)

@Immutable
data class AnimWatchUiState(
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
        memberTier = "GOLD ROYAL PASS",
        watchedHours = 0,
        completedAnimeCount = 0,
        favoriteGenre = "Action"
    ),
    val settings: PlaybackSettings = PlaybackSettings(),
    val playerSession: PlayerSession? = null,
    val cacheSizeMb: Int = 184
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
    private val repository: AnimeRepository = MockAnimeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<AnimWatchUiState> = _uiState.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    private var playerTickerJob: Job? = null
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
            settings = repository.getInitialPlaybackSettings()
        )
    }

    fun emitMessage(message: String) {
        _toastMessages.tryEmit(message)
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
        val historyForAnime = state.watchHistory.find { it.animeId == animeId }

        val targetEpisode: Episode = when {
            episodeId != null -> anime.episodes.find { it.id == episodeId }
            historyForAnime != null -> anime.episodes.find { it.id == historyForAnime.episodeId }
            else -> anime.episodes.firstOrNull()
        } ?: anime.episodes.firstOrNull() ?: return

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
        }?.id ?: "sanka"

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
            selectedSubtitleId = subtitleId
        )

        _uiState.update { it.copy(playerSession = session) }
        recordWatchProgress(anime, targetEpisode, resumePos)
        startPlayerTicker()
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
            authorBadge = "GOLD VIP",
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
