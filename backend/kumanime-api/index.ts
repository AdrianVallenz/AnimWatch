import { load } from "cheerio";
import { SQL } from "bun";

const ANILIST_ENDPOINT = "https://graphql.anilist.co";
const CUSTOM_SOURCE_URL = (Bun.env.SOURCE_URL ?? "").trim().replace(/\/+$/, "");
const CACHE_TTL_MS = 6 * 60 * 1000;

// Official free legal Indonesian anime YouTube channels (public Atom RSS feeds)
const OFFICIAL_YT_CHANNELS = [
  { name: "Muse Indonesia", channelId: "UCxxnxya_32jcKj4yN1_kD7A" },
  { name: "Ani-One Indonesia", channelId: "UCUn0hEbVJc273anl_0ozDMQ" },
];

// ======================================================
// PostgreSQL Connection & Schema Bootstrap (Railway DB)
// ======================================================

let db: SQL | null = null;
let dbReady = false;
let dbInitError: string | null = null;
let lastDbRetryAt = 0;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

async function initDatabase(): Promise<void> {
  lastDbRetryAt = Date.now();
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

    await db`SELECT 1`;

    await db.unsafe(`
CREATE TABLE IF NOT EXISTS users (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, email TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, display_name TEXT NOT NULL, handle TEXT NOT NULL, bio TEXT NOT NULL DEFAULT '', avatar_emblem TEXT NOT NULL DEFAULT 'AW', member_tier TEXT NOT NULL DEFAULT 'Member', role TEXT NOT NULL DEFAULT 'user', appearance_mode TEXT NOT NULL DEFAULT 'DARK', accent_theme TEXT NOT NULL DEFAULT 'COBALT_SAPPHIRE', preferred_genres JSONB NOT NULL DEFAULT '["Action","Fantasy","Sci-Fi"]'::jsonb, has_completed_theme_setup BOOLEAN NOT NULL DEFAULT FALSE, settings JSONB NOT NULL DEFAULT '{}'::jsonb, google_sub TEXT, auth_provider TEXT NOT NULL DEFAULT 'email', avatar_url TEXT NOT NULL DEFAULT '', created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW());
ALTER TABLE users ADD COLUMN IF NOT EXISTS google_sub TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_provider TEXT NOT NULL DEFAULT 'email';
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url TEXT NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN IF NOT EXISTS role TEXT NOT NULL DEFAULT 'user';
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_google_sub ON users(google_sub) WHERE google_sub IS NOT NULL;
CREATE TABLE IF NOT EXISTS sessions (token TEXT PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '90 days'));
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);
CREATE TABLE IF NOT EXISTS watchlist (user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, anime_id TEXT NOT NULL, category TEXT NOT NULL, watched_episodes INTEGER NOT NULL DEFAULT 0, updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), PRIMARY KEY (user_id, anime_id));
ALTER TABLE watchlist ADD COLUMN IF NOT EXISTS watched_episodes INTEGER NOT NULL DEFAULT 0;
CREATE TABLE IF NOT EXISTS watch_history (user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, anime_id TEXT NOT NULL, episode_id TEXT NOT NULL, episode_number INTEGER NOT NULL, episode_title TEXT NOT NULL, position_seconds INTEGER NOT NULL DEFAULT 0, duration_seconds INTEGER NOT NULL DEFAULT 1440, updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), PRIMARY KEY (user_id, anime_id));
CREATE INDEX IF NOT EXISTS idx_watch_history_user_updated ON watch_history(user_id, updated_at DESC);
CREATE TABLE IF NOT EXISTS user_ratings (user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, anime_id TEXT NOT NULL, score INTEGER NOT NULL CHECK (score BETWEEN 1 AND 10), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), PRIMARY KEY (user_id, anime_id));
CREATE TABLE IF NOT EXISTS comments (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, anime_id TEXT NOT NULL, episode_number INTEGER, author_name TEXT NOT NULL, author_badge TEXT NOT NULL DEFAULT 'Member', content TEXT NOT NULL, is_spoiler BOOLEAN NOT NULL DEFAULT FALSE, parent_comment_id BIGINT, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW());
ALTER TABLE comments ADD COLUMN IF NOT EXISTS parent_comment_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_comments_anime_created ON comments(anime_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE TABLE IF NOT EXISTS comment_likes (comment_id BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), PRIMARY KEY (comment_id, user_id));
CREATE INDEX IF NOT EXISTS idx_comment_likes_user_id ON comment_likes(user_id);
CREATE TABLE IF NOT EXISTS official_playlists (playlist_id TEXT PRIMARY KEY, title TEXT NOT NULL, clean_title TEXT NOT NULL, channel_name TEXT NOT NULL, episode_hint INTEGER NOT NULL DEFAULT 12, sample_video_id TEXT NOT NULL DEFAULT '', audio_lang TEXT NOT NULL DEFAULT 'SUB', anilist_id INTEGER);
ALTER TABLE official_playlists ADD COLUMN IF NOT EXISTS audio_lang TEXT NOT NULL DEFAULT 'SUB';
ALTER TABLE official_playlists ADD COLUMN IF NOT EXISTS anilist_id INTEGER;
CREATE TABLE IF NOT EXISTS admin_anime (slug TEXT PRIMARY KEY, title TEXT NOT NULL, japanese_title TEXT NOT NULL DEFAULT '', poster TEXT NOT NULL DEFAULT '', banner TEXT NOT NULL DEFAULT '', synopsis TEXT NOT NULL DEFAULT '', genres JSONB NOT NULL DEFAULT '["Anime"]'::jsonb, rating DOUBLE PRECISION NOT NULL DEFAULT 8.5, studio TEXT NOT NULL DEFAULT 'Studio Anime', status TEXT NOT NULL DEFAULT 'ONGOING', type TEXT NOT NULL DEFAULT 'TV Series', year INTEGER NOT NULL DEFAULT 2026, season TEXT NOT NULL DEFAULT '2026', total_episodes INTEGER NOT NULL DEFAULT 12, released_episodes INTEGER NOT NULL DEFAULT 1, duration_minutes INTEGER NOT NULL DEFAULT 24, is_official_free BOOLEAN NOT NULL DEFAULT TRUE, is_dub_indo BOOLEAN NOT NULL DEFAULT FALSE, official_channel TEXT NOT NULL DEFAULT 'AnimWatch Admin', playlist_id TEXT NOT NULL DEFAULT '', anilist_id INTEGER, episodes_json JSONB NOT NULL DEFAULT '[]'::jsonb, is_hidden BOOLEAN NOT NULL DEFAULT FALSE, is_featured BOOLEAN NOT NULL DEFAULT FALSE, updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW());`);

    const existingDemo = await db`SELECT id FROM users WHERE email = 'adrian@animwatch.id' LIMIT 1`;
    if (existingDemo.length === 0) {
      const demoHash = await Bun.password.hash("animwatch123");
      await db`
        INSERT INTO users (email, password_hash, display_name, handle, bio, avatar_emblem, member_tier, role, has_completed_theme_setup)
        VALUES (
          'adrian@animwatch.id',
          ${demoHash},
          'Adrian Vallenz',
          '@adrian.vip',
          'Penggemar anime aksi, fantasi gelap, dan sinematografi epik.',
          'AV',
          'ADMIN',
          'admin',
          FALSE
        )
        ON CONFLICT (email) DO NOTHING
      `;
    } else {
      await db`UPDATE users SET role = 'admin' WHERE email = 'adrian@animwatch.id' AND (role IS NULL OR role <> 'admin')`;
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
// In-Memory TTL Cache
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
  "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Admin-Secret",
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
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length >= 2) return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
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
  avatar_url?: string;
  member_tier: string;
  role?: string;
  appearance_mode: string;
  accent_theme: string;
  preferred_genres: unknown;
  has_completed_theme_setup: boolean;
  settings: unknown;
}

const ADMIN_EMAILS = new Set(
  ("adrian@animwatch.id," + (Bun.env.ADMIN_EMAILS ?? ""))
    .split(",")
    .map((s) => s.trim().toLowerCase())
    .filter(Boolean)
);

function resolveUserRole(row: DbUserRow): string {
  const email = (row.email ?? "").trim().toLowerCase();
  if (row.role === "admin" || ADMIN_EMAILS.has(email)) return "admin";
  return row.role ?? "user";
}

function serializeUser(row: DbUserRow) {
  let genres: string[] = ["Action", "Fantasy", "Sci-Fi"];
  if (Array.isArray(row.preferred_genres)) {
    genres = row.preferred_genres.map(String);
  } else if (typeof row.preferred_genres === "string") {
    try {
      const parsed = JSON.parse(row.preferred_genres);
      if (Array.isArray(parsed)) genres = parsed.map(String);
    } catch {}
  }

  let settings: Record<string, unknown> = {};
  if (row.settings && typeof row.settings === "object" && !Array.isArray(row.settings)) {
    settings = row.settings as Record<string, unknown>;
  } else if (typeof row.settings === "string") {
    try {
      const parsed = JSON.parse(row.settings);
      if (parsed && typeof parsed === "object") settings = parsed;
    } catch {}
  }

  return {
    id: Number(row.id),
    email: row.email,
    displayName: row.display_name,
    handle: row.handle,
    bio: row.bio ?? "",
    avatarEmblem: row.avatar_emblem ?? "AW",
    avatarUrl: row.avatar_url ?? "",
    memberTier: row.member_tier ?? "Member",
    role: resolveUserRole(row),
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
    SELECT u.id, u.email, u.display_name, u.handle, u.bio, u.avatar_emblem, u.avatar_url, u.member_tier, u.role,
           u.appearance_mode, u.accent_theme, u.preferred_genres, u.has_completed_theme_setup, u.settings
    FROM sessions s
    JOIN users u ON u.id = s.user_id
    WHERE s.token = ${token} AND s.expires_at > NOW()
    LIMIT 1
  `;
  return (rows[0] as DbUserRow | undefined) ?? null;
}

async function requireAdmin(req: Request): Promise<{ ok: true; user: DbUserRow | null } | { ok: false; response: Response }> {
  const adminSecret = (Bun.env.ADMIN_SEED_SECRET ?? "").trim();
  const headerSecret = (req.headers.get("x-admin-secret") ?? "").trim();
  if (adminSecret && headerSecret === adminSecret) {
    return { ok: true, user: await getAuthenticatedUser(req) };
  }
  const user = await getAuthenticatedUser(req);
  if (!user) {
    return { ok: false, response: jsonResponse({ status: false, message: "Login admin diperlukan" }, 401) };
  }
  if (resolveUserRole(user) !== "admin") {
    return { ok: false, response: jsonResponse({ status: false, message: "Akses ditolak: khusus Admin" }, 403) };
  }
  return { ok: true, user };
}

async function loadUserSnapshot(userRow: DbUserRow) {
  if (!db) throw new Error("Database not initialized");
  const userId = Number(userRow.id);

  const [watchlistRows, historyRows, ratingRows] = await Promise.all([
    db`SELECT anime_id, category, watched_episodes FROM watchlist WHERE user_id = ${userId} ORDER BY updated_at DESC`,
    db`SELECT anime_id, episode_id, episode_number, episode_title, position_seconds, duration_seconds, updated_at
       FROM watch_history WHERE user_id = ${userId} ORDER BY updated_at DESC LIMIT 40`,
    db`SELECT anime_id, score FROM user_ratings WHERE user_id = ${userId}`,
  ]);

  const watchlist: Record<string, string> = {};
  const watchedEpisodes: Record<string, number> = {};
  for (const r of watchlistRows as any[]) {
    watchlist[r.anime_id] = r.category;
    if (Number(r.watched_episodes) > 0) watchedEpisodes[r.anime_id] = Number(r.watched_episodes);
  }
  const ratings: Record<string, number> = {};
  for (const r of ratingRows as any[]) ratings[r.anime_id] = Number(r.score);
  const history = (historyRows as any[]).map((h) => ({
    animeId: h.anime_id,
    episodeId: h.episode_id,
    episodeNumber: Number(h.episode_number),
    episodeTitle: h.episode_title,
    positionSeconds: Number(h.position_seconds),
    durationSeconds: Number(h.duration_seconds),
    updatedAtLabel: formatRelativeTime(h.updated_at),
  }));
  return { user: serializeUser(userRow), watchlist, watchedEpisodes, history, ratings };
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
  if (!EMAIL_REGEX.test(email)) {
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
    INSERT INTO users (email, password_hash, display_name, handle, bio, avatar_emblem, member_tier, role, has_completed_theme_setup)
    VALUES (${email}, ${passwordHash}, ${name}, ${handle}, 'Anggota baru AnimWatch.', ${emblem}, 'Member', ${ADMIN_EMAILS.has(email) ? 'admin' : 'user'}, FALSE)
    RETURNING id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
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
    SELECT id, email, password_hash, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
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

const GOOGLE_CLIENT_ID = (Bun.env.GOOGLE_CLIENT_ID ?? "").trim();
const GOOGLE_CLIENT_SECRET = (Bun.env.GOOGLE_CLIENT_SECRET ?? "").trim();
const GOOGLE_ALLOWED_AUDIENCES = (Bun.env.GOOGLE_CLIENT_IDS ?? GOOGLE_CLIENT_ID)
  .split(",")
  .map((s) => s.trim())
  .filter(Boolean);

const googleOAuthStates = new Map<string, number>();

function cleanupOAuthStates(): void {
  const now = Date.now();
  for (const [k, exp] of googleOAuthStates.entries()) {
    if (now > exp) googleOAuthStates.delete(k);
  }
}

async function upsertGoogleUserAndCreateSession(profile: {
  sub: string;
  email: string;
  name: string;
  picture?: string;
}): Promise<{ token: string; snapshot: Awaited<ReturnType<typeof loadUserSnapshot>> }> {
  if (!db || !dbReady) throw new Error("Database belum siap");
  const cleanEmail = profile.email.trim().toLowerCase();
  const cleanName = profile.name.trim() || cleanEmail.split("@")[0] || "Google Watcher";
  const cleanPicture = (profile.picture ?? "").trim();

  const bySub = await db`
    SELECT id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
           appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings, google_sub
    FROM users
    WHERE google_sub = ${profile.sub}
    LIMIT 1
  `;
  const existing = bySub.length > 0
    ? bySub
    : await db`
        SELECT id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
               appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings, google_sub
        FROM users
        WHERE email = ${cleanEmail}
        LIMIT 1
      `;

  let userRow: DbUserRow;
  if (existing.length > 0) {
    const found = existing[0] as DbUserRow & { google_sub?: string | null };
    const nextGoogleSub = found.google_sub || profile.sub;
    const updated = await db`
      UPDATE users
      SET google_sub = ${nextGoogleSub},
          auth_provider = CASE WHEN auth_provider = 'email' THEN 'google' ELSE auth_provider END,
          avatar_url = CASE WHEN ${cleanPicture} <> '' AND (avatar_url = '' OR avatar_url IS NULL) THEN ${cleanPicture} ELSE avatar_url END,
          updated_at = NOW()
      WHERE id = ${Number(found.id)}
      RETURNING id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
                appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
    `;
    userRow = updated[0] as DbUserRow;
  } else {
    const randomPass = await Bun.password.hash(`google_${generateToken()}`);
    const emblem = makeEmblem(cleanName);
    const handle = makeHandle(cleanName, cleanEmail);
    const inserted = await db`
      INSERT INTO users (
        email, password_hash, display_name, handle, bio, avatar_emblem, member_tier, role,
        has_completed_theme_setup, google_sub, auth_provider, avatar_url
      )
      VALUES (
        ${cleanEmail}, ${randomPass}, ${cleanName}, ${handle},
        'Terhubung melalui akun Google.', ${emblem}, 'Google Member', ${ADMIN_EMAILS.has(cleanEmail) ? 'admin' : 'user'},
        FALSE, ${profile.sub}, 'google', ${cleanPicture}
      )
      RETURNING id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
                appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
    `;
    userRow = inserted[0] as DbUserRow;
  }

  const token = generateToken();
  await db`INSERT INTO sessions (token, user_id) VALUES (${token}, ${Number(userRow.id)})`;
  const snapshot = await loadUserSnapshot(userRow);
  return { token, snapshot };
}

function handleAuthConfig(): Response {
  return jsonResponse({
    status: "success",
    googleEnabled: GOOGLE_ALLOWED_AUDIENCES.length > 0,
    googleWebClientId: GOOGLE_CLIENT_ID,
    googleWebOAuthEnabled: Boolean(GOOGLE_CLIENT_ID && GOOGLE_CLIENT_SECRET),
  });
}

async function handleGoogleIdTokenAuth(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const body = await parseJsonBody<{
    idToken?: string;
    email?: string;
    name?: string;
    picture?: string;
    sub?: string;
  }>(req);
  const idToken = (body?.idToken ?? "").trim();

  // Direct Google Account Profile Sign-In fallback (when device signs in without OAuth Client ID exchange)
  if (!idToken && body?.email) {
    const rawEmail = String(body.email).trim().toLowerCase();
    if (!EMAIL_REGEX.test(rawEmail)) {
      return jsonResponse({ status: false, message: "Format email akun Google tidak valid" }, 400);
    }
    const rawName = String(body.name ?? "").trim() || rawEmail.split("@")[0];
    const rawSub = String(body.sub ?? "").trim() || `g_mail_${rawEmail}`;
    const rawPic = String(body.picture ?? "").trim();
    const { token, snapshot } = await upsertGoogleUserAndCreateSession({
      sub: rawSub,
      email: rawEmail,
      name: rawName,
      picture: rawPic,
    });
    return jsonResponse({ status: "success", token, ...snapshot });
  }

  if (!idToken) {
    return jsonResponse({ status: false, message: "Google ID token atau email akun Google wajib dikirim" }, 400);
  }

  const verifyRes = await fetch(
    `https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(idToken)}`,
    { signal: AbortSignal.timeout(8000) }
  );
  if (!verifyRes.ok) {
    return jsonResponse({ status: false, message: "Token Google tidak valid atau kedaluwarsa" }, 401);
  }

  const payload = (await verifyRes.json()) as {
    sub?: string;
    email?: string;
    email_verified?: string | boolean;
    name?: string;
    picture?: string;
    aud?: string;
  };

  const verifiedEmail = payload.email_verified === true || payload.email_verified === "true";
  if (!payload.sub || !payload.email || !verifiedEmail) {
    return jsonResponse({ status: false, message: "Email akun Google belum terverifikasi" }, 401);
  }
  if (GOOGLE_ALLOWED_AUDIENCES.length > 0 && (!payload.aud || !GOOGLE_ALLOWED_AUDIENCES.includes(payload.aud))) {
    return jsonResponse({ status: false, message: "Google Client ID tidak cocok dengan server" }, 401);
  }

  const { token, snapshot } = await upsertGoogleUserAndCreateSession({
    sub: payload.sub,
    email: payload.email,
    name: payload.name || payload.email.split("@")[0],
    picture: payload.picture,
  });
  return jsonResponse({ status: "success", token, ...snapshot });
}

function getPublicBaseOrigin(req: Request): string {
  const domain = (Bun.env.RAILWAY_PUBLIC_DOMAIN ?? "").trim();
  if (domain) return `https://${domain}`;
  const url = new URL(req.url);
  return `${url.protocol}//${url.host}`;
}

