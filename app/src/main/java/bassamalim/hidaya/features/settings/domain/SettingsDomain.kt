package bassamalim.hidaya.features.settings.domain

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.core.models.TimeOfDay
import bassamalim.hidaya.core.services.PrayersNotificationService
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class SettingsDomain @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val prayersRepository: PrayersRepository,
    private val notificationsRepository: NotificationsRepository,
    private val locationRepository: LocationRepository,
    private val alarm: Alarm
) {

    suspend fun resetPrayerTimes() {
        val location = locationRepository.getLocation().first() ?: return

        val prayerTimes = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )

        alarm.setAll(prayerTimes)
    }

    fun getLanguage() = appSettingsRepository.getLanguage()

    fun setLanguage(language: Language) {
        appSettingsRepository.setLanguage(language)
    }

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

    fun setNumeralsLanguage(numeralsLanguage: Language) {
        appSettingsRepository.setNumeralsLanguage(numeralsLanguage)
    }

    fun getTimeFormat() = appSettingsRepository.getTimeFormat()

    fun setTimeFormat(timeFormat: TimeFormat) {
        appSettingsRepository.setTimeFormat(timeFormat)
    }

    fun getTheme() = appSettingsRepository.getTheme()

    fun setTheme(theme: Theme) {
        appSettingsRepository.setTheme(theme)
    }

    fun getDevotionReminderEnabledMap() =
        notificationsRepository.getDevotionalReminderEnabledMap()

    fun getDevotionReminderTimeOfDayMap() =
        notificationsRepository.getDevotionalReminderTimes()

    fun getContinuousPrayersNotificationEnabled() =
        prayersRepository.getContinuousPrayersNotificationEnabled()

    fun enableContinuousPrayersNotification(context: Context) {
        prayersRepository.setContinuousPrayersNotificationEnabled(true)

        val serviceIntent = Intent(context, PrayersNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        }
        else {
            context.startService(serviceIntent)
        }
    }

    fun disableContinuousPrayersNotification(context: Context) {
        prayersRepository.setContinuousPrayersNotificationEnabled(false)

        context.stopService(
            Intent(
                context,
                PrayersNotificationService::class.java
            )
        )
    }

    fun getPrayerTimesCalculatorSettings() = prayersRepository.getPrayerTimesCalculatorSettings()

    fun setPrayerTimeCalculationMethod(calculationMethod: PrayerTimeCalculationMethod) {
        prayersRepository.setCalculationMethod(calculationMethod)
    }

    fun setPrayerTimeJuristicMethod(juristicMethod: PrayerTimeJuristicMethod) {
        prayersRepository.setJuristicMethod(juristicMethod)
    }

    fun setHighLatitudesAdjustmentMethod(adjustmentMethod: HighLatitudesAdjustmentMethod) {
        prayersRepository.setAdjustHighLatitudes(adjustmentMethod)
    }

    fun getAthanAudioId() = prayersRepository.getAthanAudioId()

    fun setAthanAudioId(athanAudioId: Int) {
        prayersRepository.setAthanAudioId(athanAudioId)
    }

    fun restartActivity(activity: Activity) {
        ActivityUtils.restartActivity(activity)
    }

    fun getLocation() = locationRepository.getLocation()

    suspend fun getPrayerTime(prayer: Prayer): Calendar {
        val location = locationRepository.getLocation().first()!!

        var prayerTime = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )[prayer]!!

        // if prayer time passed
        if (prayerTime.timeInMillis < System.currentTimeMillis()) {
            prayerTime = PrayerTimeUtils.getPrayerTimes(
                settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
                selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
                location = location,
                calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            )[prayer]!!
        }

        return prayerTime
    }

    suspend fun setDevotionalReminder(reminder: Reminder.Devotional, hour: Int, minute: Int) {
        val adjustedTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            add(Calendar.MINUTE, 30)
        }

        setDevotionReminderEnabled(reminder, true)
        setDevotionReminderTimeOfDay(
            reminder = reminder,
            timeOfDay = TimeOfDay(
                hour = adjustedTime.get(Calendar.HOUR_OF_DAY),
                minute = adjustedTime.get(Calendar.MINUTE)
            )
        )

        setAlarm(reminder)
    }

    fun cancelDevotionalReminder(reminder: Reminder.Devotional) {
        setDevotionReminderEnabled(reminder, false)
        cancelAlarm(reminder)
    }

    private fun setDevotionReminderEnabled(reminder: Reminder.Devotional, enabled: Boolean) {
        notificationsRepository.setDevotionalReminderEnabled(enabled, reminder)
    }

    private fun setDevotionReminderTimeOfDay(reminder: Reminder.Devotional, timeOfDay: TimeOfDay) {
        notificationsRepository.setDevotionalReminderTimes(timeOfDay, reminder)
    }

    private suspend fun setAlarm(reminder: Reminder) {
        alarm.setAlarm(reminder)
    }

    private fun cancelAlarm(reminder: Reminder) {
        alarm.cancelAlarm(reminder)
    }

}