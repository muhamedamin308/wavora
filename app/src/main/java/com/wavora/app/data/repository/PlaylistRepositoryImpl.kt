package com.wavora.app.data.repository

import com.wavora.app.data.local.dao.PlaylistDao
import com.wavora.app.data.local.entity.PlaylistEntity
import com.wavora.app.data.local.entity.PlaylistSongCrossRef
import com.wavora.app.data.local.entity.SongEntity
import com.wavora.app.data.local.entity.mappers.toDomain
import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.Song
import com.wavora.app.domain.repository.interfaces.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> =
        playlistDao.getAllPlaylists()
            .map { entities ->
                entities.map { entity ->
                    // Sync song count and duration for each playlist
                    // In production, consider caching counts in PlaylistEntity
                    entity.toDomain(songCount = 0, totalDuration = 0L)
                }
            }
            .distinctUntilChanged()

    override fun getPlaylistById(id: Long): Flow<Playlist?> =
        playlistDao.getPlaylistById(id)
            .map { entity -> entity?.toDomain(songCount = 0, totalDuration = 0L) }

    override fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistDao.getSongsInPlaylist(playlistId)
            .map { entities -> entities.map(SongEntity::toDomain) }

    override suspend fun createPlaylist(name: String): Long =
        withContext(Dispatchers.IO) {
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    name = name
                )
            )
        }

    override suspend fun renamePlaylist(id: Long, newName: String) =
        withContext(Dispatchers.IO) {
            playlistDao.renamePlaylist(id, newName.trim())
        }

    override suspend fun deletePlaylist(id: Long) =
        withContext(Dispatchers.IO) {
            playlistDao.deletePlaylist(id)
        }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) =
        withContext(Dispatchers.IO) {
            val nextPosition = (playlistDao.getMaxPosition(playlistId) ?: -1) + 1
            playlistDao.insertPlaylistSongCrossRef(
                PlaylistSongCrossRef(
                    playlistId = playlistId,
                    songId = songId,
                    position = nextPosition,
                )
            )
            playlistDao.touchUpdatedAt(playlistId)
        }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        withContext(Dispatchers.IO) {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
            playlistDao.touchUpdatedAt(playlistId)
        }

    override suspend fun reorderSongs(
        playlistId: Long,
        fromIndex: Int,
        toIndex: Int,
    ) = withContext(Dispatchers.IO) {
        playlistDao.reorderSong(playlistId, fromIndex, toIndex)
    }
}