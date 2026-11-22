import { load } from "cheerio";
import { SQL } from "bun";

const ANILIST_ENDPOINT = "https://graphql.anilist.co";
const CUSTOM_SOURCE_URL = (Bun.env.SOURCE_URL ?? "").trim().replace(/\/+$/, "");
const CACHE_TTL_MS = 5 * 60 * 1000;

// ======================================================
// PostgreSQL Connection & Schema Bootstrap (Railway DB)
// ======================================================

let db: SQL | null = null;
let dbReady = false;
let dbInitError: string | null = null;

async function initDatabase(): Promise<void> {
  const url = (Bun.env.DATABASE_URL ?? "").trim();
  if (!url) {
    dbInitError = "DATABASE_URL not configured";
    console.warn("[DB] DATABASE_URL not set; running in catalog-only mode.");
    return;
  }

  try {
    db = new SQL({
      url,
      max: 8,
      idleTimeout: 30,
      connectionTimeout: 10,
      tls: { rejectUnauthorized: false },
    });

    // Verify connectivity
    await db`SELECT 1`;

    // Schema designed with Postgres best practices:
    // bigint identity PKs, text, timestamptz, boolean, composite keys & explicit FK indexes.
    await db.unsafe(`
      CREATE TABLE IF NOT EXISTS users (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        email TEXT NOT NULL UNIQUE,
        password_hash TEXT NOT NULL,
        display_name TEXT NOT NULL,
        handle TEXT NOT NULL,
        bio TEXT NOT NULL DEFAULT '',
        avatar_emblem TEXT NOT NULL DEFAULT 'AW',
        member_tier TEXT NOT NULL DEFAULT 'Member',
        appearance_mode TEXT NOT NULL DEFAULT 'DARK',
        accent_theme TEXT NOT NULL DEFAULT 'COBALT_SAPPHIRE',
        preferred_genres JSONB NOT NULL DEFAULT '["Action","Fantasy","Sci-Fi"]'::jsonb,
        has_completed_theme_setup BOOLEAN NOT NULL DEFAULT FALSE,
        settings JSONB NOT NULL DEFAULT '{}'::jsonb,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );

      CREATE TABLE IF NOT EXISTS sessions (
        token TEXT PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '90 days')
      );
      CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);

      CREATE TABLE IF NOT EXISTS watchlist (
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        anime_id TEXT NOT NULL,
        category TEXT NOT NULL,
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        PRIMARY KEY (user_id, anime_id)
      );

      CREATE TABLE IF NOT EXISTS watch_history (
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        anime_id TEXT NOT NULL,
        episode_id TEXT NOT NULL,
        episode_number INTEGER NOT NULL,
        episode_title TEXT NOT NULL,
        position_seconds INTEGER NOT NULL DEFAULT 0,
        duration_seconds INTEGER NOT NULL DEFAULT 1440,
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        PRIMARY KEY (user_id, anime_id)
      );
      CREATE INDEX IF NOT EXISTS idx_watch_history_user_updated ON watch_history(user_id, updated_at DESC);

      CREATE TABLE IF NOT EXISTS user_ratings (
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        anime_id TEXT NOT NULL,
        score INTEGER NOT NULL CHECK (score BETWEEN 1 AND 10),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        PRIMARY KEY (user_id, anime_id)
      );

      CREATE TABLE IF NOT EXISTS comments (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        anime_id TEXT NOT NULL,
        episode_number INTEGER,
        author_name TEXT NOT NULL,
        author_badge TEXT NOT NULL DEFAULT 'Member',
        content TEXT NOT NULL,
        is_spoiler BOOLEAN NOT NULL DEFAULT FALSE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );
      CREATE INDEX IF NOT EXISTS idx_comments_anime_created ON comments(anime_id, created_at DESC);
      CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);

      CREATE TABLE IF NOT EXISTS comment_likes (
        comment_id BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        PRIMARY KEY (comment_id, user_id)
      );
      CREATE INDEX IF NOT EXISTS idx_comment_likes_user_id ON comment_likes(user_id);
    `);

    // Seed demo VIP account if not already present so instant Demo VIP login persists to DB
    const existingDemo = await db`SELECT id FROM users WHERE email = 'adrian@animwatch.id' LIMIT 1`;
    if (existingDemo.length === 0) {
      const demoHash = await Bun.password.hash("animwatch123");
      await db`
        INSERT INTO users (email, password_hash, display_name, handle, bio, avatar_emblem, member_tier, has_completed_theme_setup)
        VALUES (
          'adrian@animwatch.id',
          ${demoHash},
          'Adrian Vallenz',
          '@adrian.vip',
          'Penggemar anime aksi, fantasi gelap, dan sinematografi epik.',
          'AV',
          'PRO VIP',
          FALSE
        )
        ON CONFLICT (email) DO NOTHING
      `;
    }

    dbReady = true;
    dbInitError = null;
    console.log("[DB] PostgreSQL connected & schema initialized.");
  } catch (err) {
    dbReady = false;
    dbInitError = err instanceof Error ? err.message : String(err);
    console.error("[DB] Failed to initialize PostgreSQL:", dbInitError);
  }
}

