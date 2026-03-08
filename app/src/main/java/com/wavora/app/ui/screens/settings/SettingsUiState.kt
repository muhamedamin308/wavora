package com.wavora.app.ui.screens.settings

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class SettingsUiState(
    val isDarkTheme: Boolean = true,
    val useDynamicColor: Boolean = true,
    val skipDurationSec: Int = 10,
    val crossfadeDurationMs: Int = 0,     // 0 = disabled
    val gaplessPlayback: Boolean = true,
    val showAlbumArtOnLockScreen: Boolean = true,
    val libraryPath: String = "All storage",
    val appVersion: String = "1.0.0",
)

sealed interface SettingsEvent {
    data object NavigateToEqualizer : SettingsEvent
}