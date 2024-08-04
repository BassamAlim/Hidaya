package bassamalim.hidaya.features.main.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.TabEnter
import bassamalim.hidaya.core.ui.TabExit
import bassamalim.hidaya.core.ui.TabPopEnter
import bassamalim.hidaya.core.ui.TabPopExit
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.features.home.ui.HomeUI
import bassamalim.hidaya.features.more.ui.MoreUI
import bassamalim.hidaya.features.prayers.ui.PrayersUI
import bassamalim.hidaya.features.quran.QuranUI
import bassamalim.hidaya.features.supplicationsCategories.AthkarScreen

@Composable
fun MainUI(
    viewModel: MainViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomNavController = rememberNavController()

    MyScaffold(
        title = stringResource(R.string.app_name),
        topBar = {
            TopBar(
                hijriDate = state.hijriDate,
                gregorianDate = state.gregorianDate,
                onDateClick = viewModel::onDateClick
            )
        },
        bottomBar = { MyBottomNavigation(bottomNavController) }
    ) {
        NavigationGraph(bottomNavController, it)
    }
}

@Composable
private fun TopBar(
    hijriDate: String,
    gregorianDate: String,
    onDateClick: () -> Unit
) {
    TopAppBar(
        backgroundColor = AppTheme.colors.primary,
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App name
                MyText(
                    stringResource(R.string.app_name),
                    textColor = AppTheme.colors.onPrimary
                )

                Column(
                    Modifier
                        .fillMaxHeight()
                        .clickable(onClick = onDateClick),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Hijri date
                        MyText(
                            text = hijriDate,
                            fontSize = 16.nsp,
                            fontWeight = FontWeight.Bold,
                            textColor = AppTheme.colors.onPrimary
                        )

                        // Gregorian date
                        MyText(
                            text = gregorianDate,
                            fontSize = 16.nsp,
                            textColor = AppTheme.colors.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationGraph(
    bottomNavController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        bottomNavController,
        startDestination = BottomNavItem.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(
            route = BottomNavItem.Home.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            HomeUI(
                hiltViewModel()
            )
        }

        composable(
            route = BottomNavItem.Prayers.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            PrayersUI(
                hiltViewModel()
            )
        }

        composable(
            route = BottomNavItem.Quran.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            QuranUI(
                hiltViewModel()
            )
        }

        composable(
            route = BottomNavItem.Athkar.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            AthkarScreen(
                hiltViewModel()
            )
        }

        composable(
            route = BottomNavItem.More.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            MoreUI(
                hiltViewModel()
            )
        }
    }
}