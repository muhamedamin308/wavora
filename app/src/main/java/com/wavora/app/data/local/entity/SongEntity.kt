package com.wavora.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author Muhamed Amin Hassan on 09,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Room entity for persisting songs scanned from MediaStore.
 *
 * Design decisions:
 *  - [mediaStoreId] is stored separately from Room's [id] so we can detect
 *    when a song is updated in MediaStore (same id, different metadata).
 *  - [albumArtUri] is nullable — not all files embed cover art.
 *  - [isFavorite] / [playCount] are WAVORA-owned fields that never come
 *    from MediaStore — they survive a full library rescan.
 *
 * Index on [mediaStoreId]: O(1) lookup during diff scan.
 * Index on [albumId] / [artistId]: efficient JOIN-free tab queries.
 */

@Entity(
    tableName = "songs",
    indices = [
        Index("media_store_id", unique = true),
        Index("album_id"),
        Index("artist_id"),
        Index("date_added"),
    ],
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "media_store_id") val mediaStoreId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "title_key") val titleKey: String,     // lowercase, for sort
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "artist_id") val artistId: Long,
    @ColumnInfo(name = "album_name") val albumName: String,
    @ColumnInfo(name = "album_id") val albumId: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "content_uri") val contentUri: String,
    @ColumnInfo(name = "album_art_uri") val albumArtUri: String?,
    @ColumnInfo(name = "date_added") val dateAdded: Long,       // Unix epoch seconds
    @ColumnInfo(name = "track_number") val trackNumber: Int,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "bitrate") val bitrate: Int,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,

    // WAVORA-owned — preserved across rescans
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "play_count") val playCount: Int = 0,
)


// ─────────────────────────────────────────────────────────────────────────────
//  FTS virtual table — enables fast full-text search in Phase 6
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-Text Search 4 shadow table.
 *
 * Room keeps this in sync with [SongEntity] via a trigger (auto-generated).
 * We store only the fields searched by the user.
 *
 * Usage: `songDao.searchSongs("%rock%")` — Room translates to FTS MATCH queries.
 *
 * [contentEntity] links FTS rows back to the real songs table.
 */

@Fts4(contentEntity = SongEntity::class)
@Entity(tableName = "songs_fts")
data class SongFtsEntity(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "album_name") val albumName: String,
)