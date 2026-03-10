package com.wavora.app.di

import com.wavora.app.data.repository.MusicRepositoryImpl
import com.wavora.app.data.repository.PlaylistRepositoryImpl
import com.wavora.app.domain.repository.interfaces.MusicRepository
import com.wavora.app.domain.repository.interfaces.PlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Binds repository interfaces to their concrete implementations.
 *
 * Using @Binds instead of @Provides:
 *  - Zero overhead — no lambda wrapper, direct vtable binding.
 *  - Compiler validates that the implementation actually implements the interface.
 *  - Enables swapping implementations per build variant (e.g. fake for UI tests).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(implementation: MusicRepositoryImpl): MusicRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(implementation: PlaylistRepositoryImpl): PlaylistRepository
}