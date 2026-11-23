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

    await db`SELECT 1`;

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
        google_sub TEXT,
        auth_provider TEXT NOT NULL DEFAULT 'email',
        avatar_url TEXT NOT NULL DEFAULT '',
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );
      ALTER TABLE users ADD COLUMN IF NOT EXISTS google_sub TEXT;
      ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_provider TEXT NOT NULL DEFAULT 'email';
      ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url TEXT NOT NULL DEFAULT '';
      CREATE UNIQUE INDEX IF NOT EXISTS idx_users_google_sub ON users(google_sub) WHERE google_sub IS NOT NULL;

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
        watched_episodes INTEGER NOT NULL DEFAULT 0,
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        PRIMARY KEY (user_id, anime_id)
      );
      ALTER TABLE watchlist ADD COLUMN IF NOT EXISTS watched_episodes INTEGER NOT NULL DEFAULT 0;

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

      CREATE TABLE IF NOT EXISTS official_playlists (
        playlist_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        clean_title TEXT NOT NULL,
        channel_name TEXT NOT NULL,
        episode_hint INTEGER NOT NULL DEFAULT 12,
        sample_video_id TEXT NOT NULL DEFAULT ''
      );
    `);

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
    db`SELECT anime_id, category, watched_episodes FROM watchlist WHERE user_id = ${userId} ORDER BY updated_at DESC`,
    db`SELECT anime_id, episode_id, episode_number, episode_title, position_seconds, duration_seconds, updated_at
       FROM watch_history WHERE user_id = ${userId} ORDER BY updated_at DESC LIMIT 40`,
    db`SELECT anime_id, score FROM user_ratings WHERE user_id = ${userId}`,
  ]);

  const watchlist: Record<string, string> = {};
  const watchedEpisodes: Record<string, number> = {};
  for (const r of watchlistRows as Array<{ anime_id: string; category: string; watched_episodes: number }>) {
    watchlist[r.anime_id] = r.category;
    if (Number(r.watched_episodes) > 0) {
      watchedEpisodes[r.anime_id] = Number(r.watched_episodes);
    }
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
    watchedEpisodes,
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

  const existing = await db`
    SELECT id, email, display_name, handle, bio, avatar_emblem, member_tier,
           appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
    FROM users
    WHERE google_sub = ${profile.sub} OR email = ${cleanEmail}
    LIMIT 1
  `;

  let userRow: DbUserRow;
  if (existing.length > 0) {
    const found = existing[0] as DbUserRow;
    const updated = await db`
      UPDATE users
      SET google_sub = ${profile.sub},
          auth_provider = CASE WHEN auth_provider = 'email' THEN 'google' ELSE auth_provider END,
          avatar_url = CASE WHEN ${cleanPicture} <> '' THEN ${cleanPicture} ELSE avatar_url END,
          updated_at = NOW()
      WHERE id = ${Number(found.id)}
      RETURNING id, email, display_name, handle, bio, avatar_emblem, member_tier,
                appearance_mode, accent_theme, preferred_genres, has_completed_theme_setup, settings
    `;
    userRow = updated[0] as DbUserRow;
  } else {
    const randomPass = await Bun.password.hash(`google_${generateToken()}`);
    const emblem = makeEmblem(cleanName);
    const handle = makeHandle(cleanName, cleanEmail);
    const inserted = await db`
      INSERT INTO users (
        email, password_hash, display_name, handle, bio, avatar_emblem, member_tier,
        has_completed_theme_setup, google_sub, auth_provider, avatar_url
      )
      VALUES (
        ${cleanEmail}, ${randomPass}, ${cleanName}, ${handle},
        'Terhubung melalui akun Google.', ${emblem}, 'Google Member',
        FALSE, ${profile.sub}, 'google', ${cleanPicture}
      )
      RETURNING id, email, display_name, handle, bio, avatar_emblem, member_tier,
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
    googleEnabled: Boolean(GOOGLE_CLIENT_ID),
    googleWebClientId: GOOGLE_CLIENT_ID,
    googleWebOAuthEnabled: Boolean(GOOGLE_CLIENT_ID && GOOGLE_CLIENT_SECRET),
  });
}

