package com.wavora.app.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.wavora.app.core.utils.Constants
import com.wavora.app.data.local.entity.AlbumEntity
import com.wavora.app.data.local.entity.ArtistEntity
import com.wavora.app.data.local.entity.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Queries MediaStore to discover all audio files on the device.
 *
 * Design decisions:
 *  - Always runs on [Dispatchers.IO] — ContentResolver queries are blocking I/O.
 *  - Returns raw entity lists rather than domain models — the repository
 *    handles the diff logic and Room upserts.
 *  - Uses the stable MediaStore.Audio columns available since API 26 (our minSdk).
 *  - Album art URI is built with [ContentUris] — preferred over storing the raw
 *    `_data` path which may differ across Android versions.
 *  - [Constants.MIN_SONG_DURATION_MS]: filters ringtones, notification sounds, and UI effects.
 *
 * Battery note: This scan is deliberately not called on the main thread or
 * on a timer. It is triggered only by:
 *   1. First app launch (permission granted)
 *   2. WorkManager job on MEDIA_MOUNTED broadcast or manual rescan
 *   3. ContentObserver detecting a change in the audio content URI (Phase 2+)
 */

@Singleton
class MediaStoreScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    /**
     * Result of a full MediaStore scan.
     * Songs, albums, and artists are all derived in one pass.
     */
    data class ScanData(
        val songs: List<SongEntity>,
        val albums: List<AlbumEntity>,
        val artists: List<ArtistEntity>,
    )

    /**
     * Execute a full scan of the device's audio MediaStore.
     * Must NOT be called on the main thread.
     */
    suspend fun scan(): ScanData = withContext(Dispatchers.IO) {
        val songs = querySongs()
        val albums = buildAlbums(songs)
        val artists = buildArtists(songs)
        ScanData(songs, albums, artists)
    }

    private fun querySongs(): List<SongEntity> {
        val songs = mutableListOf<SongEntity>()
        val resolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,           // absolute file path
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
        )

        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(Constants.MIN_SONG_DURATION_MS.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val contentUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        resolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val bitrateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val mediaStoreId = cursor.getLong(idColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val contentUri2 = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaStoreId
                ).toString()
                val albumArtUri = buildAlbumArtUri(albumId)

                songs += SongEntity(
                    mediaStoreId = mediaStoreId,
                    title = cursor.getString(titleColumn).orEmpty().trim().ifEmpty { "Unknown" },
                    titleKey = cursor.getString(titleColumn).orEmpty().lowercase().trim(),
                    artistName = cursor.getArtistName(artistColumn),
                    artistId = cursor.getLong(artistIdColumn),
                    albumName = cursor.getAlbumName(albumColumn),
                    albumId = albumId,
                    durationMs = cursor.getLong(durationColumn),
                    path = cursor.getString(dataColumn).orEmpty(),
                    contentUri = contentUri2,
                    albumArtUri = albumArtUri,
                    dateAdded = cursor.getLong(dateColumn),
                    trackNumber = cursor.getInt(trackColumn).let { raw ->
                        // Track can be encoded as disc*1000 + track (e.g. 1004 = disc 1, track 4)
                        if (raw >= 1000) raw % 1000 else raw
                    },
                    year = cursor.getInt(yearColumn),
                    bitrate = cursor.getInt(bitrateColumn) / 1000, // bps → kbps
                    mimeType = cursor.getString(mimeColumn).orEmpty(),
                    sizeBytes = cursor.getLong(sizeColumn),
                )
            }
        }
        return songs
    }

    private fun buildAlbums(songs: List<SongEntity>): List<AlbumEntity> {
        return songs
            .groupBy { it.albumId }
            .map { (albumId, albumSongs) ->
                val first = albumSongs.first()
                AlbumEntity(
                    mediaStoreAlbumId = albumId,
                    title = first.albumName,
                    titleKey = first.albumName.lowercase(),
                    artistName = first.artistName,
                    artistId = first.artistId,
                    albumArtUri = first.albumArtUri,
                    songCount = albumSongs.size,
                    year = albumSongs.mapNotNull { it.year.takeIf { y -> y > 0 } }.maxOrNull() ?: 0,
                    totalDurationMs = albumSongs.sumOf { it.durationMs },
                )
            }
            .sortedBy { it.titleKey }
    }

    private fun buildArtists(songs: List<SongEntity>): List<ArtistEntity> {
        return songs
            .groupBy { it.artistId }
            .map { (artistId, artistSongs) ->
                val first = artistSongs.first()
                val albumCount = artistSongs.map { it.albumId }.distinct().size
                // use the art from the most recent album
                val thumbnail = artistSongs
                    .maxByOrNull { it.dateAdded }
                    ?.albumArtUri
                ArtistEntity(
                    mediaStoreArtistId = artistId,
                    name = first.artistName,
                    nameKey = first.artistName.lowercase(),
                    albumCount = albumCount,
                    songCount = artistSongs.size,
                    thumbnailUri = thumbnail,
                )
            }
            .sortedBy { it.nameKey }
    }

    // Helpers
    private fun buildAlbumArtUri(albumId: Long): String? {
        if (albumId <= 0) return null
        return ContentUris.withAppendedId(
            "content://media/external/audio/albumart".toUri(),
            albumId,
        ).toString()
    }

    private fun Cursor.getArtistName(column: Int): String {
        val raw = getString(column)?.trim()
        return if (raw.isNullOrEmpty() || raw == "<unknown>")
            "Unknown Artist"
        else raw
    }

    private fun Cursor.getAlbumName(column: Int): String {
        val raw = getString(column)?.trim()
        return if (raw.isNullOrEmpty() || raw == "<unknown>") "Unknown Album" else raw
    }

    /**
     * Returns all audio MediaStore IDs currently visible to this app.
     * Used by the diff algorithm to detect deleted files quickly
     * without loading the full song metadata.
     */
    suspend fun getAllMediaStoreIds(): Set<Long> = withContext(Dispatchers.IO) {
        val ids = mutableSetOf<Long>()
        val resolver = context.contentResolver
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        resolver.query(
            uri,
            arrayOf(MediaStore.Audio.Media._ID),
            "${MediaStore.Audio.Media.DURATION} >= ?",
            arrayOf(Constants.MIN_SONG_DURATION_MS.toString()),
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            while (cursor.moveToNext()) ids += cursor.getLong(idColumn)
        }
        ids
    }
}