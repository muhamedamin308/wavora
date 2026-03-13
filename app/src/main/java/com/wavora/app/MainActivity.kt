package com.wavora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wavora.app.data.repository.PlayerRepositoryImpl
import com.wavora.app.navigation.WavoraNavHost
import com.wavora.app.ui.theme.WavoraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var playerRepository: PlayerRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called BEFORE super.onCreate() to attach splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            WavoraTheme {
                Surface(
                    modifier =
                        Modifier.fillMaxSize()
                ) {
                    WavoraNavHost()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        playerRepository.connect()
    }

    override fun onStop() {
        super.onStop()
        // Do NOT disconnect here — the MediaController must survive background
        // playback so the notification controls keep working.
        // We only disconnect in onDestroy (when the user fully exits).
    }

    override fun onDestroy() {
        if (isFinishing) playerRepository.disconnect()
        super.onDestroy()
    }
}
