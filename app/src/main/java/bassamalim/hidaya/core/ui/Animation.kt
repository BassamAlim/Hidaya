@file:OptIn(ExperimentalAnimationApi::class)

package bassamalim.hidaya.core.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import bassamalim.hidaya.features.main.BottomNavItem

val inFromBottom = { _: AnimatedContentScope<NavBackStackEntry> ->
    slideInVertically(
        initialOffsetY = { 500 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(200))
}

val outToBottom = { _: AnimatedContentScope<NavBackStackEntry> ->
    slideOutVertically(
        targetOffsetY = { -500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(200))
}

val inFromTop = { _: AnimatedContentScope<NavBackStackEntry> ->
    slideInVertically(
        initialOffsetY = { -500 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(200))
}

val outToTop = { _: AnimatedContentScope<NavBackStackEntry> ->
    slideOutVertically(
        targetOffsetY = { 500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(200))
}


val bottomNavBarWeightMap = hashMapOf(
    BottomNavItem.Home.route to 1,
    BottomNavItem.Prayers.route to 2,
    BottomNavItem.Quran.route to 3,
    BottomNavItem.Athkar.route to 4,
    BottomNavItem.More.route to 5
)

val TabEnter = { an: AnimatedContentScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) inFromLeftTransition
    else inFromRightTransition
}

val TabExit = { an: AnimatedContentScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) outToRightTransition
    else outToLeftTransition
}

val TabPopEnter = { an: AnimatedContentScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) inFromLeftTransition
    else inFromRightTransition
}

val TabPopExit = { an: AnimatedContentScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) outToRightTransition
    else outToLeftTransition
}

val inFromRight = { _: AnimatedContentScope<NavBackStackEntry> ->
    inFromRightTransition
}

val inFromLeft = { _: AnimatedContentScope<NavBackStackEntry> ->
    inFromLeftTransition
}

val outToRight = { _: AnimatedContentScope<NavBackStackEntry> ->
    outToRightTransition
}

val outToLeft = { _: AnimatedContentScope<NavBackStackEntry> ->
    outToLeftTransition
}

val inFromRightTransition = slideInHorizontally(
    initialOffsetX = { 1000 },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

val outToLeftTransition = slideOutHorizontally(
    targetOffsetX = { -500 },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

val inFromLeftTransition = slideInHorizontally(
    initialOffsetX = { -1000 },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(100))

val outToRightTransition = slideOutHorizontally(
    targetOffsetX = { 500 },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))