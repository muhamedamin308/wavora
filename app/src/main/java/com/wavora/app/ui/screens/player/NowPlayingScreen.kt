package com.wavora.app.ui.screens.player

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wavora.app.core.utils.toDisplayDuration
import com.wavora.app.domain.model.PlayerState
import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Full-screen Now Playing screen.
 *
 * Layout (top → bottom):
 *  1. Navigation row (close + queue button)
 *  2. Album art (large, square with rounded corners)
 *  3. Song title + artist + favourite button
 *  4. Seek bar with current position / total duration
 *  5. Playback controls (shuffle, prev, play/pause, next, repeat)
 *  6. Secondary actions row (sleep timer, lyrics toggle)
 *
 * Phase 1: Full layout skeleton, correct state wiring to PlayerViewModel.
 * Phase 5: Album art loaded via Coil, background blur, waveform, animations.
 * Phase 7: Lyrics panel toggle, sleep timer sheet.
 */

@Composable
fun EnhancedNowPlayingScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onNavigateToQueue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState = state.playerState
    val snackbarHostState = remember { SnackbarHostState() }

    // Enhanced color animation with multiple layers
    val backgroundColor by animateColorAsState(
        targetValue = if (state.dominantColor != 0L)
            Color(state.dominantColor).copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.background,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bgColor",
    )

    val surfaceColor by animateColorAsState(
        targetValue = if (state.dominantColor != 0L)
            Color(state.dominantColor).copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "surfaceColor",
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PlayerEvent.NavigateUp -> onNavigateUp()
                is PlayerEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        backgroundColor,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.background
                    ),
                    center = Offset(0.5f, 0.3f),
                    radius = 1200f
                )
            )
    ) {
        // Animated blur effect behind content
        AnimatedBlurLayer(isPlaying = playerState.isPlaying)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Enhanced top bar with glass morphism
            EnhancedTopBar(
                onNavigateUp = onNavigateUp,
                onNavigateToQueue = onNavigateToQueue,
                surfaceColor = surfaceColor
            )

            Spacer(Modifier.height(24.dp))

            // Interactive album art with gestures
            InteractiveAlbumArtSection(
                song = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                onColorExtracted = viewModel::onDominantColorExtracted,
                onDoubleTap = viewModel::onToggleFavorites,
                onSwipeLeft = viewModel::onSkipToNext,
                onSwipeRight = viewModel::onSkipToPrevious,
            )

            Spacer(Modifier.height(32.dp))

            // Enhanced song info with marquee
            EnhancedSongInfoSection(
                playerState = playerState,
                viewModel = viewModel,
                surfaceColor = surfaceColor
            )

            Spacer(Modifier.height(24.dp))

            // Waveform-style seek bar
            WaveformSeekBarSection(
                playerState = playerState,
                onSeek = viewModel::onSeekTo,
                surfaceColor = surfaceColor
            )

            Spacer(Modifier.height(24.dp))

            // Enhanced controls with haptic feedback
            EnhancedMainControlsSection(
                playerState = playerState,
                viewModel = viewModel,
                surfaceColor = surfaceColor
            )

            Spacer(Modifier.height(16.dp))

            // Live audio visualizer
            LiveAudioVisualizer(
                isPlaying = playerState.isPlaying,
                surfaceColor = surfaceColor
            )

            Spacer(Modifier.height(24.dp))

            // Interactive lyrics panel
            InteractiveLyricsPanel(
                isVisible = state.isLyricsVisible,
                songTitle = playerState.currentSong?.title,
                artistName = playerState.currentSong?.artistName,
                lyrics = null,
                currentLineIndex = -1,
                positionMs = playerState.positionMs,
                surfaceColor = surfaceColor
            )

            Spacer(Modifier.height(16.dp))

            // Enhanced secondary controls
            EnhancedSecondaryControlsSection(
                viewModel = viewModel,
                playerState = playerState,
                surfaceColor = surfaceColor
            )

            Spacer(Modifier.height(32.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )
    }

    if (state.isSleepTimerSheetVisible) {
        EnhancedSleepTimerBottomSheet(
            onDismiss = viewModel::onDismissSleepTimerSheet,
            remainingMs = playerState.sleepTimerRemainingMs,
            onTimerSelected = viewModel::onSetSleepTimer,
            onCancelTimer = viewModel::onCancelSleepTimer,
            surfaceColor = surfaceColor
        )
    }
}

@Composable
private fun AnimatedBlurLayer(isPlaying: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.1f else 0f,
        animationSpec = tween(1000),
        label = "blurAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun EnhancedTopBar(
    onNavigateUp: () -> Unit,
    onNavigateToQueue: () -> Unit,
    surfaceColor: Color,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = surfaceColor.copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = onNavigateUp,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Rounded.KeyboardArrowDown, "Close")
            }

            Text(
                "Now Playing",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )

            FilledIconButton(
                onClick = onNavigateToQueue,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue")
            }
        }
    }
}

