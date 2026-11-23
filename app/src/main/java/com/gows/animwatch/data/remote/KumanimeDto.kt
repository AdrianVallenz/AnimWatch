package com.gows.animwatch.data.remote

import com.google.gson.annotations.SerializedName

data class LatestResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("data") val data: List<LatestItemDto>? = null
)

data class LatestItemDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("japanese_title") val japaneseTitle: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("banner") val banner: String? = null,
    @SerializedName("episode") val episode: String? = null,
    @SerializedName("latest_eps_slug") val latestEpsSlug: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("rating_count") val ratingCount: String? = null,
    @SerializedName("views_label") val viewsLabel: String? = null,
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("total_episodes") val totalEpisodes: Int? = null,
    @SerializedName("released_episodes") val releasedEpisodes: Int? = null,
    @SerializedName("airing_day") val airingDay: String? = null,
    @SerializedName("airing_time") val airingTime: String? = null,
    @SerializedName("trailer_url") val trailerUrl: String? = null,
    @SerializedName("is_official_free") val isOfficialFree: Boolean? = null,
    @SerializedName("is_dub_indo") val isDubIndo: Boolean? = null,
    @SerializedName("audio_lang") val audioLang: String? = null,
    @SerializedName("official_channel") val officialChannel: String? = null,
    @SerializedName("stream_type") val streamType: String? = null,
    @SerializedName("external_links") val externalLinks: List<ExternalLinkDto>? = null
)

data class PopularResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("data") val data: List<PopularItemDto>? = null
)

data class PopularItemDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("japanese_title") val japaneseTitle: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("banner") val banner: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("rating_count") val ratingCount: String? = null,
    @SerializedName("views_label") val viewsLabel: String? = null,
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("total_episodes") val totalEpisodes: Int? = null,
    @SerializedName("released_episodes") val releasedEpisodes: Int? = null,
    @SerializedName("airing_day") val airingDay: String? = null,
    @SerializedName("airing_time") val airingTime: String? = null,
    @SerializedName("trailer_url") val trailerUrl: String? = null,
    @SerializedName("is_official_free") val isOfficialFree: Boolean? = null,
    @SerializedName("is_dub_indo") val isDubIndo: Boolean? = null,
    @SerializedName("audio_lang") val audioLang: String? = null,
    @SerializedName("official_channel") val officialChannel: String? = null,
    @SerializedName("stream_type") val streamType: String? = null,
    @SerializedName("latest_eps_slug") val latestEpsSlug: String? = null,
    @SerializedName("episodes") val episodes: List<AnimeDetailEpisodeDto>? = null,
    @SerializedName("external_links") val externalLinks: List<ExternalLinkDto>? = null
)

data class ExternalLinkDto(
    @SerializedName("site") val site: String? = null,
    @SerializedName("url") val url: String? = null
)

data class MovieResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("data") val data: List<CatalogCardDto>? = null
)

data class SearchResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("query") val query: String? = null,
    @SerializedName("data") val data: List<CatalogCardDto>? = null
)

data class CatalogCardDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("japanese_title") val japaneseTitle: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("banner") val banner: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("release") val release: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("rating_count") val ratingCount: String? = null,
    @SerializedName("views_label") val viewsLabel: String? = null,
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("total_episodes") val totalEpisodes: Int? = null,
    @SerializedName("released_episodes") val releasedEpisodes: Int? = null,
    @SerializedName("airing_day") val airingDay: String? = null,
    @SerializedName("airing_time") val airingTime: String? = null,
    @SerializedName("trailer_url") val trailerUrl: String? = null,
    @SerializedName("is_official_free") val isOfficialFree: Boolean? = null,
    @SerializedName("is_dub_indo") val isDubIndo: Boolean? = null,
    @SerializedName("audio_lang") val audioLang: String? = null,
    @SerializedName("official_channel") val officialChannel: String? = null,
    @SerializedName("stream_type") val streamType: String? = null,
    @SerializedName("latest_eps_slug") val latestEpsSlug: String? = null,
    @SerializedName("external_links") val externalLinks: List<ExternalLinkDto>? = null
)

data class AnimeDetailResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("animeData") val animeData: AnimeDetailDataDto? = null
)

data class AnimeDetailCharacterDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("voice_actor") val voiceActor: String? = null,
    @SerializedName("emblem") val emblem: String? = null
)

