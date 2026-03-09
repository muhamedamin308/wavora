package com.wavora.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author Muhamed Amin Hassan on 09,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Cached album record derived from MediaStore.
 *
 * We store albums in Room rather than computing them on-the-fly from songs
 * because:
 *  1. Sorting albums by name/year is O(1) with an index vs O(n log n) in memory.
 *  2. Album art URIs come from a separate MediaStore table; caching avoids
 *     repeated ContentResolver queries.
 *  3. [totalDurationMs] is a pre-aggregated sum — cheaper than summing songs every time.
 */

@Entity(
    tableName = "albums",
    indices = [
        Index("media_store_album_id", unique = true),
        Index("artist_id"),
    ],
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "media_store_album_id") val mediaStoreAlbumId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "title_key") val titleKey: String,   // lowercase, for sort
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "artist_id") val artistId: Long,
    @ColumnInfo(name = "album_art_uri") val albumArtUri: String?,
    @ColumnInfo(name = "song_count") val songCount: Int,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "total_duration_ms") val totalDurationMs: Long,
)

// ─────────────────────────────────────────────────────────────────────────────
//  ArtistEntity
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cached artist record aggregated from the songs table.
 *
 * [thumbnailUri] is taken from the most recently added album for the artist.
 * Room keeps the count denormalized here to avoid GROUP BY on every render.
 */

@Entity(
    tableName = "artists",
    indices = [
        Index("media_store_artist_id", unique = true),
        Index("name_key"),
    ],
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "media_store_artist_id") val mediaStoreArtistId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "name_key") val nameKey: String,    // lowercase, for sort
    @ColumnInfo(name = "album_count") val albumCount: Int,
    @ColumnInfo(name = "song_count") val songCount: Int,
    @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: String?,
)