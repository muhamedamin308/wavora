package com.wavora.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.wavora.app.data.local.dao.SongDao.Companion.MIN_DURATION_MS
import com.wavora.app.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 09,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * DAO for song library operations.
 *
 * All read queries return [Flow] — Room emits a new list whenever the
 * underlying table changes, so the UI always renders fresh data without
 * polling or manual refresh calls.
 *
 * Sort order is baked into separate query methods (not passed as a parameter)
 * because Room cannot use parameters in ORDER BY clauses safely. This is
 * the recommended pattern — the Repository chooses the right method.
 *
 * [MIN_DURATION_MS] filters out ringtones and notification sounds.
 */
@Dao
interface SongDao {

    companion object {
        private const val MIN_DURATION_MS = 30_000 // 30 seconds
        private const val BASE_WHERE = "WHERE duration_ms >= $MIN_DURATION_MS"
    }

    // Insert / Update / Delete
    /**
     * Insert or replace a batch of songs during a library scan.
     * REPLACE conflict strategy: updates existing rows by [media_store_id] index.
     */
    @Insert(onConflict = REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Insert(onConflict = REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    /**
     * Preserve WAVORA-owned fields (favorites, play count) while updating
     * MediaStore-sourced metadata. Called for songs that exist in both the
     * old cache and the fresh scan.
     */
    @Query(
        """
        UPDATE songs SET
            title = :title,
            title_key = :titleKey,
            artist_name = :artistName,
            artist_id = :artistId,
            album_name = :albumName,
            album_id = :albumId,
            duration_ms = :durationMs,
            path = :path,
            content_uri = :contentUri,
            album_art_uri = :albumArtUri,
            date_added = :dateAdded,
            track_number = :trackNumber,
            year = :year,
            bitrate = :bitrate,
            mime_type = :mimeType,
            size_bytes = :sizeBytes
        WHERE media_store_id = :mediaStoreId
    """
    )
    suspend fun updateMetadata(
        mediaStoreId: Long,
        title: String, titleKey: String,
        artistName: String, artistId: Long,
        albumName: String, albumId: Long,
        durationMs: Long, path: String,
        contentUri: String, albumArtUri: String?,
        dateAdded: Long, trackNumber: Int,
        year: Int, bitrate: Int,
        mimeType: String, sizeBytes: Long,
    )

    /** Remove songs whose MediaStore IDs are no longer in the device scan. */
    @Query("DELETE FROM songs WHERE media_store_id NOT IN (:validMediaStoreIds)")
    suspend fun deleteRemovedSongs(validMediaStoreIds: List<Long>): Int

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    // READS
    @Query("SELECT * FROM songs $BASE_WHERE ORDER BY title_key ASC")
    fun getAllSongsByTitle(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs $BASE_WHERE ORDER BY title_key DESC")
    fun getAllSongsByTitleDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs $BASE_WHERE ORDER BY artist_name ASC, title_key ASC")
    fun getAllSongsByArtist(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs $BASE_WHERE ORDER BY duration_ms ASC")
    fun getAllSongsByDurationAsc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs $BASE_WHERE ORDER BY duration_ms DESC")
    fun getAllSongsByDurationDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs $BASE_WHERE ORDER BY date_added DESC")
    fun getAllSongsByDateAddedDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs $BASE_WHERE ORDER BY date_added ASC")
    fun getAllSongsByDateAddedAsc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongById(id: Long): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE media_store_id = :mediaStoreId")
    suspend fun getSongByMediaStoreId(mediaStoreId: Long): SongEntity?

    @Query("SELECT * FROM songs $BASE_WHERE AND album_id = :albumId ORDER BY track_number ASC")
    fun getSongsForAlbum(albumId: Long): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs $BASE_WHERE AND artist_id = :artistId ORDER BY title_key ASC")
    fun getSongsForArtist(artistId: Long): Flow<List<SongEntity>>

    // Folder queries — derive folder from the path column
    @Query(
        """
        SELECT DISTINCT substr(path, 1, length(path) - length(replace(path, '/', '')) - 1 +
            length(path) - length(replace(substr(path, 2), '/', '')))
        FROM songs $BASE_WHERE
    """
    )
    fun getDistinctFolderPaths(): Flow<List<String>>

    @Query("SELECT * FROM songs $BASE_WHERE AND path LIKE :folderPrefix || '%' ORDER BY title_key ASC")
    fun getSongsInFolder(folderPrefix: String): Flow<List<SongEntity>>


    // Favorites
    @Query("SELECT * FROM songs WHERE is_favorite = 1 AND duration_ms >= $MIN_DURATION_MS ORDER BY title_key ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>


    @Query("UPDATE songs SET is_favorite = NOT is_favorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    // Statistics
    @Query("SELECT COUNT(*) FROM songs $BASE_WHERE")
    fun getSongCount(): Flow<Int>

    /** Returns all MediaStore IDs currently in the DB — used for diff scanning. */
    @Query("SELECT media_store_id FROM songs")
    suspend fun getAllMediaStoreIds(): List<Long>

    // FTS Search Phase 6
    /**
     * Full-text search using the FTS4 shadow table.
     * [query] must be pre-formatted as a FTS MATCH expression,
     * e.g. "\"rock\"*" for prefix search.
     *
     * Returns matching song IDs; join with songs table for full entities.
     */
    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN songs_fts ON songs.rowid = songs_fts.rowid
        WHERE songs_fts MATCH :query
        AND songs.duration_ms >= $MIN_DURATION_MS
        ORDER BY songs.title_key ASC
    """
    )
    fun searchSongs(query: String): Flow<List<SongEntity>>

    // ── PLAY COUNT ────────────────────────────────────────────────────────────

    @Query("UPDATE songs SET play_count = play_count + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: Long)

    /**
     * Top N most-played songs — used by Smart Playlist in Phase 7.
     */
    @Query("""
        SELECT * FROM songs $BASE_WHERE
        ORDER BY play_count DESC
        LIMIT :limit
    """)
    fun getMostPlayedSongs(limit: Int): Flow<List<SongEntity>>
}