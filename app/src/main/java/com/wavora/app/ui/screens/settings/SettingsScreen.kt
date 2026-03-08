package com.wavora.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.NavigateToEqualizer -> onNavigateToEqualizer()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            WavoraTopBar(title = "Settings")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Appearance
            item { SettingsSectionHeader("Appearance") }

            item {
                SettingsToggleRow(
                    icon = Icons.Filled.DarkMode,
                    title = "Dark theme",
                    subtitle = "OLED-optimised true black",
                    checked = state.isDarkTheme,
                    onToggle = viewModel::onDarkThemeToggle,
                )
            }

            item {
                SettingsToggleRow(
                    icon = Icons.Filled.Palette,
                    title = "Material You (Dynamic color)",
                    subtitle = "Match album art & wallpaper colors",
                    checked = state.useDynamicColor,
                    onToggle = viewModel::onDynamicColorToggle,
                )
            }

            // Playback
            item { SettingsSectionHeader("Playback") }

            item {
                SettingsToggleRow(
                    icon = Icons.Filled.BlurCircular,
                    title = "Gapless playback",
                    subtitle = "Remove silence between tracks",
                    checked = state.gaplessPlayback,
                    onToggle = viewModel::onGaplessToggle,
                )
            }

            item {
                SettingsClickRow(
                    icon = Icons.Filled.Equalizer,
                    title = "Equalizer",
                    subtitle = "Adjust bass, treble and audio presets",
                    onClick = viewModel::onEqualizerClick,
                )
            }

            // Library
            item { SettingsSectionHeader("Library") }

            item {
                SettingsClickRow(
                    icon = Icons.Filled.FolderOpen,
                    title = "Music folders",
                    subtitle = state.libraryPath,
                    onClick = { /* Phase 6: folder picker */ },
                )
            }

            // Lock-Screen
            item { SettingsSectionHeader("Lock Screen") }

            item {
                SettingsToggleRow(
                    icon = Icons.Filled.LockOpen,
                    title = "Show album art on lock screen",
                    subtitle = "Displays album art in media controls",
                    checked = state.showAlbumArtOnLockScreen,
                    onToggle = viewModel::onLockScreenArtToggle,
                )
            }

            // About
            item { SettingsSectionHeader("About") }

            item {
                SettingsInfoRow(
                    icon = Icons.Filled.Info,
                    title = "Version",
                    value = state.appVersion,
                )
            }
        }
    }
}

// Settings Row Composables
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
