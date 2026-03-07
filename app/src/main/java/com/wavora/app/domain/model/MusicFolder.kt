package com.wavora.app.domain.model

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
/**
 * Represents a file-system folder containing one or more songs.
 * Derived at scan time by extracting the parent directory of each [Song.path].
 */
data class MusicFolder(
    val path: String, // e.g. "/storage/emulated/0/Music/Rock"
    val name: String, // leaf folder name, e.g. "Rock"
    val songCount: Int,
    val totalDuration: Long,
)

