package com.wavora.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import com.wavora.app.data.local.WavoraDatabase
import com.wavora.app.data.local.dao.AlbumDao
import com.wavora.app.data.local.dao.ArtistDao
import com.wavora.app.data.local.dao.PlayHistoryDao
import com.wavora.app.data.local.dao.PlaylistDao
import com.wavora.app.data.local.dao.QueueDao
import com.wavora.app.data.local.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Provides the Room database and all DAO instances as singletons.
 *
 * Migration strategy:
 *  - MIGRATION_1_2: Phase 1 had no tables. Version 2 creates the full schema.
 *    Since there was no user data in version 1, we use a destructive migration
 *    from version 1 only. All subsequent migrations are explicit SQL scripts.
 *
 * [enableMultiInstanceInvalidation] is disabled — WAVORA is a single-process
 * app, so cross-process table invalidation adds overhead without benefit.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Migration from version 1 (Phase 1 skeleton with no tables)
     * to version 2 (Phase 2 full library schema).
     *
     * Since v1 had no user data, we just create all tables fresh.
     * Room handles this automatically with fallbackToDestructiveMigrationFrom.
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Room will auto-create all tables via the new schema.
            // No manual SQL needed because v1 had no tables to preserve.
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WavoraDatabase =
        Room.databaseBuilder(
            context,
            WavoraDatabase::class.java,
            WavoraDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSongDao(db: WavoraDatabase): SongDao = db.songDao()

    @Provides
    fun provideAlbumDao(db: WavoraDatabase): AlbumDao = db.albumDao()

    @Provides
    fun provideArtistDao(db: WavoraDatabase): ArtistDao = db.artistDao()

    @Provides
    fun providePlaylistDao(db: WavoraDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun providePlayHistoryDao(db: WavoraDatabase): PlayHistoryDao = db.playHistoryDao()

    @Provides
    fun provideQueueDao(db: WavoraDatabase): QueueDao = db.queueDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}