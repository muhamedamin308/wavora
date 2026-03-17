package com.wavora.app.ui.screens.settings

import androidx.lifecycle.viewModelScope
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.domain.repository.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
) : BaseViewModel<SettingsUiState, SettingsEvent>(SettingsUiState()) {

    init {
        prefsRepository.preferences
            .onEach { prefs -> updateState { copy(prefs = prefs, isLoaded = true) } }
            .launchIn(viewModelScope)
    }

    fun onDarkThemeToggle(enabled: Boolean) = safeLaunch { prefsRepository.setDarkTheme(enabled) }
    fun onDynamicColorToggle(enabled: Boolean) =
        safeLaunch { prefsRepository.setDynamicColors(enabled) }

    fun onGaplessToggle(enabled: Boolean) =
        safeLaunch { prefsRepository.setGaplessPlayback(enabled) }

    fun onLockScreenArtToggle(enabled: Boolean) =
        safeLaunch { prefsRepository.setShowAlbumArtOnLockScreen(enabled) }

    fun onSkipDurationSelected(seconds: Int) =
        safeLaunch { prefsRepository.setSkipDuration(seconds) }

    fun onCrossfadeSelected(ms: Int) = safeLaunch { prefsRepository.setCrossfadeDuration(ms) }

    fun onEqualizerClick() = emitEvent(SettingsEvent.NavigateToEqualizer)
}