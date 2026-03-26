package com.wavora.app.core

import com.wavora.app.core.utils.fileName
import com.wavora.app.core.utils.parentDirectory
import com.wavora.app.core.utils.pluralLabel
import com.wavora.app.core.utils.toDisplayDuration
import com.wavora.app.core.utils.truncate
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * @author Muhamed Amin Hassan on 26,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

class ExtensionsTest {
    // ── toDisplayDuration ─────────────────────────────────────────────────────

    @Test
    fun `toDisplayDuration zero is 0 00`() =
        assertEquals("0:00", 0L.toDisplayDuration())

    @Test
    fun `toDisplayDuration 30 seconds`() =
        assertEquals("0:30", 30_000L.toDisplayDuration())

    @Test
    fun `toDisplayDuration 1 minute exactly`() =
        assertEquals("1:00", 60_000L.toDisplayDuration())

    @Test
    fun `toDisplayDuration 3 minutes 45 seconds`() =
        assertEquals("3:45", 225_000L.toDisplayDuration())

    @Test
    fun `toDisplayDuration 59 minutes 59 seconds`() =
        assertEquals("59:59", 3_599_000L.toDisplayDuration())

    @Test
    fun `toDisplayDuration 1 hour exactly uses h mm ss format`() =
        assertEquals("1:00:00", 3_600_000L.toDisplayDuration())

    @Test
    fun `toDisplayDuration 1 hour 2 minutes 3 seconds`() =
        assertEquals("1:02:03", (3_600_000L + 123_000L).toDisplayDuration())

    @Test
    fun `toDisplayDuration 10 hours`() =
        assertEquals("10:00:00", 36_000_000L.toDisplayDuration())

    @Test
    fun `toDisplayDuration sub-second rounds down`() =
        // 999ms → 0 seconds
        assertEquals("0:00", 999L.toDisplayDuration())

    @Test
    fun `toDisplayDuration negative returns 0 00`() =
        // Negative positions can occur briefly during seek
        assertEquals("0:00", (-1000L).coerceAtLeast(0L).toDisplayDuration())

    // ── pluralLabel ───────────────────────────────────────────────────────────

    @Test
    fun `pluralLabel 0 uses plural`() =
        assertEquals("0 songs", 0.pluralLabel("song"))

    @Test
    fun `pluralLabel 1 uses singular`() =
        assertEquals("1 song", 1.pluralLabel("song"))

    @Test
    fun `pluralLabel 2 uses plural`() =
        assertEquals("2 songs", 2.pluralLabel("song"))

    @Test
    fun `pluralLabel 100 uses plural`() =
        assertEquals("100 songs", 100.pluralLabel("song"))

    @Test
    fun `pluralLabel custom plural form`() =
        assertEquals("3 libraries", 3.pluralLabel("library", "libraries"))

    @Test
    fun `pluralLabel 1 with custom plural uses singular`() =
        assertEquals("1 library", 1.pluralLabel("library", "libraries"))

    @Test
    fun `pluralLabel album`() =
        assertEquals("1 album", 1.pluralLabel("album"))

    @Test
    fun `pluralLabel albums`() =
        assertEquals("5 albums", 5.pluralLabel("album"))

    // ── truncate ──────────────────────────────────────────────────────────────

    @Test
    fun `truncate short string unchanged`() =
        assertEquals("hello", "hello".truncate(10))

    @Test
    fun `truncate string exactly at limit unchanged`() =
        assertEquals("hello", "hello".truncate(5))

    @Test
    fun `truncate string over limit gets ellipsis`() =
        assertEquals("hell…", "hello world".truncate(5))

    @Test
    fun `truncate to 1 char`() =
        assertEquals("…", "hello".truncate(1))

    @Test
    fun `truncate empty string unchanged`() =
        assertEquals("", "".truncate(5))

    @Test
    fun `truncate long song title`() {
        val title = "A Very Long Song Title That Exceeds The Display Width"
        val result = title.truncate(20)
        assertEquals(20, result.length)
        assertEquals("A Very Long Song Ti…", result)
    }

    // ── fileName ─────────────────────────────────────────────────────────────

    @Test
    fun `fileName returns last path component`() =
        assertEquals("song.mp3", "/storage/emulated/0/Music/song.mp3".fileName())

    @Test
    fun `fileName with nested path`() =
        assertEquals("track.flac", "/music/Rock/Classic/track.flac".fileName())

    @Test
    fun `fileName with no slash returns full string`() =
        assertEquals("song.mp3", "song.mp3".fileName())

    @Test
    fun `fileName with trailing slash returns empty`() =
        assertEquals("", "/music/".fileName())

    // ── parentPath ────────────────────────────────────────────────────────────

    @Test
    fun `parentPath returns directory component`() =
        assertEquals(
            "/storage/emulated/0/Music",
            "/storage/emulated/0/Music/song.mp3".parentDirectory()
        )

    @Test
    fun `parentPath nested path`() =
        assertEquals("/music/Rock", "/music/Rock/song.mp3".parentDirectory())

    @Test
    fun `parentPath with no slash returns empty`() =
        assertEquals("", "song.mp3".parentDirectory())

    @Test
    fun `parentPath root-level file`() =
        assertEquals("", "/song.mp3".parentDirectory())
}