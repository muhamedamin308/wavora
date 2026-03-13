package com.wavora.app.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavora.app.domain.model.PlayerState
import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.ui.theme.PlaybackAccent
import com.wavora.app.ui.theme.ShapeAlbumArt
import com.wavora.app.ui.theme.ShapeCircle

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
fun NowPlayingScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onNavigateToQueue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState = state.playerState

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.background,
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top navigation row
            NowPlayingTopBar(
                onNavigateUp = onNavigateUp,
                onQueueClick = onNavigateToQueue,
            )

            Spacer(modifier.height(32.dp))

            // 2. Album Art
            AlbumArtSection()

            Spacer(modifier.height(32.dp))

            // 3. Song Info + Favorite
            SongInfoRow(
                playerState = playerState,
                onFavoriteClick = { /* Phase 2 */ }
            )

            Spacer(modifier.height(24.dp))

            // 4. Seek Bar
            SeekBarSection(
                playerState = playerState,
                onSeek = viewModel::onSeekToMs
            )

            Spacer(modifier.height(16.dp))

            // 5. Playback Control
            PlaybackControlsRow(
                playerState = playerState,
                onPlayPause = viewModel::onPlayPauseToggle,
                onSkipNext = viewModel::onSkipToNext,
                onSkipPrev = viewModel::onSkipToPrevious,
                onShuffle = viewModel::onShuffleToggle,
                onRepeat = viewModel::onRepeatModeToggle,
            )

            Spacer(modifier.height(24.dp))

            // 6. Secondary actions
            SecondaryActionsRow(
                isLyricsVisible = state.isLyricsVisible,
                onLyricsToggle = viewModel::onToggleLyrics,
                onSleepTimer = { /* Phase 7 */ }
            )
        }
    }
}

@Composable
fun SecondaryActionsRow(
    isLyricsVisible: Boolean,
    onLyricsToggle: () -> Unit,
    onSleepTimer: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Lyrics toggle (Phase 7)
        TextButton(
            onClick = onLyricsToggle
        ) {
            Icon(Icons.Filled.Lyrics, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (isLyricsVisible) "Hide Lyrics" else "Lyrics",
                style = MaterialTheme.typography.labelMedium,
            )
        }

        // Sleep timer (Phase 7)
        TextButton(onClick = onSleepTimer) {
            Icon(Icons.Filled.Bedtime, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Timer", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun PlaybackControlsRow(
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OnShuffleIconButton(playerState, onShuffle)
        OnSkipPreviousIconButton(playerState, onSkipPrev)
        OnPlayPauseIconButton(playerState, onPlayPause)
        OnSkipNextIconButton(playerState, onSkipNext)
        OnRepeatIconButton(playerState, onRepeat)
    }
}

@Composable
fun SeekBarSection(playerState: PlayerState, onSeek: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = playerState.progress,
            onValueChange = { fraction ->
                onSeek((fraction * playerState.durationMs).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = PlaybackAccent,
                activeTrackColor = PlaybackAccent
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatMs(playerState.positionMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatMs(playerState.durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SongInfoRow(playerState: PlayerState, onFavoriteClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playerState.currentSong?.title ?: "No Song playing!",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = playerState.currentSong?.artistName ?: "—",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (playerState.currentSong?.isFavorite == true)
                    Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favourite",
                tint = if (playerState.currentSong?.isFavorite == true)
                    PlaybackAccent else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    }
}

@Composable
fun AlbumArtSection() {
    // Phase 5: Replace with Coil AsyncImage + blur background effect
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(ShapeAlbumArt)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = "Album art",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
        )
    }
}

// Sub-Composables
@Composable
private fun NowPlayingTopBar(
    onNavigateUp: () -> Unit,
    onQueueClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onNavigateUp
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Close player"
            )
            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = onQueueClick) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Open queue")
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun OnShuffleIconButton(
    playerState: PlayerState,
    onShuffle: () -> Unit,
) {
    IconButton(onClick = onShuffle) {
        Icon(
            imageVector = Icons.Filled.Shuffle,
            contentDescription = "Shuffle",
            tint = if (playerState.isShuffleOn) PlaybackAccent
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OnSkipPreviousIconButton(
    playerState: PlayerState,
    onSkipPrev: () -> Unit,
) {
    IconButton(
        onClick = onSkipPrev,
        modifier = Modifier.size(48.dp),
        enabled = playerState.hasPrevious
    ) {
        Icon(
            imageVector = Icons.Filled.SkipPrevious,
            contentDescription = "Previous",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun OnSkipNextIconButton(
    playerState: PlayerState,
    onSkipNext: () -> Unit,
) {
    IconButton(
        onClick = onSkipNext,
        modifier = Modifier.size(48.dp),
        enabled = playerState.hasNext,
    ) {
        Icon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = "Next",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun OnRepeatIconButton(
    playerState: PlayerState,
    onRepeat: () -> Unit,
) {
    IconButton(onClick = onRepeat) {
        Icon(
            imageVector = when (playerState.repeatMode) {
                RepeatMode.ONE -> Icons.Filled.RepeatOne
                else -> Icons.Filled.Repeat
            },
            contentDescription = "Repeat",
            tint = if (playerState.repeatMode != RepeatMode.NONE) PlaybackAccent
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OnPlayPauseIconButton(
    playerState: PlayerState,
    onPlayPause: () -> Unit,
) {
    FilledIconButton(
        onClick = onPlayPause,
        modifier = Modifier.size(64.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = PlaybackAccent,
            contentColor = Color.White
        ),
        shape = ShapeCircle
    ) {
        Icon(
            imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
            modifier = Modifier.size(32.dp),
        )
    }
}