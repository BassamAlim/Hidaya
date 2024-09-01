package bassamalim.hidaya.features.prayers.prayersMain.domain

import android.app.Application
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import bassamalim.hidaya.features.prayers.prayerSettings.ui.PrayerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class PrayersDomain @Inject constructor(
    private val app: Application,
    private val prayersRepository: PrayersRepository,
    private val appSettingsRepo: AppSettingsRepository,
    private val appStateRepo: AppStateRepository,
    private val notificationsRepository: NotificationsRepository,
    private val locationRepo: LocationRepository
) {

    val location = locationRepo.getLocation()

    suspend fun getLanguage() = appSettingsRepo.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getHijriMonths() = appStateRepo.getHijriMonths()

    fun getCountryName(countryId: Int, language: Language) =
        locationRepo.getCountryName(
            countryId = countryId,
            language = language
        )

    fun getCityName(cityId: Int, language: Language) =
        locationRepo.getCityName(
            cityId = cityId,
            language = language
        )

    fun getPrayerNames() = prayersRepository.getPrayerNames()

    fun getPrayerSettings(): Flow<Map<PID, PrayerSettings>> {
        return combine(
            getNotificationTypes(),
            getTimeOffsets(),
            getReminderOffsets()
        ) { notificationTypes, timeOffsets, reminderOffsets ->
            val prayersPIDs = listOf(
                PID.FAJR, PID.SUNRISE, PID.DHUHR, PID.ASR, PID.MAGHRIB, PID.ISHAA
            )

            prayersPIDs.associateWith { pid ->
                PrayerSettings(
                    notificationType = notificationTypes[pid] ?: NotificationType.NOTIFICATION,
                    timeOffset = timeOffsets[pid] ?: 0,
                    reminderOffset = reminderOffsets[pid] ?: 0
                )
            }
        }
    }

    suspend fun updatePrayerSettings(pid: PID, prayerSettings: PrayerSettings) {
        notificationsRepository.setNotificationType(
            pid = pid,
            type = prayerSettings.notificationType
        )

        prayersRepository.setTimeOffset(pid = pid, timeOffset = prayerSettings.timeOffset)

        notificationsRepository.setPrayerReminderOffset(
            pid = pid,
            offset = prayerSettings.reminderOffset
        )
    }

    private fun getNotificationTypes() = notificationsRepository.getNotificationTypeMap()

    private fun getTimeOffsets() = prayersRepository.getTimeOffsets()

    private fun getReminderOffsets() = notificationsRepository.getPrayerReminderOffsetMap()

    suspend fun getShouldShowTutorial() = prayersRepository.getShouldShowTutorial().first()

    suspend fun setDoNotShowAgain() {
        prayersRepository.setShouldShowTutorial(false)
    }

    suspend fun getTimes(
        location: Location,
        dateOffset: Int
    ): Map<PID, String?> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, dateOffset)
        }

        val prayerTimes = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepo.getTimeZone(location.ids.cityId),
            location = location,
            calendar = calendar
        )

        return PrayerTimeUtils.formatPrayerTimes(
            prayerTimes = prayerTimes,
            language = appSettingsRepo.getLanguage().first(),
            timeFormat = appSettingsRepo.getTimeFormat().first(),
            numeralsLanguage = appSettingsRepo.getNumeralsLanguage().first(),
        )
    }

    suspend fun updatePrayerTimeAlarms(pid: PID) {
        Alarms(app).setPidAlarm(pid)
    }

}