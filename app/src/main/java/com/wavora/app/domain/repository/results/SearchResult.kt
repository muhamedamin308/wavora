package com.wavora.app.domain.repository.results

import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Artist
import com.wavora.app.domain.model.Song

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
data class SearchResult(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
) {
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty()

    val totalCount: Int
        get() = songs.size + artists.size + albums.size
}
