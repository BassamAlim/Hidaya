package bassamalim.hidaya.features.main.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp

sealed class BottomNavItem(var route: String, var titleRId: Int, var icon: Int) {
    data object Home: BottomNavItem("home", R.string.title_home, R.drawable.ic_home)
    data object Prayers: BottomNavItem("prayers", R.string.title_prayers, R.drawable.ic_clock)
    data object Quran: BottomNavItem("quran", R.string.title_quran, R.drawable.ic_bar_quran)
    data object Athkar: BottomNavItem("athkar", R.string.title_athkar, R.drawable.ic_duaa)
    data object More: BottomNavItem("more", R.string.title_more, R.drawable.ic_more)
}

@Composable
fun MyBottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Prayers,
        BottomNavItem.Quran,
        BottomNavItem.Athkar,
        BottomNavItem.More
    )

    BottomNavigation(
        backgroundColor = AppTheme.colors.primary,
        contentColor = AppTheme.colors.secondary,
        elevation = 12.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        items.forEach { item ->
            val title = stringResource(item.titleRId)

            BottomNavigationItem(
                label = {
                    MyText(
                        title,
                        fontSize = 9.nsp,
                        textColor = AppTheme.colors.secondary,
                        softWrap = false
                    )
                },
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                selectedContentColor = AppTheme.colors.secondary,
                unselectedContentColor = AppTheme.colors.onPrimary,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { screenRoute ->
                            popUpTo(screenRoute) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}