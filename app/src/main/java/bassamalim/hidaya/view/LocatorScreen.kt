package bassamalim.hidaya.view

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.viewmodel.LocatorVM

@Composable
fun LocatorUI(
    navController: NavController = rememberNavController(),
    viewModel: LocatorVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val requestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.onLocationRequestResult(permissions)
    }

    LaunchedEffect(null) {
        viewModel.provide(navController, requestLauncher)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyText(
            text = stringResource(R.string.disclaimer),
            fontSize = 26.nsp,
            modifier = Modifier.padding(horizontal = 15.dp)
        )

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyButton(
                text = stringResource(R.string.locate),
                fontSize = 22.sp,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.accent),
                textColor = AppTheme.colors.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 30.dp)
            ) {
                viewModel.onLocateClick()
            }

            MyButton(
                text = stringResource(R.string.choose_manually),
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 30.dp)
            ) {
                viewModel.onChooseLocationClick()
            }

            if (state.showSkipLocationBtn) {
                MyButton(
                    text = stringResource(R.string.rejected),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 30.dp)
                ) {
                    viewModel.onSkipLocationClick()
                }
            }
        }
    }

    LaunchedEffect(key1 = state.showAllowLocationToast) {
        Toast.makeText(
            context,
            context.getString(R.string.choose_allow_all_the_time),
            Toast.LENGTH_LONG
        ).show()
    }
}