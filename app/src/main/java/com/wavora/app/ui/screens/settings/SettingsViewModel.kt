package com.wavora.app.ui.screens.settings

import com.wavora.app.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // Phase 2+: DataStore injected for persistence
) : BaseViewModel<SettingsUiState, SettingsEvent>(SettingsUiState()) {

    fun onDarkThemeToggle(enabled: Boolean) = updateState { copy(isDarkTheme = enabled) }
    fun onDynamicColorToggle(enabled: Boolean) = updateState { copy(useDynamicColor = enabled) }
    fun onGaplessToggle(enabled: Boolean) = updateState { copy(gaplessPlayback = enabled) }
    fun onLockScreenArtToggle(enabled: Boolean) =
        updateState { copy(showAlbumArtOnLockScreen = enabled) }

    fun onEqualizerClick() = emitEvent(SettingsEvent.NavigateToEqualizer)
}