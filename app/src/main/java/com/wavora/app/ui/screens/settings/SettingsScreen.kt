package com.wavora.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.BlurOff
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavora.app.BuildConfig
import com.wavora.app.ui.components.WavoraTopBar

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToEqualizer: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = state.prefs

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.NavigateToEqualizer -> onNavigateToEqualizer()
            }
        }
    }

    // dialog state for multi-options picker
    var showSkipDurationPicker by remember { mutableStateOf(false) }
    var showCrossfadePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            WavoraTopBar(title = "Settings")
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
        ) {
            // Appearance
            item { SettingsSectionHeader("Appearance") }

            item {
                SwitchSettingRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark theme",
                    checked = prefs.isDarkTheme,
                    onCheckedChange = viewModel::onDarkThemeToggle,
                )
            }

            item {
                SwitchSettingRow(
                    icon = Icons.Outlined.Palette,
                    title = "Dynamic colour",
                    subtitle = "Use your wallpaper colours (Android 12+)",
                    checked = prefs.useDynamicColors,
                    onCheckedChange = viewModel::onDynamicColorToggle,
                )
            }

            // Playback
            item { SettingsSectionHeader("Playback") }

            item {
                SwitchSettingRow(
                    icon = Icons.Outlined.BlurOff,
                    title = "Gapless playback",
                    subtitle = "Remove silence between tracks",
                    checked = prefs.gaplessPlayback,
                    onCheckedChange = viewModel::onGaplessToggle,
                )
            }

            item {
                ClickableSettingRow(
                    icon = Icons.Outlined.FastForward,
                    title = "Skip duration",
                    subtitle = "${prefs.skipDurationSec}s per tap",
                    onClick = { showSkipDurationPicker = true },
                )
            }

            item {
                ClickableSettingRow(
                    icon = Icons.Outlined.Tune,
                    title = "Crossfade",
                    subtitle = if (prefs.crossfadeDurationMs == 0) "Disabled"
                    else "${prefs.crossfadeDurationMs / 1000}s",
                    onClick = { showCrossfadePicker = true },
                )
            }

            item {
                ClickableSettingRow(
                    icon = Icons.Outlined.Equalizer,
                    title = "Equalizer",
                    subtitle = "Phase 7",
                    onClick = viewModel::onEqualizerClick,
                )
            }

            // Lock-Screen
            item { SettingsSectionHeader("Lock Screen") }

            item {
                SwitchSettingRow(
                    icon = Icons.Outlined.Lock,
                    title = "Show album art",
                    subtitle = "Display artwork on the lock screen",
                    checked = prefs.showAlbumArtOnLockScreen,
                    onCheckedChange = viewModel::onLockScreenArtToggle,
                )
            }

            // About
            item { SettingsSectionHeader("About") }

            item {
                ClickableSettingRow(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = BuildConfig.VERSION_NAME,
                    onClick = {},
                )
            }
        }
    }

    // ── Pickers ───────────────────────────────────────────────────────────────

    if (showSkipDurationPicker) {
        OptionPickerDialog(
            title = "Skip duration",
            options = listOf(5, 10, 15, 20, 30),
            selected = prefs.skipDurationSec,
            labelFor = { "${it}s" },
            onSelect = { viewModel.onSkipDurationSelected(it); showSkipDurationPicker = false },
            onDismiss = { showSkipDurationPicker = false },
        )
    }

    if (showCrossfadePicker) {
        OptionPickerDialog(
            title = "Crossfade",
            options = listOf(0, 1000, 2000, 3000, 5000),
            selected = prefs.crossfadeDurationMs,
            labelFor = { if (it == 0) "Disabled" else "${it / 1000}s" },
            onSelect = { viewModel.onCrossfadeSelected(it); showCrossfadePicker = false },
            onDismiss = { showCrossfadePicker = false },
        )
    }
}

// Settings Row Composables
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
fun SwitchSettingRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ClickableSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.ChevronRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun <T> OptionPickerDialog(
    title: String,
    options: List<T>,
    selected: T,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(labelFor(option), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}