await initDatabase();

// ======================================================
// In-memory TTL Cache for Anime Catalog
// ======================================================

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
  "Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Content-Type": "application/json; charset=utf-8",
};

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: CORS_HEADERS,
  });
}

function generateToken(): string {
  const bytes = new Uint8Array(32);
  crypto.getRandomValues(bytes);
  return Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
}

function makeEmblem(name: string): string {
  const parts = name
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }
  return (name.trim().slice(0, 2) || "AW").toUpperCase();
}

function makeHandle(name: string, email: string): string {
  const base = (name.trim() || email.split("@")[0] || "member")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ".")
    .replace(/^\.+|\.+$/g, "")
    .slice(0, 18);
  return `@${base || "member"}`;
}

function formatRelativeTime(isoOrDate: unknown): string {
  const dt = isoOrDate instanceof Date ? isoOrDate : new Date(String(isoOrDate));
  const diffSec = Math.max(0, Math.floor((Date.now() - dt.getTime()) / 1000));
  if (diffSec < 60) return "Baru saja";
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin} menit lalu`;
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr} jam lalu`;
  const diffDays = Math.floor(diffHr / 24);
  if (diffDays < 30) return `${diffDays} hari lalu`;
  return `${Math.floor(diffDays / 30)} bulan lalu`;
}

async function parseJsonBody<T>(req: Request): Promise<T | null> {
  try {
    return (await req.json()) as T;
  } catch {
    return null;
  }
}

function getBearerToken(req: Request): string | null {
  const header = req.headers.get("authorization") ?? "";
  const match = header.match(/^Bearer\s+(.+)$/i);
  return match ? match[1].trim() : null;
}

interface DbUserRow {
  id: number | string;
  email: string;
  password_hash?: string;
  display_name: string;
  handle: string;
  bio: string;
  avatar_emblem: string;
  member_tier: string;
  appearance_mode: string;
  accent_theme: string;
  preferred_genres: unknown;
  has_completed_theme_setup: boolean;
  settings: unknown;
}

function serializeUser(row: DbUserRow) {
  let genres: string[] = ["Action", "Fantasy", "Sci-Fi"];
  if (Array.isArray(row.preferred_genres)) {
    genres = row.preferred_genres.map(String);
  } else if (typeof row.preferred_genres === "string") {
    try {
      const parsed = JSON.parse(row.preferred_genres);
      if (Array.isArray(parsed)) genres = parsed.map(String);
    } catch {
      // keep default
    }
  }

  let settings: Record<string, unknown> = {};
  if (row.settings && typeof row.settings === "object" && !Array.isArray(row.settings)) {
    settings = row.settings as Record<string, unknown>;
  } else if (typeof row.settings === "string") {
    try {
      const parsed = JSON.parse(row.settings);
      if (parsed && typeof parsed === "object") settings = parsed;
    } catch {
      // keep default
    }
  }

  return {
    id: Number(row.id),
    email: row.email,
    displayName: row.display_name,
    handle: row.handle,
    bio: row.bio ?? "",
    avatarEmblem: row.avatar_emblem ?? "AW",
    memberTier: row.member_tier ?? "Member",
    appearanceMode: row.appearance_mode ?? "DARK",
    accentTheme: row.accent_theme ?? "COBALT_SAPPHIRE",
    preferredGenres: genres,
    hasCompletedThemeSetup: Boolean(row.has_completed_theme_setup),
    settings,
  };
}

