package bassamalim.hidaya.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bassamalim.hidaya.R
import bassamalim.hidaya.dialogs.DateEditorDialog
import bassamalim.hidaya.screens.*
import bassamalim.hidaya.ui.components.BottomNavItem
import bassamalim.hidaya.ui.components.MyBottomNavigation
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.viewmodel.MainVM

@Composable
fun MainUI(
    navController: NavHostController = rememberNavController(),
    viewModel: MainVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val bottomNavController = rememberNavController()

    MyScaffold(
        title = stringResource(R.string.app_name),
        topBar = {
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
                        MyText(
                            stringResource(R.string.app_name),
                            textColor = AppTheme.colors.onPrimary
                        )

                        Column(
                            Modifier
                                .fillMaxHeight()
                                .clickable { state.dateEditorShown = true },
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 10.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                val content = getTodayScreenContent(dateOffset)

                                MyText(
                                    text = content[0],
                                    fontSize = 16.nsp,
                                    fontWeight = FontWeight.Bold,
                                    textColor = AppTheme.colors.onPrimary
                                )

                                MyText(
                                    text = content[1],
                                    fontSize = 16.nsp,
                                    textColor = AppTheme.colors.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = { MyBottomNavigation(bottomNavController) }
    ) {
        NavigationGraph(navController, it)

        DateEditorDialog(this, pref, dateOffset, dateEditorShown).Dialog()
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, padding: PaddingValues) {
    NavHost(
        navController,
        startDestination = BottomNavItem.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(
            route = BottomNavItem.Home.route,
            arguments = listOf(
                navArgument("is_located") { type = NavType.BoolType },
                navArgument("coordinates") { type = NavType.FloatArrayType}
            )
        ) {
            HomeUI(navController)
        }
        composable(
            route = BottomNavItem.Prayers.route,
            arguments = listOf(
                navArgument("is_located") { type = NavType.BoolType },
                navArgument("coordinates") { type = NavType.FloatArrayType}
            )
        ) {
            PrayersUI(navController)
        }
        composable(BottomNavItem.Quran.route) {
            QuranUI(navController)
        }
        composable(BottomNavItem.Athkar.route) {
            AthkarUI(navController)
        }
        composable(BottomNavItem.More.route) {
            MoreUI(navController)
        }
    }
}