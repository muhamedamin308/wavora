package com.wavora.app.domain.repository.interfaces

import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
// PlaylistRepository — CRUD for user playlists
interface PlaylistRepository {

    fun getAllPlayLists(): Flow<List<Playlist>>

    fun getPlaylistById(id: Long): Flow<Playlist?>

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    suspend fun createPlaylist(name: String): Long // returns new playlist ID

    suspend fun renamePlaylist(id: Long, newName: String)

    suspend fun deletePlaylist(id: Long)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    //  Persist a new position order after a drag-reorder operation.
    suspend fun reorderSongs(playlistId: Long, fromIndex: Int, toIndex: Int)
}