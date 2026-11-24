package com.gows.animwatch.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface KumanimeApiService {

    // Catalog Endpoints
    @GET("official-free")
    suspend fun getOfficialFree(): PopularResponseDto

    @GET("official-free/page/{page}")
    suspend fun getOfficialFreePage(@Path("page") page: Int): PopularResponseDto

    @GET("dub-indo")
    suspend fun getDubIndo(): PopularResponseDto

    @GET("dub-indo/page/{page}")
    suspend fun getDubIndoPage(@Path("page") page: Int): PopularResponseDto

    @GET("sub-eng")
    suspend fun getSubEng(): PopularResponseDto

    @GET("sub-eng/page/{page}")
    suspend fun getSubEngPage(@Path("page") page: Int): PopularResponseDto

    @GET("latest")
    suspend fun getLatest(): LatestResponseDto

    @GET("latest/page/{page}")
    suspend fun getLatestPage(@Path("page") page: Int): LatestResponseDto

    @GET("popular")
    suspend fun getPopular(): PopularResponseDto

    @GET("popular/page/{page}")
    suspend fun getPopularPage(@Path("page") page: Int): PopularResponseDto

    @GET("movie")
    suspend fun getMovies(): MovieResponseDto

    @GET("movie/page/{page}")
    suspend fun getMoviesPage(@Path("page") page: Int): MovieResponseDto

    @GET("completed")
    suspend fun getCompleted(): PopularResponseDto

    @GET("completed/page/{page}")
    suspend fun getCompletedPage(@Path("page") page: Int): PopularResponseDto

    @GET("search/{query}")
    suspend fun searchAnime(@Path("query") query: String): SearchResponseDto

    @GET("anime/{slug}")
    suspend fun getAnimeDetail(@Path("slug") slug: String): AnimeDetailResponseDto

    @GET("episode/{slug}")
    suspend fun getEpisodeDetail(@Path("slug") slug: String): EpisodeDetailResponseDto

    // PostgreSQL Auth & User Persistence Endpoints
    @GET("auth/config")
    suspend fun getAuthConfig(): Response<AuthConfigDto>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<AuthResponseDto>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body body: GoogleAuthRequestDto): Response<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") authHeader: String): Response<SimpleStatusResponseDto>

    @GET("user/me")
    suspend fun getMe(@Header("Authorization") authHeader: String): Response<AuthResponseDto>

    @PATCH("user/profile")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Body body: UpdateProfileRequestDto
    ): Response<AuthResponseDto>

    @PATCH("user/preferences")
    suspend fun updatePreferences(
        @Header("Authorization") authHeader: String,
        @Body body: UpdatePreferencesRequestDto
    ): Response<AuthResponseDto>

    @PUT("user/watchlist")
    suspend fun upsertWatchlist(
        @Header("Authorization") authHeader: String,
        @Body body: WatchlistUpsertRequestDto
    ): Response<SimpleStatusResponseDto>

    @PUT("user/history")
    suspend fun upsertHistory(
        @Header("Authorization") authHeader: String,
        @Body body: HistoryUpsertRequestDto
    ): Response<SimpleStatusResponseDto>

    @DELETE("user/history/{animeId}")
    suspend fun deleteHistoryItem(
        @Header("Authorization") authHeader: String,
        @Path("animeId") animeId: String
    ): Response<SimpleStatusResponseDto>

    @DELETE("user/history")
    suspend fun clearHistory(@Header("Authorization") authHeader: String): Response<SimpleStatusResponseDto>

    @PUT("user/ratings")
    suspend fun upsertRating(
        @Header("Authorization") authHeader: String,
        @Body body: RatingUpsertRequestDto
    ): Response<SimpleStatusResponseDto>

    @GET("comments/{animeId}")
    suspend fun getComments(
        @Path("animeId") animeId: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<CommentsListResponseDto>

    @POST("comments")
    suspend fun postComment(
        @Header("Authorization") authHeader: String,
        @Body body: PostCommentRequestDto
    ): Response<PostCommentResponseDto>

    @POST("comments/{id}/like")
    suspend fun toggleCommentLike(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Response<ToggleCommentLikeResponseDto>

    @DELETE("comments/{id}")
    suspend fun deleteComment(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Response<SimpleStatusResponseDto>

    // Admin CRUD Endpoints
    @POST("admin/verify")
    suspend fun verifyAdmin(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Body body: AdminVerifyRequestDto
    ): Response<AdminVerifyResponseDto>

    @GET("admin/overview")
    suspend fun getAdminOverview(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?
    ): Response<AdminOverviewResponseDto>

    @POST("admin/anime")
    suspend fun adminCreateAnime(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Body body: AdminSaveAnimeRequestDto
    ): Response<AdminSaveAnimeResponseDto>

    @PUT("admin/anime/{slug}")
    suspend fun adminUpdateAnime(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Path("slug") slug: String,
        @Body body: AdminSaveAnimeRequestDto
    ): Response<AdminSaveAnimeResponseDto>

    @DELETE("admin/anime/{slug}")
    suspend fun adminDeleteAnime(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Path("slug") slug: String
    ): Response<SimpleStatusResponseDto>

    @POST("admin/playlists")
    suspend fun adminCreatePlaylist(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Body body: AdminSavePlaylistRequestDto
    ): Response<AdminSavePlaylistResponseDto>

    @PUT("admin/playlists/{playlistId}")
    suspend fun adminUpdatePlaylist(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Path("playlistId") playlistId: String,
        @Body body: AdminSavePlaylistRequestDto
    ): Response<AdminSavePlaylistResponseDto>

    @DELETE("admin/playlists/{playlistId}")
    suspend fun adminDeletePlaylist(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Path("playlistId") playlistId: String
    ): Response<SimpleStatusResponseDto>

    @DELETE("admin/comments/{id}")
    suspend fun adminDeleteComment(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Path("id") id: String
    ): Response<SimpleStatusResponseDto>

    @PATCH("admin/users/{id}")
    suspend fun adminUpdateUser(
        @Header("Authorization") authHeader: String?,
        @Header("X-Admin-Secret") adminSecret: String?,
        @Path("id") id: String,
        @Body body: AdminUpdateUserRequestDto
    ): Response<AdminUpdateUserResponseDto>
}

object KumanimeApiClient {

    const val DEFAULT_BASE_URL = "https://kumanime-api-production.up.railway.app/api/"

    @Volatile
    private var cachedBaseUrl: String? = null

    @Volatile
    private var cachedService: KumanimeApiService? = null

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private fun isLocalHost(hostOrRaw: String): Boolean {
        val cleanHost = hostOrRaw
            .substringAfter("://")
            .substringBefore("/")
            .substringBefore(":")
            .lowercase()
        return cleanHost == "localhost" ||
            cleanHost == "127.0.0.1" ||
            cleanHost == "10.0.2.2" ||
            cleanHost.startsWith("10.") ||
            cleanHost.startsWith("192.168.") ||
            Regex("^172\\.(1[6-9]|2\\d|3[01])\\.").containsMatchIn(cleanHost)
    }

    fun isValidBaseUrl(rawUrl: String): Boolean {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return false
        return normalizeBaseUrlOrNull(trimmed) != null
    }

    private fun normalizeBaseUrlOrNull(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return DEFAULT_BASE_URL
        val withScheme = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            isLocalHost(trimmed) -> "http://$trimmed"
            else -> "https://$trimmed"
        }
        val withoutTrailing = withScheme.trimEnd('/')
        val candidate = if (withoutTrailing.endsWith("/api", ignoreCase = true)) {
            withoutTrailing.dropLast(4) + "/api/"
        } else {
            "$withoutTrailing/api/"
        }
        val parsed = candidate.toHttpUrlOrNull() ?: return null
        return if (parsed.host.isNotBlank()) parsed.toString() else null
    }

    fun normalizeBaseUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return DEFAULT_BASE_URL
        return normalizeBaseUrlOrNull(trimmed) ?: DEFAULT_BASE_URL
    }

    fun create(rawBaseUrl: String): KumanimeApiService {
        val normalized = normalizeBaseUrl(rawBaseUrl)
        val existing = cachedService
        if (existing != null && cachedBaseUrl == normalized) {
            return existing
        }
        synchronized(this) {
            val recheck = cachedService
            if (recheck != null && cachedBaseUrl == normalized) {
                return recheck
            }
            val service = Retrofit.Builder()
                .baseUrl(normalized)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(KumanimeApiService::class.java)
            cachedBaseUrl = normalized
            cachedService = service
            return service
        }
    }
}
