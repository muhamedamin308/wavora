package com.wavora.app.domain.model

import com.wavora.app.core.utils.toDisplayDuration


/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class Song(
    val id: Long,
    val mediaStoreId: Long,
    val title: String,
    val artistName: String,
    val albumName: String,
    val albumId: Long,
    val artistId: Long,
    val duration: Long, // milliseconds
    val path: String, // absolute file path
    val contentUri: String, // content://media/external/audio/media/{id}
    val albumArtUri: String?, // nullable — not all songs have embedded art
    val dateAdded: Long, //  Unix epoch seconds (from MediaStore)
    val trackNumber: Int,
    val year: Int,
    val bitrate: Int, // kbps — 0 if unavailable
    val mimeType: String, // e.g. "audio/mpeg", "audio/flac"
    val size: Long, // bytes
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
)

// Formatted duration string for display (e.g. "3:45", "1:02:30").
val Song.durationFormatted: String
    get() = duration.toDisplayDuration()

// File size formatted for display (e.g. "8.2 MB").
val Song.sizeFormatted: String
    get() = when {
        size >= 1_000_000 -> "%.1f MB".format(size / 1_000_000.0)
        size >= 1_000 -> "%.1f KB".format(size / 1_000.0)
        else -> "$size B"
    }