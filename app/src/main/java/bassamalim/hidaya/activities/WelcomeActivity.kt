package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.screens.SettingsScreen
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils

class WelcomeActivity: AppCompatActivity() {

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
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    text = stringResource(id = R.string.welcome_message),
                    fontSize = 26.sp
                )

                Box(
                    Modifier.weight(1F)
                ) {
                    settingsScreen.SettingsUI()
                }

                MyButton(
                    text = stringResource(id = R.string.save),
                    fontSize = 24.sp,
                    innerPadding = PaddingValues(vertical = 2.dp, horizontal = 25.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    val intent = Intent(
                        this@WelcomeActivity, LocationActivity::class.java
                    )
                    intent.action = "initial"
                    startActivity(intent)

                    PreferenceManager
                        .getDefaultSharedPreferences(this@WelcomeActivity)
                        .edit()
                        .putBoolean("new_user", false)
                        .apply()
                }
            }
        }
    }

}