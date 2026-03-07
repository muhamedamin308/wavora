package com.wavora.app.domain.model

import com.wavora.app.core.utils.toDisplayDuration

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
data class Album(
    val id: Long,
    val title: String,
    val artistName: String,
    val artistId: Long,
    val albumArtUri: String?, // nullable — not all albums have embedded art
    val songCount: Int,
    val year: Int, // 0 if unknown
    val totalDuration: Long, // sum of all songs durations in ms
)

val Album.totalDurationFormatted: String
    get() = totalDuration.toDisplayDuration()