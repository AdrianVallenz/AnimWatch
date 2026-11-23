package com.gows.animwatch.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class AnimeType(val label: String) {
    TV_SERIES("TV Series"),
    MOVIE("Movie"),
    OVA("OVA"),
    ONA("ONA")
}

enum class AnimeStatus(val label: String) {
    ONGOING("Ongoing"),
    COMPLETED("Tamat")
}

enum class WatchlistCategory(val label: String) {
    WATCHING("Disimpan"),
    PLAN_TO_WATCH("Disimpan"),
    COMPLETED("Disimpan"),
    ON_HOLD("Disimpan")
}

enum class ReleaseDay(val label: String, val shortLabel: String, val dayOrder: Int) {
    SENIN("Senin", "Sen", 1),
    SELASA("Selasa", "Sel", 2),
    RABU("Rabu", "Rab", 3),
    KAMIS("Kamis", "Kam", 4),
    JUMAT("Jumat", "Jum", 5),
    SABTU("Sabtu", "Sab", 6),
    MINGGU("Minggu", "Min", 7)
}

enum class SortOption(val label: String) {
    POPULAR("Terpopuler"),
    TOP_RATING("Rating Tertinggi"),
    LATEST("Terbaru Rilis"),
    TITLE_AZ("Judul A–Z")
}

enum class CatalogPreset(val title: String, val subtitle: String) {
    ALL("Semua Katalog", "Seluruh koleksi anime"),
    OFFICIAL_FREE("Siap Tonton • Sub Indo", "Episode lengkap Sub Indo siap diputar langsung"),
    DUB_INDO("Kategori Dub Indo", "Koleksi anime sulih suara Bahasa Indonesia"),
    ONGOING("Sedang Tayang", "Episode baru hadir setiap minggu"),
    TRENDING("Sedang Hangat", "Paling banyak ditonton minggu ini"),
    TOP_RATED("Top 10 Peringkat", "Skor tertinggi pilihan penonton"),
    LATEST("Rilis Terbaru", "Episode baru saja diperbarui"),
    COMPLETED("Tamat — Siap Maraton", "Tonton tuntas dari episode awal hingga akhir"),
    MOVIES("Anime Movie", "Film layar lebar kualitas sinematik")
}

/**
 * Curated artwork visual themes so every poster, banner, and simulated
 * streaming scene renders crisp, atmospheric custom graphics before API hookup.
 */
enum class AnimeArtTheme(
    val primaryBg: Color,
    val secondaryBg: Color,
    val accentGlow: Color,
    val kanjiCrest: String,
    val motif: ArtMotif
) {
    CRIMSON_BLADE(
        primaryBg = Color(0xFF241016),
        secondaryBg = Color(0xFF0E0B10),
        accentGlow = Color(0xFFE0455A),
        kanjiCrest = "鬼刃",
        motif = ArtMotif.KATANA_SLASH
    ),
    CELESTIAL_ASTRAL(
        primaryBg = Color(0xFF12182D),
        secondaryBg = Color(0xFF0A0D16),
        accentGlow = Color(0xFF4C7CF5),
        kanjiCrest = "星詠",
        motif = ArtMotif.MAGIC_RING
    ),
    ABYSSAL_SHADOW(
        primaryBg = Color(0xFF17132A),
        secondaryBg = Color(0xFF0B0A14),
        accentGlow = Color(0xFF8B6CE8),
        kanjiCrest = "影王",
        motif = ArtMotif.CROWN_MONOLITH
    ),
    CYBER_PROTOCOL(
        primaryBg = Color(0xFF0D1E24),
        secondaryBg = Color(0xFF080E12),
        accentGlow = Color(0xFF22B8CF),
        kanjiCrest = "零式",
        motif = ArtMotif.MECHA_GRID
    ),
    SAKURA_TWILIGHT(
        primaryBg = Color(0xFF24121E),
        secondaryBg = Color(0xFF0E0A10),
        accentGlow = Color(0xFFE0569C),
        kanjiCrest = "桜風",
        motif = ArtMotif.TORII_MOON
    ),
    IMPERIAL_ALCHEMY(
        primaryBg = Color(0xFF231510),
        secondaryBg = Color(0xFF0E0A0A),
        accentGlow = Color(0xFFEB6B40),
        kanjiCrest = "錬金",
        motif = ArtMotif.SOLAR_ECLIPSE
    ),
    PHANTOM_VOYAGE(
        primaryBg = Color(0xFF0E1E1B),
        secondaryBg = Color(0xFF080E0D),
        accentGlow = Color(0xFF2FC480),
        kanjiCrest = "航海",
        motif = ArtMotif.COMPASS_STAR
    ),
    CHRONO_SANCTUM(
        primaryBg = Color(0xFF161529),
        secondaryBg = Color(0xFF0A0A14),
        accentGlow = Color(0xFF6B8AFF),
        kanjiCrest = "刻霊",
        motif = ArtMotif.HOURGLASS_SIGIL
    )
}

