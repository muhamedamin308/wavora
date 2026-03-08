package com.wavora.app.ui.screens.playlist

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.wavora.app.ui.components.EmptyState
import com.wavora.app.ui.components.WavoraTopBar

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun PlaylistScreen(
    playlistId: Long,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {

    LaunchedEffect(playlistId) { viewModel.loadPlaylist(playlistId) }

    Scaffold(
        modifier = modifier,
        topBar = { WavoraTopBar(title = "Playlist", onNavigateUp = onNavigateUp) },
    ) { innerPadding ->
        // Phase 4: song list with drag-reorder
        EmptyState(
            title = "Playlist $innerPadding",
            subtitle = "Song list will appear in Phase 4",
        )
    }
}