package com.wavora.app.ui.screens.queue

import androidx.lifecycle.viewModelScope
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 14,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class QueueUiState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
)

sealed interface QueueEvent {
    data object NavigateUp : QueueEvent
}

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
) : BaseViewModel<QueueUiState, QueueEvent>(QueueUiState()) {

    init {
        playerRepository.playerState
            .onEach { playerState ->
                updateState {
                    copy(
                        queue = playerState.queue,
                        currentIndex = playerState.currentQueueIndex,
                        isPlaying = playerState.isPlaying,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSongClicked(index: Int) = safeLaunch {
        // Skip to that index in the queue via seekTo — Phase 5 will add skipToQueueIndex
        val songs = currentState.queue
        if (index in songs.indices)
            playerRepository.playAll(songs)
    }

    fun onRemoveFromQueue(index: Int) = safeLaunch {
        playerRepository.removeFromQueue(index)
    }

    fun onClearQueue() = safeLaunch {
        playerRepository.clearQueue()
    }

    /**
     * Called when the user finishes a drag-reorder gesture.
     * [from] and [to] are the indices in [QueueUiState.queue].
     */
    fun onMoveItem(from: Int, to: Int) {
        if (from == to) return
        // Optimistic update — apply locally before the service confirms
        val reorder = currentState.queue.toMutableList().apply {
            add(to, removeAt(from))
        }
        updateState { copy(queue = reorder) }
        safeLaunch { playerRepository.moveQueueItem(from, to) }
    }


}