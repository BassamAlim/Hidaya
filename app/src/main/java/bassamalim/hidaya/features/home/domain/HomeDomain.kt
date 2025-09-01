package bassamalim.hidaya.features.home.domain

import android.app.Application
import bassamalim.hidaya.core.data.repositories.AnalyticsRepository
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.models.AnalyticsEvent
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.Response
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.OsUtils.getDeviceId
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max

class HomeDomain @Inject constructor(
    app: Application,
    private val prayersRepository: PrayersRepository,
    private val locationRepository: LocationRepository,
    private val quranRepository: QuranRepository,
    private val userRepository: UserRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val analyticsRepository: AnalyticsRepository
) {

    private val deviceId = getDeviceId(app)

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
            numeralsLanguage = getNumeralsLanguage().first()
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

    fun getPreviousPrayer(times: Map<Prayer, Calendar?>): Prayer? {
        val currentMillis = System.currentTimeMillis()
        for (prayer in times.entries.reversed()) {
            val millis = prayer.value!!.timeInMillis
            if (millis < currentMillis) return prayer.key
        }
        return null
    }

    fun getNextPrayer(times: Map<Prayer, Calendar?>): Prayer? {
        val currentMillis = System.currentTimeMillis()
        for (prayer in times.entries) {
            val millis = prayer.value!!.timeInMillis
            if (millis > currentMillis) return prayer.key
        }
        return null
    }

    fun getLanguage() = appSettingsRepository.getLanguage()

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

    fun getWerdPage() = quranRepository.getWerdPageNum()

    fun isWerdDone() = quranRepository.isWerdDone()

    fun getLocalRecord() = userRepository.getLocalRecord()

    fun getLocation() = locationRepository.getLocation()

    fun getPrayerNames() = prayersRepository.getPrayerNames()

    suspend fun syncRecords(): Boolean {
        val remoteRecord = userRepository.getRemoteRecord(deviceId)

        if (remoteRecord == null) return false

        return when (val response = remoteRecord.first()) {
            is Response.Success -> {
                val remoteRecord = response.data!!

                val localRecord = getLocalRecord().first()

                val latestRecord = UserRecord(
                    userId = remoteRecord.userId,
                    quranPages = max(
                        localRecord.quranPages,
                        remoteRecord.quranPages
                    ),
                    recitationsTime = max(
                        localRecord.recitationsTime,
                        remoteRecord.recitationsTime
                    )
                )

                if (remoteRecord.quranPages != latestRecord.quranPages ||
                    remoteRecord.recitationsTime > latestRecord.recitationsTime) {
                    userRepository.setRemoteRecord(
                        deviceId = deviceId,
                        record = latestRecord
                    )
                }

                if (localRecord.quranPages != latestRecord.quranPages ||
                    localRecord.recitationsTime > latestRecord.recitationsTime) {
                    userRepository.setLocalRecord(
                        UserRecord(
                            quranPages = latestRecord.quranPages,
                            recitationsTime = latestRecord.recitationsTime
                        )
                    )
                }

                true
            }
            is Response.Error -> {
                if (response.message == "Device not registered") {
                    val remoteRecord = registerDevice(deviceId)
                    if (remoteRecord != null) {
                        userRepository.setLocalRecord(
                            UserRecord(
                                quranPages = remoteRecord.quranPages,
                                recitationsTime = remoteRecord.recitationsTime
                            )
                        )
                        true
                    } else false
                } else false
            }
        }
    }

    fun trackDailyWerdViewed() {
        analyticsRepository.trackEvent(AnalyticsEvent.DailyWerdViewed)
    }

    private suspend fun registerDevice(deviceId: String) = userRepository.registerDevice(deviceId)

}