async function handleGoogleIdTokenAuth(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const body = await parseJsonBody<{ idToken?: string }>(req);
  const idToken = (body?.idToken ?? "").trim();
  if (!idToken) {
    return jsonResponse({ status: false, message: "Google ID token wajib dikirim" }, 400);
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
  if (GOOGLE_ALLOWED_AUDIENCES.length > 0 && payload.aud && !GOOGLE_ALLOWED_AUDIENCES.includes(payload.aud)) {
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
  const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>AnimWatch Google Login</title>
  <meta http-equiv="refresh" content="0;url=${safeHref}" />
  <style>
    body{font-family:system-ui,sans-serif;background:#0C0C10;color:#F3F4F6;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0;padding:24px;text-align:center;}
    .card{max-width:360px;background:#171821;border:1px solid rgba(255,255,255,.12);border-radius:20px;padding:24px;}
    a{display:inline-block;margin-top:16px;padding:12px 22px;border-radius:999px;background:#3B82F6;color:#fff;text-decoration:none;font-weight:600;}
  </style>
</head>
<body>
  <div class="card">
    <h2>AnimWatch</h2>
    <p>${safeMsg}</p>
    <a href="${safeHref}">Kembali ke AnimWatch</a>
  </div>
  <script>window.location.replace(${JSON.stringify(appDeepLink)});</script>
</body>
</html>`;
  return new Response(html, {
    status: 200,
    headers: { "Content-Type": "text/html; charset=utf-8" },
  });
}

function handleGoogleOAuthStart(req: Request): Response {
  if (!GOOGLE_CLIENT_ID || !GOOGLE_CLIENT_SECRET) {
    return htmlRedirectResponse(
      "animwatch://auth/google?error=" + encodeURIComponent("Google OAuth belum dikonfigurasi di server (GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET)."),
      "Google OAuth belum dikonfigurasi di server."
    );
  }
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
    if (!info.sub || !info.email) throw new Error("Profil Google tidak lengkap");

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

  const body = await parseJsonBody<{ animeId?: string; category?: string | null; watchedEpisodes?: number | null }>(req);
  const animeId = (body?.animeId ?? "").trim();
  if (!animeId) return jsonResponse({ status: false, message: "animeId required" }, 400);

  const category = body?.category?.trim() || null;
  const watchedEp = body?.watchedEpisodes != null ? Math.max(0, Math.floor(Number(body.watchedEpisodes))) : null;

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

function isOfficialFullEpisodeTitle(title: string): boolean {
  const lower = title.toLowerCase();
  if (
    lower.includes("#short") ||
    lower.includes("#highlight") ||
    lower.includes("highlight") ||
    lower.includes("cuplikan") ||
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
  return /\b(episode|ep\.?\s*\d+|eps\.?\s*\d+|s\d+e\d+|movie|ova)\b/i.test(title);
}

function parseOfficialEpisodeTitle(rawTitle: string): { seriesTitle: string; episodeNumber: number } {
  let clean = rawTitle
    .replace(/\[[^\]]*(bahasa indonesia|takarir|sub indo|english sub|dub)[^\]]*\]/gi, "")
    .replace(/【[^】]*】/g, "")
    .replace(/\|\s*(dub\s*jp|sub\s*indo|muse indonesia|ani-one).*$/i, "")
    .replace(/^dub\s*id\s*[-–:]\s*/i, "")
    .trim();

  const epMatch = clean.match(/(?:episode|eps?\.?|misi)\s*0*(\d+)/i) || clean.match(/s\d+e0*(\d+)/i);
  const episodeNumber = epMatch ? Number(epMatch[1]) : 1;

  const seriesTitle = clean
    .replace(/[-–:|]*\s*(?:episode|eps?\.?|misi)\s*\d+.*$/i, "")
    .replace(/[-–:|]*\s*s\d+e\d+.*$/i, "")
    .replace(/[-–:\s]+$/, "")
    .trim() || rawTitle.slice(0, 48);

  return { seriesTitle, episodeNumber };
}

// Verified 100% embeddable Muse Indonesia playlists mapped to AniList IDs
const MUSE_PL: Record<number, string> = {
  108465: "PLPanbgyToztZQCUAX0zVQe4HtNDvNtRTw", 127720: "PLPanbgyToztZQCUAX0zVQe4HtNDvNtRTw",
  146065: "PLPanbgyToztZQCUAX0zVQe4HtNDvNtRTw", 166873: "PLPanbgyToztZQCUAX0zVQe4HtNDvNtRTw",
  154587: "PLPanbgyToztZ7OBDPkXwBuhPvgd5OBZ70", 140960: "PLPanbgyToztbu7tZq6L49MPhra92n2ixf",
  142838: "PLPanbgyToztbu7tZq6L49MPhra92n2ixf", 158927: "PLPanbgyToztbu7tZq6L49MPhra92n2ixf",
  171018: "PLPanbgyToztaeQFokLfCgkmP_C3szkxCR", 16498: "PLPanbgyToztZJLy-bGH_tD6rA1ObhW7iz",
  101280: "PLPanbgyToztbl5IdZxm_aC5G7U5QYHjTG", 108511: "PLPanbgyToztbl5IdZxm_aC5G7U5QYHjTG",
  11061: "PLPanbgyToztYQo4C8qRLt4cN_MEjp-lRp", 21355: "PLPanbgyToztYegE7sxpW75Mbzly9zBePx",
  108632: "PLPanbgyToztYThySIw-tvX8O3J-NHhMAT", 119661: "PLPanbgyToztYThySIw-tvX8O3J-NHhMAT",
  163134: "PLPanbgyToztZpP3zanOqFo20ky85-q1Yp", 189046: "PLPanbgyToztb0iW-DI48ODtCrlyl42Qoa",
  151970: "PLPanbgyToztYlSxKLf7qFl6VpLYn2T_i1", 176508: "PLPanbgyToztYlSxKLf7qFl6VpLYn2T_i1",
  21507: "PLr28jJH6_sqHL0sd3axKcnj7-YlJaFfef", 98659: "PLPanbgyToztZBmp90FrnrDGXDDu5J6-LR",
  145545: "PLPanbgyToztYszfRvXrP_w8-DPbVpr8wy", 146066: "PLPanbgyToztYYjVy4KU5BXp3b-MmCMGwE",
  180745: "PLPanbgyToztYgSdFfzM-MO6vXGcQn-FbR", 20755: "PLPanbgyToztbc_aK0sdHOzXKcH3wuDq1Z",
  101165: "PLPanbgyTozta-OkjrTHB_-ClPo6IuEWlE", 12189: "PLPanbgyToztYq6eXRxJCx8wxaQ0Yc2NHz",
  159831: "PLPanbgyToztZN2SaJWB2i_pKwtsdGv2p7", 155783: "PLPanbgyToztZmsgNPEL9XHwb0ilTs5ljv",
  6702: "PLPanbgyToztbtJMcDT57RXRIiQDjyIFx8", 99749: "PLPanbgyToztaDhNmS0krUvUkYmylqD7Mi",
  139095: "PLPanbgyToztYlvpi2MvbIYlZ1_8xUDf4o", 21450: "PLPanbgyToztY2RnKQVkxDGhJcLlIV9bIT",
  105156: "PLPanbgyToztYOhY1JG0xIvTkGdHNyCRlU", 163292: "PLPanbgyToztYx8pk4_Sy3R94Eg18zACf0",
  187663: "PLPanbgyToztY5d8qXvBR5s3s5L4QMy3jU", 132474: "PLPanbgyToztY9q0XKsNpDu8hjS3Njnomb",
  98503: "PLPanbgyToztYx6oMEi7jzfsxbkusruG2N", 128712: "PLCvHFllySUco",
  152677: "PLAFJcVhr6Dkc", 152682: "PLF-Ab5Bnb45U", 176314: "PLZqRcDDhscWc",
  177634: "PLGWnhssZ3bgE", 162694: "PLWExLiCEALJw", 107693: "PLGfHXiwBGlUM",
  184492: "PLPanbgyToztbsmc1HPxlCK3oQIafoNBgv", 108478: "PLPanbgyToztYeEbbQUautfzW7zz-5cgmS",
  156039: "PLPanbgyToztYH0PU5B62ONgeTZXoE-EOj", 196144: "PLPanbgyToztZac25sNFH7tE7Ud07JCUnL",
  199547: "PLPanbgyToztZEvP1IiB5U3TcIB1zP4OxG", 21855: "PLPanbgyToztY-KiGHbZLR1BSl90fFAVNp",
  102926: "PLPanbgyToztZIT_IzLXTzpUBf0DFsoy1A", 1604: "PLPanbgyToztZ4hEoVgz3UmqVhX11WF79B",
  18767: "PLPanbgyToztZ63N8VunmCGGc5_axMSK3l", 21290: "PLPanbgyToztZsiNojl0AvsR5IB9AbIs9G",
  194447: "PLPanbgyToztZubEuygztnx_4W4PY6twVj", 20457: "PLPanbgyToztZOEvzidlYlJ7X1UP3F9Lf5",
  112608: "PLPanbgyToztYEVdOT3XpVqiAdKK2iMxRq", 143337: "PLPanbgyToztZtSAc9G06namZBqs_tKzWn",
  10800: "PLPanbgyToztaNIqyXWPUl-NQp97qUSeM3", 14397: "PLPanbgyToztbsrJNq7sR7rtSE2aTLD1Lb",
  101215: "PLPanbgyToztZeiNlVq54ptLfHTGzBDR25", 21873: "PLKteuQzwzADU",
  187538: "PLO43emqzsDpQ", 501: "PLNa7zQqHz9Uc",
  174576: "PLPanbgyToztZL03NxAn1aHQxDsnCu_-xl", 20789: "PLPanbgyToztYAzNKcsCBkn_53lih7V2LH",
  99539: "PLPanbgyToztY4YGogr-NvM2erIuzCSF8b", 108928: "PLPanbgyToztYUBC8M6GqnlF6YbT7HlBDo",
  100977: "PLPanbgyTozta4TKRkbiiU8A5-KBUfj5KO", 117533: "PLPanbgyToztYhN4tev6-PlmBThyvtD2xv",
  170695: "PLPanbgyToztacfueJyf4OFLQziViFKQ50", 156415: "PLPanbgyToztZVZgaofBHOgWb624juQTQ8",
  169417: "PLPanbgyToztYbZZNJq7Gf0-gOIPHcNJKi", 137908: "PLPanbgyToztZK2bfGabT5IjqCctvNkYlY",
  168374: "PLPanbgyToztbS2nnLNoUmLmAACitarsAv", 166794: "PLPanbgyToztbRLkbFAYI_MewzYcgyzfLk",
  141821: "PLPanbgyToztZ3CmrVllbkncpqEsQwbh-6", 9513: "PLPanbgyToztbSpT6je9oRWaqe4_3AxeGb",
  147642: "PLPanbgyToztbx1r01Ub9Ps9Imaohto8up", 158028: "PLPanbgyToztaF7MtQR-ZWL9SmG5WzKZ91",
  163078: "PLPanbgyToztYfKRDSnwzzxsLiE35fSbBo", 170130: "PLPanbgyToztarmJZfU9GyRSCBCnIiJONX",
  114124: "PLPanbgyToztaehPnu02ArgIJcP4DFJ2bO", 116006: "PLPanbgyToztaC8yJC3SVMP3NSxN0cb4Wh",
  116005: "PLPanbgyToztaCN6AS9cPj_ydTWj2btL9N", 117343: "PLPanbgyToztb2-Qf_38TnxtRbjv6iM_Qz",
  114340: "PLPanbgyToztbhZQmZ-5bZTkZtKWhKas-2",
};

// Verified 100% embeddable Ani-One Indonesia playlists mapped to AniList IDs
const ANIONE_PL: Record<number, string> = {
  113415: "PLNAQymMf9eaYxBDom48OG_F4aLhnBZcui", 116674: "PLVz5xYUXIegU",
  137822: "PLQz4WGeaorjA", 20832: "PLOXBvOkrpC_s", 105333: "PLQL6iBDZsabo",
  172019: "PLODWQCZoYSHM", 21459: "PLRIKUnMprNvI", 21856: "PLA6LI1cj8LDk",
  100166: "PLDoNd3kTAiyk", 104276: "PLXd7zGqfH3ZI", 117193: "PLRnE9XnIC1T4",
  139630: "PLNgw6VnFxawE", 163139: "PLLKM3uH6S8OA", 162804: "PLNAQymMf9eaa139ptcijlI0JizvpFXFDc",
  143338: "PLSnQpJo_dujc", 19815: "PLWyzUoEg2tlo", 20464: "PLE7eV_vewpAg",
  918: "PLSz4xtUGSzEA", 11771: "PLSzQ99-K5DYo", 16894: "PLX7mM0yTy0Bk",
  20725: "PLNB4urP2esgU", 20923: "PLdYbu6KhtdUU", 21518: "PLImbXtgn5ZUM",
  136804: "PLexFyslfo_78", 150075: "PLDQ7JF2opq5k", 105228: "PLcDBBq1D3wOY",
  173172: "PLFDsIfMtIv44", 21613: "PLcKfVhnYS2Nk", 125206: "PLbBhmk2Bw-jA",
  139518: "PLIfTSfpnDLec", 103632: "PLA6VgIPypTSE", 139587: "PLeyB-SZ7G05M",
  159042: "PLDv-inCJtX5A", 2581: "PLDHeNIjQwazE", 160900: "PLScAfDnCDpRU",
  167419: "PLNAQymMf9eab5ejKPqDXIdFEaJEuxSrAW", 14813: "PLRYt0oMV87fU",
  185: "PLTcut_BhoIa4", 20987: "PLGKjS1fjrROc", 147864: "PLNAQymMf9eaYJZdl3_y3WAY9wufqB8eTn",
  164702: "PLLkfZbT5GmdU",
};

const MUSE_ID_PLAYLIST_BY_ANILIST_ID: Record<number, { playlistId: string; channelName: string }> = {};
for (const [id, pl] of Object.entries(MUSE_PL)) {
  MUSE_ID_PLAYLIST_BY_ANILIST_ID[Number(id)] = { playlistId: pl, channelName: "Muse Indonesia" };
}
for (const [id, pl] of Object.entries(ANIONE_PL)) {
  MUSE_ID_PLAYLIST_BY_ANILIST_ID[Number(id)] = { playlistId: pl, channelName: "Ani-One Indonesia" };
}

const DUPLICATE_SEASON_ALIASES = new Set([127720, 146065, 166873, 142838, 158927, 108511, 119661, 176508, 18767, 21873]);
const FEATURED_MUSE_ANILIST_IDS: number[] = [
  108465, 154587, 140960, 171018, 16498, 113415, 116674, 137822, 101280, 11061,
  21355, 108632, 163134, 189046, 151970, 21507, 20832, 105333, 172019, 21459,
  ...Object.keys(MUSE_ID_PLAYLIST_BY_ANILIST_ID).map(Number).filter((id) => !DUPLICATE_SEASON_ALIASES.has(id)),
];

interface CrawledPlaylistSummary {
  playlistId: string;
  title: string;
  cleanTitle: string;
  channelName: string;
  thumbnail: string;
  episodeHint: number;
}

function cleanPlaylistDisplayTitle(raw: string): string {
  return raw
    .replace(/\[[^\]]*\]/g, "")
    .replace(/【[^】]*】/g, "")
    .replace(/\((?:lengkap|dub|sub|takarir|bahasa indonesia|uncensored|censored)[^)]*\)/gi, "")
    .replace(/\|\s*(sub indo|dub id|dub jp|muse indonesia|ani-one).*$/i, "")
    .replace(/-\s*(sub indo|dub id|bahasa indonesia|takarir indonesia).*$/i, "")
    .replace(/^dub\s*id\s*[-–:]\s*/i, "")
    .replace(/^#ultraearlybird\s*[《«]?/i, "")
    .replace(/[》»]/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

function isSkippablePlaylistTitle(title: string): boolean {
  return /(^pv\b|\bpv\b|trailer|teaser|highlight|cuplikan|#short|shorts|\bost\b|opening|ending|music video|karaoke|promo|preview|interview|compilation|kompilasi|semua episode|anime festival)/i.test(title);
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

    const dedup = new Map<string, CrawledPlaylistSummary & { _score: number }>();
    for (const item of seen.values()) {
      const key = item.cleanTitle.toLowerCase().replace(/[^a-z0-9]+/g, "");
      if (key.length < 2) continue;
      const isSub = /takarir|sub indo/i.test(item.title) || !/bahasa indonesia|dub id/i.test(item.title);
      const score = (isSub ? 100 : 0) + item.episodeHint;
      const prev = dedup.get(key);
      if (!prev || score > prev._score) dedup.set(key, { ...item, _score: score });
    }
    return [...dedup.values()].map(({ _score, ...rest }) => rest);
  } catch {
    return [];
  }
}

async function getAllLiveChannelPlaylists(): Promise<CrawledPlaylistSummary[]> {
  const key = "yt-all-channel-playlists-v1";
  const cached = getCached<CrawledPlaylistSummary[]>(key);
  if (cached) return cached;

  const [dbRows, museList, anioneList] = await Promise.all([
    db && dbReady
      ? db`SELECT playlist_id, title, clean_title, channel_name, episode_hint, sample_video_id FROM official_playlists ORDER BY channel_name DESC, episode_hint DESC`
          .then((rows) =>
            (rows as Array<{ playlist_id: string; title: string; clean_title: string; channel_name: string; episode_hint: number; sample_video_id: string }>).map((r) => ({
              playlistId: r.playlist_id,
              title: r.title,
              cleanTitle: r.clean_title,
              channelName: r.channel_name,
              thumbnail: r.sample_video_id ? `https://i.ytimg.com/vi/${r.sample_video_id}/hqdefault.jpg` : "",
              episodeHint: Number(r.episode_hint || 12),
            }))
          )
          .catch(() => [] as CrawledPlaylistSummary[])
      : Promise.resolve([] as CrawledPlaylistSummary[]),
    crawlPublicChannelPlaylists("@MuseIndonesia", "Muse Indonesia", 16),
    crawlPublicChannelPlaylists("@AniOneID", "Ani-One Indonesia", 10),
  ]);
  const byId = new Map<string, CrawledPlaylistSummary>();
  for (const item of [...museList, ...anioneList, ...dbRows]) {
    if (!byId.has(item.playlistId)) byId.set(item.playlistId, item);
  }
  const combined = [...byId.values()];
  return setCached(key, combined, 2 * 60 * 60 * 1000);
}