async function getAuthenticatedUser(req: Request): Promise<DbUserRow | null> {
  if (!db || !dbReady) return null;
  const token = getBearerToken(req);
  if (!token) return null;

  const rows = await db`
    SELECT u.id, u.email, u.display_name, u.handle, u.bio, u.avatar_emblem, u.member_tier,
           u.appearance_mode, u.accent_theme, u.preferred_genres, u.has_completed_theme_setup, u.settings
    FROM sessions s
    JOIN users u ON u.id = s.user_id
    WHERE s.token = ${token} AND s.expires_at > NOW()
    LIMIT 1
  `;
  return (rows[0] as DbUserRow | undefined) ?? null;
}

async function loadUserSnapshot(userRow: DbUserRow) {
  if (!db) throw new Error("Database not initialized");
  const userId = Number(userRow.id);

  const [watchlistRows, historyRows, ratingRows] = await Promise.all([
    db`SELECT anime_id, category FROM watchlist WHERE user_id = ${userId} ORDER BY updated_at DESC`,
    db`SELECT anime_id, episode_id, episode_number, episode_title, position_seconds, duration_seconds, updated_at
       FROM watch_history WHERE user_id = ${userId} ORDER BY updated_at DESC LIMIT 40`,
    db`SELECT anime_id, score FROM user_ratings WHERE user_id = ${userId}`,
  ]);

  const watchlist: Record<string, string> = {};
  for (const r of watchlistRows as Array<{ anime_id: string; category: string }>) {
    watchlist[r.anime_id] = r.category;
  }

  const ratings: Record<string, number> = {};
  for (const r of ratingRows as Array<{ anime_id: string; score: number }>) {
    ratings[r.anime_id] = Number(r.score);
  }

  const history = (
    historyRows as Array<{
      anime_id: string;
      episode_id: string;
      episode_number: number;
      episode_title: string;
      position_seconds: number;
      duration_seconds: number;
      updated_at: unknown;
    }>
  ).map((h) => ({
    animeId: h.anime_id,
    episodeId: h.episode_id,
    episodeNumber: Number(h.episode_number),
    episodeTitle: h.episode_title,
    positionSeconds: Number(h.position_seconds),
    durationSeconds: Number(h.duration_seconds),
    updatedAtLabel: formatRelativeTime(h.updated_at),
  }));

  return {
    user: serializeUser(userRow),
    watchlist,
    history,
    ratings,
  };
}

// ======================================================
// Auth & User Sync Handlers
// ======================================================

async function handleRegister(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const body = await parseJsonBody<{ displayName?: string; email?: string; password?: string }>(req);
  const name = (body?.displayName ?? "").trim();
  const email = (body?.email ?? "").trim().toLowerCase();
  const password = body?.password ?? "";

  if (name.length < 2) return jsonResponse({ status: false, message: "Nama minimal 2 karakter" }, 400);
  if (!email.includes("@") || !email.includes(".")) {
    return jsonResponse({ status: false, message: "Format email tidak valid" }, 400);
  }
  if (password.length < 6) return jsonResponse({ status: false, message: "Kata sandi minimal 6 karakter" }, 400);

  const existing = await db`SELECT id FROM users WHERE email = ${email} LIMIT 1`;
  if (existing.length > 0) {
    return jsonResponse({ status: false, message: "Email sudah terdaftar. Silakan masuk." }, 409);
  }

  const passwordHash = await Bun.password.hash(password);
  const emblem = makeEmblem(name);
  const handle = makeHandle(name, email);

  const inserted = await db`
    INSERT INTO users (email, password_hash, display_name, handle, bio, avatar_emblem, member_tier, has_completed_theme_setup)
    VALUES (${email}, ${passwordHash}, ${name}, ${handle}, 'Anggota baru AnimWatch.', ${emblem}, 'Member', FALSE)
    RETURNING id, email, display_name, handle, bio, avatar_emblem, member_tier,
              appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
  `;
  const userRow = inserted[0] as DbUserRow;
  const token = generateToken();
  await db`INSERT INTO sessions (token, user_id) VALUES (${token}, ${Number(userRow.id)})`;

  const snapshot = await loadUserSnapshot(userRow);
  return jsonResponse({ status: "success", token, ...snapshot }, 201);
}

