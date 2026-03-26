package com.wavora.app.ui.screens.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.core.utils.Constants
import com.wavora.app.core.utils.audioStoragePermission
import com.wavora.app.core.utils.hasAudioPermission
import com.wavora.app.core.utils.pluralLabel
import com.wavora.app.core.utils.toDisplayDuration
import com.wavora.app.domain.model.Song
import com.wavora.app.ui.components.AddToPlaylistBottomSheet
import com.wavora.app.ui.components.EmptyState
import com.wavora.app.ui.components.LoadingScreen
import com.wavora.app.ui.screens.smartplaylist.SmartPlaylistType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedLibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    onNavigateToSmartPlaylist: (String) -> Unit,
    onNavigateToFolder: (String) -> Unit = {},
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

    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.RequestPermission -> permissionLauncher.launch(
                    audioStoragePermission()
                )

                is LibraryEvent.ShowError -> snackBarHostState.showSnackbar(event.message)
                is LibraryEvent.NavigateToSong -> onNavigateToNowPlaying()
                is LibraryEvent.ShowSnackbar -> snackBarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            EnhancedTopBar(
                songCount = state.songCount,
                isScanning = state.isScanning,
                onRescanClick = viewModel::onRescanClicked,
                onSortClick = { /* Phase 6: SortBottomSheet */ }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EnhancedTabRow(
                currentPage = pagerState.currentPage,
                onTabClick = { index -> scope.launch { pagerState.animateScrollToPage(index) } }
            )

            if (!state.hasStoragePermission) {
                EnhancedPermissionRationale(onGrantClick = {
                    permissionLauncher.launch(audioStoragePermission())
                })
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (LibraryTab.entries[page]) {
                        LibraryTab.SONGS -> EnhancedSongsTab(
                            state,
                            viewModel::onSongClicked,
                            viewModel::onSongLongPressed
                        )

                        LibraryTab.ALBUMS -> EnhancedAlbumsTab(state, onNavigateToAlbum)
                        LibraryTab.ARTISTS -> EnhancedArtistsTab(state, onNavigateToArtist)
                        LibraryTab.FOLDERS -> EnhancedFoldersTab(state, onNavigateToFolder)
                        LibraryTab.PLAYLISTS -> EnhancedPlaylistsTab(
                            state,
                            onNavigateToPlaylist,
                            onNavigateToSmartPlaylist
                        )
                    }
                }
            }
        }
    }

    // Enhanced Add to Playlist Sheet
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

// ══════════════════════════════════════════════════════════════════════════════
// Enhanced Top Bar
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EnhancedTopBar(
    songCount: Int,
    isScanning: Boolean,
    onRescanClick: () -> Unit,
    onSortClick: () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = if (isScanning) 4.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Constants.APP_NAME,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    )

                    // Animated scanning indicator
                    AnimatedVisibility(
                        visible = isScanning,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PulsingDot()
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Scanning...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = songCount > 0) {
                    Text(
                        text = songCount.pluralLabel("song"),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AnimatedIconButton(
                    icon = Icons.Filled.Refresh,
                    contentDescription = "Rescan library",
                    onClick = onRescanClick,
                    isLoading = isScanning
                )
                AnimatedIconButton(
                    icon = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort",
                    onClick = onSortClick
                )
            }
        }
    }
}

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(scale)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
}

