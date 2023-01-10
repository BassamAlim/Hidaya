package bassamalim.hidaya.view

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.viewmodel.QuranViewerVM
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun QuranViewerUI(
    navController: NavController = rememberNavController(),
    viewModel: QuranViewerVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
}