package bassamalim.hidaya.features.home.domain

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.helpers.PrayerTimesCalculator
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.PTUtils
import bassamalim.hidaya.features.home.data.HomeRepository
import bassamalim.hidaya.features.leaderboard.LeaderboardUserRecord
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max

class HomeDomain @Inject constructor(
    private val app: Application,
    private val repository: HomeRepository
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
        settings = repository.getPrayerTimesCalculatorSettings().first(),
        timeFormat = repository.getTimeFormat().first(),
        timeOffsets = repository.getTimeOffsets().first(),
        numeralsLanguage = getNumeralsLanguage()
    )

    private suspend fun getUTCOffset() = PTUtils.getUTCOffset(
        locationType = location.first()!!.type,
        timeZone = repository.getTimeZone(location.first()!!.cityId)
    )

    suspend fun getNumeralsLanguage() = repository.getNumeralsLanguage()

    fun getTimeFormat() = repository.getTimeFormat()

    fun getWerdPage() = repository.getWerdPage()

    fun getIsWerdDone() = repository.getIsWerdDone()

    fun getLocalRecord() = repository.getLocalRecord()

    private fun getLocation() = repository.getLocation()

    fun getPrayerTimesCalculatorSettings() = repository.getPrayerTimesCalculatorSettings()

    fun getPrayerTimeOffsets() = repository.getTimeOffsets()

    fun getPrayerNames() = repository.getPrayerNames()

    suspend fun syncRecords(): Boolean {
        if (!isInternetConnected(app)) return false

        return when (
            val response = repository.getRemoteRecord(deviceId)
        ) {
            is Response.Success -> {
                val remoteRecord = response.data!!

                val localRecord = getLocalRecord().first()

                val latestRecord = LeaderboardUserRecord(
                    userId = remoteRecord.userId,
                    quranRecord = max(
                        localRecord.quranPages,
                        remoteRecord.quranRecord
                    ),
                    recitationsRecord = max(
                        localRecord.recitationsTime,
                        remoteRecord.recitationsRecord
                    )
                )

                if (remoteRecord.quranRecord != latestRecord.quranRecord ||
                    remoteRecord.recitationsRecord > latestRecord.recitationsRecord) {
                    repository.setRemoteRecord(
                        deviceId = deviceId,
                        record = latestRecord
                    )
                }

                if (localRecord.quranPages != latestRecord.quranRecord ||
                    localRecord.recitationsTime > latestRecord.recitationsRecord) {
                    repository.setLocalRecord(
                        UserRecord(
                            quranPages = latestRecord.quranRecord,
                            recitationsTime = latestRecord.recitationsRecord
                        )
                    )
                }

                true
            }
            is Response.Error -> {
                if (response.message == "Device not registered") {
                    val remoteRecord = registerDevice(deviceId)
                    if (remoteRecord != null) {
                        repository.setLocalRecord(
                            UserRecord(
                                quranPages = remoteRecord.quranRecord,
                                recitationsTime = remoteRecord.recitationsRecord
                            )
                        )
                        true
                    } else false
                } else false
            }
        }
    }

    private suspend fun registerDevice(deviceId: String) = repository.registerDevice(deviceId)

    @SuppressLint("HardwareIds")
    private fun getDeviceId(app: Application) =
        Settings.Secure.getString(
            app.contentResolver,
            Settings.Secure.ANDROID_ID
        )

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetwork != null &&
                connectivityManager.getNetworkCapabilities(
                    connectivityManager.activeNetwork
                ) != null
    }

}