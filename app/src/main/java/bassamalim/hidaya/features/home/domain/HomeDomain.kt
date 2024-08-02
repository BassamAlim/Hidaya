package bassamalim.hidaya.features.home.domain

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.helpers.PrayerTimesCalculator
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.OS.getDeviceId
import bassamalim.hidaya.core.utils.PTUtils
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max

class HomeDomain @Inject constructor(
    private val app: Application,
    private val appSettingsRepo: AppSettingsRepository,
    private val prayersRepository: PrayersRepository,
    private val quranRepo: QuranRepository,
    private val userRepo: UserRepository
) {

    private val deviceId = getDeviceId(app)
    val location = getLocation()

    suspend fun getTimes(): Array<Calendar?> {
        val prayerTimesCalculator = getPrayerTimesCalculator()
        val loc = location.first()!!
        return prayerTimesCalculator.getPrayerTimes(
            lat = loc.latitude,
            lon = loc.longitude,
            tZone = getUTCOffset().toDouble(),
            date = Calendar.getInstance()
        )
    }

    suspend fun getStrTimes(): ArrayList<String> {
        val prayerTimesCalculator = getPrayerTimesCalculator()
        val loc = location.first()!!
        return prayerTimesCalculator.getStrPrayerTimes(
            lat = loc.latitude,
            lng = loc.longitude,
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
            lat = loc.latitude,
            lon = loc.longitude,
            tZone = getUTCOffset().toDouble(),
            date = tomorrow
        )[0]!!
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
            lng = loc.longitude,
            tZone = utcOffset.toDouble(),
            date = tomorrow
        )[0]
    }

    fun getUpcomingPrayerIndex(times: Array<Calendar?>): Int {
        val currentMillis = System.currentTimeMillis()
        for (i in times.indices) {
            val millis = times[i]!!.timeInMillis
            if (millis > currentMillis) return i
        }
        return -1
    }

    private suspend fun getPrayerTimesCalculator() = PrayerTimesCalculator(
        settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
        timeFormat = appSettingsRepo.getTimeFormat().first(),
        timeOffsets = prayersRepository.getTimeOffsets().first(),
        numeralsLanguage = appSettingsRepo.getNumeralsLanguage().first()
    )

    private suspend fun getUTCOffset() = PTUtils.getUTCOffset(
        locationType = location.first()!!.type,
        timeZone = userRepo.getTimeZone(location.first()!!.cityId)
    )

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getWerdPage() = quranRepo.getWerdPage()

    fun getIsWerdDone() = quranRepo.getIsWerdDone()

    fun getLocalRecord() = userRepo.getLocalRecord()

    private fun getLocation() = userRepo.getLocation()

    fun getPrayerNames() = prayersRepository.getPrayerNames()

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