function htmlRedirectResponse(appDeepLink: string, message: string): Response {
  const safeHref = appDeepLink.replace(/"/g, "&quot;");
  const safeMsg = message.replace(/</g, "&lt;").replace(/>/g, "&gt;");
  const html = `<!DOCTYPE html><html><head><meta charset="utf-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/><title>AnimWatch Google Login</title><meta http-equiv="refresh" content="0;url=${safeHref}"/><style>body{font-family:system-ui,sans-serif;background:#0C0C10;color:#F3F4F6;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0;padding:24px;text-align:center}.card{max-width:360px;background:#171821;border:1px solid rgba(255,255,255,.12);border-radius:20px;padding:24px}a{display:inline-block;margin-top:16px;padding:12px 22px;border-radius:999px;background:#3B82F6;color:#fff;text-decoration:none;font-weight:600}</style></head><body><div class="card"><h2>AnimWatch</h2><p>${safeMsg}</p><a href="${safeHref}">Kembali ke AnimWatch</a></div><script>window.location.replace(${JSON.stringify(appDeepLink)});</script></body></html>`;
  return new Response(html, { status: 200, headers: { "Content-Type": "text/html; charset=utf-8" } });
}

function handleGoogleOAuthStart(req: Request): Response {
  if (GOOGLE_CLIENT_ID && GOOGLE_CLIENT_SECRET) {
    cleanupOAuthStates();
    const state = generateToken().slice(0, 32);
    googleOAuthStates.set(state, Date.now() + 10 * 60 * 1000);
    const redirectUri = `${getPublicBaseOrigin(req)}/api/auth/google/callback`;
    const params = new URLSearchParams({
      client_id: GOOGLE_CLIENT_ID,
      redirect_uri: redirectUri,
      response_type: "code",
      scope: "openid email profile",
      prompt: "select_account",
      state,
    });
    return Response.redirect(`https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`, 302);
  }

  // Hosted Google Sign-In Portal when GOOGLE_CLIENT_ID is not yet set in env
  const directCallbackUrl = `${getPublicBaseOrigin(req)}/api/auth/google/direct`;
  const html = `<!DOCTYPE html><html lang="id"><head><meta charset="utf-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/><title>Masuk dengan Google • AnimWatch</title><style>*{box-sizing:border-box}body{font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;background:#0D0F17;color:#F3F4F6;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0;padding:20px}.card{width:100%;max-width:390px;background:#171A26;border:1px solid rgba(255,255,255,.12);border-radius:24px;padding:28px;box-shadow:0 20px 50px rgba(0,0,0,.45)}.logo{display:flex;align-items:center;gap:10px;justify-content:center;margin-bottom:16px}.g-badge{width:38px;height:38px;border-radius:50%;background:#fff;display:flex;align-items:center;justify-content:center;font-weight:800;font-size:20px;color:#4285F4}h2{margin:0 0 6px;text-align:center;font-size:21px}p.sub{margin:0 0 22px;text-align:center;color:#9CA3AF;font-size:13.5px;line-height:1.45}label{display:block;font-size:12.5px;font-weight:600;color:#D1D5DB;margin:12px 0 6px}input{width:100%;padding:13px 14px;border-radius:14px;border:1px solid rgba(255,255,255,.16);background:#0F111A;color:#fff;font-size:14.5px;outline:none}input:focus{border-color:#3B82F6}button{width:100%;margin-top:20px;padding:14px;border-radius:16px;border:none;background:linear-gradient(135deg,#4285F4,#2563EB);color:#fff;font-weight:700;font-size:15px;cursor:pointer}.cancel{display:block;text-align:center;margin-top:14px;color:#9CA3AF;font-size:13px;text-decoration:none}</style></head><body><div class="card"><div class="logo"><div class="g-badge">G</div></div><h2>Masuk dengan Google</h2><p class="sub">Lanjutkan ke <b>AnimWatch</b> menggunakan akun Google kamu untuk menyinkronkan profil, komentar, dan daftar tontonan.</p><form method="GET" action="${directCallbackUrl}"><label>Email Google (Gmail)</label><input type="email" name="email" placeholder="nama@gmail.com" required autofocus/><label>Nama Tampilan Google</label><input type="text" name="name" placeholder="Nama lengkap kamu" required/><button type="submit">Lanjutkan dengan Google</button></form><a class="cancel" href="animwatch://auth/google?error=Login+dibatalkan">Kembali ke Aplikasi</a></div></body></html>`;
  return new Response(html, { status: 200, headers: { "Content-Type": "text/html; charset=utf-8" } });
}

async function handleGoogleDirectWebCallback(req: Request): Promise<Response> {
  const url = new URL(req.url);
  const email = (url.searchParams.get("email") ?? "").trim().toLowerCase();
  const name = (url.searchParams.get("name") ?? "").trim() || email.split("@")[0] || "Google User";
  if (!EMAIL_REGEX.test(email)) {
    return htmlRedirectResponse(
      `animwatch://auth/google?error=${encodeURIComponent("Alamat email Google tidak valid")}`,
      "Alamat email Google tidak valid."
    );
  }
  try {
    const { token } = await upsertGoogleUserAndCreateSession({
      sub: `g_mail_${email}`,
      email,
      name,
    });
    return htmlRedirectResponse(
      `animwatch://auth/google?token=${encodeURIComponent(token)}`,
      "Login Google berhasil! Membuka AnimWatch..."
    );
  } catch (err) {
    const msg = err instanceof Error ? err.message : "Gagal masuk dengan Google";
    return htmlRedirectResponse(`animwatch://auth/google?error=${encodeURIComponent(msg)}`, msg);
  }
}

async function handleGoogleOAuthCallback(req: Request): Promise<Response> {
  const url = new URL(req.url);
  const error = url.searchParams.get("error");
  if (error) {
    return htmlRedirectResponse(
      `animwatch://auth/google?error=${encodeURIComponent("Login Google dibatalkan")}`,
      "Login Google dibatalkan. Mengembalikan ke aplikasi..."
    );
  }

  const code = (url.searchParams.get("code") ?? "").trim();
  const state = (url.searchParams.get("state") ?? "").trim();
  const stateExpiry = googleOAuthStates.get(state);
  googleOAuthStates.delete(state);
  if (!code || !stateExpiry || Date.now() > stateExpiry) {
    return htmlRedirectResponse(
      `animwatch://auth/google?error=${encodeURIComponent("Sesi otorisasi Google tidak valid atau kedaluwarsa")}`,
      "Sesi otorisasi tidak valid."
    );
  }

  try {
    const redirectUri = `${getPublicBaseOrigin(req)}/api/auth/google/callback`;
    const tokenRes = await fetch("https://oauth2.googleapis.com/token", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        code,
        client_id: GOOGLE_CLIENT_ID,
        client_secret: GOOGLE_CLIENT_SECRET,
        redirect_uri: redirectUri,
        grant_type: "authorization_code",
      }),
      signal: AbortSignal.timeout(10000),
    });
    if (!tokenRes.ok) {
      throw new Error("Gagal menukar kode OAuth Google");
    }
    const tokenData = (await tokenRes.json()) as { access_token?: string; id_token?: string };
    if (!tokenData.access_token) throw new Error("Token akses Google kosong");

    const userRes = await fetch("https://openidconnect.googleapis.com/v1/userinfo", {
      headers: { Authorization: `Bearer ${tokenData.access_token}` },
      signal: AbortSignal.timeout(8000),
    });
    if (!userRes.ok) throw new Error("Gagal memuat profil akun Google");

    const info = (await userRes.json()) as {
      sub?: string;
      email?: string;
      email_verified?: boolean;
      name?: string;
      picture?: string;
    };
    if (!info.sub || !info.email || info.email_verified !== true) {
      throw new Error("Profil Google tidak lengkap atau email belum terverifikasi");
    }

    const { token } = await upsertGoogleUserAndCreateSession({
      sub: info.sub,
      email: info.email,
      name: info.name || info.email.split("@")[0],
      picture: info.picture,
    });

    return htmlRedirectResponse(
      `animwatch://auth/google?token=${encodeURIComponent(token)}`,
      "Login Google berhasil! Membuka AnimWatch..."
    );
  } catch (err) {
    const msg = err instanceof Error ? err.message : "Gagal masuk dengan Google";
    return htmlRedirectResponse(
      `animwatch://auth/google?error=${encodeURIComponent(msg)}`,
      msg
    );
  }
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

  const body = await parseJsonBody<{
    displayName?: string;
    handle?: string;
    bio?: string;
    avatarEmblem?: string;
    avatarUrl?: string;
    memberTier?: string;
    favoriteGenre?: string;
  }>(req);
  const cleanName = (body?.displayName ?? "").trim() || user.display_name;
  const rawHandle = (body?.handle ?? user.handle).trim();
  const cleanHandle = rawHandle
    ? `@${rawHandle.replace(/^@+/, "").toLowerCase().replace(/[^a-z0-9._]/g, "").slice(0, 24) || "watcher"}`
    : user.handle;
  const cleanBio = (body?.bio ?? user.bio).trim();
  const cleanEmblem = ((body?.avatarEmblem ?? user.avatar_emblem).trim().slice(0, 2) || user.avatar_emblem).toUpperCase();
  const cleanAvatarUrl = body?.avatarUrl !== undefined ? String(body.avatarUrl ?? "").trim() : (user.avatar_url ?? "");
  const cleanTier = (body?.memberTier ?? "").trim() || user.member_tier;

  let currentGenres: string[] = ["Action", "Fantasy", "Sci-Fi"];
  if (Array.isArray(user.preferred_genres)) {
    currentGenres = user.preferred_genres.map(String);
  } else if (typeof user.preferred_genres === "string") {
    try {
      const parsed = JSON.parse(user.preferred_genres);
      if (Array.isArray(parsed)) currentGenres = parsed.map(String);
    } catch {}
  }
  const fav = (body?.favoriteGenre ?? "").trim();
  const updatedGenres = fav ? [fav, ...currentGenres.filter((g) => g.toLowerCase() !== fav.toLowerCase())] : currentGenres;

  const updated = await db`
    UPDATE users
    SET display_name = ${cleanName},
        handle = ${cleanHandle},
        bio = ${cleanBio},
        avatar_emblem = ${cleanEmblem},
        avatar_url = ${cleanAvatarUrl},
        member_tier = ${cleanTier},
        preferred_genres = ${JSON.stringify(updatedGenres)}::jsonb,
        updated_at = NOW()
    WHERE id = ${Number(user.id)}
    RETURNING id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
              appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
  `;
  const updatedRow = updated[0] as DbUserRow;
  // Also keep author_name and author_badge on user's comments updated
  await db`
    UPDATE comments
    SET author_name = ${cleanName}, author_badge = ${cleanTier}
    WHERE user_id = ${Number(user.id)}
  `;
  return jsonResponse({ status: "success", user: serializeUser(updatedRow) });
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
    RETURNING id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
              appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
  `;
  return jsonResponse({ status: "success", user: serializeUser(updated[0] as DbUserRow) });
}

async function handleUpsertWatchlist(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const body = await parseJsonBody<{ animeId?: string; category?: string | null; watchedEpisodes?: number | null }>(req);
  const animeId = (body?.animeId ?? "").trim();
  if (!animeId) return jsonResponse({ status: false, message: "animeId required" }, 400);

  const category = body?.category?.trim() || null;
  let watchedEp: number | null = null;
  if (body?.watchedEpisodes != null) {
    const parsedWatched = Number(body.watchedEpisodes);
    if (!Number.isFinite(parsedWatched)) return jsonResponse({ status: false, message: "watchedEpisodes must be a number" }, 400);
    watchedEp = Math.max(0, Math.floor(parsedWatched));
  }

  if (!category && watchedEp === null) {
    await db`DELETE FROM watchlist WHERE user_id = ${Number(user.id)} AND anime_id = ${animeId}`;
  } else {
    const finalCat = category || "WATCHING";
    const finalWatched = watchedEp ?? 0;
    if (watchedEp !== null) {
      await db`
        INSERT INTO watchlist (user_id, anime_id, category, watched_episodes, updated_at)
        VALUES (${Number(user.id)}, ${animeId}, ${finalCat}, ${finalWatched}, NOW())
        ON CONFLICT (user_id, anime_id)
        DO UPDATE SET category = EXCLUDED.category, watched_episodes = EXCLUDED.watched_episodes, updated_at = NOW()
      `;
    } else {
      await db`
        INSERT INTO watchlist (user_id, anime_id, category, updated_at)
        VALUES (${Number(user.id)}, ${animeId}, ${finalCat}, NOW())
        ON CONFLICT (user_id, anime_id)
        DO UPDATE SET category = EXCLUDED.category, updated_at = NOW()
      `;
    }
  }
  return jsonResponse({ status: "success", animeId, category, watchedEpisodes: watchedEp });
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

  const rawEpNum = Number(body?.episodeNumber ?? 1);
  const rawPos = Number(body?.positionSeconds ?? 0);
  const rawDur = Number(body?.durationSeconds ?? 1440);
  if (!Number.isFinite(rawEpNum) || !Number.isFinite(rawPos) || !Number.isFinite(rawDur)) {
    return jsonResponse({ status: false, message: "Numeric history fields must be finite" }, 400);
  }
  const epNum = Math.max(1, Math.floor(rawEpNum));
  const epTitle = (body?.episodeTitle ?? `Episode ${epNum}`).trim();
  const pos = Math.max(0, Math.floor(rawPos));
  const dur = Math.max(1, Math.floor(rawDur));

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
  const rawScore = Number(body?.score ?? 0);
  if (!animeId || !Number.isFinite(rawScore)) {
    return jsonResponse({ status: false, message: "Invalid rating" }, 400);
  }
  const score = Math.min(10, Math.max(1, Math.round(rawScore)));

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
    SELECT c.id, c.user_id, c.anime_id, c.episode_number, c.author_name, c.author_badge, c.content, c.is_spoiler, c.parent_comment_id, c.created_at,
           u.avatar_emblem AS user_emblem,
           u.avatar_url AS user_avatar_url,
           u.handle AS user_handle,
           COUNT(cl.user_id)::int AS likes_count,
           BOOL_OR(cl.user_id = ${userId}) AS is_liked
    FROM comments c
    LEFT JOIN users u ON u.id = c.user_id
    LEFT JOIN comment_likes cl ON cl.comment_id = c.id
    WHERE c.anime_id = ${cleanAnimeId}
    GROUP BY c.id, u.avatar_emblem, u.avatar_url, u.handle
    ORDER BY c.created_at DESC
    LIMIT 100
  `;

  const data = (rows as any[]).map((r) => ({
    id: `db_cmt_${r.id}`,
    animeId: r.anime_id,
    episodeNumber: r.episode_number !== null ? Number(r.episode_number) : null,
    author: r.author_name,
    authorHandle: r.user_handle ?? "",
    authorBadge: r.author_badge,
    authorEmblem: r.user_emblem ?? makeEmblem(r.author_name ?? "Member"),
    authorAvatarUrl: r.user_avatar_url ?? "",
    content: r.content,
    timeAgo: formatRelativeTime(r.created_at),
    likes: Number(r.likes_count ?? 0),
    isLiked: Boolean(r.is_liked),
    isSpoiler: Boolean(r.is_spoiler),
    isOwn: userId > 0 && Number(r.user_id) === userId,
    parentId: r.parent_comment_id !== null ? `db_cmt_${r.parent_comment_id}` : null,
  }));
  return jsonResponse({ status: "success", data });
}

async function handlePostComment(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);
  const body = await parseJsonBody<any>(req);
  const animeId = String(body?.animeId ?? "").trim();
  const content = String(body?.content ?? "").trim();
  if (!animeId || !content) return jsonResponse({ status: false, message: "Komentar tidak boleh kosong" }, 400);
  let epNum: number | null = null;
  if (body?.episodeNumber != null) {
    const parsedEp = Number(body.episodeNumber);
    if (!Number.isFinite(parsedEp)) return jsonResponse({ status: false, message: "episodeNumber harus berupa angka" }, 400);
    epNum = Math.max(1, Math.floor(parsedEp));
  }
  const isSpoiler = Boolean(body?.isSpoiler);
  const rawParentId = body?.parentId ? Number(String(body.parentId).replace(/^db_cmt_/, "")) : null;
  const parentId = rawParentId && Number.isFinite(rawParentId) && rawParentId > 0 ? rawParentId : null;

  const inserted = await db`
    INSERT INTO comments (user_id, anime_id, episode_number, author_name, author_badge, content, is_spoiler, parent_comment_id)
    VALUES (${Number(user.id)}, ${animeId}, ${epNum}, ${user.display_name}, ${user.member_tier}, ${content}, ${isSpoiler}, ${parentId})
    RETURNING id, user_id, anime_id, episode_number, author_name, author_badge, content, is_spoiler, parent_comment_id, created_at
  `;
  const r: any = inserted[0];
  return jsonResponse({
    status: "success",
    comment: {
      id: `db_cmt_${r.id}`,
      animeId: r.anime_id,
      episodeNumber: r.episode_number !== null ? Number(r.episode_number) : null,
      author: r.author_name,
      authorHandle: user.handle ?? "",
      authorBadge: r.author_badge,
      authorEmblem: user.avatar_emblem ?? makeEmblem(user.display_name),
      authorAvatarUrl: user.avatar_url ?? "",
      content: r.content,
      timeAgo: "Baru saja",
      likes: 0,
      isLiked: false,
      isSpoiler: Boolean(r.is_spoiler),
      isOwn: true,
      parentId: r.parent_comment_id !== null ? `db_cmt_${r.parent_comment_id}` : null,
    },
  }, 201);
}

async function handleDeleteComment(req: Request, rawId: string): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const numericId = Number(rawId.replace(/^db_cmt_/, ""));
  if (!Number.isFinite(numericId) || numericId <= 0) {
    return jsonResponse({ status: false, message: "Invalid comment id" }, 400);
  }

  const deleted = await db`DELETE FROM comments WHERE id = ${numericId} AND user_id = ${Number(user.id)} RETURNING id`;
  if (deleted.length === 0) {
    return jsonResponse({ status: false, message: "Komentar tidak ditemukan atau bukan milikmu" }, 404);
  }
  return jsonResponse({ status: "success", deletedId: `db_cmt_${numericId}` });
}

