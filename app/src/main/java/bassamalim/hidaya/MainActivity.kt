package bassamalim.hidaya

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.AthanService
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onLaunch()

        handleAction(intent.action)

        val startRoute = intent.getStringExtra("start_route")
        setContent {
            AppTheme {
                Navigator(startRoute)
            }
        }
    }

    private fun onLaunch() {
        DBUtils.testDB(this, PrefUtils.getPreferences(this))

        ActivityUtils.onActivityCreateSetTheme(this)
        ActivityUtils.onActivityCreateSetLocale(this)
    }

    private fun handleAction(action: String?) {
        if (action == null) return

        when (action) {
            Global.STOP_ATHAN -> {
                // stop athan if it is running
                stopService(Intent(this, AthanService::class.java))
            }
        }
    }

}