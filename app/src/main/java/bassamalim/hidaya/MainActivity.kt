package bassamalim.hidaya

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.receivers.DailyUpdateReceiver
import bassamalim.hidaya.receivers.DeviceBootReceiver
import bassamalim.hidaya.services.AthanService
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sp = PrefUtils.getPreferences(this)

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
        DBUtils.testDB(this, sp)

        ActivityUtils.onActivityCreateSetTheme(this)
        ActivityUtils.onActivityCreateSetLocale(this)
        ActivityUtils.onActivityCreateSetLocale(applicationContext)

        initFirebase()

        dailyUpdate()

        setAlarms()

        setupBootReceiver()
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
        val language = PrefUtils.getLanguage(sp)

        return if (language == Language.ENGLISH) LayoutDirection.Ltr
        else LayoutDirection.Rtl
    }

    private fun initFirebase() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            // update at most every six hours
            .setMinimumFetchIntervalInSeconds(3600 * 6).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun setAlarms() {
        val location = LocUtils.retrieveLocation(sp)
        if (location != null) {
            val times = PTUtils.getTimes(
                sp,
                DBUtils.getDB(this)
            )!!
            Alarms(this, times)
        }
        else if (!sp.getBoolean(Prefs.FirstTime.key, true)) {
            Toast.makeText(
                this,
                getString(R.string.give_location_permission_toast),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun dailyUpdate() {
        val intent = Intent(this, DailyUpdateReceiver::class.java)
        intent.action = "daily"
        sendBroadcast(intent)
    }

    private fun setupBootReceiver() {
        packageManager.setComponentEnabledSetting(
            ComponentName(this, DeviceBootReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

}