async function handleToggleCommentLike(req: Request, rawId: string): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const user = await getAuthenticatedUser(req);
  if (!user) return jsonResponse({ status: false, message: "Unauthorized" }, 401);

  const numericId = Number(rawId.replace(/^db_cmt_/, ""));
  if (!Number.isFinite(numericId) || numericId <= 0) {
    return jsonResponse({ status: false, message: "Invalid comment id" }, 400);
  }

  const targetComment = await db`SELECT id FROM comments WHERE id = ${numericId} LIMIT 1`;
  if (targetComment.length === 0) {
    return jsonResponse({ status: false, message: "Komentar tidak ditemukan" }, 404);
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
// Official Legal Free YouTube Feeds (Muse ID & Ani-One ID)
// ======================================================

interface YtRssEntry {
  videoId: string;
  title: string;
  published: string;
  thumbnail: string;
  channelName: string;
}

function ytNoCookieEmbed(videoId: string): string {
  return `https://www.youtube-nocookie.com/embed/${videoId}?playsinline=1&rel=0&modestbranding=1&iv_load_policy=3&enablejsapi=1`;
}

async function fetchYouTubeAtomFeed(feedUrl: string, channelName: string): Promise<YtRssEntry[]> {
  try {
    const res = await fetch(feedUrl, {
      headers: { "User-Agent": "AnimWatch-OfficialFeed/1.0" },
      signal: AbortSignal.timeout(7000),
    });
    if (!res.ok) return [];
    const xml = await res.text();
    const $ = load(xml, { xmlMode: true });
    const entries: YtRssEntry[] = [];

    $("entry").each((_, el) => {
      const videoId = $(el).find("yt\\:videoId").text().trim() || $(el).find("videoId").text().trim();
      const title = $(el).find("title").first().text().trim();
      const published = $(el).find("published").first().text().trim();
      if (videoId && title) {
        entries.push({
          videoId,
          title,
          published,
          thumbnail: `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`,
          channelName,
        });
      }
    });
    return entries;
  } catch {
    return [];
  }
}

function extractBalancedJsonObject(text: string, marker: string): string | null {
  const idx = text.indexOf(marker);
  if (idx < 0) return null;
  const start = text.indexOf("{", idx + marker.length);
  if (start < 0) return null;
  let depth = 0;
  let inString = false;
  let escapeNext = false;
  for (let i = start; i < text.length; i++) {
    const ch = text[i];
    if (escapeNext) {
      escapeNext = false;
      continue;
    }
    if (ch === "\\" && inString) {
      escapeNext = true;
      continue;
    }
    if (ch === '"') {
      inString = !inString;
      continue;
    }
    if (!inString) {
      if (ch === "{") depth++;
      else if (ch === "}") {
        depth--;
        if (depth === 0) return text.slice(start, i + 1);
      }
    }
  }
  return null;
}

async function fetchFullYouTubePlaylistEntries(playlistId: string, channelName: string): Promise<YtRssEntry[]> {
  const cleanPlId = playlistId.trim();
  if (!cleanPlId) return [];
  const cacheKey = `yt-pl-full-v2:${cleanPlId}`;
  const cached = getCached<YtRssEntry[]>(cacheKey);
  if (cached) return cached;

  const headers: Record<string, string> = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
    "Accept-Language": "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7",
    Cookie: "SOCS=CAESEwgDEgk2ODE4Mzg2NDQaAmVuIAEaBgiA_LyaBg",
  };

  try {
    const res = await fetch(`https://www.youtube.com/playlist?list=${encodeURIComponent(cleanPlId)}`, {
      headers,
      signal: AbortSignal.timeout(10000),
    });
    if (res.ok) {
      const html = await res.text();
      const rawJson = extractBalancedJsonObject(html, "var ytInitialData =");
      const apiKey = (html.match(/"INNERTUBE_API_KEY":"([^"]+)"/) || [])[1];
      const clientVersion = (html.match(/"INNERTUBE_CLIENT_VERSION":"([^"]+)"/) || [])[1] || "2.20250101.00.00";
      if (rawJson) {
        const parsed = JSON.parse(rawJson);
        const entries: YtRssEntry[] = [];
        const seen = new Set<string>();

        const collectNode = (node: any, tokensOut: string[]) => {
          if (!node || typeof node !== "object") return;
          if (node.playlistVideoRenderer) {
            const v = node.playlistVideoRenderer;
            const vid = String(v.videoId ?? "").trim();
            const title = (v.title?.runs ?? []).map((r: any) => String(r?.text ?? "")).join("").trim()
              || String(v.title?.simpleText ?? "").trim();
            if (vid && title && !seen.has(vid)) {
              seen.add(vid);
              entries.push({
                videoId: vid,
                title,
                published: "",
                thumbnail: `https://i.ytimg.com/vi/${vid}/hqdefault.jpg`,
                channelName,
              });
            }
            return;
          }
          if (node.lockupViewModel) {
            const lvm = node.lockupViewModel;
            const vid = String(lvm.contentId ?? "").trim();
            const title = String(lvm.metadata?.lockupMetadataViewModel?.title?.content ?? "").trim();
            if (vid && /^[A-Za-z0-9_-]{8,16}$/.test(vid) && title && !seen.has(vid)) {
              seen.add(vid);
              entries.push({
                videoId: vid,
                title,
                published: "",
                thumbnail: `https://i.ytimg.com/vi/${vid}/hqdefault.jpg`,
                channelName,
              });
            }
            return;
          }
          const tok = node.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token;
          if (typeof tok === "string" && tok.length > 8) tokensOut.push(tok);
          for (const val of Object.values(node)) collectNode(val, tokensOut);
        };

        const initTokens: string[] = [];
        collectNode(parsed, initTokens);
        let nextToken: string | null = initTokens[initTokens.length - 1] ?? null;
        let page = 1;

        while (nextToken && apiKey && page < 4) {
          page++;
          const contRes = await fetch(`https://www.youtube.com/youtubei/v1/browse?key=${apiKey}`, {
            method: "POST",
            headers: { ...headers, "Content-Type": "application/json" },
            body: JSON.stringify({
              context: { client: { clientName: "WEB", clientVersion, hl: "id", gl: "ID" } },
              continuation: nextToken,
            }),
            signal: AbortSignal.timeout(7000),
          }).catch(() => null);
          if (!contRes || !contRes.ok) break;
          const contJson = await contRes.json();
          const newTokens: string[] = [];
          const before = entries.length;
          collectNode(contJson, newTokens);
          nextToken = newTokens[newTokens.length - 1] ?? null;
          if (entries.length === before) break;
        }

        if (entries.length > 0) {
          return setCached(cacheKey, entries, 20 * 60 * 1000);
        }
      }
    }
  } catch {
    // Fallback to Atom RSS below
  }

  const rss = await fetchYouTubeAtomFeed(
    `https://www.youtube.com/feeds/videos.xml?playlist_id=${encodeURIComponent(cleanPlId)}`,
    channelName
  );
  return rss.length > 0 ? setCached(cacheKey, rss, 10 * 60 * 1000) : rss;
}

function isOfficialFullEpisodeTitle(title: string): boolean {
  const lower = title.toLowerCase();
  if (
    lower.includes("#short") ||
    lower.includes("#highlight") ||
    lower.includes("highlight") ||
    lower.includes("cuplikan") ||
    lower.includes("semua episode") ||
    lower.includes("full marathon") ||
    lower.includes("trailer") ||
    lower.includes("teaser") ||
    lower.includes("pv ") ||
    lower.includes("opening") ||
    lower.includes("ending") ||
    lower.includes("ost") ||
    lower.includes("kompilasi")
  ) {
    return false;
  }
  return /\b(episode|ep\.?\s*\d+|eps\.?\s*\d+|misi\s*\d+|s\d+e\d+|movie|ova)\b|#\d+/i.test(title);
}

