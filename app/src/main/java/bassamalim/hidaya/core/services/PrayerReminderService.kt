package bassamalim.hidaya.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.core.Activity
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.utils.LangUtils.translateTimeNums
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.SortedMap
import javax.inject.Inject

@AndroidEntryPoint
class PrayerReminderService : Service() {

    @Inject @ApplicationScope lateinit var scope: CoroutineScope
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var appSettingsRepository: AppSettingsRepository

    // State management using StateFlow
    private val _serviceState = MutableStateFlow(ServiceState())
    val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

    private var countdownJob: Job? = null
    private var initializationJob: Job? = null

    companion object {
        private const val TAG = "PrayerReminderService"
        private const val NOTIFICATION_ID = 247
        private const val CHANNEL_ID = "PrayerReminderServiceChannel"
        private const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L
        private const val COUNTDOWN_INTERVAL_MS = 1000L

        @Volatile
        var isRunning = false
            private set
    }

    data class ServiceState(
        val prayerName: String = "",
        val timing: String = "",
        val isPassed: Boolean = false,
        val isInitialized: Boolean = false,
        val error: String? = null
    )

    data class PrayerData(
        val times: Map<Prayer, Calendar?>,
        val formattedTimes: Map<Prayer, String>,
        val yesterdayIshaa: Calendar,
        val formattedYesterdayIshaa: String,
        val tomorrowFajr: Calendar,
        val formattedTomorrowFajr: String,
        val previousPrayer: Prayer?,
        val nextPrayer: Prayer?,
        val previousPrayerWasYesterday: Boolean,
        val nextPrayerIsTomorrow: Boolean
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        isRunning = true

        initializationJob = scope.launch {
            try {
                initializeService()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize service", e)
                handleError("Initialization failed: ${e.message}")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun initializeService() {
        try {
            val location = locationRepository.getLocation().firstOrNull()
            if (location == null) {
                handleError("Location not available")
                return
            }

            val prayerData = buildPrayerData(location)
            _serviceState.value = _serviceState.value.copy(isInitialized = true, error = null)

            startCountdown(prayerData)
            Log.d(TAG, "Service initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Service initialization failed", e)
            handleError("Service initialization failed: ${e.message}")
        }
    }

    private suspend fun buildPrayerData(location: Location): PrayerData {
        val language = appSettingsRepository.getLanguage().first()
        val numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first()

        val times = getPrayerTimeMap(location)
        val formattedTimes = getStrPrayerTimeMap(location, language, numeralsLanguage)
        val yesterdayIshaa = getYesterdayIshaa(location)
        val formattedYesterdayIshaa = getStrYesterdayIshaa(location, language, numeralsLanguage)
        val tomorrowFajr = getTomorrowFajr(location)
        val formattedTomorrowFajr = getStrTomorrowFajr(location, language, numeralsLanguage)

        val (previousPrayer, previousPrayerWasYesterday) = getPreviousPrayer(times)
        val (nextPrayer, nextPrayerIsTomorrow) = getNextPrayer(times)

        return PrayerData(
            times = times,
            formattedTimes = formattedTimes,
            yesterdayIshaa = yesterdayIshaa,
            formattedYesterdayIshaa = formattedYesterdayIshaa,
            tomorrowFajr = tomorrowFajr,
            formattedTomorrowFajr = formattedTomorrowFajr,
            previousPrayer = previousPrayer,
            nextPrayer = nextPrayer,
            previousPrayerWasYesterday = previousPrayerWasYesterday,
            nextPrayerIsTomorrow = nextPrayerIsTomorrow
        )
    }

    private fun startCountdown(prayerData: PrayerData) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            try {
                runCountdown(prayerData)
            } catch (e: Exception) {
                Log.e(TAG, "Countdown failed", e)
                handleError("Countdown failed: ${e.message}")
                // Retry after delay
                delay(5000)
                restartService()
            }
        }
    }

    private suspend fun runCountdown(prayerData: PrayerData) {
        val prayerNames = prayersRepository.getPrayerNames()
        val language = appSettingsRepository.getLanguage().first()
        val numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first()

        val nextPrayerTime = if (prayerData.nextPrayerIsTomorrow) {
            prayerData.tomorrowFajr.timeInMillis
        } else {
            prayerData.times[prayerData.nextPrayer]?.timeInMillis ?: return
        }

        var remainingTime = nextPrayerTime - System.currentTimeMillis()

        while (remainingTime > 0) {
            val currentTime = System.currentTimeMillis()
            val previousPrayerTime = if (prayerData.nextPrayer == Prayer.FAJR) {
                prayerData.yesterdayIshaa.timeInMillis
            } else {
                prayerData.times[prayerData.previousPrayer]?.timeInMillis ?: continue
            }

            val timeFromPreviousPrayer = currentTime - previousPrayerTime
            val fifteenMinutesPassed = timeFromPreviousPrayer >= FIFTEEN_MINUTES_MS

            if (fifteenMinutesPassed) {
                updateStateWithElapsedTime(
                    timeFromPreviousPrayer,
                    prayerNames[prayerData.previousPrayer] ?: "",
                    language,
                    numeralsLanguage
                )
            } else {
                updateStateWithRemainingTime(
                    remainingTime,
                    prayerNames[prayerData.nextPrayer] ?: "",
                    language,
                    numeralsLanguage
                )
            }

            delay(COUNTDOWN_INTERVAL_MS)
            remainingTime = nextPrayerTime - System.currentTimeMillis()
        }

        // Time to restart for next prayer cycle
        restartService()
    }

    private fun updateStateWithElapsedTime(
        elapsedMs: Long,
        prayerName: String,
        language: Language,
        numeralsLanguage: Language
    ) {
        val formattedTime = formatDuration(elapsedMs)
        val translatedTime = translateTimeNums(formattedTime, language, numeralsLanguage)

        _serviceState.value = _serviceState.value.copy(
            prayerName = prayerName,
            timing = translatedTime,
            isPassed = true
        )

        updateNotification("$prayerName passed", translatedTime)
    }

    private fun updateStateWithRemainingTime(
        remainingMs: Long,
        prayerName: String,
        language: Language,
        numeralsLanguage: Language
    ) {
        val formattedTime = formatDuration(remainingMs)
        val translatedTime = translateTimeNums(formattedTime, language, numeralsLanguage)

        _serviceState.value = _serviceState.value.copy(
            prayerName = prayerName,
            timing = translatedTime,
            isPassed = false
        )

        updateNotification("Next: $prayerName", "in $translatedTime")
    }

    private fun formatDuration(durationMs: Long): String {
        val hours = (durationMs / (60 * 60 * 1000)) % 24
        val minutes = (durationMs / (60 * 1000)) % 60
        val seconds = (durationMs / 1000) % 60

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private suspend fun restartService() {
        try {
            val location = locationRepository.getLocation().firstOrNull()
            if (location != null) {
                val prayerData = buildPrayerData(location)
                startCountdown(prayerData)
            } else {
                handleError("Location not available for restart")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart service", e)
            handleError("Restart failed: ${e.message}")
        }
    }

    private fun handleError(message: String) {
        Log.e(TAG, message)
        _serviceState.value = _serviceState.value.copy(error = message)
        updateNotification("Prayer Service Error", message)
    }

    private suspend fun getPrayerTimeMap(location: Location): Map<Prayer, Calendar?> {
        return PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )
    }

    private suspend fun getStrPrayerTimeMap(
        location: Location,
        language: Language,
        numeralsLanguage: Language
    ): Map<Prayer, String> {
        return PrayerTimeUtils.formatPrayerTimes(
            prayerTimes = getPrayerTimeMap(location) as SortedMap<Prayer, Calendar?>,
            timeFormat = appSettingsRepository.getTimeFormat().first(),
            language = language,
            numeralsLanguage = numeralsLanguage
        )
    }

    private suspend fun getYesterdayIshaa(location: Location): Calendar {
        val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        return PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = yesterdayCalendar
        )[Prayer.ISHAA] ?: Calendar.getInstance()
    }

    private suspend fun getStrYesterdayIshaa(
        location: Location,
        language: Language,
        numeralsLanguage: Language
    ): String {
        return PrayerTimeUtils.formatPrayerTime(
            time = getYesterdayIshaa(location),
            language = language,
            numeralsLanguage = numeralsLanguage,
            timeFormat = appSettingsRepository.getTimeFormat().first()
        )
    }

    private suspend fun getTomorrowFajr(location: Location): Calendar {
        val tomorrowCalendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
        return PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = tomorrowCalendar
        )[Prayer.FAJR] ?: Calendar.getInstance()
    }

