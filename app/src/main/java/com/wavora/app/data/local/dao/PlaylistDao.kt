package com.wavora.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.wavora.app.data.local.entity.PlaylistEntity
import com.wavora.app.data.local.entity.PlaylistSongCrossRef
import com.wavora.app.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
@Dao
interface PlaylistDao {
    // playlists
    @Insert(onConflict = REPLACE)
    suspend fun insertPlaylist(playList: PlaylistEntity): Long

    @Query("update playlists set name = :name, updated_at = :updatedAt where id = :id")
    suspend fun renamePlaylist(
        id: Long,
        name: String,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("SELECT * FROM playlists ORDER BY updated_at DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistById(id: Long): Flow<PlaylistEntity?>

    // songs count (for metadata display without loading all songs)
    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = :playlistId")
    fun getPlaylistSongCount(playlistId: Long): Flow<Int>

    @Query(
        """
        SELECT SUM(s.duration_ms) FROM songs s
        INNER JOIN playlist_songs ps ON ps.song_id = s.id
        WHERE ps.playlist_id = :playlistId
    """
    )
    fun getPlaylistTotalDuration(playlistId: Long): Flow<Long?>

    // songs in playlist
    /**
     * Returns songs in a playlist ordered by [position].
     * Joining songs ensures we always get the latest song metadata (title,
     * artist, album art) even if the song was updated by a rescan.
     */
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON ps.song_id = s.id
        WHERE ps.playlist_id = :playlistId
        ORDER BY ps.position ASC
    """
    )
    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>

    // Cross-ref CRUD
    @Insert(onConflict = REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT MAX(position) FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?

    /**
     * Atomically move a song from [fromPos] to [toPos] within a playlist.
     *
     * Strategy — three-step swap to avoid temporary unique constraint violations:
     *  1. Move the dragged item to a temp position (-1).
     *  2. Shift other items to fill the gap.
     *  3. Place the dragged item at the final position.
     *
     * All steps run inside a single Room transaction.
     */
    @Transaction
    suspend fun reorderSong(playlistId: Long, fromPos: Int, toPos: Int) {
        if (fromPos == toPos) return
        // 1. park dragged item at sentinel position
        shiftPosition(playlistId, fromPos, -999)

        if (fromPos < toPos) {
            // Moving down: shift items in (fromPos+1 .. toPos) up by one
            shiftRangeDown(playlistId, fromPos + 1, toPos)
        } else {
            // Moving up: shift items in (toPos ... fromPos-1) down by one
            shiftRangeUp(playlistId, toPos, fromPos - 1)
        }

        // 3. place at final position
        shiftPosition(playlistId, -999, toPos)
        // Touch updated_at on the playlist header
        touchUpdatedAt(playlistId)
    }

    @Query("UPDATE playlist_songs SET position = :toPos WHERE playlist_id = :playlistId AND position = :fromPos")
    suspend fun shiftPosition(playlistId: Long, fromPos: Int, toPos: Int)

    @Query("UPDATE playlist_songs SET position = position - 1 WHERE playlist_id = :playlistId AND position BETWEEN :start AND :end")
    suspend fun shiftRangeDown(playlistId: Long, start: Int, end: Int)

    @Query("UPDATE playlist_songs SET position = position + 1 WHERE playlist_id = :playlistId AND position BETWEEN :start AND :end")
    suspend fun shiftRangeUp(playlistId: Long, start: Int, end: Int)

    @Query("UPDATE playlists SET updated_at = :time WHERE id = :id")
    suspend fun touchUpdatedAt(id: Long, time: Long = System.currentTimeMillis())

    @Query("UPDATE playlists SET thumbnail_uri = :uri WHERE id = :id")
    suspend fun updateThumbnail(id: Long, uri: String?)
}