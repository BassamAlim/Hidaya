package bassamalim.hidaya.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.nav.Navigation
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.receivers.DailyUpdateReceiver
import bassamalim.hidaya.core.receivers.DeviceBootReceiver
import bassamalim.hidaya.core.services.AthanService
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.utils.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Activity : ComponentActivity() {

    @Inject lateinit var sp: SharedPreferences
    @Inject lateinit var db: AppDatabase
    @Inject lateinit var navigator: Navigator
    private var shouldWelcome = false
    private var startRoute: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shouldWelcome = PrefUtils.getBoolean(sp, Prefs.FirstTime)
        startRoute = intent.getStringExtra("start_route")

        val isFirstLaunch = savedInstanceState == null

        if (isFirstLaunch) {
            handleAction(intent.action)

            try {  // remove after a while
                ActivityUtils.onActivityCreateSetLocale(this)
                LocationType.valueOf(PrefUtils.getString(sp, Prefs.LocationType))
            } catch (e: Exception) {
                Log.e(Global.TAG, "Neuralyzing", e)
                ActivityUtils.clearAppData(this)
            }
        }

        preLaunch(isFirstLaunch)
        if (isFirstLaunch) {
            if (shouldWelcome) {
                launch()
                postLaunch()
            }
            else getLocationAndLaunch()
        }
        else launch()
    }

    private fun handleAction(action: String?) {
        if (action == null) return

        when (action) {
            Global.STOP_ATHAN -> {
                // stop athan if it is running
                stopService(Intent(this, AthanService::class.java))
            }
            Global.GO_TO_TELAWA -> {
                startRoute = Screen.TelawatClient(
                    "back",
                    intent.getStringExtra("media_id")!!
                ).route
            }
        }
    }

    private fun preLaunch(firstLaunch: Boolean = false) {
        if (firstLaunch && DBUtils.needsRevival(sp, db))
            DBUtils.reviveDB(this, sp)

        ActivityUtils.onActivityCreateSetLocale(this)
        ActivityUtils.onActivityCreateSetTheme(this)
        ActivityUtils.onActivityCreateSetLocale(applicationContext)
        ActivityUtils.onActivityCreateSetTheme(applicationContext)
    }

    private fun getLocationAndLaunch() {
        val locType = LocationType.valueOf(
            PrefUtils.getString(sp, Prefs.LocationType)
        )
        if (locType == LocationType.Auto) {
            if (granted()) locate()
            else {
                permissionRequestLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
        else {
            launch()
            postLaunch()
        }
    }

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onPermissionRequestResult(result)
    }

    private fun onPermissionRequestResult(result: Map<String, Boolean>) {
        if (result.keys.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) return

        val fineLoc = result[Manifest.permission.ACCESS_FINE_LOCATION]
        val coarseLoc = result[Manifest.permission.ACCESS_COARSE_LOCATION]
        if (fineLoc != null && fineLoc && coarseLoc != null && coarseLoc) {
            sp.edit()
                .putString(Prefs.LocationType.key, LocationType.Auto.name)
                .apply()

            locate()

            requestBackgroundLocationPermission()
        }
        else {
            launch()
            postLaunch()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                getString(R.string.choose_allow_all_the_time),
                Toast.LENGTH_LONG
            ).show()

            permissionRequestLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
        }
    }

    private fun launch() {
        setContent {
            ActivityUtils.onActivityCreateSetLocale(LocalContext.current)

            AppTheme(
                direction = getDirection()
            ) {
                Navigation(
                    navigator = navigator,
                    thenTo = startRoute,
                    shouldWelcome = shouldWelcome
                )
            }
        }
    }

    private fun getDirection(): LayoutDirection {
        val language = PrefUtils.getLanguage(sp)

        return if (language == Language.ENGLISH) LayoutDirection.Ltr
        else LayoutDirection.Rtl
    }

    private fun postLaunch() {
        initFirebase()

        fetchAndActivateRemoteConfig()

        dailyUpdate()

        setAlarms()  // because maybe location changed

        setupBootReceiver()
    }

    private fun granted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener { location: Location? ->
                storeLocation(location)

                launch()

                postLaunch()
            }

        requestBackgroundLocationPermission()
    }

    private fun storeLocation(location: Location?) {
        if (location == null) return

        val closestCity = db.cityDao().getClosest(location.latitude, location.longitude)

        sp.edit()
            .putInt(Prefs.CountryID.key, closestCity.countryId)
            .putInt(Prefs.CityID.key, closestCity.id)
            .apply()

        LocUtils.storeLocation(sp, location)
    }

    private fun initFirebase() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            // update at most every six hours
            .setMinimumFetchIntervalInSeconds(3600 * 6).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun fetchAndActivateRemoteConfig() {
        FirebaseRemoteConfig.getInstance().fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) Log.i(Global.TAG, "RemoteConfig update Success")
                else Log.e(Global.TAG, "RemoteConfig update Failed")
            }
    }

    private fun dailyUpdate() {
        val intent = Intent(this, DailyUpdateReceiver::class.java)
        intent.action = "daily"
        sendBroadcast(intent)
    }

    private fun setAlarms() {
        val location = LocUtils.retrieveLocation(sp)
        if (location != null) {
            val times = PTUtils.getTimes(sp, db)!!
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

    private fun setupBootReceiver() {
        packageManager.setComponentEnabledSetting(
            ComponentName(this, DeviceBootReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

}