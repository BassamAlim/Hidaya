package bassamalim.hidaya.features.settings.domain

import android.app.Application
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.models.TimeOfDay
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class SettingsDomain @Inject constructor(
    private val app: Application,
    private val appSettingsRepository: AppSettingsRepository,
    private val prayersRepository: PrayersRepository,
    private val notificationsRepository: NotificationsRepository,
    private val locationRepository: LocationRepository
) {

    suspend fun resetPrayerTimes() {
        val location = locationRepository.getLocation().first() ?: return

        val prayerTimes = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )

        Alarms(app).setAll(prayerTimes)
    }

    suspend fun setAlarm(pid: PID) {
        Alarms(app).setPidAlarm(pid)
    }

    fun cancelAlarm(pid: PID) {
        PrayerTimeUtils.cancelAlarm(app, pid)
    }

    fun getLanguage() = appSettingsRepository.getLanguage()

    suspend fun setLanguage(language: Language) {
        appSettingsRepository.setLanguage(language)
    }

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

    suspend fun setNumeralsLanguage(numeralsLanguage: Language) {
        appSettingsRepository.setNumeralsLanguage(numeralsLanguage)
    }

    fun getTimeFormat() = appSettingsRepository.getTimeFormat()

    suspend fun setTimeFormat(timeFormat: TimeFormat) {
        appSettingsRepository.setTimeFormat(timeFormat)
    }

    fun getTheme() = appSettingsRepository.getTheme()

    suspend fun setTheme(theme: Theme) {
        appSettingsRepository.setTheme(theme)
    }

    fun getDevotionReminderEnabledMap() =
        notificationsRepository.getDevotionReminderEnabledMap()

    suspend fun setDevotionReminderEnabled(enabled: Boolean, pid: PID) {
        notificationsRepository.setDevotionReminderEnabled(enabled, pid)
    }

    fun getDevotionReminderTimeOfDayMap() =
        notificationsRepository.getDevotionReminderTimeOfDayMap()

    suspend fun setDevotionReminderTimeOfDay(timeOfDay: TimeOfDay, pid: PID) {
        notificationsRepository.setDevotionReminderTimeOfDay(timeOfDay, pid)
    }

    fun getPrayerTimesCalculatorSettings() = prayersRepository.getPrayerTimesCalculatorSettings()

    suspend fun setPrayerTimeCalculationMethod(calculationMethod: PrayerTimeCalculationMethod) {
        prayersRepository.setCalculationMethod(calculationMethod)
    }

    suspend fun setPrayerTimeJuristicMethod(juristicMethod: PrayerTimeJuristicMethod) {
        prayersRepository.setJuristicMethod(juristicMethod)
    }

    suspend fun setHighLatitudesAdjustmentMethod(adjustmentMethod: HighLatitudesAdjustmentMethod) {
        prayersRepository.setAdjustHighLatitudes(adjustmentMethod)
    }

    fun getAthanId() = prayersRepository.getAthanAudioId()

    suspend fun setAthanId(athanId: Int) {
        prayersRepository.setAthanAudioId(athanId)
    }

}