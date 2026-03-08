package com.wavora.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.wavora.app.navigation.WavoraRoutes

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
/**
 * Describes a bottom navigation tab.
 */
private data class BottomNavItem(
    val route: WavoraRoutes,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    BottomNavItem(
        route = WavoraRoutes.Library,
        label = "Library",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic,
    ),
    BottomNavItem(
        route = WavoraRoutes.Search,
        label = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
    ),
    BottomNavItem(
        route = WavoraRoutes.Settings,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
)

/**
 * WAVORA bottom navigation bar.
 *
 * Uses Material 3 [NavigationBar] with filled/outlined icon pairs for
 * selected/unselected states — no labels on unselected items for a cleaner look.
 *
 * @param currentDestination The active [NavDestination] for highlighting the correct tab.
 * @param onNavigate         Callback with the route string to navigate to.
 */

@Composable
fun WavoraBottomNav(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar {
        navItems.forEach { item ->
            val isSelected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.route.route }
                ?: false

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                },
                alwaysShowLabel = false
            )
        }
    }
}