@Composable
private fun InteractiveAlbumArtSection(
    song: Song?,
    isPlaying: Boolean,
    onColorExtracted: (Long) -> Unit,
    onDoubleTap: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .graphicsLayer {
                scaleX = scale * animatedScale
                scaleY = scale * animatedScale
                rotationZ = animatedRotation
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onPress = {
                        scale = 0.95f
                        tryAwaitRelease()
                        scale = 1f
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        rotation = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        rotation = (dragAmount / 10f).coerceIn(-15f, 15f)
                        if (dragAmount > 50) {
                            onSwipeRight()
                        } else if (dragAmount < -50) {
                            onSwipeLeft()
                        }
                    }
                )
            }
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(1.05f)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isPlaying) 0.3f else 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main album art
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            shadowElevation = if (isPlaying) 16.dp else 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                song?.albumArtUri?.let {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song.albumArtUri)
                            .size(800)
                            .allowHardware(false)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album art for ${song.albumName}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onSuccess = { state ->
                            val bitmap = (state.result.drawable as? BitmapDrawable)?.bitmap
                            bitmap?.let {
                                val palette = Palette.from(it).generate()
                                val swatch = palette.dominantSwatch ?: palette.vibrantSwatch
                                swatch?.rgb?.toLong()?.let(onColorExtracted)
                            }
                        }
                    )
                } ?: Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Playing indicator overlay
                if (isPlaying) {
                    PlayingIndicatorOverlay()
                }
            }
        }
    }
}

@Composable
private fun PlayingIndicatorOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "playing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "playingAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}

@Composable
private fun EnhancedSongInfoSection(
    playerState: PlayerState,
    viewModel: PlayerViewModel,
    surfaceColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor.copy(alpha = 0.4f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Marquee effect for long titles
                BasicMarqueeText(
                    text = playerState.currentSong?.title ?: "Not playing",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = playerState.currentSong?.artistName ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = playerState.currentSong?.albumName ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            AnimatedFavoriteButton(
                isFavorite = playerState.currentSong?.isFavorite == true,
                onClick = viewModel::onToggleFavorites
            )
        }
    }
}

@Composable
private fun BasicMarqueeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    var shouldAnimate by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.then(
            if (shouldAnimate) {
                Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    delayMillis = 2000,
                    initialDelayMillis = 2000
                )
            } else Modifier
        ),
        onTextLayout = { result ->
            shouldAnimate = result.hasVisualOverflow
        }
    )
}

@Composable
private fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    var bounceScale by remember { mutableFloatStateOf(1f) }

    val scale by animateFloatAsState(
        targetValue = bounceScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favoriteScale"
    )

    FilledIconButton(
        onClick = {
            bounceScale = 1.3f
            onClick()
        },
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isFavorite)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    LaunchedEffect(bounceScale) {
        if (bounceScale != 1f) {
            delay(150)
            bounceScale = 1f
        }
    }
}

@Composable
private fun WaveformSeekBarSection(
    playerState: PlayerState,
    onSeek: (Float) -> Unit,
    surfaceColor: Color,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }

    val displayProgress = if (isDragging) dragPosition else playerState.progress

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Waveform visualization
            WaveformVisualizer(
                progress = displayProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(Modifier.height(8.dp))

            Slider(
                value = displayProgress,
                onValueChange = { fraction ->
                    isDragging = true
                    dragPosition = fraction
                },
                onValueChangeFinished = {
                    onSeek(dragPosition)
                    isDragging = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(if (isDragging) 20.dp else 16.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.background,
                                CircleShape
                            )
                    )
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = playerState.positionMs.toDisplayDuration(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                )
                Text(
                    text = playerState.durationMs.toDisplayDuration(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                )
            }
        }
    }
}

@Composable
private fun WaveformVisualizer(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val barCount = 50
    val random = remember { Random(42) }
    val heights = remember { List(barCount) { random.nextFloat() * 0.5f + 0.5f } }

    Canvas(modifier = modifier) {
        val barWidth = size.width / barCount
        val progressBarIndex = (progress * barCount).toInt()

        heights.forEachIndexed { index, height ->
            val barHeight = size.height * height
            val isActive = index <= progressBarIndex

            drawRoundRect(
                color = if (isActive) Color(0xFF6200EE) else Color(0xFFE0E0E0),
                topLeft = Offset(index * barWidth + barWidth * 0.2f, (size.height - barHeight) / 2),
                size = Size(barWidth * 0.6f, barHeight),
                cornerRadius = CornerRadius(barWidth * 0.3f)
            )
        }
    }
}

