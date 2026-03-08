package com.wavora.app.ui.screens.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavora.app.core.utils.Constants
import com.wavora.app.core.utils.audioStoragePermission
import com.wavora.app.core.utils.hasAudioPermission
import com.wavora.app.ui.components.EmptyState
import kotlinx.coroutines.launch

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Main library screen — the app's home destination.
 *
 * Structure:
 *  ┌─────────────────────────────┐
 *  │   "WAVORA" TopAppBar        │
 *  ├─────────────────────────────┤
 *  │   TabRow (5 tabs)           │
 *  ├─────────────────────────────┤
 *  │   HorizontalPager           │
 *  │    └─ active tab content    │
 *  └─────────────────────────────┘
 *
 * Phase 1: Permission handling + empty states + tab navigation fully wired.
 * Phase 2: Tab content lists populated from real data.
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToNowPlaying: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = state.selectedTab.ordinal,
        pageCount = { LibraryTab.entries.size }
    )

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.onPermissionGranted()
        else viewModel.onPermissionDenied()
    }

    // check permission on first composition
    LaunchedEffect(Unit) {
        if (context.hasAudioPermission())
            viewModel.onPermissionGranted()
        else
            permissionLauncher.launch(audioStoragePermission())
    }

    // Sync Pager with viewModel tab selected
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(LibraryTab.entries[pagerState.currentPage])
    }

    // Consume one-shot events
    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.NavigateToSong -> onNavigateToNowPlaying()
                LibraryEvent.RequestPermission -> permissionLauncher.launch(audioStoragePermission())
                is LibraryEvent.ShowError -> snackBarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            LibraryTopBar(
                onSortClick = { /* Phase 6: show SortBottomSheet */ },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ── Tab row ───────────────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
            ) {
                LibraryTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                            viewModel.onTabSelected(tab)
                        },
                        text = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }

            // ── Pager content ─────────────────────────────────────────────────
            if (!state.hasStoragePermission) {
                PermissionRationale(
                    onGrantClick = { permissionLauncher.launch(audioStoragePermission()) },
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    when (LibraryTab.entries[page]) {
                        LibraryTab.SONGS -> SongsTabContent(state)
                        LibraryTab.ALBUMS -> AlbumsTabContent(state, onNavigateToAlbum)
                        LibraryTab.ARTISTS -> ArtistsTabContent(state, onNavigateToArtist)
                        LibraryTab.FOLDERS -> FoldersTabContent(state)
                        LibraryTab.PLAYLISTS -> PlaylistsTabContent(state, onNavigateToPlaylist)
                    }
                }
            }
        }
    }
}

// Top bar

@Composable
private fun LibraryTopBar(
    onSortClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = Constants.APP_NAME,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
            )
        },
        actions = {
            IconButton(
                onClick = onSortClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sort,
                    contentDescription = "Sort"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// Tab content composables  (populated with real data in Phase 2)
@Composable
private fun SongsTabContent(state: LibraryUiState) {
    EmptyState(
        icon = Icons.Filled.MusicNote,
        title = "No songs yet",
        subtitle = "Your music will appear here after the library scans your device.",
    )
}

@Composable
private fun AlbumsTabContent(
    state: LibraryUiState,
    onAlbumClick: (Long) -> Unit,
) {
    EmptyState(
        title = "No albums yet",
        subtitle = "Albums will appear once your library is scanned."
    )
}

@Composable
private fun ArtistsTabContent(
    state: LibraryUiState,
    onArtistClick: (Long) -> Unit,
) {
    EmptyState(
        title = "No artists yet",
        subtitle = "Artists will appear once your library is scanned."
    )
}

@Composable
private fun FoldersTabContent(state: LibraryUiState) {
    EmptyState(
        title = "No folders yet",
        subtitle = "Folders will appear once your library is scanned."
    )
}

@Composable
private fun PlaylistsTabContent(
    state: LibraryUiState,
    onPlaylistClick: (Long) -> Unit,
) {
    EmptyState(
        title = "No playlists",
        subtitle = "Tap + to create your first playlist.",
    )
}

// Permission rationale
@Composable
private fun PermissionRationale(
    onGrantClick: () -> Unit,
) {
    EmptyState(
        title = "Permission needed",
        subtitle = "WAVORA needs access to your audio files to build your library. Your music never leaves your device.",
        action = {
            Button(onClick = onGrantClick) {
                Text("Grant Permission")
            }
        },
    )
}