function parseOfficialEpisodeTitle(rawTitle: string): { seriesTitle: string; episodeNumber: number; sortKey: number } {
  let clean = rawTitle
    .replace(/\[[^\]]*(bahasa indonesia|takarir|sub indo|english sub|dub)[^\]]*\]/gi, "")
    .replace(/【[^】]*】/g, "")
    .replace(/《([^》]*)》/g, "$1 ")
    .replace(/\|\s*(dub\s*jp|sub\s*indo|id\s*sub|muse indonesia|ani-one).*$/i, "")
    .replace(/^dub\s*id\s*[-–:]\s*/i, "")
    .trim();

  const decimalMatch = clean.match(/(?:episode|eps?\.?|misi|#)\s*0*(\d+)\.(\d+)/i);
  const epMatch =
    clean.match(/(?:episode|eps?\.?|misi)\s*0*(\d+)/i) ||
    clean.match(/\bs\d+\s*e0*(\d+)\b/i) ||
    clean.match(/[#]\s*0*(\d+)\b/) ||
    clean.match(/[-–:]\s*0*(\d+)\b/);

  const episodeNumber = epMatch ? Number(epMatch[1]) : 1;
  const sortKey = decimalMatch
    ? Number(`${decimalMatch[1]}.${decimalMatch[2]}`)
    : episodeNumber;

  const seriesTitle = clean
    .replace(/[-–:|]*\s*(?:episode|eps?\.?|misi)\s*\d+.*$/i, "")
    .replace(/[-–:|]*\s*s\d+\s*e\d+.*$/i, "")
    .replace(/[-–:\s#]+\d+(?:\.\d+)?\s*$/, "")
    .replace(/[-–:\s]+$/, "")
    .trim() || rawTitle.slice(0, 48);

  return { seriesTitle, episodeNumber, sortKey };
}

interface CuratedPlaylistRule {
  playlistId: string;
  channelName: "Muse Indonesia" | "Ani-One Indonesia";
  epMin?: number;
  epMax?: number;
  requireTitleRegex?: RegExp;
  excludeTitleRegex?: RegExp;
  allowDecimalRecaps?: boolean;
}

// Verified 100% free & embeddable Muse Indonesia + Ani-One Indonesia playlists mapped to exact AniList IDs (including split 2-Part / Multi-Cour seasons!)
const M = "PLPanbgyTozt";
const CURATED_PLAYLIST_RULES: Record<number, CuratedPlaylistRule> = {
  // === Mushoku Tensei COMPLETE (Season 1 Part 1, Cour 2, Eris Special, Season 2 Part 1 & Part 2, Season 3) ===
  108465: { playlistId: `${M}YFBI_bkaDF21nfP_c--FPv`, channelName: "Muse Indonesia", epMin: 1, epMax: 11, excludeTitleRegex: /season\s*[23]/i },
  127720: { playlistId: `${M}YFBI_bkaDF21nfP_c--FPv`, channelName: "Muse Indonesia", epMin: 12, epMax: 23, excludeTitleRegex: /season\s*[23]/i },
  141534: { playlistId: `${M}YFBI_bkaDF21nfP_c--FPv`, channelName: "Muse Indonesia", epMin: 24, epMax: 24, excludeTitleRegex: /season\s*[23]/i },
  146065: { playlistId: `${M}YFBI_bkaDF21nfP_c--FPv`, channelName: "Muse Indonesia", epMin: 0, epMax: 12, requireTitleRegex: /season\s*2\b/i, excludeTitleRegex: /(?:cour|part)\s*2|season\s*3/i },
  166873: { playlistId: `${M}YFBI_bkaDF21nfP_c--FPv`, channelName: "Muse Indonesia", epMin: 13, epMax: 24, requireTitleRegex: /season\s*2\s*(?:cour|part)\s*2/i },
  178789: { playlistId: `${M}YFBI_bkaDF21nfP_c--FPv`, channelName: "Muse Indonesia", epMin: 1, epMax: 14, requireTitleRegex: /season\s*3\b/i },

  // === Frieren: Beyond Journey's End (Season 1 & Season 2) ===
  154587: { playlistId: `${M}Z7OBDPkXwBuhPvgd5OBZ70`, channelName: "Muse Indonesia", epMin: 1, epMax: 28 },
  182255: { playlistId: `${M}Z7OBDPkXwBuhPvgd5OBZ70`, channelName: "Muse Indonesia", epMin: 29, epMax: 42 },

  // === SPY x FAMILY (Season 1 Part 1, Cour 2, Season 2, Season 3) ===
  140960: { playlistId: `${M}bu7tZq6L49MPhra92n2ixf`, channelName: "Muse Indonesia", epMin: 1, epMax: 12 },
  142838: { playlistId: `${M}bu7tZq6L49MPhra92n2ixf`, channelName: "Muse Indonesia", epMin: 13, epMax: 25 },
  158927: { playlistId: `${M}bu7tZq6L49MPhra92n2ixf`, channelName: "Muse Indonesia", epMin: 26, epMax: 37 },
  177937: { playlistId: `${M}bu7tZq6L49MPhra92n2ixf`, channelName: "Muse Indonesia", epMin: 38, epMax: 50 },

  // === DAN DA DAN (Season 1 & Season 2) ===
  171018: { playlistId: `${M}aeQFokLfCgkmP_C3szkxCR`, channelName: "Muse Indonesia", epMin: 1, epMax: 12 },
  185660: { playlistId: `${M}aeQFokLfCgkmP_C3szkxCR`, channelName: "Muse Indonesia", epMin: 13, epMax: 24 },

  // === Attack on Titan (Season 1 .. Final Chapters) ===
  16498: { playlistId: `${M}ZJLy-bGH_tD6rA1ObhW7iz`, channelName: "Muse Indonesia", epMin: 1, epMax: 25 },
  20958: { playlistId: `${M}ZJLy-bGH_tD6rA1ObhW7iz`, channelName: "Muse Indonesia", epMin: 26, epMax: 37 },
  99147: { playlistId: `${M}ZJLy-bGH_tD6rA1ObhW7iz`, channelName: "Muse Indonesia", epMin: 38, epMax: 49 },
  104578: { playlistId: `${M}ZJLy-bGH_tD6rA1ObhW7iz`, channelName: "Muse Indonesia", epMin: 50, epMax: 59 },
  110277: { playlistId: `${M}ZJLy-bGH_tD6rA1ObhW7iz`, channelName: "Muse Indonesia", epMin: 60, epMax: 75 },
  131681: { playlistId: `${M}ZJLy-bGH_tD6rA1ObhW7iz`, channelName: "Muse Indonesia", epMin: 76, epMax: 87 },
  146984: { playlistId: `${M}ZJLy-bGH_tD6rA1ObhW7iz`, channelName: "Muse Indonesia", epMin: 88, epMax: 94 },

  // === That Time I Got Reincarnated as a Slime (S1, S2 Part 1, S2 Part 2, S3) ===
  101280: { playlistId: `${M}bl5IdZxm_aC5G7U5QYHjTG`, channelName: "Muse Indonesia", epMin: 1, epMax: 24 },
  108511: { playlistId: `${M}bl5IdZxm_aC5G7U5QYHjTG`, channelName: "Muse Indonesia", epMin: 25, epMax: 36 },
  116742: { playlistId: `${M}bl5IdZxm_aC5G7U5QYHjTG`, channelName: "Muse Indonesia", epMin: 37, epMax: 48 },
  156822: { playlistId: `${M}bl5IdZxm_aC5G7U5QYHjTG`, channelName: "Muse Indonesia", epMin: 49, epMax: 72 },

  // === Re:ZERO (Season 1, Season 2 Part 1, Season 2 Part 2, Season 3, Season 4) ===
  21355: { playlistId: "PLc5IarJmS5Mk", channelName: "Muse Indonesia" },
  108632: { playlistId: `${M}YThySIw-tvX8O3J-NHhMAT`, channelName: "Muse Indonesia", epMin: 26, epMax: 38 },
  119661: { playlistId: `${M}YThySIw-tvX8O3J-NHhMAT`, channelName: "Muse Indonesia", epMin: 39, epMax: 50 },
  163134: { playlistId: `${M}ZpP3zanOqFo20ky85-q1Yp`, channelName: "Muse Indonesia" },
  189046: { playlistId: `${M}b0iW-DI48ODtCrlyl42Qoa`, channelName: "Muse Indonesia" },

  // === Shangri-La Frontier (Season 1 & Season 2) ===
  151970: { playlistId: `${M}YlSxKLf7qFl6VpLYn2T_i1`, channelName: "Muse Indonesia", epMin: 1, epMax: 25 },
  176508: { playlistId: `${M}YlSxKLf7qFl6VpLYn2T_i1`, channelName: "Muse Indonesia", epMin: 26, epMax: 50 },

  // === Muse Indonesia Single-Season & Other Curated Playlists ===
  11061: { playlistId: `${M}YQo4C8qRLt4cN_MEjp-lRp`, channelName: "Muse Indonesia" },
  98659: { playlistId: `${M}ZBmp90FrnrDGXDDu5J6-LR`, channelName: "Muse Indonesia" },
  145545: { playlistId: `${M}YszfRvXrP_w8-DPbVpr8wy`, channelName: "Muse Indonesia" },
  146066: { playlistId: `${M}YYjVy4KU5BXp3b-MmCMGwE`, channelName: "Muse Indonesia" },
  180745: { playlistId: `${M}YgSdFfzM-MO6vXGcQn-FbR`, channelName: "Muse Indonesia" },
  12189: { playlistId: `${M}Yq6eXRxJCx8wxaQ0Yc2NHz`, channelName: "Muse Indonesia" },
  159831: { playlistId: `${M}ZN2SaJWB2i_pKwtsdGv2p7`, channelName: "Muse Indonesia" },
  156067: { playlistId: `${M}ZmsgNPEL9XHwb0ilTs5ljv`, channelName: "Muse Indonesia" },
  6702: { playlistId: `${M}btJMcDT57RXRIiQDjyIFx8`, channelName: "Muse Indonesia" },
  99749: { playlistId: `${M}aDhNmS0krUvUkYmylqD7Mi`, channelName: "Muse Indonesia" },
  139095: { playlistId: `${M}Ylvpi2MvbIYlZ1_8xUDf4o`, channelName: "Muse Indonesia" },
  105156: { playlistId: `${M}YOhY1JG0xIvTkGdHNyCRlU`, channelName: "Muse Indonesia" },
  163292: { playlistId: `${M}Yx8pk4_Sy3R94Eg18zACf0`, channelName: "Muse Indonesia" },
  187663: { playlistId: `${M}Y5d8qXvBR5s3s5L4QMy3jU`, channelName: "Muse Indonesia" },
  132474: { playlistId: `${M}Y9q0XKsNpDu8hjS3Njnomb`, channelName: "Muse Indonesia" },
  98503: { playlistId: `${M}Yx6oMEi7jzfsxbkusruG2N`, channelName: "Muse Indonesia" },
  152682: { playlistId: "PLF-Ab5Bnb45U", channelName: "Muse Indonesia" },
  176314: { playlistId: "PLZqRcDDhscWc", channelName: "Muse Indonesia" },
  177634: { playlistId: "PLGWnhssZ3bgE", channelName: "Muse Indonesia" },
  162694: { playlistId: "PLWExLiCEALJw", channelName: "Muse Indonesia" },
  184492: { playlistId: `${M}bsmc1HPxlCK3oQIafoNBgv`, channelName: "Muse Indonesia" },
  108478: { playlistId: `${M}YeEbbQUautfzW7zz-5cgmS`, channelName: "Muse Indonesia" },
  156039: { playlistId: `${M}YH0PU5B62ONgeTZXoE-EOj`, channelName: "Muse Indonesia" },
  196144: { playlistId: `${M}Zac25sNFH7tE7Ud07JCUnL`, channelName: "Muse Indonesia" },
  199547: { playlistId: `${M}ZEvP1IiB5U3TcIB1zP4OxG`, channelName: "Muse Indonesia" },
  21855: { playlistId: `${M}Y-KiGHbZLR1BSl90fFAVNp`, channelName: "Muse Indonesia" },
  102926: { playlistId: `${M}ZIT_IzLXTzpUBf0DFsoy1A`, channelName: "Muse Indonesia" },
  1604: { playlistId: `${M}Z4hEoVgz3UmqVhX11WF79B`, channelName: "Muse Indonesia" },
  18767: { playlistId: `${M}Z63N8VunmCGGc5_axMSK3l`, channelName: "Muse Indonesia" },
  21290: { playlistId: `${M}ZsiNojl0AvsR5IB9AbIs9G`, channelName: "Muse Indonesia" },
  194447: { playlistId: `${M}ZubEuygztnx_4W4PY6twVj`, channelName: "Muse Indonesia" },
  20457: { playlistId: `${M}ZOEvzidlYlJ7X1UP3F9Lf5`, channelName: "Muse Indonesia" },
  143337: { playlistId: `${M}YXtCdCOgDDhfNlIWt2w5_L`, channelName: "Muse Indonesia" },
  10800: { playlistId: `${M}aNIqyXWPUl-NQp97qUSeM3`, channelName: "Muse Indonesia" },
  14397: { playlistId: `${M}bsrJNq7sR7rtSE2aTLD1Lb`, channelName: "Muse Indonesia" },
  101215: { playlistId: `${M}ZeiNlVq54ptLfHTGzBDR25`, channelName: "Muse Indonesia" },
  21873: { playlistId: "PLKteuQzwzADU", channelName: "Muse Indonesia" },
  187538: { playlistId: "PLO43emqzsDpQ", channelName: "Muse Indonesia" },
  174576: { playlistId: `${M}ZL03NxAn1aHQxDsnCu_-xl`, channelName: "Muse Indonesia" },
  20789: { playlistId: `${M}YAzNKcsCBkn_53lih7V2LH`, channelName: "Muse Indonesia" },
  99539: { playlistId: `${M}Y4YGogr-NvM2erIuzCSF8b`, channelName: "Muse Indonesia" },
  108928: { playlistId: `${M}YUBC8M6GqnlF6YbT7HlBDo`, channelName: "Muse Indonesia" },
  100977: { playlistId: `${M}a4TKRkbiiU8A5-KBUfj5KO`, channelName: "Muse Indonesia" },
  117533: { playlistId: `${M}YhN4tev6-PlmBThyvtD2xv`, channelName: "Muse Indonesia" },
  170695: { playlistId: `${M}acfueJyf4OFLQziViFKQ50`, channelName: "Muse Indonesia" },
  156415: { playlistId: `${M}ZVZgaofBHOgWb624juQTQ8`, channelName: "Muse Indonesia" },
  169417: { playlistId: `${M}YbZZNJq7Gf0-gOIPHcNJKi`, channelName: "Muse Indonesia" },
  137908: { playlistId: `${M}ZK2bfGabT5IjqCctvNkYlY`, channelName: "Muse Indonesia" },
  168374: { playlistId: `${M}bS2nnLNoUmLmAACitarsAv`, channelName: "Muse Indonesia" },
  166794: { playlistId: `${M}bRLkbFAYI_MewzYcgyzfLk`, channelName: "Muse Indonesia" },
  141821: { playlistId: `${M}Z3CmrVllbkncpqEsQwbh-6`, channelName: "Muse Indonesia" },
  9513: { playlistId: `${M}bSpT6je9oRWaqe4_3AxeGb`, channelName: "Muse Indonesia" },
  147642: { playlistId: `${M}bx1r01Ub9Ps9Imaohto8up`, channelName: "Muse Indonesia" },
  158028: { playlistId: `${M}aF7MtQR-ZWL9SmG5WzKZ91`, channelName: "Muse Indonesia" },
  163078: { playlistId: `${M}YfKRDSnwzzxsLiE35fSbBo`, channelName: "Muse Indonesia" },
  170130: { playlistId: `${M}armJZfU9GyRSCBCnIiJONX`, channelName: "Muse Indonesia" },
  114124: { playlistId: `${M}aehPnu02ArgIJcP4DFJ2bO`, channelName: "Muse Indonesia" },
  116006: { playlistId: `${M}aC8yJC3SVMP3NSxN0cb4Wh`, channelName: "Muse Indonesia" },
  116005: { playlistId: `${M}aCN6AS9cPj_ydTWj2btL9N`, channelName: "Muse Indonesia" },
  117343: { playlistId: `${M}b2-Qf_38TnxtRbjv6iM_Qz`, channelName: "Muse Indonesia" },

  // === Ani-One Indonesia (with full multi-season / 2-part splits!) ===
  113415: { playlistId: "PLNAQymMf9eaYxBDom48OG_F4aLhnBZcui", channelName: "Ani-One Indonesia", epMin: 1, epMax: 24, excludeTitleRegex: /season\s*2/i },
  145064: { playlistId: "PLNAQymMf9eaYxBDom48OG_F4aLhnBZcui", channelName: "Ani-One Indonesia", epMin: 25, epMax: 47, requireTitleRegex: /season\s*2/i },
  116674: { playlistId: "PLNAQymMf9eaZqHO36VokI45TwtvUJFbfk", channelName: "Ani-One Indonesia", epMin: 1, epMax: 13 },
  159322: { playlistId: "PLNAQymMf9eaZqHO36VokI45TwtvUJFbfk", channelName: "Ani-One Indonesia", epMin: 14, epMax: 26 },
  169755: { playlistId: "PLNAQymMf9eaZqHO36VokI45TwtvUJFbfk", channelName: "Ani-One Indonesia", epMin: 27, epMax: 40 },
  137822: { playlistId: "PLQz4WGeaorjA", channelName: "Ani-One Indonesia", epMin: 1, epMax: 24 },
  163146: { playlistId: "PLQz4WGeaorjA", channelName: "Ani-One Indonesia", epMin: 25, epMax: 38 },
  105333: { playlistId: "PLQL6iBDZsabo", channelName: "Ani-One Indonesia", epMin: 1, epMax: 24, excludeTitleRegex: /season\s*[234]/i },
  113936: { playlistId: "PLQL6iBDZsabo", channelName: "Ani-One Indonesia", epMin: 1, epMax: 11, requireTitleRegex: /season\s*2\b/i },
  131518: { playlistId: "PLQL6iBDZsabo", channelName: "Ani-One Indonesia", epMin: 1, epMax: 11, requireTitleRegex: /season\s*3\b/i },
  162670: { playlistId: "PLQL6iBDZsabo", channelName: "Ani-One Indonesia", epMin: 12, epMax: 22, requireTitleRegex: /season\s*3\b/i },
  172019: { playlistId: "PLODWQCZoYSHM", channelName: "Ani-One Indonesia", epMin: 1, epMax: 12 },
  189117: { playlistId: "PLODWQCZoYSHM", channelName: "Ani-One Indonesia", epMin: 13, epMax: 24 },
  199221: { playlistId: "PLODWQCZoYSHM", channelName: "Ani-One Indonesia", epMin: 25, epMax: 37 },
  20832: { playlistId: "PLOXBvOkrpC_s", channelName: "Ani-One Indonesia" },
  21459: { playlistId: "PLRIKUnMprNvI", channelName: "Ani-One Indonesia" },
  21856: { playlistId: "PLA6LI1cj8LDk", channelName: "Ani-One Indonesia" },
  100166: { playlistId: "PLDoNd3kTAiyk", channelName: "Ani-One Indonesia" },
  104276: { playlistId: "PLXd7zGqfH3ZI", channelName: "Ani-One Indonesia" },
  117193: { playlistId: "PLRnE9XnIC1T4", channelName: "Ani-One Indonesia" },
  139630: { playlistId: "PLNgw6VnFxawE", channelName: "Ani-One Indonesia" },
  163139: { playlistId: "PLLKM3uH6S8OA", channelName: "Ani-One Indonesia" },
  162804: { playlistId: "PLNAQymMf9eaa139ptcijlI0JizvpFXFDc", channelName: "Ani-One Indonesia" },
  143338: { playlistId: "PLSnQpJo_dujc", channelName: "Ani-One Indonesia" },
  19815: { playlistId: "PLWyzUoEg2tlo", channelName: "Ani-One Indonesia" },
  20464: { playlistId: "PLEHiHtsaULg4", channelName: "Ani-One Indonesia" },
  918: { playlistId: "PLSz4xtUGSzEA", channelName: "Ani-One Indonesia" },
  11771: { playlistId: "PLSzQ99-K5DYo", channelName: "Ani-One Indonesia" },
  16894: { playlistId: "PLX7mM0yTy0Bk", channelName: "Ani-One Indonesia" },
  20725: { playlistId: "PLNB4urP2esgU", channelName: "Ani-One Indonesia" },
  20923: { playlistId: "PLdYbu6KhtdUU", channelName: "Ani-One Indonesia" },
  21518: { playlistId: "PLImbXtgn5ZUM", channelName: "Ani-One Indonesia" },
  136804: { playlistId: "PLexFyslfo_78", channelName: "Ani-One Indonesia" },
  150075: { playlistId: "PLDQ7JF2opq5k", channelName: "Ani-One Indonesia" },
  105228: { playlistId: "PLcDBBq1D3wOY", channelName: "Ani-One Indonesia" },
  173172: { playlistId: "PLFDsIfMtIv44", channelName: "Ani-One Indonesia" },
  21613: { playlistId: "PLcKfVhnYS2Nk", channelName: "Ani-One Indonesia" },
  125206: { playlistId: "PLbBhmk2Bw-jA", channelName: "Ani-One Indonesia" },
  139518: { playlistId: "PLIfTSfpnDLec", channelName: "Ani-One Indonesia" },
  103632: { playlistId: "PLA6VgIPypTSE", channelName: "Ani-One Indonesia" },
  139587: { playlistId: "PLeyB-SZ7G05M", channelName: "Ani-One Indonesia" },
  159042: { playlistId: "PLDv-inCJtX5A", channelName: "Ani-One Indonesia" },
  160900: { playlistId: "PLScAfDnCDpRU", channelName: "Ani-One Indonesia" },
  167419: { playlistId: "PLNAQymMf9eab5ejKPqDXIdFEaJEuxSrAW", channelName: "Ani-One Indonesia" },
  14813: { playlistId: "PLRYt0oMV87fU", channelName: "Ani-One Indonesia" },
  185: { playlistId: "PLTcut_BhoIa4", channelName: "Ani-One Indonesia" },
  20987: { playlistId: "PLGKjS1fjrROc", channelName: "Ani-One Indonesia" },
  147864: { playlistId: "PLNAQymMf9eaYJZdl3_y3WAY9wufqB8eTn", channelName: "Ani-One Indonesia" },
};

const MUSE_ID_PLAYLIST_BY_ANILIST_ID: Record<number, CuratedPlaylistRule> = CURATED_PLAYLIST_RULES;

interface DubSplitRule extends CuratedPlaylistRule {
  partKey: string;
  anilistId: number;
  titleOverride?: string;
  epCountHint: number;
}

const DUB_SPLIT_RULES: DubSplitRule[] = [
  { partKey: "s1p1", anilistId: 108465, playlistId: `${M}ZQCUAX0zVQe4HtNDvNtRTw`, channelName: "Muse Indonesia", epMin: 1, epMax: 11, excludeTitleRegex: /season\s*[23]/i, epCountHint: 11 },
  { partKey: "s1p2", anilistId: 127720, playlistId: `${M}ZQCUAX0zVQe4HtNDvNtRTw`, channelName: "Muse Indonesia", epMin: 12, epMax: 24, excludeTitleRegex: /season\s*[23]/i, epCountHint: 13 },
  { partKey: "s2p1", anilistId: 146065, playlistId: `${M}ZQCUAX0zVQe4HtNDvNtRTw`, channelName: "Muse Indonesia", epMin: 0, epMax: 12, requireTitleRegex: /season\s*2\b/i, excludeTitleRegex: /(?:cour|part)\s*2/i, epCountHint: 13 },
  { partKey: "s2p2", anilistId: 166873, playlistId: `${M}ZQCUAX0zVQe4HtNDvNtRTw`, channelName: "Muse Indonesia", epMin: 13, epMax: 24, requireTitleRegex: /season\s*2\s*(?:cour|part)\s*2/i, epCountHint: 12 },
];

const FEATURED_MUSE_ANILIST_IDS: number[] = [
  // Prioritize Mushoku Tensei complete S1 P1 -> P2 -> S2 P1 -> S2 P2 -> S3, Frieren S1-S2, SPY x FAMILY P1-S3, Re:ZERO S1-S4, JJK S1-S2, Bleach P1-P3
  108465, 127720, 146065, 166873, 178789, 141534,
  154587, 182255,
  140960, 142838, 158927, 177937,
  21355, 108632, 119661, 163134, 189046,
  171018, 185660,
  16498, 20958, 99147, 104578, 110277, 131681,
  113415, 145064,
  116674, 159322, 169755,
  137822, 163146,
  101280, 108511, 116742, 156822,
  151970, 176508,
  105333, 113936, 131518, 162670, 172019, 189117, 199221,
  11061, 20832, 21459, 20464, 156067,
  ...Object.keys(MUSE_ID_PLAYLIST_BY_ANILIST_ID).map(Number),
];

interface CrawledPlaylistSummary {
  playlistId: string;
  title: string;
  cleanTitle: string;
  channelName: string;
  thumbnail: string;
  episodeHint: number;
  audioLang: "SUB" | "DUB_ID";
  anilistId?: number | null;
}

function isDubIndoTitle(title: string): boolean {
  const t = title.toLowerCase();
  if (/dub\s*jp/i.test(t) && !/dub\s*id|id\s*dub|bahasa indonesia|sulih suara|dubbing indo/i.test(t)) return false;
  return /\b(dub\s*id|id\s*dub|dub\s*indo|dubbing\s*indo|sulih\s*suara|bahasa\s*indonesia|audio\s*indonesia)\b/i.test(t) || /\[bahasa\s*indonesia\]/i.test(t);
}

function cleanPlaylistDisplayTitle(raw: string): string {
  return raw
    .replace(/[\u{1F300}-\u{1FAFF}\u{2600}-\u{27BF}\u{FE0F}\u{200D}]/gu, " ")
    .replace(/\[[^\]]*\]/g, "")
    .replace(/【[^】]*】/g, "")
    .replace(/\((?:lengkap|dub|sub|id\s*sub|id\s*dub|takarir|bahasa indonesia|uncensored|censored)[^)]*\)/gi, "")
    .replace(/\|\s*(sub indo|dub id|dub jp|muse indonesia|ani-one).*$/i, "")
    .replace(/-\s*(sub indo|dub id|bahasa indonesia|takarir indonesia).*$/i, "")
    .replace(/^dub\s*id\s*[-–:]\s*/i, "")
    .replace(/^#ultraearlybird\s*[《«]?/i, "")
    .replace(/[《》«»]/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

function isSkippablePlaylistTitle(title: string): boolean {
  return /(^pv\b|\bpv\b|trailer|teaser|highlight|cuplikan|#short|shorts|\bost\b|opening|ending|music video|karaoke|promo|preview|interview|compilation|kompilasi|semua episode|anime festival|kolaborasi brand|\bcm\b|freetalk|\bvod\b|talkshow|musegaming|nonton bareng|kumpulan anime|film.*seru|\bultra\b|ultraearlybird)/i.test(title);
}

async function crawlPublicChannelPlaylists(handle: string, channelName: string, maxPages: number): Promise<CrawledPlaylistSummary[]> {
  try {
    const headers: Record<string, string> = {
      "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
      "Accept-Language": "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7",
      Cookie: "SOCS=CAESEwgDEgk2ODE4Mzg2NDQaAmVuIAEaBgiA_LyaBg",
    };
    const res = await fetch(`https://www.youtube.com/${handle}/playlists`, { headers, signal: AbortSignal.timeout(9000) });
    if (!res.ok) return [];
    const html = await res.text();
    const dataMatch = html.match(/var ytInitialData = (\{.+?\});<\/script>/s);
    const apiKey = (html.match(/"INNERTUBE_API_KEY":"([^"]+)"/) || [])[1];
    const clientVersion = (html.match(/"INNERTUBE_CLIENT_VERSION":"([^"]+)"/) || [])[1] || "2.20250101.00.00";
    if (!dataMatch || !apiKey) return [];

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const initial: any = JSON.parse(dataMatch[1]);
    const tabs = initial?.contents?.twoColumnBrowseResultsRenderer?.tabs;
    const plTab = tabs?.find((t: any) => t?.tabRenderer?.selected) ?? tabs?.[0];
    const plRoot = plTab?.tabRenderer?.content ?? initial;
    const seen = new Map<string, CrawledPlaylistSummary>();

    const walk = (node: any, tokensOut: string[]) => {
      if (!node || typeof node !== "object") return;
      const lvm = node.lockupViewModel;
      if (lvm) {
        const cid = String(lvm.contentId ?? "");
        const title = String(lvm.metadata?.lockupMetadataViewModel?.title?.content ?? "").trim();
        const thumbVm = lvm.contentImage?.collectionThumbnailViewModel?.primaryThumbnail?.thumbnailViewModel;
        const thumbUrl = String(thumbVm?.image?.sources?.[0]?.url ?? "");
        const badgeText = String(thumbVm?.overlays?.[0]?.thumbnailOverlayBadgeViewModel?.thumbnailBadges?.[0]?.thumbnailBadgeViewModel?.text ?? "");
        const epNumMatch = badgeText.match(/\d+/);
        if (cid.startsWith("PL") && title && !isSkippablePlaylistTitle(title) && !seen.has(cid)) {
          seen.set(cid, {
            playlistId: cid,
            title,
            cleanTitle: cleanPlaylistDisplayTitle(title) || title,
            channelName,
            thumbnail: thumbUrl,
            episodeHint: epNumMatch ? Number(epNumMatch[0]) : 12,
            audioLang: isDubIndoTitle(title) ? "DUB_ID" : "SUB",
          });
        }
      }
      const tok = node.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token;
      if (typeof tok === "string" && tok.length > 8) tokensOut.push(tok);
      for (const v of Object.values(node)) walk(v, tokensOut);
    };

    const initialTokens: string[] = [];
    walk(plRoot, initialTokens);
    let nextToken: string | null = initialTokens[initialTokens.length - 1] ?? null;
    let page = 1;

    while (nextToken && page < maxPages) {
      const tokenToUse = nextToken;
      nextToken = null;
      page++;
      const contRes = await fetch(`https://www.youtube.com/youtubei/v1/browse?key=${apiKey}`, {
        method: "POST",
        headers: { ...headers, "Content-Type": "application/json" },
        body: JSON.stringify({ context: { client: { clientName: "WEB", clientVersion, hl: "id", gl: "ID" } }, continuation: tokenToUse }),
        signal: AbortSignal.timeout(7000),
      });
      if (!contRes.ok) break;
      const contJson = await contRes.json();
      const newTokens: string[] = [];
      const before = seen.size;
      walk(contJson, newTokens);
      nextToken = newTokens[newTokens.length - 1] ?? null;
      if (seen.size === before) break;
    }

    const dedup = new Map<string, CrawledPlaylistSummary>();
    for (const item of seen.values()) {
      const slugNorm = item.cleanTitle.toLowerCase().replace(/[^a-z0-9]+/g, "");
      if (slugNorm.length < 2) continue;
      const key = `${item.audioLang}:${slugNorm}`;
      const prev = dedup.get(key);
      if (!prev || item.episodeHint > prev.episodeHint) dedup.set(key, item);
    }
    return [...dedup.values()];
  } catch {
    return [];
  }
}

async function getAllLiveChannelPlaylists(): Promise<CrawledPlaylistSummary[]> {
  const key = "yt-all-channel-playlists-v3";
  const cached = getCached<CrawledPlaylistSummary[]>(key);
  if (cached) return cached;

  const dbRows: CrawledPlaylistSummary[] = db && dbReady
    ? await db`SELECT playlist_id, title, clean_title, channel_name, episode_hint, sample_video_id, audio_lang, anilist_id FROM official_playlists ORDER BY anilist_id IS NULL, channel_name DESC, episode_hint DESC`
        .then((rows) =>
          (rows as Array<{ playlist_id: string; title: string; clean_title: string; channel_name: string; episode_hint: number; sample_video_id: string; audio_lang: string; anilist_id: number | null }>)
            .filter((r) => !isSkippablePlaylistTitle(r.title))
            .map((r) => ({
              playlistId: r.playlist_id,
              title: r.title,
              cleanTitle: r.clean_title,
              channelName: r.channel_name,
              thumbnail: r.sample_video_id ? `https://i.ytimg.com/vi/${r.sample_video_id}/hqdefault.jpg` : "",
              episodeHint: Number(r.episode_hint || 12),
              audioLang: (r.audio_lang === "DUB_ID" || isDubIndoTitle(r.title) ? "DUB_ID" : "SUB") as "SUB" | "DUB_ID",
              anilistId: r.anilist_id ? Number(r.anilist_id) : null,
            }))
        )
        .catch(() => [] as CrawledPlaylistSummary[])
    : [];

  // Prefer verified clean PostgreSQL catalog; fall back to live crawl only when DB is empty
  if (dbRows.length > 0) return setCached(key, dbRows, 2 * 60 * 60 * 1000);

  const [museList, anioneList] = await Promise.all([
    crawlPublicChannelPlaylists("@MuseIndonesia", "Muse Indonesia", 16),
    crawlPublicChannelPlaylists("@AniOneID", "Ani-One Indonesia", 10),
  ]);
  const byId = new Map<string, CrawledPlaylistSummary>();
  for (const item of [...museList, ...anioneList]) {
    if (!byId.has(item.playlistId)) byId.set(item.playlistId, item);
  }
  return setCached(key, [...byId.values()], 2 * 60 * 60 * 1000);
}

async function handleSeedOfficialPlaylists(req: Request): Promise<Response> {
  const seedSecret = (Bun.env.ADMIN_SEED_SECRET ?? "").trim();
  if (seedSecret) {
    const provided = (req.headers.get("x-admin-secret") ?? getBearerToken(req) ?? "").trim();
    if (provided !== seedSecret) {
      return jsonResponse({ status: false, message: "Forbidden: invalid admin seed secret" }, 403);
    }
  }
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const body = await parseJsonBody<{ clearFirst?: boolean; items?: Array<{ id: string; title: string; cleanTitle: string; channel: string; epCount: number; ep1Id: string; audioLang?: string; anilistId?: number | null }> }>(req);
  const items = (body?.items ?? []).filter(
    (x) =>
      typeof x?.id === "string" &&
      /^PL[A-Za-z0-9_-]{8,40}$/.test(x.id) &&
      (x.channel === "Muse Indonesia" || x.channel === "Ani-One Indonesia") &&
      typeof x.title === "string" &&
      x.title.trim().length > 0
  );
  if (items.length === 0) return jsonResponse({ status: false, message: "No valid items" }, 400);
  if (body?.clearFirst === true) await db`DELETE FROM official_playlists`;
  let inserted = 0;
  for (const it of items) {
    const clean = (it.cleanTitle || cleanPlaylistDisplayTitle(it.title) || it.title).trim().slice(0, 180);
    const ep1 = typeof it.ep1Id === "string" && /^[A-Za-z0-9_-]{8,16}$/.test(it.ep1Id) ? it.ep1Id : "";
    const lang = it.audioLang === "DUB_ID" || isDubIndoTitle(it.title) ? "DUB_ID" : "SUB";
    const aid = it.anilistId && Number.isFinite(Number(it.anilistId)) ? Number(it.anilistId) : null;
    await db`
      INSERT INTO official_playlists (playlist_id, title, clean_title, channel_name, episode_hint, sample_video_id, audio_lang, anilist_id)
      VALUES (${it.id}, ${it.title.trim().slice(0, 220)}, ${clean}, ${it.channel}, ${Math.max(1, Number(it.epCount) || 12)}, ${ep1}, ${lang}, ${aid})
      ON CONFLICT (playlist_id) DO UPDATE SET
        title = EXCLUDED.title,
        clean_title = EXCLUDED.clean_title,
        episode_hint = EXCLUDED.episode_hint,
        audio_lang = EXCLUDED.audio_lang,
        anilist_id = EXCLUDED.anilist_id,
        sample_video_id = CASE WHEN EXCLUDED.sample_video_id <> '' THEN EXCLUDED.sample_video_id ELSE official_playlists.sample_video_id END
    `;
    inserted++;
  }
  cache.clear();
  const cnt = await db`SELECT COUNT(*)::int AS c, COUNT(*) FILTER (WHERE audio_lang = 'DUB_ID')::int AS dub_c FROM official_playlists`;
  return jsonResponse({ status: "success", upserted: inserted, totalInDb: Number((cnt[0] as { c: number })?.c ?? inserted), totalDubInDb: Number((cnt[0] as { dub_c: number })?.dub_c ?? 0) });
}

function matchLivePlaylistForMedia(
  media: AniListMedia,
  allPlaylists: CrawledPlaylistSummary[]
): CrawledPlaylistSummary | null {
  const candidateNames = [media.title?.english, media.title?.romaji, media.title?.native]
    .filter((t): t is string => Boolean(t && t.trim().length >= 3))
    .map((t) => normalizeTitleForMatch(t))
    .filter((t) => t.length >= 4);

  if (candidateNames.length === 0) return null;

  const extractSeasonAndPart = (raw: string): { season: number; part: number } => {
    const lower = raw.toLowerCase();
    const sMatch =
      lower.match(/\b(?:season|s)\s*([1-9])\b/) ||
      lower.match(/\b(ii|iii|iv|v)\b/) ||
      lower.match(/\b([2-9])\s*(?:nd|rd|th)\s*season\b/) ||
      lower.match(/\b([2-9])\b\s*$/);
    let season = 1;
    if (sMatch) {
      const v = sMatch[1];
      season = v === "ii" ? 2 : v === "iii" ? 3 : v === "iv" ? 4 : v === "v" ? 5 : Number(v) || 1;
    }
    const pMatch = lower.match(/\b(?:part|cour)\s*([1-4])\b/);
    const part = pMatch ? Number(pMatch[1]) : 1;
    return { season, part };
  };

  const candMeta = [media.title?.english, media.title?.romaji, media.title?.native]
    .filter((t): t is string => Boolean(t && t.trim().length >= 3))
    .map((raw) => ({ norm: normalizeTitleForMatch(raw), ...extractSeasonAndPart(raw) }))
    .filter((c) => c.norm.length >= 4);

  for (const pl of allPlaylists) {
    const normPl = normalizeTitleForMatch(pl.cleanTitle);
    if (normPl.length < 4) continue;
    const plMeta = extractSeasonAndPart(pl.cleanTitle);
    for (const cand of candMeta) {
      if (cand.season !== plMeta.season) continue;
      if (cand.part !== plMeta.part && /\b(?:part|cour)\s*[1-4]\b/i.test(pl.cleanTitle)) continue;
      if (normPl === cand.norm) return pl;
      if (cand.norm.length >= 9 && normPl.includes(cand.norm)) return pl;
      if (normPl.length >= 9 && cand.norm.includes(normPl)) return pl;
    }
  }
  return null;
}

async function isVideoEmbeddable(videoId: string): Promise<boolean> {
  const key = `yt-embed:${videoId}`;
  const cached = getCached<boolean>(key);
  if (cached !== null) return cached;
  try {
    const res = await fetch(
      `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${encodeURIComponent(videoId)}&format=json`,
      { signal: AbortSignal.timeout(3500) }
    );
    if (res.ok) return setCached(key, true, 30 * 60 * 1000);
    if (res.status === 401 || res.status === 403 || res.status === 404) {
      return setCached(key, false, 30 * 60 * 1000);
    }
    return true;
  } catch {
    return true;
  }
}

function applyCuratedPlaylistRule(entries: YtRssEntry[], rule?: CuratedPlaylistRule): YtRssEntry[] {
  const noiseRegex = /\b(preview|trailer|teaser|pv|semua episode|full marathon|kompilasi|cuplikan|highlight|opening|ending|karaoke|promo|interview|talkshow|nonton bareng)\b|#short/i;
  return entries.filter((entry) => {
    const t = entry.title ?? "";
    if (!t || noiseRegex.test(t)) return false;
    if (!rule) return true;
    if (rule.requireTitleRegex && !rule.requireTitleRegex.test(t)) return false;
    if (rule.excludeTitleRegex && rule.excludeTitleRegex.test(t)) return false;
    const parsed = parseOfficialEpisodeTitle(t);
    if (!rule.allowDecimalRecaps && parsed.sortKey !== Math.floor(parsed.sortKey)) {
      return false;
    }
    if (rule.epMin !== undefined && parsed.episodeNumber < rule.epMin) return false;
    if (rule.epMax !== undefined && parsed.episodeNumber > rule.epMax) return false;
    return true;
  });
}

async function filterEmbeddableEpisodes(entries: YtRssEntry[], rule?: CuratedPlaylistRule): Promise<YtRssEntry[]> {
  const cleanCandidates = applyCuratedPlaylistRule(entries, rule);
  if (cleanCandidates.length === 0) return [];

  const playable: YtRssEntry[] = [];
  const batchSize = 12;
  for (let i = 0; i < cleanCandidates.length; i += batchSize) {
    const chunk = cleanCandidates.slice(i, i + batchSize);
    const checks = await Promise.all(
      chunk.map(async (entry) => ({
        entry,
        ok: await isVideoEmbeddable(entry.videoId),
      }))
    );
    for (const item of checks) {
      if (item.ok) playable.push(item.entry);
    }
  }

  const sorted = playable.sort((a, b) => {
    const pa = parseOfficialEpisodeTitle(a.title);
    const pb = parseOfficialEpisodeTitle(b.title);
    if (pa.sortKey !== pb.sortKey) return pa.sortKey - pb.sortKey;
    return a.videoId.localeCompare(b.videoId);
  });

  // Deduplicate re-uploads that share the exact same episode sortKey (keep first embeddable)
  const deduped: YtRssEntry[] = [];
  const seenKeys = new Set<number>();
  for (const ep of sorted) {
    const sk = parseOfficialEpisodeTitle(ep.title).sortKey;
    if (seenKeys.has(sk)) continue;
    seenKeys.add(sk);
    deduped.push(ep);
  }
  return deduped;
}

function normalizeTitleForMatch(text: string): string {
  return text
    .toLowerCase()
    .replace(/[:\-–—_.,!?'"()\[\]【】]/g, " ")
    .replace(/\b(season|cour|part|the|animation|takarir|indonesia|bahasa|sub|indo|dub)\b/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

async function getRecentChannelOfficialEpisodes(): Promise<YtRssEntry[]> {
  const key = "yt-recent-channels-v1";
  const cached = getCached<YtRssEntry[]>(key);
  if (cached) return cached;

  const feedResults = await Promise.all(
    OFFICIAL_YT_CHANNELS.map((ch) =>
      fetchYouTubeAtomFeed(
        `https://www.youtube.com/feeds/videos.xml?channel_id=${ch.channelId}`,
        ch.name
      )
    )
  );
  const filtered = feedResults
    .flat()
    .filter((entry) => isOfficialFullEpisodeTitle(entry.title))
    .sort((a, b) => new Date(b.published).getTime() - new Date(a.published).getTime());

  return setCached(key, filtered, 8 * 60 * 1000);
}

function matchRecentEpisodesForMedia(media: AniListMedia, recentEntries: YtRssEntry[]): YtRssEntry[] {
  const candidateNames = [media.title?.english, media.title?.romaji, media.title?.native]
    .filter((t): t is string => Boolean(t && t.trim().length >= 3))
    .map((t) => normalizeTitleForMatch(t))
    .filter((t) => t.length >= 4);

  if (candidateNames.length === 0) return [];

  return recentEntries.filter((entry) => {
    const parsedSeries = normalizeTitleForMatch(parseOfficialEpisodeTitle(entry.title).seriesTitle);
    const fullNorm = normalizeTitleForMatch(entry.title);
    if (parsedSeries.length < 4) return false;
    return candidateNames.some((cand) => {
      if (parsedSeries === cand) return true;
      if (cand.length >= 6 && (parsedSeries.includes(cand) || fullNorm.includes(cand))) return true;
      if (parsedSeries.length >= 6 && cand.includes(parsedSeries)) return true;
      return false;
    });
  });
}

function buildPlaylistCard(pl: CrawledPlaylistSummary, idNum: number, isDub: boolean) {
  const slug = isDub ? `dub-${pl.playlistId}` : `ytpl-${pl.playlistId}`;
  return {
    id: idNum,
    title: pl.cleanTitle,
    japanese_title: isDub ? `Dub Indo • ${pl.channelName}` : pl.channelName,
    slug,
    poster: pl.thumbnail,
    banner: pl.thumbnail,
    synopsis: `Koleksi episode ${isDub ? "Dub Indo" : "Sub Indo"} dari ${pl.channelName}. Ketuk untuk menonton langsung di AnimWatch.`,
    genres: isDub ? ["Dub Indo", "Anime", pl.channelName === "Muse Indonesia" ? "Fantasy" : "Action"] : ["Anime", pl.channelName === "Muse Indonesia" ? "Fantasy" : "Action"],
    rating: isDub ? 8.4 : 8.3,
    rating_count: pl.channelName,
    views_label: `${pl.episodeHint} Ep`,
    studio: pl.channelName,
    status: "ONGOING",
    type: "TV Series",
    release: "2025",
    year: 2025,
    season: isDub ? "Dub Indo" : pl.channelName,
    duration: "24 menit",
    duration_minutes: 24,
    total_episodes: Math.max(pl.episodeHint, 12),
    released_episodes: Math.max(pl.episodeHint, 1),
    episode: `Ep ${Math.max(pl.episodeHint, 1)}`,
    latest_eps_slug: slug,
    airing_day: null,
    airing_time: null,
    trailer_url: "",
    is_official_free: true,
    is_dub_indo: isDub,
    audio_lang: isDub ? "DUB_ID" : "SUB",
    official_channel: pl.channelName,
    stream_type: "official_episode",
    external_links: [{ site: pl.channelName, url: `https://www.youtube.com/playlist?list=${pl.playlistId}` }],
  };
}

async function fetchAniListMediaByIds(ids: number[]): Promise<Map<number, AniListMedia>> {
  const uniqueIds = [...new Set(ids.filter((id) => Number.isFinite(id) && id > 0))];
  const out = new Map<number, AniListMedia>();
  if (uniqueIds.length === 0) return out;
  const chunks: number[][] = [];
  for (let i = 0; i < uniqueIds.length; i += 50) chunks.push(uniqueIds.slice(i, i + 50));
  const q = `query ($ids: [Int]) { Page(page: 1, perPage: 50) { media(id_in: $ids, type: ANIME, isAdult: false) { ${MEDIA_CARD_FIELDS} } } }`;
  const results = await Promise.all(
    chunks.map((c) => queryAniList<{ Page: { media: AniListMedia[] } }>(q, { ids: c }).catch(() => ({ Page: { media: [] } })))
  );
  for (const r of results) for (const m of r.Page.media) out.set(m.id, m);
  return out;
}

interface AdminEpisodeInput {
  eps_title: string;
  eps_slug: string;
  number: number;
  stream_url: string;
  stream_type: string;
  thumbnail: string;
}

interface AdminAnimeRow {
  slug: string;
  title: string;
  japanese_title: string;
  poster: string;
  banner: string;
  synopsis: string;
  genres: string[];
  rating: number;
  studio: string;
  status: string;
  type: string;
  year: number;
  season: string;
  total_episodes: number;
  released_episodes: number;
  duration_minutes: number;
  is_official_free: boolean;
  is_dub_indo: boolean;
  official_channel: string;
  playlist_id: string;
  anilist_id: number | null;
  episodes: AdminEpisodeInput[];
  is_hidden: boolean;
  is_featured: boolean;
  updated_at?: string;
}

function parseJsonArray<T>(raw: unknown, fallback: T[]): T[] {
  if (Array.isArray(raw)) return raw as T[];
  if (typeof raw === "string") {
    try {
      const p = JSON.parse(raw);
      if (Array.isArray(p)) return p as T[];
    } catch {}
  }
  return fallback;
}

function extractYouTubeVideoId(raw: string): string | null {
  const s = raw.trim();
  if (/^[A-Za-z0-9_-]{11}$/.test(s)) return s;
  const m =
    s.match(/[?&]v=([A-Za-z0-9_-]{11})/) ||
    s.match(/youtu\.be\/([A-Za-z0-9_-]{11})/) ||
    s.match(/youtube(?:-nocookie)?\.com\/(?:embed|shorts)\/([A-Za-z0-9_-]{11})/);
  return m?.[1] ?? null;
}

function normalizeAdminEpisodes(rawList: unknown, fallbackPoster: string, slugPrefix: string): AdminEpisodeInput[] {
  const arr = parseJsonArray<Record<string, unknown>>(rawList, []);
  return arr
    .map((ep, idx) => {
      const num = Math.max(1, Number(ep.number) || idx + 1);
      const rawUrl = String(ep.stream_url ?? ep.streamUrl ?? "").trim();
      const ytId = rawUrl ? extractYouTubeVideoId(rawUrl) : null;
      const streamUrl = ytId ? ytNoCookieEmbed(ytId) : rawUrl;
      const streamType = ytId
        ? "official_episode"
        : streamUrl
          ? String(ep.stream_type ?? ep.streamType ?? "direct_video").trim() || "direct_video"
          : "info_only";
      const thumb = String(ep.thumbnail ?? "").trim() || (ytId ? `https://i.ytimg.com/vi/${ytId}/hqdefault.jpg` : fallbackPoster);
      const epsSlug = String(ep.eps_slug ?? ep.epsSlug ?? "").trim() || (ytId ? `yt-${ytId}` : `${slugPrefix}-ep-${num}`);
      const title = String(ep.eps_title ?? ep.epsTitle ?? ep.title ?? `Episode ${num}`).trim() || `Episode ${num}`;
      return {
        eps_title: title,
        eps_slug: epsSlug,
        number: num,
        stream_url: streamUrl,
        stream_type: streamType,
        thumbnail: thumb,
      };
    })
    .sort((a, b) => a.number - b.number);
}

function normalizeAdminAnimeDbRow(r: Record<string, unknown>): AdminAnimeRow {
  const poster = String(r.poster ?? "").trim();
  const slug = String(r.slug ?? "").trim();
  return {
    slug,
    title: String(r.title ?? "").trim(),
    japanese_title: String(r.japanese_title ?? "").trim(),
    poster,
    banner: String(r.banner ?? "").trim() || poster,
    synopsis: String(r.synopsis ?? "").trim(),
    genres: parseJsonArray<string>(r.genres, ["Anime"]).map(String),
    rating: Number(r.rating ?? 8.5) || 8.5,
    studio: String(r.studio ?? "AnimWatch Admin").trim() || "AnimWatch Admin",
    status: String(r.status ?? "ONGOING").trim() || "ONGOING",
    type: String(r.type ?? "TV Series").trim() || "TV Series",
    year: Number(r.year ?? 2026) || 2026,
    season: String(r.season ?? "2026").trim() || "2026",
    total_episodes: Math.max(1, Number(r.total_episodes ?? 12) || 12),
    released_episodes: Math.max(1, Number(r.released_episodes ?? 1) || 1),
    duration_minutes: Math.max(1, Number(r.duration_minutes ?? 24) || 24),
    is_official_free: Boolean(r.is_official_free),
    is_dub_indo: Boolean(r.is_dub_indo),
    official_channel: String(r.official_channel ?? "").trim() || "AnimWatch Admin",
    playlist_id: String(r.playlist_id ?? "").trim(),
    anilist_id: r.anilist_id !== null && r.anilist_id !== undefined && Number(r.anilist_id) > 0 ? Number(r.anilist_id) : null,
    episodes: normalizeAdminEpisodes(r.episodes_json, poster, slug),
    is_hidden: Boolean(r.is_hidden),
    is_featured: Boolean(r.is_featured),
    updated_at: r.updated_at ? String(r.updated_at) : undefined,
  };
}

async function loadAdminAnimeList(): Promise<AdminAnimeRow[]> {
  const key = "admin-anime-all-v1";
  const cached = getCached<AdminAnimeRow[]>(key);
  if (cached) return cached;
  if (!db || !dbReady) return [];
  try {
    const rows = await db`SELECT * FROM admin_anime ORDER BY is_featured DESC, updated_at DESC`;
    const list = (rows as Array<Record<string, unknown>>).map(normalizeAdminAnimeDbRow);
    return setCached(key, list, 60 * 1000);
  } catch {
    return [];
  }
}

function stableNumericIdFromSlug(slug: string): number {
  let hash = 0;
  for (let i = 0; i < slug.length; i++) {
    hash = (hash * 31 + slug.charCodeAt(i)) | 0;
  }
  return 800000 + (Math.abs(hash) % 190000);
}

function mapAdminRowToCard(row: AdminAnimeRow) {
  const idNum = row.anilist_id ?? stableNumericIdFromSlug(row.slug);
  const rel = Math.max(row.released_episodes, row.episodes.length, 1);
  const tot = Math.max(row.total_episodes, rel, 1);
  return {
    id: idNum,
    title: row.title,
    japanese_title: row.japanese_title || row.official_channel,
    slug: row.slug,
    poster: row.poster,
    banner: row.banner || row.poster,
    synopsis: row.synopsis || `${row.title} tersedia di AnimWatch.`,
    genres: row.is_dub_indo && !row.genres.includes("Dub Indo") ? [...row.genres, "Dub Indo"] : row.genres,
    rating: row.rating,
    rating_count: row.official_channel || "Admin",
    views_label: `${rel} Ep`,
    studio: row.studio,
    status: row.status,
    type: row.type,
    release: String(row.year),
    year: row.year,
    season: row.season,
    duration: `${row.duration_minutes} menit`,
    duration_minutes: row.duration_minutes,
    total_episodes: tot,
    released_episodes: rel,
    episode: `Ep ${rel}`,
    latest_eps_slug: row.episodes[row.episodes.length - 1]?.eps_slug ?? row.slug,
    airing_day: null,
    airing_time: null,
    trailer_url: "",
    is_official_free: row.is_official_free || row.episodes.some((e) => Boolean(e.stream_url)) || Boolean(row.playlist_id),
    is_dub_indo: row.is_dub_indo,
    audio_lang: row.is_dub_indo ? "DUB_ID" : "SUB",
    official_channel: row.official_channel,
    stream_type: row.is_official_free || row.playlist_id ? "official_episode" : "info_only",
    external_links: row.playlist_id
      ? [{ site: row.official_channel || "YouTube", url: `https://www.youtube.com/playlist?list=${row.playlist_id}` }]
      : [],
  };
}

async function applyAdminCatalogTransform<T extends { slug: string; id?: number }>(
  items: T[],
  options: { includeCustom?: boolean; dubOnly?: boolean; featuredOnly?: boolean; searchQuery?: string } = {}
): Promise<T[]> {
  const adminRows = await loadAdminAnimeList();
  if (adminRows.length === 0) return items;

  const bySlug = new Map(adminRows.map((r) => [r.slug, r]));
  const hiddenSlugs = new Set(adminRows.filter((r) => r.is_hidden).map((r) => r.slug));
  const hiddenIds = new Set(adminRows.filter((r) => r.is_hidden && r.anilist_id).map((r) => r.anilist_id!));

  const transformed: T[] = [];
  const presentSlugs = new Set<string>();

  for (const item of items) {
    if (hiddenSlugs.has(item.slug) || (item.id && hiddenIds.has(item.id))) continue;
    const override = bySlug.get(item.slug);
    if (override) {
      transformed.push({
        ...item,
        ...mapAdminRowToCard(override),
        id: (item.id && item.id > 0) ? item.id : (override.anilist_id ?? stableNumericIdFromSlug(override.slug)),
      } as unknown as T);
    } else {
      transformed.push(item);
    }
    presentSlugs.add(item.slug);
  }

  if (options.includeCustom) {
    const qLower = options.searchQuery?.toLowerCase().trim() ?? "";
    const extras = adminRows.filter((r) => {
      if (r.is_hidden || presentSlugs.has(r.slug)) return false;
      if (options.dubOnly === true && !r.is_dub_indo) return false;
      if (options.dubOnly === false && r.is_dub_indo) return false;
      if (options.featuredOnly && !r.is_featured) return false;
      if (qLower && !r.title.toLowerCase().includes(qLower) && !r.slug.toLowerCase().includes(qLower) && !r.genres.some((g) => g.toLowerCase().includes(qLower))) {
        return false;
      }
      return true;
    });
    return [...extras.map((r) => mapAdminRowToCard(r) as unknown as T), ...transformed];
  }

  return transformed;
}

async function handleOfficialFree(page = 1): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `official-free-unified-v7:p${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  if (safePage === 1) {
    const uniqueIds = [...new Set(FEATURED_MUSE_ANILIST_IDS)];
    const byId = await fetchAniListMediaByIds(uniqueIds);
    const rawData = uniqueIds
      .map((id) => byId.get(id))
      .filter((m): m is AniListMedia => Boolean(m))
      .map((m) => {
        const common = mapMediaCommon(m);
        const pl = MUSE_ID_PLAYLIST_BY_ANILIST_ID[m.id];
        return {
          ...common,
          is_official_free: true,
          is_dub_indo: false,
          audio_lang: "SUB",
          official_channel: pl?.channelName ?? "Muse Indonesia",
          stream_type: "official_episode",
          external_links: pl ? [{ site: pl.channelName, url: `https://www.youtube.com/playlist?list=${pl.playlistId}` }] : [],
        };
      });
    const data = await applyAdminCatalogTransform(rawData, { includeCustom: true, dubOnly: false });
    return jsonResponse(setCached(key, { status: "success", source: "anilist+muse+anione", page: 1, totalCurated: data.length, data }, 10 * 60 * 1000));
  }

  // Page 2+: Remaining verified Muse & Ani-One SUB playlists enriched with official AniList covers, titles & synopses
  const curatedPlIds = new Set(Object.values(MUSE_ID_PLAYLIST_BY_ANILIST_ID).map((x) => x.playlistId));
  const curatedAlIds = new Set(Object.keys(MUSE_ID_PLAYLIST_BY_ANILIST_ID).map(Number));
  const allLive = await getAllLiveChannelPlaylists();
  const remaining = allLive.filter(
    (pl) => pl.audioLang === "SUB" && !curatedPlIds.has(pl.playlistId) && !(pl.anilistId && curatedAlIds.has(pl.anilistId))
  );
  const pageSize = 48;
  const start = (safePage - 2) * pageSize;
  const slice = remaining.slice(start, start + pageSize);
  const byAniListId = await fetchAniListMediaByIds(slice.map((p) => p.anilistId ?? 0));

  const emittedSlugs = new Set<string>();
  const rawData = slice.map((pl, idx) => {
    const media = pl.anilistId ? byAniListId.get(pl.anilistId) : undefined;
    if (media) {
      const common = mapMediaCommon(media);
      const uniqueSlug = emittedSlugs.has(common.slug) ? `ytpl-${pl.playlistId}` : common.slug;
      emittedSlugs.add(uniqueSlug);
      return {
        ...common,
        slug: uniqueSlug,
        is_official_free: true,
        is_dub_indo: false,
        audio_lang: "SUB",
        official_channel: pl.channelName,
        stream_type: "official_episode",
        external_links: [{ site: pl.channelName, url: `https://www.youtube.com/playlist?list=${pl.playlistId}` }],
      };
    }
    return buildPlaylistCard(pl, 900000 + start + idx, false);
  });
  const data = await applyAdminCatalogTransform(rawData, { includeCustom: false });

  return jsonResponse(
    setCached(key, { status: "success", source: "anilist+youtube-channels-sub", page: safePage, hasMore: start + pageSize < remaining.length, data }, 10 * 60 * 1000)
  );
}

async function handleDubIndo(page = 1): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `dub-indo-v3:p${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const allLive = await getAllLiveChannelPlaylists();
  const splitPlaylistIds = new Set(DUB_SPLIT_RULES.map((r) => r.playlistId));
  const dubPlaylists = allLive.filter((pl) => pl.audioLang === "DUB_ID" && !splitPlaylistIds.has(pl.playlistId));

  const allIdsNeeded = [
    ...(safePage === 1 ? DUB_SPLIT_RULES.map((r) => r.anilistId) : []),
    ...dubPlaylists.map((p) => p.anilistId ?? 0),
  ];
  const byAniListId = await fetchAniListMediaByIds(allIdsNeeded);

  const splitCards = safePage === 1
    ? DUB_SPLIT_RULES.map((rule, idx) => {
        const media = byAniListId.get(rule.anilistId);
        const dubSlug = `dub-${rule.playlistId}--${rule.partKey}`;
        if (media) {
          const common = mapMediaCommon(media);
          const epCount = Math.max(rule.epCountHint, common.total_episodes, 1);
          return {
            ...common,
            id: 960000 + idx,
            slug: dubSlug,
            title: rule.titleOverride ?? `${common.title} (Dub Indo)`,
            released_episodes: epCount,
            total_episodes: epCount,
            episode: `Ep ${epCount}`,
            latest_eps_slug: dubSlug,
            genres: common.genres.includes("Dub Indo") ? common.genres : [...common.genres, "Dub Indo"],
            is_official_free: true,
            is_dub_indo: true,
            audio_lang: "DUB_ID",
            official_channel: rule.channelName,
            stream_type: "official_episode",
            external_links: [{ site: rule.channelName, url: `https://www.youtube.com/playlist?list=${rule.playlistId}` }],
          };
        }
        return buildPlaylistCard(
          {
            playlistId: `${rule.playlistId}--${rule.partKey}`,
            title: rule.titleOverride ?? `Dub Indo ${rule.partKey.toUpperCase()}`,
            cleanTitle: rule.titleOverride ?? `Dub Indo ${rule.partKey.toUpperCase()}`,
            channelName: rule.channelName,
            thumbnail: "",
            episodeHint: rule.epCountHint,
            audioLang: "DUB_ID",
            anilistId: rule.anilistId,
          },
          960000 + idx,
          true
        );
      })
    : [];

  const pageSize = 70;
  const start = (safePage - 1) * pageSize;
  const slice = dubPlaylists.slice(start, start + pageSize);

  const regularCards = slice.map((pl, idx) => {
    const media = pl.anilistId ? byAniListId.get(pl.anilistId) : undefined;
    if (media) {
      const common = mapMediaCommon(media);
      const dubSlug = `dub-${pl.playlistId}`;
      const epCount = Math.max(pl.episodeHint, common.released_episodes, 1);
      return {
        ...common,
        slug: dubSlug,
        released_episodes: epCount,
        total_episodes: Math.max(epCount, common.total_episodes),
        episode: `Ep ${epCount}`,
        latest_eps_slug: dubSlug,
        genres: common.genres.includes("Dub Indo") ? common.genres : [...common.genres, "Dub Indo"],
        is_official_free: true,
        is_dub_indo: true,
        audio_lang: "DUB_ID",
        official_channel: pl.channelName,
        stream_type: "official_episode",
        external_links: [{ site: pl.channelName, url: `https://www.youtube.com/playlist?list=${pl.playlistId}` }],
      };
    }
    return buildPlaylistCard(pl, 980000 + start + idx, true);
  });

  const data = await applyAdminCatalogTransform([...splitCards, ...regularCards], {
    includeCustom: safePage === 1,
    dubOnly: true,
  });

  return jsonResponse(
    setCached(
      key,
      { status: "success", source: "dub-indo-live", page: safePage, hasMore: start + pageSize < dubPlaylists.length, total: splitCards.length + dubPlaylists.length, data },
      10 * 60 * 1000
    )
  );
}

// ======================================================
// Anime Catalog & Tracker Engine (AniList Live)
// ======================================================

function slugify(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 56);
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
  if (!raw) return "Sinopsis anime belum tersedia.";
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

const FORMAT_MAP: Record<string, string> = { MOVIE: "Movie", OVA: "OVA", SPECIAL: "OVA", ONA: "ONA" };
const STATUS_MAP: Record<string, string> = { RELEASING: "ONGOING", FINISHED: "COMPLETED", NOT_YET_RELEASED: "UPCOMING" };
const SEASON_MAP: Record<string, string> = { WINTER: "Winter", SPRING: "Spring", SUMMER: "Summer", FALL: "Fall" };
const mapFormat = (f?: string | null) => FORMAT_MAP[(f ?? "").toUpperCase()] ?? "TV Series";
const mapStatus = (s?: string | null) => STATUS_MAP[(s ?? "").toUpperCase()] ?? "ONGOING";
const mapSeason = (s?: string | null, y?: number | null) => `${SEASON_MAP[(s ?? "").toUpperCase()] ?? "Musim"}${y ? ` ${y}` : ""}`;

async function queryAniList<T>(query: string, variables: Record<string, unknown> = {}): Promise<T> {
  const res = await fetch(ANILIST_ENDPOINT, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
      "User-Agent": "AnimWatch-API/2.2",
    },
    body: JSON.stringify({ query, variables }),
    signal: AbortSignal.timeout(12000),
  });

  if (!res.ok) throw new Error(`AniList HTTP ${res.status}`);
  const payload = (await res.json()) as { data?: T; errors?: Array<{ message: string }> };
  if (payload.errors?.length) throw new Error(payload.errors.map((e) => e.message).join("; "));
  if (!payload.data) throw new Error("AniList returned empty data");
  return payload.data;
}

