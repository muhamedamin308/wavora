package com.wavora.app.ui.screens.player

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
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
import com.wavora.app.ui.components.nowPlayingSemantics
import com.wavora.app.ui.components.repeatSemantics
import com.wavora.app.ui.components.seekBarSemantics
import com.wavora.app.ui.components.shuffleSemantics
import com.wavora.app.ui.theme.ShapeAlbumArt

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
    onNavigateToEqualizer: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState = state.playerState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PlayerEvent.NavigateUp -> onNavigateUp()
                is PlayerEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // Animate background color from album art dominant color
    val backgroundColor by animateColorAsState(
        targetValue = if (state.dominantColor != 0L)
            Color(state.dominantColor).copy(alpha = 0.6f)
        else
            MaterialTheme.colorScheme.background,
        animationSpec = tween(600),
        label = "bgColor",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .nowPlayingSemantics(playerState)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        MaterialTheme.colorScheme.background,
                    ),
                    endY = 900f,
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Top bar ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown, "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    "Now Playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(onClick = onNavigateToQueue) {
                    Icon(
                        Icons.Filled.QueueMusic, "Queue",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Album art ────────────────────────────────────────────────────
            AlbumArtSection(
                song = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                onColorExtracted = viewModel::onDominantColorExtracted,
            )

            Spacer(Modifier.height(32.dp))

            // ── Song info ────────────────────────────────────────────────────
            SongInfoSection(playerState = playerState, viewModel = viewModel)

            Spacer(Modifier.height(24.dp))

            // ── Seek bar ─────────────────────────────────────────────────────
            SeekBarSection(playerState = playerState, onSeek = viewModel::onSeekTo)

            Spacer(Modifier.height(16.dp))

            // ── Main controls ────────────────────────────────────────────────
            MainControlsSection(playerState = playerState, viewModel = viewModel)

            Spacer(Modifier.height(24.dp))

            // ── Lyrics panel ─────────────────────────────────────────────────────
            com.wavora.app.ui.components.LyricsPanel(
                isVisible = state.isLyricsVisible,
                songTitle = playerState.currentSong?.title,
                artistName = playerState.currentSong?.artistName,
                lyrics = null,  // Phase 9: load from LRC file
                currentLineIndex = -1,
                positionMs = playerState.positionMs,
            )

            Spacer(Modifier.height(8.dp))

            // ── Secondary controls ────────────────────────────────────────────
            SecondaryControlsSection(viewModel = viewModel, onNavigateToEqualizer)
        }

        // Snackbar pinned to bottom of the Box
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )
    }

    // Sleep timer bottom sheet — shown when isSleepTimerSheetVisible == true
    if (state.isSleepTimerSheetVisible) {
        com.wavora.app.ui.components.SleepTimerBottomSheet(
            onDismiss = viewModel::onDismissSleepTimerSheet,
            remainingMs = playerState.sleepTimerRemainingMs,
            onTimerSelected = viewModel::onSetSleepTimer,
            onCancelTimer = viewModel::onCancelSleepTimer,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Album art
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AlbumArtSection(
    song: Song?,
    isPlaying: Boolean,
    onColorExtracted: (Long) -> Unit,
) {
    val artSize = if (isPlaying) 280.dp else 240.dp
    val animatedSize by androidx.compose.animation.core.animateDpAsState(
        targetValue = artSize,
        animationSpec = tween(300),
        label = "artSize",
    )

    Box(
        modifier = Modifier
            .size(animatedSize)
            .clip(ShapeAlbumArt)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (song?.albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.albumArtUri)
                    .size(600)
                    .allowHardware(false) // needed for palette extraction
                    .crossfade(true)
                    .build(),
                contentDescription = "Album art for ${song.albumName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { state ->
                    // Extract dominant color for the background gradient
                    val bitmap =
                        (state.result.drawable as? BitmapDrawable)?.bitmap
                    bitmap?.let {
                        val palette = Palette.from(it).generate()
                        val swatch = palette.dominantSwatch ?: palette.vibrantSwatch
                        swatch?.rgb?.toLong()?.let(onColorExtracted)
                    }
                },
            )
        } else {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Song info + favourite
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SongInfoSection(playerState: PlayerState, viewModel: PlayerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playerState.currentSong?.title ?: "Not playing",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = playerState.currentSong?.artistName ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Favourite button
        val isFav = playerState.currentSong?.isFavorite == true
        IconButton(onClick = viewModel::onToggleFavorites) {
            Icon(
                if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isFav) "Remove from favourites" else "Add to favourites",
                tint = if (isFav) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Seek bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SeekBarSection(playerState: PlayerState, onSeek: (Float) -> Unit) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }

    val displayProgress = if (isDragging) dragPosition else playerState.progress

    Column(modifier = Modifier.fillMaxWidth()) {
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
            modifier = Modifier
                .fillMaxWidth()
                .seekBarSemantics(playerState.positionMs, playerState.durationMs),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = playerState.positionMs.toDisplayDuration(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = playerState.durationMs.toDisplayDuration(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Main controls: shuffle / prev / play-pause / next / repeat
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MainControlsSection(playerState: PlayerState, viewModel: PlayerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Shuffle
        IconButton(
            onClick = viewModel::onShuffleToggle,
            modifier = Modifier.shuffleSemantics(playerState.isShuffleOn),
        ) {
            Icon(
                Icons.Filled.Shuffle,
                contentDescription = null,  // handled by shuffleSemantics
                tint = if (playerState.isShuffleOn) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }

        // Previous
        IconButton(
            onClick = viewModel::onSkipToPrevious,
            enabled = playerState.hasPrevious,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                Icons.Filled.SkipPrevious, "Previous",
                Modifier.size(36.dp),
                tint = if (playerState.hasPrevious) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Play / Pause — large primary button
        FilledIconButton(
            onClick = viewModel::onPlayPauseToggle,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(
                if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                modifier = Modifier.size(40.dp),
            )
        }

        // Next
        IconButton(
            onClick = viewModel::onSkipToNext,
            enabled = playerState.hasNext,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                Icons.Filled.SkipNext, "Next",
                Modifier.size(36.dp),
                tint = if (playerState.hasNext) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Repeat
        IconButton(
            onClick = viewModel::onRepeatModeToggle,
            modifier = Modifier.repeatSemantics(playerState.repeatMode),
        ) {
            Icon(
                imageVector = when (playerState.repeatMode) {
                    RepeatMode.NONE -> Icons.Filled.Repeat
                    RepeatMode.ALL -> Icons.Filled.Repeat
                    RepeatMode.ONE -> Icons.Filled.RepeatOne
                },
                contentDescription = null,  // handled by repeatSemantics
                tint = if (playerState.repeatMode != RepeatMode.NONE)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Secondary controls: lyrics / timer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SecondaryControlsSection(
    viewModel: PlayerViewModel,
    onNavigateToEqualizer: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TextButton(onClick = viewModel::onToggleLyrics) {
            Icon(Icons.Filled.Lyrics, null, Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Lyrics", style = MaterialTheme.typography.labelMedium)
        }
        val timerActive =
            viewModel.uiState.collectAsStateWithLifecycle().value.playerState.sleepTimerRemainingMs > 0
        TextButton(onClick = viewModel::onShowSleepTimerSheet) {
            Icon(
                Icons.Filled.Bedtime, null, Modifier.size(18.dp),
                tint = if (timerActive) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Sleep Timer", style = MaterialTheme.typography.labelMedium,
                color = if (timerActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
            )
        }
        TextButton(onClick = { onNavigateToEqualizer() }) {
            Icon(Icons.Filled.Equalizer, null, Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("EQ", style = MaterialTheme.typography.labelMedium)
        }
    }
}
