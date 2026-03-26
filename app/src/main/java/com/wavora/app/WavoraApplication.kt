package com.wavora.app

import android.app.Application
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import com.wavora.app.core.performance.CoilImageCacheConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * WAVORA Application entry point.
 *
 * Responsibilities:
 *  - Bootstrap Hilt's dependency injection graph
 *  - Configure WorkManager to use HiltWorkerFactory (required for @HiltWorker injection)
 *  - Enable StrictMode in debug builds to catch disk/network access on main thread,
 *    leaked SQLite cursors, and other violations early
 */
@HiltAndroidApp
class WavoraApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var coilImageCacheConfig: CoilImageCacheConfig

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.ENABLE_STRICT_MODE) android.util.Log.DEBUG
                else android.util.Log.ERROR
            )
            .build()

    override fun onCreate() {
        super.onCreate()
        // Install app-wide Coil ImageLoader with tuned cache settings
        Coil.setImageLoader(coilImageCacheConfig.build())
        if (BuildConfig.ENABLE_STRICT_MODE) {
            enableStrictMode()
        }
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                // penaltyDeath only in debug — crashes fast on violations
                .penaltyDeath()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectLeakedRegistrationObjects()
                // API 26+: catch explicit-termination violations (DataStore, WorkManager)
                .apply {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        detectContentUriWithoutPermission()
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        detectCredentialProtectedWhileLocked()
                    }
                }
                .penaltyLog()
                .build()   // No penaltyDeath on VM — would crash on Coil background threads
        )
    }
}
