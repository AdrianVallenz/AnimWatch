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
    @SerializedName("trailer_url") val trailerUrl: String? = null
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
    @SerializedName("episodes") val episodes: List<AnimeDetailEpisodeDto>? = null,
    @SerializedName("characters") val characters: List<AnimeDetailCharacterDto>? = null,
    @SerializedName("related_slugs") val relatedSlugs: List<String>? = null
)

data class AnimeDetailEpisodeDto(
    @SerializedName("eps_title") val epsTitle: String? = null,
    @SerializedName("eps_slug") val epsSlug: String? = null,
    @SerializedName("number") val number: Int? = null
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
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("anime_slug") val animeSlug: String? = null,
    @SerializedName("next_eps_slug") val nextEpsSlug: String? = null,
    @SerializedName("prev_eps_slug") val prevEpsSlug: String? = null
)
