package bassamalim.hidaya.features.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.features.NavGraphs
import bassamalim.hidaya.features.appCurrentDestinationAsState
import bassamalim.hidaya.features.destinations.AthkarUIDestination
import bassamalim.hidaya.features.destinations.HomeUIDestination
import bassamalim.hidaya.features.destinations.MoreUIDestination
import bassamalim.hidaya.features.destinations.PrayersUIDestination
import bassamalim.hidaya.features.destinations.QuranUIDestination
import bassamalim.hidaya.features.startAppDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class BottomNavDestination(
    val direction: DirectionDestinationSpec,
    val icon: Int,
    @StringRes val label: Int
) {
    Home(HomeUIDestination, R.drawable.ic_home, R.string.title_home),
    Prayers(PrayersUIDestination, R.drawable.ic_clock, R.string.title_prayers),
    Quran(QuranUIDestination, R.drawable.ic_bar_quran, R.string.title_quran),
    Athkar(AthkarUIDestination, R.drawable.ic_duaa, R.string.title_athkar),
    More(MoreUIDestination, R.drawable.ic_more, R.string.title_more)
}

@Composable
fun BottomBar(navController: NavController) {
    val currentDestination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination

    BottomNavigation(
        backgroundColor = AppTheme.colors.primary,
        contentColor = Color.Green,
        elevation = 12.dp
    ) {
        BottomNavDestination.values().forEach { destination ->
            BottomNavigationItem(
                selected = currentDestination == destination.direction,
                onClick = {
                    navController.navigate(destination.direction.route) {
                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = {
                    MyText(
                        stringResource(destination.label),
                        fontSize = 12.nsp,
                        textColor = AppTheme.colors.accent,
                        softWrap = false
                    )
                },
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        painter = painterResource(destination.icon),
                        contentDescription = stringResource(destination.label),
                        modifier = Modifier.size(24.dp)
                    )
                },
                selectedContentColor = AppTheme.colors.accent,
                unselectedContentColor = AppTheme.colors.onPrimary
            )
        }
    }
}