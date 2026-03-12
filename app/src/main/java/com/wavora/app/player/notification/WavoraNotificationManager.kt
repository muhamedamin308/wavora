package com.wavora.app.player.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import com.wavora.app.player.PlayerConstants

/**
 * @author Muhamed Amin Hassan on 11,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
/**
 * Manages the playback notification for [WavoraPlaybackService].
 *
 * Uses [PlayerNotificationManager] from Media3 which:
 *  - Automatically keeps notification in sync with player state (play/pause,
 *    current track, album art) with zero manual work.
 *  - Attaches a [MediaStyle] notification that shows media controls on the
 *    lock screen and in the notification shade.
 *  - Handles the foreground service promotion/demotion automatically —
 *    when playing, the service moves to foreground; when paused and
 *    notification dismissed, it returns to background.
 *  - Loads album art asynchronously via MediaMetadata.artworkUri (set from
 *    [WavoraPlaybackService.Song.toMediaItem]).
 *
 * Battery:
 *  - No polling or timers here — PlayerNotificationManager reacts to
 *    ExoPlayer state changes via [Player.Listener].
 *  - Notification updates are coalesced by the system — rapid state changes
 *    don't flood the notification manager.
 */

@UnstableApi
class WavoraNotificationManager(
    private val context: Context,
    private val mediaSession: MediaSession,
) {
    private val notificationManager: NotificationManager =
        context.getSystemService()!!

    private var playerNotificationManager: PlayerNotificationManager? = null

    /**
     * Create the notification channel (idempotent — safe to call multiple times).
     * Must be called before [startNotification].
     */
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            PlayerConstants.NOTIFICATION_CHANNEL_ID,
            PlayerConstants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,   // LOW = no sound, no vibration
        ).apply {
            description = "WAVORA music playback controls"
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Attach [PlayerNotificationManager] to the ExoPlayer inside the MediaSession.
     * Called once in [WavoraPlaybackService.onCreate].
     *
     * [PlayerNotificationManager] handles:
     *  - Previous / Play-Pause / Next action buttons
     *  - Album art loading from MediaMetadata.artworkUri
     *  - Foreground service promotion when playing
     *  - MediaSession token attachment (for lock screen)
     */
    fun startNotification(serviceCallback: PlayerNotificationManager.NotificationListener) {
        playerNotificationManager = PlayerNotificationManager.Builder(
            context,
            PlayerConstants.NOTIFICATION_ID,
            PlayerConstants.NOTIFICATION_CHANNEL_ID,
        )
            .setNotificationListener(serviceCallback)
            .setSmallIconResourceId(android.R.drawable.ic_media_play) // Phase 10: custom icon
            .setChannelDescriptionResourceId(android.R.string.untitled) // placeholder
            .build()
            .also { mgr ->
                mgr.setMediaSessionToken(mediaSession.sessionCompatToken)
                mgr.setUseFastForwardAction(false)
                mgr.setUseRewindAction(false)
                mgr.setUsePreviousAction(true)
                mgr.setUseNextAction(true)
                mgr.setUseStopAction(false)
                mgr.setPlayer(mediaSession.player)
            }
    }

    fun stopNotification() {
        playerNotificationManager?.setPlayer(null)
        playerNotificationManager = null
    }
}