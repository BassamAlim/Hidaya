package bassamalim.hidaya.features.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.*
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.features.NavGraphs
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.rememberNavHostEngine

@Composable
fun MainUI(
    vm: MainVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val engine = rememberNavHostEngine()
    val navController = engine.rememberNavController()

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
                                    text = st.hijriDate,
                                    fontSize = 16.nsp,
                                    fontWeight = FontWeight.Bold,
                                    textColor = AppTheme.colors.onPrimary
                                )

                                MyText(
                                    text = st.gregorianDate,
                                    fontSize = 16.nsp,
                                    textColor = AppTheme.colors.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = { BottomBar(navController) }
    ) {
        NavigationGraph(
            navController = navController,
            padding = it
        )

        DateEditorDialog(
            shown = st.dateEditorShown,
            offsetText = st.dateEditorOffsetText,
            dateText = st.dateEditorDateText,
            onNextDay = { vm.onDateEditorNextDay() },
            onPreviousDay = { vm.onDateEditorPrevDay() },
            onCancel = { vm.onDateEditorCancel() },
            onSubmit = { vm.onDateEditorSubmit() }
        )
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    padding: PaddingValues
) {
    DestinationsNavHost(
        navGraph = NavGraphs.root,
        navController = navController,
        modifier = Modifier.padding(padding)
    )
}