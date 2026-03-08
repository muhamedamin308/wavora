package com.wavora.app.ui.screens.player

import com.wavora.app.domain.model.PlayerState

/**
 * @author Muhamed Amin Hassan on 08,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class PlayerUiState(
    val playerState: PlayerState = PlayerState.Empty,
    val isLyricsVisible: Boolean = false,
    val dominantColor: Long = 0L, // ARGB from album art — drives dynamic theming
)
