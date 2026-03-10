package com.wavora.app.di

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkerFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Binds [HiltWorkerFactory] so WorkManager knows how to inject [LibraryScanWorker].
 *
 * Also requires the app to call [androidx.work.Configuration.Builder.setWorkerFactory]
 * with the Hilt factory — done in [WavoraApplication].
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {
    @Binds
    abstract fun bindWorkerFactory(
        factory: HiltWorkerFactory,
    ): WorkerFactory
}