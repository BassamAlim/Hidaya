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
import java.util.SortedMap
import javax.inject.Inject

class PrayersBoardDomain @Inject constructor(
    private val prayersRepository: PrayersRepository,
    private val locationRepository: LocationRepository,
    private val notificationsRepository: NotificationsRepository,
    private val appStateRepository: AppStateRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val alarm: Alarm
) {

    fun getLocation() = locationRepository.getLocation()

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getHijriMonths() = appStateRepository.getHijriMonthNames()

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
            val prayers = listOf(
                Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHAA
            )
            prayers.associateWith { prayer ->
                PrayerSettings(
                    notificationType = notificationTypes[prayer.toReminder()]!!,
                    reminderOffset = reminderOffsets[prayer.toExtraReminder()]!!
                )
            }
        }
    }

    private fun getNotificationTypes() = notificationsRepository.getNotificationTypes()

    private fun getReminderOffsets() = notificationsRepository.getPrayerExtraReminderTimeOffsets()

    suspend fun getShouldShowTutorial() = prayersRepository.getShouldShowTutorial().first()

    suspend fun setDoNotShowAgain() {
        prayersRepository.setShouldShowTutorial(false)
    }

    suspend fun getTimes(
        location: Location,
        date: Calendar
    ): SortedMap<Prayer, String> {
        val prayerTimes = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = date
        )

        return PrayerTimeUtils.formatPrayerTimes(
            prayerTimes = prayerTimes,
            language = appSettingsRepository.getLanguage().first(),
            timeFormat = appSettingsRepository.getTimeFormat().first(),
            numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first(),
        )
    }

}