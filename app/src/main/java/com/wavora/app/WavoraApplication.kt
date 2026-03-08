package com.wavora.app

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

@HiltAndroidApp
class WavoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        enableStrictMode()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        /**
         * StrictMode catches common bugs during development:
         *  - Disk reads / writes on the main thread (causes jank)
         *  - Leaked Closeable objects (Cursors, SQLite, streams)
         *  - Incorrect ViewModel usage
         *
         * Crashes in debug only; release builds are unaffected.
         */
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .build()
        )
    }
}