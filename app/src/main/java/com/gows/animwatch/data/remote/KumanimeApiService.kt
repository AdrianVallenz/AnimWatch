package com.gows.animwatch.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface KumanimeApiService {

    @GET("latest")
    suspend fun getLatest(): LatestResponseDto

    @GET("latest/page/{page}")
    suspend fun getLatestPage(@Path("page") page: Int): LatestResponseDto

    @GET("popular")
    suspend fun getPopular(): PopularResponseDto

    @GET("movie")
    suspend fun getMovies(): MovieResponseDto

    @GET("movie/page/{page}")
    suspend fun getMoviesPage(@Path("page") page: Int): MovieResponseDto

    @GET("completed")
    suspend fun getCompleted(): PopularResponseDto

    @GET("search/{query}")
    suspend fun searchAnime(@Path("query") query: String): SearchResponseDto

    @GET("anime/{slug}")
    suspend fun getAnimeDetail(@Path("slug") slug: String): AnimeDetailResponseDto

    @GET("episode/{slug}")
    suspend fun getEpisodeDetail(@Path("slug") slug: String): EpisodeDetailResponseDto
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

    fun normalizeBaseUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim().ifEmpty { DEFAULT_BASE_URL }
        val withScheme = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "http://$trimmed"
        }
        val withoutTrailing = withScheme.trimEnd('/')
        return if (withoutTrailing.endsWith("/api", ignoreCase = true)) {
            "$withoutTrailing/"
        } else {
            "$withoutTrailing/api/"
        }
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
