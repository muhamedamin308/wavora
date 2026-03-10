package com.wavora.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wavora.app.data.local.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(artists: List<ArtistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artist: ArtistEntity): Long

    @Query("DELETE FROM artists WHERE media_store_artist_id NOT IN (:validIds)")
    suspend fun deleteRemovedArtists(validIds: List<Long>): Int

    @Query("DELETE FROM artists")
    suspend fun deleteAll()

    @Query("SELECT * FROM artists ORDER BY name_key ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :id")
    fun getArtistById(id: Long): Flow<ArtistEntity?>

    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' LIMIT 20")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>

    @Query("SELECT media_store_artist_id FROM artists")
    suspend fun getAllMediaStoreIds(): List<Long>

    @Query(
        """
        UPDATE artists SET
            song_count  = (SELECT COUNT(*) FROM songs WHERE songs.artist_id = artists.id AND songs.duration_ms >= 30000),
            album_count = (SELECT COUNT(DISTINCT album_id) FROM songs WHERE songs.artist_id = artists.id)
        WHERE id = :artistId
    """
    )
    suspend fun refreshCounts(artistId: Long)
}