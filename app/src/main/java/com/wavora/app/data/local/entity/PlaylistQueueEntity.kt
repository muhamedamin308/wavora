package com.wavora.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wavora.app.domain.model.PlaylistType

/**
 * @author Muhamed Amin Hassan on 09,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

// Playlist Entity
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: String = PlaylistType.USER.name,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: String? = null,
)


// ─────────────────────────────────────────────────────────────────────────────
//  PlaylistSongCrossRef — junction table with position for drag-reorder
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Many-to-many relationship between playlists and songs.
 *
 * [position] is the user-defined sort order within a playlist.
 * On drag-reorder, we update only the affected rows via a single UPDATE statement.
 *
 * Foreign keys + ON DELETE CASCADE ensure no orphaned records when a song is
 * removed from the library or a playlist is deleted.
 */

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlist_id", "song_id"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("playlist_id"),
        Index("song_id"),
    ]
)
data class PlaylistSongCrossRef(
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
    @ColumnInfo(name = "song_id") val songId: Long,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
)


// ─────────────────────────────────────────────────────────────────────────────
//  PlayHistoryEntity — drives Smart Playlists and statistics
// ─────────────────────────────────────────────────────────────────────────────

/**
 * One row per play event.
 *
 * Not one row per song — that would prevent "Recently Played" ordering and
 * would lose the time-series data needed for play heat maps (future feature).
 *
 * [durationPlayedMs] tracks how much of the song was actually heard,
 * enabling smarter "Most Played" ranking (skipped songs don't count as much).
 *
 * Foreign key to songs with SET NULL so history is NOT deleted when a song is
 * removed — we keep [songId] null and preserve the row for statistics.
 */

@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("song_id"), Index("played_at")],
)
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "song_id") val songId: Long?,
    @ColumnInfo(name = "played_at") val playedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "duration_played_ms") val durationPlayedMs: Long = 0,
)

// ─────────────────────────────────────────────────────────────────────────────
//  QueueEntity — persists the playback queue across app restarts
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Stores the ordered playback queue so that killing WAVORA and reopening it
 * restores both the queue and the current song position.
 *
 * The entire queue is replaced atomically on each queue change (not per-row).
 * This keeps the implementation simple and avoids out-of-order position bugs.
 */

@Entity(
    tableName = "queue",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("song_id"), Index("position")],
)
data class QueueEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "song_id") val songId: Long,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "is_current") val isCurrent: Boolean = false,
)