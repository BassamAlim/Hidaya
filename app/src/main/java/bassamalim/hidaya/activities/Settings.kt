package bassamalim.hidaya.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import bassamalim.hidaya.R
import bassamalim.hidaya.screens.SettingsScreen
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils

class Settings : ComponentActivity() {

    private lateinit var settingsScreen: SettingsScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetLocale(this)

        settingsScreen = SettingsScreen(this)

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    @Composable
    private fun UI() {
        MyScaffold(stringResource(R.string.settings)) {
            settingsScreen.SettingsUI()
        }
    }

}