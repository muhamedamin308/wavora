package com.wavora.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wavora.app.core.utils.Constants
import com.wavora.app.ui.theme.DarkBackground
import com.wavora.app.ui.theme.PlaybackAccent
import com.wavora.app.ui.theme.Violet30
import com.wavora.app.ui.theme.Violet50
import kotlinx.coroutines.delay


/**
 * Onboarding screen shown on first launch before storage permission is granted.
 *
 * Design:
 *  - Three animated feature cards slide in sequentially (staggered 120ms apart)
 *    to communicate WAVORA's value prop before asking for permission.
 *  - Large animated icon pulses to draw attention.
 *  - "Get started" button triggers the OS permission dialog.
 *  - If permission was previously denied, a rationale text appears explaining
 *    why it's needed — matching Google's recommended UX.
 *  - Entirely stateless — state lives in [LibraryViewModel] via the permission
 *    launcher pattern already in place.
 *
 * This screen is shown by [LibraryScreen] when [hasStoragePermission] is false
 * AND [isFirstLaunch] is true. Subsequent denials show [PermissionRationale].
 */
@Composable
fun OnboardingScreen(
    onGrantPermission: () -> Unit,
    showRationale: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var animVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        animVisible = true
    }

    val iconScale by animateFloatAsState(
        targetValue = if (animVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Violet30, DarkBackground),
                    endY = 1200f,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            // ── App icon ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(iconScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Violet50, Violet30),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.GraphicEq,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = Color.White,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── App name + tagline ─────────────────────────────────────────────
            Text(
                text = Constants.APP_NAME,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Where Every Wave Tells a Story",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            // ── Feature cards — staggered entrance ────────────────────────────
            val features = listOf(
                Triple(
                    Icons.Filled.LibraryMusic, "Your music, offline",
                    "WAVORA plays music stored on your device. No subscriptions. No internet needed."
                ),
                Triple(
                    Icons.Filled.PhoneAndroid, "Zero data collection",
                    "Your listening habits stay private. Nothing is uploaded or shared."
                ),
                Triple(
                    Icons.Filled.Lock, "One permission only",
                    "We only need access to audio files. Nothing else, ever."
                ),
            )

            features.forEachIndexed { index, (icon, title, body) ->
                val visible = animVisible
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { 60 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    ),
                ) {
                    LaunchedEffect(Unit) { delay(index * 120L) }
                    FeatureCard(icon = icon, title = title, body = body)
                }
                Spacer(Modifier.height(12.dp))

                Spacer(Modifier.height(32.dp))

                // ── Rationale text (shown after first denial) ─────────────────────
                if (showRationale) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Audio permission was denied. WAVORA can't find your music without it. Tap below to try again.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── CTA button ─────────────────────────────────────────────────────
                Button(
                    onClick = onGrantPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlaybackAccent,
                        contentColor = Color.White,
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Icon(Icons.Filled.MusicNote, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (showRationale) "Try again" else "Get started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, body: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PlaybackAccent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(22.dp), tint = PlaybackAccent)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                )
            }
        }
    }
}
