package com.wavora.app.navigation

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

sealed class WavoraRoutes(val route: String) {

    // Root Screen
    /** Main library screen — app's home destination. */
    data object Library : WavoraRoutes("Library")

    /** Full-Screen Now Playing. */
    data object NowPlaying : WavoraRoutes("now_playing")

    /** Playback Queue. */
    data object PlaybackQueue : WavoraRoutes("queue")

    /** Universal Search. */
    data object Search : WavoraRoutes("search")

    /** App Settings. */
    data object Settings : WavoraRoutes("Settings")

    /** Equalizer Screen. */
    data object Equalizer : WavoraRoutes("equalizer")

    // Library Detail Destinations
    data object AlbumDetails : WavoraRoutes("album/{albumId}") {
        const val ARG_ALBUM_ID = "albumId"
        fun createRoute(albumId: Long) = "album/$albumId"
    }

    data object ArtistDetails : WavoraRoutes("artist/{artistId}") {
        const val ARG_ARTIST_ID = "artistId"
        fun createRoute(artistId: Long) = "artist/$artistId"
    }

    data object PlaylistDetails : WavoraRoutes("playlist/{playlistId}") {
        const val ARG_PLAYLIST_ID = "playlistId"
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }

    data object FolderDetails : WavoraRoutes("folder/{folderPath}") {
        const val ARG_FOLDER_PATH = "folderPath"
        fun createRoute(encodedPath: Long) = "folder/$encodedPath"
    }
}

// All top-level bottom-navigation destinations
val TOP_LEVEL_ROUTES = listOf(
    WavoraRoutes.Library,
    WavoraRoutes.Search,
    WavoraRoutes.Settings
)