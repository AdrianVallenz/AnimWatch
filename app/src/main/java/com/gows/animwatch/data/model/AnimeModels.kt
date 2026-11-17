package com.gows.animwatch.data.model

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
    WATCHING("Sedang Ditonton"),
    PLAN_TO_WATCH("Rencana Tonton"),
    COMPLETED("Selesai"),
    ON_HOLD("Ditunda")
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
    ALL("Semua Katalog", "Seluruh koleksi anime Sub Indo"),
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
        primaryBg = Color(0xFF1F0C10),
        secondaryBg = Color(0xFF0A080C),
        accentGlow = Color(0xFFE5B84B),
        kanjiCrest = "鬼刃",
        motif = ArtMotif.KATANA_SLASH
    ),
    CELESTIAL_ASTRAL(
        primaryBg = Color(0xFF121629),
        secondaryBg = Color(0xFF07080F),
        accentGlow = Color(0xFFF8D477),
        kanjiCrest = "星詠",
        motif = ArtMotif.MAGIC_RING
    ),
    ABYSSAL_SHADOW(
        primaryBg = Color(0xFF0E1220),
        secondaryBg = Color(0xFF06060A),
        accentGlow = Color(0xFFE5B84B),
        kanjiCrest = "影王",
        motif = ArtMotif.CROWN_MONOLITH
    ),
    CYBER_PROTOCOL(
        primaryBg = Color(0xFF0C181C),
        secondaryBg = Color(0xFF07090C),
        accentGlow = Color(0xFFF5C95B),
        kanjiCrest = "零式",
        motif = ArtMotif.MECHA_GRID
    ),
    SAKURA_TWILIGHT(
        primaryBg = Color(0xFF21121A),
        secondaryBg = Color(0xFF0A080C),
        accentGlow = Color(0xFFF3C66E),
        kanjiCrest = "桜風",
        motif = ArtMotif.TORII_MOON
    ),
    IMPERIAL_ALCHEMY(
        primaryBg = Color(0xFF1E170A),
        secondaryBg = Color(0xFF080709),
        accentGlow = Color(0xFFFAD776),
        kanjiCrest = "錬金",
        motif = ArtMotif.SOLAR_ECLIPSE
    ),
    PHANTOM_VOYAGE(
        primaryBg = Color(0xFF0E1722),
        secondaryBg = Color(0xFF06080C),
        accentGlow = Color(0xFFE5B84B),
        kanjiCrest = "航海",
        motif = ArtMotif.COMPASS_STAR
    ),
    CHRONO_SANCTUM(
        primaryBg = Color(0xFF181421),
        secondaryBg = Color(0xFF07070B),
        accentGlow = Color(0xFFF0C65E),
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
    val streamUrl: String = "",
    val servers: List<StreamServer> = emptyList(),
    val qualities: List<VideoQuality> = emptyList(),
    val subtitles: List<SubtitleTrack> = emptyList()
)

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
    val artTheme: AnimeArtTheme,
    val posterTag: String,
    val posterUrl: String = "",
    val bannerUrl: String = "",
    val characters: List<CharacterRole> = emptyList(),
    val relatedIds: List<String> = emptyList(),
    val episodes: List<Episode> = emptyList()
) {
    val latestEpisodeNumber: Int
        get() = episodes.maxOfOrNull { it.number } ?: releasedEpisodes
}

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
    val status: DownloadStatus
) {
    val downloadedMb: Int
        get() = (totalMb * progress).toInt()
}

data class CommentItem(
    val id: String,
    val animeId: String,
    val episodeNumber: Int?,
    val author: String,
    val authorBadge: String,
    val content: String,
    val timeAgo: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val isSpoiler: Boolean = false
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val animeId: String?,
    val isRead: Boolean,
    val tag: String
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
    val wifiOnlyDownloads: Boolean = true,
    val episodeNotifications: Boolean = true
)
