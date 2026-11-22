package com.gows.animwatch.data.remote

import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeArtTheme
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.StreamServer
import com.gows.animwatch.data.model.SubtitleTrack
import com.gows.animwatch.data.model.VideoQuality
import kotlin.math.abs

object KumanimeMapper {

    private const val UPSTREAM_SITE_HOST = "https://anime-indo.biz"

    private val defaultServers = listOf(
        StreamServer("kumanime", "Kumanime Embed", "ID-MAIN", latencyMs = 28, isRecommended = true),
        StreamServer("gdrive", "DriveMirror", "Global", latencyMs = 45, isRecommended = false)
    )

    private val defaultQualities = listOf(
        VideoQuality("1080p", "1920x1080 FHD", sizeEstimateMb = 320, isUltraHd = true),
        VideoQuality("720p", "1280x720 HD", sizeEstimateMb = 185, isUltraHd = false),
        VideoQuality("480p", "854x480 SD", sizeEstimateMb = 105, isUltraHd = false)
    )

    private val defaultSubtitles = listOf(
        SubtitleTrack("id", "Bahasa Indonesia (Hardsub)", "ID"),
        SubtitleTrack("off", "Nonaktif", "OFF")
    )

    fun normalizeUrl(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty() || value == "-") return ""
        return when {
            value.startsWith("http://", ignoreCase = true) ||
                value.startsWith("https://", ignoreCase = true) -> value
            value.startsWith("//") -> "https:$value"
            value.startsWith("/") -> "$UPSTREAM_SITE_HOST$value"
            else -> "$UPSTREAM_SITE_HOST/$value"
        }
    }

    private fun pickArtTheme(key: String): AnimeArtTheme {
        val values = AnimeArtTheme.entries
        return values[abs(key.hashCode()) % values.size]
    }

    private fun extractNumber(text: String?, fallback: Int = 1): Int {
        if (text.isNullOrBlank()) return fallback
        return Regex("\\d+").find(text)?.value?.toIntOrNull() ?: fallback
    }

    private fun parseAnimeType(rawType: String?): AnimeType {
        val lower = rawType?.lowercase().orEmpty()
        return when {
            "movie" in lower -> AnimeType.MOVIE
            "ova" in lower -> AnimeType.OVA
            "ona" in lower -> AnimeType.ONA
            else -> AnimeType.TV_SERIES
        }
    }

    fun mapLatestItem(dto: LatestItemDto, index: Int): Anime? {
        val slug = dto.slug?.trim().orEmpty()
        val title = dto.title?.trim().orEmpty()
        if (slug.isEmpty() || title.isEmpty()) return null

        val epNumber = extractNumber(dto.episode, fallback = 1)
        val poster = normalizeUrl(dto.poster)
        val ep = Episode(
            id = "remote_ep_$slug",
            animeId = slug,
            number = epNumber,
            title = "Episode $epNumber",
            durationSeconds = 24 * 60,
            releaseDate = "Rilis Terbaru",
            synopsis = "Streaming anime Sub Indo melalui Kumanime API.",
            slug = slug,
            servers = defaultServers,
            qualities = defaultQualities,
            subtitles = defaultSubtitles
        )

        return Anime(
            id = slug,
            slug = slug,
            title = title,
            japaneseTitle = "Kumanime Live Release",
            synopsis = "Episode $epNumber baru saja dirilis di AnimeIndo (Kumanime API). Ketuk untuk memuat sinopsis lengkap dan daftar seluruh episode.",
            rating = 8.6,
            ratingCountLabel = "LIVE",
            totalEpisodes = epNumber,
            releasedEpisodes = epNumber,
            durationMinutes = 24,
            year = 2026,
            season = "Ongoing",
            studio = "Kumanime API",
            status = AnimeStatus.ONGOING,
            type = AnimeType.TV_SERIES,
            ageRating = "13+",
            genres = listOf("Sub Indo", "Ongoing"),
            releaseDay = null,
            releaseTimeWib = null,
            viewsLabel = "LIVE",
            popularityRank = index + 1,
            isFeatured = index < 3,
            isTrending = index < 6,
            isNewRelease = true,
            artTheme = pickArtTheme(slug),
            posterTag = "EP $epNumber BARU",
            posterUrl = poster,
            bannerUrl = poster,
            isRemote = true,
            isEpisodeEntry = true,
            latestEpisodeSlug = slug,
            episodes = listOf(ep)
        )
    }

    fun mapPopularItem(dto: PopularItemDto, index: Int): Anime? {
        val slug = dto.slug?.trim().orEmpty()
        val title = dto.title?.trim().orEmpty()
        if (slug.isEmpty() || title.isEmpty()) return null

        val poster = normalizeUrl(dto.poster)
        val genres = dto.genres
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.ifEmpty { listOf("Anime", "Sub Indo") }
            ?: listOf("Anime", "Sub Indo")

        return Anime(
            id = slug,
            slug = slug,
            title = title,
            japaneseTitle = "Popular Series",
            synopsis = "Serial populer pilihan penonton Sub Indo dari Kumanime API. Ketuk untuk melihat sinopsis dan seluruh episode.",
            rating = (9.3 - (index * 0.08)).coerceAtLeast(8.0).let { Math.round(it * 10.0) / 10.0 },
            ratingCountLabel = "POPULER",
            totalEpisodes = 12,
            releasedEpisodes = 12,
            durationMinutes = 24,
            year = 2026,
            season = "Popular",
            studio = "Kumanime API",
            status = AnimeStatus.ONGOING,
            type = AnimeType.TV_SERIES,
            ageRating = "13+",
            genres = genres,
            releaseDay = null,
            releaseTimeWib = null,
            viewsLabel = "POPULER",
            popularityRank = index + 1,
            isFeatured = index < 4,
            isTrending = true,
            isNewRelease = false,
            artTheme = pickArtTheme(slug),
            posterTag = "POPULER #${index + 1}",
            posterUrl = poster,
            bannerUrl = poster,
            isRemote = true,
            isEpisodeEntry = false,
            episodes = emptyList()
        )
    }

    fun mapCatalogCard(dto: CatalogCardDto, index: Int, isMovieList: Boolean): Anime? {
        val slug = dto.slug?.trim().orEmpty()
        val title = dto.title?.trim().orEmpty()
        if (slug.isEmpty() || title.isEmpty()) return null

        val poster = normalizeUrl(dto.poster)
        val type = if (isMovieList) AnimeType.MOVIE else parseAnimeType(dto.type)
        val durationMin = extractNumber(dto.duration, fallback = if (type == AnimeType.MOVIE) 105 else 24)
        val year = extractNumber(dto.release, fallback = 2025).let { if (it in 1970..2030) it else 2025 }

        return Anime(
            id = slug,
            slug = slug,
            title = title,
            japaneseTitle = dto.type?.trim().orEmpty().ifEmpty { type.label },
            synopsis = dto.synopsis?.trim().orEmpty().ifEmpty {
                "Ketuk untuk memuat informasi lengkap dan daftar episode dari Kumanime API."
            },
            rating = 8.5,
            ratingCountLabel = "API",
            totalEpisodes = if (type == AnimeType.MOVIE) 1 else 12,
            releasedEpisodes = if (type == AnimeType.MOVIE) 1 else 12,
            durationMinutes = durationMin,
            year = year,
            season = if (type == AnimeType.MOVIE) "Movie $year" else "$year",
            studio = "Kumanime API",
            status = if (type == AnimeType.MOVIE) AnimeStatus.COMPLETED else AnimeStatus.ONGOING,
            type = type,
            ageRating = "13+",
            genres = listOf(type.label, "Sub Indo"),
            releaseDay = null,
            releaseTimeWib = null,
            viewsLabel = "API",
            popularityRank = index + 1,
            isFeatured = false,
            isTrending = false,
            isNewRelease = false,
            artTheme = pickArtTheme(slug),
            posterTag = if (type == AnimeType.MOVIE) "MOVIE" else "SUB INDO",
            posterUrl = poster,
            bannerUrl = poster,
            isRemote = true,
            isEpisodeEntry = false,
            episodes = emptyList()
        )
    }

    fun mergeAnimeDetail(existing: Anime?, slug: String, detail: AnimeDetailDataDto): Anime {
        val cleanTitle = detail.title?.trim().orEmpty().ifEmpty { existing?.title ?: slug }
        val poster = normalizeUrl(detail.poster).ifEmpty { existing?.posterUrl.orEmpty() }
        val genres = detail.genres
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.ifEmpty { existing?.genres ?: listOf("Anime", "Sub Indo") }
            ?: existing?.genres ?: listOf("Anime", "Sub Indo")

        val rawEpisodes = detail.episodes.orEmpty()
        val mappedEpisodes = rawEpisodes.mapIndexedNotNull { idx, epDto ->
            val epSlug = epDto.epsSlug?.trim().orEmpty()
            if (epSlug.isEmpty()) return@mapIndexedNotNull null
            val epLabel = epDto.epsTitle?.trim().orEmpty()
            val number = extractNumber(epLabel, fallback = idx + 1)
            Episode(
                id = "remote_ep_$epSlug",
                animeId = slug,
                number = number,
                title = epLabel.replace(Regex("^Episode(?=\\d)"), "Episode ").ifEmpty { "Episode $number" },
                durationSeconds = (existing?.durationMinutes ?: 24) * 60,
                releaseDate = "Sub Indo",
                synopsis = detail.synopsis?.trim().orEmpty().ifEmpty { "Tersedia melalui Kumanime API." },
                slug = epSlug,
                servers = defaultServers,
                qualities = defaultQualities,
                subtitles = defaultSubtitles
            )
        }.sortedBy { it.number }

        val finalEpisodes = if (mappedEpisodes.isNotEmpty()) mappedEpisodes else existing?.episodes.orEmpty()
        val epCount = finalEpisodes.size.coerceAtLeast(existing?.releasedEpisodes ?: 1)

        return (existing ?: Anime(
            id = slug,
            slug = slug,
            title = cleanTitle,
            japaneseTitle = "Sub Indo • Kumanime API",
            synopsis = "",
            rating = 8.7,
            ratingCountLabel = "LIVE",
            totalEpisodes = epCount,
            releasedEpisodes = epCount,
            durationMinutes = 24,
            year = 2026,
            season = "Kumanime",
            studio = "Kumanime API",
            status = AnimeStatus.ONGOING,
            type = AnimeType.TV_SERIES,
            ageRating = "13+",
            genres = genres,
            releaseDay = null,
            releaseTimeWib = null,
            viewsLabel = "LIVE",
            popularityRank = 99,
            artTheme = pickArtTheme(slug),
            posterTag = "SUB INDO",
            posterUrl = poster,
            bannerUrl = poster,
            isRemote = true
        )).copy(
            id = slug,
            slug = slug,
            title = cleanTitle,
            synopsis = detail.synopsis?.trim().orEmpty().ifEmpty { existing?.synopsis.orEmpty() },
            posterUrl = poster,
            bannerUrl = poster,
            genres = genres,
            totalEpisodes = maxOf(epCount, existing?.totalEpisodes ?: epCount),
            releasedEpisodes = epCount,
            isRemote = true,
            isEpisodeEntry = false,
            episodes = finalEpisodes
        )
    }

    fun applyEpisodeDetail(episode: Episode, detail: EpisodeDetailDataDto): Episode {
        val stream = normalizeUrl(detail.streamUrl)
        val nextSlug = detail.nextEpsSlug?.trim()?.takeIf { it.isNotEmpty() && it != "-" }
        val prevSlug = detail.prevEpsSlug?.trim()?.takeIf { it.isNotEmpty() && it != "-" }
        return episode.copy(
            title = detail.title?.trim().orEmpty().ifEmpty { episode.title },
            synopsis = detail.synopsis?.trim().orEmpty().ifEmpty { episode.synopsis },
            streamUrl = stream,
            nextEpisodeSlug = nextSlug,
            prevEpisodeSlug = prevSlug
        )
    }
}
