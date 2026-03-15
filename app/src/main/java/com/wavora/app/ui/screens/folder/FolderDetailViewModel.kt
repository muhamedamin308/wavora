package com.wavora.app.ui.screens.folder

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
 * @author Muhamed Amin Hassan on 15,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playerRepository: PlayerRepository,
) : BaseViewModel<FolderDetailUiState, Nothing>(FolderDetailUiState()) {
    // Nav arg is URL-decoded in the composable and passed via savedStateHandle
    private val folderPath: String = checkNotNull(savedStateHandle["folderPath"])

    init {
        updateState { copy(folderPath = folderPath) }

        musicRepository.getSongsForFolder(folderPath)
            .onEach { songs -> updateState { copy(songs = AsyncResult.Success(songs)) } }
            .catch { e -> updateState { copy(songs = AsyncResult.Error(e.message ?: "Error")) } }
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