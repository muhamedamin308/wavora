package com.wavora.app.data.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wavora.app.data.local.WavoraDatabase
import com.wavora.app.data.local.dao.PlaylistDao
import com.wavora.app.data.local.dao.SongDao
import com.wavora.app.data.local.entity.PlaylistEntity
import com.wavora.app.data.local.entity.PlaylistSongCrossRef
import com.wavora.app.data.local.entity.SongEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDaoIntegrationTest {
    private lateinit var db: WavoraDatabase
    private lateinit var playlistDao: PlaylistDao
    private lateinit var songDao: SongDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WavoraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        playlistDao = db.playlistDao()
        songDao = db.songDao()
    }

    @After
    fun testDown() {
        db.close()
    }

    // Helpers

    private suspend fun insertPlaylist(id: Long, name: String): Long {
        return playlistDao.insertPlaylist(
            PlaylistEntity(id = id, name = name, createdAt = 0L, updatedAt = 0L)
        )
    }

    private suspend fun insertSong(id: Long): SongEntity {
        val entity = SongEntity(
            id = id, mediaStoreId = id, title = "Song $id",
            titleKey = "song $id", artistName = "Artist", albumName = "Album",
            albumId = 1L, artistId = 1L, durationMs = 200_000L,
            path = "/music/$id.mp3", contentUri = "content://media/$id",
            albumArtUri = null, dateAdded = 0L, trackNumber = id.toInt(),
            year = 2024, bitrate = 320, mimeType = "audio/mpeg", sizeBytes = 5_000_000L,
            isFavorite = false, playCount = 0,
        )
        songDao.insertSong(entity)
        return entity
    }

    private suspend fun addToPlaylist(playlistId: Long, songId: Long, position: Int) {
        playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, songId, position))
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Test
    fun insertingPlaylistMakesPlaylistRetrievable() = runTest {
        insertPlaylist(1L, "Rock Classics")
        val playlist = playlistDao.getPlaylistById(1L).first()
        assertEquals("Rock Classics", playlist?.name)
    }

    @Test
    fun renamingPlaylistUpdatesName() = runTest {
        insertPlaylist(1L, "Old Name")
        playlistDao.renamePlaylist(1L, "New Name")
        val playlist = playlistDao.getPlaylistById(1L).first()
        assertEquals("New Name", playlist?.name)
    }

    @Test
    fun deletingPlaylistRemovesItFromGetAllPlaylists() = runTest {
        insertPlaylist(1L, "To Delete")
        insertPlaylist(2L, "Keep This")
        playlistDao.deletePlaylist(1L)
        val playlists = playlistDao.getAllPlaylists().first()
        assertEquals(1, playlists.size)
        assertEquals("Keep This", playlists[0].name)
    }

    @Test
    fun getAllPlaylistsReturnsPlaylistsNewestFirst() = runTest {
        insertPlaylist(1L, "First")
        insertPlaylist(2L, "Second")
        val playlists = playlistDao.getAllPlaylists().first()
        // updated_at order — 2 was inserted after 1, so updatedAt >= first
        assertEquals(2, playlists.size)
    }

// ── Song membership ───────────────────────────────────────────────────────

    @Test
    fun addingSongToPlaylistMakesSongVisibleInGetSongsInPlaylist() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(42L)
        addToPlaylist(1L, 42L, 0)
        val songs = playlistDao.getSongsInPlaylist(1L).first()
        assertEquals(1, songs.size)
        assertEquals(42L, songs[0].id)
    }

    @Test
    fun removingSongFromPlaylistRemovesOnlyThatSong() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(1L); insertSong(2L); insertSong(3L)
        addToPlaylist(1L, 1L, 0)
        addToPlaylist(1L, 2L, 1)
        addToPlaylist(1L, 3L, 2)
        playlistDao.removeSongFromPlaylist(1L, 2L)
        val songs = playlistDao.getSongsInPlaylist(1L).first()
        assertEquals(2, songs.size)
        assertFalse(songs.any { it.id == 2L })
    }

    @Test
    fun getSongsInPlaylistReturnsSongsOrderedByPosition() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(1L); insertSong(2L); insertSong(3L)
        addToPlaylist(1L, 3L, 0)  // song 3 at position 0
        addToPlaylist(1L, 1L, 1)  // song 1 at position 1
        addToPlaylist(1L, 2L, 2)  // song 2 at position 2
        val songs = playlistDao.getSongsInPlaylist(1L).first()
        assertEquals(3L, songs[0].id)  // position 0 first
        assertEquals(1L, songs[1].id)
        assertEquals(2L, songs[2].id)
    }

