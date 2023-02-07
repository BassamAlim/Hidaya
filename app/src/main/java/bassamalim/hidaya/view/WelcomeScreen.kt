package bassamalim.hidaya.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import bassamalim.hidaya.viewmodel.WelcomeVM

@Composable
fun WelcomeUI(
    navController: NavController = rememberNavController(),
    viewModel: WelcomeVM = hiltViewModel()
) {
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

//            Settings.AppearanceSettings(this@WelcomeActivity, pref)

            MyButton(
                text = stringResource(R.string.save),
                fontSize = 24.sp,
                innerPadding = PaddingValues(vertical = 2.dp, horizontal = 25.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                viewModel.save(navController)
            }
        }
    }
}