enum class ArtMotif {
    KATANA_SLASH,
    MAGIC_RING,
    CROWN_MONOLITH,
    MECHA_GRID,
    TORII_MOON,
    SOLAR_ECLIPSE,
    COMPASS_STAR,
    HOURGLASS_SIGIL
}

data class CharacterRole(
    val name: String,
    val role: String,
    val voiceActor: String,
    val emblem: String
)

data class StreamServer(
    val id: String,
    val name: String,
    val region: String,
    val latencyMs: Int,
    val isRecommended: Boolean = false
)

data class VideoQuality(
    val label: String,
    val resolution: String,
    val sizeEstimateMb: Int,
    val isUltraHd: Boolean = false
)

data class SubtitleTrack(
    val id: String,
    val label: String,
    val code: String
)

data class SubtitleCue(
    val startSec: Int,
    val endSec: Int,
    val indoText: String,
    val englishText: String
)

@Immutable
data class ExternalPlatformLink(
    val site: String,
    val url: String
)

@Immutable
data class Episode(
    val id: String,
    val animeId: String,
    val number: Int,
    val title: String,
    val durationSeconds: Int,
    val releaseDate: String,
    val synopsis: String,
    val introStartSec: Int = 15,
    val introEndSec: Int = 85,
    val isFiller: Boolean = false,
    val slug: String = "",
    val streamUrl: String = "",
    val streamType: String = "info_only",
    val thumbnailUrl: String = "",
    val nextEpisodeSlug: String? = null,
    val prevEpisodeSlug: String? = null,
    val servers: List<StreamServer> = emptyList(),
    val qualities: List<VideoQuality> = emptyList(),
    val subtitles: List<SubtitleTrack> = emptyList()
)

@Immutable
data class Anime(
    val id: String,
    val slug: String,
    val title: String,
    val japaneseTitle: String,
    val synopsis: String,
    val rating: Double,
    val ratingCountLabel: String,
    val totalEpisodes: Int,
    val releasedEpisodes: Int,
    val durationMinutes: Int,
    val year: Int,
    val season: String,
    val studio: String,
    val status: AnimeStatus,
    val type: AnimeType,
    val ageRating: String,
    val genres: List<String>,
    val releaseDay: ReleaseDay?,
    val releaseTimeWib: String?,
    val viewsLabel: String,
    val popularityRank: Int,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isNewRelease: Boolean = false,
    val isOfficialFree: Boolean = false,
    val isDubIndo: Boolean = false,
    val officialChannel: String? = null,
    val artTheme: AnimeArtTheme,
    val posterTag: String,
    val posterUrl: String = "",
    val bannerUrl: String = "",
    val isRemote: Boolean = false,
    val isEpisodeEntry: Boolean = false,
    val latestEpisodeSlug: String? = null,
    val lastAiredAtEpochSec: Long = 0L,
    val characters: List<CharacterRole> = emptyList(),
    val relatedIds: List<String> = emptyList(),
    val externalLinks: List<ExternalPlatformLink> = emptyList(),
    val episodes: List<Episode> = emptyList()
) {
    val latestEpisodeNumber: Int
        get() = maxOf(episodes.maxOfOrNull { it.number } ?: 0, releasedEpisodes)
}

