package com.wavora.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wavora.app.data.repository.PlayerRepositoryImpl
import com.wavora.app.domain.model.UserPreferences
import com.wavora.app.domain.repository.preferences.UserPreferencesRepository
import com.wavora.app.navigation.WavoraNavHost
import com.wavora.app.ui.theme.WavoraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var playerRepository: PlayerRepositoryImpl

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called BEFORE super.onCreate() to attach splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            val prefs by userPreferencesRepository.preferences
                .collectAsState(UserPreferences())

            val systemDark = isSystemInDarkTheme()
            val useDark = prefs.isDarkTheme || systemDark

            WavoraTheme(
                darkTheme = useDark,
                dynamicColor = prefs.useDynamicColors,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WavoraNavHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleAudioIntent(intent)
    }

    private fun handleAudioIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (intent.action != Intent.ACTION_VIEW) return
        val type = intent.type ?: contentResolver.getType(uri) ?: return
        if (!type.startsWith("audio/")) return
        // Route to player — service will resolve the URI via MediaStore
        android.util.Log.d("MainActivity", "Audio deep link: $uri")
        // Phase 11: full deep-link routing to NowPlaying with the specific track
    }

    override fun onStart() {
        super.onStart(); playerRepository.connect()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) playerRepository.disconnect(); super.onDestroy()
    }
}
