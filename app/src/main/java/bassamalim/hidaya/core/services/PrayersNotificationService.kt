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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.SortedMap
import javax.inject.Inject

@AndroidEntryPoint
class PrayersNotificationService : Service() {

    @Inject @ApplicationScope lateinit var scope: CoroutineScope
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    private val _serviceState = MutableStateFlow(ServiceState())
    private var countdownJob: Job? = null
    private var initializationJob: Job? = null

    companion object {
        private const val TAG = "PrayerReminderService"
        private const val NOTIFICATION_ID = 247
        private const val CHANNEL_ID = "PrayerReminderServiceChannel"
        private const val THIRTY_MINUTES_MS = 30 * 60 * 1000L
        private const val COUNTDOWN_INTERVAL_MS = 1000L

        @Volatile
        var isRunning = false
            private set
    }

    data class ServiceState(
        val prayerName: String = "",
        val prayerTime: String = "",
        val timing: String = "",
        val isPassed: Boolean = false,
        val isInitialized: Boolean = false,
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
            startCountdown(prayerData)
            Log.d(TAG, "Service initialized successfully")
        } catch (e: Exception) {
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

        val nextPrayerTime =
            if (prayerData.nextPrayerIsTomorrow)
                prayerData.tomorrowFajr.timeInMillis
            else
                prayerData.times[prayerData.nextPrayer]?.timeInMillis ?: return

        var remainingTime = nextPrayerTime - System.currentTimeMillis()

        while (remainingTime > 0) {
            val previousPrayerTime = if (prayerData.previousPrayerWasYesterday)
                prayerData.yesterdayIshaa.timeInMillis
            else prayerData.times[prayerData.previousPrayer]?.timeInMillis ?: continue
            val currentTime = System.currentTimeMillis()

            val timeFromPreviousPrayer = currentTime - previousPrayerTime
            if (timeFromPreviousPrayer < THIRTY_MINUTES_MS) updateStateWithElapsedTime(
                prayerName = prayerNames[prayerData.previousPrayer] ?: "",
                prayerTime = prayerData.formattedTimes[prayerData.previousPrayer] ?: "",
                elapsedMs = timeFromPreviousPrayer,
                language = language,
                numeralsLanguage = numeralsLanguage
            )
            else updateStateWithRemainingTime(
                prayerName = prayerNames[prayerData.nextPrayer] ?: "",
                prayerTime = prayerData.formattedTimes[prayerData.nextPrayer] ?: "",
                remainingMs = remainingTime,
                language = language,
                numeralsLanguage = numeralsLanguage
            )

            delay(COUNTDOWN_INTERVAL_MS)
            remainingTime = nextPrayerTime - System.currentTimeMillis()
        }

        restartService()
    }

    private fun updateStateWithElapsedTime(
        prayerName: String,
        prayerTime: String,
        elapsedMs: Long,
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

        updateNotification(
            title = "$prayerName: $prayerTime",
            content = String.format(resources.getString(R.string.passed), translatedTime)
        )
    }

    private fun updateStateWithRemainingTime(
        prayerName: String,
        prayerTime: String,
        remainingMs: Long,
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

        updateNotification(
            title = "$prayerName: $prayerTime",
            content = String.format(resources.getString(R.string.remaining), translatedTime)
        )
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
            }
            else {
                handleError("Location not available for restart")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart service", e)
            handleError("Restart failed: ${e.message}")
        }
    }

    private fun handleError(message: String?) {
        Log.e(TAG, "Failed to start service: ${message ?: "Unknown error"}")
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.cancel(NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) stopForeground(STOP_FOREGROUND_REMOVE)
        else stopForeground(true)
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
        if (wasYesterday) previousPrayer = Prayer.ISHAA

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
        if (isTomorrow) nextPrayer = Prayer.FAJR

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
        return createNotificationBuilder(
            resources.getString(R.string.prayer_alerts),
            resources.getString(R.string.initializing)
        ).build()
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
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
//            .setContentText(content)
            .setSmallIcon(R.drawable.small_launcher_foreground)
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
//                    .bigText(content)
                    .setBigContentTitle(title)
                    .setSummaryText(content)
            )
//            .setColorized(true)
//            .setCustomContentView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        countdownJob?.cancel()
        initializationJob?.cancel()
        isRunning = false
    }
}