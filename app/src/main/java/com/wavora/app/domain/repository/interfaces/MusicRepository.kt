package com.wavora.app.domain.repository.interfaces

import android.app.appsearch.SearchResult
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Artist
import com.wavora.app.domain.model.MusicFolder
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.model.SortOrder
import com.wavora.app.domain.repository.results.ScanResult
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
interface MusicRepository {
    // all songs, ordered by [sortOrder].
    fun getAllSongs(sortOrder: SortOrder = SortOrder.TITLE_ASC): Flow<List<Song>>

    // Single song by its Room primary key.
    fun getSongById(id: Long): Flow<Song?>

    // All albums.
    fun getAllAlbums(): Flow<List<Album>>

    // All songs belonging to a specific album.
    fun getSongsForAlbum(albumId: Long): Flow<List<Song>>

    // All Artists
    fun getAllArtists(): Flow<List<Artist>>

    // All albums for a specific artist.
    fun getAlbumsForArtist(artistId: Long): Flow<List<Album>>

    // All songs by a specific artist.
    fun getSongsForArtist(artistId: Long): Flow<List<Song>>

    // Unique folder paths derived from song file paths.
    fun getFolders(): Flow<List<MusicFolder>>

    // All songs within a specific folder path.
    fun getSongsForFolder(folderPath: String): Flow<List<Song>>

    // Full-text search across song titles, artist names, and album names.
    fun search(query: String): Flow<SearchResult>

    // Total number of songs in the library.
    fun getTotalSongCount(): Flow<Int>

    // Favourites
    // All songs marked as favourite.
    fun getFavouriteSongs(): Flow<List<Song>>

    // Toggle the favourite flag for a song.
    suspend fun toggleFavourite(songId: Long)

    // Library scan
    /**
     * Triggers a full MediaStore scan and updates the Room cache.
     * Called by WorkManager on a background thread (Phase 2).
     *
     * @return Number of songs added / updated / removed.
     */
    suspend fun scanLibrary(): ScanResult
}