object FranchiseSeasonHelper {
    private val KNOWN_ROOT_PREFIXES = listOf(
        "re:zero",
        "mushoku tensei",
        "frieren",
        "spy x family",
        "spy×family",
        "dan da dan",
        "dandadan",
        "bleach",
        "blue lock",
        "bluelock",
        "attack on titan",
        "shingeki no kyojin",
        "classroom of the elite",
        "my hero academia",
        "boku no hero academia",
        "that time i got reincarnated as a slime",
        "tensei shitara slime datta ken",
        "welcome to demon school! iruma-kun",
        "mairimashita! iruma-kun",
        "the seven deadly sins",
        "nanatsu no taizai",
        "cells at work",
        "hataraku saibou",
        "made in abyss",
        "konosuba",
        "kono subarashii sekai",
        "jujutsu kaisen",
        "dr. stone",
        "dr.stone",
        "tsukimichi",
        "kuroko's basketball",
        "kuroko no basket",
        "food wars",
        "shokugeki no souma",
        "fairy tail",
        "haikyu",
        "yu yu hakusho",
        "yuu yuu hakusho",
        "sakuna",
        "tensui no sakuna-hime",
        "puniru",
        "shoshimin",
        "shoushimin",
        "bungo stray dogs",
        "bananya",
        "armor shop for ladies",
        "otona no bouguya-san",
        "jojo's bizarre adventure",
        "jojo no kimyou na bouken",
        "trigun",
        "megalobox",
        "megalo box",
        "tis time for torture, princess",
        "himesama \"goumon\" no jikan",
        "anyway, i'm falling in love with you",
        "douse, koishite shimaunda",
        "with vengeance, sincerely, your broken saintess",
        "as a reincarnated aristocrat",
        "tensei kizoku, kantei skill",
        "reincarnated as a sword",
        "tensei shitara ken deshita",
        "sasaki and peeps",
        "sasaki to pii-chan",
        "the most heretical last boss queen",
        "higeki no genkyou to naru",
        "i've been killing slimes for 300 years",
        "slime taoshite 300-nen",
        "ascendance of a bookworm",
        "honzuki no gekokujou",
        "chihayafuru",
        "dorohedoro",
        "kaiju no. 8",
        "kaijuu 8-gou",
        "bang dream",
        "shangri-la frontier",
        "yuru camp",
        "laid-back camp"
    )