async function handleLogin(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const body = await parseJsonBody<{ email?: string; password?: string }>(req);
  const email = (body?.email ?? "").trim().toLowerCase();
  const password = body?.password ?? "";

  if (!email || !password) {
    return jsonResponse({ status: false, message: "Email dan kata sandi wajib diisi" }, 400);
  }

  const rows = await db`
    SELECT id, email, password_hash, display_name, handle, bio, avatar_emblem, member_tier,
           appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
    FROM users WHERE email = ${email} LIMIT 1
  `;
  const userRow = rows[0] as DbUserRow | undefined;
  if (!userRow || !userRow.password_hash) {
    return jsonResponse({ status: false, message: "Akun tidak ditemukan. Silakan daftar terlebih dahulu." }, 401);
  }

  const valid = await Bun.password.verify(password, userRow.password_hash);
  if (!valid) {
    return jsonResponse({ status: false, message: "Kata sandi salah. Silakan periksa kembali." }, 401);
  }

  const token = generateToken();
  await db`INSERT INTO sessions (token, user_id) VALUES (${token}, ${Number(userRow.id)})`;

  const snapshot = await loadUserSnapshot(userRow);
  return jsonResponse({ status: "success", token, ...snapshot });
}

async function handleLogout(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: "success" });
  const token = getBearerToken(req);
  if (token) {
    await db`DELETE FROM sessions WHERE token = ${token}`;
  }
  return jsonResponse({ status: "success" });
}

async function handleGetMe(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Sesi tidak valid atau telah berakhir" }, 401);
  const snapshot = await loadUserSnapshot(user);
  return jsonResponse({ status: "success", ...snapshot });
}

async function handleUpdateProfile(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const body = await parseJsonBody<{ displayName?: string; bio?: string; avatarEmblem?: string }>(req);
  const cleanName = (body?.displayName ?? "").trim() || user.display_name;
  const cleanBio = (body?.bio ?? user.bio).trim();
  const cleanEmblem = ((body?.avatarEmblem ?? user.avatar_emblem).trim().slice(0, 2) || user.avatar_emblem).toUpperCase();

  const updated = await db`
    UPDATE users
    SET display_name = ${cleanName}, bio = ${cleanBio}, avatar_emblem = ${cleanEmblem}, updated_at = NOW()
    WHERE id = ${Number(user.id)}
    RETURNING id, email, display_name, handle, bio, avatar_emblem, member_tier,
              appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
  `;
  return jsonResponse({ status: "success", user: serializeUser(updated[0] as DbUserRow) });
}

async function handleUpdatePreferences(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const body = await parseJsonBody<{
    appearanceMode?: string;
    accentTheme?: string;
    preferredGenres?: string[];
    hasCompletedThemeSetup?: boolean;
    settings?: Record<string, unknown>;
  }>(req);

  const current = serializeUser(user);
  const appearanceMode = body?.appearanceMode ?? current.appearanceMode;
  const accentTheme = body?.accentTheme ?? current.accentTheme;
  const preferredGenres = body?.preferredGenres ?? current.preferredGenres;
  const hasCompletedThemeSetup =
    typeof body?.hasCompletedThemeSetup === "boolean"
      ? body.hasCompletedThemeSetup
      : current.hasCompletedThemeSetup;
  const mergedSettings = body?.settings ? { ...current.settings, ...body.settings } : current.settings;

  const updated = await db`
    UPDATE users
    SET appearance_mode = ${appearanceMode},
        accent_theme = ${accentTheme},
        preferred_genres = ${JSON.stringify(preferredGenres)}::jsonb,
        has_completed_theme_setup = ${hasCompletedThemeSetup},
        settings = ${JSON.stringify(mergedSettings)}::jsonb,
        updated_at = NOW()
    WHERE id = ${Number(user.id)}
    RETURNING id, email, display_name, handle, bio, avatar_emblem, member_tier,
              appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
  `;
  return jsonResponse({ status: "success", user: serializeUser(updated[0] as DbUserRow) });
}

