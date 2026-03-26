package com.wavora.app.ui.screens.smartplaylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.core.utils.pluralLabel
import com.wavora.app.domain.model.Song
import com.wavora.app.ui.components.EmptyState
import com.wavora.app.ui.components.LoadingScreen
import com.wavora.app.ui.components.WavoraTopBar
import com.wavora.app.ui.screens.library.EnhancedSongListItem

/**
 * @author Muhamed Amin Hassan on 18,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun SmartPlaylistScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SmartPlaylistViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val songs = (state.songs as? AsyncResult.Success)?.data ?: emptyList()

    Scaffold(
        modifier = modifier,
        topBar = {
            WavoraTopBar(
                title = state.type.displayName,
                onNavigateUp = onNavigateUp
            )
        },
    ) { innerPadding ->
        when (state.songs) {
            is AsyncResult.Error -> EmptyState(title = "Couldn't load songs")
            AsyncResult.Loading -> LoadingScreen()
            is AsyncResult.Success -> LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                )
            ) {
                // Header
                item(key = "header") {
                    SmartPlaylistHeader(
                        type = state.type,
                        songCount = songs.size,
                        onPlayAll = { viewModel.onPlayAll(false) },
                        onShuffle = { viewModel.onPlayAll(true) },
                    )
                }

                if (songs.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            modifier = Modifier.fillParentMaxHeight(0.5f),
                            icon = state.type.icon,
                            title = "No songs yet",
                            subtitle = state.type.subtitle,
                        )
                    }
                } else {
                    itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                        // For Most Played, show play count badge instead of album
                        val trailingLabel = if (state.type == SmartPlaylistType.MOST_PLAYED)
                            "${song.playCount}×" else null

                        SmartPlaylistSongRow(
                            song = song,
                            position = index + 1,
                            trailingLabel = trailingLabel,
                            onClick = { viewModel.onSongClicked(index) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SmartPlaylistHeader(
    type: SmartPlaylistType,
    songCount: Int,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                type.icon,
                null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(type.displayName, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = if (songCount > 0) songCount.pluralLabel("song") else type.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPlayAll,
                enabled = songCount > 0,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Play all")
            }
            OutlinedButton(
                onClick = onShuffle,
                enabled = songCount > 0,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.Shuffle, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Shuffle")
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
    }
}


@Composable
fun SmartPlaylistSongRow(
    song: Song,
    position: Int,
    trailingLabel: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$position",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(36.dp)
                .padding(start = 16.dp),
        )
        EnhancedSongListItem(
            song = song,
            onClick = onClick,
            modifier = Modifier.weight(1f),
        )

        trailingLabel?.let {
            Text(
                text = trailingLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp),
            )
        }
    }
}