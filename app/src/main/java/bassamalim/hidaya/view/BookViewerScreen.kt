package bassamalim.hidaya.view

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.viewmodel.BookViewerVM
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun BookViewerUI(
    navController: NavController = rememberNavController(),
    viewModel: BookViewerVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
}