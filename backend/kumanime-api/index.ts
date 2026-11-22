import { load } from "cheerio";

const ANILIST_ENDPOINT = "https://graphql.anilist.co";
const CUSTOM_SOURCE_URL = (Bun.env.SOURCE_URL ?? "").trim().replace(/\/+$/, "");
const CACHE_TTL_MS = 5 * 60 * 1000;

interface CacheEntry<T> {
  expiresAt: number;
  data: T;
}

const cache = new Map<string, CacheEntry<unknown>>();

function getCached<T>(key: string): T | null {
  const hit = cache.get(key);
  if (!hit) return null;
  if (Date.now() > hit.expiresAt) {
    cache.delete(key);
    return null;
  }
  return hit.data as T;
}

function setCached<T>(key: string, data: T, ttlMs = CACHE_TTL_MS): T {
  cache.set(key, { expiresAt: Date.now() + ttlMs, data });
  return data;
}

const CORS_HEADERS: Record<string, string> = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Content-Type": "application/json; charset=utf-8",
};

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: CORS_HEADERS,
  });
}

function slugify(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 64);
}

function makeAnimeSlug(title: string, id: number): string {
  const base = slugify(title) || "anime";
  return `${base}-${id}`;
}

function extractIdFromSlug(slug: string): number | null {
  const trimmed = slug.trim();
  if (/^\d+$/.test(trimmed)) return Number(trimmed);
  const epMatch = trimmed.match(/^ep-(\d+)-(\d+)$/i);
  if (epMatch) return Number(epMatch[1]);
  const trailing = trimmed.match(/-(\d+)$/);
  if (trailing) return Number(trailing[1]);
  return null;
}

function cleanDescription(raw?: string | null): string {
  if (!raw) return "Sinopsis resmi anime tersedia melalui katalog live AnimWatch.";
  return raw
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<\/?[^>]+(>|$)/g, "")
    .replace(/&quot;/g, '"')
    .replace(/&#039;|&apos;/g, "'")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

function formatWibSchedule(airingAtSec?: number | null): { day: string | null; time: string | null } {
  if (!airingAtSec) return { day: null, time: null };
  const wibDate = new Date((airingAtSec + 7 * 3600) * 1000);
  const days = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];
  const day = days[wibDate.getUTCDay()] ?? null;
  const hh = String(wibDate.getUTCHours()).padStart(2, "0");
  const mm = String(wibDate.getUTCMinutes()).padStart(2, "0");
  return { day, time: `${hh}:${mm} WIB` };
}

function formatCount(n?: number | null): string {
  if (!n || n <= 0) return "Live";
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000) return `${Math.round(n / 1_000)}K`;
  return String(n);
}

function mapFormat(format?: string | null): string {
  switch ((format ?? "").toUpperCase()) {
    case "MOVIE":
      return "Movie";
    case "OVA":
      return "OVA";
    case "ONA":
      return "ONA";
    case "SPECIAL":
      return "OVA";
    default:
      return "TV Series";
  }
}

function mapStatus(status?: string | null): string {
  switch ((status ?? "").toUpperCase()) {
    case "RELEASING":
      return "ONGOING";
    case "FINISHED":
      return "COMPLETED";
    case "NOT_YET_RELEASED":
      return "UPCOMING";
    default:
      return "ONGOING";
  }
}

function mapSeason(season?: string | null, year?: number | null): string {
  const s = (season ?? "").toUpperCase();
  const label =
    s === "WINTER"
      ? "Winter"
      : s === "SPRING"
      ? "Spring"
      : s === "SUMMER"
      ? "Summer"
      : s === "FALL"
      ? "Fall"
      : "Musim";
  return year ? `${label} ${year}` : label;
}

function buildTrailerEmbed(trailer?: { id?: string | null; site?: string | null } | null): string {
  if (!trailer?.id) return "";
  const site = (trailer.site ?? "").toLowerCase();
  if (site === "youtube") {
    return `https://www.youtube.com/embed/${trailer.id}?rel=0&modestbranding=1`;
  }
  if (site === "dailymotion") {
    return `https://www.dailymotion.com/embed/video/${trailer.id}`;
  }
  return "";
}

