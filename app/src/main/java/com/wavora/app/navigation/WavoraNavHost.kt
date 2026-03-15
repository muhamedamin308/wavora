package com.wavora.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wavora.app.core.utils.Constants.NAV_ANIM_DURATION
import com.wavora.app.core.utils.lateralSlideIn
import com.wavora.app.core.utils.lateralSlideOut
import com.wavora.app.core.utils.slideInFromBottom
import com.wavora.app.core.utils.slideOutToBottom
import com.wavora.app.ui.components.MiniPlayer
import com.wavora.app.ui.components.WavoraBottomNav
import com.wavora.app.ui.screens.library.LibraryScreen
import com.wavora.app.ui.screens.player.NowPlayingScreen
import com.wavora.app.ui.screens.playlist.PlaylistScreen
import com.wavora.app.ui.screens.queue.QueueScreen
import com.wavora.app.ui.screens.search.SearchScreen
import com.wavora.app.ui.screens.settings.SettingsScreen

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Root navigation graph for WAVORA.
 *
 * Architecture decisions:
 *  - [rememberNavController] is created here and passed down to [WavoraBottomNav]
 *    so the bottom nav can observe the current destination without a shared ViewModel.
 *  - [com.wavora.app.ui.components.MiniPlayer] lives inside [Scaffold] so it appears above every screen content
 *    but below the bottom nav bar.
 *  - Transitions: lateral slide for peer screens; vertical slide-up for Now Playing.
 *  - All screen composables are stateless — they receive state and callbacks via
 *    their own ViewModels injected by Hilt.
 */

@Composable
fun WavoraNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if we're on a top-level destination (show bottom nav + mini player)
    val isTopLevel = currentDestination?.hierarchy?.any { destination ->
        TOP_LEVEL_ROUTES.any { it.route == destination.route }
    } ?: true

    // Hide mini player on the full Now Playing screen
    val showMiniPlayer = currentDestination?.route !in listOf(
        WavoraRoutes.NowPlaying.route,
        WavoraRoutes.PlaybackQueue.route,
    )

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                WavoraBottomNav(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Avoid building a large back stack when tapping bottom nav items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = WavoraRoutes.Library.route,
                modifier = Modifier.fillMaxSize(),

                // Default transitions — smooth fade + lateral slide
                enterTransition = { lateralSlideIn(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { lateralSlideOut(AnimatedContentTransitionScope.SlideDirection.Left) },
                popEnterTransition = { lateralSlideIn(AnimatedContentTransitionScope.SlideDirection.Right) },
                popExitTransition = { lateralSlideOut(AnimatedContentTransitionScope.SlideDirection.Right) },
            ) {
                // Top-Level Destinations
                composable(WavoraRoutes.Library.route) {
                    LibraryScreen(
                        onNavigateToAlbum = { id ->
                            navController.navigate(
                                WavoraRoutes.AlbumDetails.createRoute(
                                    id
                                )
                            )
                        },
                        onNavigateToArtist = { id ->
                            navController.navigate(
                                WavoraRoutes.ArtistDetails.createRoute(
                                    id
                                )
                            )
                        },
                        onNavigateToPlaylist = { id ->
                            navController.navigate(
                                WavoraRoutes.PlaylistDetails.createRoute(
                                    id
                                )
                            )
                        },
                        onNavigateToNowPlaying = { navController.navigate(WavoraRoutes.NowPlaying.route) }
                    )
                }

                composable(WavoraRoutes.Search.route) {
                    SearchScreen(
                        onNavigateToAlbum = { id ->
                            navController.navigate(
                                WavoraRoutes.AlbumDetails.createRoute(
                                    id
                                )
                            )
                        },
                        onNavigateToArtist = { id ->
                            navController.navigate(
                                WavoraRoutes.ArtistDetails.createRoute(
                                    id
                                )
                            )
                        },
                        onNavigateToNowPlaying = { navController.navigate(WavoraRoutes.NowPlaying.route) },
                    )
                }

                composable(WavoraRoutes.Settings.route) {
                    SettingsScreen(
                        onNavigateToEqualizer = {
                            navController.navigate(WavoraRoutes.Equalizer.route)
                        }
                    )
                }

                // player Destinations
                composable(
                    route = WavoraRoutes.NowPlaying.route,
                    enterTransition = { slideInFromBottom() },
                    exitTransition = { fadeOut(tween(NAV_ANIM_DURATION)) },
                    popEnterTransition = { fadeIn(tween(NAV_ANIM_DURATION)) },
                    popExitTransition = { slideOutToBottom() },
                ) {
                    NowPlayingScreen(
                        onNavigateUp = {
                            navController.navigateUp()
                        },
                        onNavigateToQueue = {
                            navController.navigate(WavoraRoutes.PlaybackQueue.route)
                        }
                    )
                }

                composable(WavoraRoutes.PlaybackQueue.route) {
                    QueueScreen(onNavigateUp = { navController.navigateUp() })
                }

                composable(WavoraRoutes.Equalizer.route) {
                    // EqualizerScreen added in Phase 7
                    PlaceholderScreen("Equalizer")
                }

                // Details Destinations
                composable(
                    route = WavoraRoutes.AlbumDetails.route,
                    arguments = listOf(
                        navArgument(
                            WavoraRoutes.AlbumDetails.ARG_ALBUM_ID
                        ) {
                            type = NavType.LongType
                        }
                    )
                ) { backStack ->
                    val albumId =
                        backStack.arguments?.getLong(WavoraRoutes.AlbumDetails.ARG_ALBUM_ID)
                            ?: return@composable
                    PlaceholderScreen("Album Details - id: $albumId")
                }

                composable(
                    route = WavoraRoutes.ArtistDetails.route,
                    arguments = listOf(navArgument(WavoraRoutes.ArtistDetails.ARG_ARTIST_ID) {
                        type = NavType.LongType
                    }),
                ) { backStack ->
                    val artistId =
                        backStack.arguments?.getLong(WavoraRoutes.ArtistDetails.ARG_ARTIST_ID)
                            ?: return@composable
                    PlaceholderScreen("Artist Detail — id: $artistId")
                }

                composable(
                    route = WavoraRoutes.PlaylistDetails.route,
                    arguments = listOf(navArgument(WavoraRoutes.PlaylistDetails.ARG_PLAYLIST_ID) {
                        type = NavType.LongType
                    }),
                ) { backStack ->
                    val playlistId =
                        backStack.arguments?.getLong(WavoraRoutes.PlaylistDetails.ARG_PLAYLIST_ID)
                            ?: return@composable
                    PlaylistScreen(
                        playlistId = playlistId,
                        onNavigateUp = { navController.navigateUp() },
                    )
                }

                composable(
                    route = WavoraRoutes.FolderDetails.route,
                    arguments = listOf(navArgument(WavoraRoutes.FolderDetails.ARG_FOLDER_PATH) {
                        type = NavType.StringType
                    }),
                ) { backStack ->
                    val path =
                        backStack.arguments?.getString(WavoraRoutes.FolderDetails.ARG_FOLDER_PATH)
                            ?: return@composable
                    PlaceholderScreen("Folder: $path")
                }
            }

            // ── Mini Player sits above all content, pinned to bottom ──────
            if (showMiniPlayer) {
                MiniPlayer(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onExpand = { navController.navigate(WavoraRoutes.NowPlaying.route) },
                )
            }
        }
    }
}
