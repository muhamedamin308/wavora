package com.wavora.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wavora.app.data.local.dao.AlbumDao
import com.wavora.app.data.local.dao.ArtistDao
import com.wavora.app.data.local.dao.PlayHistoryDao
import com.wavora.app.data.local.dao.PlaylistDao
import com.wavora.app.data.local.dao.QueueDao
import com.wavora.app.data.local.dao.SongDao
import com.wavora.app.data.local.entity.AlbumEntity
import com.wavora.app.data.local.entity.ArtistEntity
import com.wavora.app.data.local.entity.PlayHistoryEntity
import com.wavora.app.data.local.entity.PlaylistEntity
import com.wavora.app.data.local.entity.PlaylistSongCrossRef
import com.wavora.app.data.local.entity.QueueEntity
import com.wavora.app.data.local.entity.SongEntity
import com.wavora.app.data.local.entity.SongFtsEntity

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@Database(
    entities = [
        SongEntity::class,
        SongFtsEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlayHistoryEntity::class,
        QueueEntity::class,
    ],
    version = 2,
    exportSchema = true
)
abstract class WavoraDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun queueDao(): QueueDao

    companion object {
        const val DATABASE_NAME = "wavora_database"
    }
}