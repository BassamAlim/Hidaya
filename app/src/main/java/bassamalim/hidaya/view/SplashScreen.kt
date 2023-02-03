package bassamalim.hidaya.view

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.viewmodel.SplashVM

@Composable
fun SplashUI(
    navController: NavController = rememberNavController(),
    viewModel: SplashVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity
    val requestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.onLocationRequestResult(permissions)
    }

    println("HERE")

    LaunchedEffect(null) {
        viewModel.provide(navController, requestLauncher)

        val splashScreen = activity.installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
    }

    LaunchedEffect(key1 = state.showAllowLocationToastShown) {
        Toast.makeText(
            context,
            context.getString(R.string.choose_allow_all_the_time),
            Toast.LENGTH_LONG
        ).show()
    }

    LaunchedEffect(null) {
        viewModel.enter()
    }
}