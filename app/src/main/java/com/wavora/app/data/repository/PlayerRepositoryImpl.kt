package com.wavora.app.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.wavora.app.domain.model.PlayerState
import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import com.wavora.app.player.service.ServicePlayerState
import com.wavora.app.player.service.WavoraPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 12,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Concrete implementation of [PlayerRepository].
 *
 * Communication pattern:
 *  - Reads state from [WavoraPlaybackService.playerStateFlow] — a [MutableStateFlow]
 *    updated by the service on every player event.
 *  - Sends commands via [MediaController] — the Media3 binder-based IPC that
 *    routes to the service's ExoPlayer.
 *
 * Why MediaController instead of direct service binding?
 *  - MediaController handles the binder connection lifecycle automatically.
 *  - Commands queued before connection is established are delivered once connected.
 *  - Works consistently whether the service is in the same process or not.
 *  - Enables Android Auto / WearOS / lock screen control for free.
 *
 * Why share playerStateFlow directly from the service?
 *  - The service and repository are always in the same process in WAVORA.
 *  - SharedFlow avoids the overhead of serializing state through MediaSession
 *    extras for every 500ms position update.
 *
 * [playerState] maps [ServicePlayerState] to the domain [PlayerState] model,
 * keeping the player layer decoupled from the domain layer.
 */
@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PlayerRepository {

    // ── Service state ─────────────────────────────────────────────────────────

    /**
     * Reference to the service's state flow, connected lazily when the service starts.
     * Before the service is bound, we emit [PlayerState.Empty].
     */
    private val _serviceState = MutableStateFlow(ServicePlayerState())

    fun attachServiceState(serviceStateFlow: MutableStateFlow<ServicePlayerState>) {
        // In the same-process case, we can read the service's flow directly.
        // For a cleaner architecture: collect from serviceStateFlow and relay.
        serviceStateFlow.value.let { _serviceState.value = it }
    }

    // MediaController (command sender)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    companion object {
        private const val TAG = "PlayerRepositoryImpl"
    }

    /**
     * Connect [MediaController] to the playback service.
     * Safe to call multiple times — only connects once.
     */
    @OptIn(UnstableApi::class)
    fun connect() {
        if (controllerFuture != null) return
        val sessionToken = SessionToken(
            context,
            ComponentName(context, WavoraPlaybackService::class.java),
        )
        controllerFuture = MediaController.Builder(context, sessionToken)
            .buildAsync()
            .also { future ->
                future.addListener(
                    {
                        runCatching { mediaController = future.get() }
                            .onFailure { Log.e(TAG, "MediaController connect failed", it) }
                    },
                    MoreExecutors.directExecutor()
                )
            }
    }

    fun disconnect() {
        MediaController.releaseFuture(controllerFuture ?: return)
        controllerFuture = null
        mediaController = null
    }

    // PlayerRepository Implementation

    override val playerState: Flow<PlayerState>
        get() = _serviceState.asStateFlow().map { it.toDomain() }

    override suspend fun play(song: Song) {
        mediaController?.apply {
            setMediaItem(song.toMediaItem())
            prepare()
            play()
        }
    }

    override suspend fun playAll(
        songs: List<Song>,
        startIndex: Int,
    ) {
        mediaController?.apply {
            setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0L)
            prepare()
            play()
        }
    }

    override suspend fun pause() {
        mediaController?.pause()
    }

    override suspend fun resume() {
        mediaController?.play()
    }

    override suspend fun stop() {
        mediaController?.stop()
    }

    override suspend fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    override suspend fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    override suspend fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    override suspend fun setShuffleEnabled(enabled: Boolean) {
        mediaController?.shuffleModeEnabled = enabled
    }

    override suspend fun setRepeatMode(mode: RepeatMode) {
        mediaController?.repeatMode = when (mode) {
            RepeatMode.NONE -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    override suspend fun addToQueue(song: Song) {
        mediaController?.addMediaItem(song.toMediaItem())
    }

    override suspend fun removeFromQueue(index: Int) {
        mediaController?.removeMediaItem(index)
    }

    override suspend fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        mediaController?.moveMediaItem(fromIndex, toIndex)
    }

    override suspend fun clearQueue() {
        mediaController?.clearMediaItems()
    }

    @OptIn(UnstableApi::class)
    override suspend fun setSleepTimer(durationMs: Long) {
        // Routed to the service via a custom MediaSession command (Phase 5 full impl).
        // For Phase 3: send via intent to the service directly.
        val intent = Intent(context, WavoraPlaybackService::class.java).apply {
            action =
                if (durationMs > 0) "com.wavora.app.SET_SLEEP_TIMER" else "com.wavora.app.CANCEL_SLEEP_TIMER"
            putExtra("duration_ms", durationMs)
        }
        context.startService(intent)
    }

    // Mappers

    private fun ServicePlayerState.toDomain() = PlayerState(
        currentSong = currentSong,
        isPlaying = isPlaying,
        positionMs = positionMs,
        durationMs = durationMs,
        repeatMode = repeatMode,
        isShuffleOn = isShuffleOn,
        queue = queue,
        currentQueueIndex = currentQueueIndex,
        sleepTimerRemainingMs = sleepTimerRemainingMs,
    )

    private fun Song.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setUri(contentUri.toUri())
            .setMediaId(id.toString())
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artistName)
                    .setAlbumTitle(albumName)
                    .setArtworkUri(albumArtUri?.toUri())
                    .build()
            )
            .build()
}