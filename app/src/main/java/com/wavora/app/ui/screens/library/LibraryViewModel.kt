package com.wavora.app.ui.screens.library

import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.model.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * ViewModel for the main library screen.
 *
 * Phase 1: State machine skeleton only.
 * Phase 2: [MusicRepository] injected; real data flows wired.
 *
 * Intentionally uses constructor injection pattern even in the stub phase —
 * this means Phase 2 only needs to uncomment / add code, not refactor structure.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    // MusicRepository injected in Phase 2:
    // private val musicRepository: MusicRepository,
    // private val playlistRepository: PlaylistRepository,
) : BaseViewModel<LibraryUiState, LibraryEvent>(LibraryUiState()) {

    fun onTabSelected(tab: LibraryTab) {
        updateState { copy(selectedTab = tab) }
    }

    fun onSortOrderChanged(sortOrder: SortOrder) {
        updateState { copy(sortOrder = sortOrder) }
        // Phase 2: re-trigger data load with new sort
    }

    fun onPermissionGranted() {
        updateState { copy(hasStoragePermission = true) }
        // Phase 2: trigger library scan
    }

    fun onPermissionDenied() {
        updateState { copy(hasStoragePermission = false) }
        emitEvent(LibraryEvent.ShowError("Storage permission is required to show your music."))
    }

    fun onSongClicked(song: Song) {
        emitEvent(LibraryEvent.NavigateToSong(song))
        // Phase 3: route to PlayerRepository.play(song)
    }
}