package com.wavora.app.ui.screens.smartplaylist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @author Muhamed Amin Hassan on 18,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

enum class SmartPlaylistType(
    val displayName: String,
    val icon: ImageVector,
    val subtitle: String,
) {
    MOST_PLAYED(
        displayName = "Most Played",
        Icons.Filled.BarChart,
        subtitle = "Your top 50 songs by play count",
    ),
    RECENTLY_PLAYED(
        displayName = "Recently Played",
        icon = Icons.Filled.History,
        subtitle = "Last 50 songs you listened to",
    ),
    RECENTLY_ADDED(
        displayName = "Recently Added",
        icon = Icons.Filled.FiberNew,
        subtitle = "Last 50 songs added to your library",
    ),
    FAVOURITES(
        displayName = "Favourites",
        icon = Icons.Filled.Favorite,
        subtitle = "Songs you've marked as favourite",
    ),
}