package com.gows.animwatch

import com.gows.animwatch.data.remote.AnimeDetailDataDto
import com.gows.animwatch.data.remote.AnimeDetailEpisodeDto
import com.gows.animwatch.data.remote.AnimeDetailResponseDto
import com.gows.animwatch.data.remote.CatalogCardDto
import com.gows.animwatch.data.remote.EpisodeDetailDataDto
import com.gows.animwatch.data.remote.EpisodeDetailResponseDto
import com.gows.animwatch.data.remote.KumanimeApiClient
import com.gows.animwatch.data.remote.KumanimeApiService
import com.gows.animwatch.data.remote.KumanimeMapper
import com.gows.animwatch.data.remote.KumanimeRemoteDataSource
import com.gows.animwatch.data.remote.LatestItemDto
import com.gows.animwatch.data.remote.LatestResponseDto
import com.gows.animwatch.data.remote.MovieResponseDto
import com.gows.animwatch.data.remote.PopularItemDto
import com.gows.animwatch.data.remote.PopularResponseDto
import com.gows.animwatch.data.remote.SearchResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KumanimeIntegrationTest {

    private class FakeKumanimeService : KumanimeApiService {
        override suspend fun getLatest(): LatestResponseDto = LatestResponseDto(
            status = "success",
            data = listOf(
                LatestItemDto(
                    title = "Solo Leveling S2",
                    slug = "solo-leveling-s2-episode-12",
                    poster = "/img/solo.jpg",
                    episode = "12"
                )
            )
        )

        override suspend fun getLatestPage(page: Int): LatestResponseDto = getLatest()

        override suspend fun getPopular(): PopularResponseDto = PopularResponseDto(
            status = "success",
            data = listOf(
                PopularItemDto(
                    title = "One Piece",
                    poster = "https://anime-indo.biz/img/op.jpg",
                    slug = "one-piece",
                    genres = listOf("Action", "Adventure")
                )
            )
        )

        override suspend fun getMovies(): MovieResponseDto = MovieResponseDto(
            status = "success",
            data = listOf(
                CatalogCardDto(
                    title = "Suzume no Tojimari",
                    poster = "https://anime-indo.biz/img/suzume.jpg",
                    slug = "suzume-no-tojimari",
                    type = "Movie",
                    synopsis = "Petualangan menutup pintu bencana.",
                    release = "2023",
                    duration = "122 min"
                )
            )
        )

        override suspend fun getMoviesPage(page: Int): MovieResponseDto = getMovies()

        override suspend fun getCompleted(): PopularResponseDto = PopularResponseDto(
            status = "success",
            data = listOf(
                PopularItemDto(
                    title = "Frieren: Beyond Journey's End",
                    poster = "https://cdn.example.com/frieren.jpg",
                    slug = "sousou-no-frieren-154587",
                    genres = listOf("Adventure", "Drama", "Fantasy"),
                    rating = 9.1,
                    studio = "Madhouse",
                    status = "COMPLETED",
                    totalEpisodes = 28,
                    releasedEpisodes = 28
                )
            )
        )

        override suspend fun searchAnime(query: String): SearchResponseDto = SearchResponseDto(
            status = "success",
            query = query,
            data = listOf(
                CatalogCardDto(
                    title = "Frieren: Beyond Journey's End",
                    poster = "//cdn.example.com/frieren.jpg",
                    slug = "frieren",
                    type = "TV",
                    synopsis = "Perjalanan penyihir elf.",
                    release = "2024",
                    duration = "24 min"
                )
            )
        )

        override suspend fun getAnimeDetail(slug: String): AnimeDetailResponseDto = AnimeDetailResponseDto(
            status = "success",
            animeData = AnimeDetailDataDto(
                title = "Solo Leveling Season 2",
                poster = "/img/solo-full.jpg",
                synopsis = "Sung Jinwoo kembali beraksi.",
                genres = listOf("Action", "Fantasy"),
                episodes = listOf(
                    AnimeDetailEpisodeDto("Episode1", "solo-leveling-s2-episode-1"),
                    AnimeDetailEpisodeDto("Episode12", "solo-leveling-s2-episode-12")
                )
            )
        )

        override suspend fun getEpisodeDetail(slug: String): EpisodeDetailResponseDto = EpisodeDetailResponseDto(
            status = "success",
            epsDetail = EpisodeDetailDataDto(
                title = "Solo Leveling S2 Episode 12",
                poster = "/img/solo.jpg",
                streamUrl = "//gdriveplayer.to/embed2.php?link=abc",
                synopsis = "Pertarungan puncak.",
                animeSlug = "solo-leveling-s2",
                nextEpsSlug = "-",
                prevEpsSlug = "solo-leveling-s2-episode-11"
            )
        )
    }

    @Test
    fun baseUrlNormalizationHandlesMissingSlashAndApiPath() {
        assertEquals("http://10.0.2.2:3000/api/", KumanimeApiClient.normalizeBaseUrl("http://10.0.2.2:3000"))
        assertEquals("http://10.0.2.2:3000/api/", KumanimeApiClient.normalizeBaseUrl("10.0.2.2:3000/api"))
        assertEquals("https://kumanime.vercel.app/api/", KumanimeApiClient.normalizeBaseUrl("https://kumanime.vercel.app/"))
    }

    @Test
    fun urlNormalizerFixesProtocolRelativeAndRelativePaths() {
        assertEquals("https://gdriveplayer.to/embed", KumanimeMapper.normalizeUrl("//gdriveplayer.to/embed"))
        assertEquals("https://anime-indo.biz/img/test.jpg", KumanimeMapper.normalizeUrl("/img/test.jpg"))
        assertEquals("", KumanimeMapper.normalizeUrl("-"))
    }

    @Test
    fun remoteDataSourceFetchesCatalogResolvesLatestSlugAndEpisodeStream() = runTest {
        val dataSource = KumanimeRemoteDataSource(serviceProvider = { FakeKumanimeService() })

        val catalogResult = dataSource.fetchInitialCatalog("https://kumanime-api-production.up.railway.app/api/")
        assertTrue(catalogResult.isSuccess)
        val catalog = catalogResult.getOrThrow()
        assertEquals(4, catalog.size)

        val completedEntry = catalog.first { it.slug == "sousou-no-frieren-154587" }
        assertEquals("Madhouse", completedEntry.studio)
        assertEquals(9.1, completedEntry.rating, 0.01)
        assertEquals(28, completedEntry.totalEpisodes)

        val latestEntry = catalog.first { it.isEpisodeEntry }
        assertEquals("solo-leveling-s2-episode-12", latestEntry.slug)

        // Resolving detail for a /latest entry resolves parent anime_slug automatically
        val detailResult = dataSource.fetchAnimeDetail("http://10.0.2.2:3000/api/", latestEntry)
        assertTrue(detailResult.isSuccess)
        val detailed = detailResult.getOrThrow()
        assertEquals("solo-leveling-s2", detailed.slug)
        assertFalse(detailed.isEpisodeEntry)
        assertEquals(2, detailed.episodes.size)
        assertEquals("Episode 1", detailed.episodes.first().title)

        // Fetching episode stream normalizes //gdriveplayer.to/... to https://...
        val streamRes = dataSource.fetchEpisodeStream("http://10.0.2.2:3000/api/", detailed.episodes.last())
        assertTrue(streamRes.isSuccess)
        val stream = streamRes.getOrThrow()
        assertEquals("https://gdriveplayer.to/embed2.php?link=abc", stream.episode.streamUrl)
        assertEquals("solo-leveling-s2-episode-11", stream.episode.prevEpisodeSlug)
        assertEquals(null, stream.episode.nextEpisodeSlug)

        // Search works
        val searchRes = dataSource.searchAnime("http://10.0.2.2:3000/api/", "frieren")
        assertTrue(searchRes.isSuccess)
        assertEquals("https://cdn.example.com/frieren.jpg", searchRes.getOrThrow().first().posterUrl)
    }
}
