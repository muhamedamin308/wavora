package com.wavora.app.ui.screens.search

import androidx.lifecycle.ViewModel
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.results.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class SearchUiState(
    val query: String = "",
    val results: AsyncResult<SearchResult> = AsyncResult.Success(SearchResult()),
)

sealed interface SearchEvent {
    data class NavigateToAlbum(val albumId: Long) : SearchEvent
    data class NavigateToArtist(val artistId: Long) : SearchEvent
    data class PlaySong(val song: Song) : SearchEvent
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    // Injected in Phase 2: private val musicRepository: MusicRepository,
) : BaseViewModel<SearchUiState, SearchEvent>(SearchUiState()) {

    private val queryFlow = MutableStateFlow("")

    init {
        // Phase 2: wire debounced search to Room FTS4 query
        // safeLaunch {
        //     queryFlow
        //         .debounce(Constants.SEARCH_DEBOUNCE_MS)
        //         .distinctUntilChanged()
        //         .filter { it.length >= Constants.SEARCH_MIN_CHARS || it.isEmpty() }
        //         .flatMapLatest { q ->
        //             if (q.isEmpty()) flowOf(SearchResult())
        //             else musicRepository.search(q)
        //         }
        //         .collect { result -> updateState { copy(results = AsyncResult.Success(result)) } }
        // }
    }

    fun onQueryChanged(query: String) {
        updateState { copy(query = query) }
        queryFlow.value = query
    }

    fun clearQuery() = onQueryChanged("")

    fun onSongClicked(song: Song)     = emitEvent(SearchEvent.PlaySong(song))
    fun onAlbumClicked(albumId: Long) = emitEvent(SearchEvent.NavigateToAlbum(albumId))
    fun onArtistClicked(artistId: Long) = emitEvent(SearchEvent.NavigateToArtist(artistId))
}
