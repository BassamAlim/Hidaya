package bassamalim.hidaya.ui.components

import android.util.Log
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.ui.theme.AppTheme

sealed class BottomNavItem(var icon: Int, var screen_route: String){
    object Home: BottomNavItem(R.drawable.ic_home,"home")
    object Prayers: BottomNavItem(R.drawable.ic_clock,"prayers")
    object Quran: BottomNavItem(R.drawable.ic_bar_quran,"quran")
    object Athkar: BottomNavItem(R.drawable.ic_duaa,"athkar")
    object More: BottomNavItem(R.drawable.ic_more,"more")
}

@Composable
fun MyBottomNavigation(navController: NavController) {
    Log.d(Global.TAG, "5")

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Prayers,
        BottomNavItem.Quran,
        BottomNavItem.Athkar,
        BottomNavItem.More
    )
    val titles = listOf(
        stringResource(id = R.string.title_home),
        stringResource(id = R.string.title_prayers),
        stringResource(id = R.string.title_quran),
        stringResource(id = R.string.title_athkar),
        stringResource(id = R.string.title_more),
    )

    BottomNavigation(
        backgroundColor = AppTheme.colors.primary,
        contentColor = AppTheme.colors.accent,
        elevation = 12.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, item ->
            BottomNavigationItem(
                label = { MyText(text = titles[index], fontSize = 10.sp) },
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = titles[index],
                        modifier = Modifier.size(24.dp)
                    )
                },
                selectedContentColor = AppTheme.colors.accent,
                unselectedContentColor = AppTheme.colors.onPrimary,
                selected = currentRoute == item.screen_route,
                onClick = {
                    navController.navigate(item.screen_route) {
                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
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