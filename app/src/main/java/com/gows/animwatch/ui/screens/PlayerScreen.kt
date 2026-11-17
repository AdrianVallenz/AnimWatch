package com.gows.animwatch.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.DownloadStatus
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.SubtitleSizeOption
import com.gows.animwatch.data.model.VideoAspectMode
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.ui.components.AnimatedStreamingSceneCanvas
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.GoldTagChip
import com.gows.animwatch.ui.components.WatchlistCategorySheet
import com.gows.animwatch.ui.components.formatTimeSeconds
import com.gows.animwatch.ui.state.AnimWatchUiState
import com.gows.animwatch.ui.state.PlayerSession
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianCardAlt
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusEmerald
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
    onToggleLike: () -> Unit,
    onSelectWatchlist: (String, WatchlistCategory?) -> Unit,
    onDownloadEpisode: (Anime, Episode, String) -> Unit,
    onPostComment: (String, Int?, String, Boolean) -> Unit,
    onToggleCommentLike: (String) -> Unit,
    onOpenAnimeDetail: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showControls by remember { mutableStateOf(true) }
    var activeModal by remember { mutableStateOf(PlayerModal.NONE) }
    var showWatchlistSheet by remember { mutableStateOf(false) }

    var brightnessLevel by remember { mutableFloatStateOf(0.85f) }
    var volumeLevel by remember { mutableFloatStateOf(0.75f) }
    var activeGestureHud by remember { mutableStateOf<String?>(null) }
    var seekFeedbackLabel by remember { mutableStateOf<String?>(null) }

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubSeconds by remember { mutableIntStateOf(session.positionSeconds) }

    var commentDraft by remember { mutableStateOf("") }
    var revealedSpoilers by remember { mutableStateOf(setOf<String>()) }

    // Manage landscape orientation & immersive mode when fullscreen
    DisposableEffect(session.isFullscreen, view) {
        val activity = view.context as? Activity
        val window = activity?.window
        if (activity != null && window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            if (session.isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            if (activity != null && window != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.getInsetsController(window, view)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Auto-hide controls while playing
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
    val downloadTask = uiState.downloads.find { it.episodeId == episode.id }
    val watchlistCategory = uiState.watchlist[anime.id]
    val episodeComments = remember(uiState.comments, anime.id) {
        uiState.comments.filter { it.animeId == anime.id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .let { if (!session.isFullscreen) it.statusBarsPadding() else it }
    ) {
        // ===========================
        // VIDEO VIEWPORT AREA
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

        Box(
            modifier = playerBoxModifier
                .background(Color.Black)
                .clip(RoundedCornerShape(0.dp))
                .pointerInput(session.isLocked) {
                    detectTapGestures(
                        onTap = { showControls = !showControls },
                        onDoubleTap = { offset ->
                            if (session.isLocked) return@detectTapGestures
                            if (offset.x < size.width * 0.45f) {
                                onSeekBy(-10)
                                seekFeedbackLabel = "« 10 detik"
                            } else if (offset.x > size.width * 0.55f) {
                                onSeekBy(10)
                                seekFeedbackLabel = "10 detik »"
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
                                    brightnessLevel = (brightnessLevel + delta).coerceIn(0.15f, 1.0f)
                                    activeGestureHud = "BRIGHTNESS:${(brightnessLevel * 100).toInt()}"
                                } else {
                                    volumeLevel = (volumeLevel + delta).coerceIn(0f, 1.0f)
                                    activeGestureHud = "VOLUME:${(volumeLevel * 100).toInt()}"
                                }
                            }
                        )
                    }
                }
        ) {
            // Live animated scene canvas
            AnimatedStreamingSceneCanvas(
                artTheme = anime.artTheme,
                isPlaying = session.isPlaying,
                positionSeconds = session.positionSeconds,
                playbackSpeed = session.playbackSpeed,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scaleFactor)
            )

            // Simulated screen brightness dimming layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = (1f - brightnessLevel) * 0.65f))
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.68f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = subtitleText,
                        color = Color(0xFFFFF8E7),
                        fontSize = (13f * uiState.settings.subtitleSize.scale).sp,
                        fontWeight = FontWeight.SemiBold,
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldGradientBrush)
                        .clickable(onClick = onSkipIntro)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lewati Intro",
                        color = TextOnGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
                        .clip(RoundedCornerShape(24.dp))
                        .background(ObsidianVoid.copy(alpha = 0.85f))
                        .border(1.dp, GoldBright, RoundedCornerShape(24.dp))
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = seekFeedbackLabel.orEmpty(),
                        color = GoldBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Brightness / Volume Drag HUD
            activeGestureHud?.let { hud ->
                val isBrightness = hud.startsWith("BRIGHTNESS")
                val pct = hud.substringAfter(":").toIntOrNull() ?: 50
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianVoid.copy(alpha = 0.86f))
                        .border(1.dp, GoldBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isBrightness) Icons.Rounded.Brightness6 else Icons.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBrightness) "Kecerahan $pct%" else "Volume $pct%",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Auto-Next Episode Countdown Overlay
            session.autoNextCountdownSec?.let { count ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianVoid.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianElevated)
                            .border(1.dp, GoldBorder, RoundedCornerShape(18.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Episode Berikutnya dalam ${count}s",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldBright
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
                                height = 38.dp
                            )
                            GoldGradientButton(
                                text = "Putar Sekarang",
                                icon = Icons.Rounded.SkipNext,
                                onClick = onNextEpisode,
                                height = 38.dp
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
                                .background(ObsidianVoid.copy(alpha = 0.8f))
                                .border(1.dp, GoldBright, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Buka Kunci",
                                tint = GoldBright
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
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
                                    tint = TextPrimary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = anime.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Ep ${episode.number} • ${session.selectedQuality} • ${currentServer?.name ?: "Server"}",
                                    color = GoldBright,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(onClick = { activeModal = PlayerModal.CAST }) {
                                Icon(
                                    imageVector = Icons.Rounded.Cast,
                                    contentDescription = "Cast",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = onToggleLock) {
                                Icon(
                                    imageVector = Icons.Rounded.LockOpen,
                                    contentDescription = "Kunci Layar",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (session.isFullscreen) {
                                IconButton(onClick = { activeModal = PlayerModal.EPISODES }) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlaylistPlay,
                                        contentDescription = "Episode",
                                        tint = GoldBright,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Center Transport Controls
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            IconButton(
                                onClick = onPreviousEpisode,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipPrevious,
                                    contentDescription = "Episode Sebelumnya",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    onSeekBy(-10)
                                    seekFeedbackLabel = "« 10 detik"
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Replay10,
                                    contentDescription = "Mundur 10s",
                                    tint = TextPrimary,
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
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    onSeekBy(10)
                                    seekFeedbackLabel = "10 detik »"
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Forward10,
                                    contentDescription = "Maju 10s",
                                    tint = TextPrimary,
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
                                    tint = TextPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Bottom Scrub Bar & Player Option Pills
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
                                    color = GoldBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
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
                                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp)
                                        .padding(horizontal = 8.dp)
                                )

                                Text(
                                    text = formatTimeSeconds(session.durationSeconds),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
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
                                            tint = TextPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = onToggleFullscreen,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (session.isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                            contentDescription = "Layar Penuh",
                                            tint = GoldBright,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ===========================
        // PORTRAIT CONTENT BELOW VIDEO
        // ===========================
        if (!session.isFullscreen) {
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
                            GoldTagChip(text = "EPISODE ${episode.number}", highlighted = true)
                            GoldTagChip(text = session.selectedQuality)
                            GoldTagChip(text = currentServer?.name ?: "SankaStream")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = episode.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${anime.title} • ${anime.studio}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldPrimary,
                            modifier = Modifier.clickable { onOpenAnimeDetail(anime.id) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = episode.synopsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PlayerActionChip(
                                icon = Icons.Rounded.ThumbUp,
                                label = if (session.isEpisodeLiked) "Disukai" else "Suka",
                                active = session.isEpisodeLiked,
                                onClick = onToggleLike
                            )
                            PlayerActionChip(
                                icon = if (watchlistCategory != null) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                                label = if (watchlistCategory != null) "Tersimpan" else "Koleksi",
                                active = watchlistCategory != null,
                                onClick = { showWatchlistSheet = true }
                            )
                            PlayerActionChip(
                                icon = when (downloadTask?.status) {
                                    DownloadStatus.COMPLETED -> Icons.Rounded.CheckCircle
                                    else -> Icons.Rounded.Download
                                },
                                label = when (downloadTask?.status) {
                                    DownloadStatus.COMPLETED -> "Tersimpan"
                                    DownloadStatus.DOWNLOADING -> "${(downloadTask.progress * 100).toInt()}%"
                                    else -> "Unduh"
                                },
                                active = downloadTask != null,
                                onClick = { onDownloadEpisode(anime, episode, session.selectedQuality) }
                            )
                            PlayerActionChip(
                                icon = Icons.Rounded.Share,
                                label = "Bagikan",
                                active = false,
                                onClick = { onShowMessage("Tautan ${anime.title} Ep ${episode.number} disalin") }
                            )
                            PlayerActionChip(
                                icon = Icons.Rounded.Flag,
                                label = "Lapor",
                                active = false,
                                onClick = { activeModal = PlayerModal.REPORT }
                            )
                        }
                    }
                    HorizontalDivider(color = DividerSubtle)
                }

                // Quick Server & Quality Bar
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                text = "Lihat Detail Anime →",
                                color = GoldBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onOpenAnimeDetail(anime.id) }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(anime.episodes, key = { it.id }) { ep ->
                                val isCurrent = ep.id == episode.id
                                Column(
                                    modifier = Modifier
                                        .width(126.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isCurrent) GoldGradientBrush
                                            else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard))
                                        )
                                        .border(
                                            1.dp,
                                            if (isCurrent) Color.Transparent else GoldSubtleBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectEpisode(ep) }
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "EP ${ep.number}",
                                        color = if (isCurrent) TextOnGold else GoldBright,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ep.title,
                                        color = if (isCurrent) TextOnGold.copy(alpha = 0.85f) else TextSecondary,
                                        fontSize = 11.sp,
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
                        Text(
                            text = "Komentar Penonton (${episodeComments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = commentDraft,
                                onValueChange = { commentDraft = it },
                                placeholder = { Text("Komentar untuk Ep ${episode.number}...", color = TextTertiary, fontSize = 12.5.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldBright,
                                    unfocusedBorderColor = DividerSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = GoldBright
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (commentDraft.isNotBlank()) {
                                        onPostComment(anime.id, episode.number, commentDraft, false)
                                        commentDraft = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldGradientBrush)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Kirim",
                                    tint = TextOnGold
                                )
                            }
                        }
                    }
                }

                items(episodeComments, key = { it.id }) { comment ->
                    CommentCardItem(
                        comment = comment,
                        spoilerRevealed = comment.id in revealedSpoilers,
                        onRevealSpoiler = { revealedSpoilers = revealedSpoilers + comment.id },
                        onLikeClick = { onToggleCommentLike(comment.id) },
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
                episode.qualities.forEach { q ->
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
                episode.servers.forEach { srv ->
                    val selected = srv.id == session.selectedServerId
                    ModalOptionRow(
                        title = srv.name,
                        subtitle = "Region ${srv.region} • Ping ${srv.latencyMs}ms",
                        badge = if (srv.isRecommended) "CEPAT" else null,
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
                episode.subtitles.forEach { sub ->
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
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ukuran Teks Subtitle",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubtitleSizeOption.entries.forEach { opt ->
                        val selected = uiState.settings.subtitleSize == opt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) GoldGradientBrush else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard)))
                                .border(1.dp, if (selected) Color.Transparent else GoldSubtleBorder, RoundedCornerShape(10.dp))
                                .clickable { onSelectSubtitleSize(opt) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.label,
                                color = if (selected) TextOnGold else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
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
                        badge = if (ep.id == episode.id) "DIPUTAR" else null,
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
                titleContentColor = GoldBright,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Tv, contentDescription = null, tint = GoldBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cast ke Perangkat")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Pilih perangkat di jaringan Wi-Fi yang sama:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        listOf("Samsung Smart TV 4K (Ruang Tamu)", "Google TV Streamer (Kamar)").forEach { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianCard)
                                    .border(1.dp, GoldSubtleBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        onShowMessage("Terhubung ke $device")
                                        activeModal = PlayerModal.NONE
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Cast, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(device, color = TextPrimary, fontSize = 13.sp)
                            }
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
                titleContentColor = GoldBright,
                title = { Text("Laporkan Masalah Stream") },
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianCard)
                                    .clickable {
                                        onShowMessage("Laporan terkirim: $reason. Terima kasih!")
                                        activeModal = PlayerModal.NONE
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(reason, color = TextPrimary, fontSize = 13.sp)
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

    if (showWatchlistSheet) {
        WatchlistCategorySheet(
            animeTitle = anime.title,
            currentCategory = watchlistCategory,
            onSelectCategory = { cat -> onSelectWatchlist(anime.id, cat) },
            onDismiss = { showWatchlistSheet = false }
        )
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
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianVoid.copy(alpha = 0.72f))
            .border(0.75.dp, GoldBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GoldBright,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
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
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (active) GoldTintStrong else ObsidianCard)
                .border(1.dp, if (active) GoldBright else GoldSubtleBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) GoldBright else TextSecondary,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (active) GoldBright else TextSecondary,
            fontSize = 10.5.sp,
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
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = GoldBright
            )
            Spacer(modifier = Modifier.height(12.dp))
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
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) GoldTintStrong else ObsidianCard)
            .border(
                1.dp,
                if (selected) GoldBright else GoldSubtleBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    color = if (selected) GoldBright else TextPrimary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
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
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )
            }
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = GoldBright
            )
        }
    }
}
