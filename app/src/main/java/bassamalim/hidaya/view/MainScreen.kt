package bassamalim.hidaya.view

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.viewmodel.HomeVM
import bassamalim.hidaya.viewmodel.MainVM
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@RootNavGraph(start = true)
@Composable
fun MainUI(
    navController: NavController = rememberNavController(),
    viewModel: MainVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
}