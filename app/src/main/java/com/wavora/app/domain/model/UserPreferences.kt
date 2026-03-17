package com.wavora.app.domain.model

/**
 * @author Muhamed Amin Hassan on 17,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class UserPreferences(
    val isDarkTheme: Boolean = true,
    val useDynamicColors: Boolean = true,
    val skipDurationSec: Int = 10,
    val crossfadeDurationMs: Int = 0, // 0 - disabled
    val gaplessPlayback: Boolean = true,
    val showAlbumArtOnLockScreen: Boolean = true,
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val lastScanTimestampMs: Long = 0L,
)