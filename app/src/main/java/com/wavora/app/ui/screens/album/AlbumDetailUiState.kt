package com.wavora.app.ui.screens.album

import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Song

/**
 * @author Muhamed Amin Hassan on 15,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class AlbumDetailUiState(
    val album: AsyncResult<Album> = AsyncResult.Loading,
    val songs: AsyncResult<List<Song>> = AsyncResult.Loading
)