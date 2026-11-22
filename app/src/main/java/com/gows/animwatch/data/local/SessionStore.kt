package com.gows.animwatch.data.local

import android.content.Context

interface SessionStore {
    fun getAuthToken(): String?
    fun saveAuthToken(token: String?)
    fun getApiBaseUrl(): String?
    fun saveApiBaseUrl(url: String)
    fun getAppearanceMode(): String?
    fun saveAppearanceMode(mode: String)
    fun getAccentTheme(): String?
    fun saveAccentTheme(theme: String)
}

class InMemorySessionStore : SessionStore {
    private var token: String? = null
    private var apiBaseUrl: String? = null
    private var appearanceMode: String? = null
    private var accentTheme: String? = null

    override fun getAuthToken(): String? = token
    override fun saveAuthToken(token: String?) { this.token = token }
    override fun getApiBaseUrl(): String? = apiBaseUrl
    override fun saveApiBaseUrl(url: String) { this.apiBaseUrl = url }
    override fun getAppearanceMode(): String? = appearanceMode
    override fun saveAppearanceMode(mode: String) { this.appearanceMode = mode }
    override fun getAccentTheme(): String? = accentTheme
    override fun saveAccentTheme(theme: String) { this.accentTheme = theme }
}

class SharedPreferencesSessionStore(context: Context) : SessionStore {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

    companion object {
        private const val PREFS_NAME = "animwatch_session_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_API_BASE_URL = "api_base_url"
        private const val KEY_APPEARANCE_MODE = "appearance_mode"
        private const val KEY_ACCENT_THEME = "accent_theme"
    }
}
