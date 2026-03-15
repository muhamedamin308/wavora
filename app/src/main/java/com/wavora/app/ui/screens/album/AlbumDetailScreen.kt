package com.wavora.app.ui.screens.album

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.core.utils.pluralLabel
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.model.totalDurationFormatted
import com.wavora.app.ui.components.EmptyState
import com.wavora.app.ui.components.LoadingScreen
import com.wavora.app.ui.screens.library.SongListItem
import com.wavora.app.ui.theme.PlaybackAccent

/**
 * @author Muhamed Amin Hassan on 15,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Composable
fun AlbumDetailScreen(
    onNavigateUp: () -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val album = (state.album as? AsyncResult.Success)?.data
    val songs = (state.songs as? AsyncResult.Success)?.data ?: emptyList()

    // Dominant color extracted from album art for the hero gradient
    var dominantColor by remember { mutableStateOf(Color.Transparent) }
    val heroGradient = Brush.verticalGradient(
        colors = listOf(dominantColor.copy(alpha = 0.7f), Color.Transparent),
        endY = 600f,
    )

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        when (state.album) {
            is AsyncResult.Error -> EmptyState(
                title = "Album not found",
                subtitle = "It may have been removed during a library rescan.",
            )

            AsyncResult.Loading -> LoadingScreen()
            is AsyncResult.Success -> LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
                )
            ) {
                // ── Hero header ───────────────────────────────────────────────
                item(key = "hero") {
                    AlbumHero(
                        album = album!!,
                        songs = songs,
                        heroGradient = heroGradient,
                        onNavigateUp = onNavigateUp,
                        onNavigateToArtist = onNavigateToArtist,
                        onColorExtracted = { dominantColor = it },
                        onPlayAll = { viewModel.onPlayAll(shuffle = false) },
                        onShuffle = { viewModel.onPlayAll(shuffle = true) },
                    )
                }

                // ── Song list ─────────────────────────────────────────────────
                itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Track number badge
                        Text(
                            text = if (song.trackNumber > 0) "${song.trackNumber}" else "—",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .width(36.dp)
                                .padding(start = 16.dp),
                        )
                        SongListItem(
                            song = song,
                            onClick = { viewModel.onSongClicked(index) },
                            modifier = Modifier.weight(1f),
                            showAlbumArt = false,
                        )
                    }
                    HorizontalDivider(Modifier.padding(start = 52.dp))
                }
            }
        }
    }
}

@Composable
fun AlbumHero(
    album: Album,
    songs: List<Song>,
    heroGradient: Brush,
    onNavigateUp: () -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onColorExtracted: (Color) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        album.albumArtUri?.let {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.albumArtUri)
                    .allowHardware(false)
                    .size(800)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                onSuccess = { result ->
                    val bmp = (result.result.drawable as? BitmapDrawable)?.bitmap
                    bmp?.let {
                        val palette = Palette.from(it).generate()
                        val swatch = palette.dominantSwatch ?: palette.vibrantSwatch
                        swatch?.rgb?.let { rgb -> onColorExtracted(Color(rgb)) }
                    }
                }
            )
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Album, null, Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Gradient overlay darkening bottom of hero for text legibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(heroGradient)
        )

        // Back button top-left
        IconButton(
            onClick = onNavigateUp,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .align(Alignment.TopStart)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = androidx.compose.foundation.shape.CircleShape,
                ),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    // Metadata + buttons below the art
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            album.title,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        TextButton(
            onClick = { onNavigateToArtist(album.artistId) },
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                album.artistName,
                style = MaterialTheme.typography.titleMedium,
                color = PlaybackAccent
            )
        }

        Text(
            text = buildString {
                if (album.year > 0) append("${album.year} · ")
                append(songs.size.pluralLabel("song"))
                append(" · ")
                append(album.totalDurationFormatted)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Play")
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

        Spacer(Modifier.height(4.dp))
        HorizontalDivider()
    }
}