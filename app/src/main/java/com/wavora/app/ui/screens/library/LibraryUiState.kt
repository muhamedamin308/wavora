package com.wavora.app.ui.screens.library

import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Artist
import com.wavora.app.domain.model.MusicFolder
import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.model.SortOrder

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
/**
 * Complete UI state for [LibraryScreen].
 * Each tab has its own [AsyncResult] so tabs can load independently.
 */
data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.SONGS,
    val songs: AsyncResult<List<Song>> = AsyncResult.Loading,
    val albums: AsyncResult<List<Album>> = AsyncResult.Loading,
    val artists: AsyncResult<List<Artist>> = AsyncResult.Loading,
    val folders: AsyncResult<List<MusicFolder>> = AsyncResult.Loading,
    val playlists: AsyncResult<List<Playlist>> = AsyncResult.Loading,
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val hasStoragePermission: Boolean = false,
    val isScanning: Boolean = false,
    val songCount: Int = 0,
)

enum class LibraryTab(val label: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    FOLDERS("Folders"),
    PLAYLISTS("Playlists"),
}