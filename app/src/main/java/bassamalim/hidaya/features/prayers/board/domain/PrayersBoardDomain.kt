package bassamalim.hidaya.features.prayers.board.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import bassamalim.hidaya.features.prayers.settings.ui.PrayerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class PrayersBoardDomain @Inject constructor(
    private val prayersRepository: PrayersRepository,
    private val locationRepository: LocationRepository,
    private val notificationsRepository: NotificationsRepository,
    private val appStateRepository: AppStateRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val alarm: Alarm
) {

    val location = locationRepository.getLocation()

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getHijriMonths() = appStateRepository.getHijriMonths()

    suspend fun getCountryName(countryId: Int, language: Language) =
        locationRepository.getCountryName(
            countryId = countryId,
            language = language
        )

    suspend fun getCityName(cityId: Int, language: Language) =
        locationRepository.getCityName(
            cityId = cityId,
            language = language
        )

    fun getPrayerNames() = prayersRepository.getPrayerNames()

    fun getPrayerSettings(): Flow<Map<Prayer, PrayerSettings>> {
        return combine(
            getNotificationTypes(),
            getReminderOffsets()
        ) { notificationTypes, reminderOffsets ->
            val prayersPIDs = listOf(
                Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHAA
            )

            prayersPIDs.associateWith { pid ->
                PrayerSettings(
                    notificationType = notificationTypes[pid.toReminder()]!!,
                    reminderOffset = reminderOffsets[pid.toReminder()]!!
                )
            }
        }
    }

    suspend fun updatePrayerSettings(prayer: Prayer, prayerSettings: PrayerSettings) {
        notificationsRepository.setNotificationType(
            prayer = prayer.toReminder(),
            type = prayerSettings.notificationType
        )

        notificationsRepository.setPrayerExtraReminderOffset(
            prayer = prayer.toReminder(),
            offset = prayerSettings.reminderOffset
        )
    }

    private fun getNotificationTypes() = notificationsRepository.getNotificationTypes()

    private fun getReminderOffsets() = notificationsRepository.getPrayerExtraReminderTimeOffsets()

    suspend fun getShouldShowTutorial() = prayersRepository.getShouldShowTutorial().first()

    suspend fun setDoNotShowAgain() {
        prayersRepository.setShouldShowTutorial(false)
    }

    suspend fun getTimes(
        location: Location,
        dateOffset: Int
    ): Map<Prayer, String?> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, dateOffset)
        }

        val prayerTimes = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = calendar
        )

        return PrayerTimeUtils.formatPrayerTimes(
            prayerTimes = prayerTimes,
            language = appSettingsRepository.getLanguage().first(),
            timeFormat = appSettingsRepository.getTimeFormat().first(),
            numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first(),
        )
    }

    suspend fun updatePrayerTimeAlarms(prayer: Prayer) {
        alarm.setAlarm(prayer.toReminder())
    }

}