async function handleSeedOfficialPlaylists(req: Request): Promise<Response> {
  if (!db || !dbReady) return jsonResponse({ status: false, message: "Database belum siap" }, 503);
  const body = await parseJsonBody<{ items?: Array<{ id: string; title: string; cleanTitle: string; channel: string; epCount: number; ep1Id: string }> }>(req);
  const items = (body?.items ?? []).filter(
    (x) =>
      typeof x?.id === "string" &&
      /^PL[A-Za-z0-9_-]{8,40}$/.test(x.id) &&
      (x.channel === "Muse Indonesia" || x.channel === "Ani-One Indonesia") &&
      typeof x.title === "string" &&
      x.title.trim().length > 0
  );
  if (items.length === 0) return jsonResponse({ status: false, message: "No valid items" }, 400);
  let inserted = 0;
  for (const it of items) {
    const clean = (it.cleanTitle || cleanPlaylistDisplayTitle(it.title) || it.title).trim().slice(0, 180);
    const ep1 = typeof it.ep1Id === "string" && /^[A-Za-z0-9_-]{8,16}$/.test(it.ep1Id) ? it.ep1Id : "";
    await db`
      INSERT INTO official_playlists (playlist_id, title, clean_title, channel_name, episode_hint, sample_video_id)
      VALUES (${it.id}, ${it.title.trim().slice(0, 220)}, ${clean}, ${it.channel}, ${Math.max(1, Number(it.epCount) || 12)}, ${ep1})
      ON CONFLICT (playlist_id) DO UPDATE SET
        clean_title = EXCLUDED.clean_title,
        episode_hint = EXCLUDED.episode_hint,
        sample_video_id = CASE WHEN EXCLUDED.sample_video_id <> '' THEN EXCLUDED.sample_video_id ELSE official_playlists.sample_video_id END
    `;
    inserted++;
  }
  cache.delete("yt-all-channel-playlists-v1");
  for (const k of cache.keys()) {
    if (k.startsWith("official-free-unified-v5:")) cache.delete(k);
  }
  const cnt = await db`SELECT COUNT(*)::int AS c FROM official_playlists`;
  return jsonResponse({ status: "success", upserted: inserted, totalInDb: Number((cnt[0] as { c: number })?.c ?? inserted) });
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

  const extractSeasonNum = (s: string): string => {
    const m = s.match(/\b([2-9]|ii|iii|iv|v)\b$/i);
    if (!m) return "1";
    const v = m[1].toLowerCase();
    return v === "ii" ? "2" : v === "iii" ? "3" : v === "iv" ? "4" : v === "v" ? "5" : v;
  };

  for (const pl of allPlaylists) {
    const normPl = normalizeTitleForMatch(pl.cleanTitle);
    if (normPl.length < 4) continue;
    const plSeason = extractSeasonNum(normPl);
    for (const cand of candidateNames) {
      if (normPl === cand) return pl;
      if (extractSeasonNum(cand) !== plSeason) continue;
      if (cand.length >= 9 && normPl.includes(cand)) return pl;
      if (normPl.length >= 9 && cand.includes(normPl)) return pl;
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
    return setCached(key, res.ok, 30 * 60 * 1000);
  } catch {
    return true;
  }
}

async function filterEmbeddableEpisodes(entries: YtRssEntry[]): Promise<YtRssEntry[]> {
  const cleanCandidates = entries.filter(
    (e) => !/\b(preview|trailer|teaser|pv|semua episode|full marathon|kompilasi)\b/i.test(e.title)
  );
  if (cleanCandidates.length === 0) return [];
  const checks = await Promise.all(
    cleanCandidates.map(async (entry) => ({
      entry,
      ok: await isVideoEmbeddable(entry.videoId),
    }))
  );
  return checks
    .filter((item) => item.ok)
    .map((item) => item.entry)
    .sort((a, b) => parseOfficialEpisodeTitle(a.title).episodeNumber - parseOfficialEpisodeTitle(b.title).episodeNumber);
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

async function handleOfficialFree(page = 1): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `official-free-unified-v5:p${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  // Page 1 & 2: Batched AniList metadata for all 118+ curated verified Muse Indonesia & Ani-One Indonesia series
  if (safePage === 1) {
    const uniqueIds = [...new Set(FEATURED_MUSE_ANILIST_IDS)];
    const chunks: number[][] = [];
    for (let i = 0; i < uniqueIds.length; i += 50) {
      chunks.push(uniqueIds.slice(i, i + 50));
    }

    const query = `
      query ($ids: [Int]) {
        Page(page: 1, perPage: 50) {
          media(id_in: $ids, type: ANIME, isAdult: false) {
            ${MEDIA_CARD_FIELDS}
          }
        }
      }
    `;
    const batchResults = await Promise.all(
      chunks.map((ids) =>
        queryAniList<{ Page: { media: AniListMedia[] } }>(query, { ids }).catch(() => ({ Page: { media: [] } }))
      )
    );

    const byId = new Map<number, AniListMedia>();
    for (const batch of batchResults) {
      for (const m of batch.Page.media) byId.set(m.id, m);
    }

    const data = uniqueIds
      .map((id) => byId.get(id))
      .filter((m): m is AniListMedia => Boolean(m))
      .map((m) => {
        const common = mapMediaCommon(m);
        const pl = MUSE_ID_PLAYLIST_BY_ANILIST_ID[m.id];
        return {
          ...common,
          is_official_free: true,
          official_channel: pl?.channelName ?? "Muse Indonesia",
          stream_type: "official_episode",
          external_links: pl
            ? [{ site: pl.channelName, url: `https://www.youtube.com/playlist?list=${pl.playlistId}` }]
            : [],
        };
      });

    return jsonResponse(
      setCached(key, { status: "success", source: "anilist+muse+anione", page: 1, totalCurated: data.length, data }, 20 * 60 * 1000)
    );
  }

  // Page 2+: Remaining LIVE crawled Muse Indonesia & Ani-One Indonesia playlists (ALL 400+ channel playlists!)
  const curatedPlaylistIds = new Set(Object.values(MUSE_ID_PLAYLIST_BY_ANILIST_ID).map((x) => x.playlistId));
  const allLive = await getAllLiveChannelPlaylists();
  const remaining = allLive.filter((pl) => !curatedPlaylistIds.has(pl.playlistId));
  const pageSize = 48;
  const start = (safePage - 2) * pageSize;
  const slice = remaining.slice(start, start + pageSize);

  const data = slice.map((pl, idx) => ({
    id: 900000 + start + idx,
    title: pl.cleanTitle,
    japanese_title: pl.channelName,
    slug: `ytpl-${pl.playlistId}`,
    poster: pl.thumbnail,
    banner: pl.thumbnail,
    synopsis: `Koleksi episode dari ${pl.channelName}. Ketuk untuk memuat daftar episode dan tonton langsung di AnimWatch.`,
    genres: ["Anime", pl.channelName === "Muse Indonesia" ? "Fantasy" : "Action"],
    rating: 8.3,
    rating_count: pl.channelName,
    views_label: `${pl.episodeHint} Ep`,
    studio: pl.channelName,
    status: "ONGOING",
    type: "TV Series",
    release: "2025",
    year: 2025,
    season: pl.channelName,
    duration: "24 menit",
    duration_minutes: 24,
    total_episodes: Math.max(pl.episodeHint, 12),
    released_episodes: Math.max(pl.episodeHint, 1),
    episode: `Ep ${Math.max(pl.episodeHint, 1)}`,
    latest_eps_slug: `ytpl-${pl.playlistId}`,
    airing_day: null,
    airing_time: null,
    trailer_url: "",
    is_official_free: true,
    official_channel: pl.channelName,
    stream_type: "official_episode",
    external_links: [{ site: pl.channelName, url: `https://www.youtube.com/playlist?list=${pl.playlistId}` }],
  }));

  return jsonResponse(
    setCached(
      key,
      { status: "success", source: "youtube-channels-live", page: safePage, hasMore: start + pageSize < remaining.length, data },
      20 * 60 * 1000
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
    return ytNoCookieEmbed(trailer.id);
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
      "User-Agent": "AnimWatch-API/2.2",
    },
    body: JSON.stringify({ query, variables }),
  });

  if (!res.ok) throw new Error(`AniList HTTP ${res.status}`);
  const payload = (await res.json()) as { data?: T; errors?: Array<{ message: string }> };
  if (payload.errors?.length) throw new Error(payload.errors.map((e) => e.message).join("; "));
  if (!payload.data) throw new Error("AniList returned empty data");
  return payload.data;
}

