package bassamalim.hidaya.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.dataSources.room.daos.SurasDao
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.di.IoDispatcher
import bassamalim.hidaya.core.enums.StartAction
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.core.nav.Navigation
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.receivers.DailyUpdateReceiver
import bassamalim.hidaya.core.receivers.DeviceBootReceiver
import bassamalim.hidaya.core.services.AthanService
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DbUtils
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import com.google.android.gms.location.LocationServices
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class Activity : ComponentActivity() {

    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var appStateRepository: AppStateRepository
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var recitationsRepository: RecitationsRepository
    @Inject lateinit var remembrancesRepository: RemembrancesRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var surasDao: SurasDao
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var alarm: Alarm
    @Inject @IoDispatcher lateinit var dispatcher: CoroutineDispatcher
    private var shouldOnboard = false
    private var startRoute: String? = null
    private lateinit var language: Language
    private lateinit var theme: Flow<Theme>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isFirstLaunch = savedInstanceState == null
        startRoute = intent.getStringExtra("start_route")

        theme = appSettingsRepository.getTheme()

        lifecycleScope.launch {
            if (isFirstLaunch) testDb()

            shouldOnboard = !appStateRepository.isOnboardingCompleted().first()
            language = appSettingsRepository.getLanguage().first()

            bootstrapApp()

            if (isFirstLaunch) {
                handleAction(intent.action)

                if (shouldOnboard) {
                    launchApp()
                    postLaunch()
                }
                else getLocationAndLaunch()
            }
            else launchApp()
        }
    }

    private suspend fun testDb() {
        val shouldReviveDb = DbUtils.shouldReviveDb(
            lastDbVersion = appStateRepository.getLastDbVersion().first(),
            test = surasDao::getPlainNamesAr,
            dispatcher = dispatcher
        )

        if (shouldReviveDb) {
            DbUtils.resetDB(this)
            appStateRepository.setLastDbVersion(Global.DB_VERSION)
            ActivityUtils.restartApplication(this)
        }
        else Log.d(Global.TAG, "Database is up to date")
    }

    private suspend fun bootstrapApp() {
        ActivityUtils.bootstrapApp(
            context = this@Activity,
            applicationContext = applicationContext,
            language = language,
            theme = theme.first()
        )
    }

    private suspend fun handleAction(action: String?) {
        if (action == null || action == "android.intent.action.MAIN") return

        val startAction = StartAction.valueOf(action)
        when (startAction) {
            StartAction.STOP_ATHAN -> {
                // stop athan if it is running
                stopService(Intent(this, AthanService::class.java))
            }
            StartAction.GO_TO_RECITATION -> {
                startRoute = Screen.RecitationPlayer(
                    "back",
                    intent.getStringExtra("media_id")!!
                ).route
            }
            StartAction.RESET_DATABASE -> {
                restoreDbData()
            }
        }
    }

    private suspend fun restoreDbData() {
        DbUtils.restoreDbData(
            suraFavorites = quranRepository.getSuraFavoritesBackup().first(),
            setSuraFavorites = quranRepository::setSuraFavorites,
            reciterFavorites = recitationsRepository.getReciterFavoritesBackup().first(),
            setReciterFavorites = recitationsRepository::setReciterFavorites,
            remembranceFavorites = remembrancesRepository.getFavoritesBackup().first(),
            setRemembranceFavorites = remembrancesRepository::setFavorites,
        )
        Log.d(Global.TAG, "Database data restored")
    }

    private suspend fun getLocationAndLaunch() {
        val location = locationRepository.getLocation().first()
        if (location != null && location.type == LocationType.AUTO) {
            if (granted()) locate()
            else {
                permissionRequestLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
        else {
            launchApp()
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
            locate()

            requestBackgroundLocationPermission()
        }
        else {
            launchApp()
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

    private fun launchApp() {
        setContent {
            val themeState by theme.collectAsState(initial = Theme.DARK, lifecycleScope.coroutineContext)

            ActivityUtils.onActivityCreateSetLocale(
                context = LocalContext.current,
                language = language
            )

            AppTheme(
                theme = themeState,
                direction = getDirection(language)
            ) {
                Navigation(
                    navigator = navigator,
                    thenTo = startRoute,
                    shouldOnboard = shouldOnboard
                )
            }
        }
    }

    private fun getDirection(language: Language): LayoutDirection {
        return if (language == Language.ENGLISH) LayoutDirection.Ltr
        else LayoutDirection.Rtl
    }

    private fun postLaunch() {
        lifecycleScope.launch {
            initFirebase()

            fetchAndActivateRemoteConfig()

            dailyUpdate()

            setAlarms()  // because maybe location changed

            setupBootReceiver()
        }
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

                launchApp()

                postLaunch()
            }

        requestBackgroundLocationPermission()
    }

    private fun storeLocation(location: Location?) {
        if (location != null) {
            lifecycleScope.launch {
                locationRepository.setLocation(location)
            }
        }
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

    private suspend fun setAlarms() {
        val location = locationRepository.getLocation().first()
        if (location != null) {
            val prayerTimes = PrayerTimeUtils.getPrayerTimes(
                settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
                timeZoneId = locationRepository.getTimeZone(location.ids.cityId),
                location = location,
                calendar = Calendar.getInstance()
            )
            alarm.setAll(prayerTimes)
        }
        else if (!appStateRepository.isOnboardingCompleted().first()) {
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