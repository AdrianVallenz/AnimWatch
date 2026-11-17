package com.gows.animwatch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.NotificationItem
import com.gows.animwatch.data.model.WatchProgress
import com.gows.animwatch.data.model.WatchlistCategory
import com.gows.animwatch.ui.state.MainTab
import com.gows.animwatch.ui.state.PlayerSession
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBorderBrush
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.MonoFont
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianGlass
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

fun formatTimeSeconds(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@Composable
fun GoldGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 44.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 0.dp,
        animationSpec = spring(stiffness = 120f, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "btn_press_y"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = 120f, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "btn_press_scale"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .scale(scale)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(GoldGradientBrush)
            .border(1.dp, GoldBright.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextOnGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text.uppercase(),
                color = TextOnGold,
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GoldOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isActive: Boolean = false,
    height: Dp = 44.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 0.dp,
        animationSpec = spring(stiffness = 120f, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "out_btn_y"
    )
    val bgColor = if (isActive) GoldTintStrong else ObsidianCard
    val borderColor = if (isActive) GoldBright else GoldBorder
    val contentColor = if (isActive) GoldBright else TextPrimary

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) GoldBright else GoldPrimary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text.uppercase(),
                color = contentColor,
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    channelCode: String? = null,
    actionLabel: String? = "SEMUA",
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (subtitle != null) 34.dp else 20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GoldGradientBrush)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                if (channelCode != null) {
                    Text(
                        text = channelCode,
                        fontFamily = MonoFont,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (onActionClick != null && actionLabel != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldSubtleBorder, RoundedCornerShape(6.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionLabel.uppercase(),
                    fontFamily = MonoFont,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright,
                    letterSpacing = 0.6.sp
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = GoldBright,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun GoldTagChip(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (highlighted) GoldGradientBrush else Brush.linearGradient(listOf(GoldTint, GoldTint)))
            .border(
                width = 1.dp,
                color = if (highlighted) GoldBright else GoldBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = MonoFont,
            color = if (highlighted) TextOnGold else GoldBright,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
    }
}

@Composable
fun AnimePosterCard(
    anime: Anime,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp? = 148.dp
) {
    val widthMod = if (cardWidth != null) modifier.width(cardWidth) else modifier.fillMaxWidth()

    Column(
        modifier = widthMod
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
        ) {
            AnimePosterArtwork(
                artTheme = anime.artTheme,
                title = anime.title,
                modifier = Modifier.fillMaxSize()
            )

            // Top row: Rating & Bookmark Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianVoid.copy(alpha = 0.88f))
                        .border(1.dp, GoldBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = anime.rating.toString(),
                            fontFamily = MonoFont,
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianVoid.copy(alpha = 0.85f))
                        .border(1.dp, if (isBookmarked) GoldBright else GoldSubtleBorder, RoundedCornerShape(6.dp))
                        .clickable(onClick = onBookmarkClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                        contentDescription = "Simpan ke Daftar Tonton",
                        tint = if (isBookmarked) GoldBright else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Bottom telemetry inside bezel
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(ObsidianVoid.copy(alpha = 0.72f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (anime.status == AnimeStatus.ONGOING) {
                        LedStatusDot(color = StatusLive, pulsing = true, size = 6.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = if (anime.type.label == "Movie") "MOVIE // 4K"
                        else "EP ${anime.releasedEpisodes}/${anime.totalEpisodes}",
                        fontFamily = MonoFont,
                        color = GoldBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${anime.year}",
                    fontFamily = MonoFont,
                    color = TextTertiary,
                    fontSize = 9.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = anime.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = anime.genres.take(2).joinToString(" / "),
            fontFamily = MonoFont,
            fontSize = 10.sp,
            color = TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ContinueWatchingCard(
    anime: Anime,
    progress: WatchProgress,
    onPlayClick: () -> Unit,
    onDetailClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(244.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianCard)
            .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onPlayClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            AnimePosterArtwork(
                artTheme = anime.artTheme,
                title = anime.title,
                modifier = Modifier.fillMaxSize(),
                showKanjiWatermark = false
            )

            // Center Play Key
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianVoid.copy(alpha = 0.85f))
                    .border(1.5.dp, GoldBright, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Lanjutkan",
                    tint = GoldBright,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Remove key top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(ObsidianVoid.copy(alpha = 0.82f))
                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(5.dp))
                    .clickable(onClick = onRemoveClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Hapus",
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Remaining telemetry badge bottom left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ObsidianVoid.copy(alpha = 0.90f))
                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EP ${progress.episodeNumber} // -${progress.remainingMinutes}M",
                    fontFamily = MonoFont,
                    color = GoldBright,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDetailClick)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            AnalogMeterGauge(
                progress = progress.progressFraction.coerceAtLeast(0.06f),
                segments = 16,
                height = 6.dp
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = progress.episodeTitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TopRankedCard(
    rank: Int,
    anime: Anime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(164.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 18.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
            ) {
                AnimePosterArtwork(
                    artTheme = anime.artTheme,
                    title = anime.title,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianVoid.copy(alpha = 0.88f))
                        .border(0.75.dp, GoldBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${anime.rating}",
                            fontFamily = MonoFont,
                            color = GoldBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${anime.viewsLabel} VIEWS",
                fontFamily = MonoFont,
                fontSize = 10.sp,
                color = TextTertiary
            )
        }

        // Cassette Rank Number Stamp
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 0.dp, y = (-42).dp)
                .clip(RoundedCornerShape(6.dp))
                .background(ObsidianVoid)
                .border(1.2.dp, GoldBright, RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = String.format("#%02d", rank),
                style = TextStyle(
                    fontFamily = MonoFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    brush = GoldGradientBrush
                )
            )
        }
    }
}

@Composable
fun CompactAnimeRowCard(
    anime: Anime,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingBadge: String? = null,
    rankNumber: Int? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianCard)
            .border(1.dp, GoldSubtleBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rankNumber != null) {
            Text(
                text = String.format("#%02d", rankNumber),
                style = TextStyle(
                    fontFamily = MonoFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    brush = if (rankNumber <= 3) GoldGradientBrush else Brush.linearGradient(
                        listOf(TextSecondary, TextTertiary)
                    )
                ),
                modifier = Modifier.width(38.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(66.dp)
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
        ) {
            AnimePosterArtwork(
                artTheme = anime.artTheme,
                title = anime.title,
                modifier = Modifier.fillMaxSize(),
                showKanjiWatermark = false
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GoldTagChip(
                    text = trailingBadge ?: anime.status.label,
                    highlighted = anime.status == AnimeStatus.ONGOING
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${anime.rating}",
                        fontFamily = MonoFont,
                        color = GoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "// ${anime.year}",
                    fontFamily = MonoFont,
                    color = TextTertiary,
                    fontSize = 10.5.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${anime.genres.take(2).joinToString(" / ")} • EP ${anime.releasedEpisodes}/${anime.totalEpisodes}",
                fontFamily = MonoFont,
                fontSize = 10.5.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldGradientBrush)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Putar",
                    tint = TextOnGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                    contentDescription = "Simpan",
                    tint = if (isBookmarked) GoldBright else TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingGoldBottomNav(
    currentTab: MainTab,
    libraryBadgeCount: Int,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(10.dp),
        color = ObsidianGlass,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, GoldBorderBrush, RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                val selected = tab == currentTab
                val icon = when (tab) {
                    MainTab.HOME -> Icons.Rounded.Home
                    MainTab.EXPLORE -> Icons.Rounded.Explore
                    MainTab.LIBRARY -> Icons.Rounded.VideoLibrary
                    MainTab.PROFILE -> Icons.Rounded.Person
                }
                val tint by animateColorAsState(
                    targetValue = if (selected) GoldBright else TextTertiary,
                    label = "nav_tint"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (selected) GoldTintStrong else Color.Transparent,
                    label = "nav_bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .then(
                            if (selected) Modifier.border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                            else Modifier
                        )
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )
                            if (tab == MainTab.LIBRARY && libraryBadgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-3).dp)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(GoldBright),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$libraryBadgeCount",
                                        fontFamily = MonoFont,
                                        color = TextOnGold,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (selected) {
                                LedStatusDot(color = GoldBright, pulsing = false, size = 4.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = tab.label.uppercase(),
                                fontFamily = MonoFont,
                                color = tint,
                                fontSize = 9.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    anime: Anime,
    session: PlayerSession,
    onExpand: () -> Unit,
    onTogglePlay: () -> Unit,
    onNextEpisode: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (session.durationSeconds > 0) {
        (session.positionSeconds.toFloat() / session.durationSeconds).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianElevated)
            .border(1.2.dp, GoldBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onExpand)
            .padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(58.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
            ) {
                AnimePosterArtwork(
                    artTheme = anime.artTheme,
                    title = anime.title,
                    modifier = Modifier.fillMaxSize(),
                    showKanjiWatermark = false
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TapeReelSpinner(
                isSpinning = session.isPlaying,
                size = 22.dp,
                tint = GoldBright
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "EP ${session.episodeNumber} // ${formatTimeSeconds(session.positionSeconds)} - ${formatTimeSeconds(session.durationSeconds)}",
                    fontFamily = MonoFont,
                    fontSize = 10.5.sp,
                    color = GoldPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onTogglePlay, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = if (session.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Putar/Jeda",
                    tint = GoldBright
                )
            }
            IconButton(onClick = onNextEpisode, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Episode Berikutnya",
                    tint = TextPrimary
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Tutup Mini Player",
                    tint = TextTertiary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        AnalogMeterGauge(
            progress = progress,
            segments = 24,
            height = 5.dp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistCategorySheet(
    animeTitle: String,
    currentCategory: WatchlistCategory?,
    onSelectCategory: (WatchlistCategory?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianElevated,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "SIMPAN KE KOLEKSI",
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GoldBright,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = animeTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))

            WatchlistCategory.entries.forEach { category ->
                val selected = category == currentCategory
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) GoldTintStrong else ObsidianCard)
                        .border(
                            1.dp,
                            if (selected) GoldBright else GoldSubtleBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onSelectCategory(category)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) GoldBright else TextPrimary
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = GoldBright
                        )
                    }
                }
            }

            if (currentCategory != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusLive.copy(alpha = 0.14f))
                        .border(1.dp, StatusLive.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .clickable {
                            onSelectCategory(null)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = StatusLive,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HAPUS DARI DAFTAR TONTON",
                        fontFamily = MonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = StatusLive
                    )
                }
            }
        }
    }
}

@Composable
fun RatingDialog(
    animeTitle: String,
    initialScore: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var score by remember { mutableIntStateOf(if (initialScore > 0) initialScore else 9) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        titleContentColor = GoldBright,
        textContentColor = TextPrimary,
        title = {
            Text(
                text = "SKOR PENILAIAN",
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = animeTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "$score / 10",
                    style = TextStyle(
                        fontFamily = MonoFont,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        brush = GoldGradientBrush
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { s ->
                        val active = s * 2 <= score
                        IconButton(onClick = { score = s * 2 }) {
                            Icon(
                                imageVector = if (active) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                contentDescription = "$s",
                                tint = if (active) GoldBright else TextTertiary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    listOf(6, 7, 8, 9, 10).forEach { exact ->
                        val selected = score == exact
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) GoldGradientBrush else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard)))
                                .border(1.dp, if (selected) GoldBright else GoldSubtleBorder, RoundedCornerShape(6.dp))
                                .clickable { score = exact }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$exact",
                                fontFamily = MonoFont,
                                color = if (selected) TextOnGold else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            GoldGradientButton(
                text = "Simpan",
                onClick = {
                    onConfirm(score)
                    onDismiss()
                },
                height = 38.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("BATAL", fontFamily = MonoFont, color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    notifications: List<NotificationItem>,
    onNotificationClick: (NotificationItem) -> Unit,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianElevated,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = GoldBright
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOG NOTIFIKASI",
                        fontFamily = MonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }
                TextButton(onClick = onMarkAllRead) {
                    Text(
                        "TANDAI DIBACA",
                        fontFamily = MonoFont,
                        color = GoldPrimary,
                        fontSize = 11.sp
                    )
                }
            }

            HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(vertical = 8.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada notifikasi baru", color = TextTertiary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (item.isRead) ObsidianCard else GoldTint)
                                .border(
                                    1.dp,
                                    if (item.isRead) DividerSubtle else GoldBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onNotificationClick(item) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            LedStatusDot(
                                color = if (item.isRead) TextTertiary else GoldBright,
                                pulsing = !item.isRead,
                                size = 8.dp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GoldTagChip(text = item.tag, highlighted = !item.isRead)
                                    Text(
                                        text = item.timeAgo,
                                        fontFamily = MonoFont,
                                        fontSize = 10.sp,
                                        color = TextTertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
