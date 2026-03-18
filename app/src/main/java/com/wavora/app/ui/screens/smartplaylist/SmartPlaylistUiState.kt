package com.wavora.app.ui.screens.smartplaylist

import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Song

/**
 * @author Muhamed Amin Hassan on 18,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class SmartPlaylistUiState(
    val type: SmartPlaylistType = SmartPlaylistType.MOST_PLAYED,
    val songs: AsyncResult<List<Song>> = AsyncResult.Loading
)
