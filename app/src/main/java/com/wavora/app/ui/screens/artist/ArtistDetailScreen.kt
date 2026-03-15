package com.wavora.app.ui.screens.artist

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.wavora.app.core.utils.pluralLabel
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Artist
import com.wavora.app.ui.components.EmptyState
import com.wavora.app.ui.components.LoadingScreen
import com.wavora.app.ui.screens.library.SongListItem
import com.wavora.app.ui.theme.ShapeAlbumArt

/**
 * @author Muhamed Amin Hassan on 15,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun ArtistDetailScreen(
    onNavigateUp: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val artist = (state.artist as? AsyncResult.Success)?.data
    val albums = (state.albums as? AsyncResult.Success)?.data ?: emptyList()
    val songs = (state.songs as? AsyncResult.Success)?.data ?: emptyList()

    Scaffold(modifier = modifier) { innerPadding ->
        when (state.artist) {
            is AsyncResult.Loading -> LoadingScreen()
            is AsyncResult.Error -> EmptyState(title = "Artist not found")
            is AsyncResult.Success -> LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                ),
            ) {
                // ── Artist header ─────────────────────────────────────────────
                item(key = "header") {
                    ArtistHeader(
                        artist = artist!!,
                        songCount = songs.size,
                        albumCount = albums.size,
                        onNavigateUp = onNavigateUp,
                        onPlayAll = { viewModel.onPlayAll(false) },
                        onShuffle = { viewModel.onPlayAll(true) },
                    )
                }

                // Artist Row
                if (albums.isNotEmpty()) {
                    item(key = "albums_header") {
                        Text(
                            "Albums",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
                        )
                    }
                    item(key = "albums_row") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(albums, key = { it.id }) { album ->
                                AlbumCard(
                                    album = album,
                                    onClick = { onNavigateToAlbum(album.id) },
                                )
                            }
                        }
                    }
                }

                // ── Songs section ─────────────────────────────────────────────
                item(key = "songs_header") {
                    Text(
                        "Songs",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
                    )
                }

                itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                    SongListItem(
                        song = song,
                        onClick = { viewModel.onSongClicked(index) },
                    )
                    HorizontalDivider(Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

@Composable
private fun ArtistHeader(
    artist: Artist,
    songCount: Int,
    albumCount: Int,
    onNavigateUp: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top navigation row with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Artist avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (artist.thumbnailUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artist.thumbnailUri)
                            .size(240)
                            .crossfade(true)
                            .build(),
                        contentDescription = artist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                } else {
                    Icon(
                        Icons.Filled.Person, null,
                        Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                artist.name,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${albumCount.pluralLabel("album")} · ${songCount.pluralLabel("song")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onPlayAll,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Play all")
                }
                OutlinedButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Shuffle, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Shuffle")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(Modifier.padding(horizontal = 20.dp))
    }
}

@Composable
private fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        shape = ShapeAlbumArt,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                album.albumArtUri?.let {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.albumArtUri)
                            .size(280)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } ?: run {
                    Icon(
                        Icons.Filled.Album, null, Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    album.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (album.year > 0) "${album.year}" else "Unknown year",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}