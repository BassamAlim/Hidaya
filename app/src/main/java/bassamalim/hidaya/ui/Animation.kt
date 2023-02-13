@file:OptIn(ExperimentalAnimationApi::class)

package bassamalim.hidaya.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

/*sealed class AnimationPackage constructor(
    val enter: (AnimatedContentScope<NavBackStackEntry>) -> EnterTransition,
    val exit: (AnimatedContentScope<NavBackStackEntry>) -> ExitTransition,
    val popEnter: (AnimatedContentScope<NavBackStackEntry>) -> EnterTransition,
    val popExit: (AnimatedContentScope<NavBackStackEntry>) -> ExitTransition
) {
    object Vertical: AnimationPackage(
        enter = inFromBottom,
        exit = outToBottom,
        popEnter = inFromTop,
        popExit = outToTop
    )

    object Right: AnimationPackage(
        enter = inFromRight,
        exit = outToLeft,
        popEnter = inFromLeft,
        popExit = outToRight
    )

    object Left: AnimationPackage(
        enter = inFromLeft,
        exit = outToRight,
        popEnter = inFromRight,
        popExit = outToLeft
    )
}*/


val inFromBottom = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideInVertically(
        initialOffsetY = { 1000 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(100))
}

val outToBottom = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideOutVertically(
        targetOffsetY = { -500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
}

val inFromTop = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideInVertically(
        initialOffsetY = { -1000 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(100))
}

val outToTop = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideOutVertically(
        targetOffsetY = { 500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
}


val inFromRight = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideInHorizontally(
        initialOffsetX = { 1000 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(100))
}

val outToLeft = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideOutHorizontally(
        targetOffsetX = { -500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
}

val inFromLeft = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideInHorizontally(
        initialOffsetX = { -1000 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(100))
}

val outToRight = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideOutHorizontally(
        targetOffsetX = { 500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
}