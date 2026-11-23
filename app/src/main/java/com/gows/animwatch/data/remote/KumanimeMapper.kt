package com.gows.animwatch.data.remote

import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeArtTheme
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
import com.gows.animwatch.data.model.CharacterRole
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.ExternalPlatformLink
import com.gows.animwatch.data.model.ReleaseDay
import com.gows.animwatch.data.model.StreamServer
import com.gows.animwatch.data.model.SubtitleTrack
import com.gows.animwatch.data.model.VideoQuality
import kotlin.math.abs

object KumanimeMapper {

    private const val UPSTREAM_SITE_HOST = "https://anime-indo.biz"

    private val defaultServers = listOf(
        StreamServer("kumanime", "Kumanime Embed", "ID-MAIN", latencyMs = 24, isRecommended = true),
        StreamServer("gdrive", "DriveMirror", "Global", latencyMs = 42, isRecommended = false)
    )

    private val defaultQualities = listOf(
        VideoQuality("1080p", "1920x1080 FHD", sizeEstimateMb = 320, isUltraHd = true),
        VideoQuality("720p", "1280x720 HD", sizeEstimateMb = 185, isUltraHd = false),
        VideoQuality("480p", "854x480 SD", sizeEstimateMb = 105, isUltraHd = false)
    )

    private val defaultSubtitles = listOf(
        SubtitleTrack("id", "Bahasa Indonesia", "ID"),
        SubtitleTrack("en", "English", "EN"),
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
            "ova" in lower || "special" in lower -> AnimeType.OVA
            "ona" in lower -> AnimeType.ONA
            else -> AnimeType.TV_SERIES
        }
    }

    private fun parseAnimeStatus(rawStatus: String?, fallback: AnimeStatus): AnimeStatus {
        return when (rawStatus?.trim()?.uppercase()) {
            "ONGOING", "RELEASING", "UPCOMING", "NOT_YET_RELEASED" -> AnimeStatus.ONGOING
            "COMPLETED", "FINISHED" -> AnimeStatus.COMPLETED
            else -> fallback
        }
    }

    private fun parseReleaseDay(rawDay: String?): ReleaseDay? {
        return when (rawDay?.trim()?.uppercase()) {
            "MONDAY", "SENIN" -> ReleaseDay.SENIN
            "TUESDAY", "SELASA" -> ReleaseDay.SELASA
            "WEDNESDAY", "RABU" -> ReleaseDay.RABU
            "THURSDAY", "KAMIS" -> ReleaseDay.KAMIS
            "FRIDAY", "JUMAT" -> ReleaseDay.JUMAT
            "SATURDAY", "SABTU" -> ReleaseDay.SABTU
            "SUNDAY", "MINGGU" -> ReleaseDay.MINGGU
            else -> null
        }
    }

    fun mapLatestItem(dto: LatestItemDto, index: Int): Anime? {
        val slug = dto.slug?.trim().orEmpty()
        val title = dto.title?.trim().orEmpty()
        if (slug.isEmpty() || title.isEmpty()) return null

        val epNumber = dto.releasedEpisodes?.takeIf { it > 0 } ?: extractNumber(dto.episode, fallback = 1)
        val totalEpisodes = dto.totalEpisodes?.takeIf { it > 0 } ?: maxOf(epNumber, 12)
        val poster = normalizeUrl(dto.poster)
        val banner = normalizeUrl(dto.banner).ifEmpty { poster }
        val hasCanonicalSlug = !dto.latestEpsSlug.isNullOrBlank()
        val latestEpSlug = dto.latestEpsSlug?.trim()?.takeIf { it.isNotEmpty() } ?: slug
        val genres = dto.genres?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.ifEmpty { listOf("Anime", "Ongoing") }
            ?: listOf("Anime", "Ongoing")

        val ep = Episode(
            id = "remote_ep_$latestEpSlug",
            animeId = slug,
            number = epNumber,
            title = "Episode $epNumber",
            durationSeconds = (dto.durationMinutes ?: 24) * 60,
            releaseDate = "Rilis Terbaru",
            synopsis = dto.synopsis?.trim().orEmpty().ifEmpty { "Streaming anime di AnimWatch." },
            slug = latestEpSlug,
            streamUrl = "",
            streamType = "info_only",
            servers = defaultServers,
            qualities = defaultQualities,
            subtitles = defaultSubtitles
        )

        return Anime(
            id = slug,
            slug = slug,
            title = title,
            japaneseTitle = dto.japaneseTitle?.trim().orEmpty().ifEmpty { "Rilis Terbaru" },
            synopsis = dto.synopsis?.trim().orEmpty().ifEmpty {
                "Episode $epNumber baru saja dirilis. Ketuk untuk memuat sinopsis lengkap dan seluruh episode."
            },
            rating = dto.rating?.coerceIn(1.0, 10.0) ?: 8.5,
            ratingCountLabel = dto.ratingCount?.trim().orEmpty().ifEmpty { "Live" },
            totalEpisodes = totalEpisodes,
            releasedEpisodes = epNumber,
            durationMinutes = dto.durationMinutes ?: 24,
            year = dto.year?.takeIf { it in 1970..2035 } ?: 2026,
            season = dto.season?.trim().orEmpty().ifEmpty { "Ongoing" },
            studio = dto.studio?.trim().orEmpty().ifEmpty { "Kumanime API" },
            status = parseAnimeStatus(dto.status, AnimeStatus.ONGOING),
            type = parseAnimeType(dto.type),
            ageRating = "13+",
            genres = genres,
            releaseDay = parseReleaseDay(dto.airingDay),
            releaseTimeWib = dto.airingTime?.trim()?.takeIf { it.isNotEmpty() },
            viewsLabel = dto.viewsLabel?.trim().orEmpty().ifEmpty { "Live" },
            popularityRank = index + 1,
            isFeatured = index < 3,
            isTrending = index < 6,
            isNewRelease = true,
            isOfficialFree = dto.isOfficialFree == true,
            officialChannel = dto.officialChannel?.trim()?.takeIf { it.isNotEmpty() },
            artTheme = pickArtTheme(slug),
            posterTag = "Ep $epNumber Baru",
            posterUrl = poster,
            bannerUrl = banner,
            isRemote = true,
            isEpisodeEntry = !hasCanonicalSlug,
            latestEpisodeSlug = latestEpSlug,
            episodes = listOf(ep)
        )
    }

    fun mapPopularItem(
        dto: PopularItemDto,
        index: Int,
        defaultStatus: AnimeStatus = AnimeStatus.ONGOING,
        markTrending: Boolean = true,
        markFeatured: Boolean = true
    ): Anime? {
        val slug = dto.slug?.trim().orEmpty()
        val title = dto.title?.trim().orEmpty()
        if (slug.isEmpty() || title.isEmpty()) return null

        val poster = normalizeUrl(dto.poster)
        val banner = normalizeUrl(dto.banner).ifEmpty { poster }
        val genres = dto.genres
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.ifEmpty { listOf("Anime") }
            ?: listOf("Anime")

        val status = parseAnimeStatus(dto.status, defaultStatus)
        val totalEpisodes = dto.totalEpisodes?.takeIf { it > 0 } ?: 12
        val releasedEpisodes = dto.releasedEpisodes?.takeIf { it > 0 } ?: totalEpisodes
        val computedRating = dto.rating?.coerceIn(1.0, 10.0)
            ?: (9.3 - (index * 0.08)).coerceAtLeast(8.0).let { Math.round(it * 10.0) / 10.0 }
        val isOfficialFree = dto.isOfficialFree == true
        val officialChannel = dto.officialChannel?.trim()?.takeIf { it.isNotEmpty() }
        val mappedLinks = dto.externalLinks.orEmpty().mapNotNull { link ->
            val site = link.site?.trim().orEmpty()
            val url = link.url?.trim().orEmpty()
            if (site.isNotEmpty() && url.startsWith("http")) ExternalPlatformLink(site, url) else null
        }
        val mappedEpisodes = dto.episodes.orEmpty().mapIndexedNotNull { idx, epDto ->
            val epSlug = epDto.epsSlug?.trim().orEmpty()
            if (epSlug.isEmpty()) return@mapIndexedNotNull null
            val epLabel = epDto.epsTitle?.trim().orEmpty()
            val number = epDto.number?.takeIf { it > 0 } ?: extractNumber(epLabel, fallback = idx + 1)
            val epStream = normalizeUrl(epDto.streamUrl)
            Episode(
                id = "remote_ep_$epSlug",
                animeId = slug,
                number = number,
                title = epLabel.ifEmpty { "Episode $number" },
                durationSeconds = (dto.durationMinutes ?: 24) * 60,
                releaseDate = officialChannel ?: "Episode $number",
                synopsis = dto.synopsis?.trim().orEmpty().ifEmpty { "Tersedia untuk ditonton di AnimWatch." },
                slug = epSlug,
                streamUrl = epStream,
                streamType = epDto.streamType ?: if (epStream.isNotBlank()) "official_episode" else "info_only",
                thumbnailUrl = normalizeUrl(epDto.thumbnail),
                servers = defaultServers,
                qualities = defaultQualities,
                subtitles = defaultSubtitles
            )
        }.sortedBy { it.number }

        return Anime(
            id = slug,
            slug = slug,
            title = title,
            japaneseTitle = dto.japaneseTitle?.trim().orEmpty().ifEmpty { "Serial Populer" },
            synopsis = dto.synopsis?.trim().orEmpty().ifEmpty {
                "Serial populer pilihan penonton. Ketuk untuk melihat sinopsis lengkap dan seluruh episode."
            },
            rating = computedRating,
            ratingCountLabel = dto.ratingCount?.trim().orEmpty().ifEmpty { "Populer" },
            totalEpisodes = totalEpisodes,
            releasedEpisodes = releasedEpisodes,
            durationMinutes = dto.durationMinutes ?: 24,
            year = dto.year?.takeIf { it in 1970..2035 } ?: 2026,
            season = dto.season?.trim().orEmpty().ifEmpty { if (status == AnimeStatus.COMPLETED) "Tamat" else "Populer" },
            studio = dto.studio?.trim().orEmpty().ifEmpty { "Kumanime API" },
            status = status,
            type = parseAnimeType(dto.type),
            ageRating = "13+",
            genres = genres,
            releaseDay = parseReleaseDay(dto.airingDay),
            releaseTimeWib = dto.airingTime?.trim()?.takeIf { it.isNotEmpty() },
            viewsLabel = dto.viewsLabel?.trim().orEmpty().ifEmpty { "Populer" },
            popularityRank = index + 1,
            isFeatured = markFeatured && index < 4,
            isTrending = markTrending,
            isNewRelease = status == AnimeStatus.ONGOING && index < 6,
            isOfficialFree = isOfficialFree,
            officialChannel = officialChannel,
            artTheme = pickArtTheme(slug),
            posterTag = when {
                isOfficialFree -> officialChannel ?: "Pilihan"
                status == AnimeStatus.COMPLETED -> "Tamat • $computedRating"
                else -> "Populer #${index + 1}"
            },
            posterUrl = poster,
            bannerUrl = banner,
            isRemote = true,
            isEpisodeEntry = false,
            externalLinks = mappedLinks,
            episodes = mappedEpisodes
        )
    }

    fun mapCatalogCard(dto: CatalogCardDto, index: Int, isMovieList: Boolean): Anime? {
        val slug = dto.slug?.trim().orEmpty()
        val title = dto.title?.trim().orEmpty()
        if (slug.isEmpty() || title.isEmpty()) return null

        val poster = normalizeUrl(dto.poster)
        val banner = normalizeUrl(dto.banner).ifEmpty { poster }
        val type = if (isMovieList) AnimeType.MOVIE else parseAnimeType(dto.type)
        val durationMin = dto.durationMinutes?.takeIf { it > 0 }
            ?: extractNumber(dto.duration, fallback = if (type == AnimeType.MOVIE) 105 else 24)
        val year = dto.year?.takeIf { it in 1970..2035 }
            ?: extractNumber(dto.release, fallback = 2025).let { if (it in 1970..2035) it else 2025 }
        val genres = dto.genres?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.ifEmpty { listOf(type.label) }
            ?: listOf(type.label)
        val status = parseAnimeStatus(
            dto.status,
            if (type == AnimeType.MOVIE) AnimeStatus.COMPLETED else AnimeStatus.ONGOING
        )
        val totalEpisodes = dto.totalEpisodes?.takeIf { it > 0 } ?: if (type == AnimeType.MOVIE) 1 else 12
        val releasedEpisodes = dto.releasedEpisodes?.takeIf { it > 0 } ?: totalEpisodes

        return Anime(
            id = slug,
            slug = slug,
            title = title,
            japaneseTitle = dto.japaneseTitle?.trim().orEmpty().ifEmpty {
                dto.type?.trim().orEmpty().ifEmpty { type.label }
            },
            synopsis = dto.synopsis?.trim().orEmpty().ifEmpty {
                "Ketuk untuk memuat informasi lengkap dan daftar episode dari Kumanime API."
            },
            rating = dto.rating?.coerceIn(1.0, 10.0) ?: 8.5,
            ratingCountLabel = dto.ratingCount?.trim().orEmpty().ifEmpty { "Live" },
            totalEpisodes = totalEpisodes,
            releasedEpisodes = releasedEpisodes,
            durationMinutes = durationMin,
            year = year,
            season = dto.season?.trim().orEmpty().ifEmpty { if (type == AnimeType.MOVIE) "Movie $year" else "$year" },
            studio = dto.studio?.trim().orEmpty().ifEmpty { "Kumanime API" },
            status = status,
            type = type,
            ageRating = "13+",
            genres = genres,
            releaseDay = parseReleaseDay(dto.airingDay),
            releaseTimeWib = dto.airingTime?.trim()?.takeIf { it.isNotEmpty() },
            viewsLabel = dto.viewsLabel?.trim().orEmpty().ifEmpty { "Live" },
            popularityRank = index + 1,
            isFeatured = false,
            isTrending = false,
            isNewRelease = false,
            isOfficialFree = dto.isOfficialFree == true,
            officialChannel = dto.officialChannel?.trim()?.takeIf { it.isNotEmpty() },
            artTheme = pickArtTheme(slug),
            posterTag = if (type == AnimeType.MOVIE) "Movie" else "Populer",
            posterUrl = poster,
            bannerUrl = banner,
            isRemote = true,
            isEpisodeEntry = false,
            episodes = emptyList()
        )
    }

    fun mergeAnimeDetail(existing: Anime?, slug: String, detail: AnimeDetailDataDto): Anime {
        val cleanTitle = detail.title?.trim().orEmpty().ifEmpty { existing?.title ?: slug }
        val poster = normalizeUrl(detail.poster).ifEmpty { existing?.posterUrl.orEmpty() }
        val banner = normalizeUrl(detail.banner).ifEmpty { existing?.bannerUrl?.ifEmpty { poster } ?: poster }
        val genres = detail.genres
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.ifEmpty { existing?.genres ?: listOf("Anime") }
            ?: existing?.genres ?: listOf("Anime")

        val durationMin = detail.durationMinutes?.takeIf { it > 0 } ?: existing?.durationMinutes ?: 24
        val channelLabel = detail.officialChannel?.trim()?.takeIf { it.isNotEmpty() } ?: existing?.officialChannel

        val rawEpisodes = detail.episodes.orEmpty()
        val mappedEpisodes = rawEpisodes.mapIndexedNotNull { idx, epDto ->
            val epSlug = epDto.epsSlug?.trim().orEmpty()
            if (epSlug.isEmpty()) return@mapIndexedNotNull null
            val epLabel = epDto.epsTitle?.trim().orEmpty()
            val number = epDto.number?.takeIf { it > 0 } ?: extractNumber(epLabel, fallback = idx + 1)
            val resolvedStream = normalizeUrl(epDto.streamUrl)
            val resolvedType = epDto.streamType?.takeIf { it.isNotBlank() && it != "official_trailer" }
                ?: if (resolvedStream.isNotBlank()) "official_episode" else "info_only"
            Episode(
                id = "remote_ep_$epSlug",
                animeId = slug,
                number = number,
                title = epLabel.replace(Regex("^Episode(?=\\d)"), "Episode ").ifEmpty { "Episode $number" },
                durationSeconds = durationMin * 60,
                releaseDate = channelLabel ?: "Episode $number",
                synopsis = detail.synopsis?.trim().orEmpty().ifEmpty { "Episode tersedia di AnimWatch." },
                slug = epSlug,
                streamUrl = resolvedStream,
                streamType = resolvedType,
                thumbnailUrl = normalizeUrl(epDto.thumbnail),
                servers = defaultServers,
                qualities = defaultQualities,
                subtitles = defaultSubtitles
            )
        }.sortedBy { it.number }

        val mappedLinks = detail.externalLinks.orEmpty().mapNotNull { link ->
            val site = link.site?.trim().orEmpty()
            val url = link.url?.trim().orEmpty()
            if (site.isNotEmpty() && url.startsWith("http")) ExternalPlatformLink(site, url) else null
        }

        val mappedCharacters = detail.characters.orEmpty().mapNotNull { ch ->
            val name = ch.name?.trim().orEmpty()
            if (name.isEmpty()) return@mapNotNull null
            CharacterRole(
                name = name,
                role = ch.role?.trim().orEmpty().ifEmpty { "Karakter" },
                voiceActor = ch.voiceActor?.trim().orEmpty().ifEmpty { "Seiyuu Jepang" },
                emblem = ch.emblem?.trim().orEmpty().ifEmpty { name.take(1).uppercase() }
            )
        }

        val finalEpisodes = if (mappedEpisodes.isNotEmpty()) mappedEpisodes else existing?.episodes.orEmpty()
        val epCount = detail.releasedEpisodes?.takeIf { it > 0 }
            ?: finalEpisodes.size.coerceAtLeast(existing?.releasedEpisodes ?: 1)
        val totalEps = detail.totalEpisodes?.takeIf { it > 0 }
            ?: maxOf(epCount, existing?.totalEpisodes ?: epCount)

        return (existing ?: Anime(
            id = slug,
            slug = slug,
            title = cleanTitle,
            japaneseTitle = detail.japaneseTitle?.trim().orEmpty().ifEmpty { "Kumanime Live" },
            synopsis = "",
            rating = detail.rating?.coerceIn(1.0, 10.0) ?: 8.7,
            ratingCountLabel = detail.ratingCount?.trim().orEmpty().ifEmpty { "Live" },
            totalEpisodes = totalEps,
            releasedEpisodes = epCount,
            durationMinutes = durationMin,
            year = detail.year?.takeIf { it in 1970..2035 } ?: 2026,
            season = detail.season?.trim().orEmpty().ifEmpty { "Kumanime" },
            studio = detail.studio?.trim().orEmpty().ifEmpty { "Kumanime API" },
            status = parseAnimeStatus(detail.status, AnimeStatus.ONGOING),
            type = AnimeType.TV_SERIES,
            ageRating = "13+",
            genres = genres,
            releaseDay = parseReleaseDay(detail.airingDay),
            releaseTimeWib = detail.airingTime?.trim()?.takeIf { it.isNotEmpty() },
            viewsLabel = "Live",
            popularityRank = 99,
            artTheme = pickArtTheme(slug),
            posterTag = "Populer",
            posterUrl = poster,
            bannerUrl = banner,
            isRemote = true
        )).copy(
            id = slug,
            slug = slug,
            title = cleanTitle,
            japaneseTitle = detail.japaneseTitle?.trim().orEmpty().ifEmpty { existing?.japaneseTitle.orEmpty() },
            synopsis = detail.synopsis?.trim().orEmpty().ifEmpty { existing?.synopsis.orEmpty() },
            rating = detail.rating?.coerceIn(1.0, 10.0) ?: existing?.rating ?: 8.7,
            ratingCountLabel = detail.ratingCount?.trim().orEmpty().ifEmpty { existing?.ratingCountLabel ?: "Live" },
            studio = detail.studio?.trim().orEmpty().ifEmpty { existing?.studio ?: "Kumanime API" },
            season = detail.season?.trim().orEmpty().ifEmpty { existing?.season ?: "Kumanime" },
            year = detail.year?.takeIf { it in 1970..2035 } ?: existing?.year ?: 2026,
            status = parseAnimeStatus(detail.status, existing?.status ?: AnimeStatus.ONGOING),
            durationMinutes = durationMin,
            posterUrl = poster,
            bannerUrl = banner,
            genres = genres,
            releaseDay = parseReleaseDay(detail.airingDay) ?: existing?.releaseDay,
            releaseTimeWib = detail.airingTime?.trim()?.takeIf { it.isNotEmpty() } ?: existing?.releaseTimeWib,
            totalEpisodes = maxOf(totalEps, epCount),
            releasedEpisodes = epCount,
            isRemote = true,
            isEpisodeEntry = false,
            isOfficialFree = (detail.isOfficialFree == true) || (existing?.isOfficialFree == true),
            officialChannel = detail.officialChannel?.trim()?.takeIf { it.isNotEmpty() } ?: existing?.officialChannel,
            episodes = finalEpisodes,
            characters = mappedCharacters.ifEmpty { existing?.characters.orEmpty() },
            relatedIds = detail.relatedSlugs?.filter { it.isNotBlank() }?.ifEmpty { existing?.relatedIds.orEmpty() }
                ?: existing?.relatedIds.orEmpty(),
            externalLinks = mappedLinks.ifEmpty { existing?.externalLinks.orEmpty() }
        )
    }

    fun applyEpisodeDetail(episode: Episode, detail: EpisodeDetailDataDto): Episode {
        val stream = normalizeUrl(detail.streamUrl)
        val nextSlug = detail.nextEpsSlug?.trim()?.takeIf { it.isNotEmpty() && it != "-" }
        val prevSlug = detail.prevEpsSlug?.trim()?.takeIf { it.isNotEmpty() && it != "-" }
        return episode.copy(
            title = detail.title?.trim().orEmpty().ifEmpty { episode.title },
            synopsis = detail.synopsis?.trim().orEmpty().ifEmpty { episode.synopsis },
            streamUrl = stream.ifEmpty { episode.streamUrl },
            streamType = detail.streamType?.takeIf { it.isNotBlank() } ?: episode.streamType,
            nextEpisodeSlug = nextSlug,
            prevEpisodeSlug = prevSlug
        )
    }
}
