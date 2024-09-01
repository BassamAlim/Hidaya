package bassamalim.hidaya.features.home.domain

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.OS.getDeviceId
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.SortedMap
import javax.inject.Inject
import kotlin.math.max

class HomeDomain @Inject constructor(
    private val app: Application,
    private val appSettingsRepository: AppSettingsRepository,
    private val prayersRepository: PrayersRepository,
    private val locationRepository: LocationRepository,
    private val quranRepository: QuranRepository,
    private val userRepository: UserRepository
) {

    private val deviceId = getDeviceId(app)
    val location = getLocation()

    suspend fun getPrayerTimeMap(): SortedMap<PID, Calendar?> {
        return PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepository.getTimeZone(location.first()!!.cityId),
            location = location.first()!!,
            calendar = Calendar.getInstance()
        )
    }

    suspend fun getStrPrayerTimeMap(): SortedMap<PID, String> {
        val prayerTimeMap = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepository.getTimeZone(location.first()!!.cityId),
            location = location.first()!!,
            calendar = Calendar.getInstance()
        )

        return PrayerTimeUtils.formatPrayerTimes(
            prayerTimes = prayerTimeMap,
            timeFormat = appSettingsRepository.getTimeFormat().first(),
            language = appSettingsRepository.getLanguage().first(),
            numeralsLanguage = getNumeralsLanguage()
        )
    }

    suspend fun getTomorrowFajr(): Calendar {
        return PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepository.getTimeZone(location.first()!!.cityId),
            location = location.first()!!,
            calendar = Calendar.getInstance().apply { this[Calendar.DATE]++ }
        )[PID.FAJR]!!
    }

    suspend fun getStrTomorrowFajr(): String {
        val time = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepository.getTimeZone(location.first()!!.cityId),
            location = location.first()!!,
            calendar = Calendar.getInstance().apply { this[Calendar.DATE]++ }
        )[PID.FAJR]!!

        return PrayerTimeUtils.formatPrayerTime(
            time = time,
            language = appSettingsRepository.getLanguage().first(),
            numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first(),
            timeFormat = appSettingsRepository.getTimeFormat().first()
        )
    }

    fun getUpcomingPrayer(times: Map<PID, Calendar?>): PID? {
        val currentMillis = System.currentTimeMillis()
        for (prayer in times.entries) {
            val millis = prayer.value!!.timeInMillis
            if (millis > currentMillis) return prayer.key
        }
        return null
    }

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getWerdPage() = quranRepository.getWerdPage()

    fun getIsWerdDone() = quranRepository.getIsWerdDone()

    fun getLocalRecord() = userRepository.getLocalRecord()

    private fun getLocation() = locationRepository.getLocation()

    fun getPrayerNames() = prayersRepository.getPrayerNames()

    suspend fun syncRecords(): Boolean {
        if (!isInternetConnected(app)) return false

        return when (
            val response = userRepository.getRemoteRecord(deviceId)
        ) {
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

    private suspend fun registerDevice(deviceId: String) = userRepository.registerDevice(deviceId)

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetwork != null &&
                connectivityManager.getNetworkCapabilities(
                    connectivityManager.activeNetwork
                ) != null
    }

}