data class AnimeDetailDataDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("japanese_title") val japaneseTitle: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("banner") val banner: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("rating_count") val ratingCount: String? = null,
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("views_label") val viewsLabel: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("total_episodes") val totalEpisodes: Int? = null,
    @SerializedName("released_episodes") val releasedEpisodes: Int? = null,
    @SerializedName("airing_day") val airingDay: String? = null,
    @SerializedName("airing_time") val airingTime: String? = null,
    @SerializedName("trailer_url") val trailerUrl: String? = null,
    @SerializedName("is_official_free") val isOfficialFree: Boolean? = null,
    @SerializedName("is_dub_indo") val isDubIndo: Boolean? = null,
    @SerializedName("audio_lang") val audioLang: String? = null,
    @SerializedName("official_channel") val officialChannel: String? = null,
    @SerializedName("episodes") val episodes: List<AnimeDetailEpisodeDto>? = null,
    @SerializedName("characters") val characters: List<AnimeDetailCharacterDto>? = null,
    @SerializedName("related_slugs") val relatedSlugs: List<String>? = null,
    @SerializedName("external_links") val externalLinks: List<ExternalLinkDto>? = null
)

data class AnimeDetailEpisodeDto(
    @SerializedName("eps_title") val epsTitle: String? = null,
    @SerializedName("eps_slug") val epsSlug: String? = null,
    @SerializedName("number") val number: Int? = null,
    @SerializedName("stream_url") val streamUrl: String? = null,
    @SerializedName("stream_type") val streamType: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null
)

data class EpisodeDetailResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("eps_detail") val epsDetail: EpisodeDetailDataDto? = null
)

data class EpisodeDetailDataDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("stream_url") val streamUrl: String? = null,
    @SerializedName("stream_type") val streamType: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("anime_slug") val animeSlug: String? = null,
    @SerializedName("next_eps_slug") val nextEpsSlug: String? = null,
    @SerializedName("prev_eps_slug") val prevEpsSlug: String? = null
)

// ======================================================
// PostgreSQL Auth, User Sync & Comments DTOs
// ======================================================

data class SimpleStatusResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("message") val message: String? = null
)

data class RegisterRequestDto(
    @SerializedName("displayName") val displayName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class GoogleAuthRequestDto(
    @SerializedName("idToken") val idToken: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("picture") val picture: String? = null,
    @SerializedName("sub") val sub: String? = null
)

data class AuthConfigDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("googleEnabled") val googleEnabled: Boolean? = null,
    @SerializedName("googleWebClientId") val googleWebClientId: String? = null,
    @SerializedName("googleWebOAuthEnabled") val googleWebOAuthEnabled: Boolean? = null
)

data class UserProfileDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("handle") val handle: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("avatarEmblem") val avatarEmblem: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("memberTier") val memberTier: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("appearanceMode") val appearanceMode: String? = null,
    @SerializedName("accentTheme") val accentTheme: String? = null,
    @SerializedName("preferredGenres") val preferredGenres: List<String>? = null,
    @SerializedName("hasCompletedThemeSetup") val hasCompletedThemeSetup: Boolean? = null,
    @SerializedName("settings") val settings: Map<String, Any?>? = null
)

data class WatchHistoryDto(
    @SerializedName("animeId") val animeId: String? = null,
    @SerializedName("episodeId") val episodeId: String? = null,
    @SerializedName("episodeNumber") val episodeNumber: Int? = null,
    @SerializedName("episodeTitle") val episodeTitle: String? = null,
    @SerializedName("positionSeconds") val positionSeconds: Int? = null,
    @SerializedName("durationSeconds") val durationSeconds: Int? = null,
    @SerializedName("updatedAtLabel") val updatedAtLabel: String? = null
)

data class AuthResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: UserProfileDto? = null,
    @SerializedName("watchlist") val watchlist: Map<String, String>? = null,
    @SerializedName("watchedEpisodes") val watchedEpisodes: Map<String, Int>? = null,
    @SerializedName("history") val history: List<WatchHistoryDto>? = null,
    @SerializedName("ratings") val ratings: Map<String, Int>? = null
)

data class UpdateProfileRequestDto(
    @SerializedName("displayName") val displayName: String,
    @SerializedName("handle") val handle: String? = null,
    @SerializedName("bio") val bio: String,
    @SerializedName("avatarEmblem") val avatarEmblem: String,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("memberTier") val memberTier: String? = null,
    @SerializedName("favoriteGenre") val favoriteGenre: String? = null
)

