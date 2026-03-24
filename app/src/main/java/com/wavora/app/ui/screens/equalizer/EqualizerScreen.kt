package com.wavora.app.ui.screens.equalizer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wavora.app.ui.components.WavoraTopBar
import com.wavora.app.ui.theme.PlaybackAccent

/**
 * @author Muhamed Amin Hassan on 24,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun EqualizerScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EqualizerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            WavoraTopBar(
                title = "Equalizer",
                onNavigateUp = onNavigateUp,
                actions = {
                    // Enable / disable toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (state.isEnabled) "On" else "Off",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(4.dp))
                        Switch(
                            checked = state.isEnabled,
                            onCheckedChange = viewModel::onEnabledToggle,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            // EQ-Visualizer + sliders
            EqBandSliders(
                state = state,
                onBandChanged = viewModel::onBandGainChanged,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(24.dp))
            // Presets Row
            Text(
                "Presets",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(EQ_PRESETS) { index, preset ->
                    val selected = state.selectedPreset == index
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onPresetSelected(index) },
                        label = { Text(preset.name) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Filled.GraphicEq, null, Modifier.size(16.dp)) }
                        } else null,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EqBandSliders(
    state: EqualizerUiState,
    onBandChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        state.bandGains.forEachIndexed { index, gain ->
            EqBandColumn(
                freqLabel = state.bandFreqLabels.getOrElse(index) { "—" },
                gain = gain,
                minGain = state.minGainMb,
                maxGain = state.maxGainMb,
                isEnabled = state.isEnabled,
                onChanged = { newGain -> onBandChanged(index, newGain) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun EqBandColumn(
    freqLabel: String,
    gain: Int,
    minGain: Int,
    maxGain: Int,
    isEnabled: Boolean,
    onChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val range = (maxGain - minGain).toFloat()
    val fraction = ((gain - minGain).toFloat() / range).coerceIn(0f, 1f)
    // Animated bar height for visual flair
    val barFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(200),
        label = "barFraction_$freqLabel",
    )

    Column(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // dB label
        Text(
            text = "${gain / 100}dB",
            style = MaterialTheme.typography.labelSmall,
            color = if (isEnabled) PlaybackAccent
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))

        // Vertical slider (rotated)
        Slider(
            value = fraction,
            onValueChange = { f -> onChanged((f * range + minGain).toInt()) },
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = if (isEnabled) PlaybackAccent else MaterialTheme.colorScheme.outline,
                activeTrackColor = if (isEnabled) PlaybackAccent else MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier
                .weight(1f)
                .graphicsLayer(rotationZ = -90f)
                .width(160.dp),
        )

        Spacer(Modifier.height(4.dp))
        // Frequency label
        Text(
            text = freqLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}