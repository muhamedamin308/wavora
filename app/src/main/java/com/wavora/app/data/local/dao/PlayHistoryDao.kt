package com.wavora.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wavora.app.data.local.entity.PlayHistoryEntity
import com.wavora.app.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Dao
interface PlayHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PlayHistoryEntity): Long

    /**
     * Top N most-played song IDs, ordered by how many times they have been played.
     * Used by the "Most Played" smart playlist.
     */
    @Query(
        """
            select s.* from songs s
            inner join (
                select song_id, count(*) as plays
                from play_history
                where song_id is not null
                group by song_id
                order by plays desc
                limit :limit
            ) ph on ph.song_id = s.id
            where s.duration_ms >= 30000
            order by ph.plays desc
        """
    )
    fun getMostPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>

    /**
     * Most recently played songs — one entry per unique song.
     * Used by the "Recently Played" smart playlist.
     */
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN (
            SELECT song_id, MAX(played_at) as last_played
            FROM play_history
            WHERE song_id IS NOT NULL
            GROUP BY song_id
            ORDER BY last_played DESC
            LIMIT :limit
        ) ph ON ph.song_id = s.id
        WHERE s.duration_ms >= 30000
        ORDER BY ph.last_played DESC
    """
    )
    fun getRecentlyPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>

    /** Prune very old entries — keep last 90 days. Called by WorkManager monthly. */
    @Query("DELETE FROM play_history WHERE played_at < :thresholdMs")
    suspend fun pruneOldEntries(thresholdMs: Long)
}