    fun extractFranchiseKey(anime: Anime): String {
        val rawLower = "${anime.title} | ${anime.japaneseTitle} | ${anime.slug}".lowercase()
            .replace("’", "'")
            .replace("‘", "'")
            .replace("×", "x")
        for (prefix in KNOWN_ROOT_PREFIXES) {
            if (rawLower.contains(prefix)) {
                return when (prefix) {
                    "boku no hero academia" -> "my hero academia"
                    "tensei shitara slime datta ken" -> "that time i got reincarnated as a slime"
                    "mairimashita! iruma-kun" -> "welcome to demon school! iruma-kun"
                    "nanatsu no taizai" -> "the seven deadly sins"
                    "hataraku saibou" -> "cells at work"
                    "kono subarashii sekai" -> "konosuba"
                    "dr.stone" -> "dr. stone"
                    "kuroko no basket" -> "kuroko's basketball"
                    "shokugeki no souma" -> "food wars"
                    "yuu yuu hakusho" -> "yu yu hakusho"
                    "tensui no sakuna-hime" -> "sakuna"
                    "shoushimin" -> "shoshimin"
                    "otona no bouguya-san" -> "armor shop for ladies"
                    "jojo no kimyou na bouken" -> "jojo's bizarre adventure"
                    "megalo box" -> "megalobox"
                    "himesama \"goumon\" no jikan" -> "tis time for torture, princess"
                    "douse, koishite shimaunda" -> "anyway, i'm falling in love with you"
                    "tensei kizoku, kantei skill" -> "as a reincarnated aristocrat"
                    "tensei shitara ken deshita" -> "reincarnated as a sword"
                    "sasaki to pii-chan" -> "sasaki and peeps"
                    "higeki no genkyou to naru" -> "the most heretical last boss queen"
                    "slime taoshite 300-nen" -> "i've been killing slimes for 300 years"
                    "honzuki no gekokujou" -> "ascendance of a bookworm"
                    "kaijuu 8-gou" -> "kaiju no. 8"
                    "laid-back camp" -> "yuru camp"
                    "spy×family" -> "spy x family"
                    "dandadan" -> "dan da dan"
                    "bluelock" -> "blue lock"
                    "shingeki no kyojin" -> "attack on titan"
                    else -> prefix
                }
            }
        }
        return anime.title.lowercase()
            .replace("’", "'")
            .replace(Regex("\\b(season\\s*\\d+|\\d+(st|nd|rd|th)\\s*season|s\\d+|part\\s*\\d+|cour\\s*\\d+|final\\s*season|final\\s*arc|act\\s*ii.*|ii|iii|iv)\\b", RegexOption.IGNORE_CASE), "")
            .substringBefore(":")
            .substringBefore(" -")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }

    /** Returns Pair(seasonNumber, partNumberNullable) cleanly supporting 2-Part / Multi-Cour anime. */
    fun extractSeasonAndPart(anime: Anime): Pair<Int, Int?> {
        val cleanTitle = anime.title.trim().lowercase()
        val cleanSlug = anime.slug.trim().lowercase()
        val combined = "$cleanTitle $cleanSlug"

        // Explicit Dub split slug marker e.g. dub-PL...--s2p2
        Regex("--s(\\d+)p(\\d+)").find(cleanSlug)?.let { m ->
            val s = m.groupValues[1].toIntOrNull() ?: 1
            val p = m.groupValues[2].toIntOrNull()
            return s to p
        }

        val partNum = Regex("\\b(?:part|cour)\\s*([1-9][0-9]?)\\b")
            .find(combined)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: Regex("\\b([2-9])(?:nd|rd|th)\\s+(?:part|cour)\\b")
                .find(combined)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: when {
                combined.contains("thousand-year blood war") && combined.contains("separation") -> 2
                combined.contains("thousand-year blood war") && combined.contains("conflict") -> 3
                combined.contains("thousand-year blood war") -> 1
                Regex("\\bfinal\\s+chapters\\b").containsMatchIn(combined) -> 3
                else -> null
            }

        val explicitSeason = Regex("(?:season\\s*|\\bs)([1-9][0-9]?)\\b")
            .find(combined)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: Regex("\\b([1-9][0-9]?)(?:st|nd|rd|th)\\s+season\\b")
                .find(combined)?.groupValues?.getOrNull(1)?.toIntOrNull()

        val namedSeason = when {
            explicitSeason != null -> explicitSeason
            Regex("\\bfinal\\s+season\\b").containsMatchIn(combined) && combined.contains("hero academia") -> 8
            Regex("\\bfinal\\s+season\\b").containsMatchIn(combined) && combined.contains("attack on titan") -> 4
            Regex("\\b(second\\s+plate|stone\\s+wars|vs\\.?\\s*u-?20|2nd\\s+season|\\bii\\b)").containsMatchIn(combined) -> 2
            Regex("\\b(new\\s+world|3rd\\s+season|\\biii\\b)").containsMatchIn(combined) -> 3
            Regex("\\b(science\\s+future|4th\\s+season|\\biv\\b)").containsMatchIn(combined) -> 4
            Regex("\\s2$").containsMatchIn(cleanTitle) && partNum == null -> 2
            Regex("\\s3$").containsMatchIn(cleanTitle) && partNum == null -> 3
            Regex("\\s4$").containsMatchIn(cleanTitle) && partNum == null -> 4
            else -> 1
        }
        return namedSeason to partNum
    }

