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
import androidx.core.splashscreen.SplashScreen
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
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.di.IoDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.StartAction
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.core.models.Response
import bassamalim.hidaya.core.nav.Navigation
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.receivers.DailyUpdateReceiver
import bassamalim.hidaya.core.receivers.DeviceBootReceiver
import bassamalim.hidaya.core.services.AthanService
import bassamalim.hidaya.core.services.PrayersNotificationService
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.getColorScheme
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DbUtils
import bassamalim.hidaya.core.utils.OsUtils
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

    companion object {
        private const val FIREBASE_FETCH_INTERVAL = 3600 * 6L // 6 hours
        private const val ACTION_DAILY = "daily"
    }

    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var appStateRepository: AppStateRepository
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var recitationsRepository: RecitationsRepository
    @Inject lateinit var remembrancesRepository: RemembrancesRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var userRepository: UserRepository
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
        val splashScreen = setupSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                initializeApp(splashScreen = splashScreen, savedInstanceState = savedInstanceState)
            } catch (e: Exception) {
                Log.e(Globals.TAG, "Error during app initialization", e)
                // Fallback to basic app launch
                launchApp()
            }
        }
    }

    private fun setupSplashScreen() = installSplashScreen().apply {
        setKeepOnScreenCondition { true }
    }

    private suspend fun initializeApp(splashScreen: SplashScreen, savedInstanceState: Bundle?) {
        initializeTheme()
        requestNotificationPermission()
        splashScreen.setKeepOnScreenCondition { false }

        val isLaunch = savedInstanceState == null
        startRoute = intent.getStringExtra("start_route")

        if (isLaunch) testDb()

        initializeUserSettings()
        configureActivity()

        if (isLaunch) handleStartupFlow()
        else launchApp()
    }

    private suspend fun initializeTheme() {
        theme = appSettingsRepository.getTheme()
        initialTheme = theme.first()
        applyTheme(initialTheme)
    }

    private suspend fun initializeUserSettings() {
        shouldOnboard = !appStateRepository.isOnboardingCompleted().first()
        language = appSettingsRepository.getLanguage().first()
    }

    private fun configureActivity() {
        ActivityUtils.configure(
            context = this@Activity,
            applicationContext = applicationContext,
            language = language
        )
    }

    private suspend fun handleStartupFlow() {
        try {
            handleAction(intent.action)
            
            if (shouldOnboard) {
                launchApp()
                postLaunch()
            }
            else {
                getLocationAndLaunch()
            }
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Error in startup flow", e)
            launchApp()
            postLaunch()
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

    private suspend fun testDb() {
        try {
            val shouldReviveDb = DbUtils.shouldReviveDb(
                lastDbVersion = appStateRepository.getLastDbVersion().first(),
                test = surasDao::getPlainNamesAr,
                dispatcher = dispatcher
            )

            if (shouldReviveDb) {
                Log.d(Globals.TAG, "Database needs revival, resetting...")
                DbUtils.resetDB(this)
                appStateRepository.setLastDbVersion(Globals.DB_VERSION)
                ActivityUtils.restartApplication(this)
            }
            else {
                Log.d(Globals.TAG, "Database is up to date")
            }
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Error during database test", e)
        }
    }

    private suspend fun handleAction(action: String?) {
        if (action == null || action == Intent.ACTION_MAIN) return

        try {
            val startAction = StartAction.valueOf(action)
            when (startAction) {
                StartAction.STOP_ATHAN -> {
                    stopService(Intent(this, AthanService::class.java))
                    Log.d(Globals.TAG, "Athan service stopped")
                }
                StartAction.GO_TO_RECITATION -> {
                    val mediaId = intent.getStringExtra("media_id")
                    if (mediaId != null) {
                        startRoute = Screen.RecitationPlayer("back", mediaId).route
                    }
                    else {
                        Log.w(Globals.TAG, "Media ID not found for recitation")
                    }
                }
                StartAction.RESET_DATABASE -> {
                    restoreDbData()
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.e(Globals.TAG, "Unknown start action: $action", e)
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Error handling action: $action", e)
        }
    }

    private suspend fun restoreDbData() {
        try {
            DbUtils.restoreDbData(
                suraFavorites = quranRepository.getSuraFavoritesBackup().first(),
                setSuraFavorites = quranRepository::setSuraFavorites,
                reciterFavorites = recitationsRepository.getReciterFavoritesBackup().first(),
                setReciterFavorites = recitationsRepository::setReciterFavorites,
                remembranceFavorites = remembrancesRepository.getFavoritesBackup().first(),
                setRemembranceFavorites = remembrancesRepository::setFavorites,
            )
            Log.d(Globals.TAG, "Database data restored successfully")
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Failed to restore database data", e)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionRequestLauncher.launch(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            )
        }
    }

    private suspend fun getLocationAndLaunch() {
        val location = locationRepository.getLocation().first()
        if (location != null && location.type == LocationType.AUTO) {
            if (granted()) locate()
            else {
                locationPermissionRequestLauncher.launch(arrayOf(
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

    private val locationPermissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onLocationPermissionRequestResult(result)
    }

    private val notificationPermissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onNotificationPermissionRequestResult(result)
    }

    private fun onLocationPermissionRequestResult(result: Map<String, Boolean>) {
        // Skip if this is a callback for background location or usage stats
        if (result.keys.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ||
            result.keys.contains(Manifest.permission.PACKAGE_USAGE_STATS)) {
            return
        }

        val fineLoc = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLoc = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLoc && coarseLoc) {
            locate()
            requestExtraPermissions()
        }
        else {
            Log.w(Globals.TAG, "Location permissions denied")
            launchApp()
            postLaunch()
        }
    }

    private fun onNotificationPermissionRequestResult(result: Map<String, Boolean>) {
        val notificationGranted = result[Manifest.permission.POST_NOTIFICATIONS] ?: false
        if (notificationGranted) {
            Log.d(Globals.TAG, "Notification permission granted")
        }
        else {
            Log.w(Globals.TAG, "Notification permission denied")
        }
    }

    private fun requestExtraPermissions() {
        val permissions = mutableListOf<String>()

        // Request background location permission for Android Q and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !hasBackgroundLocationPermission()
        ) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.choose_allow_all_the_time),
                Toast.LENGTH_LONG
            ).show()

            locationPermissionRequestLauncher.launch(permissions.toTypedArray())
        }

        requestExactAlarmPermission()
    }

    private fun hasBackgroundLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(Globals.TAG, "Failed to request exact alarm permission", e)
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
            try {
                initializeServices()
                setupSystemComponents()
                startPrayerServiceIfNeeded()
                registerLeaderboardUser()
            } catch (e: Exception) {
                Log.e(Globals.TAG, "Error during post-launch initialization", e)
            }
        }
    }

    private suspend fun initializeServices() {
        initFirebase()
        fetchAndActivateRemoteConfig()
        dailyUpdate()
        setAlarms()
        // testAthan()
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

    private fun setupSystemComponents() {
        setupBootReceiver()
    }

    private suspend fun startPrayerServiceIfNeeded() {
        try {
            val isNotificationEnabled =
                prayersRepository.getContinuousPrayersNotificationEnabled().first()
            if (isNotificationEnabled && !PrayersNotificationService.isRunning) {
                runPrayerReminderService()
            }
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Failed to check prayer notification settings", e)
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
        try {
            LocationServices.getFusedLocationProviderClient(this)
                .lastLocation
                .addOnSuccessListener { location: Location? ->
                    storeLocation(location)
                    launchApp()
                    postLaunch()
                }
                .addOnFailureListener { e ->
                    Log.e(Globals.TAG, "Failed to get location", e)
                    launchApp()
                    postLaunch()
                }
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Error during location request", e)
            launchApp()
            postLaunch()
        }

        requestExtraPermissions()
    }

    private fun storeLocation(location: Location?) {
        location?.let { validLocation ->
            lifecycleScope.launch {
                try {
                    locationRepository.setLocation(validLocation)
                    Log.d(Globals.TAG, "Location stored: lat=${validLocation.latitude}, " +
                            "lng=${validLocation.longitude}")
                } catch (e: Exception) {
                    Log.e(Globals.TAG, "Failed to store location", e)
                }
            }
        } ?: Log.w(Globals.TAG, "Received null location, not storing")
    }

    private fun initFirebase() {
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(FIREBASE_FETCH_INTERVAL)
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            Log.d(Globals.TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Failed to initialize Firebase", e)
        }
    }

    private fun fetchAndActivateRemoteConfig() {
        try {
            FirebaseRemoteConfig.getInstance()
                .fetchAndActivate()
                .addOnSuccessListener { 
                    Log.i(Globals.TAG, "RemoteConfig update successful") 
                }
                .addOnFailureListener { e ->
                    Log.e(Globals.TAG, "RemoteConfig update failed", e)
                }
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Error fetching remote config", e)
        }
    }

    private fun dailyUpdate() {
        try {
            sendBroadcast(
                Intent(this, DailyUpdateReceiver::class.java).apply {
                    action = ACTION_DAILY
                }
            )
            Log.d(Globals.TAG, "Daily update broadcast sent")
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Failed to send daily update broadcast", e)
        }
    }

    private suspend fun setAlarms() {
        try {
            val location = locationRepository.getLocation().first()
            if (location != null) {
                val prayerTimes = PrayerTimeUtils.getPrayerTimes(
                    settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
                    selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
                    location = location,
                    calendar = Calendar.getInstance()
                )
                alarm.setAll(prayerTimes)
                Log.d(Globals.TAG, "Prayer alarms set successfully")
            }
            else {
                Log.w(Globals.TAG, "Cannot set alarms - location is null")
            }
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Failed to set prayer alarms", e)
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
        try {
            val serviceIntent = Intent(this, PrayersNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            }
            else {
                startService(serviceIntent)
            }
            Log.d(Globals.TAG, "Prayer reminder service started")
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Failed to start prayer reminder service", e)
        }
    }

    suspend fun registerLeaderboardUser() {
        val deviceId = OsUtils.getDeviceId(this.application)
        val remoteRecord = userRepository.getRemoteRecord(deviceId)?.first()

        if (remoteRecord is Response.Error && remoteRecord.message == "Device not registered") {
            val newRecord = userRepository.registerDevice(deviceId)
            if (newRecord != null) {
                userRepository.setLocalRecord(newRecord)
            }
        }
    }

}