    private suspend fun getStrTomorrowFajr(
        location: Location,
        language: Language,
        numeralsLanguage: Language
    ): String {
        return PrayerTimeUtils.formatPrayerTime(
            time = getTomorrowFajr(location),
            language = language,
            numeralsLanguage = numeralsLanguage,
            timeFormat = appSettingsRepository.getTimeFormat().first()
        )
    }

    private fun getPreviousPrayer(times: Map<Prayer, Calendar?>): Pair<Prayer?, Boolean> {
        val currentMillis = System.currentTimeMillis()
        var previousPrayer: Prayer? = null

        for ((prayer, time) in times.entries.reversed()) {
            if (time != null && time.timeInMillis < currentMillis) {
                previousPrayer = prayer
                break
            }
        }

        val wasYesterday = previousPrayer == null
        if (wasYesterday) {
            previousPrayer = Prayer.ISHAA
        }

        return Pair(previousPrayer, wasYesterday)
    }

    private fun getNextPrayer(times: Map<Prayer, Calendar?>): Pair<Prayer?, Boolean> {
        val currentMillis = System.currentTimeMillis()
        var nextPrayer: Prayer? = null

        for ((prayer, time) in times.entries) {
            if (time != null && time.timeInMillis > currentMillis) {
                nextPrayer = prayer
                break
            }
        }

        val isTomorrow = nextPrayer == null
        if (isTomorrow) {
            nextPrayer = Prayer.FAJR
        }

        return Pair(nextPrayer, isTomorrow)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prayer Reminder Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows prayer countdown and notifications"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }

            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return createNotificationBuilder("Prayer Service", "Initializing...").build()
    }

    private fun updateNotification(title: String, content: String) {
        val notification = createNotificationBuilder(title, content).build()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationBuilder(title: String, content: String): NotificationCompat.Builder {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, Activity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        countdownJob?.cancel()
        initializationJob?.cancel()
        isRunning = false
    }
}