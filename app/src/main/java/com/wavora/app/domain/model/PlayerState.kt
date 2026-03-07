package com.wavora.app.domain.model

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val isShuffleOn: Boolean = false,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = 0,
    val sleepTimerRemainingMs: Long = 0L, // 0 = no timer active
) {
    // Progress as a fraction in 0f...1f for seek bar rendering.
    val progress: Float
        get() = if (durationMs > 0) positionMs.toFloat() / durationMs.toFloat() else 0f

    // True when there is a next song in the queue.
    val hasNext: Boolean
        get() = currentQueueIndex < queue.lastIndex || repeatMode != RepeatMode.NONE

    // True when there is a previous song in the queue OR position > 3s (restart).
    val hasPrevious
        get() = currentQueueIndex > 0 || positionMs > 3_000

    companion object {
        val Empty = PlayerState()
    }
}

// Maps to ExoPlayer's Player.REPEAT_MODE_* constants.
enum class RepeatMode {
    NONE, // there's no repeat
    ONE, // repeat the current song
    ALL // repeat all songs forever
}

// Sort options available throughout the library.
enum class SortOrder {
    TITLE_ASC,
    TITLE_DESC,
    ARTIST_ASC,
    DURATION_ASC,
    DURATION_DESC,
    DATE_ADDED_ASC,
    DATE_ADDED_DESC
}