async function queryAniList<T>(query: string, variables: Record<string, unknown> = {}): Promise<T> {
  const res = await fetch(ANILIST_ENDPOINT, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
      "User-Agent": "AnimWatch-KumanimeAPI/2.0",
    },
    body: JSON.stringify({ query, variables }),
  });

  if (!res.ok) {
    throw new Error(`AniList HTTP ${res.status}`);
  }
  const payload = (await res.json()) as { data?: T; errors?: Array<{ message: string }> };
  if (payload.errors?.length) {
    throw new Error(payload.errors.map((e) => e.message).join("; "));
  }
  if (!payload.data) {
    throw new Error("AniList returned empty data");
  }
  return payload.data;
}

interface AniListMedia {
  id: number;
  title?: { romaji?: string | null; english?: string | null; native?: string | null };
  coverImage?: { extraLarge?: string | null; large?: string | null };
  bannerImage?: string | null;
  description?: string | null;
  averageScore?: number | null;
  popularity?: number | null;
  episodes?: number | null;
  duration?: number | null;
  seasonYear?: number | null;
  startDate?: { year?: number | null } | null;
  season?: string | null;
  status?: string | null;
  format?: string | null;
  genres?: string[] | null;
  studios?: { nodes?: Array<{ name?: string | null }> | null } | null;
  nextAiringEpisode?: { episode?: number | null; airingAt?: number | null } | null;
  trailer?: { id?: string | null; site?: string | null } | null;
  streamingEpisodes?: Array<{
    title?: string | null;
    thumbnail?: string | null;
    url?: string | null;
    site?: string | null;
  }> | null;
  characters?: {
    edges?: Array<{
      role?: string | null;
      node?: { name?: { full?: string | null } | null } | null;
      voiceActors?: Array<{ name?: { full?: string | null } | null }> | null;
    }> | null;
  } | null;
  relations?: {
    edges?: Array<{
      relationType?: string | null;
      node?: {
        id: number;
        type?: string | null;
        title?: { romaji?: string | null; english?: string | null } | null;
      } | null;
    }> | null;
  } | null;
}

const MEDIA_CARD_FIELDS = `
  id
  title { romaji english native }
  coverImage { extraLarge large }
  bannerImage
  description(asHtml: false)
  averageScore
  popularity
  episodes
  duration
  seasonYear
  startDate { year }
  season
  status
  format
  genres
  studios(isMain: true) { nodes { name } }
  nextAiringEpisode { episode airingAt }
  trailer { id site }
`;

function pickTitle(m: AniListMedia): string {
  return m.title?.english?.trim() || m.title?.romaji?.trim() || m.title?.native?.trim() || `Anime #${m.id}`;
}

function mapMediaCommon(m: AniListMedia) {
  const title = pickTitle(m);
  const slug = makeAnimeSlug(m.title?.romaji || title, m.id);
  const poster = m.coverImage?.extraLarge || m.coverImage?.large || "";
  const banner = m.bannerImage || poster;
  const rating = m.averageScore ? Number((m.averageScore / 10).toFixed(1)) : 8.2;
  const status = mapStatus(m.status);
  const nextEp = m.nextAiringEpisode?.episode ?? null;
  const totalEpisodes =
    m.episodes && m.episodes > 0
      ? m.episodes
      : nextEp && nextEp > 1
      ? Math.max(12, nextEp)
      : m.format === "MOVIE"
      ? 1
      : 12;
  const releasedEpisodes =
    status === "COMPLETED"
      ? totalEpisodes
      : nextEp && nextEp > 1
      ? nextEp - 1
      : Math.min(totalEpisodes, 1);
  const year = m.seasonYear || m.startDate?.year || 2026;
  const studio = m.studios?.nodes?.[0]?.name?.trim() || "Studio Anime";
  const schedule = formatWibSchedule(m.nextAiringEpisode?.airingAt);
  const genres = (m.genres ?? []).filter(Boolean);

  return {
    id: m.id,
    title,
    japanese_title: m.title?.native || m.title?.romaji || title,
    slug,
    poster,
    banner,
    synopsis: cleanDescription(m.description),
    genres: genres.length ? genres : ["Anime", "Sub Indo"],
    rating,
    rating_count: formatCount(m.popularity),
    views_label: formatCount(m.popularity),
    studio,
    status,
    type: mapFormat(m.format),
    release: String(year),
    year,
    season: mapSeason(m.season, year),
    duration: `${m.duration || (m.format === "MOVIE" ? 105 : 24)} menit`,
    duration_minutes: m.duration || (m.format === "MOVIE" ? 105 : 24),
    total_episodes: totalEpisodes,
    released_episodes: releasedEpisodes,
    episode: `Ep ${releasedEpisodes}`,
    latest_eps_slug: `ep-${m.id}-${releasedEpisodes}`,
    airing_day: schedule.day,
    airing_time: schedule.time,
    trailer_url: buildTrailerEmbed(m.trailer),
  };
}

