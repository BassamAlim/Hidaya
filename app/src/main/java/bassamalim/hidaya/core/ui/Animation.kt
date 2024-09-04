package bassamalim.hidaya.core.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry
import bassamalim.hidaya.features.main.ui.BottomNavItem

val inFromBottom = { _: AnimatedContentTransitionScope<NavBackStackEntry> ->
    slideInVertically(
        initialOffsetY = { 500 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(200))
}

val outToBottom = { _: AnimatedContentTransitionScope<NavBackStackEntry> ->
    slideOutVertically(
        targetOffsetY = { -500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(200))
}

val inFromTop = { _: AnimatedContentTransitionScope<NavBackStackEntry> ->
    slideInVertically(
        initialOffsetY = { -500 },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(200))
}

val outToTop = { _: AnimatedContentTransitionScope<NavBackStackEntry> ->
    slideOutVertically(
        targetOffsetY = { 500 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(200))
}


val bottomNavBarWeightMap = hashMapOf(
    BottomNavItem.Home.route to 1,
    BottomNavItem.PrayersBoard.route to 2,
    BottomNavItem.QuranMenu.route to 3,
    BottomNavItem.Remembrances.route to 4,
    BottomNavItem.More.route to 5
)

val TabEnter = { an: AnimatedContentTransitionScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) inFromLeftTransition
    else inFromRightTransition
}

val TabExit = { an: AnimatedContentTransitionScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) outToRightTransition
    else outToLeftTransition
}

val TabPopEnter = { an: AnimatedContentTransitionScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) inFromLeftTransition
    else inFromRightTransition
}

val TabPopExit = { an: AnimatedContentTransitionScope<NavBackStackEntry> ->
    val from = an.initialState.destination.route
    val to = an.targetState.destination.route
    val fromWeight = bottomNavBarWeightMap[from] ?: 0
    val toWeight = bottomNavBarWeightMap[to] ?: 0

    if (fromWeight < toWeight) outToRightTransition
    else outToLeftTransition
}

val inFromRight = { _: AnimatedContentTransitionScope<NavBackStackEntry> ->
    inFromRightTransition
}

val inFromLeft = { _: AnimatedContentTransitionScope<NavBackStackEntry> ->
    inFromLeftTransition
}

val outToRight = { _: AnimatedContentTransitionScope<NavBackStackEntry> ->
    outToRightTransition
}

val outToLeft = { _: AnimatedContentTransitionScope<NavBackStackEntry> ->
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