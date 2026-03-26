package com.wavora.app.ui.screens.equalizer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wavora.app.ui.components.WavoraTopBar

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

    val containerColor by animateColorAsState(
        targetValue = if (state.isEnabled)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "eq_bg"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            WavoraTopBar(
                title = "Equalizer",
                onNavigateUp = onNavigateUp,
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (state.isEnabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Switch(
                            checked = state.isEnabled,
                            onCheckedChange = viewModel::onEnabledToggle
                        )
                    }
                },
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(16.dp))

            // 🎧 EQ MAIN CARD
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        "Sound Control",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    EqBandSlidersEnhanced(
                        state = state,
                        onBandChanged = viewModel::onBandGainChanged
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 🎚️ PRESETS CARD
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {

                    Text(
                        "Presets",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(EQ_PRESETS) { index, preset ->
                            val selected = state.selectedPreset == index

                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onPresetSelected(index) },
                                label = {
                                    Text(
                                        preset.name,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Default.GraphicEq,
                                            contentDescription = null
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EqBandSlidersEnhanced(
    state: EqualizerUiState,
    onBandChanged: (Int, Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        state.bandGains.forEachIndexed { index, gain ->

            EqBandColumnEnhanced(
                freqLabel = state.bandFreqLabels.getOrElse(index) { "—" },
                gain = gain,
                minGain = state.minGainMb,
                maxGain = state.maxGainMb,
                isEnabled = state.isEnabled,
                onChanged = { onBandChanged(index, it) }
            )
        }
    }
}

@Composable
private fun EqBandColumnEnhanced(
    freqLabel: String,
    gain: Int,
    minGain: Int,
    maxGain: Int,
    isEnabled: Boolean,
    onChanged: (Int) -> Unit,
) {
    val range = (maxGain - minGain).toFloat()
    val fraction = ((gain - minGain) / range).coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(250),
        label = "eq_anim"
    )

    val activeColor = if (isEnabled)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {

        // 🎯 Gain Label
        Text(
            "${gain / 100} dB",
            style = MaterialTheme.typography.labelSmall,
            color = activeColor
        )

        Spacer(Modifier.height(6.dp))

        // 🎨 Visual Bar (NEW)
        Box(
            modifier = Modifier
                .height(140.dp)
                .width(6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedFraction)
                    .align(Alignment.BottomCenter)
                    .background(activeColor)
            )
        }

        Spacer(Modifier.height(6.dp))

        // 🎚️ Slider
        Slider(
            value = fraction,
            onValueChange = {
                val newGain = (it * range + minGain).toInt()
                onChanged(newGain)
            },
            enabled = isEnabled,
            modifier = Modifier
                .graphicsLayer(rotationZ = -90f)
                .width(120.dp),
        )

        Spacer(Modifier.height(4.dp))

        Text(
            freqLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}