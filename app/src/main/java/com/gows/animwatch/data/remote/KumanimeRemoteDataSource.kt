package com.gows.animwatch.data.remote

import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class EpisodeStreamResult(
    val episode: Episode,
    val resolvedAnimeSlug: String?
)

/**
 * Network gateway for AnimWatch Kumanime API (Railway AniList-live + MastayY/kumanime-api compatible).
 * Isolates Retrofit calls and DTO mapping so ViewModel stays clean and testable.
 */
class KumanimeRemoteDataSource(
    private val serviceProvider: (String) -> KumanimeApiService = { KumanimeApiClient.create(it) }
) {

    suspend fun fetchInitialCatalog(baseUrl: String): Result<List<Anime>> = withContext(Dispatchers.IO) {
        runCatching {
            val api = serviceProvider(baseUrl)
            coroutineScope {
                val popularDeferred = async { runCatching { api.getPopular() }.getOrNull() }
                val latestDeferred = async { runCatching { api.getLatest() }.getOrNull() }
                val moviesDeferred = async { runCatching { api.getMovies() }.getOrNull() }
                val completedDeferred = async { runCatching { api.getCompleted() }.getOrNull() }

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

            // Classic /api/latest returns episode slugs instead of anime slugs.
            // Resolve the parent anime_slug via /api/episode/{slug} first.
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
}
