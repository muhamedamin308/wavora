package com.wavora.app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.wavora.app.core.utils.toDisplayDuration
import com.wavora.app.domain.model.PlayerState
import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song

/**
 * Accessibility helpers for WAVORA.
 *
 * Accessibility goals:
 *  - Every interactive element has a meaningful [contentDescription] that
 *    TalkBack can announce. Dynamic state (playing/paused, repeat mode,
 *    favourite) is reflected via [stateDescription].
 *  - Decorative icons use [clearAndSetSemantics {}] to be ignored by TalkBack
 *    so they don't clutter the focus order.
 *  - Seek bar announces position as "X of Y" in minutes:seconds.
 *  - Album art announces "Album art for [album] by [artist]" or "No album art".
 *  - Song rows announce the full song title, artist, album, and duration as a
 *    single semantic node so TalkBack reads it as one coherent item.
 *
 * Usage:
 * ```kotlin
 * Modifier.semantics { playButtonSemantics(isPlaying) }
 * Modifier.songRowSemantics(song)
 * ```
 */

// Play / Pause button
fun Modifier.playPauseSemantics(isPlaying: Boolean): Modifier = semantics {
    role = Role.Button
    contentDescription = if (isPlaying) "Pause" else "Play"
    stateDescription = if (isPlaying) "Playing" else "Pause"
}

// ── Shuffle button ────────────────────────────────────────────────────────────

fun Modifier.shuffleSemantics(isEnabled: Boolean): Modifier = semantics {
    role = Role.Button
    contentDescription = "Shuffle"
    stateDescription = if (isEnabled) "Shuffle on" else "Shuffle off"
}

// ── Repeat button ─────────────────────────────────────────────────────────────

fun Modifier.repeatSemantics(mode: RepeatMode): Modifier = semantics {
    role = Role.Button
    contentDescription = "Repeat"
    stateDescription = when (mode) {
        RepeatMode.NONE -> "Repeat off"
        RepeatMode.ALL -> "Repeat all"
        RepeatMode.ONE -> "Repeat one"
    }
}


// ── Favourite button ──────────────────────────────────────────────────────────

fun Modifier.favouriteSemantics(isFavourite: Boolean): Modifier = semantics {
    role = Role.Button
    contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites"
    stateDescription = if (isFavourite) "Added to favourites" else "Not in favourites"
}

// ── Seek bar ──────────────────────────────────────────────────────────────────

fun Modifier.seekBarSemantics(positionMs: Long, durationMs: Long): Modifier = semantics {
    contentDescription = "Seek bar"
    stateDescription = "${positionMs.toDisplayDuration()} of ${durationMs.toDisplayDuration()}"
}

// ── Album art ─────────────────────────────────────────────────────────────────

fun Modifier.albumArtSemantics(song: Song?): Modifier =
    if (song == null) clearAndSetSemantics {}
    else semantics {
        contentDescription =
            if (song.albumArtUri != null)
                "Album art for ${song.albumName} by ${song.artistName}"
            else
                "No album art for ${song.albumName}"
    }

// ── Song list row ─────────────────────────────────────────────────────────────

/**
 * Makes a song list row announce all relevant info as a single semantic node.
 * Without this, TalkBack reads the title, artist, duration as separate items
 * making it very noisy during list navigation.
 */
fun Modifier.songRowSemantics(song: Song): Modifier = semantics(mergeDescendants = true) {
    contentDescription = buildString {
        append(song.title)
        append(", by ")
        append(song.artistName)
        append(", from ")
        append(song.albumName)
        append(", ")
        append(song.duration.toDisplayDuration())
        if (song.isFavorite) append(", in favourites")
    }
    role = Role.Button
}

// ── Player state announcement ─────────────────────────────────────────────────

/**
 * Full player state for the NowPlaying screen — announced on screen entry
 * so TalkBack users know what's playing without navigating to each element.
 */
fun Modifier.nowPlayingSemantics(playerState: PlayerState): Modifier = semantics {
    val song = playerState.currentSong
    contentDescription = if (song == null) "No song playing"
    else buildString {
        append("Now playing: ")
        append(song.title)
        append(" by ")
        append(song.artistName)
    }
    stateDescription = if (playerState.isPlaying) "Playing" else "Paused"
}

// ── Decorative icon helper ────────────────────────────────────────────────────

/**
 * Marks an icon as purely decorative — TalkBack will skip it entirely.
 * Use for icons that are redundant with adjacent text labels.
 */
fun Modifier.decorative(): Modifier = clearAndSetSemantics {}
