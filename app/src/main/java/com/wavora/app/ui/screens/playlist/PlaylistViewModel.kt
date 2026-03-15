package com.wavora.app.ui.screens.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import com.wavora.app.domain.repository.interfaces.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class PlaylistUiState(
    val playlist: AsyncResult<Playlist> = AsyncResult.Loading,
    val songs: AsyncResult<List<Song>> = AsyncResult.Loading,
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
)

sealed interface PlaylistEvent {
    data object NavigateUp : PlaylistEvent
    data class ShowSnackbar(val message: String) : PlaylistEvent
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val playerRepository: PlayerRepository,
) : BaseViewModel<PlaylistUiState, PlaylistEvent>(PlaylistUiState()) {

    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    init {
        combine(
            playlistRepository.getPlaylistById(playlistId),
            playlistRepository.getSongsInPlaylist(playlistId),
        ) { playlist, songs ->
            updateState {
                copy(
                    playlist = if (playlist != null) AsyncResult.Success(playlist)
                    else AsyncResult.Error("Playlist not found"),
                    songs = AsyncResult.Success(songs)
                )
            }
        }
            .catch { exception ->
                updateState {
                    copy(
                        playlist = AsyncResult.Error(
                            exception.message ?: "Error"
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // playback
    fun onPlayAll() = safeLaunch {
        val songs = (currentState.songs as? AsyncResult.Success)?.data ?: return@safeLaunch
        if (songs.isNotEmpty()) playerRepository.playAll(songs, startIndex = 0)
    }

    fun onSongClicked(index: Int) = safeLaunch {
        val songs = (currentState.songs as? AsyncResult.Success)?.data ?: return@safeLaunch
        playerRepository.playAll(songs, startIndex = index)
    }

    // song management
    fun onRemoveSong(songId: Long) = safeLaunch {
        playlistRepository.removeSongFromPlaylist(playlistId, songId)
        emitEvent(PlaylistEvent.ShowSnackbar("Remove from playlist"))
    }

    fun onMoveItem(from: Int, to: Int) = safeLaunch {
        playlistRepository.reorderSongs(playlistId, from, to)
    }

    // Playlist CRUD
    fun onRenameClicked() = updateState { copy(showRenameDialog = true) }
    fun onRenameConfirmed(newName: String) {
        updateState { copy(showRenameDialog = false) }
        safeLaunch { playlistRepository.renamePlaylist(playlistId, newName) }
    }

    fun onRenameDismissed() = updateState { copy(showRenameDialog = false) }

    fun onDeleteClicked() = updateState { copy(showDeleteDialog = true) }
    fun onDeleteConfirmed() {
        updateState { copy(showDeleteDialog = false) }
        safeLaunch {
            playlistRepository.deletePlaylist(playlistId)
            emitEvent(PlaylistEvent.NavigateUp)
        }
    }

    fun onDeleteDismissed() = updateState { copy(showDeleteDialog = false) }
}