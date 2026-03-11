package com.wavora.app.player.queue

import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 11,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
/**
 * Manages the playback queue with shuffle support.
 *
 * Design decisions:
 *  - Pure Kotlin — no Android framework dependencies. Fully unit-testable.
 *  - [originalQueue] stores the unshuffled order so disabling shuffle restores it.
 *  - [playQueue] is the active order (shuffled or original).
 *  - Shuffle uses Fisher-Yates, pinning the current song at index 0 so it
 *    keeps playing without a skip.
 *  - All state is exposed as [StateFlow] so collectors always get the latest
 *    value immediately upon subscription.
 *  - Thread-safety: all mutations are synchronized on [lock]. The service calls
 *    these methods from the main thread (ExoPlayer callback thread), so a
 *    simple mutex is sufficient.
 */
@Singleton
class QueueManager @Inject constructor() {
    private val lock = Any()

    // Internal State
    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    private val _currentIndex = MutableStateFlow(0)
    private val _isShuffled = MutableStateFlow(false)
    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)

    private var originalQueue = listOf<Song>()

    // Public State
    val queue = _queue.asStateFlow()
    val currentIndex = _currentIndex.asStateFlow()
    val isShuffled = _isShuffled.asStateFlow()
    val repeatMode = _repeatMode.asStateFlow()

    val currentSong: Song?
        get() = _queue.value.getOrNull(_currentIndex.value)

    val hasNext: Boolean
        get() {
            val q = _queue.value
            val i = _currentIndex.value
            return when (_repeatMode.value) {
                RepeatMode.NONE -> i < q.lastIndex
                RepeatMode.ONE -> true
                RepeatMode.ALL -> q.isNotEmpty()
            }
        }

    val hasPrevious: Boolean
        get() = _currentIndex.value > 0 || _repeatMode.value != RepeatMode.NONE

    // ── Queue mutations ───────────────────────────────────────────────────────

    /**
     * Replace the entire queue and start at [startIndex].
     * If shuffle is on, the list is shuffled with [startIndex] song pinned first.
     */

    fun setQueue(songs: List<Song>, startIndex: Int = 0) = synchronized(lock) {
        originalQueue = songs.toList()
        if (_isShuffled.value) {
            val shuffled = songs.toMutableList()
            val current = shuffled.removeAt(startIndex)
            shuffled.shuffle()
            shuffled.add(0, current)
            _queue.value = shuffled
            _currentIndex.value = 0
        } else {
            _queue.value = songs.toList()
            _currentIndex.value = startIndex.coerceIn(0, songs.lastIndex.coerceAtLeast(0))
        }
    }

    fun addToQueue(song: Song) = synchronized(lock) {
        _queue.value += song
        if (!_isShuffled.value)
            originalQueue = originalQueue + song
    }

    fun addNext(song: Song) = synchronized(lock) {
        val insertAt = _currentIndex.value + 1
        val mutable = _queue.value.toMutableList()
        mutable.add(insertAt.coerceIn(0, mutable.size), song)
        _queue.value = mutable
    }

    fun removeAt(index: Int) = synchronized(lock) {
        val q = _queue.value.toMutableList()
        if (index !in q.indices) return@synchronized
        q.removeAt(index)
        _queue.value = q

        // Adjust current index if we removed a song before or at it
        if (index <= _currentIndex.value && _currentIndex.value > 0)
            _currentIndex.value -= 1
    }

    fun moveItem(from: Int, to: Int) = synchronized(lock) {
        val q = _queue.value.toMutableList()
        if (from !in q.indices || to !in q.indices) return@synchronized
        val song = q.removeAt(from)
        q.add(to, song)
        _queue.value = q

        // Keep current Index tracking the same song
        _currentIndex.value = when {
            from == _currentIndex.value -> to
            _currentIndex.value in (from + 1)..to -> _currentIndex.value - 1
            _currentIndex.value in to..<from -> _currentIndex.value + 1
            else -> _currentIndex.value
        }
    }

    fun clearQueue() = synchronized(lock) {
        _queue.value = emptyList()
        originalQueue = emptyList()
        _currentIndex.value = 0
    }

    // Navigation
    /**
     * Advance to the next song. Returns the next [Song] or null if the queue
     * is exhausted and repeat is off.
     */
    fun skipToNext(): Song? = synchronized(lock) {
        val q = _queue.value
        if (q.isEmpty()) return null
        return when (_repeatMode.value) {
            RepeatMode.ONE -> q[_currentIndex.value] // replay current
            RepeatMode.NONE -> {
                if (_currentIndex.value < q.lastIndex) {
                    _currentIndex.value += 1
                    q[_currentIndex.value]
                } else null
            }

            RepeatMode.ALL -> {
                _currentIndex.value = (_currentIndex.value + 1) % q.size
                q[_currentIndex.value]
            }
        }
    }


    /**
     * Go to the previous song. Returns the song to play or null.
     * Callers should check position first — if > [PlayerConstants.RESTART_THRESHOLD_MS],
     * restart the current song instead of skipping back.
     */
    fun skipToPrevious(): Song? = synchronized(lock) {
        val q = _queue.value
        if (q.isEmpty()) return null

        return when (_repeatMode.value) {
            RepeatMode.ALL -> {
                _currentIndex.value = if (_currentIndex.value == 0) q.lastIndex
                else _currentIndex.value - 1
                q[_currentIndex.value]
            }

            else -> {
                if (_currentIndex.value > 0) {
                    _currentIndex.value -= 1
                    q[_currentIndex.value]
                } else q[0]
            }
        }
    }

    fun skipToIndex(index: Int): Song? = synchronized(lock) {
        val q = _queue.value
        if (index !in q.indices) return null
        _currentIndex.value = index
        q[index]
    }

    // Playback modes

    fun setRepeatMode(mode: RepeatMode) = synchronized(lock) {
        _repeatMode.value = mode
    }

    /**
     * Toggle shuffle. When enabling, the current song stays at position 0
     * and the rest are shuffled. When disabling, the original order is
     * restored with the current song at its original position.
     */
    fun setShuffleEnabled(enabled: Boolean) = synchronized(lock) {
        if (_isShuffled.value == enabled) return@synchronized
        _isShuffled.value = enabled

        val current = currentSong ?: return@synchronized

        if (enabled) {
            val shuffled = originalQueue.toMutableList()
            shuffled.remove(current)
            shuffled.add(0, current)
            _queue.value = shuffled
            _currentIndex.value = 0
        } else {
            // restore original order, find current song's position
            _queue.value = originalQueue.toList()
            _currentIndex.value = originalQueue.indexOfFirst { it.id == current.id }
                .coerceAtLeast(0)
        }
    }

    // Helpers
    fun setCurrentIndex(index: Int) = synchronized(lock) {
        _currentIndex.value = index.coerceIn(0, (_queue.value.size - 1).coerceAtLeast(0))
    }
}