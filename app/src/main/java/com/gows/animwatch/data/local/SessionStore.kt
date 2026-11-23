package com.gows.animwatch.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

interface SessionStore {
    fun getAuthToken(): String?
    fun saveAuthToken(token: String?)
    fun getApiBaseUrl(): String?
    fun saveApiBaseUrl(url: String)
    fun getAppearanceMode(): String?
    fun saveAppearanceMode(mode: String)
    fun getAccentTheme(): String?
    fun saveAccentTheme(theme: String)
    fun getReadNotificationIds(): Set<String> = emptySet()
    fun saveReadNotificationIds(ids: Set<String>) {}
    fun getReminderAnimeIds(): Set<String> = emptySet()
    fun saveReminderAnimeIds(ids: Set<String>) {}
    fun getKnownLatestEpisodes(): Map<String, Int> = emptyMap()
    fun saveKnownLatestEpisodes(episodes: Map<String, Int>) {}
    fun getLastSystemNotifiedSignature(): String? = null
    fun saveLastSystemNotifiedSignature(signature: String) {}
}

class InMemorySessionStore : SessionStore {
    private var token: String? = null
    private var apiBaseUrl: String? = null
    private var appearanceMode: String? = null
    private var accentTheme: String? = null
    private var readNotificationIds: Set<String> = emptySet()
    private var reminderAnimeIds: Set<String> = emptySet()
    private var knownLatestEpisodes: Map<String, Int> = emptyMap()
    private var lastSystemSignature: String? = null

    override fun getAuthToken(): String? = token
    override fun saveAuthToken(token: String?) { this.token = token }
    override fun getApiBaseUrl(): String? = apiBaseUrl
    override fun saveApiBaseUrl(url: String) { this.apiBaseUrl = url }
    override fun getAppearanceMode(): String? = appearanceMode
    override fun saveAppearanceMode(mode: String) { this.appearanceMode = mode }
    override fun getAccentTheme(): String? = accentTheme
    override fun saveAccentTheme(theme: String) { this.accentTheme = theme }
    override fun getReadNotificationIds(): Set<String> = readNotificationIds
    override fun saveReadNotificationIds(ids: Set<String>) { this.readNotificationIds = ids }
    override fun getReminderAnimeIds(): Set<String> = reminderAnimeIds
    override fun saveReminderAnimeIds(ids: Set<String>) { this.reminderAnimeIds = ids }
    override fun getKnownLatestEpisodes(): Map<String, Int> = knownLatestEpisodes
    override fun saveKnownLatestEpisodes(episodes: Map<String, Int>) { this.knownLatestEpisodes = episodes }
    override fun getLastSystemNotifiedSignature(): String? = lastSystemSignature
    override fun saveLastSystemNotifiedSignature(signature: String) { this.lastSystemSignature = signature }
}

class SharedPreferencesSessionStore(context: Context) : SessionStore {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)?.takeIf { it.isNotBlank() }

    override fun saveAuthToken(token: String?) {
        prefs.edit().apply {
            if (token.isNullOrBlank()) remove(KEY_AUTH_TOKEN)
            else putString(KEY_AUTH_TOKEN, token)
        }.apply()
    }

    override fun getApiBaseUrl(): String? = prefs.getString(KEY_API_BASE_URL, null)?.takeIf { it.isNotBlank() }

    override fun saveApiBaseUrl(url: String) {
        prefs.edit().putString(KEY_API_BASE_URL, url).apply()
    }

    override fun getAppearanceMode(): String? = prefs.getString(KEY_APPEARANCE_MODE, null)

    override fun saveAppearanceMode(mode: String) {
        prefs.edit().putString(KEY_APPEARANCE_MODE, mode).apply()
    }

    override fun getAccentTheme(): String? = prefs.getString(KEY_ACCENT_THEME, null)

    override fun saveAccentTheme(theme: String) {
        prefs.edit().putString(KEY_ACCENT_THEME, theme).apply()
    }

    override fun getReadNotificationIds(): Set<String> =
        prefs.getStringSet(KEY_READ_NOTIFICATIONS, emptySet())?.toSet().orEmpty()

    override fun saveReadNotificationIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_READ_NOTIFICATIONS, ids.take(200).toSet()).apply()
    }

    override fun getReminderAnimeIds(): Set<String> =
        prefs.getStringSet(KEY_REMINDER_ANIME_IDS, emptySet())?.toSet().orEmpty()

    override fun saveReminderAnimeIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_REMINDER_ANIME_IDS, ids).apply()
    }

    override fun getKnownLatestEpisodes(): Map<String, Int> {
        val raw = prefs.getString(KEY_KNOWN_LATEST_EPISODES, null) ?: return emptyMap()
        return runCatching {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson<Map<String, Int>>(raw, type).orEmpty()
        }.getOrDefault(emptyMap())
    }

    override fun saveKnownLatestEpisodes(episodes: Map<String, Int>) {
        prefs.edit().putString(KEY_KNOWN_LATEST_EPISODES, gson.toJson(episodes)).apply()
    }

    override fun getLastSystemNotifiedSignature(): String? =
        prefs.getString(KEY_LAST_SYSTEM_SIGNATURE, null)?.takeIf { it.isNotBlank() }

    override fun saveLastSystemNotifiedSignature(signature: String) {
        prefs.edit().putString(KEY_LAST_SYSTEM_SIGNATURE, signature).apply()
    }

    companion object {
        private const val PREFS_NAME = "animwatch_session_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_API_BASE_URL = "api_base_url"
        private const val KEY_APPEARANCE_MODE = "appearance_mode"
        private const val KEY_ACCENT_THEME = "accent_theme"
        private const val KEY_READ_NOTIFICATIONS = "read_notification_ids"
        private const val KEY_REMINDER_ANIME_IDS = "reminder_anime_ids"
        private const val KEY_KNOWN_LATEST_EPISODES = "known_latest_episodes"
        private const val KEY_LAST_SYSTEM_SIGNATURE = "last_system_notified_signature"
    }
}
