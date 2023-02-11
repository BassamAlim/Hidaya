package bassamalim.hidaya.view

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.viewmodel.SplashVM

@Composable
fun SplashUI(
    nc: NavController = rememberNavController(),
    vm: SplashVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsState()
    val context = LocalContext.current
    val requestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        vm.onLocationRequestResult(permissions)
    }

    DisposableEffect(key1 = vm) {
//        val splashScreen = activity.installSplashScreen()
//        splashScreen.setKeepOnScreenCondition { false }

        vm.onStart(nc, requestLauncher)
        onDispose {}
    }

    if (st.allowBGLocShown) {
        LaunchedEffect(null) {
            Toast.makeText(
                context,
                context.getString(R.string.choose_allow_all_the_time),
                Toast.LENGTH_LONG
            ).show()
        }
    }

}