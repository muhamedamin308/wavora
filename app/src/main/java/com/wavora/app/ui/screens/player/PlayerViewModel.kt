package com.wavora.app.ui.screens.player

import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.domain.model.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
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
    // Injected in Phase 3:
    // private val playerRepository: PlayerRepository,
) : BaseViewModel<PlayerUiState, PlayerEvent>(PlayerUiState()) {

    // ── Playback actions (routed to PlayerRepository in Phase 3) ──────────────
    fun togglePlayPause() {
        // Phase 3: if (currentState.playerState.isPlaying) playerRepository.pause()
        //          else playerRepository.resume()
        updateState { copy(playerState = playerState.copy(isPlaying = !playerState.isPlaying)) }
    }

    fun skipToNext() {
        // Phase 3: safeLaunch { playerRepository.skipToNext() }
    }

    fun skipToPrevious() {
        // Phase 3: safeLaunch { playerRepository.skipToPrevious() }
    }

    fun seekTo(positionMs: Long) {
        // Phase 3: safeLaunch { playerRepository.seekTo(positionMs) }
    }

    fun toggleShuffle() {
        val newShuffle = !currentState.playerState.isShuffleOn
        updateState { copy(playerState = playerState.copy(isShuffleOn = newShuffle)) }
        // Phase 3: safeLaunch { playerRepository.setShuffleEnabled(newShuffle) }
    }

    fun cycleRepeatMode() {
        val next = when (currentState.playerState.repeatMode) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
        updateState {
            copy(playerState = playerState.copy(repeatMode = next))
        }
        // Phase 3: safeLaunch { playerRepository.setRepeatMode(next) }
    }

    fun toggleLyrics() {
        updateState { copy(isLyricsVisible = !isLyricsVisible) }
    }

    fun onDominantColorExtracted(argb: Long) {
        updateState { copy(dominantColor = argb) }
    }
}
