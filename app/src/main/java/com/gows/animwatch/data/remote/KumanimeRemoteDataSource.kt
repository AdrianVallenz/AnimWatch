package com.gows.animwatch.data.remote

import com.google.gson.Gson
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.CommentItem
import com.gows.animwatch.data.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Response

data class EpisodeStreamResult(
    val episode: Episode,
    val resolvedAnimeSlug: String?
)

/**
 * Network gateway for AnimWatch Kumanime + PostgreSQL API on Railway.
 * Isolates Retrofit calls and DTO mapping so ViewModel stays clean and testable.
 */
class KumanimeRemoteDataSource(
    private val serviceProvider: (String) -> KumanimeApiService = { KumanimeApiClient.create(it) }
) {
    private val gson = Gson()

    private fun extractErrorMessage(response: Response<*>, fallback: String): String {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        if (!raw.isNullOrBlank()) {
            val parsed = runCatching { gson.fromJson(raw, SimpleStatusResponseDto::class.java) }.getOrNull()
            if (!parsed?.message.isNullOrBlank()) {
                return parsed!!.message!!
            }
        }
        return fallback
    }

    private fun bearer(token: String): String = "Bearer $token"

    // ======================================================
    // Anime Catalog Endpoints
    // ======================================================

    suspend fun fetchInitialCatalog(baseUrl: String): Result<List<Anime>> = withContext(Dispatchers.IO) {
        runCatching {
            val api = serviceProvider(baseUrl)
            coroutineScope {
                val officialFreeDeferred = async { runCatching { api.getOfficialFree() }.getOrNull() }
                val popularDeferred = async { runCatching { api.getPopular() }.getOrNull() }
                val latestDeferred = async { runCatching { api.getLatest() }.getOrNull() }
                val moviesDeferred = async { runCatching { api.getMovies() }.getOrNull() }
                val completedDeferred = async { runCatching { api.getCompleted() }.getOrNull() }

                val officialFreeList = officialFreeDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapPopularItem(
                            dto = dto.copy(isOfficialFree = true),
                            index = idx,
                            defaultStatus = AnimeStatus.ONGOING,
                            markTrending = false,
                            markFeatured = false
                        )
                    }
                val popularList = popularDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto -> KumanimeMapper.mapPopularItem(dto, idx) }
                val latestList = latestDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto -> KumanimeMapper.mapLatestItem(dto, idx) }
                val moviesList = moviesDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto -> KumanimeMapper.mapCatalogCard(dto, idx, isMovieList = true) }
                val completedList = completedDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapPopularItem(
                            dto = dto,
                            index = idx,
                            defaultStatus = AnimeStatus.COMPLETED,
                            markTrending = false,
                            markFeatured = false
                        )
                    }

                val merged = LinkedHashMap<String, Anime>()
                popularList.forEach { merged[it.id] = it }
                officialFreeList.forEach { item ->
                    if (!merged.containsKey(item.id)) {
                        merged[item.id] = item
                    }
                }

                latestList.forEach { item ->
                    val existing = merged[item.id]
                    if (existing == null) {
                        merged[item.id] = item
                    } else {
                        merged[item.id] = existing.copy(
                            isNewRelease = true,
                            releaseDay = existing.releaseDay ?: item.releaseDay,
                            releaseTimeWib = existing.releaseTimeWib ?: item.releaseTimeWib,
                            latestEpisodeSlug = existing.latestEpisodeSlug ?: item.latestEpisodeSlug,
                            releasedEpisodes = maxOf(existing.releasedEpisodes, item.releasedEpisodes)
                        )
                    }
                }

                completedList.forEach { comp ->
                    if (!merged.containsKey(comp.id)) {
                        merged[comp.id] = comp
                    }
                }

                moviesList.forEach { movie ->
                    if (!merged.containsKey(movie.id)) {
                        merged[movie.id] = movie
                    }
                }

                if (merged.isEmpty()) {
                    throw IllegalStateException("Kumanime API merespons kosong. Pastikan server aktif.")
                }
                merged.values.toList()
            }
        }
    }

    suspend fun searchAnime(baseUrl: String, query: String): Result<List<Anime>> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) return@runCatching emptyList()
            val api = serviceProvider(baseUrl)
            val response = api.searchAnime(trimmed)
            response.data.orEmpty().mapIndexedNotNull { idx, dto ->
                KumanimeMapper.mapCatalogCard(dto, idx, isMovieList = false)
            }
        }
    }

    suspend fun fetchAnimeDetail(baseUrl: String, existing: Anime): Result<Anime> = withContext(Dispatchers.IO) {
        runCatching {
            val api = serviceProvider(baseUrl)
            var targetSlug = existing.slug

            if (existing.isEpisodeEntry) {
                val epResp = api.getEpisodeDetail(existing.slug).epsDetail
                val resolvedSlug = epResp?.animeSlug?.trim()
                if (!resolvedSlug.isNullOrEmpty() && resolvedSlug != "-") {
                    targetSlug = resolvedSlug
                }
            }

            val detailDto = api.getAnimeDetail(targetSlug).animeData
                ?: throw IllegalStateException("Detail anime tidak ditemukan untuk slug: $targetSlug")
            KumanimeMapper.mergeAnimeDetail(existing, targetSlug, detailDto)
        }
    }

    suspend fun fetchEpisodeStream(baseUrl: String, episode: Episode): Result<EpisodeStreamResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val epSlug = episode.slug.ifBlank { return@runCatching EpisodeStreamResult(episode, null) }
                val api = serviceProvider(baseUrl)
                val detailDto = api.getEpisodeDetail(epSlug).epsDetail
                    ?: throw IllegalStateException("Detail episode tidak ditemukan untuk slug: $epSlug")

                val enriched = KumanimeMapper.applyEpisodeDetail(episode, detailDto)
                val parentSlug = detailDto.animeSlug?.trim()?.takeIf { it.isNotEmpty() && it != "-" }
                EpisodeStreamResult(enriched, parentSlug)
            }
        }

    // ======================================================
    // PostgreSQL Auth, Profile, Watchlist, History & Comments
    // ======================================================

    suspend fun registerUser(
        baseUrl: String,
        displayName: String,
        email: String,
        password: String
    ): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).register(RegisterRequestDto(displayName, email, password))
            val body = res.body()
            if (res.isSuccessful && body?.user != null && !body.token.isNullOrBlank()) {
                body
            } else {
                throw IllegalStateException(extractErrorMessage(res, "Gagal mendaftar akun"))
            }
        }
    }

    suspend fun loginUser(
        baseUrl: String,
        email: String,
        password: String
    ): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).login(LoginRequestDto(email, password))
            val body = res.body()
            if (res.isSuccessful && body?.user != null && !body.token.isNullOrBlank()) {
                body
            } else {
                throw IllegalStateException(extractErrorMessage(res, "Email atau kata sandi tidak valid"))
            }
        }
    }

    suspend fun logoutUser(baseUrl: String, token: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            serviceProvider(baseUrl).logout(bearer(token))
            Unit
        }
    }

    suspend fun fetchCurrentUser(baseUrl: String, token: String): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).getMe(bearer(token))
            val body = res.body()
            if (res.isSuccessful && body?.user != null) {
                body
            } else {
                throw IllegalStateException(extractErrorMessage(res, "Sesi berakhir"))
            }
        }
    }

    suspend fun updateUserProfile(
        baseUrl: String,
        token: String,
        displayName: String,
        bio: String,
        avatarEmblem: String
    ): Result<UserProfileDto> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).updateProfile(
                bearer(token),
                UpdateProfileRequestDto(displayName, bio, avatarEmblem)
            )
            val user = res.body()?.user
            if (res.isSuccessful && user != null) user
            else throw IllegalStateException(extractErrorMessage(res, "Gagal menyimpan profil"))
        }
    }

    suspend fun updateUserPreferences(
        baseUrl: String,
        token: String,
        request: UpdatePreferencesRequestDto
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).updatePreferences(bearer(token), request)
            if (!res.isSuccessful) {
                throw IllegalStateException(extractErrorMessage(res, "Gagal menyimpan preferensi"))
            }
        }
    }

    suspend fun syncWatchlist(
        baseUrl: String,
        token: String,
        animeId: String,
        category: String?,
        watchedEpisodes: Int? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).upsertWatchlist(
                bearer(token),
                WatchlistUpsertRequestDto(animeId, category, watchedEpisodes)
            )
            if (!res.isSuccessful) throw IllegalStateException("Gagal menyimpan watchlist")
        }
    }

    suspend fun syncHistoryItem(
        baseUrl: String,
        token: String,
        request: HistoryUpsertRequestDto
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).upsertHistory(bearer(token), request)
            if (!res.isSuccessful) throw IllegalStateException("Gagal menyimpan riwayat")
        }
    }

    suspend fun deleteHistoryItem(baseUrl: String, token: String, animeId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                serviceProvider(baseUrl).deleteHistoryItem(bearer(token), animeId)
                Unit
            }
        }

    suspend fun clearHistory(baseUrl: String, token: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            serviceProvider(baseUrl).clearHistory(bearer(token))
            Unit
        }
    }

    suspend fun syncRating(baseUrl: String, token: String, animeId: String, score: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                serviceProvider(baseUrl).upsertRating(bearer(token), RatingUpsertRequestDto(animeId, score))
                Unit
            }
        }

    suspend fun fetchComments(baseUrl: String, animeId: String, token: String?): Result<List<CommentItem>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val res = serviceProvider(baseUrl).getComments(animeId, token?.let { bearer(it) })
                res.body()?.data.orEmpty().mapNotNull { dto ->
                    val id = dto.id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    CommentItem(
                        id = id,
                        animeId = dto.animeId ?: animeId,
                        episodeNumber = dto.episodeNumber,
                        author = dto.author ?: "Member",
                        authorBadge = dto.authorBadge ?: "Member",
                        content = dto.content.orEmpty(),
                        timeAgo = dto.timeAgo ?: "Baru saja",
                        likes = dto.likes ?: 0,
                        isLiked = dto.isLiked ?: false,
                        isSpoiler = dto.isSpoiler ?: false
                    )
                }
            }
        }

    suspend fun postComment(
        baseUrl: String,
        token: String,
        animeId: String,
        episodeNumber: Int?,
        content: String,
        isSpoiler: Boolean
    ): Result<CommentItem> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).postComment(
                bearer(token),
                PostCommentRequestDto(animeId, episodeNumber, content, isSpoiler)
            )
            val dto = res.body()?.comment
            if (res.isSuccessful && dto != null && !dto.id.isNullOrBlank()) {
                CommentItem(
                    id = dto.id,
                    animeId = dto.animeId ?: animeId,
                    episodeNumber = dto.episodeNumber,
                    author = dto.author ?: "Member",
                    authorBadge = dto.authorBadge ?: "Member",
                    content = dto.content ?: content,
                    timeAgo = dto.timeAgo ?: "Baru saja",
                    likes = dto.likes ?: 0,
                    isLiked = dto.isLiked ?: false,
                    isSpoiler = dto.isSpoiler ?: isSpoiler
                )
            } else {
                throw IllegalStateException(extractErrorMessage(res, "Gagal mengirim komentar"))
            }
        }
    }

    suspend fun toggleCommentLike(
        baseUrl: String,
        token: String,
        commentId: String
    ): Result<Pair<Boolean, Int>> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).toggleCommentLike(bearer(token), commentId)
            val body = res.body()
            if (res.isSuccessful && body != null) {
                Pair(body.liked ?: false, body.likes ?: 0)
            } else {
                throw IllegalStateException("Gagal menyukai komentar")
            }
        }
    }
}
