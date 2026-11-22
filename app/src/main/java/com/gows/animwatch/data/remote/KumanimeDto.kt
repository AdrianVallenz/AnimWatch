package com.gows.animwatch.data.remote

import com.google.gson.annotations.SerializedName

data class LatestResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("data") val data: List<LatestItemDto>? = null
)

data class LatestItemDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("episode") val episode: String? = null
)

data class PopularResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("data") val data: List<PopularItemDto>? = null
)

data class PopularItemDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("genres") val genres: List<String>? = null
)

data class MovieResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("data") val data: List<CatalogCardDto>? = null
)

data class SearchResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("query") val query: String? = null,
    @SerializedName("data") val data: List<CatalogCardDto>? = null
)

data class CatalogCardDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("release") val release: String? = null,
    @SerializedName("duration") val duration: String? = null
)

data class AnimeDetailResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("animeData") val animeData: AnimeDetailDataDto? = null
)

data class AnimeDetailDataDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("episodes") val episodes: List<AnimeDetailEpisodeDto>? = null
)

data class AnimeDetailEpisodeDto(
    @SerializedName("eps_title") val epsTitle: String? = null,
    @SerializedName("eps_slug") val epsSlug: String? = null
)

data class EpisodeDetailResponseDto(
    @SerializedName("status") val status: String? = null,
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
