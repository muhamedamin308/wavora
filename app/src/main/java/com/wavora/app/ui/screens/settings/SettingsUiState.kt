package com.wavora.app.ui.screens.settings

import com.wavora.app.domain.model.UserPreferences

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class SettingsUiState(
    val prefs: UserPreferences = UserPreferences(),
    val isLoaded: Boolean = false
)

sealed interface SettingsEvent {
    data object NavigateToEqualizer : SettingsEvent
}