package com.wavora.app.core.utils

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
object Constants {
    // App
    const val APP_NAME = "Wavora"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "wavora_playback_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Music Playback"
    const val NOTIFICATION_ID = 1001

    // Data Store keys
    const val PREFS_NAME = "wavora_preferences"
    const val PREF_SORT_ORDER = "sort_order"
    const val PREF_REPEAT_MODE = "repeat_mode"
    const val PREF_SHUFFLE_ENABLED = "shuffle_enabled"
    const val PREF_EQ_ENABLED = "eq_enabled"
    const val PREF_EQ_PRESET = "eq_preset"
    const val PREF_LAST_PLAYED_ID = "last_played_song_id"
    const val PREF_LAST_POSITION_MS = "last_position_ms"
    const val PREF_THEME_MODE = "theme_mode"

    // WorkManager tags
    const val WORK_TAG_LIBRARY_SCAN = "library_scan"

    // Library scanning
    /** Minimum song duration in milliseconds — filters out ringtones and sound effects. */
    const val MIN_SONG_DURATION_MS = 30_000L

    // Player
    /** Seek position update interval during playback. */
    const val SEEK_UPDATE_INTERVAL_MS = 500L

    /** If this close to end, skip forward counts as "skip to next" not "restart". */
    const val SKIP_PREVIOUS_THRESHOLD_MS = 3_000L

    /** Buffer config — lower values save battery; higher values prevent stutter on slow storage. */
    const val BUFFER_MIN_MS_BATTERY = 300
    const val BUFFER_MAX_MS_BATTERY = 2_000
    const val BUFFER_MIN_MS_CHARGING = 3_000
    const val BUFFER_MAX_MS_CHARGING = 10_000

    // Album - Artist
    /** Target width/height for album art loading via Coil. Full-res is wasteful. */
    const val ALBUM_ART_SIZE_PX = 300

    // Search
    /** Debounce delay for search input to avoid hammering Room on every keystroke. */
    const val SEARCH_DEBOUNCE_MS = 300L

    /** Minimum query length before a search is triggered. */
    const val SEARCH_MIN_CHARS = 2

    // Smart Playlists
    const val SMART_PLAYLIST_LIMIT = 50

}