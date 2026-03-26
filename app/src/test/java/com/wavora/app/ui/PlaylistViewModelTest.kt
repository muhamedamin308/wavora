package com.wavora.app.ui

/**
 * @author Muhamed Amin Hassan on 26,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */


import androidx.lifecycle.SavedStateHandle
import com.wavora.app.core.result.AsyncResult
import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.RepeatMode
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import com.wavora.app.domain.repository.interfaces.PlaylistRepository
import com.wavora.app.ui.screens.playlist.PlaylistEvent
import com.wavora.app.ui.screens.playlist.PlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var playlistRepo: FakePlaylistRepository
    private lateinit var playerRepo: FakePlayerForPlaylist

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        playlistRepo = FakePlaylistRepository()
        playerRepo = FakePlayerForPlaylist()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakePlaylist(id: Long = 1L, name: String = "My Playlist") = Playlist(
        id = id, name = name, songCount = 0, totalDuration = 0L,
        createdAt = 0L, updatedAt = 0L, thumbnailUri = null,
    )

    private fun fakeSong(id: Long) = Song(
        id = id, mediaStoreId = id, title = "Song $id", artistName = "Artist",
        albumName = "Album", albumId = 1L, artistId = 1L, duration = 200_000L,
        path = "/music/$id.mp3", contentUri = "content://media/$id",
        albumArtUri = null, dateAdded = 0L, trackNumber = 1,
        year = 2024, bitrate = 320, mimeType = "audio/mpeg", size = 5_000_000L,
    )

    private fun vm(playlistId: Long = 1L) = PlaylistViewModel(
        savedStateHandle = SavedStateHandle(mapOf("playlistId" to playlistId)),
        playlistRepository = playlistRepo,
        playerRepository = playerRepo,
    )

    // ── Loading state ─────────────────────────────────────────────────────────

    @Test
    fun `loads playlist and songs on init`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L, "Rock"))
        playlistRepo.songs.value = listOf(fakeSong(1L), fakeSong(2L))
        val viewModel = vm(1L)
        advanceUntilIdle()

        val playlist = (viewModel.uiState.value.playlist as AsyncResult.Success).data
        val songs = (viewModel.uiState.value.songs as AsyncResult.Success).data
        assertEquals("Rock", playlist.name)
        assertEquals(2, songs.size)
    }

    @Test
    fun `shows error state when playlist not found`() = runTest {
        playlistRepo.playlists.value = emptyList()
        val viewModel = vm(99L)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.playlist is AsyncResult.Error)
    }

    // ── Playback ──────────────────────────────────────────────────────────────

    @Test
    fun `onPlayAll plays all songs from index 0`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L))
        playlistRepo.songs.value = listOf(fakeSong(1L), fakeSong(2L))
        val viewModel = vm(1L)
        advanceUntilIdle()

        viewModel.onPlayAll()
        advanceUntilIdle()

        assertEquals(0, playerRepo.lastPlayAllStartIndex)
        assertEquals(2, playerRepo.lastPlayAllSongs?.size)
    }

    @Test
    fun `onPlayAll does nothing when playlist is empty`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L))
        playlistRepo.songs.value = emptyList()
        val viewModel = vm(1L)
        advanceUntilIdle()

        viewModel.onPlayAll()
        advanceUntilIdle()

        assertNull(playerRepo.lastPlayAllSongs)
    }

    @Test
    fun `onSongClicked plays from correct index`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L))
        playlistRepo.songs.value = listOf(fakeSong(1L), fakeSong(2L), fakeSong(3L))
        val viewModel = vm(1L)
        advanceUntilIdle()

        viewModel.onSongClicked(2)
        advanceUntilIdle()

        assertEquals(2, playerRepo.lastPlayAllStartIndex)
    }

    // ── Song removal ──────────────────────────────────────────────────────────

    @Test
    fun `onRemoveSong calls repository and emits snackbar`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L))
        playlistRepo.songs.value = listOf(fakeSong(10L))
        val viewModel = vm(1L)
        advanceUntilIdle()

        var snackbarMessage: String? = null
        val job = launch {
            viewModel.events.collect { event ->
                if (event is PlaylistEvent.ShowSnackbar) snackbarMessage = event.message
            }
        }

        viewModel.onRemoveSong(10L)
        advanceUntilIdle()

        assertEquals(1L, playlistRepo.lastRemovedFromPlaylist?.first)
        assertEquals(10L, playlistRepo.lastRemovedFromPlaylist?.second)
        assertNotNull(snackbarMessage)
        job.cancel()
    }

    // ── Rename ────────────────────────────────────────────────────────────────

    @Test
    fun `onRenameClicked shows rename dialog`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L))
        val viewModel = vm(1L)
        advanceUntilIdle()

        viewModel.onRenameClicked()
        assertTrue(viewModel.uiState.value.showRenameDialog)
    }

    @Test
    fun `onRenameConfirmed calls repository and hides dialog`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L, "Old Name"))
        val viewModel = vm(1L)
        advanceUntilIdle()

        viewModel.onRenameClicked()
        viewModel.onRenameConfirmed("New Name")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showRenameDialog)
        assertEquals(Pair(1L, "New Name"), playlistRepo.lastRenamed)
    }

    @Test
    fun `onRenameDismissed hides dialog without calling repository`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L))
        val viewModel = vm(1L)
        advanceUntilIdle()

        viewModel.onRenameClicked()
        viewModel.onRenameDismissed()

        assertFalse(viewModel.uiState.value.showRenameDialog)
        assertNull(playlistRepo.lastRenamed)
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    fun `onDeleteConfirmed deletes playlist and navigates up`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L))
        val viewModel = vm(1L)
        advanceUntilIdle()

        var navigatedUp = false
        val job = launch {
            viewModel.events.collect { event ->
                if (event is PlaylistEvent.NavigateUp) navigatedUp = true
            }
        }

        viewModel.onDeleteConfirmed()
        advanceUntilIdle()

        assertEquals(1L, playlistRepo.lastDeletedPlaylistId)
        assertTrue(navigatedUp)
        job.cancel()
    }

    @Test
    fun `onDeleteDismissed hides dialog without deleting`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L))
        val viewModel = vm(1L)
        advanceUntilIdle()

        viewModel.onDeleteClicked()
        viewModel.onDeleteDismissed()

        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertNull(playlistRepo.lastDeletedPlaylistId)
    }

    // ── Live updates ──────────────────────────────────────────────────────────

    @Test
    fun `playlist name update reflects in UI`() = runTest {
        playlistRepo.playlists.value = listOf(fakePlaylist(1L, "Rock"))
        val viewModel = vm(1L)
        advanceUntilIdle()

        playlistRepo.playlists.value = listOf(fakePlaylist(1L, "Metal"))
        advanceUntilIdle()

        val name = (viewModel.uiState.value.playlist as AsyncResult.Success).data.name
        assertEquals("Metal", name)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Fakes
// ─────────────────────────────────────────────────────────────────────────────

class FakePlaylistRepository : PlaylistRepository {
    val playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val songs = MutableStateFlow<List<Song>>(emptyList())

    var lastDeletedPlaylistId: Long? = null
    var lastRenamed: Pair<Long, String>? = null
    var lastRemovedFromPlaylist: Pair<Long, Long>? = null
    var lastAddedToPlaylist: Pair<Long, Long>? = null

    override fun getAllPlaylists(): Flow<List<Playlist>> = playlists
    override fun getPlaylistById(id: Long): Flow<Playlist?> =
        playlists.map { it.find { p -> p.id == id } }

    override fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> = songs

    override suspend fun createPlaylist(name: String): Long {
        val id = (playlists.value.maxOfOrNull { it.id } ?: 0L) + 1L
        playlists.value += Playlist(id, name, 0, 0L, 0L, 0L, thumbnailUri = null)
        return id
    }

    override suspend fun renamePlaylist(id: Long, newName: String) {
        lastRenamed = Pair(id, newName)
        playlists.value = playlists.value.map { if (it.id == id) it.copy(name = newName) else it }
    }

    override suspend fun deletePlaylist(id: Long) {
        lastDeletedPlaylistId = id
        playlists.value = playlists.value.filter { it.id != id }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        lastAddedToPlaylist = Pair(playlistId, songId)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        lastRemovedFromPlaylist = Pair(playlistId, songId)
    }

    override suspend fun reorderSongs(playlistId: Long, fromIndex: Int, toIndex: Int) {}
}

class FakePlayerForPlaylist : PlayerRepository {
    override val playerState: Flow<com.wavora.app.domain.model.PlayerState> =
        MutableStateFlow(com.wavora.app.domain.model.PlayerState.Empty)

    var lastPlayAllSongs: List<Song>? = null
    var lastPlayAllStartIndex: Int? = null

    override suspend fun play(song: Song) {}
    override suspend fun playAll(songs: List<Song>, startIndex: Int) {
        lastPlayAllSongs = songs
        lastPlayAllStartIndex = startIndex
    }

    override suspend fun pause() {}
    override suspend fun resume() {}
    override suspend fun stop() {}
    override suspend fun skipToNext() {}
    override suspend fun skipToPrevious() {}
    override suspend fun seekTo(positionMs: Long) {}
    override suspend fun setShuffleEnabled(enabled: Boolean) {}
    override suspend fun setRepeatMode(mode: RepeatMode) {}
    override suspend fun addToQueue(song: Song) {}
    override suspend fun removeFromQueue(index: Int) {}
    override suspend fun moveQueueItem(fromIndex: Int, toIndex: Int) {}
    override suspend fun clearQueue() {}
    override suspend fun setSleepTimer(durationMs: Long) {}
}