async function handleUpsertWatchlist(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const body = await parseJsonBody<{ animeId?: string; category?: string | null }>(req);
  const animeId = (body?.animeId ?? "").trim();
  if (!animeId) return jsonResponse({ status: false, message: "animeId required" }, 400);

  const category = body?.category?.trim() || null;
  if (!category) {
    await db`DELETE FROM watchlist WHERE user_id = ${Number(user.id)} AND anime_id = ${animeId}`;
  } else {
    await db`
      INSERT INTO watchlist (user_id, anime_id, category, updated_at)
      VALUES (${Number(user.id)}, ${animeId}, ${category}, NOW())
      ON CONFLICT (user_id, anime_id)
      DO UPDATE SET category = EXCLUDED.category, updated_at = NOW()
    `;
  }
  return jsonResponse({ status: "success", animeId, category });
}

async function handleUpsertHistory(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const body = await parseJsonBody<{
    animeId?: string;
    episodeId?: string;
    episodeNumber?: number;
    episodeTitle?: string;
    positionSeconds?: number;
    durationSeconds?: number;
  }>(req);

  const animeId = (body?.animeId ?? "").trim();
  const episodeId = (body?.episodeId ?? "").trim();
  if (!animeId || !episodeId) return jsonResponse({ status: false, message: "animeId & episodeId required" }, 400);

  const epNum = Math.max(1, Number(body?.episodeNumber ?? 1));
  const epTitle = (body?.episodeTitle ?? `Episode ${epNum}`).trim();
  const pos = Math.max(0, Math.floor(Number(body?.positionSeconds ?? 0)));
  const dur = Math.max(1, Math.floor(Number(body?.durationSeconds ?? 1440)));

  await db`
    INSERT INTO watch_history (user_id, anime_id, episode_id, episode_number, episode_title, position_seconds, duration_seconds, updated_at)
    VALUES (${Number(user.id)}, ${animeId}, ${episodeId}, ${epNum}, ${epTitle}, ${pos}, ${dur}, NOW())
    ON CONFLICT (user_id, anime_id)
    DO UPDATE SET
      episode_id = EXCLUDED.episode_id,
      episode_number = EXCLUDED.episode_number,
      episode_title = EXCLUDED.episode_title,
      position_seconds = EXCLUDED.position_seconds,
      duration_seconds = EXCLUDED.duration_seconds,
      updated_at = NOW()
  `;
  return jsonResponse({ status: "success" });
}

async function handleDeleteHistory(req: Request, animeId?: string): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  if (animeId) {
    await db`DELETE FROM watch_history WHERE user_id = ${Number(user.id)} AND anime_id = ${decodeURIComponent(animeId)}`;
  } else {
    await db`DELETE FROM watch_history WHERE user_id = ${Number(user.id)}`;
  }
  return jsonResponse({ status: "success" });
}

async function handleUpsertRating(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const body = await parseJsonBody<{ animeId?: string; score?: number }>(req);
  const animeId = (body?.animeId ?? "").trim();
  const score = Math.min(10, Math.max(1, Math.round(Number(body?.score ?? 0))));
  if (!animeId || score < 1) return jsonResponse({ status: false, message: "Invalid rating" }, 400);

  await db`
    INSERT INTO user_ratings (user_id, anime_id, score, updated_at)
    VALUES (${Number(user.id)}, ${animeId}, ${score}, NOW())
    ON CONFLICT (user_id, anime_id)
    DO UPDATE SET score = EXCLUDED.score, updated_at = NOW()
  `;
  return jsonResponse({ status: "success", animeId, score });
}

async function handleGetComments(req: Request, animeId: string): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: "success", data: [] });
  const cleanAnimeId = decodeURIComponent(animeId).trim();
  const user = await getAuthenticatedUser(req);
  const userId = user ? Number(user.id) : -1;

  const rows = await db`
    SELECT c.id, c.anime_id, c.episode_number, c.author_name, c.author_badge, c.content, c.is_spoiler, c.created_at,
           COUNT(cl.user_id)::int AS likes_count,
           BOOL_OR(cl.user_id = ${userId}) AS is_liked
    FROM comments c
    LEFT JOIN comment_likes cl ON cl.comment_id = c.id
    WHERE c.anime_id = ${cleanAnimeId}
    GROUP BY c.id
    ORDER BY c.created_at DESC
    LIMIT 60
  `;

  const data = (
    rows as Array<{
      id: number | string;
      anime_id: string;
      episode_number: number | null;
      author_name: string;
      author_badge: string;
      content: string;
      is_spoiler: boolean;
      created_at: unknown;
      likes_count: number;
      is_liked: boolean | null;
    }>
  ).map((r) => ({
    id: `db_cmt_${r.id}`,
    animeId: r.anime_id,
    episodeNumber: r.episode_number !== null ? Number(r.episode_number) : null,
    author: r.author_name,
    authorBadge: r.author_badge,
    content: r.content,
    timeAgo: formatRelativeTime(r.created_at),
    likes: Number(r.likes_count ?? 0),
    isLiked: Boolean(r.is_liked),
    isSpoiler: Boolean(r.is_spoiler),
  }));

  return jsonResponse({ status: "success", data });
}

