package com.gows.animwatch.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.DownloadStatus
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.FranchiseSeasonHelper
import com.gows.animwatch.data.model.SubtitleSizeOption
import com.gows.animwatch.data.model.VideoAspectMode
import com.gows.animwatch.data.model.WatchlistCategory
import kotlin.math.abs
import kotlin.math.roundToInt
import com.gows.animwatch.ui.components.AnalogMeterGauge
import com.gows.animwatch.ui.components.AnimatedStreamingSceneCanvas
import com.gows.animwatch.ui.components.CassetteLinearShimmer
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.LedStatusDot
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.components.TapeReelSpinner
import com.gows.animwatch.ui.components.formatTimeSeconds
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.state.PlayerSession
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.MediaScrimDark
import com.gows.animwatch.ui.theme.MediaTextLight
import com.gows.animwatch.ui.theme.MediaTextSecondary
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary
import kotlinx.coroutines.delay

private enum class PlayerModal {
    NONE,
    QUALITY,
    SERVER,
    SUBTITLE,
    SPEED,
    EPISODES,
    CAST,
    REPORT
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PlayerScreen(
    anime: Anime,
    episode: Episode,
    session: PlayerSession,
    uiState: AnimWatchUiState,
    subtitleText: String?,
    onMinimize: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onSeekBy: (Int) -> Unit,
    onSkipIntro: () -> Unit,
    onNextEpisode: () -> Unit,
    onPreviousEpisode: () -> Unit,
    onCancelAutoNext: () -> Unit,
    onSelectEpisode: (Episode) -> Unit,
    onSelectQuality: (String) -> Unit,
    onSelectServer: (String, String) -> Unit,
    onSelectSubtitle: (String, String) -> Unit,
    onSelectSubtitleSize: (SubtitleSizeOption) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onCycleAspect: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onToggleWebStreamView: () -> Unit,
    onWebProgressUpdate: (positionSec: Int, durationSec: Int) -> Unit = { _, _ -> },
    onWebEpisodeEnded: () -> Unit = {},
    onEpisodeCompleted: (String, Int) -> Unit,
    onMarkAnimeCompleted: (String) -> Unit,
    onToggleLike: () -> Unit,
    onSelectWatchlist: (String, WatchlistCategory?) -> Unit,
    onPostComment: (String, Int?, String, Boolean) -> Unit,
    onToggleCommentLike: (String) -> Unit,
    onDeleteComment: (String) -> Unit = {},
    onOpenAnimeDetail: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val activity = remember(view) { view.context.findActivity() }
    val audioManager = remember(view) {
        view.context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }
    var showControls by remember { mutableStateOf(true) }
    var activeModal by remember { mutableStateOf(PlayerModal.NONE) }
    var showAccessibilitySliders by remember { mutableStateOf(false) }

    val originalWinBrightness = remember(activity) {
        activity?.window?.attributes?.screenBrightness ?: -1f
    }
    val initialBrightness = remember(originalWinBrightness) {
        if (originalWinBrightness in 0.05f..1.0f) originalWinBrightness else 1.0f
    }
    val initialVolume = remember(audioManager) {
        val max = (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1)
        val cur = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (max * 3 / 4)
        (cur.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    }

    var hasAdjustedBrightness by remember(activity) { mutableStateOf(false) }
    var brightnessLevel by remember { mutableFloatStateOf(initialBrightness) }
    var volumeLevel by remember { mutableFloatStateOf(initialVolume) }
    var activeGestureHud by remember { mutableStateOf<String?>(null) }
    var seekFeedbackLabel by remember { mutableStateOf<String?>(null) }

    fun applyBrightness(target: Float) {
        val clamped = target.coerceIn(0.05f, 1.0f)
        hasAdjustedBrightness = true
        brightnessLevel = clamped
        activity?.window?.let { win ->
            val lp = win.attributes
            lp.screenBrightness = clamped
            win.attributes = lp
        }
        activeGestureHud = "BRIGHTNESS:${(clamped * 100).roundToInt()}"
    }

    fun applyVolume(target: Float) {
        val clamped = target.coerceIn(0.0f, 1.0f)
        volumeLevel = clamped
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val targetIdx = (clamped * max).roundToInt().coerceIn(0, max)
            runCatching { am.setStreamVolume(AudioManager.STREAM_MUSIC, targetIdx, 0) }
        }
        activeGestureHud = "VOLUME:${(clamped * 100).roundToInt()}"
    }

    val franchiseSeasons = remember(anime, uiState.allAnime) {
        FranchiseSeasonHelper.findFranchiseSeasons(anime, uiState.allAnime)
    }
    val currentSeasonIndex = remember(franchiseSeasons, anime.id) {
        franchiseSeasons.indexOfFirst { it.id == anime.id }.coerceAtLeast(0)
    }
    val nextSeason = remember(franchiseSeasons, currentSeasonIndex) {
        if (currentSeasonIndex + 1 < franchiseSeasons.size) franchiseSeasons[currentSeasonIndex + 1] else null
    }

    var isScrubbing by remember(session.episodeId) { mutableStateOf(false) }
    var scrubSeconds by remember(session.episodeId) { mutableIntStateOf(session.positionSeconds) }

    var commentDraft by remember(anime.id) { mutableStateOf("") }
    var isSpoilerDraft by remember(anime.id) { mutableStateOf(false) }
    var filterThisEpisodeOnly by remember(anime.id) { mutableStateOf(false) }
    var replyTargetAuthor by remember(anime.id) { mutableStateOf<String?>(null) }
    var revealedSpoilers by remember(anime.id) { mutableStateOf(setOf<String>()) }

    DisposableEffect(activity) {
        onDispose {
            activity?.window?.let { win ->
                val lp = win.attributes
                lp.screenBrightness = originalWinBrightness
                win.attributes = lp
            }
        }
    }

    // Manage landscape orientation, immersive bars & keep-screen-on safely via Context.findActivity()
    DisposableEffect(session.isFullscreen, view) {
        val activity = view.context.findActivity()
        val window = activity?.window
        if (activity != null && window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val controller = WindowCompat.getInsetsController(window, view)
            if (session.isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.show(WindowInsetsCompat.Type.statusBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
        onDispose {
            if (activity != null && window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                val controller = WindowCompat.getInsetsController(window, view)
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.show(WindowInsetsCompat.Type.statusBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    LaunchedEffect(showControls, session.isPlaying, session.isLocked, activeModal) {
        if (showControls && session.isPlaying && !session.isLocked && activeModal == PlayerModal.NONE) {
            delay(3600L)
            showControls = false
        }
    }

    LaunchedEffect(seekFeedbackLabel) {
        if (seekFeedbackLabel != null) {
            delay(750L)
            seekFeedbackLabel = null
        }
    }

    LaunchedEffect(activeGestureHud) {
        if (activeGestureHud != null) {
            delay(900L)
            activeGestureHud = null
        }
    }

    val currentServer = episode.servers.find { it.id == session.selectedServerId }
        ?: episode.servers.firstOrNull()
    val watchlistCategory = uiState.watchlist[anime.id]
    val episodeComments = remember(uiState.comments, anime.id, episode.number, filterThisEpisodeOnly) {
        uiState.comments.filter { c ->
            c.animeId == anime.id && (!filterThisEpisodeOnly || c.episodeNumber == episode.number)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .let { if (!session.isFullscreen) it.statusBarsPadding() else it }
    ) {
        // ===========================
        // VIDEO VIEWPORT
        // ===========================
        val playerBoxModifier = if (session.isFullscreen) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        }

        val scaleFactor = when (session.aspectMode) {
            VideoAspectMode.FIT -> 1.0f
            VideoAspectMode.FILL -> 1.12f
            VideoAspectMode.ZOOM -> 1.28f
        }

        val showingLiveWebStream = session.streamUrl.isNotBlank() && session.useWebStreamView

        // Slim Top Header Bar in Portrait Mode (minimize + title only; fullscreen uses player's native button)
        if (!session.isFullscreen && showingLiveWebStream) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MediaScrimDark)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMinimize,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Perkecil Player",
                        tint = MediaTextLight
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = anime.title,
                        color = MediaTextLight,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append("Ep ${episode.number}")
                            if (!anime.officialChannel.isNullOrBlank()) {
                                append(" • ${anime.officialChannel}")
                            }
                        },
                        color = MediaTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = playerBoxModifier
                .background(MediaScrimDark)
                .clip(RoundedCornerShape(0.dp))
                .let { baseMod ->
                    if (showingLiveWebStream) {
                        baseMod
                    } else {
                        baseMod
                            .pointerInput(session.isLocked) {
                                detectTapGestures(
                                    onTap = { showControls = !showControls },
                                    onDoubleTap = { offset ->
                                        if (session.isLocked) return@detectTapGestures
                                        if (offset.x < size.width * 0.45f) {
                                            onSeekBy(-10)
                                            seekFeedbackLabel = "-10 detik"
                                        } else if (offset.x > size.width * 0.55f) {
                                            onSeekBy(10)
                                            seekFeedbackLabel = "+10 detik"
                                        } else {
                                            onTogglePlayPause()
                                        }
                                    }
                                )
                            }
                            .pointerInput(session.isLocked) {
                                if (!session.isLocked) {
                                    var dragLeft = true
                                    detectVerticalDragGestures(
                                        onDragStart = { start ->
                                            dragLeft = start.x < size.width * 0.5f
                                        },
                                        onVerticalDrag = { _, dragAmount ->
                                            val delta = -dragAmount / (size.height * 0.85f)
                                            if (dragLeft) {
                                                applyBrightness(brightnessLevel + delta)
                                            } else {
                                                applyVolume(volumeLevel + delta)
                                            }
                                        }
                                    )
                                }
                            }
                    }
                }
        ) {
            if (showingLiveWebStream) {
                KumanimeWebStreamPlayer(
                    streamUrl = session.streamUrl,
                    isFullscreen = session.isFullscreen,
                    isPlaying = session.isPlaying,
                    onRequestFullscreen = { wantFull ->
                        if (wantFull != session.isFullscreen) onToggleFullscreen()
                    },
                    onWebProgressUpdate = onWebProgressUpdate,
                    onEpisodeEnded = onWebEpisodeEnded,
                    onVerticalGesture = { isLeftSide, deltaFraction ->
                        if (!session.isLocked) {
                            if (isLeftSide) {
                                applyBrightness(brightnessLevel + deltaFraction)
                            } else {
                                applyVolume(volumeLevel + deltaFraction)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AnimatedStreamingSceneCanvas(
                    artTheme = anime.artTheme,
                    isPlaying = session.isPlaying,
                    positionSeconds = session.positionSeconds,
                    playbackSpeed = session.playbackSpeed,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scaleFactor)
                )

                // Live Subtitle Overlay
                if (subtitleText != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(
                                bottom = if (showControls) 68.dp else 18.dp,
                                start = 24.dp,
                                end = 24.dp
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(MediaScrimDark.copy(alpha = 0.78f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = subtitleText,
                            color = MediaTextLight,
                            fontSize = (13f * uiState.settings.subtitleSize.scale).sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Skip Intro Floating Pill
                val inIntro = session.positionSeconds in episode.introStartSec..episode.introEndSec
                androidx.compose.animation.AnimatedVisibility(
                    visible = inIntro && !session.isLocked && session.autoNextCountdownSec == null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = if (showControls) 72.dp else 18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GoldGradientBrush)
                            .clickable(onClick = onSkipIntro)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lewati Intro",
                            color = TextOnGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Rounded.FastForward,
                            contentDescription = null,
                            tint = TextOnGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Double-Tap Seek Feedback
                androidx.compose.animation.AnimatedVisibility(
                    visible = seekFeedbackLabel != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MediaScrimDark.copy(alpha = 0.84f))
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = seekFeedbackLabel.orEmpty(),
                            color = MediaTextLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Auto-Next Episode Countdown Overlay
                session.autoNextCountdownSec?.let { count ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MediaScrimDark.copy(alpha = 0.84f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(ObsidianElevated)
                                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(24.dp))
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = "Episode berikutnya dalam $count dtk",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Episode ${episode.number + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                GoldOutlinedButton(
                                    text = "Batal",
                                    onClick = onCancelAutoNext,
                                    height = 40.dp
                                )
                                GoldGradientButton(
                                    text = "Putar",
                                    icon = Icons.Rounded.SkipNext,
                                    onClick = onNextEpisode,
                                    height = 40.dp
                                )
                            }
                        }
                    }
                }

                // Main Interactive Player Controls Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = showControls && session.autoNextCountdownSec == null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (session.isLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                                IconButton(
                                    onClick = onToggleLock,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MediaScrimDark.copy(alpha = 0.78f))
                                        .border(1.dp, com.gows.animwatch.ui.theme.MediaAccentBright.copy(alpha = 0.8f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Buka Kunci",
                                        tint = com.gows.animwatch.ui.theme.MediaAccentBright
                                    )
                                }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MediaScrimDark.copy(alpha = 0.54f))
                        ) {
                            // Top Control Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    if (session.isFullscreen) onToggleFullscreen() else onMinimize()
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Perkecil Player",
                                        tint = MediaTextLight
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = anime.title,
                                        color = MediaTextLight,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Ep ${episode.number} • ${session.selectedQuality} • ${currentServer?.name ?: "Server"}",
                                        color = MediaTextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(onClick = { activeModal = PlayerModal.CAST }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Cast,
                                        contentDescription = "Cast",
                                        tint = MediaTextLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = onToggleLock) {
                                    Icon(
                                        imageVector = Icons.Rounded.LockOpen,
                                        contentDescription = "Kunci Layar",
                                        tint = MediaTextLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (session.isFullscreen) {
                                    IconButton(onClick = { activeModal = PlayerModal.EPISODES }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                                            contentDescription = "Episode",
                                            tint = com.gows.animwatch.ui.theme.MediaAccentBright,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // Center Transport Controls
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                IconButton(
                                    onClick = onPreviousEpisode,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipPrevious,
                                        contentDescription = "Episode Sebelumnya",
                                        tint = MediaTextLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onSeekBy(-10)
                                        seekFeedbackLabel = "-10 detik"
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Replay10,
                                        contentDescription = "Mundur 10 detik",
                                        tint = MediaTextLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(GoldGradientBrush)
                                        .clickable(onClick = onTogglePlayPause),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (session.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = "Putar / Jeda",
                                        tint = TextOnGold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onSeekBy(10)
                                        seekFeedbackLabel = "+10 detik"
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Forward10,
                                        contentDescription = "Maju 10 detik",
                                        tint = MediaTextLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onNextEpisode,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = "Episode Berikutnya",
                                        tint = MediaTextLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Bottom Scrub Bar & Option Pills
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val shownSec = if (isScrubbing) scrubSeconds else session.positionSeconds
                                    Text(
                                        text = formatTimeSeconds(shownSec),
                                        color = MediaTextLight,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Slider(
                                        value = shownSec.toFloat(),
                                        onValueChange = {
                                            isScrubbing = true
                                            scrubSeconds = it.toInt()
                                        },
                                        onValueChangeFinished = {
                                            isScrubbing = false
                                            onSeekTo(scrubSeconds)
                                        },
                                        valueRange = 0f..session.durationSeconds.coerceAtLeast(1).toFloat(),
                                        colors = SliderDefaults.colors(
                                            thumbColor = GoldBright,
                                            activeTrackColor = GoldPrimary,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.28f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp)
                                            .padding(horizontal = 8.dp)
                                    )

                                    Text(
                                        text = formatTimeSeconds(session.durationSeconds),
                                        color = MediaTextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        PlayerBottomBadge(
                                            icon = Icons.Rounded.HighQuality,
                                            label = session.selectedQuality,
                                            onClick = { activeModal = PlayerModal.QUALITY }
                                        )
                                        PlayerBottomBadge(
                                            icon = Icons.Rounded.Dns,
                                            label = currentServer?.name ?: "Server",
                                            onClick = { activeModal = PlayerModal.SERVER }
                                        )
                                        PlayerBottomBadge(
                                            icon = Icons.Rounded.Subtitles,
                                            label = session.selectedSubtitleId.uppercase(),
                                            onClick = { activeModal = PlayerModal.SUBTITLE }
                                        )
                                        PlayerBottomBadge(
                                            icon = Icons.Rounded.Speed,
                                            label = "${session.playbackSpeed}x",
                                            onClick = { activeModal = PlayerModal.SPEED }
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = onCycleAspect,
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AspectRatio,
                                                contentDescription = "Rasio Layar",
                                                tint = MediaTextLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = onToggleFullscreen,
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (session.isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                                contentDescription = if (session.isFullscreen) "Keluar Layar Penuh" else "Layar Penuh",
                                                tint = MediaTextLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Software screen brightness dimming overlay (only after user adjusts brightness below 96%)
            if (hasAdjustedBrightness && brightnessLevel < 0.96f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = ((1f - brightnessLevel) * 0.55f).coerceIn(0f, 0.52f)))
                )
            }

            // Auto-Next Episode Countdown Overlay (works on both live WebView & simulated player)
            if (showingLiveWebStream) {
                session.autoNextCountdownSec?.let { count ->
                    val sortedEps = remember(anime.episodes) { anime.episodes.sortedBy { it.number } }
                    val curEpIdx = sortedEps.indexOfFirst { it.id == episode.id }
                    val nextEpTitle = sortedEps.getOrNull(curEpIdx + 1)?.let { "Episode ${it.number}" }
                        ?: nextSeason?.title
                        ?: "Episode Berikutnya"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MediaScrimDark.copy(alpha = 0.84f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(ObsidianElevated)
                                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(24.dp))
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = "Episode berikutnya dalam $count dtk",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextEpTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                GoldOutlinedButton(
                                    text = "Batal",
                                    onClick = onCancelAutoNext,
                                    height = 40.dp
                                )
                                GoldGradientButton(
                                    text = "Putar",
                                    icon = Icons.Rounded.SkipNext,
                                    onClick = onNextEpisode,
                                    height = 40.dp
                                )
                            }
                        }
                    }
                }
            }

            // Brightness (Left Scroll) & Volume (Right Scroll) Visual HUD Overlay
            activeGestureHud?.let { hud ->
                val isBrightness = hud.startsWith("BRIGHTNESS")
                val pct = hud.substringAfter(":").toIntOrNull() ?: 50
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(196.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MediaScrimDark.copy(alpha = 0.88f))
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 13.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isBrightness) Icons.Rounded.Brightness6 else Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBrightness) "Pencahayaan $pct%" else "Suara $pct%",
                            color = MediaTextLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AnalogMeterGauge(
                        progress = (pct / 100f).coerceIn(0f, 1f),
                        height = 6.dp
                    )
                }
            }

            if (session.isLoadingStream) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MediaScrimDark.copy(alpha = 0.86f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Menyiapkan tautan stream...",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MediaTextLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CassetteLinearShimmer(modifier = Modifier.fillMaxWidth(0.75f))
                }
            }
        }

        // ===========================
        // PORTRAIT CONTENT BELOW VIDEO
        // ===========================
        if (!session.isFullscreen) {
            // Streaming-site Episode Navigation & Source Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianCard)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    LedStatusDot(
                        color = if (session.streamType == "official_episode") StatusEmerald else GoldPrimary,
                        pulsing = false,
                        size = 7.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            session.isLoadingStream -> "Menyiapkan stream..."
                            showingLiveWebStream -> anime.officialChannel ?: "Sedang Diputar"
                            session.isPlaying -> "Mode Pemutar • ${session.selectedQuality}"
                            else -> "Dijeda"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(ObsidianVoid)
                            .border(0.75.dp, GoldSubtleBorder, CircleShape)
                            .clickable(onClick = onPreviousEpisode),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Episode Sebelumnya",
                            tint = TextPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(ObsidianVoid)
                            .border(0.75.dp, GoldSubtleBorder, CircleShape)
                            .clickable(onClick = onNextEpisode),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Episode Berikutnya",
                            tint = GoldPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (showAccessibilitySliders) GoldTintStrong else ObsidianVoid)
                            .border(
                                0.75.dp,
                                if (showAccessibilitySliders) GoldPrimary else GoldSubtleBorder,
                                CircleShape
                            )
                            .clickable { showAccessibilitySliders = !showAccessibilitySliders },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Brightness6,
                            contentDescription = "Aksesibilitas Suara & Pencahayaan",
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (session.streamUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GoldTintStrong)
                                .clickable(onClick = onToggleWebStreamView)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (session.useWebStreamView) "Pemutar" else "Video",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldPrimary
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = DividerSubtle)

            if (showAccessibilitySliders) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianCard)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Aksesibilitas: Geser Kiri layar untuk Pencahayaan • Geser Kanan layar untuk Suara",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Brightness6,
                            contentDescription = "Pencahayaan",
                            tint = GoldPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cahaya",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.width(52.dp)
                        )
                        Slider(
                            value = brightnessLevel,
                            onValueChange = { applyBrightness(it) },
                            valueRange = 0.05f..1.0f,
                            modifier = Modifier.weight(1f).height(24.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = GoldBright,
                                activeTrackColor = GoldPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(brightnessLevel * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Suara",
                            tint = GoldPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Suara",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.width(52.dp)
                        )
                        Slider(
                            value = volumeLevel,
                            onValueChange = { applyVolume(it) },
                            valueRange = 0.0f..1.0f,
                            modifier = Modifier.weight(1f).height(24.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = GoldBright,
                                activeTrackColor = GoldPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(volumeLevel * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
                HorizontalDivider(color = DividerSubtle)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Title & Current Episode Info
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GoldTagChip(text = "Episode ${episode.number}", highlighted = true)
                            GoldTagChip(text = session.selectedQuality)
                            GoldTagChip(text = anime.officialChannel ?: currentServer?.name ?: "AnimWatch")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = episode.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${anime.title} • ${anime.studio}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldPrimary,
                            modifier = Modifier.clickable { onOpenAnimeDetail(anime.id) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = episode.synopsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        val posFraction = if (session.durationSeconds > 0) {
                            session.positionSeconds.toFloat() / session.durationSeconds
                        } else 0f
                        AnalogMeterGauge(
                            progress = posFraction,
                            height = 6.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PlayerActionChip(
                                icon = Icons.Rounded.TaskAlt,
                                label = if (watchlistCategory == WatchlistCategory.COMPLETED) "Tamat" else "Tamatkan",
                                active = watchlistCategory == WatchlistCategory.COMPLETED,
                                onClick = { onMarkAnimeCompleted(anime.id) }
                            )
                            PlayerActionChip(
                                icon = if (watchlistCategory != null) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                                label = if (watchlistCategory != null) "Disimpan" else "Simpan",
                                active = watchlistCategory != null,
                                onClick = {
                                    onSelectWatchlist(
                                        anime.id,
                                        if (watchlistCategory == null) WatchlistCategory.WATCHING else null
                                    )
                                }
                            )
                            PlayerActionChip(
                                icon = Icons.Rounded.ThumbUp,
                                label = if (session.isEpisodeLiked) "Disukai" else "Suka",
                                active = session.isEpisodeLiked,
                                onClick = onToggleLike
                            )
                            PlayerActionChip(
                                icon = Icons.Rounded.Share,
                                label = "Bagikan",
                                active = false,
                                onClick = {
                                    val shareUrl = episode.streamUrl.ifBlank { "https://animwatch.id/anime/${anime.slug}" }
                                    val shareText = "Tonton ${anime.title} Episode ${episode.number} di AnimWatch: $shareUrl"
                                    val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                    clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("AnimWatch", shareUrl))
                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "${anime.title} Ep ${episode.number}")
                                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                    }
                                    runCatching {
                                        view.context.startActivity(
                                            android.content.Intent.createChooser(sendIntent, "Bagikan Episode")
                                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }.onFailure {
                                        onShowMessage("Tautan ${anime.title} Ep ${episode.number} disalin")
                                    }
                                }
                            )
                            PlayerActionChip(
                                icon = Icons.Rounded.Flag,
                                label = "Laporkan",
                                active = false,
                                onClick = { activeModal = PlayerModal.REPORT }
                            )
                        }
                    }
                    HorizontalDivider(color = DividerSubtle)
                }

                // Quick Season & Episode Switcher Strip
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (franchiseSeasons.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pilih Season (${franchiseSeasons.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                if (nextSeason != null) {
                                    val nextIdx = (currentSeasonIndex + 1).coerceAtMost(franchiseSeasons.lastIndex)
                                    val nextLabel = FranchiseSeasonHelper.formatSeasonShortLabel(nextSeason, nextIdx)
                                    Row(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(GoldGradientBrush)
                                            .clickable { onOpenAnimeDetail(nextSeason.id) }
                                            .padding(horizontal = 11.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Lanjut ke $nextLabel",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextOnGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.SkipNext,
                                            contentDescription = null,
                                            tint = TextOnGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(franchiseSeasons.size) { idx ->
                                    val sItem = franchiseSeasons[idx]
                                    val isCurrentSeason = sItem.id == anime.id
                                    val sLabel = FranchiseSeasonHelper.formatSeasonShortLabel(sItem, idx)
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isCurrentSeason) GoldGradientBrush
                                                else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
                                            )
                                            .border(
                                                0.75.dp,
                                                if (isCurrentSeason) Color.Transparent else GoldSubtleBorder,
                                                CircleShape
                                            )
                                            .clickable(enabled = !isCurrentSeason) {
                                                onOpenAnimeDetail(sItem.id)
                                            }
                                            .padding(horizontal = 13.dp, vertical = 6.5.dp)
                                    ) {
                                        Text(
                                            text = "$sLabel • ${sItem.releasedEpisodes} Ep",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isCurrentSeason) TextOnGold else TextPrimary,
                                            fontWeight = if (isCurrentSeason) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pilih Episode (${anime.episodes.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Detail Anime",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onOpenAnimeDetail(anime.id) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(anime.episodes, key = { it.id }) { ep ->
                                val isCurrent = ep.id == episode.id
                                Column(
                                    modifier = Modifier
                                        .width(134.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isCurrent) GoldGradientBrush
                                            else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
                                        )
                                        .border(
                                            if (isCurrent) 0.dp else 0.75.dp,
                                            if (isCurrent) Color.Transparent else GoldSubtleBorder,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { onSelectEpisode(ep) }
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Episode ${ep.number}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isCurrent) TextOnGold else GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = ep.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isCurrent) TextOnGold.copy(alpha = 0.88f) else TextSecondary,
                                        fontSize = 11.5.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = DividerSubtle)
                }

                // Comments Section inside Player
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Komentar Penonton (${episodeComments.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (!filterThisEpisodeOnly) GoldTintStrong else ObsidianCard)
                                        .border(0.75.dp, if (!filterThisEpisodeOnly) GoldPrimary else GoldSubtleBorder, CircleShape)
                                        .clickable { filterThisEpisodeOnly = false }
                                        .padding(horizontal = 10.dp, vertical = 4.5.dp)
                                ) {
                                    Text(
                                        text = "Semua",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (!filterThisEpisodeOnly) GoldPrimary else TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (filterThisEpisodeOnly) GoldTintStrong else ObsidianCard)
                                        .border(0.75.dp, if (filterThisEpisodeOnly) GoldPrimary else GoldSubtleBorder, CircleShape)
                                        .clickable { filterThisEpisodeOnly = true }
                                        .padding(horizontal = 10.dp, vertical = 4.5.dp)
                                ) {
                                    Text(
                                        text = "Ep ${episode.number}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (filterThisEpisodeOnly) GoldPrimary else TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        if (replyTargetAuthor != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Membalas @$replyTargetAuthor (Ketuk untuk batal)",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary,
                                modifier = Modifier.clickable { replyTargetAuthor = null }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TactileInputField(
                                label = "Komentar Episode ${episode.number}",
                                value = commentDraft,
                                onValueChange = { commentDraft = it },
                                placeholder = if (replyTargetAuthor != null) "Balas @$replyTargetAuthor..." else "Tulis komentar untuk episode ini...",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (commentDraft.isNotBlank()) {
                                        val finalContent = if (replyTargetAuthor != null && !commentDraft.startsWith("@")) {
                                            "@$replyTargetAuthor $commentDraft"
                                        } else commentDraft
                                        onPostComment(anime.id, episode.number, finalContent, isSpoilerDraft)
                                        commentDraft = ""
                                        isSpoilerDraft = false
                                        replyTargetAuthor = null
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(GoldGradientBrush)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Kirim",
                                    tint = TextOnGold
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isSpoilerDraft = !isSpoilerDraft }
                        ) {
                            Checkbox(
                                checked = isSpoilerDraft,
                                onCheckedChange = { isSpoilerDraft = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GoldPrimary,
                                    uncheckedColor = TextTertiary,
                                    checkmarkColor = TextOnGold
                                )
                            )
                            Text(
                                text = "Tandai komentar ini mengandung spoiler",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                items(episodeComments, key = { it.id }) { comment ->
                    CommentCardItem(
                        comment = comment,
                        spoilerRevealed = comment.id in revealedSpoilers,
                        onRevealSpoiler = { revealedSpoilers = revealedSpoilers + comment.id },
                        onLikeClick = { onToggleCommentLike(comment.id) },
                        onReplyClick = { replyTargetAuthor = comment.author },
                        onDeleteClick = if (comment.isOwn || comment.author.equals(uiState.userProfile.displayName, ignoreCase = true)) {
                            { onDeleteComment(comment.id) }
                        } else null,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }

    // ===========================
    // IN-PLAYER MODALS & SHEETS
    // ===========================
    when (activeModal) {
        PlayerModal.QUALITY -> {
            PlayerSelectionSheet(
                title = "Pilih Kualitas Video",
                onDismiss = { activeModal = PlayerModal.NONE }
            ) {
                val qualityList = episode.qualities.ifEmpty {
                    listOf(
                        com.gows.animwatch.data.model.VideoQuality("1080p", "1920x1080 FHD", 320, true),
                        com.gows.animwatch.data.model.VideoQuality("720p", "1280x720 HD", 185, false),
                        com.gows.animwatch.data.model.VideoQuality("480p", "854x480 SD", 105, false)
                    )
                }
                qualityList.forEach { q ->
                    val selected = q.label == session.selectedQuality
                    ModalOptionRow(
                        title = "${q.label} (${q.resolution})",
                        subtitle = "Estimasi ~${q.sizeEstimateMb} MB",
                        badge = if (q.isUltraHd) "FHD" else null,
                        selected = selected,
                        onClick = {
                            onSelectQuality(q.label)
                            activeModal = PlayerModal.NONE
                        }
                    )
                }
            }
        }

        PlayerModal.SERVER -> {
            PlayerSelectionSheet(
                title = "Pilih Server Mirror",
                onDismiss = { activeModal = PlayerModal.NONE }
            ) {
                val serverList = episode.servers.ifEmpty {
                    listOf(
                        com.gows.animwatch.data.model.StreamServer("kumanime", "Kumanime Embed", "ID-MAIN", 24, true)
                    )
                }
                serverList.forEach { srv ->
                    val selected = srv.id == session.selectedServerId
                    ModalOptionRow(
                        title = srv.name,
                        subtitle = "Region ${srv.region} • Ping ${srv.latencyMs} ms",
                        badge = if (srv.isRecommended) "Cepat" else null,
                        selected = selected,
                        onClick = {
                            onSelectServer(srv.id, srv.name)
                            activeModal = PlayerModal.NONE
                        }
                    )
                }
            }
        }

        PlayerModal.SUBTITLE -> {
            PlayerSelectionSheet(
                title = "Pengaturan Subtitle",
                onDismiss = { activeModal = PlayerModal.NONE }
            ) {
                val subtitleList = episode.subtitles.ifEmpty {
                    listOf(
                        com.gows.animwatch.data.model.SubtitleTrack("id", "Bahasa Indonesia", "ID"),
                        com.gows.animwatch.data.model.SubtitleTrack("en", "English", "EN"),
                        com.gows.animwatch.data.model.SubtitleTrack("off", "Nonaktif", "OFF")
                    )
                }
                subtitleList.forEach { sub ->
                    val selected = sub.id == session.selectedSubtitleId
                    ModalOptionRow(
                        title = sub.label,
                        subtitle = if (sub.id == "off") "Sembunyikan teks terjemahan" else "Bahasa: ${sub.code}",
                        badge = null,
                        selected = selected,
                        onClick = {
                            onSelectSubtitle(sub.id, sub.label)
                            activeModal = PlayerModal.NONE
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ukuran Teks Subtitle",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubtitleSizeOption.entries.forEach { opt ->
                        val selected = uiState.settings.subtitleSize == opt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (selected) GoldGradientBrush else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard)))
                                .border(
                                    if (selected) 0.dp else 0.75.dp,
                                    if (selected) Color.Transparent else GoldSubtleBorder,
                                    CircleShape
                                )
                                .clickable { onSelectSubtitleSize(opt) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) TextOnGold else TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        PlayerModal.SPEED -> {
            PlayerSelectionSheet(
                title = "Kecepatan Pemutaran",
                onDismiss = { activeModal = PlayerModal.NONE }
            ) {
                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                    ModalOptionRow(
                        title = if (spd == 1.0f) "1.0x (Normal)" else "${spd}x",
                        subtitle = null,
                        badge = null,
                        selected = session.playbackSpeed == spd,
                        onClick = {
                            onSelectSpeed(spd)
                            activeModal = PlayerModal.NONE
                        }
                    )
                }
            }
        }

        PlayerModal.EPISODES -> {
            PlayerSelectionSheet(
                title = "Pilih Episode (${anime.episodes.size})",
                onDismiss = { activeModal = PlayerModal.NONE }
            ) {
                anime.episodes.forEach { ep ->
                    ModalOptionRow(
                        title = "Episode ${ep.number}: ${ep.title}",
                        subtitle = "${ep.durationSeconds / 60} menit • ${ep.releaseDate}",
                        badge = if (ep.id == episode.id) "Diputar" else null,
                        selected = ep.id == episode.id,
                        onClick = {
                            onSelectEpisode(ep)
                            activeModal = PlayerModal.NONE
                        }
                    )
                }
            }
        }

        PlayerModal.CAST -> {
            AlertDialog(
                onDismissRequest = { activeModal = PlayerModal.NONE },
                containerColor = ObsidianElevated,
                shape = RoundedCornerShape(24.dp),
                titleContentColor = TextPrimary,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Tv, contentDescription = null, tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cast ke Layar TV", style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Gunakan fitur Cast / Wireless Display sistem Android untuk memproyeksikan pemutar AnimWatch ke Smart TV di jaringan Wi-Fi yang sama.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ObsidianCard)
                                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    activeModal = PlayerModal.NONE
                                    runCatching {
                                        view.context.startActivity(
                                            android.content.Intent(android.provider.Settings.ACTION_CAST_SETTINGS)
                                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }.onFailure {
                                        onShowMessage("Buka Pengaturan Android > Perangkat Terhubung > Cast untuk menyambungkan ke TV")
                                    }
                                }
                                .padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Cast, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Buka Pengaturan Cast Android", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeModal = PlayerModal.NONE }) {
                        Text("Tutup", color = TextSecondary)
                    }
                }
            )
        }

        PlayerModal.REPORT -> {
            AlertDialog(
                onDismissRequest = { activeModal = PlayerModal.NONE },
                containerColor = ObsidianElevated,
                shape = RoundedCornerShape(24.dp),
                titleContentColor = TextPrimary,
                title = {
                    Text(
                        "Laporkan Masalah",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Video tidak mau diputar / buffering terus",
                            "Subtitle tidak sinkron dengan suara",
                            "Audio hilang atau rusak",
                            "Episode salah / tertukar"
                        ).forEach { reason ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ObsidianCard)
                                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        onShowMessage("Laporan terkirim: $reason. Terima kasih!")
                                        activeModal = PlayerModal.NONE
                                    }
                                    .padding(13.dp)
                            ) {
                                Text(reason, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeModal = PlayerModal.NONE }) {
                        Text("Batal", color = TextSecondary)
                    }
                }
            )
        }

        PlayerModal.NONE -> Unit
    }
}

@Composable
private fun PlayerBottomBadge(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MediaScrimDark.copy(alpha = 0.72f))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GoldBright,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = MediaTextLight,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlayerActionChip(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (active) GoldTintStrong else ObsidianCard)
                .border(
                    0.75.dp,
                    if (active) GoldPrimary else GoldSubtleBorder,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) GoldPrimary else TextSecondary,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) GoldPrimary else TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSelectionSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianElevated,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ModalOptionRow(
    title: String,
    subtitle: String?,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) GoldTintStrong else ObsidianCard)
            .border(
                if (selected) 1.dp else 0.75.dp,
                if (selected) GoldPrimary else GoldSubtleBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) GoldPrimary else TextPrimary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (badge != null) {
                    GoldTagChip(text = badge, highlighted = true)
                }
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = GoldPrimary
            )
        }
    }
}

private val YOUTUBE_ID_REGEX = Regex(
    "(?:youtube-nocookie\\.com/embed/|youtube\\.com/embed/|youtu\\.be/|youtube\\.com/watch\\?(?:[^#]*&)?v=)([A-Za-z0-9_-]{11})"
)

private fun extractYoutubeVideoId(url: String): String? =
    YOUTUBE_ID_REGEX.find(url)?.groupValues?.getOrNull(1)

private fun buildYoutubeEmbedHtml(videoId: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
      <style>
        html,body{margin:0;padding:0;width:100%;height:100%;background:#0C0C10;overflow:hidden;}
        iframe{position:fixed;inset:0;width:100%;height:100%;border:0;}
      </style>
    </head>
    <body>
      <iframe
        id="ytplayer"
        src="https://www.youtube-nocookie.com/embed/$videoId?playsinline=1&rel=0&modestbranding=1&iv_load_policy=3&fs=1&enablejsapi=1&origin=https://www.youtube-nocookie.com"
        referrerpolicy="strict-origin-when-cross-origin"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen"
        allowfullscreen>
      </iframe>
      <script>
        (function(){
          var notified = false;
          var lastSentAt = 0;
          var knownDuration = 0;
          var lastReportedPos = 0;
          var ytState = -1;
          var frame = document.getElementById('ytplayer');
          window.__animWatchCommand = function(funcName) {
            try {
              frame.contentWindow.postMessage(JSON.stringify({event:'command',func:funcName,args:[]}),'*');
            } catch(e){}
          };
          function reportProgress(posSec, durSec) {
            var now = Date.now();
            if (now - lastSentAt < 1800) return;
            lastSentAt = now;
            if (window.AnimWatchPlayer && window.AnimWatchPlayer.onProgress) {
              window.AnimWatchPlayer.onProgress(Math.floor(posSec), Math.floor(durSec));
            }
          }
          function sendListen(){
            try {
              frame.contentWindow.postMessage(JSON.stringify({event:'listening',id:1,channel:'widget'}),'*');
              frame.contentWindow.postMessage(JSON.stringify({event:'command',func:'addEventListener',args:['onStateChange']}),'*');
              frame.contentWindow.postMessage(JSON.stringify({event:'command',func:'getDuration',args:[]}),'*');
            } catch(e){}
          }
          frame.addEventListener('load', function(){
            sendListen();
            setTimeout(sendListen, 800);
            setTimeout(sendListen, 2200);
          });
          setInterval(function(){
            try {
              if (knownDuration <= 0) sendListen();
              frame.contentWindow.postMessage(JSON.stringify({event:'command',func:'getCurrentTime',args:[]}),'*');
              frame.contentWindow.postMessage(JSON.stringify({event:'command',func:'getDuration',args:[]}),'*');
              if (ytState === 1) {
                lastReportedPos += 2;
                reportProgress(lastReportedPos, knownDuration);
              }
            } catch(e){}
          }, 2000);
          window.addEventListener('message', function(ev){
            if(typeof ev.data !== 'string') return;
            try {
              var d = JSON.parse(ev.data);
              if(d.event === 'infoDelivery' && d.info) {
                if(typeof d.info.duration === 'number' && d.info.duration > 0) {
                  knownDuration = Math.floor(d.info.duration);
                }
                if(typeof d.info.playerState === 'number') {
                  ytState = d.info.playerState;
                }
                if(typeof d.info.currentTime === 'number' && d.info.currentTime >= 0) {
                  lastReportedPos = Math.floor(d.info.currentTime);
                  reportProgress(lastReportedPos, knownDuration);
                }
              }
              if(d.event === 'onStateChange' && typeof d.info === 'number') {
                ytState = d.info;
              }
              var ended = (d.event === 'onStateChange' && d.info === 0) ||
                          (d.event === 'infoDelivery' && d.info && d.info.playerState === 0);
              if(!notified && ended){
                notified = true;
                if(knownDuration > 0 && window.AnimWatchPlayer && window.AnimWatchPlayer.onProgress){
                  window.AnimWatchPlayer.onProgress(knownDuration, knownDuration);
                }
                if(window.AnimWatchPlayer && window.AnimWatchPlayer.onVideoEnded){
                  window.AnimWatchPlayer.onVideoEnded();
                }
              }
            } catch(e){}
          });
        })();
      </script>
    </body>
    </html>
""".trimIndent()

private fun isAllowedEmbedNavigation(
    url: String,
    isMainFrame: Boolean = true,
    initialStreamHost: String = ""
): Boolean {
    val lower = url.lowercase()
    if (lower == "about:blank" ||
        lower.startsWith("https://www.youtube-nocookie.com/embed/") ||
        lower.startsWith("https://www.youtube.com/embed/")
    ) return true
    if (initialStreamHost.isNotBlank() &&
        (lower.startsWith("https://") || lower.startsWith("http://"))
    ) {
        val targetHost = runCatching { android.net.Uri.parse(url)?.host?.lowercase().orEmpty() }.getOrDefault("")
        if (targetHost == initialStreamHost || targetHost.endsWith(".$initialStreamHost")) {
            return true
        }
    }
    if (!isMainFrame) {
        return lower.startsWith("https://www.youtube-nocookie.com/") ||
            lower.startsWith("https://www.youtube.com/") ||
            lower.startsWith("https://accounts.google.com/") ||
            lower.startsWith("https://googleads.g.doubleclick.net/")
    }
    return false
}

private fun loadStreamIntoWebView(webView: WebView, streamUrl: String) {
    if (streamUrl.isBlank()) return
    if (webView.tag == streamUrl) return
    webView.tag = streamUrl
    val ytId = extractYoutubeVideoId(streamUrl)
    if (ytId != null) {
        webView.loadDataWithBaseURL(
            "https://www.youtube-nocookie.com",
            buildYoutubeEmbedHtml(ytId),
            "text/html",
            "utf-8",
            null
        )
    } else {
        webView.loadUrl(streamUrl)
    }
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
private fun KumanimeWebStreamPlayer(
    streamUrl: String,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    onRequestFullscreen: (Boolean) -> Unit,
    onWebProgressUpdate: (positionSec: Int, durationSec: Int) -> Unit,
    onEpisodeEnded: () -> Unit,
    onVerticalGesture: (isLeftSide: Boolean, deltaFraction: Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var currentWebView by remember { mutableStateOf<WebView?>(null) }
    var activeFullscreenExit by remember { mutableStateOf<(() -> Unit)?>(null) }
    val latestOnRequestFullscreen by rememberUpdatedState(onRequestFullscreen)
    val latestOnWebProgress by rememberUpdatedState(onWebProgressUpdate)
    val latestOnEpisodeEnded by rememberUpdatedState(onEpisodeEnded)
    val latestOnVerticalGesture by rememberUpdatedState(onVerticalGesture)
    val initialStreamHost = remember(streamUrl) {
        runCatching { android.net.Uri.parse(streamUrl)?.host?.lowercase().orEmpty() }.getOrDefault("")
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isFullscreen) {
        if (!isFullscreen) {
            activeFullscreenExit?.invoke()
        }
    }

    LaunchedEffect(isPlaying, currentWebView) {
        val wv = currentWebView ?: return@LaunchedEffect
        val cmd = if (isPlaying) "playVideo" else "pauseVideo"
        wv.evaluateJavascript("window.__animWatchCommand && window.__animWatchCommand('$cmd');", null)
    }

    DisposableEffect(lifecycleOwner, currentWebView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> currentWebView?.onPause()
                Lifecycle.Event.ON_RESUME -> currentWebView?.onResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeFullscreenExit?.invoke()
            activeFullscreenExit = null
            currentWebView?.apply {
                stopLoading()
                onPause()
                loadUrl("about:blank")
                clearHistory()
                (parent as? ViewGroup)?.removeView(this)
                removeAllViews()
                destroy()
            }
            currentWebView = null
        }
    }

    AndroidView(
        factory = { context ->
            val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
            val container = object : FrameLayout(context) {
                private var downX = 0f
                private var downY = 0f
                private var lastY = 0f
                private var isVerticalDragging = false

                override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
                    when (ev.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = ev.x
                            downY = ev.y
                            lastY = ev.y
                            isVerticalDragging = false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (isVerticalDragging) return true
                            val h = height
                            if (h > 0 && downY in (h * 0.08f)..(h * 0.84f)) {
                                val dx = ev.x - downX
                                val dy = ev.y - downY
                                if (abs(dy) > touchSlop && abs(dy) > abs(dx) * 1.35f) {
                                    isVerticalDragging = true
                                    lastY = ev.y
                                    parent?.requestDisallowInterceptTouchEvent(true)
                                    return true
                                }
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            isVerticalDragging = false
                        }
                    }
                    return super.onInterceptTouchEvent(ev)
                }

                override fun onTouchEvent(ev: MotionEvent): Boolean {
                    when (ev.actionMasked) {
                        MotionEvent.ACTION_MOVE -> {
                            if (isVerticalDragging && height > 0) {
                                val dy = ev.y - lastY
                                lastY = ev.y
                                val deltaFraction = -dy / (height * 0.75f)
                                val isLeft = downX < width * 0.5f
                                latestOnVerticalGesture(isLeft, deltaFraction)
                                return true
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            isVerticalDragging = false
                        }
                    }
                    return super.onTouchEvent(ev)
                }
            }.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.parseColor("#0C0C10"))
            }
            var customFullscreenView: View? = null
            var customViewCallback: WebChromeClient.CustomViewCallback? = null

            fun exitCustomFullscreen() {
                if (customFullscreenView != null) {
                    customFullscreenView?.let { container.removeView(it) }
                    customFullscreenView = null
                    customViewCallback?.onCustomViewHidden()
                    customViewCallback = null
                }
            }

            val webView = WebView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.parseColor("#0C0C10"))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportMultipleWindows(false)
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onProgress(posSec: Int, durSec: Int) {
                            post { latestOnWebProgress(posSec, durSec) }
                        }

                        @JavascriptInterface
                        fun onVideoEnded() {
                            post { latestOnEpisodeEnded() }
                        }
                    },
                    "AnimWatchPlayer"
                )
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val target = request?.url?.toString().orEmpty()
                        val isMain = request?.isForMainFrame ?: true
                        // Block all external/app redirects (never leave AnimWatch into YouTube app/browser)
                        return !isAllowedEmbedNavigation(target, isMainFrame = isMain, initialStreamHost = initialStreamHost)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return !isAllowedEmbedNavigation(url.orEmpty(), initialStreamHost = initialStreamHost)
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        if (view == null) return
                        customFullscreenView?.let { container.removeView(it) }
                        customFullscreenView = view
                        customViewCallback = callback
                        activeFullscreenExit = { exitCustomFullscreen() }
                        container.addView(
                            view,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                        latestOnRequestFullscreen(true)
                    }

                    override fun onHideCustomView() {
                        exitCustomFullscreen()
                        activeFullscreenExit = null
                        latestOnRequestFullscreen(false)
                    }
                }
                loadStreamIntoWebView(this, streamUrl)
                currentWebView = this
            }

            container.addView(webView)
            container
        },
        update = { container ->
            val webView = (0 until container.childCount)
                .map { container.getChildAt(it) }
                .filterIsInstance<WebView>()
                .firstOrNull()
            if (webView != null) {
                loadStreamIntoWebView(webView, streamUrl)
            }
        },
        modifier = modifier
    )
}
