package com.wavora.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * WAVORA shape tokens.
 *
 * Strategy: soft, modern rounded corners — approachable, not corporate.
 *  - extraSmall : chips, badges, seek bar thumb
 *  - small      : song list item background on selection
 *  - medium     : album art thumbnails, bottom sheets
 *  - large      : album detail card, mini player
 *  - extraLarge : Now Playing album art, full-screen cards
 */

val WavoraShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

// Convenience aliases for use in composable
val ShapeCircle = RoundedCornerShape(50) // fully rounded — play button
val ShapeCard = RoundedCornerShape(12.dp) // list item cards
val ShapeAlbumArt = RoundedCornerShape(8.dp) // consistent album art rounding
val ShapeSheet = RoundedCornerShape(
    // bottom sheet — only top corners
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp,
)
val ShapeMiniPlayer = RoundedCornerShape(16.dp) // mini player floating card
