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
import bassamalim.hidaya.core.helpers.PrayerTimeCalculator
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.utils.PTUtils
import bassamalim.hidaya.features.prayers.prayerSettings.ui.PrayerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class PrayersDomain @Inject constructor(
    private val app: Application,
    private val prayersRepo: PrayersRepository,
    private val appSettingsRepo: AppSettingsRepository,
    private val appStateRepo: AppStateRepository,
    private val notificationsRepo: NotificationsRepository,
    private val locationRepo: LocationRepository
) {

    val location = locationRepo.getLocation()

    suspend fun setLocation(location: Location) {
        locationRepo.setLocation(location)
    }

    suspend fun getLanguage() = appSettingsRepo.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getHijriMonths() = appStateRepo.getHijriMonths()

    fun getClosest(lat: Double, lon: Double) = locationRepo.getClosestCity(lat, lon)

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

    fun getPrayerNames() = prayersRepo.getPrayerNames()

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
        val notificationTypes = notificationsRepo.getNotificationTypeMap().first()
        notificationsRepo.setNotificationTypeMap(
            notificationTypes.toMutableMap().apply {
                this[pid] = prayerSettings.notificationType
            }.toMap()
        )

        val timeOffsets = prayersRepo.getTimeOffsets().first()
        prayersRepo.setTimeOffsets(
            timeOffsets.toMutableMap().apply {
                this[pid] = prayerSettings.timeOffset
            }.toMap()
        )

        val reminderOffsets = notificationsRepo.getPrayerReminderOffsetMap().first()
        notificationsRepo.setPrayerReminderOffsetMap(
            reminderOffsets.toMutableMap().apply {
                this[pid] = prayerSettings.reminderOffset
            }.toMap()
        )
    }

    private fun getNotificationTypes() = notificationsRepo.getNotificationTypeMap()

    private fun getTimeOffsets() = prayersRepo.getTimeOffsets()

    private fun getReminderOffsets() = notificationsRepo.getPrayerReminderOffsetMap()

    suspend fun getShouldShowTutorial() = prayersRepo.getShouldShowTutorial().first()

    suspend fun setDoNotShowAgain() {
        prayersRepo.setShouldShowTutorial(false)
    }

    fun getTimes(
        calculator: PrayerTimeCalculator,
        location: Location,
        dateOffset: Int
    ): Map<PID, String?> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, dateOffset)
        }

        val prayerTimes = calculator.getStrPrayerTimes(
            lat = location.latitude,
            lon = location.longitude,
            tZone = getUTCOffset(location).toDouble(),
            date = calendar
        )
        return mapOf(
            PID.FAJR to prayerTimes[0],
            PID.SUNRISE to prayerTimes[1],
            PID.DHUHR to prayerTimes[2],
            PID.ASR to prayerTimes[3],
            PID.MAGHRIB to prayerTimes[4],
            PID.ISHAA to prayerTimes[5]
        )
    }

    fun getPrayerTimesCalculator() = combine(
        prayersRepo.getPrayerTimesCalculatorSettings(),
        appSettingsRepo.getTimeFormat(),
        prayersRepo.getTimeOffsets(),
        appSettingsRepo.getNumeralsLanguage()
    ) {
        prayerTimesCalculatorSettings, timeFormat, timeOffsets, numeralsLanguage ->
        PrayerTimeCalculator(
            settings = prayerTimesCalculatorSettings,
            timeFormat = timeFormat,
            timeOffsets = timeOffsets,
            numeralsLanguage = numeralsLanguage
        )
    }

    private fun getUTCOffset(location: Location) = PTUtils.getUTCOffset(
        locationType = location.type,
        timeZone = locationRepo.getTimeZone(location.cityId)
    )

    fun updatePrayerTimeAlarms(pid: PID) {
        Alarms(context = app, pid = pid)
    }

}