    fun extractSeasonNumber(anime: Anime): Int? {
        val cleanTitle = anime.title.trim().lowercase()
        val cleanSlug = anime.slug.trim().lowercase()
        val combined = "$cleanTitle $cleanSlug"
        Regex("(?:season\\s*|\\bs)([1-9][0-9]?)\\b").find(combined)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { return it }
        Regex("\\b([1-9][0-9]?)(?:st|nd|rd|th)\\s+season\\b").find(combined)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { return it }
        Regex("\\b(?:part|cour)\\s*([2-9]|1[0-9])\\b").find(combined)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { return it }
        if (Regex("\\bfinal\\s+season\\b").containsMatchIn(combined) && combined.contains("hero academia")) return 8
        if (Regex("\\b(second\\s+plate|stone\\s+wars|2nd\\s+season|\\bii\\b)").containsMatchIn(combined) ||
            Regex("\\s2$").containsMatchIn(cleanTitle)
        ) return 2
        if (Regex("\\b(new\\s+world|3rd\\s+season|\\biii\\b)").containsMatchIn(combined) ||
            Regex("\\s3$").containsMatchIn(cleanTitle)
        ) return 3
        if (Regex("\\b(science\\s+future|4th\\s+season|\\biv\\b)").containsMatchIn(combined) ||
            Regex("\\s4$").containsMatchIn(cleanTitle)
        ) return 4
        return null
    }

    private fun extractTrailingNumericId(slugOrId: String): String? =
        Regex("-(\\d{1,8})$").find(slugOrId)?.groupValues?.getOrNull(1)

    fun findFranchiseSeasons(current: Anime, allAnime: List<Anime>): List<Anime> {
        val targetKey = extractFranchiseKey(current)
        val relatedNumericIds = current.relatedIds.mapNotNull { extractTrailingNumericId(it) }.toSet()
        val currentNumericId = extractTrailingNumericId(current.id)

        val matches = allAnime.filter { candidate ->
            if (candidate.isDubIndo != current.isDubIndo) return@filter false
            if (candidate.id == current.id) return@filter true
            if (candidate.id in current.relatedIds || current.id in candidate.relatedIds) return@filter true
            val candNum = extractTrailingNumericId(candidate.id)
            if (candNum != null && candNum in relatedNumericIds) return@filter true
            if (currentNumericId != null && candidate.relatedIds.any { extractTrailingNumericId(it) == currentNumericId }) return@filter true
            targetKey.length >= 4 && extractFranchiseKey(candidate) == targetKey
        }.distinctBy { it.id }

        if (matches.size <= 1) return matches

        return matches.sortedWith(
            compareBy<Anime> { a ->
                val (sNum, _) = extractSeasonAndPart(a)
                when {
                    a.type == AnimeType.TV_SERIES -> sNum
                    else -> 50 + sNum
                }
            }
                .thenBy { a -> extractSeasonAndPart(a).second ?: 1 }
                .thenBy { it.year }
                .thenBy { it.id }
        )
    }

    fun formatSeasonShortLabel(anime: Anime, index: Int): String {
        val (sNum, pNum) = extractSeasonAndPart(anime)
        return when {
            anime.type == AnimeType.MOVIE -> "Movie (${anime.year})"
            anime.type == AnimeType.OVA || anime.type == AnimeType.ONA -> {
                when {
                    anime.title.contains("eris", ignoreCase = true) -> "OVA Spesial"
                    pNum != null -> "OVA S$sNum P$pNum"
                    else -> "OVA / Spesial (${anime.year})"
                }
            }
            pNum != null -> "Season $sNum • Part $pNum"
            sNum > 1 || anime.title.contains("season", ignoreCase = true) -> "Season $sNum"
            else -> "Season ${index + 1}"
        }
    }

