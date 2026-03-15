package com.wavora.app.ui.screens.playlist

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
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.core.utils.pluralLabel
import com.wavora.app.core.utils.toDisplayDuration
import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.Song
import com.wavora.app.ui.components.ConfirmDeleteDialog
import com.wavora.app.ui.components.EmptyState
import com.wavora.app.ui.components.LoadingScreen
import com.wavora.app.ui.components.RenamePlaylistDialog
import com.wavora.app.ui.components.WavoraTopBar
import com.wavora.app.ui.screens.library.SongListItem

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun PlaylistScreen(
    playlistId: Long,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PlaylistEvent.NavigateUp -> onNavigateUp()
                is PlaylistEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val playlist = (state.playlist as? AsyncResult.Success)?.data
    val songs = (state.songs as? AsyncResult.Success)?.data ?: emptyList()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PlaylistTopBar(
                playlistName = playlist?.name ?: "Playlist",
                onNavigateUp = onNavigateUp,
                onRename = viewModel::onRenameClicked,
                onDelete = viewModel::onDeleteClicked,
            )
        },
    ) { innerPadding ->
        when (state.playlist) {
            is AsyncResult.Loading -> {
                LoadingScreen()
            }

            is AsyncResult.Error -> {
                EmptyState(
                    title = "Playlist not found",
                    subtitle = "It may have been deleted."
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding() + 100.dp,
                    ),
                ) {
                    // playlist header
                    item(key = "header") {
                        PlaylistHeader(
                            playlist = playlist,
                            songCount = songs.size,
                            onPlayAll = viewModel::onPlayAll
                        )
                    }

                    // songs list
                    if (songs.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                modifier = Modifier.fillParentMaxHeight(0.5f),
                                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                                title = "No songs",
                                subtitle = "Add songs from your library.",
                            )
                        }
                    } else {
                        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                            PlaylistSongItem(
                                song = song,
                                onClick = { viewModel.onSongClicked(index) },
                                onRemove = { viewModel.onRemoveSong(song.id) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (state.showRenameDialog && playlist != null) {
        RenamePlaylistDialog(
            currentName = playlist.name,
            onConfirm = viewModel::onRenameConfirmed,
            onDismiss = viewModel::onRenameDismissed
        )
    }

    if (state.showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete playlist",
            message = "\"${playlist?.name}\" will be permanently deleted.",
            onConfirm = viewModel::onDeleteConfirmed,
            onDismiss = viewModel::onDeleteDismissed,
        )
    }
}

@Composable
fun PlaylistTopBar(
    playlistName: String,
    onNavigateUp: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded: Boolean by remember { mutableStateOf(false) }

    WavoraTopBar(
        title = playlistName,
        onNavigateUp = onNavigateUp,
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, "More options")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = {
                    menuExpanded = false
                }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = { menuExpanded = false; onRename() },
                    leadingIcon = { Icon(Icons.Filled.Edit, null) },
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = { menuExpanded = false; onDelete() },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Delete, null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                )
            }
        }
    )
}

@Composable
private fun PlaylistHeader(
    playlist: Playlist?,
    songCount: Int,
    onPlayAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = playlist?.name ?: "",
            style = MaterialTheme.typography.headlineSmall,
        )
        if (songCount > 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${songCount.pluralLabel("song")} · ${(playlist?.totalDuration ?: 0L).toDisplayDuration()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onPlayAll,
            enabled = songCount > 0,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Play all")
        }
    }
    HorizontalDivider()
}

@Composable
private fun PlaylistSongItem(
    song: Song,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        SongListItem(
            song = song,
            onClick = onClick,
            modifier = Modifier.weight(1f),
        )
        // Drag handle + remove — visible without long-press for clarity
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(36.dp)
                .padding(end = 4.dp),
        ) {
            Icon(
                Icons.Filled.RemoveCircleOutline, "Remove",
                Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
