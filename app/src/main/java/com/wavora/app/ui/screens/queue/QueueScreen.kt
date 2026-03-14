package com.wavora.app.ui.screens.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavora.app.core.utils.toDisplayDuration
import com.wavora.app.domain.model.Song
import com.wavora.app.ui.components.WavoraTopBar
import com.wavora.app.ui.theme.PlaybackAccent

/**
 * @author Muhamed Amin Hassan on 14,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun QueueScreen(
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (state.currentIndex - 1).coerceAtLeast(0),
    )

    // Scroll to current Song when it changes
    LaunchedEffect(state.currentIndex) {
        if (state.queue.isNotEmpty()) listState.animateScrollToItem(
            state.currentIndex.coerceAtMost(
                state.queue.lastIndex
            )
        )
    }

    Scaffold(
        modifier = modifier, topBar = {
            WavoraTopBar(
                title = "Queue (${state.queue.size}", onNavigateUp = onNavigateUp, actions = {
                    if (state.queue.isNotEmpty()) {
                        IconButton(onClick = viewModel::onClearQueue) {
                            Icon(Icons.Filled.ClearAll, contentDescription = "Clear queue")
                        }
                    }
                })
        }) { innerPadding ->
        if (state.queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        null,
                        Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Queue is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState, contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 100.dp
                )
            ) {
                itemsIndexed(
                    state.queue, key = { index, song -> "${index}_${song.id}" }) { index, song ->
                    val isCurrent = index == state.currentIndex
                    QueueItemComponent(
                        song = song,
                        isCurrent = isCurrent,
                        isPlaying = isCurrent && state.isPlaying,
                        position = index + 1,
                        onClick = { viewModel.onSongClicked(index) },
                        onRemove = { viewModel.onRemoveFromQueue(index) },
                    )
                    HorizontalDivider(
                        Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QueueItemComponent(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    position: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Position Number or playing indicator
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isPlaying)
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp, null,
                    Modifier.size(20.dp), tint = PlaybackAccent
                )
            else
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isCurrent) PlaybackAccent
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }

        // Song info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .alpha(if (isCurrent) 1f else 0.85f),
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isCurrent) PlaybackAccent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${song.artistName} · ${song.duration.toDisplayDuration()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                Icons.Filled.Close, "Remove from queue",
                Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Drag handle (visual only — full drag-reorder requires reorderable-compose in Phase 5)
        Icon(
            Icons.Filled.DragHandle, "Drag to reorder",
            Modifier
                .size(20.dp)
                .padding(end = 4.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}