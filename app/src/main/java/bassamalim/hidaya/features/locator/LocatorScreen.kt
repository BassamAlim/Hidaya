package bassamalim.hidaya.features.locator

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LocatorUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: LocatorVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val requestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        vm.onLocationRequestResult(permissions)
    }

    LaunchedEffect(null) {
        vm.provide(nc, requestLauncher)
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
                vm.onLocateClk()
            }

            MyButton(
                text = stringResource(R.string.choose_manually),
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 30.dp)
            ) {
                vm.onChooseLocationClk()
            }

            if (st.showSkipLocationBtn) {
                MyButton(
                    text = stringResource(R.string.rejected),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 30.dp)
                ) {
                    vm.onSkipLocationClk()
                }
            }
        }
    }

    if (st.showAllowLocationToast) {
        LaunchedEffect(null) {
            Toast.makeText(
                ctx,
                ctx.getString(R.string.choose_allow_all_the_time),
                Toast.LENGTH_LONG
            ).show()
        }
    }

}