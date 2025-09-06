package bassamalim.hidaya.core

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.StartAction
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Navigation
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.receivers.DailyUpdateReceiver
import bassamalim.hidaya.core.receivers.DeviceBootReceiver
import bassamalim.hidaya.core.services.AthanService
import bassamalim.hidaya.core.services.PrayersNotificationService
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.getColorScheme
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
    private lateinit var initialTheme: Theme

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
        
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            theme = appSettingsRepository.getTheme()
            initialTheme = theme.first()

            applyTheme(initialTheme)

            splashScreen.setKeepOnScreenCondition { false }

            val isFirstLaunch = savedInstanceState == null
            startRoute = intent.getStringExtra("start_route")
            if (isFirstLaunch) testDb()

            shouldOnboard = !appStateRepository.isOnboardingCompleted().first()
            language = appSettingsRepository.getLanguage().first()

            ActivityUtils.configure(
                context = this@Activity,
                applicationContext = applicationContext,
                language = language
            )

//            testAthan()

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

    private fun applyTheme(theme: Theme) {
        val colorScheme = getColorScheme(theme)
        window.decorView.setBackgroundColor(colorScheme.surface.toArgb())

        enableEdgeToEdge(
            statusBarStyle =
                if (Theme.isDarkTheme(theme))
                    SystemBarStyle.dark(scrim = Color.Transparent.toArgb())
                else SystemBarStyle.light(
                    scrim = Color.Transparent.toArgb(),
                    darkScrim = Color.White.toArgb()
                )
        )
    }

//    private fun testAthan() {
//        println("Test Athan")
//
//        val reminder = Reminder.Prayer.Ishaa
//        val time = System.currentTimeMillis() + 1000 * 60
//
//        val intent = Intent(this, NotificationReceiver::class.java).apply {
//            action = "prayer"
//            putExtra("id", reminder.id)
//            putExtra("time", time)
//        }
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            this, reminder.id, intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
//    }

    private suspend fun testDb() {
        val shouldReviveDb = DbUtils.shouldReviveDb(
            lastDbVersion = appStateRepository.getLastDbVersion().first(),
            test = surasDao::getPlainNamesAr,
            dispatcher = dispatcher
        )

        if (shouldReviveDb) {
            DbUtils.resetDB(this)
            appStateRepository.setLastDbVersion(Globals.DB_VERSION)
            ActivityUtils.restartApplication(this)
        }
        else Log.d(Globals.TAG, "Database is up to date")
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
        Log.d(Globals.TAG, "Database data restored")
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
        if (result.keys.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            || result.keys.contains(Manifest.permission.PACKAGE_USAGE_STATS))
            return

        val fineLoc = result[Manifest.permission.ACCESS_FINE_LOCATION]
        val coarseLoc = result[Manifest.permission.ACCESS_COARSE_LOCATION]
        if (fineLoc != null && fineLoc && coarseLoc != null && coarseLoc) {
            locate()
            requestExtraPermissions()
        }
        else {
            launchApp()
            postLaunch()
        }
    }

    private fun requestExtraPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.choose_allow_all_the_time),
                Toast.LENGTH_LONG
            ).show()

            permissionRequestLauncher.launch(permissions.toTypedArray())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            }
        }
    }

    private fun launchApp() {
        setContent {
            val themeState by theme.collectAsState(
                initial = initialTheme,
                context = lifecycleScope.coroutineContext
            )

            applyTheme(themeState)

            ActivityUtils.onActivityCreateSetLocale(
                context = LocalContext.current,
                language = language
            )

            AppTheme(theme = themeState, direction = getDirection(language)) {
                Navigation(
                    navigator = navigator,
                    thenTo = startRoute,
                    shouldOnboard = shouldOnboard
                )
            }
        }
    }

    private fun getDirection(language: Language) = when (language) {
        Language.ARABIC -> LayoutDirection.Rtl
        Language.ENGLISH -> LayoutDirection.Ltr
    }

    private fun postLaunch() {
        lifecycleScope.launch {
            initFirebase()

            fetchAndActivateRemoteConfig()

            dailyUpdate()

            setAlarms()  // because maybe location changed

            setupBootReceiver()

            if (prayersRepository.getContinuousPrayersNotificationEnabled().first()
                && !PrayersNotificationService.isRunning)
                runPrayerReminderService()
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

        requestExtraPermissions()
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
        FirebaseRemoteConfig.getInstance()
            .fetchAndActivate()
            .addOnSuccessListener { Log.i(Globals.TAG, "RemoteConfig update Success") }
            .addOnFailureListener { Log.e(Globals.TAG, "RemoteConfig update Failed") }
    }

    private fun dailyUpdate() {
        sendBroadcast(
            Intent(this, DailyUpdateReceiver::class.java).apply {
                action = "daily"
            }
        )
    }

    private suspend fun setAlarms() {
        val location = locationRepository.getLocation().first()
        if (location != null) {
            val prayerTimes = PrayerTimeUtils.getPrayerTimes(
                settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
                selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
                location = location,
                calendar = Calendar.getInstance()
            )
            alarm.setAll(prayerTimes)
        }
    }

    private fun setupBootReceiver() {
        packageManager.setComponentEnabledSetting(
            ComponentName(this, DeviceBootReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun runPrayerReminderService() {
        val serviceIntent = Intent(this, PrayersNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent)
        else startService(serviceIntent)
    }

}