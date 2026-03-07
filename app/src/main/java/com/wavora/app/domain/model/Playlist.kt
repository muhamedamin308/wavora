package com.wavora.app.domain.model

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

// Represents a user-created or system-generated (smart) playlist.
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val totalDuration: Long, // ms
    val createdAt: Long, // Unix epoch ms
    val updatedAt: Long, // Unix epoch ms
    val type: PlaylistType = PlaylistType.USER,
    val thumbnailUri: String?, // Art of first song in playlist
)

// Distinguishes user-created playlists from auto-generated smart playlists.
enum class PlaylistType {
    USER, // Created / managed by the user
    MOST_PLAYER, // Top 50 by play count (auto, Phase 7)
    RECENTLY_PLAYED, // Last 50 played (auto, Phase 7)
    RECENTLY_ADDED,  // Last 50 added (auto, Phase 7)
    FAVOURITES // Songs marked as favourite (auto)
}