package com.wavora.app.player

/**
 * @author Muhamed Amin Hassan on 11,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
object PlayerConstants {

    // Service
    const val SERVICE_ACTION_PLAY = "com.wavora.app.PLAY"
    const val SERVICE_ACTION_PAUSE = "com.wavora.app.PAUSE"
    const val SERVICE_ACTION_SKIP_NEXT = "com.wavora.app.SKIP_NEXT"
    const val SERVICE_ACTION_SKIP_PREV = "com.wavora.app.SKIP_PREV"
    const val SERVICE_ACTION_STOP = "com.wavora.app.STOP"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "wavora_playback"
    const val NOTIFICATION_CHANNEL_NAME = "Music Player"
    const val NOTIFICATION_ID = 1001

    // Audio Focus
    // Volume level applied when ducking for a transient focus loss (e.g. n prompt.)
    const val DUCK_VOLUME = 0.2f

    // How Long to wait after transient focus loss before pausing (ms)
    const val TRANSIENT_LOSS_TIMEOUT_MS = 30_000L

    // Buffer size
    //Minimum buffer while on battery - less RAM, faster seek start
    const val BUFFER_MIN_MS_DEFAULT = 2_000
    const val BUFFER_MAX_MS_DEFAULT = 10_000
    const val BUFFER_FOR_PLAYBACK_MS = 500
    const val BUFFER_FOR_REBUFFER_MS = 1_500

    // ── Position tracking ─────────────────────────────────────────────────────
    /** How frequently the position StateFlow is updated during playback. */
    const val POSITION_UPDATE_INTERVAL_MS = 500L

    // ── Skip-previous threshold ───────────────────────────────────────────────
    /** If position > this, skip-previous restarts the current song instead. */
    const val RESTART_THRESHOLD_MS = 3_000L

    // Sleep Timer
    val SLEEP_TIMER_OPTIONS_MINUTES = listOf(5, 10, 15, 20, 30, 45, 60, 90, 120)
}