package com.gows.animwatch

import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
import com.gows.animwatch.data.model.CatalogPreset
import com.gows.animwatch.data.model.ReleaseDay
import com.gows.animwatch.data.model.SortOption
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.data.repository.MockAnimeRepository
import com.gows.animwatch.ui.state.AnimWatchViewModel
import com.gows.animwatch.ui.state.AppRoute
import com.gows.animwatch.ui.state.AudioFilter
import com.gows.animwatch.ui.state.MainTab
import com.gows.animwatch.ui.theme.AppAccentTheme
import com.gows.animwatch.ui.theme.AppAppearanceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimWatchRepositoryAndViewModelTest {

    @Test
    fun repositoryProvidesCompleteCatalogAndSchedules() {
        val repo = MockAnimeRepository()
        val animeList = repo.getAllAnime()

        assertTrue("Catalog should contain at least 15 anime", animeList.size >= 15)
        animeList.forEach { anime ->
            assertTrue("Anime ${anime.id} must have episodes", anime.episodes.isNotEmpty())
            assertTrue("Anime ${anime.id} must have genres", anime.genres.isNotEmpty())
        }

        // Every day Monday..Sunday has at least 1 scheduled anime
        ReleaseDay.entries.forEach { day ->
            assertTrue(
                "Schedule day ${day.label} should have anime",
                animeList.any { it.releaseDay == day }
            )
        }
    }

    @Test
    fun exploreFilteringAndSortingWorksAccurately() {
        val vm = AnimWatchViewModel()

        vm.updateSearchQuery("Kage")
        val byQuery = vm.getFilteredExploreAnime()
        assertEquals(1, byQuery.size)
        assertEquals("kage-no-ou", byQuery.first().id)

        vm.resetExploreFilters()
        vm.setTypeFilter(AnimeType.MOVIE)
        val movies = vm.getFilteredExploreAnime()
        assertTrue(movies.isNotEmpty())
        assertTrue(movies.all { it.type == AnimeType.MOVIE })

        vm.resetExploreFilters()
        vm.setStatusFilter(AnimeStatus.COMPLETED)
        vm.setSortOption(SortOption.TOP_RATING)
        val completedTop = vm.getFilteredExploreAnime()
        assertTrue(completedTop.all { it.status == AnimeStatus.COMPLETED })
        assertTrue(completedTop.first().rating >= completedTop.last().rating)
    }

    @Test
    fun watchlistRatingCommentsAndNavigationUpdateState() {
        val vm = AnimWatchViewModel()
        val targetId = "phantom-voyage"

        vm.setWatchlistCategory(targetId, WatchlistCategory.PLAN_TO_WATCH)
        assertEquals(WatchlistCategory.PLAN_TO_WATCH, vm.uiState.value.watchlist[targetId])

        vm.rateAnime(targetId, 10)
        assertEquals(10, vm.uiState.value.userRatings[targetId])

        val initialComments = vm.uiState.value.comments.size
        vm.postComment(targetId, 1, "Mantap sekali episode pembukanya!", isSpoiler = false)
        assertEquals(initialComments + 1, vm.uiState.value.comments.size)
        val posted = vm.uiState.value.comments.first()
        vm.toggleCommentLike(posted.id)
        assertTrue(vm.uiState.value.comments.first().isLiked)
        assertEquals(1, vm.uiState.value.comments.first().likes)

        // Navigation stack
        vm.openAnimeDetail(targetId)
        assertTrue(vm.uiState.value.currentRoute is AppRoute.AnimeDetail)
        assertTrue(vm.navigateBack())
        assertTrue(vm.uiState.value.currentRoute is AppRoute.TabsRoot)

        vm.selectTab(MainTab.LIBRARY)
        assertEquals(MainTab.LIBRARY, vm.uiState.value.currentTab)
        assertTrue(vm.navigateBack())
        assertEquals(MainTab.HOME, vm.uiState.value.currentTab)
        assertFalse(vm.navigateBack())

        val topRated = vm.getCatalogAnime(CatalogPreset.TOP_RATED, null)
        assertEquals(10, topRated.size)
    }

    @Test
    fun authenticationFlowWorksEndToEnd() {
        val vm = AnimWatchViewModel()

        // Starts unauthenticated (shows AuthScreen first)
        assertFalse(vm.uiState.value.isAuthenticated)

        // Wrong password on pre-seeded account sets error
        vm.login("adrian@animwatch.id", "wrongpass")
        assertFalse(vm.uiState.value.isAuthenticated)
        assertTrue(vm.uiState.value.authError != null)

        // Register a new user
        vm.register("Rina Takahashi", "rina@animwatch.id", "secret123")
        assertTrue(vm.uiState.value.isAuthenticated)
        assertFalse(vm.uiState.value.isGuestSession)
        assertEquals("Rina Takahashi", vm.uiState.value.userProfile.displayName)
        assertEquals("RT", vm.uiState.value.userProfile.avatarEmblem)

        // Logout returns to AuthScreen
        vm.logout()
        assertFalse(vm.uiState.value.isAuthenticated)

        // Duplicate registration is rejected
        vm.register("Rina Duplicate", "rina@animwatch.id", "secret123")
        assertFalse(vm.uiState.value.isAuthenticated)
        assertTrue(vm.uiState.value.authError != null)

        // Guest mode works
        vm.continueAsGuest()
        assertTrue(vm.uiState.value.isAuthenticated)
        assertTrue(vm.uiState.value.isGuestSession)

        // Logout then Demo VIP login -> enters Theme Onboarding first
        vm.logout()
        vm.loginDemoVip()
        assertTrue(vm.uiState.value.isAuthenticated)
        assertFalse(vm.uiState.value.isGuestSession)
        assertFalse(vm.uiState.value.hasCompletedThemeSetup)
        assertEquals("Adrian Vallenz", vm.uiState.value.userProfile.displayName)

        // Customize Light/Dark + non-gold accent + preferences and complete onboarding
        vm.setAppearanceMode(AppAppearanceMode.LIGHT)
        vm.setAccentTheme(AppAccentTheme.CRIMSON_RUBY)
        vm.togglePreferredGenre("Romance")
        vm.setGridCatalogPreferred(false)
        vm.completeThemeOnboarding()

        assertTrue(vm.uiState.value.hasCompletedThemeSetup)
        assertEquals(AppAppearanceMode.LIGHT, vm.uiState.value.appearanceMode)
        assertEquals(AppAccentTheme.CRIMSON_RUBY, vm.uiState.value.accentTheme)
        assertTrue("Romance" in vm.uiState.value.preferredGenres)
        assertFalse(vm.uiState.value.exploreFilter.isGridView)

        // Watch tracker activates completed status after finishing an anime
        val sampleAnime = vm.uiState.value.allAnime.first()
        vm.markAnimeCompleted(sampleAnime.id)
        assertEquals(WatchlistCategory.COMPLETED, vm.uiState.value.watchlist[sampleAnime.id])
        assertTrue((vm.uiState.value.watchedEpisodes[sampleAnime.id] ?: 0) >= sampleAnime.totalEpisodes)

        // Opening "Dub Indo" genre routes cleanly to AudioFilter.DUB_INDO instead of genre tag
        vm.openExploreWithGenre("Dub Indo")
        assertEquals(AudioFilter.DUB_INDO, vm.uiState.value.exploreFilter.audioFilter)
        assertTrue(vm.uiState.value.exploreFilter.selectedGenres.isEmpty())
    }
}
