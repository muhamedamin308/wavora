package com.wavora.app.ui.screens.player

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
sealed interface PlayerEvent {
    data class ShowError(val message: String) : PlayerEvent
    data object NavigateUp : PlayerEvent
}