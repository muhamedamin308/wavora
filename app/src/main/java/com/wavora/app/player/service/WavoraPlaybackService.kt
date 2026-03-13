package com.wavora.app.player.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.wavora.app.MainActivity
import com.wavora.app.data.local.dao.QueueDao
import com.wavora.app.data.local.entity.QueueEntity
import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.interfaces.MusicRepository
import com.wavora.app.player.PlayerConstants
import com.wavora.app.player.notification.WavoraNotificationManager
import com.wavora.app.player.queue.QueueManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Wavora's core playback service.
 *
 * Architecture:
 *  - Extends [MediaSessionService] (Media3) — this is the modern replacement
 *    for MediaBrowserServiceCompat. It integrates ExoPlayer + MediaSession
 *    and handles the foreground service notification automatically.
 *  - [ExoPlayer] handles all decoding, buffering, and audio routing.
 *  - [MediaSession] exposes playback controls to Bluetooth devices, lock screen,
 *    Android Auto, Wear OS, and assistant apps via MediaController.
 *  - [AudioFocusManager] handles audio focus + BECOME_NOISY independently.
 *  - [QueueManager] manages the ordered song list and shuffle state.
 *  - A [CoroutineScope] tied to the service lifetime drives the position
 *    update loop and queue persistence.
 *
 * Lifecycle:
 *  1. onCreate()     → build ExoPlayer + MediaSession + AudioFocusManager
 *  2. onStartCommand → handle intent actions (play/pause/skip)
 *  3. onGetSession() → MediaSessionService hook — returns our MediaSession
 *  4. onDestroy()    → release all resources, abandon audio focus
 *
 * Threading:
 *  - ExoPlayer and MediaSession callbacks run on the main thread.
 *  - Room and persistence operations run on [Dispatchers.IO] via coroutines.
 *  - [serviceScope] is canceled in [onDestroy].
 *
 * Battery:
 *  - ExoPlayer holds a PARTIAL_WAKE_LOCK automatically via AudioAttributes
 *    when audio is playing — no manual wakelock management needed.
 *  - Hardware audio decoder is preferred (MediaCodec) — less CPU than SW decode.
 *  - Buffer sizes are tuned for music playback (8s max) to balance memory and
 *    seek latency.
 */

@UnstableApi
@AndroidEntryPoint
class WavoraPlaybackService : MediaSessionService() {
    @Inject
    lateinit var queueManager: QueueManager

    @Inject
    lateinit var queueDao: QueueDao

    @Inject
    lateinit var musicRepository: MusicRepository

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var audioFocusManager: AudioFocusManager

    // TODO: Implement [WavoraNotificationManager] class
    private lateinit var notificationManager: WavoraNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var positionUpdateJob: Job? = null
    private var sleepTimerJob: Job? = null

    // public state read by playerRepositoryImpl via binder
    val playerStateFlow: MutableStateFlow<ServicePlayerState> =
        MutableStateFlow(ServicePlayerState())

    companion object {
        private const val TAG = "WavoraPlaybackService"
    }


    // Lifecycle

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        buildExoPlayer()
        buildMediaSession()
        buildAudioFocusManager()

        notificationManager = WavoraNotificationManager(this, mediaSession)
        notificationManager.createNotificationChannel()

