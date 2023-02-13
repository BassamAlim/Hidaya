@file:OptIn(ExperimentalAnimationApi::class)

package bassamalim.hidaya.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import bassamalim.hidaya.ui.components.BottomNavItem

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
}*/


val inFromBottom = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideInVertically(
        initialOffsetY = { 1000 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(300))
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
    ) + fadeIn(animationSpec = tween(300))
}

val outToTop = { an: AnimatedContentScope<NavBackStackEntry> ->
    slideOutVertically(
        targetOffsetY = { 500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
}


val bottomNavBarWeightMap = hashMapOf(
    BottomNavItem.Home.route to 1,
    BottomNavItem.Prayers.route to 2,
    BottomNavItem.Quran.route to 3,
    BottomNavItem.Athkar.route to 4,
    BottomNavItem.More.route to 5
)

val BottomNavEnter = { an: AnimatedContentScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) inFromLeft
    else inFromRight
}

val BottomNavExit = { an: AnimatedContentScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) outToRight
    else outToLeft
}

val BottomNavPopEnter = { an: AnimatedContentScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) inFromLeft
    else inFromRight
}

val BottomNavPopExit = { an: AnimatedContentScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) outToRight
    else outToLeft
}

val inFromRight = slideInHorizontally(
    initialOffsetX = { 1000 },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

val outToLeft = slideOutHorizontally(
    targetOffsetX = { -500 },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

val inFromLeft = slideInHorizontally(
    initialOffsetX = { -1000 },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(100))

val outToRight = slideOutHorizontally(
    targetOffsetX = { 500 },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))