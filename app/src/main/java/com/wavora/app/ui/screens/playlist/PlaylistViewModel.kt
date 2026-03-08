package com.wavora.app.ui.screens.playlist

import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class PlaylistUiState(
    val playlist: AsyncResult<Playlist> = AsyncResult.Loading,
    val songs: AsyncResult<List<Song>> = AsyncResult.Loading,
)

sealed interface PlaylistEvent {
    data object NavigateUp : PlaylistEvent
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    // Phase 4: PlaylistRepository injected
) : BaseViewModel<PlaylistUiState, PlaylistEvent>(PlaylistUiState()) {

    fun loadPlaylist(playlistId: Long) {
        // Phase 4: safeLaunch { ... }
    }
}