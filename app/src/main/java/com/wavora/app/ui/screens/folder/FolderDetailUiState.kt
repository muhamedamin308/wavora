package com.wavora.app.ui.screens.folder

import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Song

/**
 * @author Muhamed Amin Hassan on 15,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class FolderDetailUiState(
    val folderPath: String = "",
    val songs: AsyncResult<List<Song>> = AsyncResult.Loading,
)