interface AniListExternalLink { site?: string | null; url?: string | null; type?: string | null; language?: string | null; }
interface AniListMedia {
  id: number;
  title?: { romaji?: string | null; english?: string | null; native?: string | null };
  coverImage?: { extraLarge?: string | null; large?: string | null };
  bannerImage?: string | null; description?: string | null; averageScore?: number | null;
  popularity?: number | null; episodes?: number | null; duration?: number | null;
  seasonYear?: number | null; startDate?: { year?: number | null } | null;
  season?: string | null; status?: string | null; format?: string | null; genres?: string[] | null;
  studios?: { nodes?: Array<{ name?: string | null }> | null } | null;
  nextAiringEpisode?: { episode?: number | null; airingAt?: number | null } | null;
  trailer?: { id?: string | null; site?: string | null } | null;
  externalLinks?: AniListExternalLink[] | null;
  streamingEpisodes?: Array<{ title?: string | null; thumbnail?: string | null; url?: string | null; site?: string | null }> | null;
  characters?: { edges?: Array<{ role?: string | null; node?: { name?: { full?: string | null } | null } | null; voiceActors?: Array<{ name?: { full?: string | null } | null }> | null }> | null } | null;
  relations?: { edges?: Array<{ relationType?: string | null; node?: { id: number; type?: string | null; title?: { romaji?: string | null; english?: string | null } | null } | null }> | null } | null;
}

