package com.wavora.app.di

import com.wavora.app.data.repository.PlayerRepositoryImpl
import com.wavora.app.domain.repository.interfaces.PlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Muhamed Amin Hassan on 13,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun providePlayerRepository(implementation: PlayerRepositoryImpl): PlayerRepository {
        implementation.connect()
        return implementation
    }
}