// ── Reorder ───────────────────────────────────────────────────────────────

    @Test
    fun reorderingSongMovesItemDownCorrectly() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(1L); insertSong(2L); insertSong(3L); insertSong(4L)
        addToPlaylist(1L, 1L, 0)
        addToPlaylist(1L, 2L, 1)
        addToPlaylist(1L, 3L, 2)
        addToPlaylist(1L, 4L, 3)

        // Move position 0 → position 2 (song 1 moves below songs 2 and 3)
        playlistDao.reorderSong(1L, fromPos = 0, toPos = 2)

        val songs = playlistDao.getSongsInPlaylist(1L).first()
        assertEquals(2L, songs[0].id)  // song 2 moved up to pos 0
        assertEquals(3L, songs[1].id)  // song 3 moved up to pos 1
        assertEquals(1L, songs[2].id)  // song 1 now at pos 2
        assertEquals(4L, songs[3].id)  // song 4 unchanged
    }

    @Test
    fun reorderingSongMovesItemUpCorrectly() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(1L); insertSong(2L); insertSong(3L); insertSong(4L)
        addToPlaylist(1L, 1L, 0)
        addToPlaylist(1L, 2L, 1)
        addToPlaylist(1L, 3L, 2)
        addToPlaylist(1L, 4L, 3)

        // Move position 3 → position 1 (song 4 moves above songs 2 and 3)
        playlistDao.reorderSong(1L, fromPos = 3, toPos = 1)

        val songs = playlistDao.getSongsInPlaylist(1L).first()
        assertEquals(1L, songs[0].id)  // song 1 unchanged at 0
        assertEquals(4L, songs[1].id)  // song 4 now at 1
        assertEquals(2L, songs[2].id)  // song 2 shifted to 2
        assertEquals(3L, songs[3].id)  // song 3 shifted to 3
    }

    @Test
    fun reorderingSongWithSameFromAndToIsNoOp() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(1L); insertSong(2L)
        addToPlaylist(1L, 1L, 0)
        addToPlaylist(1L, 2L, 1)

        playlistDao.reorderSong(1L, fromPos = 0, toPos = 0)

        val songs = playlistDao.getSongsInPlaylist(1L).first()
        assertEquals(1L, songs[0].id)
        assertEquals(2L, songs[1].id)
    }

// ── Count queries ─────────────────────────────────────────────────────────

    @Test
    fun getPlaylistSongCountReturnsCorrectCount() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(1L); insertSong(2L); insertSong(3L)
        addToPlaylist(1L, 1L, 0)
        addToPlaylist(1L, 2L, 1)
        addToPlaylist(1L, 3L, 2)
        assertEquals(3, playlistDao.getPlaylistSongCount(1L).first())
    }

    @Test
    fun getPlaylistTotalDurationSumsSongDurations() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(1L)
        insertSong(2L)
        addToPlaylist(1L, 1L, 0)
        addToPlaylist(1L, 2L, 1)
        // Each fakeSong has durationMs = 200_000
        val total = playlistDao.getPlaylistTotalDuration(1L).first() ?: 0L
        assertEquals(400_000L, total)
    }

// ── ON DELETE CASCADE ─────────────────────────────────────────────────────

    @Test
    fun deletingPlaylistCascadesToRemovePlaylistSongsEntries() = runTest {
        insertPlaylist(1L, "My List")
        insertSong(1L)
        addToPlaylist(1L, 1L, 0)
        playlistDao.deletePlaylist(1L)

        // If cascade worked, a new playlist with same id should have 0 songs
        insertPlaylist(1L, "New List")
        val songs = playlistDao.getSongsInPlaylist(1L).first()
        assertTrue(songs.isEmpty())
    }

}