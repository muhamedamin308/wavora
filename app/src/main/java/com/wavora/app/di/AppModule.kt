package com.wavora.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.wavora.app.core.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

// DataStore delegate — one instance per process
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFS_NAME
)

/**
 * Application-scoped Hilt module.
 *
 * Provides:
 *  - [DataStore<Preferences>] — app settings persistence
 *  - [ApplicationContext] is already provided by Hilt automatically
 *
 * Repository bindings are declared in [RepositoryModule] (Phase 2).
 * Database bindings are declared in [DatabaseModule] (Phase 2).
 * Player bindings are declared in [PlayerModule] (Phase 3).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.dataStore
}