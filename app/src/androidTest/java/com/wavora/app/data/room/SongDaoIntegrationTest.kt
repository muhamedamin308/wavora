package com.wavora.app.data.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wavora.app.data.local.WavoraDatabase
import com.wavora.app.data.local.dao.SongDao
import com.wavora.app.data.local.entity.SongEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SongDaoIntegrationTest {

    private lateinit var db: WavoraDatabase
    private lateinit var dao: SongDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WavoraDatabase::class.java)
            .allowMainThreadQueries()   // test only — never in production
            .build()
        dao = db.songDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun song(
        id: Long,
        title: String = "Song $id",
        artistName: String = "Artist $id",
        albumName: String = "Album $id",
        albumId: Long = 1L,
        artistId: Long = 1L,
        duration: Long = 200_000L,
        dateAdded: Long = id * 1000L,
        trackNumber: Int = id.toInt(),
        isFavorite: Boolean = false,
        playCount: Int = 0,
        path: String = "/music/song$id.mp3",
    ) = SongEntity(
        id = id, mediaStoreId = id, title = title,
        titleKey = title.lowercase(), artistName = artistName,
        albumName = albumName, albumId = albumId, artistId = artistId,
        durationMs = duration, path = path,
        contentUri = "content://media/$id", albumArtUri = null,
        dateAdded = dateAdded, trackNumber = trackNumber, year = 2024,
        bitrate = 320, mimeType = "audio/mpeg", sizeBytes = 5_000_000L,
        isFavorite = isFavorite, playCount = playCount,
    )

    private suspend fun insert(vararg songs: SongEntity) =
        songs.forEach { dao.insertSong(it) }

    // ── Basic CRUD ────────────────────────────────────────────────────────────
    @Test
    fun insertedSongIsRetrievableById() = runTest {
        insert(song(1L, title = "Imagine"))
        val result = dao.getSongById(1L).first()
        assertEquals("Imagine", result?.title)
    }

    @Test
    fun getAllSongsByTitleReturnsSongsAlphabetically() = runTest {
        insert(song(1L, title = "Zoo"), song(2L, title = "Apple"), song(3L, title = "Mango"))
        val songs = dao.getAllSongsByTitle().first()
        assertEquals("Apple", songs[0].title)
        assertEquals("Mango", songs[1].title)
        assertEquals("Zoo", songs[2].title)
    }

    @Test
    fun getAllSongsByTitleDescReturnsSongsReverseAlphabetically() = runTest {
        insert(song(1L, title = "Apple"), song(2L, title = "Zoo"))
        val songs = dao.getAllSongsByTitleDesc().first()
        assertEquals("Zoo", songs[0].title)
        assertEquals("Apple", songs[1].title)
    }

    @Test
    fun getAllSongsByArtistSortsByArtistThenTitle() = runTest {
        insert(
            song(1L, title = "B Song", artistName = "Zara"),
            song(2L, title = "A Song", artistName = "Zara"),
            song(3L, title = "C Song", artistName = "Abba"),
        )
        val songs = dao.getAllSongsByArtist().first()
        assertEquals("Abba", songs[0].artistName)
        assertEquals("A Song", songs[1].title)  // Zara, A comes before B
    }

    @Test
    fun getAllSongsByDurationAscShortestFirst() = runTest {
        insert(
            song(1L, duration = 300_000L),
            song(2L, duration = 100_000L),
            song(3L, duration = 200_000L),
        )
        val songs = dao.getAllSongsByDurationAsc().first()
        assertEquals(100_000L, songs[0].durationMs)
        assertEquals(200_000L, songs[1].durationMs)
        assertEquals(300_000L, songs[2].durationMs)
    }

    @Test
    fun getAllSongsByDurationDescLongestFirst() = runTest {
        insert(song(1L, duration = 100_000L), song(2L, duration = 300_000L))
        val songs = dao.getAllSongsByDurationDesc().first()
        assertEquals(300_000L, songs[0].durationMs)
    }

    @Test
    fun getAllSongsByDateAddedDescNewestFirst() = runTest {
        insert(
            song(1L, dateAdded = 1000L),
            song(2L, dateAdded = 3000L),
            song(3L, dateAdded = 2000L),
        )
        val songs = dao.getAllSongsByDateAddedDesc().first()
        assertEquals(3000L, songs[0].dateAdded)
        assertEquals(2000L, songs[1].dateAdded)
        assertEquals(1000L, songs[2].dateAdded)
    }

    @Test
    fun getAllSongsByDateAddedAscOldestFirst() = runTest {
        insert(song(1L, dateAdded = 3000L), song(2L, dateAdded = 1000L))
        val songs = dao.getAllSongsByDateAddedAsc().first()
        assertEquals(1000L, songs[0].dateAdded)
    }

