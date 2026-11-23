package com.gows.animwatch

import com.gows.animwatch.data.remote.AnimeDetailDataDto
import com.gows.animwatch.data.remote.AnimeDetailEpisodeDto
import com.gows.animwatch.data.remote.AnimeDetailResponseDto
import com.gows.animwatch.data.remote.AuthConfigDto
import com.gows.animwatch.data.remote.AuthResponseDto
import com.gows.animwatch.data.remote.CatalogCardDto
import com.gows.animwatch.data.remote.CommentDto
import com.gows.animwatch.data.remote.CommentsListResponseDto
import com.gows.animwatch.data.remote.EpisodeDetailDataDto
import com.gows.animwatch.data.remote.EpisodeDetailResponseDto
import com.gows.animwatch.data.remote.ExternalLinkDto
import com.gows.animwatch.data.remote.GoogleAuthRequestDto
import com.gows.animwatch.data.remote.HistoryUpsertRequestDto
import com.gows.animwatch.data.remote.KumanimeApiClient
import com.gows.animwatch.data.remote.KumanimeApiService
import com.gows.animwatch.data.remote.KumanimeMapper
import com.gows.animwatch.data.remote.KumanimeRemoteDataSource
import com.gows.animwatch.data.remote.LatestItemDto
import com.gows.animwatch.data.remote.LatestResponseDto
import com.gows.animwatch.data.remote.LoginRequestDto
import com.gows.animwatch.data.remote.MovieResponseDto
import com.gows.animwatch.data.remote.PopularItemDto
import com.gows.animwatch.data.remote.PopularResponseDto
import com.gows.animwatch.data.remote.PostCommentRequestDto
import com.gows.animwatch.data.remote.PostCommentResponseDto
import com.gows.animwatch.data.remote.RatingUpsertRequestDto
import com.gows.animwatch.data.remote.RegisterRequestDto
import com.gows.animwatch.data.remote.SearchResponseDto
import com.gows.animwatch.data.remote.SimpleStatusResponseDto
import com.gows.animwatch.data.remote.ToggleCommentLikeResponseDto
import com.gows.animwatch.data.remote.UpdatePreferencesRequestDto
import com.gows.animwatch.data.remote.UpdateProfileRequestDto
import com.gows.animwatch.data.remote.UserProfileDto
import com.gows.animwatch.data.remote.WatchlistUpsertRequestDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

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

        override suspend fun getPopularPage(page: Int): PopularResponseDto = getPopular()

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

        override suspend fun getCompletedPage(page: Int): PopularResponseDto = getCompleted()

        override suspend fun getOfficialFree(): PopularResponseDto = PopularResponseDto(
            status = "success",
            data = listOf(
                PopularItemDto(
                    title = "Dandadan (Sub Indo)",
                    poster = "https://i.ytimg.com/vi/abc12345678/hqdefault.jpg",
                    slug = "ytfree-dandadan",
                    genres = listOf("Gratis Resmi", "Muse Indonesia"),
                    studio = "Muse Indonesia",
                    status = "ONGOING",
                    totalEpisodes = 12,
                    releasedEpisodes = 2,
                    isOfficialFree = true,
                    officialChannel = "Muse Indonesia",
                    streamType = "official_episode",
                    externalLinks = listOf(ExternalLinkDto("Muse Indonesia", "https://www.youtube.com/channel/UCxxnxya_32jcKj4yN1_kD7A")),
                    episodes = listOf(
                        AnimeDetailEpisodeDto(
                            epsTitle = "Episode 1 — Sub Indo",
                            epsSlug = "yt-abc12345678",
                            number = 1,
                            streamUrl = "https://www.youtube-nocookie.com/embed/abc12345678?playsinline=1",
                            streamType = "official_episode"
                        )
                    )
                )
            )
        )

        override suspend fun getOfficialFreePage(page: Int): PopularResponseDto = getOfficialFree()

        override suspend fun getDubIndo(): PopularResponseDto = PopularResponseDto(
            status = "success",
            data = listOf(
                PopularItemDto(
                    title = "Mushoku Tensei: Jobless Reincarnation",
                    poster = "https://cdn.example.com/mushoku.jpg",
                    slug = "dub-PLPanbgyToztZQCUAX0zVQe4HtNDvNtRTw",
                    genres = listOf("Fantasy", "Dub Indo"),
                    studio = "Muse Indonesia",
                    status = "ONGOING",
                    totalEpisodes = 15,
                    releasedEpisodes = 15,
                    isOfficialFree = true,
                    isDubIndo = true,
                    audioLang = "DUB_ID",
                    officialChannel = "Muse Indonesia",
                    streamType = "official_episode"
                )
            )
        )

        override suspend fun getDubIndoPage(page: Int): PopularResponseDto = getDubIndo()

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

        override suspend fun getAuthConfig(): Response<AuthConfigDto> = Response.success(
            AuthConfigDto(
                status = "success",
                googleEnabled = true,
                googleWebClientId = "test-client-id.apps.googleusercontent.com",
                googleWebOAuthEnabled = true
            )
        )

        override suspend fun register(body: RegisterRequestDto): Response<AuthResponseDto> = Response.success(
            AuthResponseDto(
                status = "success",
                token = "tok_register_123",
                user = UserProfileDto(
                    id = 1L,
                    email = body.email,
                    displayName = body.displayName,
                    handle = "@member",
                    avatarEmblem = "MB",
                    memberTier = "Member",
                    hasCompletedThemeSetup = false
                )
            )
        )

        override suspend fun login(body: LoginRequestDto): Response<AuthResponseDto> = Response.success(
            AuthResponseDto(
                status = "success",
                token = "tok_login_456",
                user = UserProfileDto(
                    id = 1L,
                    email = body.email,
                    displayName = "Adrian Vallenz",
                    handle = "@adrian.vip",
                    avatarEmblem = "AV",
                    memberTier = "PRO VIP",
                    appearanceMode = "DARK",
                    accentTheme = "OCEAN_CYAN",
                    hasCompletedThemeSetup = true
                ),
                watchlist = mapOf("sousou-no-frieren-154587" to "WATCHING"),
                watchedEpisodes = mapOf("sousou-no-frieren-154587" to 12),
                ratings = mapOf("sousou-no-frieren-154587" to 10)
            )
        )

        override suspend fun loginWithGoogle(body: GoogleAuthRequestDto): Response<AuthResponseDto> = Response.success(
            AuthResponseDto(
                status = "success",
                token = "tok_google_789",
                user = UserProfileDto(
                    id = 2L,
                    email = "googleuser@gmail.com",
                    displayName = "Google Watcher",
                    handle = "@google.watcher",
                    avatarEmblem = "GW",
                    memberTier = "Google Member",
                    hasCompletedThemeSetup = false
                )
            )
        )

        override suspend fun logout(authHeader: String): Response<SimpleStatusResponseDto> =
            Response.success(SimpleStatusResponseDto("success"))

        override suspend fun getMe(authHeader: String): Response<AuthResponseDto> =
            login(LoginRequestDto("adrian@animwatch.id", "animwatch123"))

        override suspend fun updateProfile(
            authHeader: String,
            body: UpdateProfileRequestDto
        ): Response<AuthResponseDto> = Response.success(
            AuthResponseDto(
                status = "success",
                user = UserProfileDto(displayName = body.displayName, bio = body.bio, avatarEmblem = body.avatarEmblem)
            )
        )

        override suspend fun updatePreferences(
            authHeader: String,
            body: UpdatePreferencesRequestDto
        ): Response<AuthResponseDto> = Response.success(AuthResponseDto(status = "success"))

        override suspend fun upsertWatchlist(
            authHeader: String,
            body: WatchlistUpsertRequestDto
        ): Response<SimpleStatusResponseDto> = Response.success(SimpleStatusResponseDto("success"))

        override suspend fun upsertHistory(
            authHeader: String,
            body: HistoryUpsertRequestDto
        ): Response<SimpleStatusResponseDto> = Response.success(SimpleStatusResponseDto("success"))

        override suspend fun deleteHistoryItem(
            authHeader: String,
            animeId: String
        ): Response<SimpleStatusResponseDto> = Response.success(SimpleStatusResponseDto("success"))

        override suspend fun clearHistory(authHeader: String): Response<SimpleStatusResponseDto> =
            Response.success(SimpleStatusResponseDto("success"))

        override suspend fun upsertRating(
            authHeader: String,
            body: RatingUpsertRequestDto
        ): Response<SimpleStatusResponseDto> = Response.success(SimpleStatusResponseDto("success"))

        override suspend fun getComments(animeId: String, authHeader: String?): Response<CommentsListResponseDto> =
            Response.success(
                CommentsListResponseDto(
                    status = "success",
                    data = listOf(
                        CommentDto(
                            id = "db_cmt_1",
                            animeId = animeId,
                            author = "Adrian Vallenz",
                            authorBadge = "PRO VIP",
                            content = "Keren banget!",
                            timeAgo = "Baru saja",
                            likes = 3,
                            isLiked = true
                        )
                    )
                )
            )

        override suspend fun postComment(
            authHeader: String,
            body: PostCommentRequestDto
        ): Response<PostCommentResponseDto> = Response.success(
            PostCommentResponseDto(
                status = "success",
                comment = CommentDto(
                    id = "db_cmt_99",
                    animeId = body.animeId,
                    episodeNumber = body.episodeNumber,
                    author = "Adrian Vallenz",
                    authorBadge = "PRO VIP",
                    content = body.content,
                    timeAgo = "Baru saja",
                    likes = 0,
                    isLiked = false,
                    isSpoiler = body.isSpoiler
                )
            )
        )

        override suspend fun toggleCommentLike(
            authHeader: String,
            id: String
        ): Response<ToggleCommentLikeResponseDto> =
            Response.success(ToggleCommentLikeResponseDto("success", liked = true, likes = 4))
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
        assertEquals(6, catalog.size)
        assertTrue(catalog.any { it.isDubIndo && it.slug.startsWith("dub-") })
        assertEquals(5, catalog.count { !it.isDubIndo })

        val officialFree = catalog.first { it.isOfficialFree && !it.isDubIndo }
        assertEquals("Muse Indonesia", officialFree.officialChannel)
        assertEquals(1, officialFree.episodes.size)
        assertEquals("official_episode", officialFree.episodes.first().streamType)
        assertEquals(1, officialFree.externalLinks.size)

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

        // Direct slug detail fetch works for related/deep-linked anime not yet in catalog
        val bySlugRes = dataSource.fetchAnimeDetailBySlug("http://10.0.2.2:3000/api/", "solo-leveling-s2")
        assertTrue(bySlugRes.isSuccess)
        assertEquals("Solo Leveling Season 2", bySlugRes.getOrThrow().title)
        assertEquals(2, bySlugRes.getOrThrow().episodes.size)
    }

    @Test
    fun remoteDataSourceHandlesCloudAuthAndComments() = runTest {
        val dataSource = KumanimeRemoteDataSource(serviceProvider = { FakeKumanimeService() })
        val base = "https://kumanime-api-production.up.railway.app/api/"

        val loginRes = dataSource.loginUser(base, "adrian@animwatch.id", "animwatch123")
        assertTrue(loginRes.isSuccess)
        val auth = loginRes.getOrThrow()
        assertEquals("tok_login_456", auth.token)
        assertEquals("OCEAN_CYAN", auth.user?.accentTheme)
        assertEquals("WATCHING", auth.watchlist?.get("sousou-no-frieren-154587"))
        assertEquals(12, auth.watchedEpisodes?.get("sousou-no-frieren-154587"))

        val postRes = dataSource.postComment(base, "tok_login_456", "sousou-no-frieren-154587", 1, "Mantap!", false)
        assertTrue(postRes.isSuccess)
        assertEquals("db_cmt_99", postRes.getOrThrow().id)

        val likeRes = dataSource.toggleCommentLike(base, "tok_login_456", "db_cmt_99")
        assertTrue(likeRes.isSuccess)
        assertEquals(Pair(true, 4), likeRes.getOrThrow())

        val cfgRes = dataSource.fetchAuthConfig(base)
        assertTrue(cfgRes.isSuccess)
        assertEquals(true, cfgRes.getOrThrow().googleEnabled)

        val googleRes = dataSource.loginWithGoogleIdToken(base, "fake_google_id_token")
        assertTrue(googleRes.isSuccess)
        assertEquals("tok_google_789", googleRes.getOrThrow().token)
        assertEquals("Google Watcher", googleRes.getOrThrow().user?.displayName)
    }
}
