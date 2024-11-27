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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.features.home.ui.HomeScreen
import bassamalim.hidaya.features.more.ui.MoreScreen
import bassamalim.hidaya.features.prayers.board.ui.PrayersBoardScreen
import bassamalim.hidaya.features.quran.surasMenu.ui.QuranSurasScreen
import bassamalim.hidaya.features.remembrances.categoriesMenu.ui.RemembranceCategoriesScreen

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomNavController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }

    MyScaffold(
        title = stringResource(R.string.app_name),
        topBar = {
            TopBar(
                hijriDate = state.hijriDate,
                gregorianDate = state.gregorianDate,
                onDateClick = viewModel::onDateClick
            )
        },
        bottomBar = { MyBottomNavigation(bottomNavController) },
        snackBarHost = { SnackbarHost(snackBarHostState) }
    ) { padding ->
        NavigationGraph(
            bottomNavController = bottomNavController,
            snackBarHostState = snackBarHostState,
            padding = padding
        )
    }
}

@Composable
private fun TopBar(hijriDate: String, gregorianDate: String, onDateClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Box(
            Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App name
                MyText(stringResource(R.string.app_name))

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable(onClick = onDateClick),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Hijri date
                        MyText(
                            text = hijriDate,
                            fontSize = 16.nsp,
                            fontWeight = FontWeight.Bold
                        )

                        // Gregorian date
                        MyText(
                            text = gregorianDate,
                            fontSize = 16.nsp
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
    snackBarHostState: SnackbarHostState,
    padding: PaddingValues
) {
    NavHost(
        navController = bottomNavController,
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
            route = BottomNavItem.QuranSuras.route,
            enterTransition = TabEnter,
            exitTransition = TabExit,
            popEnterTransition = TabPopEnter,
            popExitTransition = TabPopExit
        ) {
            QuranSurasScreen(
                hiltViewModel()
            )
        }

        composable(
            route = BottomNavItem.RemembranceCategories.route,
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
                viewModel = hiltViewModel(),
                snackBarHostState = snackBarHostState
            )
        }
    }
}