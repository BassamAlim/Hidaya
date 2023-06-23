package bassamalim.hidaya.features.welcome

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.features.settings.AppearanceSettings
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun WelcomeUI(
    vm: WelcomeVM = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val activity = LocalContext.current as Activity

    Box(
        Modifier.background(AppTheme.colors.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = stringResource(R.string.welcome_message),
                fontSize = 26.sp
            )

            AppearanceSettings(activity, vm.pref)

            MySquareButton(
                text = stringResource(R.string.save),
                fontSize = 24.sp,
                innerPadding = PaddingValues(vertical = 2.dp, horizontal = 25.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                vm.save(navigator)
            }
        }
    }
}