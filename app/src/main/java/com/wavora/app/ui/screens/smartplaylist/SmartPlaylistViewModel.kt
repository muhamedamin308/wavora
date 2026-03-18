package com.wavora.app.ui.screens.smartplaylist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.repository.interfaces.MusicRepository
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 18,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@HiltViewModel
class SmartPlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playerRepository: PlayerRepository,
) : BaseViewModel<SmartPlaylistUiState, Nothing>(SmartPlaylistUiState()) {

    private val type: SmartPlaylistType = SmartPlaylistType.valueOf(
        checkNotNull(savedStateHandle["smartPlaylistType"])
    )

    init {
        updateState { copy(type = type) }

        val flow = when (type) {
            SmartPlaylistType.RECENTLY_ADDED -> musicRepository.getRecentlyPlayedSongs()
            SmartPlaylistType.MOST_PLAYED -> musicRepository.getMostPlayedSongs()
            SmartPlaylistType.RECENTLY_PLAYED -> musicRepository.getRecentlyAddedSongs()
            SmartPlaylistType.FAVOURITES -> musicRepository.getFavouriteSongs()
        }

        flow
            .onEach { songs -> updateState { copy(songs = AsyncResult.Success(songs)) } }
            .catch { exception ->
                updateState {
                    copy(
                        songs = AsyncResult.Error(
                            exception.message ?: "Error"
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onPlayAll(shuffle: Boolean = false) = safeLaunch {
        val songs = (currentState.songs as? AsyncResult.Success)?.data?.takeIf { it.isNotEmpty() }
            ?: return@safeLaunch

        playerRepository.setShuffleEnabled(shuffle)
        playerRepository.playAll(songs)
    }

    fun onSongClicked(index: Int) = safeLaunch {
        val songs = (currentState.songs as? AsyncResult.Success)?.data ?: return@safeLaunch
        playerRepository.setShuffleEnabled(false)
        playerRepository.playAll(songs, startIndex = index)
    }
}