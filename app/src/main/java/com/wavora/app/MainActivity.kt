package com.wavora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wavora.app.ui.theme.WavoraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called BEFORE super.onCreate() to attach splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Draws content behind the system bars for a truly immersive feel.
        // Each screen is responsible for providing correct WindowInsets padding.
        enableEdgeToEdge()

        setContent {
            WavoraTheme {
                Surface() {
                    Text(
                        modifier = Modifier.fillMaxSize(),
                        text = "Hello Wavora!",
                        fontSize = 40.sp
                    )
                }
            }
        }
    }
}
