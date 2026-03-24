package com.wavora.app.ui.screens.equalizer

import android.media.audiofx.Equalizer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 24,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@HiltViewModel
class EqualizerViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState = _uiState.asStateFlow()

    private var equalizer: Equalizer? = null


    init {
        // Attach to session 0 (global) — Phase 9 will pass the real ExoPlayer session id
        attachAudioSession(audioSessionId = 0)
    }

    fun attachAudioSession(audioSessionId: Int) {
        viewModelScope.launch {
            equalizer?.release()
            runCatching {
                val eq = Equalizer(0, audioSessionId)
                eq.enabled = _uiState.value.isEnabled

                val numBands = eq.numberOfBands.toInt()
                val bandRange = eq.bandLevelRange  // [min, max] in millibels
                val minMb = bandRange[0].toInt().coerceAtLeast(-1500)
                val maxMb = bandRange[1].toInt().coerceAtMost(1500)

                // Read current Gains
                val gains = (0 until numBands).map { band ->
                    eq.getBandLevel(band.toShort()).toInt()
                }

                // Build frequency labels from the EQ bands
                val frequencyLabels = (0 until numBands).map { band ->
                    val hz = eq.getCenterFreq(band.toShort()) / 1000 // mHz -> hz
                    if (hz >= 1000) "${hz / 1000}k" else "${hz}Hz"
                }

                equalizer = eq
                _uiState.update {
                    it.copy(
                        bandGains = gains,
                        bandFreqLabels = frequencyLabels,
                        minGainMb = minMb,
                        maxGainMb = maxMb,
                        audioSessionId = audioSessionId,
                    )
                }
            }.onFailure { e ->
                Log.w(TAG, "Equalizer unavailable on this device: ${e.message}")
                // Keep UI functional with manual-only state (no AudioEffect applied)
            }
        }
    }

    fun onEnabledToggle(enabled: Boolean) {
        equalizer?.enabled = enabled
        _uiState.update { it.copy(isEnabled = enabled) }
    }

    fun onBandGainChanged(bandIndex: Int, gainMb: Int) {
        val clamped = gainMb.coerceIn(_uiState.value.minGainMb, _uiState.value.maxGainMb)
        runCatching { equalizer?.setBandLevel(bandIndex.toShort(), clamped.toShort()) }
        _uiState.update {
            val newGains = it.bandGains.toMutableList()
            newGains[bandIndex] = clamped
            it.copy(bandGains = newGains, selectedPreset = -1)  // -1 = custom
        }
    }

    fun onPresetSelected(presetIndex: Int) {
        val preset = EQ_PRESETS.getOrNull(presetIndex) ?: return
        val numBands = _uiState.value.bandGains.size

        preset.gains.forEachIndexed { band, gain ->
            if (band < gain) {
                val clamped = gain.coerceIn(_uiState.value.minGainMb, _uiState.value.maxGainMb)
                runCatching { equalizer?.setBandLevel(band.toShort(), clamped.toShort()) }
            }
        }

        _uiState.update {
            it.copy(
                bandGains = preset.gains.take(numBands),
                selectedPreset = presetIndex,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        equalizer?.release()
        equalizer = null
    }

    companion object {
        private const val TAG = "EqualizerViewModel"
    }
}