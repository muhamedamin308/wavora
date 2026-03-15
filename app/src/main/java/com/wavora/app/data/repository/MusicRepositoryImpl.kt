package com.wavora.app.data.repository

import android.content.Context
import android.util.Log
import com.wavora.app.data.local.dao.AlbumDao
import com.wavora.app.data.local.dao.ArtistDao
import com.wavora.app.data.local.dao.PlayHistoryDao
import com.wavora.app.data.local.dao.SongDao
import com.wavora.app.data.local.entity.PlayHistoryEntity
import com.wavora.app.data.local.entity.SongEntity
import com.wavora.app.data.local.entity.mappers.toDomain
import com.wavora.app.data.mediastore.MediaStoreScanner
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Artist
import com.wavora.app.domain.model.MusicFolder
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.model.SortOrder
import com.wavora.app.domain.repository.interfaces.MusicRepository
import com.wavora.app.domain.repository.results.ScanResult
import com.wavora.app.domain.repository.results.SearchResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
/**
 * Concrete implementation of [MusicRepository].
 *
 * Data flow:
 *   MediaStore (device files) ──scan──► MediaStoreScanner
 *                                             │
 *                                       ScanResult entities
 *                                             │
 *                                        diff + upsert
 *                                             │
 *                                       Room Database
 *                                             │
 *                                        Flow<List<Entity>>
 *                                             │
 *                                        .map { toDomain() }
 *                                             │
 *                                       UI / ViewModel
 *
 * Caching strategy:
 *   Room is the single source of truth. MediaStore scans only write to Room;
 *   all reads come from Room. This means:
 *   - The library is available instantly (no scan needed) after first launch.
 *   - WAVORA-owned fields (isFavorite, playCount) survive rescans.
 *   - Offline browsing works even if storage is unmounted.
 *
 * Threading:
 *   - All Room Flows are collected on [Dispatchers.IO] automatically.
 *   - [scanLibrary] is a suspend function — called from WorkManager on IO.
 *   - Mappers (toDomain) run inline on the collection dispatcher.
 */
