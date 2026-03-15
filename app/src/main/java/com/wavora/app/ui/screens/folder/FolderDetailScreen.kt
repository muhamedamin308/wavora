package com.wavora.app.ui.screens.folder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.core.utils.pluralLabel
import com.wavora.app.core.utils.toDisplayDuration
import com.wavora.app.ui.components.EmptyState
import com.wavora.app.ui.components.LoadingScreen
import com.wavora.app.ui.components.WavoraTopBar
import com.wavora.app.ui.screens.library.SongListItem

/**
 * @author Muhamed Amin Hassan on 15,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun FolderDetailScreen(
    folderPath: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FolderDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val songs = (state.songs as? AsyncResult.Success)?.data ?: emptyList()

    // Leaf folder name for the title (e.g. "/storage/.../Rock" → "Rock")
    val folderName = folderPath.substringAfterLast('/')

    Scaffold(
        modifier = modifier,
        topBar = {
            WavoraTopBar(
                title = folderName,
                onNavigateUp = onNavigateUp,
                actions = {
                    if (songs.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onPlayAll(shuffle = true) }) {
                            Icon(Icons.Filled.Shuffle, "Shuffle folder")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val result = state.songs) {
            is AsyncResult.Loading -> LoadingScreen()
            is AsyncResult.Error -> EmptyState(
                title = "Couldn't load folder",
                subtitle = result.message,
            )

            is AsyncResult.Success -> {
                if (result.data.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.FolderOff,
                        title = "Empty folder",
                        subtitle = "No audio files found in $folderName.",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding() + 80.dp,
                        ),
                    ) {
                        // Folder stats header
                        item(key = "header") {
                            FolderHeader(
                                folderPath = folderPath,
                                songCount = songs.size,
                                totalMs = songs.sumOf { it.duration },
                                onPlayAll = { viewModel.onPlayAll(false) },
                            )
                        }

                        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                            SongListItem(
                                song = song,
                                onClick = { viewModel.onSongClicked(index) },
                            )
                            HorizontalDivider(Modifier.padding(start = 72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderHeader(
    folderPath: String,
    songCount: Int,
    totalMs: Long,
    onPlayAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Full path in muted type
        Text(
            text = folderPath,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${songCount.pluralLabel("song")} · ${totalMs.toDisplayDuration()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onPlayAll,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Play all")
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
    }
}