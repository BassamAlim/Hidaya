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
import bassamalim.hidaya.core.helpers.PrayerTimeCalculator
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.OS.getDeviceId
import bassamalim.hidaya.core.utils.PTUtils
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.SortedMap
import javax.inject.Inject
import kotlin.math.max

class HomeDomain @Inject constructor(
    private val app: Application,
    private val appSettingsRepo: AppSettingsRepository,
    private val prayersRepo: PrayersRepository,
    private val locationRepo: LocationRepository,
    private val quranRepo: QuranRepository,
    private val userRepo: UserRepository
) {

    private val deviceId = getDeviceId(app)
    val location = getLocation()

    suspend fun getTimes(): SortedMap<PID, Calendar?> {
        val prayerTimesCalculator = getPrayerTimesCalculator()
        val loc = location.first()!!
        return prayerTimesCalculator.getPrayerTimes(
            latitude = loc.latitude,
            longitude = loc.longitude,
            utcOffset = getUTCOffset().toDouble(),
            calendar = Calendar.getInstance()
        )
    }

    suspend fun getStrTimes(): SortedMap<PID, String> {
        val prayerTimesCalculator = getPrayerTimesCalculator()
        val loc = location.first()!!
        return prayerTimesCalculator.getStrPrayerTimes(
            lat = loc.latitude,
            lon = loc.longitude,
            tZone = getUTCOffset().toDouble(),
            date = Calendar.getInstance()
        )
    }

    suspend fun getTomorrowFajr(): Calendar {
        val prayerTimesCalculator = getPrayerTimesCalculator()
        val loc = location.first()!!

        val tomorrow = Calendar.getInstance()
        tomorrow[Calendar.DATE]++

        val tomorrowFajr = prayerTimesCalculator.getPrayerTimes(
            latitude = loc.latitude,
            longitude = loc.longitude,
            utcOffset = getUTCOffset().toDouble(),
            calendar = tomorrow
        )[PID.FAJR]!!
        tomorrowFajr[Calendar.DATE]++

        return tomorrowFajr
    }

    suspend fun getStrTomorrowFajr(): String {
        val prayerTimesCalculator = getPrayerTimesCalculator()
        val utcOffset = getUTCOffset()
        val loc = location.first()!!

        val tomorrow = Calendar.getInstance()
        tomorrow[Calendar.DATE]++

        return prayerTimesCalculator.getStrPrayerTimes(
            lat = loc.latitude,
            lon = loc.longitude,
            tZone = utcOffset.toDouble(),
            date = tomorrow
        )[PID.FAJR]!!
    }

    fun getUpcomingPrayer(times: Map<PID, Calendar?>): PID? {
        val currentMillis = System.currentTimeMillis()
        for (prayer in times.entries) {
            val millis = prayer.value!!.timeInMillis
            if (millis > currentMillis) return prayer.key
        }
        return null
    }

    private suspend fun getPrayerTimesCalculator() = PrayerTimeCalculator(
        settings = prayersRepo.getPrayerTimesCalculatorSettings().first(),
        timeFormat = appSettingsRepo.getTimeFormat().first(),
        timeOffsets = prayersRepo.getTimeOffsets().first(),
        numeralsLanguage = appSettingsRepo.getNumeralsLanguage().first()
    )

    private suspend fun getUTCOffset() = PTUtils.getUTCOffset(
        locationType = location.first()!!.type,
        timeZone = locationRepo.getTimeZone(location.first()!!.cityId)
    )

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getWerdPage() = quranRepo.getWerdPage()

    fun getIsWerdDone() = quranRepo.getIsWerdDone()

    fun getLocalRecord() = userRepo.getLocalRecord()

    private fun getLocation() = locationRepo.getLocation()

    fun getPrayerNames() = prayersRepo.getPrayerNames()

    suspend fun syncRecords(): Boolean {
        if (!isInternetConnected(app)) return false

        return when (
            val response = userRepo.getRemoteRecord(deviceId)
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
                    userRepo.setRemoteRecord(
                        deviceId = deviceId,
                        record = latestRecord
                    )
                }

                if (localRecord.quranPages != latestRecord.quranPages ||
                    localRecord.recitationsTime > latestRecord.recitationsTime) {
                    userRepo.setLocalRecord(
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
                        userRepo.setLocalRecord(
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

    private suspend fun registerDevice(deviceId: String) = userRepo.registerDevice(deviceId)

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetwork != null &&
                connectivityManager.getNetworkCapabilities(
                    connectivityManager.activeNetwork
                ) != null
    }

}