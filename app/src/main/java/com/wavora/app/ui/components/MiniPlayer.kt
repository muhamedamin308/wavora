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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wavora.app.ui.screens.player.PlayerViewModel
import com.wavora.app.ui.theme.ShapeMiniPlayer

/**
 * Persistent bottom player strip that appears whenever a song is loaded.
 *
 * Shares [PlayerViewModel] with [NowPlayingScreen] via the Hilt NavGraph scope —
 * no redundant service connections.
 *
 * Layout:
 * ┌────────────────────────────────────────────────────────────────────────┐
 * │  [AlbumArt]  Song title         Artist name  [Prev] [Play/Pause] [Next]│
 * │  ─────────────────── progress ──────────────────────────────────────── │
 * └────────────────────────────────────────────────────────────────────────┘
 */


@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    onExpand: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState = state.playerState
    val song = playerState.currentSong

    AnimatedVisibility(
        visible = song != null,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            shape = ShapeMiniPlayer,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                // Progress bar across full width - 2dp thin
                LinearProgressIndicator(
                    progress = { playerState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onExpand)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // ── Album art ────────────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        song?.albumArtUri?.let {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(song.albumArtUri)
                                    .size(88)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: Icon(
                            Icons.Filled.MusicNote,
                            null,
                            Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    // ── Song info ────────────────────────────────────────────────────
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song?.title ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = song?.artistName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Controls
                    IconButton(
                        onClick = viewModel::onSkipToPrevious,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            Icons.Filled.SkipPrevious, "Previous",
                            Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = viewModel::onPlayPauseToggle,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            if (playerState.isPlaying) Icons.Filled.Pause
                            else Icons.Filled.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    IconButton(
                        onClick = viewModel::onSkipToNext,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            Icons.Filled.SkipNext, "Next",
                            Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}