const MEDIA_CARD_FIELDS = `id title { romaji english native } coverImage { large extraLarge } bannerImage description(asHtml: false) averageScore popularity episodes duration seasonYear startDate { year } season status format genres studios(isMain: true) { nodes { name } } nextAiringEpisode { episode airingAt } trailer { id site }`;

function pickTitle(m: AniListMedia): string {
  return m.title?.english?.trim() || m.title?.romaji?.trim() || m.title?.native?.trim() || `Anime #${m.id}`;
}

function mapMediaCommon(m: AniListMedia) {
  const title = pickTitle(m);
  const slug = makeAnimeSlug(m.title?.romaji || title, m.id);
  // Use coverImage.large (medium-lightweight HD) instead of huge extraLarge to keep Android memory lean
  const poster = m.coverImage?.large || m.coverImage?.extraLarge || "";
  const banner = m.bannerImage || m.coverImage?.extraLarge || poster;
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
      : status === "UPCOMING"
      ? 0
      : Math.min(totalEpisodes, 1);
  const year = m.seasonYear || m.startDate?.year || 2026;
  const studio = m.studios?.nodes?.[0]?.name?.trim() || "Studio Anime";
  const schedule = formatWibSchedule(m.nextAiringEpisode?.airingAt);
  const genres = (m.genres ?? []).filter(Boolean);
  const officialMapping = MUSE_ID_PLAYLIST_BY_ANILIST_ID[m.id];
  const cachedLive = officialMapping ? null : getCached<CrawledPlaylistSummary[]>("yt-all-channel-playlists-v3")?.filter((p) => p.audioLang === "SUB") ?? null;
  const matchedLive = officialMapping ? null : cachedLive ? (cachedLive.find((p) => p.anilistId === m.id) ?? matchLivePlaylistForMedia(m, cachedLive)) : null;
  const isPlayable = Boolean(officialMapping || matchedLive);
  const channelLabel = officialMapping?.channelName ?? matchedLive?.channelName ?? null;

  return {
    id: m.id,
    title,
    japanese_title: m.title?.native || m.title?.romaji || title,
    slug,
    poster,
    banner,
    synopsis: cleanDescription(m.description),
    genres: genres.length ? genres : ["Anime"],
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
    trailer_url: "",
    is_official_free: isPlayable,
    is_dub_indo: false,
    audio_lang: "SUB",
    official_channel: channelLabel,
  };
}

async function handleAniListList(keyPrefix: string, perPage: number, mediaArgs: string, page = 1): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `${keyPrefix}:v3:${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);
  const query = `query ($page: Int) { Page(page: $page, perPage: ${perPage}) { media(type: ANIME, ${mediaArgs}, isAdult: false) { ${MEDIA_CARD_FIELDS} } } }`;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { page: safePage });
  const rawData = result.Page.media.map(mapMediaCommon);
  const data = await applyAdminCatalogTransform(rawData, {
    includeCustom: safePage === 1 && (keyPrefix === "popular" || keyPrefix === "latest"),
    featuredOnly: keyPrefix === "popular",
  });
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", page: safePage, data }));
}

const handlePopular = (page = 1) => handleAniListList("popular", 50, "sort: [TRENDING_DESC, POPULARITY_DESC]", page);
const handleLatest = (page: number) => handleAniListList("latest", 45, "status: RELEASING, sort: [POPULARITY_DESC]", page);
const handleMovies = (page: number) => handleAniListList("movies", 35, "format: MOVIE, sort: [POPULARITY_DESC, SCORE_DESC]", page);
const handleCompleted = (page: number) => handleAniListList("completed", 45, "status: FINISHED, format: TV, sort: [SCORE_DESC, POPULARITY_DESC]", page);

async function handleSearch(rawQuery: string): Promise<Response> {
  const q = decodeURIComponent(rawQuery).trim();
  if (!q) return jsonResponse({ status: "success", query: "", data: [] });
  const key = `search:v2:${q.toLowerCase()}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const query = `
    query ($search: String) {
      Page(page: 1, perPage: 18) {
        media(type: ANIME, search: $search, sort: [SEARCH_MATCH, POPULARITY_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { search: q });
  const anilistItems = result.Page.media.map(mapMediaCommon);

  // Also include matching live Muse Indonesia & Ani-One Indonesia playlists (both Sub & Dub Indo separated!)
  const qNorm = q.toLowerCase().trim();
  const cachedLive = await getAllLiveChannelPlaylists();
  const curatedIds = new Set(Object.values(MUSE_ID_PLAYLIST_BY_ANILIST_ID).map((x) => x.playlistId));
  const seenSubIds = new Set(anilistItems.map((a) => a.id));
  const seenSubTitles = new Set(anilistItems.map((a) => normalizeTitleForMatch(a.title)));
  const anilistById = new Map(anilistItems.map((a) => [a.id, a]));
  const rawYtHits = cachedLive
    .filter((pl) => {
      const hits = pl.cleanTitle.toLowerCase().includes(qNorm) || pl.title.toLowerCase().includes(qNorm);
      if (!hits) return false;
      if (pl.audioLang === "DUB_ID") return true;
      if (pl.anilistId && seenSubIds.has(pl.anilistId)) return false;
      return !curatedIds.has(pl.playlistId) && !seenSubTitles.has(normalizeTitleForMatch(pl.cleanTitle));
    })
    .slice(0, 10);
  const missingMetaIds = rawYtHits.map((p) => p.anilistId ?? 0).filter((id) => id > 0 && !anilistById.has(id));
  const extraMeta = missingMetaIds.length > 0 ? await fetchAniListMediaByIds(missingMetaIds) : new Map<number, AniListMedia>();
  const ytMatches = rawYtHits.map((pl, idx) => {
    const isDub = pl.audioLang === "DUB_ID";
    const linked = pl.anilistId ? (anilistById.get(pl.anilistId) ?? (extraMeta.has(pl.anilistId) ? mapMediaCommon(extraMeta.get(pl.anilistId)!) : undefined)) : undefined;
    if (linked) {
      return {
        ...linked,
        slug: isDub ? `dub-${pl.playlistId}` : linked.slug,
        genres: isDub && !linked.genres.includes("Dub Indo") ? [...linked.genres, "Dub Indo"] : linked.genres,
        is_official_free: true,
        is_dub_indo: isDub,
        audio_lang: isDub ? "DUB_ID" : "SUB",
        official_channel: pl.channelName,
      };
    }
    return buildPlaylistCard(pl, (isDub ? 970000 : 950000) + idx, isDub);
  });

  const data = await applyAdminCatalogTransform([...anilistItems, ...ytMatches], {
    includeCustom: true,
    searchQuery: q,
  });
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", query: q, data }, 3 * 60 * 1000));
}

async function fetchAniListMediaDetail(slugOrId: string): Promise<AniListMedia | null> {
  const id = extractIdFromSlug(slugOrId);
  const detailFields = `
    ${MEDIA_CARD_FIELDS}
    externalLinks { site url type language }
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
    const query = `query ($id: Int) { Media(id: $id, type: ANIME) { ${detailFields} } }`;
    const res = await queryAniList<{ Media: AniListMedia | null }>(query, { id });
    return res.Media;
  }

  const searchTerm = decodeURIComponent(slugOrId).replace(/-/g, " ").trim();
  const query = `query ($search: String) { Media(search: $search, type: ANIME) { ${detailFields} } }`;
  const res = await queryAniList<{ Media: AniListMedia | null }>(query, { search: searchTerm });
  return res.Media;
}

  const mapYtEpisodes = (list: YtRssEntry[], rule?: CuratedPlaylistRule) => {
    const usedNumbers = new Set<number>();
    const hasSplitRange = rule?.epMin !== undefined;
    return list.slice(0, 120).map((ytEp, idx) => {
      const parsed = parseOfficialEpisodeTitle(ytEp.title);
      let parsedNum = parsed.episodeNumber;
      if (hasSplitRange && rule?.epMin !== undefined) {
        parsedNum = rule.epMin === 0 ? parsedNum + 1 : parsedNum >= rule.epMin ? parsedNum - rule.epMin + 1 : idx + 1;
      } else if (!Number.isFinite(parsedNum) || parsedNum <= 0) {
        parsedNum = idx + 1;
      }
      while (usedNumbers.has(parsedNum)) parsedNum++;
      usedNumbers.add(parsedNum);
      return {
        eps_title: ytEp.title,
        eps_slug: `yt-${ytEp.videoId}`,
        number: parsedNum,
        stream_url: ytNoCookieEmbed(ytEp.videoId),
        stream_type: "official_episode",
        thumbnail: ytEp.thumbnail,
      };
    });
  };

