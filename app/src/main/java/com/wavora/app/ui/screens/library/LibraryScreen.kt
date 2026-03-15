package com.wavora.app.ui.screens.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.core.utils.audioStoragePermission
import com.wavora.app.core.utils.hasAudioPermission
import com.wavora.app.core.utils.pluralLabel
import com.wavora.app.core.utils.toDisplayDuration
import com.wavora.app.domain.model.Song
import com.wavora.app.ui.components.AddToPlaylistBottomSheet
import com.wavora.app.ui.components.EmptyState
import com.wavora.app.ui.components.LoadingScreen
import com.wavora.app.ui.theme.ShapeAlbumArt
import kotlinx.coroutines.launch

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
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
        pageCount = { LibraryTab.entries.size },
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.onPermissionGranted() else viewModel.onPermissionDenied()
    }

    LaunchedEffect(Unit) {
        if (context.hasAudioPermission()) viewModel.onPermissionGranted()
        else permissionLauncher.launch(audioStoragePermission())
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(LibraryTab.entries[pagerState.currentPage])
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.RequestPermission -> permissionLauncher.launch(
                    audioStoragePermission()
                )

                is LibraryEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is LibraryEvent.NavigateToSong -> onNavigateToNowPlaying()
                is LibraryEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WAVORA",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                            ),
                        )
                        if (state.songCount > 0) {
                            Text(
                                text = state.songCount.pluralLabel("song"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    if (state.isScanning) {
                        Box(
                            modifier = Modifier.padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    IconButton(onClick = viewModel::onRescanClicked) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Rescan library")
                    }
                    IconButton(onClick = { /* Phase 6: SortBottomSheet */ }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
            ) {
                LibraryTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(tab.label, style = MaterialTheme.typography.labelLarge) },
                    )
                }
            }

            if (!state.hasStoragePermission) {
                PermissionRationale(onGrantClick = {
                    permissionLauncher.launch(
                        audioStoragePermission()
                    )
                })
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (LibraryTab.entries[page]) {
                        LibraryTab.SONGS -> SongsTab(
                            state,
                            viewModel::onSongClicked,
                            viewModel::onSongLongPressed
                        )

                        LibraryTab.ALBUMS -> AlbumsTab(state, onNavigateToAlbum)
                        LibraryTab.ARTISTS -> ArtistsTab(state, onNavigateToArtist)
                        LibraryTab.FOLDERS -> FoldersTab(state)
                        LibraryTab.PLAYLISTS -> PlaylistsTab(state, onNavigateToPlaylist)
                    }
                }
            }
        }
    }

    // Add to playlist sheet
    state.addToPlaylistSong?.let { song ->
        val playlistList = (state.playlists as? AsyncResult.Success)?.data ?: emptyList()
        AddToPlaylistBottomSheet(
            songTitle = song.title,
            playlists = playlistList,
            onAddToPlaylist = viewModel::onAddSongToPlaylist,
            onCreateAndAdd = viewModel::onCreatePlaylistAndAdd,
            onDismiss = viewModel::onDismissAddToPlaylist
        )
    }
}

// ── Songs ─────────────────────────────────────────────────────────────────────

