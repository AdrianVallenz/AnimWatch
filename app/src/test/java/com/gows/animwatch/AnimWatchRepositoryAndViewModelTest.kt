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
import com.gows.animwatch.ui.state.MainTab
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
}