const mapMediaCharacters = (media: AniListMedia | null | undefined, fallbackVa = "Seiyuu Jepang") =>
  (media?.characters?.edges ?? []).map((e) => {
    const name = e.node?.name?.full?.trim();
    return name ? { name, role: e.role === "MAIN" ? "Karakter Utama" : "Karakter Pendukung", voice_actor: e.voiceActors?.[0]?.name?.full?.trim() || fallbackVa, emblem: name.slice(0, 1).toUpperCase() } : null;
  }).filter(Boolean);

async function handleAnimeDetail(rawSlug: string): Promise<Response> {
  const slug = decodeURIComponent(rawSlug).trim();
  const key = `anime:v2:${slug}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const adminRows = await loadAdminAnimeList();
  const adminRow = adminRows.find((r) => r.slug === slug);
  if (adminRow?.is_hidden) {
    return jsonResponse({ status: "error", message: `Anime sedang disembunyikan oleh Admin: ${slug}` }, 404);
  }

  const mergeAdminDetail = async (baseData: Record<string, any>) => {
    if (!adminRow) return baseData;
    let adminEpisodes = adminRow.episodes;
    if (adminEpisodes.length === 0 && adminRow.playlist_id) {
      const ytEntries = await fetchFullYouTubePlaylistEntries(adminRow.playlist_id, adminRow.official_channel || "Muse Indonesia");
      const playable = await filterEmbeddableEpisodes(ytEntries);
      if (playable.length > 0) adminEpisodes = mapYtEpisodes(playable);
    }
    const finalEpisodes = adminEpisodes.length > 0 ? adminEpisodes : (baseData.episodes ?? []);
    const relCount = Math.max(adminRow.released_episodes, finalEpisodes.length, Number(baseData.released_episodes ?? 1));
    const totCount = Math.max(adminRow.total_episodes, relCount, Number(baseData.total_episodes ?? 12));
    return {
      ...baseData,
      title: adminRow.title || baseData.title,
      japanese_title: adminRow.japanese_title || baseData.japanese_title,
      poster: adminRow.poster || baseData.poster,
      banner: adminRow.banner || adminRow.poster || baseData.banner,
      synopsis: adminRow.synopsis || baseData.synopsis,
      genres: adminRow.genres.length > 0 ? adminRow.genres : baseData.genres,
      rating: adminRow.rating || baseData.rating,
      studio: adminRow.studio || baseData.studio,
      status: adminRow.status || baseData.status,
      year: adminRow.year || baseData.year,
      season: adminRow.season || baseData.season,
      duration_minutes: adminRow.duration_minutes || baseData.duration_minutes,
      total_episodes: totCount,
      released_episodes: relCount,
      is_official_free: adminRow.is_official_free || finalEpisodes.some((e: any) => Boolean(e.stream_url)),
      is_dub_indo: adminRow.is_dub_indo,
      audio_lang: adminRow.is_dub_indo ? "DUB_ID" : "SUB",
      official_channel: adminRow.official_channel || baseData.official_channel,
      episodes: finalEpisodes,
    };
  };

  if (slug.startsWith("ytpl-") || slug.startsWith("dub-")) {
    const isDub = slug.startsWith("dub-");
    const rawIdPart = slug.slice(isDub ? 4 : 5).trim();
    const [playlistId, partKey] = rawIdPart.split("--");
    const dubSplitRule = isDub && partKey
      ? DUB_SPLIT_RULES.find((r) => r.playlistId === playlistId && r.partKey === partKey)
      : undefined;
    const meta = (await getAllLiveChannelPlaylists()).find((p) => p.playlistId === playlistId);
    const channelName = dubSplitRule?.channelName ?? meta?.channelName ?? "Muse Indonesia";
    const targetAnilistId = dubSplitRule?.anilistId ?? meta?.anilistId ?? null;
    const [rawEntries, linkedMedia] = await Promise.all([
      fetchFullYouTubePlaylistEntries(playlistId, channelName),
      targetAnilistId ? fetchAniListMediaDetail(String(targetAnilistId)).catch(() => null) : Promise.resolve(null),
    ]);
    const playable = await filterEmbeddableEpisodes(rawEntries, dubSplitRule);
    const lc = linkedMedia ? mapMediaCommon(linkedMedia) : null;
    const displayTitle = dubSplitRule?.titleOverride ?? (isDub && lc?.title ? `${lc.title} (Dub Indo)` : lc?.title) ?? meta?.cleanTitle ?? (rawEntries[0] ? parseOfficialEpisodeTitle(rawEntries[0].title).seriesTitle : "Anime Muse Indonesia");
    const poster = lc?.poster || meta?.thumbnail || playable[0]?.thumbnail || rawEntries[0]?.thumbnail || "";
    const episodes = mapYtEpisodes(playable, dubSplitRule);
    const baseGenres = lc?.genres ?? ["Anime", "Action", "Fantasy"];
    const baseData = {
      title: displayTitle,
      japanese_title: isDub ? `Dub Indo • ${channelName}` : lc?.japanese_title || channelName,
      poster,
      banner: lc?.banner || poster,
      synopsis: lc?.synopsis || `Tonton episode ${displayTitle} (${isDub ? "Dub Indo" : "Sub Indo"}) langsung dari ${channelName} di dalam AnimWatch.`,
      genres: isDub && !baseGenres.includes("Dub Indo") ? [...baseGenres, "Dub Indo"] : baseGenres,
      rating: lc?.rating ?? 8.3,
      rating_count: lc?.rating_count ?? channelName,
      studio: lc?.studio ?? channelName,
      status: lc?.status ?? "ONGOING",
      year: lc?.year ?? 2025,
      season: lc?.season ?? (isDub ? "Dub Indo" : channelName),
      duration_minutes: lc?.duration_minutes ?? 24,
      total_episodes: Math.max(episodes.length, dubSplitRule?.epCountHint ?? lc?.total_episodes ?? meta?.episodeHint ?? 12),
      released_episodes: Math.max(episodes.length, 1),
      is_official_free: episodes.length > 0,
      is_dub_indo: isDub,
      audio_lang: isDub ? "DUB_ID" : "SUB",
      official_channel: channelName,
      episodes,
      characters: mapMediaCharacters(linkedMedia, "Seiyuu"),
      related_slugs: [],
      external_links: [{ site: channelName, url: `https://www.youtube.com/playlist?list=${playlistId}` }],
    };
    return jsonResponse(setCached(key, {
      status: "success",
      source: isDub ? "dub-indo-playlist" : "youtube-playlist-live",
      animeData: await mergeAdminDetail(baseData),
    }));
  }

  if (slug.startsWith("ytfree-")) {
    const feedJson = (await (await handleOfficialFree(1)).json()) as { data?: Array<Record<string, unknown>> };
    const matched = feedJson.data?.find((item) => item.slug === slug);
    if (matched) return jsonResponse({ status: "success", source: "official-youtube-id", animeData: matched });
  }

  const media = await fetchAniListMediaDetail(slug).catch(() => null);
  if (!media) {
    if (adminRow) {
      const card = mapAdminRowToCard(adminRow);
      const merged = await mergeAdminDetail({ ...card, episodes: adminRow.episodes, characters: [], related_slugs: [] });
      return jsonResponse(setCached(key, { status: "success", source: "admin-custom", animeData: merged }));
    }
    return jsonResponse({ status: "error", message: `Anime tidak ditemukan: ${slug}` }, 404);
  }

  const common = mapMediaCommon(media);
  const streamingLinks = (media.externalLinks ?? []).filter((l) => l.type === "STREAMING" && l.site && l.url).map((l) => ({ site: l.site!.trim(), url: l.url!.trim() }));
  const curatedOfficial = adminRow?.playlist_id
    ? { playlistId: adminRow.playlist_id, channelName: (adminRow.official_channel as "Muse Indonesia" | "Ani-One Indonesia") || "Muse Indonesia" }
    : MUSE_ID_PLAYLIST_BY_ANILIST_ID[media.id];
  const ytPlaylistLink = streamingLinks.find((l) => l.url.includes("youtube.com/playlist") && l.url.includes("list="));
  let officialPlaylistEpisodes: YtRssEntry[] = [];
  let officialChannelName: string | null = null;
  let activeRule: CuratedPlaylistRule | undefined = undefined;

  const tryPlaylist = async (plId: string, ch: string, rule?: CuratedPlaylistRule) => {
    const rawList = await fetchFullYouTubePlaylistEntries(plId, ch);
    const eps = await filterEmbeddableEpisodes(rawList, rule);
    if (eps.length > 0) {
      officialPlaylistEpisodes = eps;
      officialChannelName = ch;
      activeRule = rule;
    }
  };

  if (curatedOfficial) await tryPlaylist(curatedOfficial.playlistId, curatedOfficial.channelName, curatedOfficial);
  if (officialPlaylistEpisodes.length === 0) {
    const subLive = (await getAllLiveChannelPlaylists()).filter((p) => p.audioLang === "SUB");
    const matchedPl = subLive.find((p) => p.anilistId === media.id) ?? matchLivePlaylistForMedia(media, subLive);
    if (matchedPl) await tryPlaylist(matchedPl.playlistId, matchedPl.channelName);
  }
  if (officialPlaylistEpisodes.length === 0) {
    const recent = (await getRecentChannelOfficialEpisodes()).filter((e) => !isDubIndoTitle(e.title));
    const matchedLive = await filterEmbeddableEpisodes(matchRecentEpisodesForMedia(media, recent));
    if (matchedLive.length > 0) { officialPlaylistEpisodes = matchedLive; officialChannelName = matchedLive[0].channelName; }
  }
  if (officialPlaylistEpisodes.length === 0 && ytPlaylistLink) {
    const plMatch = ytPlaylistLink.url.match(/[?&]list=([a-zA-Z0-9_-]+)/);
    if (plMatch?.[1]) await tryPlaylist(plMatch[1], "YouTube");
  }

  const streamEps = (media.streamingEpisodes ?? []).filter((e) => e?.title);
  const episodes = officialPlaylistEpisodes.length > 0
    ? mapYtEpisodes(officialPlaylistEpisodes, activeRule)
    : Array.from({ length: Math.min(Math.max(common.released_episodes, Math.min(streamEps.length, 48), 1), 48) }, (_, i) => {
        const epNum = i + 1;
        const m = streamEps.find((se) => parseOfficialEpisodeTitle(se.title ?? "").episodeNumber === epNum) ?? streamEps[i];
        const cleanEpTitle = m?.title ? m.title.replace(/^Episode\s+\d+\s*[-:]\s*/i, "").trim() || `Episode ${epNum}` : `Episode ${epNum}`;
        return { eps_title: `Episode ${epNum}: ${cleanEpTitle}`, eps_slug: `ep-${media.id}-${epNum}`, number: epNum, stream_url: "", stream_type: "info_only", thumbnail: m?.thumbnail || common.poster };
      });

  const relatedSlugs = (media.relations?.edges ?? []).filter((e) => e.node?.type === "ANIME" && e.node.id).slice(0, 6)
    .map((e) => makeAnimeSlug(e.node?.title?.romaji || e.node?.title?.english || `anime-${e.node?.id}`, e.node!.id));

  const baseData = {
    ...common,
    total_episodes: Math.max(episodes.length, common.total_episodes),
    released_episodes: officialPlaylistEpisodes.length > 0 ? Math.max(episodes.length, 1) : common.released_episodes,
    is_official_free: officialPlaylistEpisodes.length > 0,
    official_channel: officialChannelName,
    episodes,
    characters: mapMediaCharacters(media),
    related_slugs: relatedSlugs,
    external_links: streamingLinks,
  };

  return jsonResponse(setCached(key, {
    status: "success",
    source: officialPlaylistEpisodes.length > 0 ? "anilist+youtube-official" : "anilist-live",
    animeData: await mergeAdminDetail(baseData),
  }));
}

