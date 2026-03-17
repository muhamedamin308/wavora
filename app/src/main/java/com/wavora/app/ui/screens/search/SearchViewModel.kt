package com.wavora.app.ui.screens.search

import androidx.lifecycle.viewModelScope
import com.wavora.app.core.base.BaseViewModel
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.core.utils.Constants
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.interfaces.MusicRepository
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import com.wavora.app.domain.repository.results.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class SearchUiState(
    val query: String = "",
    val results: AsyncResult<SearchResult> = AsyncResult.Success(SearchResult()),
    val isSearching: Boolean = false,
)

sealed interface SearchEvent {
    data class NavigateToAlbum(val albumId: Long) : SearchEvent
    data class NavigateToArtist(val artistId: Long) : SearchEvent
    data object NavigateToNowPlaying : SearchEvent
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playerRepository: PlayerRepository,
) : BaseViewModel<SearchUiState, SearchEvent>(SearchUiState()) {

    private val queryFlow = MutableStateFlow("")

    init {
        queryFlow
            .debounce(Constants.SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { query ->
                // Show spinner immediately when query crosses the threshold
                if (query.length >= Constants.SEARCH_MIN_CHARS)
                    updateState { copy(isSearching = true) }
            }
            .flatMapLatest { query ->
                if (query.length < Constants.SEARCH_MIN_CHARS)
                    flowOf(SearchResult())
                else
                    musicRepository.search(query)
                        .catch { emit(SearchResult()) }
            }
            .onEach { result ->
                updateState {
                    copy(
                        results = AsyncResult.Success(result),
                        isSearching = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        updateState { copy(query = query) }
        queryFlow.value = query
    }

    fun clearQuery() = onQueryChanged("")

    fun onSongClicked(song: Song) = safeLaunch {
        playerRepository.play(song)
        emitEvent(SearchEvent.NavigateToNowPlaying)
    }

    fun onAlbumClicked(albumId: Long) = emitEvent(SearchEvent.NavigateToAlbum(albumId))
    fun onArtistClicked(artistId: Long) = emitEvent(SearchEvent.NavigateToArtist(artistId))
}
