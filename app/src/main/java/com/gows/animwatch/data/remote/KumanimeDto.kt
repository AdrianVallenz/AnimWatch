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
    @SerializedName("trailer_url") val trailerUrl: String? = null
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
    @SerializedName("official_channel") val officialChannel: String? = null,
    @SerializedName("stream_type") val streamType: String? = null,
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
    @SerializedName("trailer_url") val trailerUrl: String? = null
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
    @SerializedName("year") val year: Int? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("total_episodes") val totalEpisodes: Int? = null,
    @SerializedName("released_episodes") val releasedEpisodes: Int? = null,
    @SerializedName("airing_day") val airingDay: String? = null,
    @SerializedName("airing_time") val airingTime: String? = null,
    @SerializedName("trailer_url") val trailerUrl: String? = null,
    @SerializedName("is_official_free") val isOfficialFree: Boolean? = null,
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
    @SerializedName("stream_type") val streamType: String? = null
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

data class UserProfileDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("handle") val handle: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("avatarEmblem") val avatarEmblem: String? = null,
    @SerializedName("memberTier") val memberTier: String? = null,
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
    @SerializedName("bio") val bio: String,
    @SerializedName("avatarEmblem") val avatarEmblem: String
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
    @SerializedName("authorBadge") val authorBadge: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("timeAgo") val timeAgo: String? = null,
    @SerializedName("likes") val likes: Int? = null,
    @SerializedName("isLiked") val isLiked: Boolean? = null,
    @SerializedName("isSpoiler") val isSpoiler: Boolean? = null
)

data class CommentsListResponseDto(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("data") val data: List<CommentDto>? = null
)

data class PostCommentRequestDto(
    @SerializedName("animeId") val animeId: String,
    @SerializedName("episodeNumber") val episodeNumber: Int?,
    @SerializedName("content") val content: String,
    @SerializedName("isSpoiler") val isSpoiler: Boolean
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
