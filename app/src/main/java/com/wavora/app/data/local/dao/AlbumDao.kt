package com.wavora.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.wavora.app.data.local.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Dao
interface AlbumDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(albums: List<AlbumEntity>)

    @Insert(onConflict = REPLACE)
    suspend fun insert(album: AlbumEntity): Long

    @Query("DELETE FROM albums WHERE media_store_album_id NOT IN (:validIds)")
    suspend fun deleteRemovedAlbums(validIds: List<Long>): Int

    @Query("DELETE FROM albums")
    suspend fun deleteAll()

    @Query("SELECT * FROM albums ORDER BY title_key ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY title_key DESC")
    fun getAllAlbumsDesc(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE artist_id = :artistId ORDER BY year DESC")
    fun getAlbumsForArtist(artistId: Long): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    fun getAlbumById(id: Long): Flow<AlbumEntity?>

    @Query("SELECT * FROM albums WHERE media_store_album_id = :mediaStoreId")
    suspend fun getAlbumByMediaStoreId(mediaStoreId: Long): AlbumEntity?

    /** Returns all MediaStore album IDs in the DB — used for scan diffing. */
    @Query("SELECT media_store_album_id FROM albums")
    suspend fun getAllMediaStoreIds(): List<Long>

    /**
     * Update the aggregated counts after a library scan.
     * Called after song upserts so counts are always consistent.
     */
    @Query(
        """
        UPDATE albums SET
        song_count = (SELECT COUNT(*) FROM songs WHERE songs.album_id == album_id AND songs.duration_ms >= 30000),
        total_duration_ms = (SELECT SUM(songs.duration_ms) FROM songs WHERE songs.album_id == album_id AND songs.duration_ms >= 30000)
        WHERE id = :albumId
    """
    )
    suspend fun refreshCounts(albumId: Long)

    @Query(
        """
        select * from albums
        inner join (
        select album_name, artist_name from songs
        where title like '%' || :query || '%'
        or artist_name like '%' || :query || '%'
        or album_name like '%' || :query || '%'
        ) s on albums.title = s.album_name
        limit 20
    """
    )
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>
}