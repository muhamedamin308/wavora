package com.wavora.app.ui.screens.library

import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Artist
import com.wavora.app.domain.model.MusicFolder
import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.model.SortOrder
import com.wavora.app.domain.repository.interfaces.MusicRepository
import com.wavora.app.domain.repository.interfaces.PlaylistRepository
import com.wavora.app.worker.LibraryScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val workManager: WorkManager,
) : BaseViewModel<LibraryUiState, LibraryEvent>(LibraryUiState()) {

    init {
        musicRepository.getTotalSongCount()
            .onEach { count -> updateState { copy(songCount = count) } }
            .catch { }
            .launchIn(viewModelScope)
    }

    fun onTabSelected(tab: LibraryTab) {
        updateState { copy(selectedTab = tab) }
    }

    fun onSortOrderChanged(sortOrder: SortOrder) {
        updateState { copy(sortOrder = sortOrder) }
        observeSongsWithCurrentSort()
    }

    fun onPermissionGranted() {
        updateState { copy(hasStoragePermission = true) }
        triggerInitialScan()
        startObservingLibrary()
    }

    fun onPermissionDenied() {
        updateState { copy(hasStoragePermission = false) }
        emitEvent(LibraryEvent.ShowError("Storage permission is required to show your music."))
    }

    fun onSongClicked(song: Song) {
        emitEvent(LibraryEvent.NavigateToSong(song))
    }

    fun onRescanClicked() = triggerInitialScan()

    fun onSongLongPressed(song: Song) = updateState { copy(addToPlaylistSong = song) }
    fun onDismissAddToPlaylist() = updateState { copy(addToPlaylistSong = null) }

    fun onAddSongToPlaylist(playlistId: Long) = safeLaunch {
        val song = currentState.addToPlaylistSong ?: return@safeLaunch
        playlistRepository.addSongToPlaylist(playlistId, song.id)
        updateState { copy(addToPlaylistSong = null) }
        emitEvent(LibraryEvent.ShowSnackbar("Added to playlist"))
    }

    fun onCreatePlaylistAndAdd(name: String) = safeLaunch {
        val song = currentState.addToPlaylistSong ?: return@safeLaunch
        val newId = playlistRepository.createPlaylist(name)
        playlistRepository.addSongToPlaylist(newId, song.id)
        updateState { copy(addToPlaylistSong = null) }
        emitEvent(LibraryEvent.ShowSnackbar("Added to \"$name\""))
    }

    private fun triggerInitialScan() {
        updateState { copy(isScanning = true) }
        LibraryScanWorker.enqueueOneTimeScan(workManager)
        LibraryScanWorker.schedulePeriodicScan(workManager)
    }

    private fun startObservingLibrary() {
        observeSongsWithCurrentSort()
        observeAlbums()
        observeArtists()
        observeFolders()
        observePlaylists()
    }

    private fun observeSongsWithCurrentSort() {
        musicRepository.getAllSongs(currentState.sortOrder)
            .map<List<Song>, AsyncResult<List<Song>>> { AsyncResult.Success(it) }
            .onStart { updateState { copy(songs = AsyncResult.Loading) } }
            .catch { exception ->
                updateState {
                    copy(
                        songs = AsyncResult.Error(
                            exception.message ?: "Error"
                        )
                    )
                }
            }
            .onEach { result -> updateState { copy(songs = result, isScanning = false) } }
            .launchIn(viewModelScope)
    }

    private fun observeArtists() {
        musicRepository.getAllArtists()
            .map<List<Artist>, AsyncResult<List<Artist>>> { AsyncResult.Success(it) }
            .onStart { updateState { copy(artists = AsyncResult.Loading) } }
            .catch { e -> updateState { copy(artists = AsyncResult.Error(e.message ?: "Error")) } }
            .onEach { result -> updateState { copy(artists = result) } }
            .launchIn(viewModelScope)
    }

    private fun observeAlbums() {
        musicRepository.getAllAlbums()
            .map<List<Album>, AsyncResult<List<Album>>> { AsyncResult.Success(it) }
            .onStart { updateState { copy(albums = AsyncResult.Loading) } }
            .catch { e -> updateState { copy(albums = AsyncResult.Error(e.message ?: "Error")) } }
            .onEach { result -> updateState { copy(albums = result) } }
            .launchIn(viewModelScope)
    }

    private fun observeFolders() {
        musicRepository.getAllFolders()
            .map<List<MusicFolder>, AsyncResult<List<MusicFolder>>> { AsyncResult.Success(it) }
            .onStart { updateState { copy(folders = AsyncResult.Loading) } }
            .catch { e -> updateState { copy(folders = AsyncResult.Error(e.message ?: "Error")) } }
            .onEach { result -> updateState { copy(folders = result) } }
            .launchIn(viewModelScope)
    }

    private fun observePlaylists() {
        playlistRepository.getAllPlaylists()
            .map<List<Playlist>, AsyncResult<List<Playlist>>> { AsyncResult.Success(it) }
            .onStart { updateState { copy(playlists = AsyncResult.Loading) } }
            .catch { e ->
                updateState {
                    copy(
                        playlists = AsyncResult.Error(
                            e.message ?: "Error"
                        )
                    )
                }
            }
            .onEach { result -> updateState { copy(playlists = result) } }
            .launchIn(viewModelScope)
    }
}