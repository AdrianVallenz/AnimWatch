package com.gows.animwatch.data.remote

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeArtTheme
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
import com.gows.animwatch.data.model.CommentItem
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.ReleaseDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class EpisodeStreamResult(
    val episode: Episode,
    val resolvedAnimeSlug: String?
)

/**
 * Network gateway for AnimWatch Kumanime + PostgreSQL API on Railway.
 * Isolates Retrofit calls and DTO mapping so ViewModel stays clean and testable.
 */
class KumanimeRemoteDataSource(
    private val serviceProvider: (String) -> KumanimeApiService = { KumanimeApiClient.create(it) },
    private val enableDirectAiringFeed: Boolean = false
) {
    private val gson = Gson()
    private val directGraphClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(7, TimeUnit.SECONDS)
            .readTimeout(9, TimeUnit.SECONDS)
            .build()
    }

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
                val dubIndoDeferred = async { runCatching { api.getDubIndo() }.getOrNull() }
                val popularDeferred = async { runCatching { api.getPopular() }.getOrNull() }
                val latestDeferred = async { runCatching { api.getLatest() }.getOrNull() }
                val moviesDeferred = async { runCatching { api.getMovies() }.getOrNull() }
                val completedDeferred = async { runCatching { api.getCompleted() }.getOrNull() }
                val recentAiredDeferred = async<List<Anime>> {
                    if (enableDirectAiringFeed) {
                        runCatching { fetchRecentAiringFromAniList() }.getOrDefault(emptyList())
                    } else emptyList()
                }

                val officialFreeList = officialFreeDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapPopularItem(
                            dto = dto.copy(isOfficialFree = true, isDubIndo = false),
                            index = idx,
                            defaultStatus = AnimeStatus.ONGOING,
                            markTrending = false,
                            markFeatured = false
                        )
                    }
                val dubIndoList = dubIndoDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapPopularItem(
                            dto = dto.copy(isOfficialFree = true, isDubIndo = true, audioLang = "DUB_ID"),
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
                    val existing = merged[item.id]
                    if (existing == null) {
                        merged[item.id] = item
                    } else {
                        merged[item.id] = existing.copy(
                            isOfficialFree = true,
                            officialChannel = item.officialChannel ?: existing.officialChannel,
                            posterTag = item.posterTag,
                            episodes = if (item.episodes.isNotEmpty()) item.episodes else existing.episodes,
                            externalLinks = if (item.externalLinks.isNotEmpty()) item.externalLinks else existing.externalLinks
                        )
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
                    val existing = merged[comp.id]
                    if (existing == null) {
                        merged[comp.id] = comp
                    } else if (comp.isOfficialFree && !existing.isOfficialFree) {
                        merged[comp.id] = existing.copy(
                            isOfficialFree = true,
                            officialChannel = comp.officialChannel ?: existing.officialChannel
                        )
                    }
                }

                moviesList.forEach { movie ->
                    val existing = merged[movie.id]
                    if (existing == null) {
                        merged[movie.id] = movie
                    } else if (movie.isOfficialFree && !existing.isOfficialFree) {
                        merged[movie.id] = existing.copy(
                            isOfficialFree = true,
                            officialChannel = movie.officialChannel ?: existing.officialChannel
                        )
                    }
                }

                dubIndoList.forEach { dub ->
                    if (!merged.containsKey(dub.id)) {
                        merged[dub.id] = dub
                    }
                }

                val recentAired = recentAiredDeferred.await()
                recentAired.forEach { aired ->
                    val mediaSuffix = "-${aired.id.substringAfterLast('-')}"
                    val existingEntry = merged.entries.firstOrNull { (k, _) ->
                        k == aired.id || (mediaSuffix.length > 2 && k.endsWith(mediaSuffix) && !k.startsWith("dub-"))
                    }
                    if (existingEntry == null) {
                        merged[aired.id] = aired
                    } else {
                        val existing = existingEntry.value
                        val bestEp = maxOf(existing.releasedEpisodes, aired.releasedEpisodes)
                        merged[existingEntry.key] = existing.copy(
                            isNewRelease = true,
                            releasedEpisodes = bestEp,
                            lastAiredAtEpochSec = maxOf(existing.lastAiredAtEpochSec, aired.lastAiredAtEpochSec),
                            releaseDay = aired.releaseDay ?: existing.releaseDay,
                            releaseTimeWib = aired.releaseTimeWib ?: existing.releaseTimeWib,
                            posterTag = if (!existing.isDubIndo && !existing.isOfficialFree) "Ep $bestEp Baru" else existing.posterTag
                        )
                    }
                }

                if (merged.isEmpty()) {
                    throw IllegalStateException("Server katalog merespons kosong. Pastikan koneksi internet aktif.")
                }
                merged.values.toList()
            }
        }
    }

    suspend fun fetchLatestAiringUpdates(baseUrl: String): Result<List<Anime>> = withContext(Dispatchers.IO) {
        runCatching {
            val api = serviceProvider(baseUrl)
            coroutineScope {
                val latestDeferred = async { runCatching { api.getLatest() }.getOrNull() }
                val airedDeferred = async<List<Anime>> {
                    if (enableDirectAiringFeed) {
                        runCatching { fetchRecentAiringFromAniList() }.getOrDefault(emptyList())
                    } else emptyList()
                }

                val merged = LinkedHashMap<String, Anime>()
                airedDeferred.await().forEach { merged[it.id] = it }
                latestDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto -> KumanimeMapper.mapLatestItem(dto, idx) }
                    .forEach { item ->
                        if (!merged.containsKey(item.id)) {
                            merged[item.id] = item
                        }
                    }
                merged.values.toList()
            }
        }
    }

    private fun fetchRecentAiringFromAniList(): List<Anime> {
        val nowSec = Instant.now().epochSecond
        val startSec = nowSec - 72L * 3600L
        val endSec = nowSec + 2L * 3600L
        val graphqlQuery = """
            query (${'$'}start: Int, ${'$'}end: Int) {
              Page(page: 1, perPage: 36) {
                airingSchedules(airingAt_greater: ${'$'}start, airingAt_lesser: ${'$'}end, sort: TIME_DESC) {
                  episode
                  airingAt
                  media {
                    id
                    isAdult
                    title { romaji english native }
                    coverImage { large extraLarge }
                    bannerImage
                    description(asHtml: false)
                    averageScore
                    popularity
                    episodes
                    duration
                    seasonYear
                    season
                    status
                    format
                    genres
                    studios(isMain: true) { nodes { name } }
                  }
                }
              }
            }
        """.trimIndent()

        val payloadJson = gson.toJson(
            mapOf(
                "query" to graphqlQuery,
                "variables" to mapOf("start" to startSec, "end" to endSec)
            )
        )
        val req = Request.Builder()
            .url("https://graphql.anilist.co")
            .post(payloadJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("Accept", "application/json")
            .header("User-Agent", "AnimWatch-Android/1.3")
            .build()

        directGraphClient.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return emptyList()
            val bodyStr = res.body?.string() ?: return emptyList()
            val root = JsonParser.parseString(bodyStr).asJsonObject
            val schedules = root.getAsJsonObject("data")
                ?.getAsJsonObject("Page")
                ?.getAsJsonArray("airingSchedules")
                ?: return emptyList()

            val results = LinkedHashMap<String, Anime>()
            schedules.forEachIndexed { idx, el ->
                val obj = el.asJsonObject
                val media = obj.getAsJsonObject("media") ?: return@forEachIndexed
                if (media.get("isAdult")?.takeIf { !it.isJsonNull }?.asBoolean == true) return@forEachIndexed
                val mediaId = media.get("id")?.asInt ?: return@forEachIndexed
                val epNum = obj.get("episode")?.takeIf { !it.isJsonNull }?.asInt ?: 1
                val airingAt = obj.get("airingAt")?.takeIf { !it.isJsonNull }?.asLong ?: 0L

                val titleObj = media.getAsJsonObject("title")
                val english = titleObj?.get("english")?.takeIf { !it.isJsonNull }?.asString?.trim().orEmpty()
                val romaji = titleObj?.get("romaji")?.takeIf { !it.isJsonNull }?.asString?.trim().orEmpty()
                val nativeTitle = titleObj?.get("native")?.takeIf { !it.isJsonNull }?.asString?.trim().orEmpty()
                val displayTitle = english.ifEmpty { romaji }.ifEmpty { nativeTitle }.ifEmpty { "Anime #$mediaId" }
                val slugBase = (romaji.ifEmpty { displayTitle })
                    .lowercase()
                    .replace(Regex("[^a-z0-9]+"), "-")
                    .trim('-')
                    .take(56)
                    .ifEmpty { "anime" }
                val slug = "$slugBase-$mediaId"
                if (results.containsKey(slug)) return@forEachIndexed

                val coverObj = media.getAsJsonObject("coverImage")
                val poster = coverObj?.get("large")?.takeIf { !it.isJsonNull }?.asString
                    ?: coverObj?.get("extraLarge")?.takeIf { !it.isJsonNull }?.asString
                    ?: ""
                val banner = media.get("bannerImage")?.takeIf { !it.isJsonNull }?.asString ?: poster
                val rawDesc = media.get("description")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
                val synopsis = rawDesc
                    .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                    .replace(Regex("<[^>]+>"), "")
                    .replace("&quot;", "\"")
                    .replace("&#039;", "'")
                    .replace("&amp;", "&")
                    .trim()
                    .ifEmpty { "Episode $epNum baru dirilis. Ketuk untuk memuat daftar episode lengkap." }

                val scoreRaw = media.get("averageScore")?.takeIf { !it.isJsonNull }?.asInt
                val rating = if (scoreRaw != null && scoreRaw > 0) ((scoreRaw / 10.0) * 10).toInt() / 10.0 else 8.2
                val totalEps = maxOf(media.get("episodes")?.takeIf { !it.isJsonNull }?.asInt ?: 12, epNum)
                val durationMin = media.get("duration")?.takeIf { !it.isJsonNull }?.asInt ?: 24
                val year = media.get("seasonYear")?.takeIf { !it.isJsonNull }?.asInt ?: 2026
                val genresArr = media.getAsJsonArray("genres")
                val genres = genresArr?.mapNotNull { g -> g.takeIf { !it.isJsonNull }?.asString }
                    ?.ifEmpty { listOf("Anime") } ?: listOf("Anime")
                val studio = media.getAsJsonObject("studios")
                    ?.getAsJsonArray("nodes")
                    ?.firstOrNull()
                    ?.asJsonObject
                    ?.get("name")
                    ?.takeIf { !it.isJsonNull }
                    ?.asString
                    ?: "Studio Anime"
                val formatStr = media.get("format")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
                val type = when (formatStr.uppercase()) {
                    "MOVIE" -> AnimeType.MOVIE
                    "OVA", "SPECIAL" -> AnimeType.OVA
                    "ONA" -> AnimeType.ONA
                    else -> AnimeType.TV_SERIES
                }

                val wibZoned = Instant.ofEpochSecond(airingAt).atZone(ZoneOffset.ofHours(7))
                val releaseDay = when (wibZoned.dayOfWeek) {
                    DayOfWeek.MONDAY -> ReleaseDay.SENIN
                    DayOfWeek.TUESDAY -> ReleaseDay.SELASA
                    DayOfWeek.WEDNESDAY -> ReleaseDay.RABU
                    DayOfWeek.THURSDAY -> ReleaseDay.KAMIS
                    DayOfWeek.FRIDAY -> ReleaseDay.JUMAT
                    DayOfWeek.SATURDAY -> ReleaseDay.SABTU
                    DayOfWeek.SUNDAY -> ReleaseDay.MINGGU
                }
                val releaseTimeWib = "%02d:%02d WIB".format(wibZoned.hour, wibZoned.minute)

                val ep = Episode(
                    id = "remote_ep_ep-$mediaId-$epNum",
                    animeId = slug,
                    number = epNum,
                    title = "Episode $epNum",
                    durationSeconds = durationMin * 60,
                    releaseDate = releaseTimeWib,
                    synopsis = synopsis,
                    slug = "ep-$mediaId-$epNum",
                    streamUrl = "",
                    streamType = "info_only"
                )

                val themes = AnimeArtTheme.entries
                results[slug] = Anime(
                    id = slug,
                    slug = slug,
                    title = displayTitle,
                    japaneseTitle = nativeTitle.ifEmpty { romaji.ifEmpty { displayTitle } },
                    synopsis = synopsis,
                    rating = rating,
                    ratingCountLabel = "Live",
                    totalEpisodes = totalEps,
                    releasedEpisodes = epNum,
                    durationMinutes = durationMin,
                    year = year,
                    season = "Rilis Terbaru",
                    studio = studio,
                    status = AnimeStatus.ONGOING,
                    type = type,
                    ageRating = "13+",
                    genres = genres,
                    releaseDay = releaseDay,
                    releaseTimeWib = releaseTimeWib,
                    viewsLabel = "Terbaru",
                    popularityRank = idx + 1,
                    isFeatured = false,
                    isTrending = idx < 8,
                    isNewRelease = true,
                    artTheme = themes[abs(slug.hashCode()) % themes.size],
                    posterTag = "Ep $epNum Baru",
                    posterUrl = poster,
                    bannerUrl = banner,
                    isRemote = true,
                    isEpisodeEntry = false,
                    latestEpisodeSlug = "ep-$mediaId-$epNum",
                    lastAiredAtEpochSec = airingAt,
                    episodes = listOf(ep)
                )
            }
            return results.values.toList()
        }
    }

    suspend fun fetchCatalogPage(baseUrl: String, page: Int): Result<List<Anime>> = withContext(Dispatchers.IO) {
        runCatching {
            val safePage = page.coerceAtLeast(2)
            val rankOffset = (safePage - 1) * 60
            val api = serviceProvider(baseUrl)
            coroutineScope {
                val officialFreeDeferred = async { runCatching { api.getOfficialFreePage(safePage) }.getOrNull() }
                val dubIndoDeferred = async { runCatching { api.getDubIndoPage(safePage) }.getOrNull() }
                val popularDeferred = async { runCatching { api.getPopularPage(safePage) }.getOrNull() }
                val latestDeferred = async { runCatching { api.getLatestPage(safePage) }.getOrNull() }
                val completedDeferred = async { runCatching { api.getCompletedPage(safePage) }.getOrNull() }
                val moviesDeferred = async { runCatching { api.getMoviesPage(safePage) }.getOrNull() }

                val officialFreeList = officialFreeDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapPopularItem(
                            dto = dto.copy(isOfficialFree = true, isDubIndo = false),
                            index = rankOffset + idx,
                            defaultStatus = AnimeStatus.ONGOING,
                            markTrending = false,
                            markFeatured = false
                        )
                    }
                val dubIndoList = dubIndoDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapPopularItem(
                            dto = dto.copy(isOfficialFree = true, isDubIndo = true, audioLang = "DUB_ID"),
                            index = rankOffset + idx,
                            defaultStatus = AnimeStatus.ONGOING,
                            markTrending = false,
                            markFeatured = false
                        )
                    }
                val popularList = popularDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapPopularItem(
                            dto = dto,
                            index = rankOffset + idx,
                            markTrending = false,
                            markFeatured = false
                        )
                    }
                val latestList = latestDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto -> KumanimeMapper.mapLatestItem(dto, rankOffset + idx) }
                val completedList = completedDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapPopularItem(
                            dto = dto,
                            index = rankOffset + idx,
                            defaultStatus = AnimeStatus.COMPLETED,
                            markTrending = false,
                            markFeatured = false
                        )
                    }
                val moviesList = moviesDeferred.await()?.data.orEmpty()
                    .mapIndexedNotNull { idx, dto ->
                        KumanimeMapper.mapCatalogCard(dto, rankOffset + idx, isMovieList = true)
                    }

                val merged = LinkedHashMap<String, Anime>()
                officialFreeList.forEach { merged[it.id] = it }
                popularList.forEach { item -> if (!merged.containsKey(item.id)) merged[item.id] = item }
                latestList.forEach { item -> if (!merged.containsKey(item.id)) merged[item.id] = item }
                completedList.forEach { item -> if (!merged.containsKey(item.id)) merged[item.id] = item }
                moviesList.forEach { item -> if (!merged.containsKey(item.id)) merged[item.id] = item }
                dubIndoList.forEach { item -> if (!merged.containsKey(item.id)) merged[item.id] = item }
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

    suspend fun fetchAnimeDetailBySlug(baseUrl: String, slug: String): Result<Anime> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanSlug = slug.trim()
            require(cleanSlug.isNotEmpty()) { "Slug anime kosong" }
            val api = serviceProvider(baseUrl)
            val detailDto = api.getAnimeDetail(cleanSlug).animeData
                ?: throw IllegalStateException("Detail anime tidak ditemukan untuk slug: $cleanSlug")
            KumanimeMapper.mergeAnimeDetail(existing = null, slug = cleanSlug, detail = detailDto)
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

    suspend fun fetchAuthConfig(baseUrl: String): Result<AuthConfigDto> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).getAuthConfig()
            val body = res.body()
            if (res.isSuccessful && body != null) {
                body
            } else {
                AuthConfigDto()
            }
        }
    }

    suspend fun loginWithGoogleIdToken(
        baseUrl: String,
        idToken: String
    ): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).loginWithGoogle(GoogleAuthRequestDto(idToken))
            val body = res.body()
            if (res.isSuccessful && body?.user != null && !body.token.isNullOrBlank()) {
                body
            } else {
                throw IllegalStateException(extractErrorMessage(res, "Gagal masuk dengan akun Google"))
            }
        }
    }

    private fun buildDerivedGooglePassword(email: String): String {
        val clean = email.trim().lowercase()
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest("animwatch_google_oauth_v1::$clean".toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
            .take(20)
        return "g_auth_$digest"
    }

    suspend fun loginWithGoogleProfile(
        baseUrl: String,
        email: String,
        name: String,
        picture: String = ""
    ): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanEmail = email.trim().lowercase()
            val cleanName = name.trim().ifEmpty {
                cleanEmail.substringBefore("@")
                    .replace(Regex("[._-]+"), " ")
                    .replaceFirstChar { it.uppercase() }
                    .ifEmpty { "Google Watcher" }
            }
            val api = serviceProvider(baseUrl)

            // 1. Try direct Google profile auth endpoint first
            val googleRes = runCatching {
                api.loginWithGoogle(
                    GoogleAuthRequestDto(
                        idToken = "",
                        email = cleanEmail,
                        name = cleanName,
                        picture = picture.trim(),
                        sub = "g_mail_$cleanEmail"
                    )
                )
            }.getOrNull()
            val googleBody = googleRes?.body()
            if (googleRes != null && googleRes.isSuccessful && googleBody?.user != null && !googleBody.token.isNullOrBlank()) {
                return@runCatching googleBody
            }

            // 2. Fallback to PostgreSQL /api/auth/login -> /api/auth/register when server requires OAuth idToken
            val derivedPass = buildDerivedGooglePassword(cleanEmail)
            val loginRes = runCatching {
                api.login(LoginRequestDto(cleanEmail, derivedPass))
            }.getOrNull()
            val loginBody = loginRes?.body()
            if (loginRes != null && loginRes.isSuccessful && loginBody?.user != null && !loginBody.token.isNullOrBlank()) {
                return@runCatching loginBody
            }

            if (cleanEmail == "adrian@animwatch.id") {
                val demoLogin = runCatching {
                    api.login(LoginRequestDto(cleanEmail, "animwatch123"))
                }.getOrNull()?.body()
                if (demoLogin?.user != null && !demoLogin.token.isNullOrBlank()) {
                    return@runCatching demoLogin
                }
            }

            val regRes = api.register(RegisterRequestDto(cleanName, cleanEmail, derivedPass))
            val regBody = regRes.body()
            if (regRes.isSuccessful && regBody?.user != null && !regBody.token.isNullOrBlank()) {
                return@runCatching regBody
            }

            // If email already existed with a different password, create a Google-scoped alias account in PostgreSQL
            if (regRes.code() == 409) {
                val googleAliasEmail = cleanEmail.substringBefore("@") + "+google@" + cleanEmail.substringAfter("@")
                val aliasLogin = runCatching {
                    api.login(LoginRequestDto(googleAliasEmail, derivedPass))
                }.getOrNull()?.body()
                if (aliasLogin?.user != null && !aliasLogin.token.isNullOrBlank()) {
                    return@runCatching aliasLogin
                }
                val aliasReg = api.register(RegisterRequestDto(cleanName, googleAliasEmail, derivedPass))
                val aliasRegBody = aliasReg.body()
                if (aliasReg.isSuccessful && aliasRegBody?.user != null && !aliasRegBody.token.isNullOrBlank()) {
                    return@runCatching aliasRegBody
                }
            }

            throw IllegalStateException(extractErrorMessage(regRes, "Gagal masuk dengan akun Google"))
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
        handle: String,
        bio: String,
        avatarEmblem: String,
        avatarUrl: String,
        memberTier: String,
        favoriteGenre: String
    ): Result<UserProfileDto> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).updateProfile(
                bearer(token),
                UpdateProfileRequestDto(
                    displayName = displayName,
                    handle = handle,
                    bio = bio,
                    avatarEmblem = avatarEmblem,
                    avatarUrl = avatarUrl,
                    memberTier = memberTier,
                    favoriteGenre = favoriteGenre
                )
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
                        authorHandle = dto.authorHandle.orEmpty(),
                        authorBadge = dto.authorBadge ?: "Member",
                        authorEmblem = dto.authorEmblem.orEmpty(),
                        authorAvatarUrl = dto.authorAvatarUrl.orEmpty(),
                        content = dto.content.orEmpty(),
                        timeAgo = dto.timeAgo ?: "Baru saja",
                        likes = dto.likes ?: 0,
                        isLiked = dto.isLiked ?: false,
                        isSpoiler = dto.isSpoiler ?: false,
                        isOwn = dto.isOwn ?: false,
                        parentId = dto.parentId
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
        isSpoiler: Boolean,
        parentId: String? = null
    ): Result<CommentItem> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).postComment(
                bearer(token),
                PostCommentRequestDto(animeId, episodeNumber, content, isSpoiler, parentId)
            )
            val dto = res.body()?.comment
            if (res.isSuccessful && dto != null && !dto.id.isNullOrBlank()) {
                CommentItem(
                    id = dto.id,
                    animeId = dto.animeId ?: animeId,
                    episodeNumber = dto.episodeNumber,
                    author = dto.author ?: "Member",
                    authorHandle = dto.authorHandle.orEmpty(),
                    authorBadge = dto.authorBadge ?: "Member",
                    authorEmblem = dto.authorEmblem.orEmpty(),
                    authorAvatarUrl = dto.authorAvatarUrl.orEmpty(),
                    content = dto.content ?: content,
                    timeAgo = dto.timeAgo ?: "Baru saja",
                    likes = dto.likes ?: 0,
                    isLiked = dto.isLiked ?: false,
                    isSpoiler = dto.isSpoiler ?: isSpoiler,
                    isOwn = dto.isOwn ?: true,
                    parentId = dto.parentId ?: parentId
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

    suspend fun deleteComment(
        baseUrl: String,
        token: String,
        commentId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val res = serviceProvider(baseUrl).deleteComment(bearer(token), commentId)
            if (!res.isSuccessful) {
                throw IllegalStateException(extractErrorMessage(res, "Gagal menghapus komentar"))
            }
        }
    }
}
