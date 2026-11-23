package com.gows.animwatch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.GoldTintStrong
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.MediaScrimDark
import com.gows.animwatch.ui.theme.MediaTextLight
import com.gows.animwatch.ui.theme.MediaTextSecondary
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianGlass
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
    height: Dp = 46.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(GoldGradientBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp),
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
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
            }
            Text(
                text = text,
                color = TextOnGold,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
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
    height: Dp = 46.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "out_btn_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isActive) GoldTintStrong else ObsidianCard,
        animationSpec = tween(220),
        label = "out_btn_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) GoldPrimary else GoldSubtleBorder,
        animationSpec = tween(220),
        label = "out_btn_border"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
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
                    tint = if (isActive) GoldBright else GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
            }
            Text(
                text = text,
                color = if (isActive) GoldBright else TextPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
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
    actionLabel: String? = "Lihat semua",
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
                    .height(if (subtitle != null) 32.dp else 20.dp)
                    .clip(CircleShape)
                    .background(GoldGradientBrush)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
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
                    .clip(CircleShape)
                    .background(GoldTint)
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
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
            .clip(CircleShape)
            .background(if (highlighted) GoldGradientBrush else Brush.linearGradient(listOf(GoldTint, GoldTint)))
            .padding(horizontal = 9.dp, vertical = 3.5.dp)
    ) {
        Text(
            text = text,
            color = if (highlighted) TextOnGold else GoldPrimary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
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
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.71f)
                .clip(RoundedCornerShape(18.dp))
                .background(ObsidianCard)
                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
        ) {
            AnimePosterArtwork(
                artTheme = anime.artTheme,
                title = anime.title,
                posterUrl = anime.posterUrl,
                decodeSizePx = 360,
                modifier = Modifier.fillMaxSize()
            )

            // Top row: Anime streaming site SUB/Muse pill + rating & bookmark
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (anime.isOfficialFree) GoldGradientBrush else Brush.linearGradient(listOf(MediaScrimDark.copy(alpha = 0.78f), MediaScrimDark.copy(alpha = 0.78f))))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = if (anime.isOfficialFree) "SUB INDO" else "ANILIST",
                            color = if (anime.isOfficialFree) TextOnGold else MediaTextLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MediaScrimDark.copy(alpha = 0.76f))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.5.dp))
                            Text(
                                text = anime.rating.toString(),
                                color = MediaTextLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MediaScrimDark.copy(alpha = 0.70f))
                        .clickable(onClick = onBookmarkClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                        contentDescription = "Simpan ke Daftar Tonton",
                        tint = if (isBookmarked) GoldBright else MediaTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Bottom streaming episode bar over smooth vignette
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (anime.status == AnimeStatus.ONGOING) {
                        LedStatusDot(color = StatusLive, pulsing = false, size = 5.5.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = when {
                            anime.type.label == "Movie" -> "Movie"
                            anime.status == AnimeStatus.COMPLETED -> "${anime.totalEpisodes} Ep"
                            else -> "Ep ${anime.releasedEpisodes}/${anime.totalEpisodes}"
                        },
                        color = MediaTextLight,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (anime.isOfficialFree) "Muse ID" else "${anime.year}",
                    color = if (anime.isOfficialFree) GoldBright else MediaTextSecondary,
                    fontSize = 9.5.sp,
                    fontWeight = if (anime.isOfficialFree) FontWeight.Bold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = anime.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = anime.genres.take(2).joinToString(" • "),
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
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
            .width(248.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ObsidianCard)
            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onPlayClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            AnimePosterArtwork(
                artTheme = anime.artTheme,
                title = anime.title,
                posterUrl = anime.posterUrl,
                modifier = Modifier.fillMaxSize(),
                showKanjiWatermark = false,
                decodeSizePx = 360
            )

                // Soft circular play button
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MediaScrimDark.copy(alpha = 0.72f))
                        .border(1.dp, GoldBright.copy(alpha = 0.75f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Lanjutkan",
                        tint = MediaTextLight,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Soft circular close button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MediaScrimDark.copy(alpha = 0.68f))
                        .clickable(onClick = onRemoveClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Hapus",
                        tint = MediaTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Remaining time pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .background(MediaScrimDark.copy(alpha = 0.76f))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Ep ${progress.episodeNumber} • Sisa ${progress.remainingMinutes} mnt",
                        color = MediaTextLight,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDetailClick)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                AnalogMeterGauge(
                    progress = progress.progressFraction.coerceAtLeast(0.06f),
                    height = 5.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
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
                .padding(start = 16.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObsidianCard)
                    .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
            ) {
                AnimePosterArtwork(
                    artTheme = anime.artTheme,
                    title = anime.title,
                    posterUrl = anime.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                    decodeSizePx = 360
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(MediaScrimDark.copy(alpha = 0.74f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
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
                            text = "${anime.rating}",
                            color = MediaTextLight,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${anime.viewsLabel} tayangan",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextTertiary
            )
        }

        // Smooth circular floating rank badge
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 2.dp, y = (-42).dp)
                .size(38.dp),
            shape = CircleShape,
            color = ObsidianElevated,
            shadowElevation = 5.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.5.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = GoldGradientBrush
                    )
                )
            }
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
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianCard)
            .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rankNumber != null) {
            Text(
                text = "#$rankNumber",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush = if (rankNumber <= 3) GoldGradientBrush else Brush.linearGradient(
                        listOf(TextSecondary, TextTertiary)
                    )
                ),
                modifier = Modifier.width(34.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(68.dp)
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AnimePosterArtwork(
                artTheme = anime.artTheme,
                title = anime.title,
                posterUrl = anime.posterUrl,
                modifier = Modifier.fillMaxSize(),
                showKanjiWatermark = false,
                decodeSizePx = 240
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
                            tint = GoldPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${anime.rating}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "• ${anime.year}",
                        color = TextTertiary,
                        fontSize = 11.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${anime.genres.take(2).joinToString(" • ")} • ${anime.releasedEpisodes}/${anime.totalEpisodes} Ep",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldGradientBrush)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Putar",
                        tint = TextOnGold,
                        modifier = Modifier.size(21.dp)
                    )
                }
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
                        contentDescription = "Simpan",
                        tint = if (isBookmarked) GoldPrimary else TextTertiary,
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        color = ObsidianGlass,
        shadowElevation = if (IsDarkTheme) 12.dp else 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp),
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
                    targetValue = if (selected) GoldPrimary else TextTertiary,
                    animationSpec = tween(220),
                    label = "nav_tint"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (selected) GoldTintStrong else Color.Transparent,
                    animationSpec = tween(220),
                    label = "nav_bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectTab(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(22.dp)
                            )
                            if (tab == MainTab.LIBRARY && libraryBadgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-3).dp)
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$libraryBadgeCount",
                                        color = TextOnGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            color = tint,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onExpand),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianElevated,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(20.dp))
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    AnimePosterArtwork(
                        artTheme = anime.artTheme,
                        title = anime.title,
                        posterUrl = anime.posterUrl,
                        modifier = Modifier.fillMaxSize(),
                        showKanjiWatermark = false
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                TapeReelSpinner(
                    isSpinning = session.isPlaying,
                    size = 18.dp,
                    tint = GoldPrimary
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
                        text = "Ep ${session.episodeNumber} • ${formatTimeSeconds(session.positionSeconds)} / ${formatTimeSeconds(session.durationSeconds)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.5.sp,
                        color = GoldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onTogglePlay, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = if (session.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Putar/Jeda",
                        tint = GoldPrimary
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
                height = 3.5.dp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Simpan ke Koleksi",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
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
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) GoldTintStrong else ObsidianCard)
                        .border(
                            1.dp,
                            if (selected) GoldPrimary else GoldSubtleBorder,
                            RoundedCornerShape(16.dp)
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
                        color = if (selected) GoldPrimary else TextPrimary
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = GoldPrimary
                        )
                    }
                }
            }

            if (currentCategory != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(StatusLive.copy(alpha = 0.12f))
                        .clickable {
                            onSelectCategory(null)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                        text = "Hapus dari Daftar Tonton",
                        style = MaterialTheme.typography.labelLarge,
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
        shape = RoundedCornerShape(24.dp),
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(
                text = "Beri Penilaian",
                style = MaterialTheme.typography.headlineSmall
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
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
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
                                tint = if (active) GoldPrimary else TextTertiary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    listOf(6, 7, 8, 9, 10).forEach { exact ->
                        val selected = score == exact
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) GoldGradientBrush else Brush.linearGradient(listOf(ObsidianCard, ObsidianCard)))
                                .border(0.75.dp, if (selected) Color.Transparent else GoldSubtleBorder, CircleShape)
                                .clickable { score = exact }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$exact",
                                color = if (selected) TextOnGold else TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
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
                height = 40.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                        tint = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifikasi",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                }
                TextButton(onClick = onMarkAllRead) {
                    Text("Tandai dibaca", color = GoldPrimary, fontSize = 12.5.sp)
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
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (item.isRead) ObsidianCard else GoldTint)
                                .border(
                                    0.75.dp,
                                    if (item.isRead) GoldSubtleBorder else GoldBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onNotificationClick(item) }
                                .padding(13.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            LedStatusDot(
                                color = if (item.isRead) TextTertiary else GoldPrimary,
                                pulsing = !item.isRead,
                                size = 8.dp,
                                modifier = Modifier.padding(top = 5.dp)
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
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
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