async function handlePostComment(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const body = await parseJsonBody<{
    animeId?: string;
    episodeNumber?: number | null;
    content?: string;
    isSpoiler?: boolean;
  }>(req);

  const animeId = (body?.animeId ?? "").trim();
  const content = (body?.content ?? "").trim();
  if (!animeId || !content) return jsonResponse({ status: false, message: "Komentar tidak boleh kosong" }, 400);

  const epNum = body?.episodeNumber != null ? Number(body.episodeNumber) : null;
  const isSpoiler = Boolean(body?.isSpoiler);

  const inserted = await db`
    INSERT INTO comments (user_id, anime_id, episode_number, author_name, author_badge, content, is_spoiler)
    VALUES (${Number(user.id)}, ${animeId}, ${epNum}, ${user.display_name}, ${user.member_tier}, ${content}, ${isSpoiler})
    RETURNING id, anime_id, episode_number, author_name, author_badge, content, is_spoiler, created_at
  `;
  const r = inserted[0] as {
    id: number | string;
    anime_id: string;
    episode_number: number | null;
    author_name: string;
    author_badge: string;
    content: string;
    is_spoiler: boolean;
    created_at: unknown;
  };

  return jsonResponse(
    {
      status: "success",
      comment: {
        id: `db_cmt_${r.id}`,
        animeId: r.anime_id,
        episodeNumber: r.episode_number !== null ? Number(r.episode_number) : null,
        author: r.author_name,
        authorBadge: r.author_badge,
        content: r.content,
        timeAgo: "Baru saja",
        likes: 0,
        isLiked: false,
        isSpoiler: Boolean(r.is_spoiler),
      },
    },
    201
  );
}

async function handleToggleCommentLike(req: Request, rawId: string): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const numericId = Number(rawId.replace(/^db_cmt_/, ""));
  if (!Number.isFinite(numericId) || numericId <= 0) {
    return jsonResponse({ status: false, message: "Invalid comment id" }, 400);
  }

  const existing = await db`
    SELECT 1 FROM comment_likes WHERE comment_id = ${numericId} AND user_id = ${Number(user.id)} LIMIT 1
  `;
  let liked = false;
  if (existing.length > 0) {
    await db`DELETE FROM comment_likes WHERE comment_id = ${numericId} AND user_id = ${Number(user.id)}`;
    liked = false;
  } else {
    await db`INSERT INTO comment_likes (comment_id, user_id) VALUES (${numericId}, ${Number(user.id)}) ON CONFLICT DO NOTHING`;
    liked = true;
  }

  const countRes = await db`SELECT COUNT(*)::int AS cnt FROM comment_likes WHERE comment_id = ${numericId}`;
  const likes = Number((countRes[0] as { cnt: number })?.cnt ?? 0);
  return jsonResponse({ status: "success", liked, likes });
}

