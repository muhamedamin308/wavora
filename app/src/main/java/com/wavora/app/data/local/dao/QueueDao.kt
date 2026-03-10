package com.wavora.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wavora.app.data.local.entity.QueueEntity
import com.wavora.app.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
@Dao
interface QueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<QueueEntity>)

    @Query("DELETE FROM queue")
    suspend fun clearQueue()

    /**
     * Replace the entire queue atomically.
     * Avoids partial state where the old queue is partially cleared and
     * the new one not yet written.
     */
    @Transaction
    suspend fun replaceQueue(items: List<QueueEntity>) {
        clearQueue()
        insertAll(items)
    }

    @Query("SELECT s.* FROM songs s INNER JOIN queue q ON q.song_id = s.id ORDER BY q.position ASC")
    fun getQueueSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM queue ORDER BY position ASC")
    suspend fun getRawQueue(): List<QueueEntity>

    @Query("SELECT position FROM queue WHERE is_current = 1 LIMIT 1")
    suspend fun getCurrentPosition(): Int?

    @Query("UPDATE queue SET is_current = (song_id = :songId AND position = :position)")
    suspend fun markCurrentSong(songId: Long, position: Int)
}