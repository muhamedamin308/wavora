package com.wavora.app.domain.repository.interfaces

import com.wavora.app.domain.model.PlayerState
import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Abstracts the ExoPlayer-backed playback service behind a clean interface.
 * Implemented in Phase 3 by [com.wavora.app.player.PlayerRepositoryImpl].
 */
interface PlayerRepository {
    // Live stream of the current [PlayerState]. Never null; defaults to [PlayerState.Empty].
    val playerState: Flow<PlayerState>

    suspend fun play(song: Song)
    suspend fun playAll(songs: List<Song>, startIndex: Int = 0)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    suspend fun skipToNext()
    suspend fun skipToPrevious()
    suspend fun seekTo(positionMs: Long)
    suspend fun setShuffleEnabled(enabled: Boolean)
    suspend fun setRepeatMode(mode: RepeatMode)
    suspend fun addToQueue(song: Song)
    suspend fun removeFromQueue(index: Int)
    suspend fun moveQueueItem(fromIndex: Int, toIndex: Int)
    suspend fun clearQueue()
}