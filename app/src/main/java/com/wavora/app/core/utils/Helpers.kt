package com.wavora.app.core.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import com.wavora.app.core.utils.Constants.NAV_ANIM_DURATION

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

// Transition Helpers
fun AnimatedContentTransitionScope<NavBackStackEntry>.lateralSlideIn(
    direction: AnimatedContentTransitionScope.SlideDirection,
) = slideIntoContainer(
    direction,
    tween(NAV_ANIM_DURATION)
) + fadeIn(tween(NAV_ANIM_DURATION))

fun AnimatedContentTransitionScope<NavBackStackEntry>.lateralSlideOut(
    direction: AnimatedContentTransitionScope.SlideDirection,
) = slideOutOfContainer(
    direction,
    tween(NAV_ANIM_DURATION)
) + fadeOut(tween(NAV_ANIM_DURATION))


fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromBottom() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Up,
        tween(NAV_ANIM_DURATION)
    ) + fadeIn(tween(NAV_ANIM_DURATION))


fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToBottom() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Down,
        tween(NAV_ANIM_DURATION)
    ) + fadeOut(tween(NAV_ANIM_DURATION))
