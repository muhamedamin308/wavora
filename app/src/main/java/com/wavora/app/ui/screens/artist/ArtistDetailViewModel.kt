package com.wavora.app.ui.screens.artist

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
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playerRepository: PlayerRepository,
) : BaseViewModel<ArtistDetailUiState, Nothing>(ArtistDetailUiState()) {

    private val artistId: Long = checkNotNull(savedStateHandle["artistId"])

    init {
        combine(
            musicRepository.getArtistById(artistId),
            musicRepository.getAlbumsForArtist(artistId),
            musicRepository.getSongsForArtist(artistId),
        ) { artist, albums, songs ->
            updateState {
                copy(
                    artist = if (artist != null) AsyncResult.Success(artist)
                    else AsyncResult.Error("Artist not found"),
                    albums = AsyncResult.Success(albums),
                    songs = AsyncResult.Success(songs),
                )
            }
        }
            .catch { e -> updateState { copy(artist = AsyncResult.Error(e.message ?: "Error")) } }
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

    fun onAlbumSongs(albumId: Long) = safeLaunch {
        val songs = (currentState.songs as? AsyncResult.Success)?.data
            ?.filter { it.albumId == albumId } ?: return@safeLaunch
        playerRepository.playAll(songs, startIndex = 0)
    }
}