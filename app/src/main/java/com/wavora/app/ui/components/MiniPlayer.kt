package com.wavora.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavora.app.domain.model.PlayerState
import com.wavora.app.ui.screens.player.PlayerViewModel
import com.wavora.app.ui.theme.ShapeMiniPlayer

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
/**
 * Persistent mini player bar, visible across all library screens.
 *
 * Design decisions:
 *  - Floats above the bottom nav with a card-like elevated surface.
 *  - Slides in/out vertically when a song starts/stops.
 *  - Shows: album art thumbnail, song title, artist name, play/pause, skip next.
 *  - Tapping anywhere (except the buttons) opens the full [NowPlayingScreen].
 *
 * The full implementation (album art, waveform animation, seek bar) is done
 * in Phase 5. This Phase 1 skeleton establishes the correct state-connection
 * pattern so the nav scaffold is properly wired from day one.
 */

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    onExpand: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = state.playerState.currentSong != null,
        modifier = modifier,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        MiniPlayerContent(
            playerState = state.playerState,
            onExpand = onExpand,
            onPlayPause = viewModel::onPlayPauseToggle,
            onSkipNext = viewModel::onSkipToNext
        )
    }
}

@Composable
private fun MiniPlayerContent(
    playerState: PlayerState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
) {
    val song = playerState.currentSong ?: return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(ShapeMiniPlayer)
            .clickable(onClick = onExpand),
        shape = ShapeMiniPlayer,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        // Progress indicator at the very top edge of the mini player
        Box(modifier = Modifier.fillMaxWidth()) {

            LinearProgressIndicator(
                progress = { playerState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Album art ────────────────────────────────────────────────────
            // Phase 5: replace with Coil AsyncImage loading song.albumArtUri
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            // ── Song info ────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ── Controls ─────────────────────────────────────────────────────
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(28.dp),
                )
            }

            IconButton(
                onClick = onSkipNext,
                enabled = playerState.hasNext,
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Skip next",
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}