@Composable
private fun AnimatedIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
) {
    var pressed by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = if (isLoading) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else tween(300),
        label = "rotation"
    )

    FilledIconButton(
        onClick = {
            pressed = true
            onClick()
        },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.graphicsLayer {
            rotationZ = rotation
            scaleX = if (pressed) 0.9f else 1f
            scaleY = if (pressed) 0.9f else 1f
        }
    ) {
        Icon(icon, contentDescription)
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(100)
            pressed = false
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Enhanced Tab Row
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EnhancedTabRow(
    currentPage: Int,
    onTabClick: (Int) -> Unit,
) {
    Surface(
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        ScrollableTabRow(
            selectedTabIndex = currentPage,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentPage]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            LibraryTab.entries.forEachIndexed { index, tab ->
                val selected = currentPage == index

                Tab(
                    selected = selected,
                    onClick = { onTabClick(index) },
                    text = {
                        Text(
                            tab.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Extension to get icon for each tab
private val LibraryTab.icon: ImageVector
    get() = when (this) {
        LibraryTab.SONGS -> Icons.Filled.MusicNote
        LibraryTab.ALBUMS -> Icons.Filled.Album
        LibraryTab.ARTISTS -> Icons.Filled.Person
        LibraryTab.FOLDERS -> Icons.Filled.Folder
        LibraryTab.PLAYLISTS -> Icons.AutoMirrored.Filled.QueueMusic
    }

// ══════════════════════════════════════════════════════════════════════════════
// Enhanced Songs Tab
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EnhancedSongsTab(
    state: LibraryUiState,
    onSongClick: (Song) -> Unit,
    onSongLongPress: (Song) -> Unit = {},
) {
    when (val result = state.songs) {
        is AsyncResult.Loading -> LoadingScreen()
        is AsyncResult.Error -> EmptyState(
            title = "Couldn't load songs",
            subtitle = result.message
        )

        is AsyncResult.Success -> if (result.data.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.MusicNote,
                title = "No songs yet",
                subtitle = "Your music will appear after the library scans your device."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(
                    items = result.data,
                    key = { it.id }
                ) { song ->
                    EnhancedSongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onLongClicked = { onSongLongPress(song) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedSongListItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClicked: (() -> Unit)? = null,
    showAlbumArt: Boolean = true,
) {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "songScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else Color.Transparent,
        animationSpec = tween(150),
        label = "songBg"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = if (isPressed) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        isPressed = true
                        onClick()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClicked?.invoke()
                    }
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showAlbumArt) {
                EnhancedAlbumArtThumbnail(
                    albumArtUri = song.albumArtUri,
                    size = 52.dp,
                    cornerRadius = 8.dp
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (showAlbumArt) "${song.artistName} • ${song.albumName}"
                    else song.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    song.duration.toDisplayDuration(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedAlbumArtThumbnail(
    albumArtUri: String?,
    size: Dp,
    cornerRadius: Dp = 8.dp,
) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(cornerRadius),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            albumArtUri?.let {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(albumArtUri)
                        .size(size.value.toInt() * 2)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } ?: Icon(
                Icons.Filled.MusicNote,
                null,
                Modifier.size(size * 0.5f),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Enhanced Albums Tab
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EnhancedAlbumsTab(state: LibraryUiState, onAlbumClick: (Long) -> Unit) {
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
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = result.data,
                    key = { it.id }
                ) { album ->
                    EnhancedAlbumCard(
                        album = album,
                        onClick = { onAlbumClick(album.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedAlbumCard(
    album: com.wavora.app.domain.model.Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "albumScale"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column {
            // Album art with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (album.albumArtUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(album.albumArtUri)
                                .size(400)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Filled.Album,
                            null,
                            Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }

                // Bottom gradient overlay for better text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
            }

            // Album info
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        album.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        album.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Enhanced Artists Tab
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EnhancedArtistsTab(state: LibraryUiState, onArtistClick: (Long) -> Unit) {
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(
                    items = result.data,
                    key = { it.id }
                ) { artist ->
                    EnhancedArtistListItem(
                        artist = artist,
                        onClick = { onArtistClick(artist.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedArtistListItem(
    artist: com.wavora.app.domain.model.Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "artistScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isPressed)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else Color.Transparent,
        onClick = {
            isPressed = true
            onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist avatar with ring
            Box {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (artist.thumbnailUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(artist.thumbnailUri)
                                    .size(112)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Filled.Person,
                                null,
                                Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Ring effect
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    artist.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            artist.albumCount.pluralLabel("album"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        artist.songCount.pluralLabel("song"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Enhanced Folders Tab
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EnhancedFoldersTab(state: LibraryUiState, onFolderClick: (String) -> Unit = {}) {
    when (val result = state.folders) {
        is AsyncResult.Loading -> LoadingScreen()
        is AsyncResult.Error -> EmptyState(
            title = "Couldn't load folders",
            subtitle = result.message
        )

        is AsyncResult.Success -> if (result.data.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Folder,
                title = "No folders yet",
                subtitle = "Music folders appear once your library is scanned."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(
                    items = result.data,
                    key = { it.path }
                ) { folder ->
                    EnhancedFolderListItem(
                        folder = folder,
                        onClick = { onFolderClick(folder.path) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedFolderListItem(
    folder: com.wavora.app.domain.model.MusicFolder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "folderScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isPressed)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else Color.Transparent,
        onClick = {
            isPressed = true
            onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Folder,
                        null,
                        Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    folder.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${folder.songCount.pluralLabel("song")} • ${folder.totalDuration.toDisplayDuration()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Enhanced Playlists Tab
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EnhancedPlaylistsTab(
    state: LibraryUiState,
    onPlaylistClick: (Long) -> Unit,
    onSmartPlaylistClick: (String) -> Unit = {},
) {
    val playlists = (state.playlists as? AsyncResult.Success)?.data ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Smart playlists section
        item(key = "smart_header") {
            SectionHeader(
                title = "Smart Playlists",
                icon = Icons.Filled.AutoAwesome
            )
        }

        item(key = "smart_grid") {
            EnhancedSmartPlaylistGrid(onSmartPlaylistClick)
        }

        // User playlists section
        item(key = "user_header") {
            SectionHeader(
                title = "My Playlists",
                icon = Icons.AutoMirrored.Filled.QueueMusic
            )
        }

        if (playlists.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    modifier = Modifier.height(160.dp),
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    title = "No playlists yet",
                    subtitle = "Long-press any song to add it to a new playlist",
                )
            }
        } else {
            items(
                items = playlists,
                key = { it.id }
            ) { playlist ->
                EnhancedPlaylistListItem(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist.id) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, bottom = 12.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EnhancedSmartPlaylistGrid(onSmartPlaylistClick: (String) -> Unit) {
    val items = SmartPlaylistType.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { type ->
                    EnhancedSmartPlaylistCard(
                        type = type,
                        onClick = { onSmartPlaylistClick(type.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EnhancedSmartPlaylistCard(
    type: SmartPlaylistType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "smartPlaylistScale"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        type.icon,
                        null,
                        Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                type.displayName,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedPlaylistListItem(
    playlist: com.wavora.app.domain.model.Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "playlistScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isPressed)
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        else Color.Transparent,
        onClick = {
            isPressed = true
            onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        null,
                        Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    playlist.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    playlist.songCount.pluralLabel("song"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Enhanced Permission Rationale
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EnhancedPermissionRationale(onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon
        val infiniteTransition = rememberInfiniteTransition(label = "permission")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "iconScale"
        )

        Icon(
            Icons.Filled.FolderOff,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            "Storage Permission Required",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Wavora needs access to your audio files to build your music library. Your music stays private and never leaves your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onGrantClick,
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Lock, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Grant Permission",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}