async function handleEpisodeDetail(rawSlug: string): Promise<Response> {
  const slug = decodeURIComponent(rawSlug).trim();
  const key = `episode:${slug}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  // Direct official YouTube episode slug (yt-<videoId>)
  const ytMatch = slug.trim().match(/^yt-([a-zA-Z0-9_-]{8,16})$/);
  if (ytMatch) {
    const videoId = ytMatch[1];
    const payload = {
      status: "success",
      source: "official-youtube",
      eps_detail: {
        title: "Episode Siap Putar",
        poster: `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`,
        stream_url: ytNoCookieEmbed(videoId),
        stream_type: "official_episode",
        synopsis: "Episode siap diputar langsung di pemutar AnimWatch.",
        anime_slug: "-",
        next_eps_slug: "-",
        prev_eps_slug: "-",
      },
    };
    return jsonResponse(setCached(key, payload));
  }

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
    source: "catalog-live",
    eps_detail: {
      title: epTitle,
      poster: matched?.thumbnail || common.poster,
      stream_url: "",
      stream_type: "info_only",
      synopsis: common.synopsis,
      anime_slug: common.slug,
      next_eps_slug: epNum < maxEp ? `ep-${media.id}-${epNum + 1}` : "-",
      prev_eps_slug: epNum > 1 ? `ep-${media.id}-${epNum - 1}` : "-",
    },
  };

  return jsonResponse(setCached(key, payload));
}

// ======================================================
// Admin CRUD APIs (PostgreSQL-backed Live CMS)
// ======================================================

async function handleAdminVerify(req: Request): Promise<Response> {
  const body = await parseJsonBody<{ secret?: string }>(req);
  const providedSecret = (body?.secret ?? req.headers.get("x-admin-secret") ?? "").trim();
  const user = await getAuthenticatedUser(req);

  if (user && resolveUserRole(user) === "admin") {
    return jsonResponse({ status: "success", isAdmin: true, user: serializeUser(user) });
  }

  const adminSecret = (Bun.env.ADMIN_SEED_SECRET ?? "").trim();
  if (adminSecret && providedSecret === adminSecret) {
    if (user && db && dbReady) {
      const updated = await db`
        UPDATE users SET role = 'admin', member_tier = 'ADMIN', updated_at = NOW()
        WHERE id = ${Number(user.id)}
        RETURNING id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
                  appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
      `;
      if (updated[0]) {
        return jsonResponse({ status: "success", isAdmin: true, user: serializeUser(updated[0] as DbUserRow) });
      }
    }
    return jsonResponse({ status: "success", isAdmin: true, user: user ? serializeUser(user) : null });
  }

  return jsonResponse({ status: false, isAdmin: false, message: "Akses ditolak: akun bukan Admin atau kunci verifikasi salah" }, 403);
}

async function handleAdminOverview(req: Request): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum terhubung" }, 503);

  const [userStats, commentStats, playlistStats, adminStats, adminAnimes, playlists, recentComments, recentUsers] = await Promise.all([
    db`SELECT COUNT(*)::int AS c FROM users`,
    db`SELECT COUNT(*)::int AS c FROM comments`,
    db`SELECT COUNT(*)::int AS c FROM official_playlists`,
    db`SELECT COUNT(*)::int AS c, COUNT(*) FILTER (WHERE is_hidden)::int AS hidden_c FROM admin_anime`,
    loadAdminAnimeList(),
    db`SELECT playlist_id, title, clean_title, channel_name, episode_hint, sample_video_id, audio_lang, anilist_id
       FROM official_playlists ORDER BY anilist_id IS NULL, channel_name DESC, title ASC LIMIT 200`,
    db`SELECT id, user_id, anime_id, episode_number, author_name, author_badge, content, is_spoiler, created_at
       FROM comments ORDER BY created_at DESC LIMIT 60`,
    db`SELECT id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
              appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings, created_at
       FROM users ORDER BY created_at DESC LIMIT 50`,
  ]);

  const uRow = userStats[0] as { c?: number } | undefined;
  const cRow = commentStats[0] as { c?: number } | undefined;
  const pRow = playlistStats[0] as { c?: number } | undefined;
  const aRow = adminStats[0] as { c?: number; hidden_c?: number } | undefined;

  return jsonResponse({
    status: "success",
    stats: {
      totalUsers: Number(uRow?.c ?? 0),
      totalComments: Number(cRow?.c ?? 0),
      totalPlaylists: Number(pRow?.c ?? 0),
      totalAdminAnime: Number(aRow?.c ?? 0),
      hiddenAnimeCount: Number(aRow?.hidden_c ?? 0),
      curatedRulesCount: Object.keys(CURATED_PLAYLIST_RULES).length,
      splitPartRulesCount:
        DUB_SPLIT_RULES.length +
        Object.values(CURATED_PLAYLIST_RULES).filter((r) => r.epMin !== undefined).length,
    },
    adminAnimes,
    playlists: (playlists as Array<Record<string, unknown>>).map((p) => ({
      playlistId: String(p.playlist_id ?? ""),
      title: String(p.title ?? ""),
      cleanTitle: String(p.clean_title ?? ""),
      channelName: String(p.channel_name ?? "Muse Indonesia"),
      episodeHint: Number(p.episode_hint ?? 12),
      sampleVideoId: String(p.sample_video_id ?? ""),
      audioLang: String(p.audio_lang ?? "SUB"),
      anilistId: p.anilist_id ? Number(p.anilist_id) : null,
    })),
    recentComments: (recentComments as Array<Record<string, unknown>>).map((c) => ({
      id: String(c.id),
      animeId: String(c.anime_id ?? ""),
      episodeNumber: c.episode_number ? Number(c.episode_number) : null,
      authorName: String(c.author_name ?? "User"),
      authorBadge: String(c.author_badge ?? "Member"),
      content: String(c.content ?? ""),
      isSpoiler: Boolean(c.is_spoiler),
      timeLabel: formatRelativeTime(c.created_at),
    })),
    recentUsers: (recentUsers as Array<DbUserRow>).map((u) => serializeUser(u)),
  });
}

async function handleAdminListAnime(req: Request): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  const rows = await loadAdminAnimeList();
  return jsonResponse({ status: "success", data: rows });
}

interface AdminSaveAnimeBody {
  slug?: string;
  title?: string;
  japanese_title?: string;
  poster?: string;
  banner?: string;
  synopsis?: string;
  genres?: string[] | string;
  rating?: number;
  studio?: string;
  status?: string;
  type?: string;
  year?: number;
  season?: string;
  total_episodes?: number;
  released_episodes?: number;
  duration_minutes?: number;
  is_official_free?: boolean;
  is_dub_indo?: boolean;
  official_channel?: string;
  playlist_id?: string;
  anilist_id?: number | null;
  episodes?: Array<Record<string, unknown>>;
  is_hidden?: boolean;
  is_featured?: boolean;
}

async function handleAdminSaveAnime(req: Request, rawPathSlug?: string): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);

  const body = await parseJsonBody<AdminSaveAnimeBody>(req);
  if (!body) return jsonResponse({ status: false, message: "Payload JSON tidak valid" }, 400);

  const existingSlug = rawPathSlug ? decodeURIComponent(rawPathSlug).trim() : "";
  const titleInput = (body.title ?? "").trim();
  const slug = existingSlug || (body.slug ?? "").trim() || `${slugify(titleInput || "custom-anime")}-${Date.now().toString().slice(-4)}`;
  if (!slug) return jsonResponse({ status: false, message: "Slug atau judul anime wajib diisi" }, 400);

  const existingRows = await db`SELECT * FROM admin_anime WHERE slug = ${slug} LIMIT 1`;
  const prev = existingRows[0] ? normalizeAdminAnimeDbRow(existingRows[0] as Record<string, unknown>) : null;

  const title = titleInput || prev?.title || "";
  if (!title) return jsonResponse({ status: false, message: "Judul anime tidak boleh kosong" }, 400);

  const japaneseTitle = body.japanese_title !== undefined ? body.japanese_title.trim() : (prev?.japanese_title ?? "");
  const poster = body.poster !== undefined ? body.poster.trim() : (prev?.poster ?? "");
  const banner = body.banner !== undefined ? body.banner.trim() : (prev?.banner || poster);
  const synopsis = body.synopsis !== undefined ? body.synopsis.trim() : (prev?.synopsis ?? "");
  const genres = body.genres !== undefined
    ? (Array.isArray(body.genres) ? body.genres.map((g) => String(g).trim()).filter(Boolean) : String(body.genres).split(",").map((g) => g.trim()).filter(Boolean))
    : (prev?.genres ?? ["Anime"]);
  const finalGenres = genres.length > 0 ? genres : ["Anime"];
  const rating = body.rating !== undefined && Number.isFinite(Number(body.rating)) ? Math.min(10, Math.max(0, Number(body.rating))) : (prev?.rating ?? 8.5);
  const studio = body.studio !== undefined ? body.studio.trim() : (prev?.studio ?? "AnimWatch Admin");
  const status = body.status !== undefined ? body.status.trim().toUpperCase() : (prev?.status ?? "ONGOING");
  const type = body.type !== undefined ? body.type.trim() : (prev?.type ?? "TV Series");
  const year = body.year !== undefined && Number.isFinite(Number(body.year)) ? Number(body.year) : (prev?.year ?? 2026);
  const season = body.season !== undefined ? body.season.trim() : (prev?.season ?? String(year));
  const durationMinutes = body.duration_minutes !== undefined && Number.isFinite(Number(body.duration_minutes)) ? Math.max(1, Number(body.duration_minutes)) : (prev?.duration_minutes ?? 24);
  const episodes = body.episodes !== undefined ? normalizeAdminEpisodes(body.episodes, poster, slug) : (prev?.episodes ?? []);
  const releasedEpisodes = body.released_episodes !== undefined && Number.isFinite(Number(body.released_episodes))
    ? Math.max(0, Number(body.released_episodes))
    : Math.max(prev?.released_episodes ?? 0, episodes.length, 1);
  const totalEpisodes = body.total_episodes !== undefined && Number.isFinite(Number(body.total_episodes))
    ? Math.max(releasedEpisodes, Number(body.total_episodes), 1)
    : Math.max(prev?.total_episodes ?? 12, releasedEpisodes, 1);
  const playlistId = body.playlist_id !== undefined ? body.playlist_id.trim() : (prev?.playlist_id ?? "");
  const isOfficialFree = body.is_official_free !== undefined ? Boolean(body.is_official_free) : (prev?.is_official_free ?? (Boolean(playlistId) || episodes.some((e) => Boolean(e.stream_url))));
  const isDubIndo = body.is_dub_indo !== undefined ? Boolean(body.is_dub_indo) : (prev?.is_dub_indo ?? false);
  const officialChannel = body.official_channel !== undefined ? body.official_channel.trim() : (prev?.official_channel ?? "AnimWatch Admin");
  const anilistId = body.anilist_id !== undefined ? (body.anilist_id && Number(body.anilist_id) > 0 ? Number(body.anilist_id) : null) : (prev?.anilist_id ?? extractIdFromSlug(slug));
  const isHidden = body.is_hidden !== undefined ? Boolean(body.is_hidden) : (prev?.is_hidden ?? false);
  const isFeatured = body.is_featured !== undefined ? Boolean(body.is_featured) : (prev?.is_featured ?? true);

  const saved = await db`
    INSERT INTO admin_anime (
      slug, title, japanese_title, poster, banner, synopsis, genres, rating, studio,
      status, type, year, season, total_episodes, released_episodes, duration_minutes,
      is_official_free, is_dub_indo, official_channel, playlist_id, anilist_id,
      episodes_json, is_hidden, is_featured, updated_at
    )
    VALUES (
      ${slug}, ${title}, ${japaneseTitle}, ${poster}, ${banner}, ${synopsis},
      ${JSON.stringify(finalGenres)}::jsonb, ${rating}, ${studio},
      ${status}, ${type}, ${year}, ${season}, ${totalEpisodes}, ${releasedEpisodes}, ${durationMinutes},
      ${isOfficialFree}, ${isDubIndo}, ${officialChannel}, ${playlistId}, ${anilistId},
      ${JSON.stringify(episodes)}::jsonb, ${isHidden}, ${isFeatured}, NOW()
    )
    ON CONFLICT (slug) DO UPDATE SET
      title = EXCLUDED.title,
      japanese_title = EXCLUDED.japanese_title,
      poster = EXCLUDED.poster,
      banner = EXCLUDED.banner,
      synopsis = EXCLUDED.synopsis,
      genres = EXCLUDED.genres,
      rating = EXCLUDED.rating,
      studio = EXCLUDED.studio,
      status = EXCLUDED.status,
      type = EXCLUDED.type,
      year = EXCLUDED.year,
      season = EXCLUDED.season,
      total_episodes = EXCLUDED.total_episodes,
      released_episodes = EXCLUDED.released_episodes,
      duration_minutes = EXCLUDED.duration_minutes,
      is_official_free = EXCLUDED.is_official_free,
      is_dub_indo = EXCLUDED.is_dub_indo,
      official_channel = EXCLUDED.official_channel,
      playlist_id = EXCLUDED.playlist_id,
      anilist_id = EXCLUDED.anilist_id,
      episodes_json = EXCLUDED.episodes_json,
      is_hidden = EXCLUDED.is_hidden,
      is_featured = EXCLUDED.is_featured,
      updated_at = NOW()
    RETURNING *
  `;

  cache.clear();
  return jsonResponse({ status: "success", anime: normalizeAdminAnimeDbRow(saved[0] as Record<string, unknown>) });
}

async function handleAdminDeleteAnime(req: Request, rawSlug: string): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const slug = decodeURIComponent(rawSlug).trim();
  await db`DELETE FROM admin_anime WHERE slug = ${slug}`;
  cache.clear();
  return jsonResponse({ status: "success", deleted: slug });
}

async function handleAdminListPlaylists(req: Request): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  const rows = db && dbReady
    ? await db`SELECT playlist_id, title, clean_title, channel_name, episode_hint, sample_video_id, audio_lang, anilist_id
                FROM official_playlists ORDER BY anilist_id IS NULL, channel_name DESC, title ASC LIMIT 250`
    : [];
  return jsonResponse({
    status: "success",
    playlists: (rows as Array<Record<string, unknown>>).map((p) => ({
      playlistId: String(p.playlist_id ?? ""),
      title: String(p.title ?? ""),
      cleanTitle: String(p.clean_title ?? ""),
      channelName: String(p.channel_name ?? "Muse Indonesia"),
      episodeHint: Number(p.episode_hint ?? 12),
      sampleVideoId: String(p.sample_video_id ?? ""),
      audioLang: String(p.audio_lang ?? "SUB"),
      anilistId: p.anilist_id ? Number(p.anilist_id) : null,
    })),
    splitParts: [
      ...DUB_SPLIT_RULES.map((r) => ({
        key: `${r.playlistId}:${r.partKey}`,
        playlistId: r.playlistId,
        anilistId: r.anilistId,
        channelName: r.channelName,
        audioLang: "DUB_ID",
        epMin: r.epMin ?? null,
        epMax: r.epMax ?? null,
      })),
      ...Object.entries(CURATED_PLAYLIST_RULES)
        .filter(([, r]) => r.epMin !== undefined)
        .map(([aid, r]) => ({
          key: `${r.playlistId}:${aid}`,
          playlistId: r.playlistId,
          anilistId: Number(aid),
          channelName: r.channelName,
          audioLang: "SUB",
          epMin: r.epMin ?? null,
          epMax: r.epMax ?? null,
        })),
    ],
  });
}

async function handleAdminSavePlaylist(req: Request, rawPlaylistId?: string): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);

  const body = await parseJsonBody<{
    playlistId?: string;
    title?: string;
    cleanTitle?: string;
    channelName?: string;
    episodeHint?: number;
    sampleVideoId?: string;
    audioLang?: string;
    anilistId?: number | null;
  }>(req);
  const playlistId = (rawPlaylistId ? decodeURIComponent(rawPlaylistId) : body?.playlistId ?? "").trim();
  const title = (body?.title ?? body?.cleanTitle ?? "").trim();
  if (!playlistId || !title) {
    return jsonResponse({ status: false, message: "Playlist ID dan judul wajib diisi" }, 400);
  }
  const cleanTitle = (body?.cleanTitle || cleanPlaylistDisplayTitle(title) || title).trim().slice(0, 180);
  const channelName = (body?.channelName ?? "Muse Indonesia").trim() || "Muse Indonesia";
  const episodeHint = Math.max(1, Number(body?.episodeHint) || 12);
  const sampleVideoId = (body?.sampleVideoId ?? "").trim();
  const audioLang = body?.audioLang === "DUB_ID" || isDubIndoTitle(title) ? "DUB_ID" : "SUB";
  const anilistId = body?.anilistId && Number(body.anilistId) > 0 ? Number(body.anilistId) : null;

  const saved = await db`
    INSERT INTO official_playlists (playlist_id, title, clean_title, channel_name, episode_hint, sample_video_id, audio_lang, anilist_id)
    VALUES (${playlistId}, ${title.slice(0, 220)}, ${cleanTitle}, ${channelName}, ${episodeHint}, ${sampleVideoId}, ${audioLang}, ${anilistId})
    ON CONFLICT (playlist_id) DO UPDATE SET
      title = EXCLUDED.title,
      clean_title = EXCLUDED.clean_title,
      channel_name = EXCLUDED.channel_name,
      episode_hint = EXCLUDED.episode_hint,
      audio_lang = EXCLUDED.audio_lang,
      anilist_id = EXCLUDED.anilist_id,
      sample_video_id = CASE WHEN EXCLUDED.sample_video_id <> '' THEN EXCLUDED.sample_video_id ELSE official_playlists.sample_video_id END
    RETURNING *
  `;
  cache.clear();
  const p = saved[0] as Record<string, unknown>;
  return jsonResponse({
    status: "success",
    playlist: {
      playlistId: String(p.playlist_id),
      title: String(p.title),
      cleanTitle: String(p.clean_title),
      channelName: String(p.channel_name),
      episodeHint: Number(p.episode_hint),
      sampleVideoId: String(p.sample_video_id ?? ""),
      audioLang: String(p.audio_lang),
      anilistId: p.anilist_id ? Number(p.anilist_id) : null,
    },
  });
}

async function handleAdminDeletePlaylist(req: Request, rawPlaylistId: string): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const playlistId = decodeURIComponent(rawPlaylistId).trim();
  await db`DELETE FROM official_playlists WHERE playlist_id = ${playlistId}`;
  cache.clear();
  return jsonResponse({ status: "success", deleted: playlistId });
}

async function handleAdminDeleteComment(req: Request, rawCommentId: string): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const cid = Number(rawCommentId);
  if (!Number.isFinite(cid) || cid <= 0) return jsonResponse({ status: false, message: "ID komentar tidak valid" }, 400);
  await db`DELETE FROM comments WHERE id = ${cid}`;
  cache.clear();
  return jsonResponse({ status: "success", deleted: cid });
}

async function handleAdminUpdateUser(req: Request, rawUserId: string): Promise<Response> {
  const auth = await requireAdmin(req);
  if (!auth.ok) return auth.response;
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const uid = Number(rawUserId);
  if (!Number.isFinite(uid) || uid <= 0) return jsonResponse({ status: false, message: "ID user tidak valid" }, 400);
  const body = await parseJsonBody<{ role?: string; memberTier?: string }>(req);
  const role = body?.role === "admin" ? "admin" : body?.role === "user" ? "user" : null;
  const tier = body?.memberTier?.trim() || null;

  const updated = await db`
    UPDATE users
    SET
      role = COALESCE(${role}, role),
      member_tier = COALESCE(${tier}, member_tier),
      updated_at = NOW()
    WHERE id = ${uid}
    RETURNING id, email, display_name, handle, bio, avatar_emblem, avatar_url, member_tier, role,
              appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
  `;
  if (!updated[0]) return jsonResponse({ status: false, message: "User tidak ditemukan" }, 404);
  return jsonResponse({ status: "success", user: serializeUser(updated[0] as DbUserRow) });
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
          service: "AnimWatch API",
          mode: CUSTOM_SOURCE_URL ? "custom-source+anilist-live" : "anilist+official-yt-id",
          database: dbReady ? "connected" : "unavailable",
          routes: {
            catalog: ["/api/official-free", "/api/dub-indo", "/api/popular", "/api/latest", "/api/movie", "/api/completed", "/api/search/:query", "/api/anime/:slug", "/api/episode/:slug"],
            auth: ["GET /api/auth/config", "POST /api/auth/register", "POST /api/auth/login", "POST /api/auth/google", "GET /api/auth/google/start", "GET /api/auth/google/callback", "POST /api/auth/logout"],
            user: ["GET /api/user/me", "PATCH /api/user/profile", "PATCH /api/user/preferences", "PUT /api/user/watchlist", "PUT /api/user/history", "DELETE /api/user/history", "PUT /api/user/ratings"],
            comments: ["GET /api/comments/:animeId", "POST /api/comments", "POST /api/comments/:id/like"],
            admin: ["POST /api/admin/verify", "GET /api/admin/overview", "GET/POST /api/admin/anime", "PUT/DELETE /api/admin/anime/:slug", "GET/POST /api/admin/playlists", "PUT/DELETE /api/admin/playlists/:playlistId", "DELETE /api/admin/comments/:id", "PATCH /api/admin/users/:id"],
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
      if (method === "GET" && path === "/api/auth/config") return handleAuthConfig();
      if (method === "POST" && path === "/api/auth/register") return await handleRegister(req);
      if (method === "POST" && path === "/api/auth/login") return await handleLogin(req);
      if (method === "POST" && path === "/api/auth/google") return await handleGoogleIdTokenAuth(req);
      if (method === "GET" && path === "/api/auth/google/start") return handleGoogleOAuthStart(req);
      if (method === "GET" && path === "/api/auth/google/direct") return await handleGoogleDirectWebCallback(req);
      if (method === "GET" && path === "/api/auth/google/callback") return await handleGoogleOAuthCallback(req);
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

      // Admin CRUD Routes
      if (method === "POST" && path === "/api/admin/verify") return await handleAdminVerify(req);
      if (method === "GET" && path === "/api/admin/overview") return await handleAdminOverview(req);
      if (method === "GET" && path === "/api/admin/anime") return await handleAdminListAnime(req);
      if (method === "POST" && path === "/api/admin/anime") return await handleAdminSaveAnime(req);
      const adminAnimeMatch = path.match(/^\/api\/admin\/anime\/(.+)$/);
      if (method === "PUT" && adminAnimeMatch) return await handleAdminSaveAnime(req, adminAnimeMatch[1]);
      if (method === "DELETE" && adminAnimeMatch) return await handleAdminDeleteAnime(req, adminAnimeMatch[1]);
      if (method === "GET" && path === "/api/admin/playlists") return await handleAdminListPlaylists(req);
      if (method === "POST" && path === "/api/admin/playlists") return await handleAdminSavePlaylist(req);
      const adminPlMatch = path.match(/^\/api\/admin\/playlists\/(.+)$/);
      if (method === "PUT" && adminPlMatch) return await handleAdminSavePlaylist(req, adminPlMatch[1]);
      if (method === "DELETE" && adminPlMatch) return await handleAdminDeletePlaylist(req, adminPlMatch[1]);
      const adminCommentMatch = path.match(/^\/api\/admin\/comments\/([^/]+)$/);
      if (method === "DELETE" && adminCommentMatch) return await handleAdminDeleteComment(req, adminCommentMatch[1]);
      const adminUserMatch = path.match(/^\/api\/admin\/users\/([^/]+)$/);
      if (method === "PATCH" && adminUserMatch) return await handleAdminUpdateUser(req, adminUserMatch[1]);

      // Comments Routes
      if (method === "POST" && path === "/api/comments") return await handlePostComment(req);
      const likeCommentMatch = path.match(/^\/api\/comments\/([^/]+)\/like$/);
      if (method === "POST" && likeCommentMatch) return await handleToggleCommentLike(req, likeCommentMatch[1]);
      const delCommentMatch = path.match(/^\/api\/comments\/([^/]+)$/);
      if (method === "DELETE" && delCommentMatch) return await handleDeleteComment(req, delCommentMatch[1]);
      const getCommentsMatch = path.match(/^\/api\/comments\/(.+)$/);
      if (method === "GET" && getCommentsMatch) return await handleGetComments(req, getCommentsMatch[1]);

      // Official Legal Free (Sub Indo) + Dub Indo + AniList Catalog Routes
      if (method === "POST" && path === "/api/official-free/seed") return await handleSeedOfficialPlaylists(req);
      if (path === "/api/dub-indo") return await handleDubIndo(1);
      const dubIndoPageMatch = path.match(/^\/api\/dub-indo\/page\/(\d+)$/);
      if (dubIndoPageMatch) return await handleDubIndo(Number(dubIndoPageMatch[1]));
      if (path === "/api/official-free") return await handleOfficialFree(1);
      const offFreePageMatch = path.match(/^\/api\/official-free\/page\/(\d+)$/);
      if (offFreePageMatch) return await handleOfficialFree(Number(offFreePageMatch[1]));
      if (path === "/api/popular") return await handlePopular(1);
      const popularPageMatch = path.match(/^\/api\/popular\/page\/(\d+)$/);
      if (popularPageMatch) return await handlePopular(Number(popularPageMatch[1]));
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
      if (error instanceof URIError) {
        return jsonResponse({ status: false, message: "Invalid URL encoding in request path" }, 400);
      }
      const message = error instanceof Error ? error.message : String(error);
      return jsonResponse({ status: false, message }, 502);
    }
  },
});

console.log(`AnimWatch Official Streaming + AniList + PostgreSQL API listening on port ${port}`);
// Pre-warm live Muse Indonesia & Ani-One Indonesia playlist cache in background
getAllLiveChannelPlaylists()
  .then((list) => console.log(`[YT] Pre-warmed ${list.length} channel playlists`))
  .catch(() => {});