data class UpdatePreferencesRequestDto(
    @SerializedName("appearanceMode") val appearanceMode: String,
    @SerializedName("accentTheme") val accentTheme: String,
    @SerializedName("preferredGenres") val preferredGenres: List<String>,
    @SerializedName("hasCompletedThemeSetup") val hasCompletedThemeSetup: Boolean,
    @SerializedName("settings") val settings: Map<String, Any>
)

data class WatchlistUpsertRequestDto(
    @SerializedName("animeId") val animeId: String,
    @SerializedName("category") val category: String?,
    @SerializedName("watchedEpisodes") val watchedEpisodes: Int? = null
)

data class HistoryUpsertRequestDto(
    @SerializedName("animeId") val animeId: String,
    @SerializedName("episodeId") val episodeId: String,
    @SerializedName("episodeNumber") val episodeNumber: Int,
    @SerializedName("episodeTitle") val episodeTitle: String,
    @SerializedName("positionSeconds") val positionSeconds: Int,
    @SerializedName("durationSeconds") val durationSeconds: Int
)

data class RatingUpsertRequestDto(
    @SerializedName("animeId") val animeId: String,
    @SerializedName("score") val score: Int
)

data class CommentDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("animeId") val animeId: String? = null,
    @SerializedName("episodeNumber") val episodeNumber: Int? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("authorHandle") val authorHandle: String? = null,
    @SerializedName("authorBadge") val authorBadge: String? = null,
    @SerializedName("authorEmblem") val authorEmblem: String? = null,
    @SerializedName("authorAvatarUrl") val authorAvatarUrl: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("timeAgo") val timeAgo: String? = null,
    @SerializedName("likes") val likes: Int? = null,
    @SerializedName("isLiked") val isLiked: Boolean? = null,
    @SerializedName("isSpoiler") val isSpoiler: Boolean? = null,
    @SerializedName("isOwn") val isOwn: Boolean? = null,
    @SerializedName("parentId") val parentId: String? = null
)

data class CommentsListResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("data") val data: List<CommentDto>? = null
)

data class PostCommentRequestDto(
    @SerializedName("animeId") val animeId: String,
    @SerializedName("episodeNumber") val episodeNumber: Int?,
    @SerializedName("content") val content: String,
    @SerializedName("isSpoiler") val isSpoiler: Boolean,
    @SerializedName("parentId") val parentId: String? = null
)

data class PostCommentResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("comment") val comment: CommentDto? = null
)

data class ToggleCommentLikeResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("liked") val liked: Boolean? = null,
    @SerializedName("likes") val likes: Int? = null
)

// ======================================================
// Admin CRUD DTOs
// ======================================================

data class AdminEpisodeDto(
    @SerializedName("eps_title") val epsTitle: String? = null,
    @SerializedName("eps_slug") val epsSlug: String? = null,
    @SerializedName("number") val number: Int? = null,
    @SerializedName("stream_url") val streamUrl: String? = null,
    @SerializedName("stream_type") val streamType: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null
)

data class AdminAnimeDto(
    @SerializedName("slug") val slug: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("japanese_title") val japaneseTitle: String = "",
    @SerializedName("poster") val poster: String = "",
    @SerializedName("banner") val banner: String = "",
    @SerializedName("synopsis") val synopsis: String = "",
    @SerializedName("genres") val genres: List<String> = emptyList(),
    @SerializedName("rating") val rating: Double = 8.5,
    @SerializedName("studio") val studio: String = "AnimWatch Admin",
    @SerializedName("status") val status: String = "ONGOING",
    @SerializedName("type") val type: String = "TV Series",
    @SerializedName("year") val year: Int = 2026,
    @SerializedName("season") val season: String = "2026",
    @SerializedName("total_episodes") val totalEpisodes: Int = 12,
    @SerializedName("released_episodes") val releasedEpisodes: Int = 1,
    @SerializedName("duration_minutes") val durationMinutes: Int = 24,
    @SerializedName("is_official_free") val isOfficialFree: Boolean = true,
    @SerializedName("is_dub_indo") val isDubIndo: Boolean = false,
    @SerializedName("official_channel") val officialChannel: String = "AnimWatch Admin",
    @SerializedName("playlist_id") val playlistId: String = "",
    @SerializedName("anilist_id") val anilistId: Int? = null,
    @SerializedName("episodes") val episodes: List<AdminEpisodeDto> = emptyList(),
    @SerializedName("is_hidden") val isHidden: Boolean = false,
    @SerializedName("is_featured") val isFeatured: Boolean = true
)

