package com.wavora.app.ui.screens.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.repository.interfaces.MusicRepository
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 15,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playerRepository: PlayerRepository,
) : BaseViewModel<AlbumDetailUiState, Nothing>(AlbumDetailUiState()) {
    private val albumId: Long = checkNotNull(savedStateHandle["albumId"])

    init {
        combine(
            musicRepository.getAlbumById(albumId),
            musicRepository.getSongsForAlbum(albumId),
        ) { album, songs ->
            updateState {
                copy(
                    album = if (album != null) AsyncResult.Success(album)
                    else AsyncResult.Error("Album not found"),
                    songs = AsyncResult.Success(songs),
                )
            }
        }
            .catch { e -> updateState { copy(album = AsyncResult.Error(e.message ?: "Error")) } }
            .launchIn(viewModelScope)
    }

    fun onPlayAll(shuffle: Boolean = false) = safeLaunch {
        val songs = (currentState.songs as? AsyncResult.Success)?.data ?: return@safeLaunch
        if (songs.isEmpty()) return@safeLaunch

        playerRepository.setShuffleEnabled(shuffle)
        playerRepository.playAll(songs, startIndex = 0)
    }

    fun onSongClicked(index: Int) = safeLaunch {
        val songs = (currentState.songs as? AsyncResult.Success)?.data ?: return@safeLaunch
        playerRepository.setShuffleEnabled(false)
        playerRepository.playAll(songs, startIndex = index)
    }
}