package bassamalim.hidaya

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.AthanService
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onLaunch()

        handleAction(intent.action)

        val startRoute = intent.getStringExtra("start_route")
        setContent {
            ActivityUtils.onActivityCreateSetLocale(LocalContext.current)

            AppTheme(
                direction = getDirection()
            ) {
                Navigator(startRoute)
            }
        }
    }

    private fun onLaunch() {
        DBUtils.testDB(this, PrefUtils.getPreferences(this))

        ActivityUtils.onActivityCreateSetTheme(this)
        ActivityUtils.onActivityCreateSetLocale(applicationContext)
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

    private fun getDirection(): LayoutDirection {
        val pref = PrefUtils.getPreferences(this)
        val language = PrefUtils.getLanguage(pref)

        return if (language == Language.ENGLISH) LayoutDirection.Ltr
        else LayoutDirection.Rtl
    }

}