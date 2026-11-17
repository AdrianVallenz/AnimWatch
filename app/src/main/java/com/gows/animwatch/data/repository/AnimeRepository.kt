package com.gows.animwatch.data.repository

import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.CommentItem
import com.gows.animwatch.data.model.DownloadTask
import com.gows.animwatch.data.model.NotificationItem
import com.gows.animwatch.data.model.PlaybackSettings
import com.gows.animwatch.data.model.SubtitleCue
import com.gows.animwatch.data.model.UserProfile
import com.gows.animwatch.data.model.WatchProgress
import com.gows.animwatch.data.model.WatchlistCategory

/**
 * Contract for anime catalog & user state.
 * Currently backed by [MockAnimeRepository] for UI/UX development;
 * can later be swapped seamlessly with an `animeindo-api` network implementation.
 */
interface AnimeRepository {
    fun getAllAnime(): List<Anime>
    fun getAllGenres(): List<String>
    fun getInitialWatchlist(): Map<String, WatchlistCategory>
    fun getInitialWatchHistory(): List<WatchProgress>
    fun getInitialDownloads(): List<DownloadTask>
    fun getInitialComments(): List<CommentItem>
    fun getInitialNotifications(): List<NotificationItem>
    fun getInitialReminders(): Set<String>
    fun getInitialUserProfile(): UserProfile
    fun getInitialPlaybackSettings(): PlaybackSettings
    fun getSubtitleCues(animeTitle: String, episodeNumber: Int): List<SubtitleCue>
}
