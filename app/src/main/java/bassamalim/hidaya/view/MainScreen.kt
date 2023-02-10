package bassamalim.hidaya.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.viewmodel.MainVM

@Composable
fun MainUI(
    nc: NavHostController = rememberNavController(),
    vm: MainVM = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
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
                                .clickable { vm.showDateEditor() },
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 10.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                MyText(
                                    text = state.hijriDate,
                                    fontSize = 16.nsp,
                                    fontWeight = FontWeight.Bold,
                                    textColor = AppTheme.colors.onPrimary
                                )

                                MyText(
                                    text = state.gregorianDate,
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
        NavigationGraph(nc, bottomNavController, it)

        DateEditorDialog(
            shown = state.dateEditorShown,
            offsetText = state.dateEditorOffsetText,
            dateText = state.dateEditorDateText,
            onNextDay = { vm.onDateEditorNextDay() },
            onPreviousDay = { vm.onDateEditorPrevDay() },
            onCancel = { vm.onDateEditorCancel() },
            onSubmit = { vm.onDateEditorSubmit() }
        )

        if (state.shouldShowLocationPermissionToast) {
            LaunchedEffect(null) {
                Toast.makeText(
                    context,
                    context.getString(R.string.give_location_permission_toast),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    bottomNavController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        bottomNavController,
        startDestination = BottomNavItem.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(BottomNavItem.Home.route) {
            HomeUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        composable(BottomNavItem.Prayers.route) {
            PrayersUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }
        composable(BottomNavItem.Quran.route) {
            QuranUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }
        composable(BottomNavItem.Athkar.route) {
            AthkarUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        composable(BottomNavItem.More.route) {
            MoreUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }
    }
}