// ======================================================
// Optional Custom SOURCE_URL Scraper (MastayY/kumanime-api)
// ======================================================

const SCRAPE_HEADERS = {
  "User-Agent":
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
  "Accept-Language": "id-ID,id;q=0.9,en-US;q=0.8",
};

async function tryScrapeCustomPopular(): Promise<unknown[] | null> {
  if (!CUSTOM_SOURCE_URL) return null;
  try {
    const res = await fetch(CUSTOM_SOURCE_URL, { headers: SCRAPE_HEADERS, signal: AbortSignal.timeout(7000) });
    if (!res.ok) return null;
    const $ = load(await res.text());
    const data: unknown[] = [];
    $("#content-wrap > .nganan > .menu > table").each((_, el) => {
      const href = $(el).find("tbody > tr > .zvidesc > a").attr("href") ?? "";
      const slug = href.match(/\/([^/]+)\/$/)?.[1] ?? "";
      const title = $(el).find("tbody > tr > .zvidesc > a").text().trim();
      const rawImg = $(el).find("tbody > tr > .zvithumb > a > img").attr("src") ?? "";
      if (slug && title) {
        data.push({
          title,
          poster: rawImg.startsWith("http") ? rawImg : `${CUSTOM_SOURCE_URL}${rawImg}`,
          slug,
          genres: ($(el).find("tbody > tr > td.zvidesc").text().match(/[A-Z][\w ]+(?=,|\s|$)/g) ?? []).slice(1),
        });
      }
    });
    return data.length > 0 ? data : null;
  } catch {
    return null;
  }
}

async function tryScrapeCustomLatest(page: number): Promise<unknown[] | null> {
  if (!CUSTOM_SOURCE_URL) return null;
  try {
    const res = await fetch(`${CUSTOM_SOURCE_URL}/page/${page}/`, {
      headers: SCRAPE_HEADERS,
      signal: AbortSignal.timeout(7000),
    });
    if (!res.ok) return null;
    const $ = load(await res.text());
    const data: unknown[] = [];
    $("#content-wrap > div.ngiri > div.menu > a").each((_, el) => {
      const href = $(el).attr("href") ?? "";
      const slug = href.match(/\/([^/]+)\/$/)?.[1] ?? "";
      const title = $(el).find("div > p").text().trim();
      const poster = $(el).find("div > img").attr("data-original") || $(el).find("div > img").attr("src") || "";
      const episode = $(el).find("span.eps").text().trim();
      if (slug && title) {
        data.push({
          title,
          slug,
          poster: poster.startsWith("http") ? poster : `${CUSTOM_SOURCE_URL}${poster}`,
          episode,
        });
      }
    });
    return data.length > 0 ? data : null;
  } catch {
    return null;
  }
}

// ======================================================
// Live Route Handlers
// ======================================================