@Composable
private fun EnhancedMainControlsSection(
    playerState: PlayerState,
    viewModel: PlayerViewModel,
    surfaceColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor.copy(alpha = 0.5f),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedControlButton(
                icon = Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                isActive = playerState.isShuffleOn,
                onClick = viewModel::onShuffleToggle,
                size = 48.dp
            )

            AnimatedControlButton(
                icon = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                isActive = false,
                enabled = playerState.hasPrevious,
                onClick = viewModel::onSkipToPrevious,
                size = 56.dp
            )

            // Large play/pause button
            val playPauseScale by animateFloatAsState(
                targetValue = if (playerState.isPlaying) 1.05f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "playPauseScale"
            )

            FilledIconButton(
                onClick = viewModel::onPlayPauseToggle,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = playPauseScale
                        scaleY = playPauseScale
                    },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                AnimatedContent(
                    targetState = playerState.isPlaying,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "playPauseIcon"
                ) { isPlaying ->
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(44.dp),
                    )
                }
            }

            AnimatedControlButton(
                icon = Icons.Filled.SkipNext,
                contentDescription = "Next",
                isActive = false,
                enabled = playerState.hasNext,
                onClick = viewModel::onSkipToNext,
                size = 56.dp
            )

            AnimatedControlButton(
                icon = when (playerState.repeatMode) {
                    RepeatMode.NONE -> Icons.Filled.Repeat
                    RepeatMode.ALL -> Icons.Filled.Repeat
                    RepeatMode.ONE -> Icons.Filled.RepeatOne
                },
                contentDescription = "Repeat: ${playerState.repeatMode}",
                isActive = playerState.repeatMode != RepeatMode.NONE,
                onClick = viewModel::onRepeatModeToggle,
                size = 48.dp
            )
        }
    }
}

@Composable
private fun AnimatedControlButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    size: Dp,
    enabled: Boolean = true,
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    FilledIconButton(
        onClick = {
            pressed = true
            onClick()
        },
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (isActive) MaterialTheme.colorScheme.primary
            else if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(size * 0.5f)
        )
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(100)
            pressed = false
        }
    }
}

@Composable
private fun LiveAudioVisualizer(
    isPlaying: Boolean,
    surfaceColor: Color,
) {
    AnimatedVisibility(
        visible = isPlaying,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            color = surfaceColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(20) { index ->
                    AnimatedBar(index = index)
                }
            }
        }
    }
}

@Composable
private fun AnimatedBar(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "bar$index")
    val height by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (300..600).random(),
                easing = FastOutSlowInEasing,
                delayMillis = index * 50
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "barHeight$index"
    )

    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight(height)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                RoundedCornerShape(2.dp)
            )
    )
}

@Composable
private fun InteractiveLyricsPanel(
    isVisible: Boolean,
    songTitle: String?,
    artistName: String?,
    lyrics: String?,
    currentLineIndex: Int,
    positionMs: Long,
    surfaceColor: Color,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp),
            color = surfaceColor.copy(alpha = 0.6f),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Lyrics,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Lyrics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.size(24.dp))
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lyrics ?: "Lyrics will appear here...",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedSecondaryControlsSection(
    viewModel: PlayerViewModel,
    playerState: PlayerState,
    surfaceColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            EnhancedSecondaryButton(
                icon = Icons.Filled.Lyrics,
                text = "Lyrics",
                onClick = viewModel::onToggleLyrics
            )

            val timerActive = playerState.sleepTimerRemainingMs > 0
            EnhancedSecondaryButton(
                icon = Icons.Filled.Bedtime,
                text = if (timerActive) "Timer Active" else "Sleep",
                onClick = viewModel::onShowSleepTimerSheet,
                isActive = timerActive
            )

            EnhancedSecondaryButton(
                icon = Icons.Filled.Equalizer,
                text = "EQ",
                onClick = { /* Phase 7 */ }
            )
        }
    }
}

@Composable
private fun EnhancedSecondaryButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedSleepTimerBottomSheet(
    onDismiss: () -> Unit,
    remainingMs: Long,
    onTimerSelected: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    surfaceColor: Color,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                "Sleep Timer",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(24.dp))

            if (remainingMs > 0) {
                Surface(
                    color = surfaceColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Time Remaining",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            remainingMs.toDisplayDuration(),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = onCancelTimer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel Timer")
                }
            } else {
                val timerOptions = listOf(
                    "5 minutes" to 5 * 60 * 1000,
                    "15 minutes" to 15 * 60 * 1000,
                    "30 minutes" to 30 * 60 * 1000,
                    "1 hour" to 60 * 60 * 1000,
                )

                timerOptions.forEach { (label, duration) ->
                    FilledTonalButton(
                        onClick = { onTimerSelected(duration) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(label)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}