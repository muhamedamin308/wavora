package com.wavora.app.ui.screens.player

import androidx.lifecycle.viewModelScope
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Shared ViewModel for both [NowPlayingScreen] and [MiniPlayer].
 *
 * Marked @HiltViewModel — shared across the composition tree so MiniPlayer
 * and NowPlayingScreen always see the same [PlayerState] without duplication.
 *
 * Phase 1: State skeleton.
 * Phase 3: [PlayerRepository] injected; all actions route to ExoPlayer service.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
) : BaseViewModel<PlayerUiState, PlayerEvent>(PlayerUiState()) {

    init {
        playerRepository.playerState
            .onEach { playerState -> updateState { copy(playerState = playerState) } }
            .launchIn(viewModelScope)
    }

    // ── Playback actions (routed to PlayerRepository in Phase 3) ──────────────
    fun onPlaySong(song: Song) = safeLaunch {
        playerRepository.play(song)
    }

    fun onPlayAll(songs: List<Song>, startIndex: Int = 0) = safeLaunch {
        playerRepository.playAll(songs, startIndex)
    }

    fun onPlayPauseToggle() = safeLaunch {
        val state = currentState.playerState
        when {
            state.currentSong == null -> return@safeLaunch
            state.isPlaying -> playerRepository.pause()
            else -> playerRepository.resume()
        }
    }

    fun onSkipToNext() = safeLaunch { playerRepository.skipToNext() }
    fun onSkipToPrevious() = safeLaunch { playerRepository.skipToPrevious() }

    fun onSeekTo(fraction: Float) = safeLaunch {
        val duration = currentState.playerState.durationMs
        if (duration > 0) playerRepository.seekTo((fraction * duration).toLong())
    }

    fun onSeekToMs(positionMs: Long) = safeLaunch {
        playerRepository.seekTo(positionMs)
    }

    fun onShuffleToggle() = safeLaunch {
        playerRepository.setShuffleEnabled(!currentState.playerState.isShuffleOn)
    }

    fun onRepeatModeToggle() = safeLaunch {
        val mode = when (currentState.playerState.repeatMode) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
        playerRepository.setRepeatMode(mode)
    }

    // Queue
    fun onAddToQueue(song: Song) = safeLaunch {
        playerRepository.addToQueue(song)
    }

    fun onRemoveFromQueue(index: Int) = safeLaunch {
        playerRepository.removeFromQueue(index)
    }

    fun onMoveQueueItem(from: Int, to: Int) = safeLaunch {
        playerRepository.moveQueueItem(from, to)
    }

    fun onClearQueue() = safeLaunch { playerRepository.clearQueue() }

    // UI-only states
    fun onToggleLyrics() = updateState {
        copy(isLyricsVisible = !isLyricsVisible)
    }

    fun onDominantColorExtracted(argb: Long) =
        updateState { copy(dominantColor = argb) }
}