async function handlePopular(): Promise<Response> {
  const key = "popular";
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const customData = await tryScrapeCustomPopular();
  if (customData) {
    return jsonResponse(setCached(key, { status: "success", source: "custom", data: customData }));
  }

  const query = `
    query {
      Page(page: 1, perPage: 20) {
        media(type: ANIME, sort: [TRENDING_DESC, POPULARITY_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query);
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", data }));
}

async function handleLatest(page: number): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `latest:${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const customData = await tryScrapeCustomLatest(safePage);
  if (customData) {
    return jsonResponse(setCached(key, { status: "success", source: "custom", data: customData }));
  }

  const query = `
    query ($page: Int) {
      Page(page: $page, perPage: 22) {
        media(type: ANIME, status: RELEASING, sort: [POPULARITY_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { page: safePage });
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", data }));
}

async function handleMovies(page: number): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `movies:${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const query = `
    query ($page: Int) {
      Page(page: $page, perPage: 16) {
        media(type: ANIME, format: MOVIE, sort: [POPULARITY_DESC, SCORE_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { page: safePage });
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", data }));
}

async function handleCompleted(page: number): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `completed:${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const query = `
    query ($page: Int) {
      Page(page: $page, perPage: 16) {
        media(type: ANIME, status: FINISHED, format: TV, sort: [SCORE_DESC, POPULARITY_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { page: safePage });
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", data }));
}

async function handleSearch(rawQuery: string): Promise<Response> {
  const q = decodeURIComponent(rawQuery).trim();
  if (!q) {
    return jsonResponse({ status: "success", query: "", data: [] });
  }
  const key = `search:${q.toLowerCase()}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const query = `
    query ($search: String) {
      Page(page: 1, perPage: 20) {
        media(type: ANIME, search: $search, sort: [SEARCH_MATCH, POPULARITY_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { search: q });
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", query: q, data }, 2 * 60 * 1000));
}

async function fetchAniListMediaDetail(slugOrId: string): Promise<AniListMedia | null> {
  const id = extractIdFromSlug(slugOrId);
  const detailFields = `
    ${MEDIA_CARD_FIELDS}
    streamingEpisodes { title thumbnail url site }
    characters(sort: [ROLE, RELEVANCE], perPage: 8) {
      edges {
        role
        node { name { full } }
        voiceActors(language: JAPANESE, sort: RELEVANCE) { name { full } }
      }
    }
    relations {
      edges {
        relationType
        node {
          id
          type
          title { romaji english }
        }
      }
    }
  `;

  if (id) {
    const query = `
      query ($id: Int) {
        Media(id: $id, type: ANIME) {
          ${detailFields}
        }
      }
    `;
    const res = await queryAniList<{ Media: AniListMedia | null }>(query, { id });
    return res.Media;
  }

  const searchTerm = decodeURIComponent(slugOrId).replace(/-/g, " ").trim();
  const query = `
    query ($search: String) {
      Media(search: $search, type: ANIME) {
        ${detailFields}
      }
    }
  `;
  const res = await queryAniList<{ Media: AniListMedia | null }>(query, { search: searchTerm });
  return res.Media;
}

async function handleAnimeDetail(slug: string): Promise<Response> {
  const key = `anime:${slug.toLowerCase()}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const media = await fetchAniListMediaDetail(slug);
  if (!media) {
    return jsonResponse({ status: "error", message: `Anime tidak ditemukan: ${slug}` }, 404);
  }

  const common = mapMediaCommon(media);
  const streamEps = (media.streamingEpisodes ?? []).filter((e) => e?.title);
  const episodeCount = Math.max(common.released_episodes, Math.min(streamEps.length, 48), 1);
  const cappedCount = Math.min(episodeCount, 48);

  const episodes = Array.from({ length: cappedCount }, (_, i) => {
    const epNum = i + 1;
    const matched = streamEps[i];
    const cleanEpTitle = matched?.title
      ? matched.title.replace(/^Episode\s+\d+\s*[-:]\s*/i, "").trim() || `Episode ${epNum}`
      : `Episode ${epNum}`;
    return {
      eps_title: `Episode ${epNum}: ${cleanEpTitle}`,
      eps_slug: `ep-${media.id}-${epNum}`,
      number: epNum,
      thumbnail: matched?.thumbnail || common.poster,
      external_url: matched?.url || "",
    };
  });

  const characters = (media.characters?.edges ?? [])
    .map((edge) => {
      const name = edge.node?.name?.full?.trim();
      if (!name) return null;
      const role = edge.role === "MAIN" ? "Karakter Utama" : "Karakter Pendukung";
      const va = edge.voiceActors?.[0]?.name?.full?.trim() || "Seiyuu Jepang";
      return {
        name,
        role,
        voice_actor: va,
        emblem: name.slice(0, 1).toUpperCase(),
      };
    })
    .filter(Boolean);

  const relatedSlugs = (media.relations?.edges ?? [])
    .filter((edge) => edge.node?.type === "ANIME" && edge.node.id)
    .slice(0, 6)
    .map((edge) => {
      const t = edge.node?.title?.romaji || edge.node?.title?.english || `anime-${edge.node?.id}`;
      return makeAnimeSlug(t, edge.node!.id);
    });

  const payload = {
    status: "success",
    source: "anilist-live",
    animeData: {
      ...common,
      episodes,
      characters,
      related_slugs: relatedSlugs,
    },
  };

  return jsonResponse(setCached(key, payload));
}

async function handleEpisodeDetail(slug: string): Promise<Response> {
  const key = `episode:${slug.toLowerCase()}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const epMatch = slug.trim().match(/^ep-(\d+)-(\d+)$/i);
  const animeId = epMatch ? Number(epMatch[1]) : extractIdFromSlug(slug);
  const epNum = epMatch ? Number(epMatch[2]) : 1;

  const media = await fetchAniListMediaDetail(animeId ? String(animeId) : slug);
  if (!media) {
    return jsonResponse({ status: "error", message: `Episode tidak ditemukan: ${slug}` }, 404);
  }

  const common = mapMediaCommon(media);
  const streamEps = media.streamingEpisodes ?? [];
  const matched = streamEps[epNum - 1];
  const epTitle = matched?.title?.trim() || `${common.title} — Episode ${epNum}`;
  const maxEp = Math.max(common.released_episodes, streamEps.length, epNum);

  const payload = {
    status: "success",
    source: "anilist-live",
    eps_detail: {
      title: epTitle,
      poster: matched?.thumbnail || common.poster,
      stream_url: common.trailer_url || "",
      synopsis: common.synopsis,
      anime_slug: common.slug,
      next_eps_slug: epNum < maxEp ? `ep-${media.id}-${epNum + 1}` : "-",
      prev_eps_slug: epNum > 1 ? `ep-${media.id}-${epNum - 1}` : "-",
    },
  };

  return jsonResponse(setCached(key, payload));
}

const port = Number(Bun.env.PORT ?? 3000);

Bun.serve({
  port,
  async fetch(req: Request): Promise<Response> {
    if (req.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    const url = new URL(req.url);
    const path = url.pathname.replace(/\/+$/, "") || "/";

    try {
      if (path === "/" || path === "/api") {
        return jsonResponse({
          status: "ok",
          service: "AnimWatch Kumanime API",
          mode: CUSTOM_SOURCE_URL ? "custom-source+anilist-fallback" : "anilist-live",
          routes: {
            latestAnime: "/api/latest",
            popularAnime: "/api/popular",
            movieAnime: "/api/movie",
            completedAnime: "/api/completed",
            search: "/api/search/:query",
            detailAnime: "/api/anime/:slug",
            detailEpisode: "/api/episode/:slug",
          },
        });
      }

      if (path === "/health") {
        return jsonResponse({ status: "ok", timestamp: new Date().toISOString() });
      }

      if (path === "/api/popular") {
        return await handlePopular();
      }

      if (path === "/api/latest") {
        return await handleLatest(1);
      }

      const latestPageMatch = path.match(/^\/api\/latest\/page\/(\d+)$/);
      if (latestPageMatch) {
        return await handleLatest(Number(latestPageMatch[1]));
      }

      if (path === "/api/movie") {
        return await handleMovies(1);
      }

      const moviePageMatch = path.match(/^\/api\/movie\/page\/(\d+)$/);
      if (moviePageMatch) {
        return await handleMovies(Number(moviePageMatch[1]));
      }

      if (path === "/api/completed") {
        return await handleCompleted(1);
      }

      const completedPageMatch = path.match(/^\/api\/completed\/page\/(\d+)$/);
      if (completedPageMatch) {
        return await handleCompleted(Number(completedPageMatch[1]));
      }

      const searchMatch = path.match(/^\/api\/search\/(.+)$/);
      if (searchMatch) {
        return await handleSearch(searchMatch[1]);
      }

      const animeMatch = path.match(/^\/api\/anime\/([^/]+)$/);
      if (animeMatch) {
        return await handleAnimeDetail(animeMatch[1]);
      }

      const episodeMatch = path.match(/^\/api\/episode\/([^/]+)$/);
      if (episodeMatch) {
        return await handleEpisodeDetail(episodeMatch[1]);
      }

      return jsonResponse({ status: "not_found", message: `Unknown route: ${path}` }, 404);
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      return jsonResponse({ status: false, message }, 502);
    }
  },
});

console.log(`AnimWatch Kumanime API listening on port ${port}`);
