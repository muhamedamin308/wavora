package com.wavora.app.ui.screens.artist

import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Artist
import com.wavora.app.domain.model.Song

/**
 * @author Muhamed Amin Hassan on 15,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class ArtistDetailUiState(
    val artist: AsyncResult<Artist> = AsyncResult.Loading,
    val albums: AsyncResult<List<Album>> = AsyncResult.Loading,
    val songs: AsyncResult<List<Song>>  = AsyncResult.Loading,
)