// ── Favourite toggle ──────────────────────────────────────────────────────

    @Test
    fun toggleFavoriteSetsIsFavoriteTrue() = runTest {
        insert(song(1L, isFavorite = false))
        dao.setFavorite(1L, true)
        val result = dao.getSongById(1L).first()
        assertTrue(result!!.isFavorite)
    }

    @Test
    fun toggleFavoriteCanSetIsFavoriteFalse() = runTest {
        insert(song(1L, isFavorite = true))
        dao.setFavorite(1L, false)
        val result = dao.getSongById(1L).first()
        assertFalse(result!!.isFavorite)
    }

    @Test
    fun getFavoriteSongsReturnsOnlyFavourites() = runTest {
        insert(
            song(1L, isFavorite = true),
            song(2L, isFavorite = false),
            song(3L, isFavorite = true),
        )
        val favs = dao.getFavoriteSongs().first()
        assertEquals(2, favs.size)
        assertTrue(favs.all { it.isFavorite })
    }

// ── Play count ────────────────────────────────────────────────────────────

    @Test
    fun incrementPlayCountIncreasesByOne() = runTest {
        insert(song(1L, playCount = 5))
        dao.incrementPlayCount(1L)
        val result = dao.getSongById(1L).first()
        assertEquals(6, result!!.playCount)
    }

    @Test
    fun getMostPlayedSongsReturnsTopSongsOrderedByPlayCount() = runTest {
        insert(
            song(1L, playCount = 10),
            song(2L, playCount = 50),
            song(3L, playCount = 5),
        )
        val most = dao.getMostPlayedSongs(2).first()
        assertEquals(50, most[0].playCount)
        assertEquals(10, most[1].playCount)
        assertEquals(2, most.size)
    }

// ── Album and artist filtering ─────────────────────────────────────────────

    @Test
    fun getSongsForAlbumReturnsOnlySongsInThatAlbum() = runTest {
        insert(
            song(1L, albumId = 10L),
            song(2L, albumId = 20L),
            song(3L, albumId = 10L),
        )
        val songs = dao.getSongsForAlbum(10L).first()
        assertEquals(2, songs.size)
        assertTrue(songs.all { it.albumId == 10L })
    }

    @Test
    fun getSongsForArtistReturnsOnlySongsByThatArtist() = runTest {
        insert(
            song(1L, artistId = 5L),
            song(2L, artistId = 7L),
        )
        val songs = dao.getSongsForArtist(5L).first()
        assertEquals(1, songs.size)
        assertEquals(5L, songs[0].artistId)
    }

// ── Folder filtering ──────────────────────────────────────────────────────

    @Test
    fun getSongsInFolderReturnsSongsMatchingFolderPrefix() = runTest {
        insert(
            song(1L, path = "/music/Rock/song1.mp3"),
            song(2L, path = "/music/Rock/song2.mp3"),
            song(3L, path = "/music/Pop/song3.mp3"),
        )
        val songs = dao.getSongsInFolder("/music/Rock").first()
        assertEquals(2, songs.size)
        assertTrue(songs.all { it.path.startsWith("/music/Rock") })
    }

// ── Song count ────────────────────────────────────────────────────────────

    @Test
    fun getSongCountReflectsNumberOfInsertedSongs() = runTest {
        insert(song(1L), song(2L), song(3L))
        assertEquals(3, dao.getSongCount().first())
    }

    @Test
    fun getSongCountIsZeroWhenDatabaseIsEmpty() = runTest {
        assertEquals(0, dao.getSongCount().first())
    }

    // ── MediaStore ID lookup ──────────────────────────────────────────────────

    @Test
    fun getAllMediaStoreIdsReturnsAllIds() = runTest {
        insert(song(1L), song(2L), song(3L))
        val ids = dao.getAllMediaStoreIds()
        assertEquals(setOf(1L, 2L, 3L), ids.toSet())
    }

}