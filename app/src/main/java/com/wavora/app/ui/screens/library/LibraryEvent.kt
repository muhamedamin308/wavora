package com.wavora.app.ui.screens.library

import com.wavora.app.domain.model.Song

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/** One-shot events fired to the Library screen. */
sealed interface LibraryEvent {
    data object RequestPermission: LibraryEvent
    data class ShowError(val message: String): LibraryEvent
    data class NavigateToSong(val song: Song): LibraryEvent
}