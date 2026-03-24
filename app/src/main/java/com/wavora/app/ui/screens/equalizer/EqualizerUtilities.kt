package com.wavora.app.ui.screens.equalizer

import androidx.compose.ui.Modifier

/**
 * @author Muhamed Amin Hassan on 24,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class EqualizerPreset(
    val name: String,
    /** Gain in mil-libels per band (typically 5 bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz). */
    val gains: List<Int>,
)

val EQ_PRESETS = listOf(
    EqualizerPreset("Flat", listOf(0, 0, 0, 0, 0)),
    EqualizerPreset("Bass Boost", listOf(600, 400, 0, -200, -200)),
    EqualizerPreset("Treble", listOf(-200, -200, 0, 400, 600)),
    EqualizerPreset("Vocal", listOf(-200, 0, 400, 200, -200)),
    EqualizerPreset("Pop", listOf(100, 300, 400, 300, 100)),
    EqualizerPreset("Rock", listOf(400, 200, -100, 200, 400)),
    EqualizerPreset("Jazz", listOf(200, 100, 300, 200, 100)),
    EqualizerPreset("Classical", listOf(300, 200, 0, 200, 300)),
    EqualizerPreset("Electronic", listOf(400, 300, 0, 300, 400)),
    EqualizerPreset("Hip-Hop", listOf(500, 400, 100, 0, 100)),
)

data class EqualizerUiState(
    val isEnabled: Boolean = true,
    val bandGains: List<Int> = List(5) { 0 },       // mil-libels, 5 bands
    val bandFreqLabels: List<String> = listOf("60Hz", "230Hz", "910Hz", "3.6k", "14k"),
    val minGainMb: Int = -1500, // -15dB
    val maxGainMb: Int = 1500, // +15dB
    val selectedPreset: Int = 0, // index into EQ_PRESETS
    val audioSessionId: Int = 0,
)