// ======================================================
// Anime Catalog Engine (AniList Live + Optional Custom SOURCE_URL)
// ======================================================

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
    case "SPECIAL":
      return "OVA";
    case "ONA":
      return "ONA";
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
      "User-Agent": "AnimWatch-KumanimeAPI/2.1",
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
    const method = req.method.toUpperCase();

    try {
      if (path === "/" || path === "/api") {
        return jsonResponse({
          status: "ok",
          service: "AnimWatch Kumanime + PostgreSQL API",
          mode: CUSTOM_SOURCE_URL ? "custom-source+anilist-fallback" : "anilist-live",
          database: dbReady ? "connected" : "unavailable",
          routes: {
            catalog: ["/api/popular", "/api/latest", "/api/movie", "/api/completed", "/api/search/:query", "/api/anime/:slug", "/api/episode/:slug"],
            auth: ["POST /api/auth/register", "POST /api/auth/login", "POST /api/auth/logout"],
            user: ["GET /api/user/me", "PATCH /api/user/profile", "PATCH /api/user/preferences", "PUT /api/user/watchlist", "PUT /api/user/history", "DELETE /api/user/history", "PUT /api/user/ratings"],
            comments: ["GET /api/comments/:animeId", "POST /api/comments", "POST /api/comments/:id/like"],
          },
        });
      }

      if (path === "/health") {
        let pgAlive = false;
        if (db && dbReady) {
          try {
            await db`SELECT 1`;
            pgAlive = true;
          } catch {
            pgAlive = false;
          }
        }
        return jsonResponse({
          status: "ok",
          database: pgAlive ? "connected" : "unavailable",
          dbError: pgAlive ? null : dbInitError,
          timestamp: new Date().toISOString(),
        });
      }

      // Auth & User DB Routes
      if (method === "POST" && path === "/api/auth/register") return await handleRegister(req);
      if (method === "POST" && path === "/api/auth/login") return await handleLogin(req);
      if (method === "POST" && path === "/api/auth/logout") return await handleLogout(req);
      if (method === "GET" && path === "/api/user/me") return await handleGetMe(req);
      if (method === "PATCH" && path === "/api/user/profile") return await handleUpdateProfile(req);
      if (method === "PATCH" && path === "/api/user/preferences") return await handleUpdatePreferences(req);
      if (method === "PUT" && path === "/api/user/watchlist") return await handleUpsertWatchlist(req);
      if (method === "PUT" && path === "/api/user/history") return await handleUpsertHistory(req);
      if (method === "DELETE" && path === "/api/user/history") return await handleDeleteHistory(req);
      const delHistMatch = path.match(/^\/api\/user\/history\/(.+)$/);
      if (method === "DELETE" && delHistMatch) return await handleDeleteHistory(req, delHistMatch[1]);
      if (method === "PUT" && path === "/api/user/ratings") return await handleUpsertRating(req);

      // Comments Routes
      if (method === "POST" && path === "/api/comments") return await handlePostComment(req);
      const likeCommentMatch = path.match(/^\/api\/comments\/([^/]+)\/like$/);
      if (method === "POST" && likeCommentMatch) return await handleToggleCommentLike(req, likeCommentMatch[1]);
      const getCommentsMatch = path.match(/^\/api\/comments\/(.+)$/);
      if (method === "GET" && getCommentsMatch) return await handleGetComments(req, getCommentsMatch[1]);

      // Anime Catalog Routes
      if (path === "/api/popular") return await handlePopular();
      if (path === "/api/latest") return await handleLatest(1);
      const latestPageMatch = path.match(/^\/api\/latest\/page\/(\d+)$/);
      if (latestPageMatch) return await handleLatest(Number(latestPageMatch[1]));
      if (path === "/api/movie") return await handleMovies(1);
      const moviePageMatch = path.match(/^\/api\/movie\/page\/(\d+)$/);
      if (moviePageMatch) return await handleMovies(Number(moviePageMatch[1]));
      if (path === "/api/completed") return await handleCompleted(1);
      const completedPageMatch = path.match(/^\/api\/completed\/page\/(\d+)$/);
      if (completedPageMatch) return await handleCompleted(Number(completedPageMatch[1]));
      const searchMatch = path.match(/^\/api\/search\/(.+)$/);
      if (searchMatch) return await handleSearch(searchMatch[1]);
      const animeMatch = path.match(/^\/api\/anime\/([^/]+)$/);
      if (animeMatch) return await handleAnimeDetail(animeMatch[1]);
      const episodeMatch = path.match(/^\/api\/episode\/([^/]+)$/);
      if (episodeMatch) return await handleEpisodeDetail(episodeMatch[1]);

      return jsonResponse({ status: "not_found", message: `Unknown route: ${method} ${path}` }, 404);
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      return jsonResponse({ status: false, message }, 502);
    }
  },
});

console.log(`AnimWatch Kumanime + PostgreSQL API listening on port ${port}`);
