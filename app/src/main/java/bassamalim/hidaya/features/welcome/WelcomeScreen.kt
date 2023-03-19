package bassamalim.hidaya.features.welcome

import android.app.Activity
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.features.settings.AppearanceSettings
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WelcomeUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: WelcomeVM = hiltViewModel()
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

            MyButton(
                text = stringResource(R.string.save),
                fontSize = 24.sp,
                innerPadding = PaddingValues(vertical = 2.dp, horizontal = 25.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                vm.save(nc)
            }
        }
    }
}