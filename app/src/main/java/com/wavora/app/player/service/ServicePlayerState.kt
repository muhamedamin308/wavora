package com.wavora.app.player.service

import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song

/**
 * @author Muhamed Amin Hassan on 11,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
/**
 * Snapshot of the service's internal player state.
 * Exposed to [PlayerRepositoryImpl] via the service binder / shared flow.
 */
data class ServicePlayerState(
    val currentSong: Song?          = null,
    val isPlaying: Boolean          = false,
    val positionMs: Long            = 0L,
    val durationMs: Long            = 0L,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val isShuffleOn: Boolean        = false,
    val queue: List<Song>           = emptyList(),
    val currentQueueIndex: Int      = 0,
    val sleepTimerEndsAtMs: Long    = 0L,
    val sleepTimerRemainingMs: Long = 0L,
    val wasPlayingBeforeFocusLoss: Boolean = false,
    val error: String?              = null,
)