        observeQueueManager()
        restoreQueueFromDatabase()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d(TAG, "Controller connected: ${controllerInfo.packageName}")
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handleIntentAction(intent?.action, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service Destroyed")
        audioFocusManager.abandonAudioFocus()
        positionUpdateJob?.cancel()
        sleepTimerJob?.cancel()
        mediaSession.release()
        player.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ExoPlayer setup
    private fun buildExoPlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                PlayerConstants.BUFFER_MIN_MS_DEFAULT,
                PlayerConstants.BUFFER_MAX_MS_DEFAULT,
                PlayerConstants.BUFFER_FOR_PLAYBACK_MS,
                PlayerConstants.BUFFER_FOR_REBUFFER_MS
            )
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ false,   // We manage focus manually via AudioFocusManager
            )
            .setHandleAudioBecomingNoisy(false) // We handle BECOME_NOISY manually
            .setLoadControl(loadControl)
            .build()

        player.addListener(playerListener)
    }

    // MediaSession Setup
    private fun buildMediaSession() {
        val sessionActivityIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityIntent)
            .setCallback(mediaSessionCallback)
            .build()
    }

    // AudioFocus setup

    private fun buildAudioFocusManager() {
        audioFocusManager = AudioFocusManager(
            context = this,
            callbacks = object : AudioFocusManager.Callbacks {
                override fun onAudioFocusGained() {
                    if (playerStateFlow.value.wasPlayingBeforeFocusLoss) {
                        player.volume = 1f
                        player.play()
                        playerStateFlow.update { it.copy(wasPlayingBeforeFocusLoss = false) }
                    }
                }

                override fun onAudioFocusLost(permanent: Boolean) {
                    val wasPlaying = player.isPlaying
                    player.pause()
                    if (!permanent)
                        playerStateFlow.update { it.copy(wasPlayingBeforeFocusLoss = wasPlaying) }
                }

                override fun onDuck() {
                    player.volume = PlayerConstants.DUCK_VOLUME
                }

                override fun onUnDuck() {
                    player.volume = 1f
                }
            }
        )
    }

    // Queue / Song loading
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return

        if (!audioFocusManager.requestAudioFocus()) {
            Log.w(TAG, "Audio focus denied — aborting play")
            return
        }

        queueManager.setQueue(songs, startIndex)

        val mediaItems = songs.map { it.toMediaItem() }
        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()

        persistQueueAsync()
    }

    fun playSong(song: Song) = playSongs(listOf(song), 0)

    fun skipToNext() {
        if (player.hasNextMediaItem())
            player.seekToNextMediaItem()
        else {
            val next = queueManager.skipToNext()
            next?.let { seekToQueueIndex(queueManager.currentIndex.value) }
        }
    }

    fun skipToPrevious(currentPositionMs: Long) {
        if (currentPositionMs > PlayerConstants.RESTART_THRESHOLD_MS)
            player.seekTo(0L)
        else if (player.hasPreviousMediaItem())
            player.seekToPreviousMediaItem()
        else {
            val prev = queueManager.skipToPrevious()
            prev?.let { seekToQueueIndex(queueManager.currentIndex.value) }
        }
    }

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    fun pause() {
        player.pause()
        if (!player.isPlaying) audioFocusManager.abandonAudioFocus()
    }

    fun resume() {
        if (audioFocusManager.requestAudioFocus()) player.play()
    }

    fun stop() {
        player.stop()
        audioFocusManager.abandonAudioFocus()
        stopSelf()
    }

    fun addToQueue(song: Song) {
        queueManager.addToQueue(song)
        player.addMediaItem(song.toMediaItem())
        persistQueueAsync()
    }

    fun removeFromQueue(index: Int) {
        queueManager.removeAt(index)
        player.removeMediaItem(index)
        persistQueueAsync()
    }

    fun moveQueueItem(from: Int, to: Int) {
        queueManager.moveItem(from, to)
        player.moveMediaItem(from, to)
        persistQueueAsync()
    }

    fun setRepeatMode(mode: RepeatMode) {
        queueManager.setRepeatMode(mode)
        player.repeatMode = when (mode) {
            RepeatMode.NONE -> REPEAT_MODE_OFF
            RepeatMode.ALL -> REPEAT_MODE_ALL
            RepeatMode.ONE -> REPEAT_MODE_ONE
        }
    }

    fun setShuffleEnabled(enable: Boolean) {
        queueManager.setShuffleEnabled(enable)
        player.shuffleModeEnabled = enable
    }

    // Sleep Timer
    fun setSleepTimer(durationMs: Long) {
        sleepTimerJob?.cancel()
        if (durationMs <= 0) {
            playerStateFlow.update { it.copy(sleepTimerEndsAtMs = 0L) }
            return
        }
        val endsAt = System.currentTimeMillis() + durationMs
        playerStateFlow.update {
            it.copy(sleepTimerEndsAtMs = endsAt)
        }
        sleepTimerJob = serviceScope.launch {
            delay(durationMs)
            pause()
            playerStateFlow.update { it.copy(sleepTimerEndsAtMs = 0L) }
        }
    }

    // ExoPlayer Listener
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayerState()
            if (isPlaying) startPositionUpdates() else stopPositionUpdates()
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int,
        ) {
            val newIndex = player.currentMediaItemIndex
            queueManager.setCurrentIndex(newIndex)
            updatePlayerState()

            // Record play history
            val song = queueManager.currentSong ?: return
            serviceScope.launch(Dispatchers.IO) {
                // TODO: Implement recordPlay(id: Long) function inside MusicRepository class
                // musicRepository.recordPlay(song.id)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Player error: ${error.message}", error)
            // Skip to next on decode error — don't let one bad file block the queue
            skipToNext()
            playerStateFlow.update { it.copy(error = error.message) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayerState()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            updatePlayerState()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            updatePlayerState()
        }
    }

    // MediaSession callbacks
    private val mediaSessionCallback = object : MediaSession.Callback {
        // Media3 MediaSession.Callback routes commands from MediaController
        // (Bluetooth, Android Auto, lock screen) through to the player.
        // Default implementations forward to the ExoPlayer — we override
        // only what requires custom logic.
    }

    // state updates
    private fun updatePlayerState() {
        val song = queueManager.currentSong
        val sleepEnds = playerStateFlow.value.sleepTimerEndsAtMs
        val remaining =
            if (sleepEnds > 0) (sleepEnds - System.currentTimeMillis()).coerceAtLeast(0) else 0L

        playerStateFlow.update { current ->
            current.copy(
                currentSong = song,
                isPlaying = player.isPlaying,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.coerceAtLeast(0L),
                repeatMode = queueManager.repeatMode.value,
                isShuffleOn = queueManager.isShuffled.value,
                queue = queueManager.queue.value,
                currentQueueIndex = queueManager.currentIndex.value,
                sleepTimerRemainingMs = remaining,
                error = null,
            )
        }
    }

    // position update loop
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = serviceScope.launch {
            while (isActive) {
                playerStateFlow.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(
                            0L
                        )
                    )
                }
                delay(PlayerConstants.POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    // Queue Observation
    private fun observeQueueManager() {
        serviceScope.launch {
            queueManager.queue.collect { updatePlayerState() }
        }
    }

    // Queue Persistence
    private fun persistQueueAsync() {
        serviceScope.launch(Dispatchers.IO) {
            val songs = queueManager.queue.value
            val current = queueManager.currentIndex.value
            val entities = songs.mapIndexed { index, song ->
                QueueEntity(
                    songId = song.id,
                    position = index,
                    isCurrent = index == current,
                )
            }
            queueDao.replaceQueue(entities)
        }
    }

    /**
     * On service start, restore the last queue from Room so the user can
     * resume playback after the app was killed.
     */
    private fun restoreQueueFromDatabase() {
        serviceScope.launch(Dispatchers.IO) {
            val rawQueue = queueDao.getRawQueue()
            if (rawQueue.isEmpty()) return@launch

            val currentPosition = rawQueue.indexOfFirst { it.isCurrent }.coerceAtLeast(0)
            // We'd need the full Song objects — for now, the queue is rebuilt
            // when the user taps play. Full restoration with song metadata
            // requires loading from songDao (done in Phase 5 with session resumption).
            Log.d(
                TAG,
                "Queue has ${rawQueue.size} items, " +
                        "current=$currentPosition (restoration deferred to Phase 5)"
            )
        }
    }

    // Intent Handling
    private fun handleIntentAction(action: String?, intent: Intent?) {
        when (action) {
            PlayerConstants.SERVICE_ACTION_PLAY -> resume()
            PlayerConstants.SERVICE_ACTION_PAUSE -> pause()
            PlayerConstants.SERVICE_ACTION_SKIP_NEXT -> skipToNext()
            PlayerConstants.SERVICE_ACTION_SKIP_PREV -> skipToPrevious(player.currentPosition)
            PlayerConstants.SERVICE_ACTION_STOP -> stop()

            "com.wavora.app.SET_SLEEP_TIMER" -> {
                val ms = intent?.getLongExtra("duration_ms", 0L) ?: 0L
                setSleepTimer(ms)
            }

            "com.wavora.app.CANCEL_SLEEP_TIMER" -> setSleepTimer(0L)
        }
    }

    // Helpers
    private fun seekToQueueIndex(index: Int) {
        player.seekTo(index, 0L)
    }

    private fun Song.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setUri(Uri.parse(contentUri))
            .setMediaId(id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artistName)
                    .setAlbumTitle(albumName)
                    .setArtworkUri(albumArtUri?.let { Uri.parse(it) })
                    .setTrackNumber(trackNumber)
                    .build()
            ).build()
}