@Singleton
class MusicRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playHistoryDao: PlayHistoryDao,
    private val mediaStoreScanner: MediaStoreScanner,
) : MusicRepository {

    // Songs
    override fun getAllSongs(sortOrder: SortOrder): Flow<List<Song>> =
        when (sortOrder) {
            SortOrder.TITLE_ASC -> songDao.getAllSongsByTitle()
            SortOrder.TITLE_DESC -> songDao.getAllSongsByTitleDesc()
            SortOrder.ARTIST_ASC -> songDao.getAllSongsByArtist()
            SortOrder.DURATION_ASC -> songDao.getAllSongsByDurationAsc()
            SortOrder.DURATION_DESC -> songDao.getAllSongsByDurationDesc()
            SortOrder.DATE_ADDED_ASC -> songDao.getAllSongsByDateAddedAsc()
            SortOrder.DATE_ADDED_DESC -> songDao.getAllSongsByDateAddedDesc()
        }.map { entities -> entities.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun getSongById(id: Long): Flow<Song?> =
        songDao.getSongById(id).map { it?.toDomain() }

    override fun getFavouriteSongs(): Flow<List<Song>> =
        songDao.getFavoriteSongs().map { it.map(SongEntity::toDomain) }

    override suspend fun toggleFavourite(songId: Long) {
        withContext(Dispatchers.IO) {
            songDao.toggleFavorite(songId)
        }
    }

    // Albums

    override fun getAllAlbums(): Flow<List<Album>> =
        albumDao.getAllAlbums()
            .map { entities -> entities.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun getAlbumById(albumId: Long): Flow<Album?> =
        albumDao.getAlbumById(albumId)
            .map { it?.toDomain() }

    override fun getSongsForAlbum(albumId: Long): Flow<List<Song>> =
        songDao.getSongsForAlbum(albumId)
            .map { it.map(SongEntity::toDomain) }

    override fun getTotalSongCount(): Flow<Int> = songDao.getSongCount()

    // Artists

    override fun getAllArtists(): Flow<List<Artist>> =
        artistDao.getAllArtists()
            .map { entities -> entities.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun getArtistById(artistId: Long): Flow<Artist?> =
        artistDao.getArtistById(artistId)
            .map { it?.toDomain() }

    override fun getAlbumsForArtist(artistId: Long): Flow<List<Album>> =
        albumDao.getAlbumsForArtist(artistId)
            .map { it.map { entity -> entity.toDomain() } }

    override fun getSongsForArtist(artistId: Long): Flow<List<Song>> =
        songDao.getSongsForArtist(artistId)
            .map { it.map(SongEntity::toDomain) }

    // Folders

    override fun getAllFolders(): Flow<List<MusicFolder>> =
        songDao.getAllSongsByTitle()
            .map { songs ->
                songs
                    .groupBy { entity ->
                        entity.path.substringAfterLast('/')
                    }
                    .map { (folderPath, folderSong) ->
                        MusicFolder(
                            path = folderPath,
                            name = folderPath.substringAfterLast('/'),
                            songCount = folderSong.size,
                            totalDuration = folderSong.sumOf { it.durationMs }
                        )
                    }
                    .sortedBy { it.name.lowercase() }
            }
            .distinctUntilChanged()

    override fun getSongsForFolder(folderPath: String): Flow<List<Song>> =
        songDao.getSongsInFolder(folderPath)
            .map { it.map(SongEntity::toDomain) }

    // Search

    override fun search(query: String): Flow<SearchResult> {
        if (query.isBlank()) return flowOf(SearchResult())

        val sanitised = query.trim().replace("\"", "")
        val ftsQuery = "\"$sanitised\"*" // FTS4 prefix match

        val songsFlow = songDao.searchSongs(ftsQuery)
            .map { it.map(SongEntity::toDomain) }
        val albumsFlow = albumDao.searchAlbums(ftsQuery)
            .map { it.map { entity -> entity.toDomain() } }
        val artistFlow = artistDao.searchArtists(ftsQuery)
            .map { it.map { entity -> entity.toDomain() } }

        return combine(songsFlow, albumsFlow, artistFlow) { songs, albums, artists ->
            SearchResult(
                songs = songs,
                albums = albums,
                artists = artists
            )
        }
    }


    // ── Library scan ──────────────────────────────────────────────────────────

    /**
     * Full diff scan: compares MediaStore with Room cache and reconciles differences.
     *
     * Algorithm:
     *  1. Scan MediaStore → get fresh entities
     *  2. Load current MediaStore IDs from Room
     *  3. Compute: added, updated, removed sets
     *  4. Upsert added/updated songs; delete removed songs
     *  5. Rebuild album and artist caches from updated songs
     *
     * This is O(n) in the number of songs — acceptable for libraries up to
     * ~50,000 songs. For larger libraries, a hash-based diff could be used.
     *
     * WAVORA-owned fields (isFavorite, playCount) are preserved via a targeted
     * UPDATE that only touches MediaStore-sourced columns.
     */

    override suspend fun scanLibrary(): ScanResult = withContext(Dispatchers.IO) {
        Log.d("MusicRepo", "Library Scan started")

        // Step 1. Media store scan
        val scanData = mediaStoreScanner.scan()
        val freshSongs = scanData.songs

        // step 2. load existing mediastore ids from room
        val cachedIds = songDao.getAllMediaStoreIds().toSet()
        val freshIds = freshSongs.map { it.mediaStoreId }.toSet()

        // step 3. classify songs
        val added = freshSongs.filter { it.mediaStoreId !in cachedIds }
        val updated = freshSongs.filter { it.mediaStoreId in cachedIds }
        val removed = cachedIds - freshIds

        // step 4. songs
        if (added.isNotEmpty()) songDao.insertAll(added)

        for (song in updated) {
            songDao.updateMetadata(
                mediaStoreId = song.mediaStoreId,
                title = song.title,
                titleKey = song.titleKey,
                artistName = song.artistName,
                artistId = song.artistId,
                albumName = song.albumName,
                albumId = song.albumId,
                durationMs = song.durationMs,
                path = song.path,
                contentUri = song.contentUri,
                albumArtUri = song.albumArtUri,
                dateAdded = song.dateAdded,
                trackNumber = song.trackNumber,
                year = song.year,
                bitrate = song.bitrate,
                mimeType = song.mimeType,
                sizeBytes = song.sizeBytes,
            )
        }

        val removedCount = if (removed.isNotEmpty())
            songDao.deleteRemovedSongs(removed.toList())
        else 0

        // step 5. rebuild albums and artists from fresh scan data
        // clear and re-insert - simpler than diffing nested aggregates
        val freshAlbumIds = scanData.albums.map { it.mediaStoreAlbumId }
        if (freshAlbumIds.isNotEmpty()) {
            albumDao.deleteRemovedAlbums(freshAlbumIds)
            albumDao.insertAll(scanData.albums)
        }

        val freshArtistIds = scanData.artists.map { it.mediaStoreArtistId }
        if (freshArtistIds.isNotEmpty()) {
            artistDao.deleteRemovedArtists(freshArtistIds)
            artistDao.insertAll(scanData.artists)
        }

        val result = ScanResult(
            added = added.size,
            updated = updated.size,
            removed = removedCount
        )

        Log.d(
            "MusicRepo",
            "Scan complete — added:${result.added} updated:${result.updated} removed:${result.removed}"
        )

        result
    }

    // play history

    /**
     * Records a play event. Called by PlayerRepository when a song starts.
     * Batched writes (every N seconds) are handled in Phase 3.
     */
    suspend fun recordPlay(songId: Long, durationPlayedMs: Long) {
        withContext(Dispatchers.IO) {
            playHistoryDao.insert(
                PlayHistoryEntity(
                    songId = songId,
                    durationPlayedMs = durationPlayedMs
                )
            )
            songDao.incrementPlayCount(songId)
        }
    }
}