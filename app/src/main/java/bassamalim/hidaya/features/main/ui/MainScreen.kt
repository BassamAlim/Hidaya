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
import bassamalim.hidaya.features.home.ui.HomeScreen
import bassamalim.hidaya.features.more.ui.MoreScreen
import bassamalim.hidaya.features.prayers.prayersBoard.ui.PrayersBoardScreen
import bassamalim.hidaya.features.quran.quranMenu.ui.QuranMenuScreen
import bassamalim.hidaya.features.remembrances.remembranceCategories.ui.RemembranceCategoriesScreen

@Composable
fun MainScreen(
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
private fun NavigationGraph(
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
            HomeScreen(
                hiltViewModel()
            )
        }

        composable(
            route = BottomNavItem.PrayersBoard.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            PrayersBoardScreen(
                hiltViewModel()
            )
        }

        composable(
            route = BottomNavItem.QuranMenu.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            QuranMenuScreen(
                hiltViewModel()
            )
        }

        composable(
            route = BottomNavItem.Remembrances.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            RemembranceCategoriesScreen(
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
            MoreScreen(
                hiltViewModel()
            )
        }
    }
}