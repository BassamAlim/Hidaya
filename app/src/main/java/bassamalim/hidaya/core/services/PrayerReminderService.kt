package bassamalim.hidaya.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PrayerReminderService : Service() {

    @Inject @ApplicationScope lateinit var scope: CoroutineScope
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    private val prayerNames = prayersRepository.getPrayerNames()
    private var times: Map<Prayer, Calendar?> = emptyMap()
    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language
    private var formattedTimes: Map<Prayer, String> = emptyMap()
    private var yesterdayIshaa: Calendar = Calendar.getInstance()
    private var formattedYesterdayIshaa: String = ""
    private var tomorrowFajr: Calendar = Calendar.getInstance()
    private var formattedTomorrowFajr: String = ""
    private var timer: CountDownTimer? = null
    private var previousPrayer: Prayer? = null
    private var nextPrayer: Prayer? = null
    private var previousPrayerWasYesterday = false
    private var nextPrayerIsTomorrow = false
    private var shouldCount = false
    private val prayerName = mutableStateOf("")
    private val timing = mutableStateOf("")
    private val isPassed = mutableStateOf(false)

    companion object {
        private const val NOTIFICATION_ID = 247
        private const val CHANNEL_ID = "PrayerReminderServiceChannel"
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        isRunning = true

        scope.launch {
            initializeData()
            if (shouldCount) count()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        // Your background work here
        performBackgroundTask()

        return START_STICKY // Restart if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun performBackgroundTask() {
        // Implementation depends on your use case
        // Consider using coroutines for long-running tasks
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for foreground service notifications"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, Activity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Title")
            .setContentText("Content")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Makes notification persistent
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    private suspend fun initializeData() {
        language = appSettingsRepository.getLanguage().first()
        numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first()

        val location = locationRepository.getLocation().first()
        if (location != null) {
            times = getPrayerTimeMap(location)
            formattedTimes = getStrPrayerTimeMap(location)
            yesterdayIshaa = getYesterdayIshaa(location)
            formattedYesterdayIshaa = getStrYesterdayIshaa(location)
            tomorrowFajr = getTomorrowFajr(location)
            formattedTomorrowFajr = getStrTomorrowFajr(location)
        }

        previousPrayer = getPreviousPrayer()
        nextPrayer = getNextPrayer()

        shouldCount = location != null && times.isNotEmpty()
    }

    private fun count() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }

        val till =
            if (nextPrayerIsTomorrow) tomorrowFajr.timeInMillis
            else times[nextPrayer]!!.timeInMillis
        timer = object : CountDownTimer(
            /* millisInFuture = */ till - System.currentTimeMillis(),
            /* countDownInterval = */ 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val previousPrayerTime =
                    if (nextPrayer == Prayer.FAJR) yesterdayIshaa
                    else times[previousPrayer]!!
                val timeFromPreviousPrayer =
                    if (nextPrayer == Prayer.FAJR)
                        System.currentTimeMillis() - previousPrayerTime.timeInMillis
                    else
                        System.currentTimeMillis() - times[previousPrayer]!!.timeInMillis

                val fifteenMinutesPassed = timeFromPreviousPrayer >= 15 * 60 * 1000
                if (fifteenMinutesPassed) {
                    val timeFromPreviousPrayerHours = timeFromPreviousPrayer / (60 * 60 * 1000) % 24
                    val timeFromPreviousPrayerMinutes = timeFromPreviousPrayer / (60 * 1000) % 60
                    val timeFromPreviousPrayerSeconds = timeFromPreviousPrayer / 1000 % 60
                    val timeFromPreviousPrayerHms = String.format(
                        Locale.US,
                        "%02d:%02d:%02d",
                        timeFromPreviousPrayerHours,
                        timeFromPreviousPrayerMinutes,
                        timeFromPreviousPrayerSeconds
                    )

                    prayerName.value = prayerNames[previousPrayer]!!
                    timing.value = translateTimeNums(
                        string = timeFromPreviousPrayerHms,
                        language = language,
                        numeralsLanguage = numeralsLanguage
                    )
                    isPassed.value = true
                }
                else {
                    val timeToNextPrayerHours = millisUntilFinished / (60 * 60 * 1000) % 24
                    val timeToNextPrayerMinutes = millisUntilFinished / (60 * 1000) % 60
                    val timeToNextPrayerSeconds = millisUntilFinished / 1000 % 60
                    val timeToNextPrayerHms = String.format(
                        Locale.US,
                        "%02d:%02d:%02d",
                        timeToNextPrayerHours,
                        timeToNextPrayerMinutes,
                        timeToNextPrayerSeconds
                    )

                    prayerName.value = prayerNames[nextPrayer]!!
                    timing.value = translateTimeNums(
                        string = timeToNextPrayerHms,
                        language = language,
                        numeralsLanguage = numeralsLanguage
                    )
                    isPassed.value = false
                }
            }

            override fun onFinish() {
                recount()
            }
        }.start()
    }

    private fun recount() {
        scope.launch {
            val location = getLocation().first()
            if (location != null) {
                times = getPrayerTimeMap(location)
                formattedTimes = getStrPrayerTimeMap(location)
                yesterdayIshaa = getYesterdayIshaa(location)
                formattedYesterdayIshaa = getStrYesterdayIshaa(location)
                tomorrowFajr = getTomorrowFajr(location)
                formattedTomorrowFajr = getStrTomorrowFajr(location)
            }

            count()
        }
    }

    suspend fun getPrayerTimeMap(location: Location) =
        PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )

    suspend fun getStrPrayerTimeMap(location: Location) =
        PrayerTimeUtils.formatPrayerTimes(
            prayerTimes = getPrayerTimeMap(location),
            timeFormat = appSettingsRepository.getTimeFormat().first(),
            language = appSettingsRepository.getLanguage().first(),
            numeralsLanguage = numeralsLanguage
        )

    suspend fun getYesterdayIshaa(location: Location) =
        PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        )[Prayer.ISHAA]!!

    suspend fun getStrYesterdayIshaa(location: Location) =
        PrayerTimeUtils.formatPrayerTime(
            time = getYesterdayIshaa(location),
            language = appSettingsRepository.getLanguage().first(),
            numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first(),
            timeFormat = appSettingsRepository.getTimeFormat().first()
        )

    suspend fun getTomorrowFajr(location: Location) =
        PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
        )[Prayer.FAJR]!!

    suspend fun getStrTomorrowFajr(location: Location) =
        PrayerTimeUtils.formatPrayerTime(
            time = getTomorrowFajr(location),
            language = appSettingsRepository.getLanguage().first(),
            numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first(),
            timeFormat = appSettingsRepository.getTimeFormat().first()
        )

    private fun getPreviousPrayer(): Prayer? {
        var previousPrayer: Prayer? = null
        val currentMillis = System.currentTimeMillis()
        for (prayer in times.entries.reversed()) {
            val millis = prayer.value!!.timeInMillis
            if (millis < currentMillis) previousPrayer = prayer.key
        }

        previousPrayerWasYesterday = false
        if (previousPrayer == null) {
            previousPrayerWasYesterday = true
            previousPrayer = Prayer.ISHAA
        }

        return previousPrayer
    }

    private fun getNextPrayer(): Prayer? {
        var nextPrayer: Prayer? = null
        val currentMillis = System.currentTimeMillis()
        for (prayer in times.entries) {
            val millis = prayer.value!!.timeInMillis
            if (millis > currentMillis) nextPrayer = prayer.key
        }

        nextPrayerIsTomorrow = false
        if (nextPrayer == null) {
            nextPrayerIsTomorrow = true
            nextPrayer = Prayer.FAJR
        }

        return nextPrayer
    }


    fun getLocation() = locationRepository.getLocation()

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        isRunning = false
    }

}