interface AniListExternalLink {
  site?: string | null;
  url?: string | null;
  type?: string | null;
  language?: string | null;
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
  externalLinks?: AniListExternalLink[] | null;
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
  coverImage { large extraLarge }
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
      : Math.min(totalEpisodes, 1);
  const year = m.seasonYear || m.startDate?.year || 2026;
  const studio = m.studios?.nodes?.[0]?.name?.trim() || "Studio Anime";
  const schedule = formatWibSchedule(m.nextAiringEpisode?.airingAt);
  const genres = (m.genres ?? []).filter(Boolean);
  const officialMapping = MUSE_ID_PLAYLIST_BY_ANILIST_ID[m.id];
  const cachedLive = officialMapping ? null : getCached<CrawledPlaylistSummary[]>("yt-all-channel-playlists-v1");
  const matchedLive = officialMapping ? null : cachedLive ? matchLivePlaylistForMedia(m, cachedLive) : null;
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
    official_channel: channelLabel,
  };
}

async function handlePopular(page = 1): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `popular:v2:${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const query = `
    query ($page: Int) {
      Page(page: $page, perPage: 50) {
        media(type: ANIME, sort: [TRENDING_DESC, POPULARITY_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { page: safePage });
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", page: safePage, data }));
}

async function handleLatest(page: number): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `latest:v2:${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const query = `
    query ($page: Int) {
      Page(page: $page, perPage: 45) {
        media(type: ANIME, status: RELEASING, sort: [POPULARITY_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { page: safePage });
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", page: safePage, data }));
}

async function handleMovies(page: number): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `movies:v2:${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const query = `
    query ($page: Int) {
      Page(page: $page, perPage: 35) {
        media(type: ANIME, format: MOVIE, sort: [POPULARITY_DESC, SCORE_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { page: safePage });
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", page: safePage, data }));
}

async function handleCompleted(page: number): Promise<Response> {
  const safePage = Number.isFinite(page) && page >= 1 ? page : 1;
  const key = `completed:v2:${safePage}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  const query = `
    query ($page: Int) {
      Page(page: $page, perPage: 45) {
        media(type: ANIME, status: FINISHED, format: TV, sort: [SCORE_DESC, POPULARITY_DESC], isAdult: false) {
          ${MEDIA_CARD_FIELDS}
        }
      }
    }
  `;
  const result = await queryAniList<{ Page: { media: AniListMedia[] } }>(query, { page: safePage });
  const data = result.Page.media.map(mapMediaCommon);
  return jsonResponse(setCached(key, { status: "success", source: "anilist-live", page: safePage, data }));
}

async function handleSearch(rawQuery: string): Promise<Response> {
  const q = decodeURIComponent(rawQuery).trim();
  if (!q) return jsonResponse({ status: "success", query: "", data: [] });
  const key = `search:${q.toLowerCase()}`;
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

  // Also include matching live Muse Indonesia & Ani-One Indonesia playlists not already in AniList hits
  const qNorm = q.toLowerCase().trim();
  const cachedLive = getCached<CrawledPlaylistSummary[]>("yt-all-channel-playlists-v1") ?? [];
  const curatedIds = new Set(Object.values(MUSE_ID_PLAYLIST_BY_ANILIST_ID).map((x) => x.playlistId));
  const seenTitles = new Set(anilistItems.map((a) => normalizeTitleForMatch(a.title)));
  const ytMatches = cachedLive
    .filter(
      (pl) =>
        !curatedIds.has(pl.playlistId) &&
        (pl.cleanTitle.toLowerCase().includes(qNorm) || pl.title.toLowerCase().includes(qNorm)) &&
        !seenTitles.has(normalizeTitleForMatch(pl.cleanTitle))
    )
    .slice(0, 8)
    .map((pl, idx) => ({
      id: 950000 + idx,
      title: pl.cleanTitle,
      japanese_title: pl.channelName,
      slug: `ytpl-${pl.playlistId}`,
      poster: pl.thumbnail,
      banner: pl.thumbnail,
      synopsis: `Koleksi episode dari ${pl.channelName}. Ketuk untuk memuat daftar episode dan tonton langsung di AnimWatch.`,
      genres: ["Anime", pl.channelName === "Muse Indonesia" ? "Fantasy" : "Action"],
      rating: 8.3,
      rating_count: pl.channelName,
      views_label: `${pl.episodeHint} Ep`,
      studio: pl.channelName,
      status: "ONGOING",
      type: "TV Series",
      release: "2025",
      year: 2025,
      season: pl.channelName,
      duration: "24 menit",
      duration_minutes: 24,
      total_episodes: Math.max(pl.episodeHint, 12),
      released_episodes: Math.max(pl.episodeHint, 1),
      episode: `Ep ${Math.max(pl.episodeHint, 1)}`,
      latest_eps_slug: `ytpl-${pl.playlistId}`,
      airing_day: null,
      airing_time: null,
      trailer_url: "",
      is_official_free: true,
      official_channel: pl.channelName,
    }));

  const data = [...anilistItems, ...ytMatches];
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

async function handleAnimeDetail(slug: string): Promise<Response> {
  const key = `anime:${slug.toLowerCase()}`;
  const cached = getCached(key);
  if (cached) return jsonResponse(cached);

  // Direct crawled YouTube playlist entry (ytpl-<playlistId>)
  if (slug.startsWith("ytpl-")) {
    const playlistId = slug.slice(5).trim();
    const allLive = await getAllLiveChannelPlaylists();
    const meta = allLive.find((p) => p.playlistId === playlistId);
    const channelName = meta?.channelName ?? "Muse Indonesia";
    const rawEntries = await fetchYouTubeAtomFeed(
      `https://www.youtube.com/feeds/videos.xml?playlist_id=${playlistId}`,
      channelName
    );
    const playable = await filterEmbeddableEpisodes(rawEntries);
    const displayTitle = meta?.cleanTitle || (rawEntries[0] ? parseOfficialEpisodeTitle(rawEntries[0].title).seriesTitle : "Anime Muse Indonesia");
    const poster = meta?.thumbnail || playable[0]?.thumbnail || rawEntries[0]?.thumbnail || "";

    const episodes = playable.slice(0, 48).map((ytEp, idx) => ({
      eps_title: ytEp.title,
      eps_slug: `yt-${ytEp.videoId}`,
      number: idx + 1,
      stream_url: ytNoCookieEmbed(ytEp.videoId),
      stream_type: "official_episode",
      thumbnail: ytEp.thumbnail,
    }));

    const payload = {
      status: "success",
      source: "youtube-playlist-live",
      animeData: {
        title: displayTitle,
        japanese_title: channelName,
        poster,
        banner: poster,
        synopsis: `Tonton episode ${displayTitle} secara langsung dari playlist ${channelName} di dalam AnimWatch.`,
        genres: ["Anime", "Action", "Fantasy"],
        rating: 8.3,
        rating_count: channelName,
        studio: channelName,
        status: "ONGOING",
        year: 2025,
        season: channelName,
        duration_minutes: 24,
        total_episodes: Math.max(episodes.length, meta?.episodeHint ?? 12),
        released_episodes: Math.max(episodes.length, 1),
        is_official_free: episodes.length > 0,
        official_channel: channelName,
        episodes,
        characters: [],
        related_slugs: [],
        external_links: [],
      },
    };
    return jsonResponse(setCached(key, payload));
  }

  // If this is a Muse ID / Ani-One ID official free entry, resolve directly from official feed
  if (slug.startsWith("ytfree-")) {
    const feedRes = await handleOfficialFree(1);
    const feedJson = (await feedRes.json()) as { data?: Array<Record<string, unknown>> };
    const matched = feedJson.data?.find((item) => item.slug === slug);
    if (matched) {
      return jsonResponse({ status: "success", source: "official-youtube-id", animeData: matched });
    }
  }

  const media = await fetchAniListMediaDetail(slug);
  if (!media) {
    return jsonResponse({ status: "error", message: `Anime tidak ditemukan: ${slug}` }, 404);
  }

  const common = mapMediaCommon(media);
  const streamingLinks = (media.externalLinks ?? [])
    .filter((l) => l.type === "STREAMING" && l.site && l.url)
    .map((l) => ({ site: l.site!.trim(), url: l.url!.trim() }));

  // Prefer verified Muse Indonesia Sub Indo playlist when available for this AniList ID
  const curatedOfficial = MUSE_ID_PLAYLIST_BY_ANILIST_ID[media.id];
  const ytPlaylistLink = streamingLinks.find((l) => l.url.includes("youtube.com/playlist") && l.url.includes("list="));
  let officialPlaylistEpisodes: YtRssEntry[] = [];
  let officialChannelName: string | null = null;

  if (curatedOfficial) {
    const rawCurated = await fetchYouTubeAtomFeed(
      `https://www.youtube.com/feeds/videos.xml?playlist_id=${curatedOfficial.playlistId}`,
      curatedOfficial.channelName
    );
    officialPlaylistEpisodes = await filterEmbeddableEpisodes(rawCurated);
    if (officialPlaylistEpisodes.length > 0) {
      officialChannelName = curatedOfficial.channelName;
    }
  }

  if (officialPlaylistEpisodes.length === 0) {
    // Dynamic fuzzy match across ALL 400+ live Muse Indonesia & Ani-One Indonesia playlists!
    const allLivePlaylists = await getAllLiveChannelPlaylists();
    const matchedPl = matchLivePlaylistForMedia(media, allLivePlaylists);
    if (matchedPl) {
      const rawMatched = await fetchYouTubeAtomFeed(
        `https://www.youtube.com/feeds/videos.xml?playlist_id=${matchedPl.playlistId}`,
        matchedPl.channelName
      );
      officialPlaylistEpisodes = await filterEmbeddableEpisodes(rawMatched);
      if (officialPlaylistEpisodes.length > 0) {
        officialChannelName = matchedPl.channelName;
      }
    }
  }

  if (officialPlaylistEpisodes.length === 0) {
    // Check live Muse Indonesia & Ani-One Indonesia channel RSS for ongoing simulcast episodes
    const recentChannelEntries = await getRecentChannelOfficialEpisodes();
    const matchedLive = await filterEmbeddableEpisodes(matchRecentEpisodesForMedia(media, recentChannelEntries));
    if (matchedLive.length > 0) {
      officialPlaylistEpisodes = matchedLive;
      officialChannelName = matchedLive[0].channelName;
    }
  }

  if (officialPlaylistEpisodes.length === 0 && ytPlaylistLink) {
    const plMatch = ytPlaylistLink.url.match(/[?&]list=([a-zA-Z0-9_-]+)/);
    if (plMatch?.[1]) {
      const rawYt = await fetchYouTubeAtomFeed(
        `https://www.youtube.com/feeds/videos.xml?playlist_id=${plMatch[1]}`,
        "YouTube"
      );
      officialPlaylistEpisodes = await filterEmbeddableEpisodes(rawYt);
      if (officialPlaylistEpisodes.length > 0) {
        officialChannelName = "YouTube";
      }
    }
  }

  const streamEps = (media.streamingEpisodes ?? []).filter((e) => e?.title);
  let episodes: Array<{
    eps_title: string;
    eps_slug: string;
    number: number;
    stream_url: string;
    stream_type: string;
    thumbnail: string;
  }>;

  if (officialPlaylistEpisodes.length > 0) {
    episodes = officialPlaylistEpisodes.slice(0, 48).map((ytEp, idx) => ({
      eps_title: ytEp.title,
      eps_slug: `yt-${ytEp.videoId}`,
      number: idx + 1,
      stream_url: ytNoCookieEmbed(ytEp.videoId),
      stream_type: "official_episode",
      thumbnail: ytEp.thumbnail,
    }));
  } else {
    const episodeCount = Math.min(Math.max(common.released_episodes, Math.min(streamEps.length, 48), 1), 48);
    episodes = Array.from({ length: episodeCount }, (_, i) => {
      const epNum = i + 1;
      const matched = streamEps[i];
      const cleanEpTitle = matched?.title
        ? matched.title.replace(/^Episode\s+\d+\s*[-:]\s*/i, "").trim() || `Episode ${epNum}`
        : `Episode ${epNum}`;
      return {
        eps_title: `Episode ${epNum}: ${cleanEpTitle}`,
        eps_slug: `ep-${media.id}-${epNum}`,
        number: epNum,
        stream_url: "",
        stream_type: "info_only",
        thumbnail: matched?.thumbnail || common.poster,
      };
    });
  }

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
    source: officialPlaylistEpisodes.length > 0 ? "anilist+youtube-official" : "anilist-live",
    animeData: {
      ...common,
      is_official_free: officialPlaylistEpisodes.length > 0,
      official_channel: officialChannelName,
      episodes,
      characters,
      related_slugs: relatedSlugs,
      external_links: streamingLinks,
    },
  };

  return jsonResponse(setCached(key, payload));
}

async function handleEpisodeDetail(slug: string): Promise<Response> {
  const key = `episode:${slug.toLowerCase()}`;
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
          service: "AnimWatch Official Streaming + AniList Tracker + PostgreSQL API",
          mode: CUSTOM_SOURCE_URL ? "custom-source+anilist-live" : "anilist+official-yt-id",
          database: dbReady ? "connected" : "unavailable",
          routes: {
            catalog: [
              "/api/official-free",
              "/api/popular",
              "/api/latest",
              "/api/movie",
              "/api/completed",
              "/api/search/:query",
              "/api/anime/:slug",
              "/api/episode/:slug",
            ],
            auth: [
              "GET /api/auth/config",
              "POST /api/auth/register",
              "POST /api/auth/login",
              "POST /api/auth/google",
              "GET /api/auth/google/start",
              "GET /api/auth/google/callback",
              "POST /api/auth/logout",
            ],
            user: [
              "GET /api/user/me",
              "PATCH /api/user/profile",
              "PATCH /api/user/preferences",
              "PUT /api/user/watchlist",
              "PUT /api/user/history",
              "DELETE /api/user/history",
              "PUT /api/user/ratings",
            ],
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
      if (method === "GET" && path === "/api/auth/config") return handleAuthConfig();
      if (method === "POST" && path === "/api/auth/register") return await handleRegister(req);
      if (method === "POST" && path === "/api/auth/login") return await handleLogin(req);
      if (method === "POST" && path === "/api/auth/google") return await handleGoogleIdTokenAuth(req);
      if (method === "GET" && path === "/api/auth/google/start") return handleGoogleOAuthStart(req);
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

      // Comments Routes
      if (method === "POST" && path === "/api/comments") return await handlePostComment(req);
      const likeCommentMatch = path.match(/^\/api\/comments\/([^/]+)\/like$/);
      if (method === "POST" && likeCommentMatch) return await handleToggleCommentLike(req, likeCommentMatch[1]);
      const getCommentsMatch = path.match(/^\/api\/comments\/(.+)$/);
      if (method === "GET" && getCommentsMatch) return await handleGetComments(req, getCommentsMatch[1]);

      // Official Legal Free + AniList Catalog Routes (with pagination!)
      if (method === "POST" && path === "/api/official-free/seed") return await handleSeedOfficialPlaylists(req);
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