@Composable
private fun SongsTab(
    state: LibraryUiState,
    onSongClick: (Song) -> Unit,
    onSongLongPress: (Song) -> Unit = {},
) {
    when (val result = state.songs) {
        is AsyncResult.Loading -> LoadingScreen()
        is AsyncResult.Error -> EmptyState(title = "Couldn't load songs", subtitle = result.message)
        is AsyncResult.Success -> if (result.data.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.MusicNote, title = "No songs yet",
                subtitle = "Your music will appear after the library scans your device."
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                items(result.data, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onLongClicked = { onSongLongPress(song) },
                    )
                    HorizontalDivider(Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClicked: (() -> Unit)? = null,
    showAlbumArt: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClicked)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showAlbumArt) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ShapeAlbumArt)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                song.albumArtUri?.let {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song.albumArtUri).size(96).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } ?: run {
                    Icon(
                        Icons.Filled.MusicNote, null, Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                song.title, style = MaterialTheme.typography.titleSmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (showAlbumArt) "${song.artistName} • ${song.albumName}"
                else song.artistName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            song.duration.toDisplayDuration(), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Albums ────────────────────────────────────────────────────────────────────

@Composable
private fun AlbumsTab(state: LibraryUiState, onAlbumClick: (Long) -> Unit) {
    when (val result = state.albums) {
        is AsyncResult.Loading -> LoadingScreen()
        is AsyncResult.Error -> EmptyState(
            title = "Couldn't load albums",
            subtitle = result.message
        )

        is AsyncResult.Success -> if (result.data.isEmpty()) {
            EmptyState(
                title = "No albums yet",
                subtitle = "Albums appear once your library is scanned."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(result.data, key = { it.id }) { album ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(album.id) },
                        shape = ShapeAlbumArt
                    ) {
                        Column {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (album.albumArtUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(album.albumArtUri).size(300).crossfade(true)
                                            .build(),
                                        contentDescription = null, contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.Album, null, Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Column(Modifier.padding(8.dp)) {
                                Text(
                                    album.title, style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    album.artistName, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Artists ───────────────────────────────────────────────────────────────────

@Composable
private fun ArtistsTab(state: LibraryUiState, onArtistClick: (Long) -> Unit) {
    when (val result = state.artists) {
        is AsyncResult.Loading -> LoadingScreen()
        is AsyncResult.Error -> EmptyState(
            title = "Couldn't load artists",
            subtitle = result.message
        )

        is AsyncResult.Success -> if (result.data.isEmpty()) {
            EmptyState(
                title = "No artists yet",
                subtitle = "Artists appear once your library is scanned."
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                items(result.data, key = { it.id }) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onArtistClick(artist.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (artist.thumbnailUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(artist.thumbnailUri).size(96).crossfade(true).build(),
                                    contentDescription = null, contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Person, null, Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                artist.name, style = MaterialTheme.typography.titleSmall,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${artist.albumCount.pluralLabel("album")} • ${
                                    artist.songCount.pluralLabel(
                                        "song"
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

// ── Folders ───────────────────────────────────────────────────────────────────

@Composable
private fun FoldersTab(state: LibraryUiState) {
    when (val result = state.folders) {
        is AsyncResult.Loading -> LoadingScreen()
        is AsyncResult.Error -> EmptyState(
            title = "Couldn't load folders",
            subtitle = result.message
        )

        is AsyncResult.Success -> if (result.data.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Folder, title = "No folders yet",
                subtitle = "Music folders appear once your library is scanned."
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                items(result.data, key = { it.path }) { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Folder, null, Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                folder.name, style = MaterialTheme.typography.titleSmall,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${folder.songCount.pluralLabel("song")} • ${folder.totalDuration.toDisplayDuration()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

// ── Playlists ─────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistsTab(state: LibraryUiState, onPlaylistClick: (Long) -> Unit) {
    when (val result = state.playlists) {
        is AsyncResult.Loading -> LoadingScreen()
        is AsyncResult.Error -> EmptyState(
            title = "Couldn't load playlists",
            subtitle = result.message
        )

        is AsyncResult.Success -> if (result.data.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd, title = "No playlists",
                subtitle = "Create your first playlist to organise your music."
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                items(result.data, key = { it.id }) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaylistClick(playlist.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(ShapeAlbumArt)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic, null, Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                playlist.name, style = MaterialTheme.typography.titleSmall,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                playlist.songCount.pluralLabel("song"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

// ── Permission ────────────────────────────────────────────────────────────────

@Composable
private fun PermissionRationale(onGrantClick: () -> Unit) {
    EmptyState(
        icon = Icons.Filled.FolderOff,
        title = "Permission needed",
        subtitle = "WAVORA needs access to your audio files to build your library. Your music never leaves your device.",
        action = { Button(onClick = onGrantClick) { Text("Grant Permission") } },
    )
}
