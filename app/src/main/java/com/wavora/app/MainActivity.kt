package com.wavora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wavora.app.navigation.WavoraNavHost
import com.wavora.app.ui.theme.WavoraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
}
