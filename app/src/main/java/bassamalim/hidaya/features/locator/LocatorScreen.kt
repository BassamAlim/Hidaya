package bassamalim.hidaya.features.locator

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp

@Composable
fun LocatorUI(
    vm: LocatorVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val requestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        vm.onLocationRequestResult(permissions)
    }

    LaunchedEffect(null) {
        vm.provide(requestLauncher)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Disclaimer
        MyText(
            text = stringResource(R.string.disclaimer),
            fontSize = 26.nsp,
            modifier = Modifier.padding(horizontal = 15.dp)
        )

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Locate button
            MySquareButton(
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

            // Choose manually button
            MySquareButton(
                text = stringResource(R.string.choose_manually),
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 30.dp)
            ) {
                vm.onChooseLocationClk()
            }

            // Skip button
            if (st.showSkipLocationBtn) {
                MySquareButton(
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

    if (st.showAllowLocationToast)
        LocationToast()
}

@Composable
private fun LocationToast() {
    val ctx = LocalContext.current
    LaunchedEffect(null) {
        Toast.makeText(
            ctx,
            ctx.getString(R.string.choose_allow_all_the_time),
            Toast.LENGTH_LONG
        ).show()
    }
}