    fun findNextSeason(current: Anime, allAnime: List<Anime>): Anime? {
        val list = findFranchiseSeasons(current, allAnime)
        val idx = list.indexOfFirst { it.id == current.id }
        return if (idx >= 0 && idx + 1 < list.size) list[idx + 1] else null
    }
}

enum class ApiConnectionStatus(val code: String, val label: String) {
    DEMO_LOCAL("MODE.DEMO", "Mode Demo Lokal"),
    SYNCING("SYNC.NET", "Menghubungkan API..."),
    CONNECTED("LIVE.API", "Terhubung Kumanime API"),
    OFFLINE_FALLBACK("FALLBACK", "Server Offline (Data Lokal)")
}

data class ApiConfigState(
    val baseUrl: String = "http://10.0.2.2:3000/api/",
    val useLiveApi: Boolean = true,
    val status: ApiConnectionStatus = ApiConnectionStatus.DEMO_LOCAL,
    val lastSyncMessage: String = "Siap terhubung ke kumanime-api",
    val remoteCount: Int = 0
)

data class WatchProgress(
    val animeId: String,
    val episodeId: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val positionSeconds: Int,
    val durationSeconds: Int,
    val updatedAtLabel: String
) {
    val progressFraction: Float
        get() = if (durationSeconds > 0) {
            (positionSeconds.toFloat() / durationSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val remainingMinutes: Int
        get() = ((durationSeconds - positionSeconds).coerceAtLeast(0) / 60)
}

enum class DownloadStatus(val label: String) {
    DOWNLOADING("Mengunduh"),
    PAUSED("Dijeda"),
    COMPLETED("Tersimpan")
}

@Immutable
data class DownloadTask(
    val episodeId: String,
    val animeId: String,
    val animeTitle: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val artTheme: AnimeArtTheme,
    val qualityLabel: String,
    val totalMb: Int,
    val progress: Float,
    val status: DownloadStatus,
    val sizeBytes: Long = 0L,
    val posterUrl: String = "",
    val localPath: String = ""
) {
    val downloadedMb: Int
        get() = (totalMb * progress).toInt()
}

data class CommentItem(
    val id: String,
    val animeId: String,
    val episodeNumber: Int?,
    val author: String,
    val authorHandle: String = "",
    val authorBadge: String,
    val authorEmblem: String = "",
    val authorAvatarUrl: String = "",
    val content: String,
    val timeAgo: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val isSpoiler: Boolean = false,
    val isOwn: Boolean = false,
    val parentId: String? = null
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val animeId: String?,
    val isRead: Boolean,
    val tag: String,
    val posterUrl: String = "",
    val episodeNumber: Int? = null
)

enum class SubtitleSizeOption(val label: String, val scale: Float) {
    SMALL("Kecil", 0.85f),
    NORMAL("Normal", 1.0f),
    LARGE("Besar", 1.22f)
}

enum class VideoAspectMode(val label: String) {
    FIT("Sesuai Layar"),
    FILL("Penuh"),
    ZOOM("Perbesar 16:9")
}

data class UserProfile(
    val displayName: String,
    val handle: String,
    val bio: String,
    val avatarEmblem: String,
    val avatarUrl: String = "",
    val memberTier: String,
    val watchedHours: Int,
    val completedAnimeCount: Int,
    val favoriteGenre: String
)

data class PlaybackSettings(
    val defaultQuality: String = "1080p",
    val defaultServer: String = "SankaStream",
    val defaultSubtitle: String = "Indonesia",
    val subtitleSize: SubtitleSizeOption = SubtitleSizeOption.NORMAL,
    val autoPlayNext: Boolean = true,
    val autoSkipIntro: Boolean = false,
    val episodeNotifications: Boolean = true
)