data class AdminSaveAnimeRequestDto(
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("japanese_title") val japaneseTitle: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("banner") val banner: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("total_episodes") val totalEpisodes: Int? = null,
    @SerializedName("released_episodes") val releasedEpisodes: Int? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("is_official_free") val isOfficialFree: Boolean? = null,
    @SerializedName("is_dub_indo") val isDubIndo: Boolean? = null,
    @SerializedName("official_channel") val officialChannel: String? = null,
    @SerializedName("playlist_id") val playlistId: String? = null,
    @SerializedName("anilist_id") val anilistId: Int? = null,
    @SerializedName("episodes") val episodes: List<AdminEpisodeDto>? = null,
    @SerializedName("is_hidden") val isHidden: Boolean? = null,
    @SerializedName("is_featured") val isFeatured: Boolean? = null
)

data class AdminSaveAnimeResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("anime") val anime: AdminAnimeDto? = null
)

data class AdminPlaylistDto(
    @SerializedName("playlistId") val playlistId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("cleanTitle") val cleanTitle: String = "",
    @SerializedName("channelName") val channelName: String = "Muse Indonesia",
    @SerializedName("episodeHint") val episodeHint: Int = 12,
    @SerializedName("sampleVideoId") val sampleVideoId: String = "",
    @SerializedName("audioLang") val audioLang: String = "SUB",
    @SerializedName("anilistId") val anilistId: Int? = null
)

data class AdminSavePlaylistRequestDto(
    @SerializedName("playlistId") val playlistId: String,
    @SerializedName("title") val title: String,
    @SerializedName("cleanTitle") val cleanTitle: String? = null,
    @SerializedName("channelName") val channelName: String = "Muse Indonesia",
    @SerializedName("episodeHint") val episodeHint: Int = 12,
    @SerializedName("sampleVideoId") val sampleVideoId: String = "",
    @SerializedName("audioLang") val audioLang: String = "SUB",
    @SerializedName("anilistId") val anilistId: Int? = null
)

data class AdminSavePlaylistResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("playlist") val playlist: AdminPlaylistDto? = null
)

data class AdminCommentDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("animeId") val animeId: String = "",
    @SerializedName("episodeNumber") val episodeNumber: Int? = null,
    @SerializedName("authorName") val authorName: String = "",
    @SerializedName("authorBadge") val authorBadge: String = "Member",
    @SerializedName("content") val content: String = "",
    @SerializedName("isSpoiler") val isSpoiler: Boolean = false,
    @SerializedName("timeLabel") val timeLabel: String = ""
)

data class AdminStatsDto(
    @SerializedName("totalUsers") val totalUsers: Int = 0,
    @SerializedName("totalComments") val totalComments: Int = 0,
    @SerializedName("totalPlaylists") val totalPlaylists: Int = 0,
    @SerializedName("totalAdminAnime") val totalAdminAnime: Int = 0,
    @SerializedName("hiddenAnimeCount") val hiddenAnimeCount: Int = 0,
    @SerializedName("curatedRulesCount") val curatedRulesCount: Int = 0,
    @SerializedName("splitPartRulesCount") val splitPartRulesCount: Int = 0
)

data class AdminVerifyRequestDto(
    @SerializedName("secret") val secret: String = ""
)

data class AdminVerifyResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("isAdmin") val isAdmin: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("user") val user: UserProfileDto? = null
)

data class AdminOverviewResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("stats") val stats: AdminStatsDto? = null,
    @SerializedName("adminAnimes") val adminAnimes: List<AdminAnimeDto>? = null,
    @SerializedName("playlists") val playlists: List<AdminPlaylistDto>? = null,
    @SerializedName("recentComments") val recentComments: List<AdminCommentDto>? = null,
    @SerializedName("recentUsers") val recentUsers: List<UserProfileDto>? = null
)

data class AdminUpdateUserRequestDto(
    @SerializedName("role") val role: String? = null,
    @SerializedName("memberTier") val memberTier: String? = null
)

data class AdminUpdateUserResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("user") val user: UserProfileDto? = null
)
