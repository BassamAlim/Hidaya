package bassamalim.hidaya.features.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.os.CountDownTimer
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.helpers.PrayTimes
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.core.utils.PTUtils
import bassamalim.hidaya.features.leaderboard.UserRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max


@HiltViewModel
class HomeVM @Inject constructor(
    app: Application,
    private val repo: HomeRepo,
    private val navigator: Navigator
): AndroidViewModel(app) {

    private val deviceId = getDeviceId(app)
    private var latestUserRecord = repo.getLocalRecord()
    private val prayerNames = repo.getPrayerNames()
    private val numeralsLanguage = repo.getNumeralsLanguage()
    private var times: Array<Calendar?> = arrayOfNulls(6)
    private var formattedTimes: List<String> = arrayListOf()
    private var tomorrowFajr: Calendar = Calendar.getInstance()
    private var formattedTomorrowFajr: String = ""
    private var timer: CountDownTimer? = null
    private var upcomingPrayer = 0
    private var tomorrow = false
    private var counterCounter = 0
    var pastTime = 0L
        private set
    var upcomingTime = 0L
        private set
    var remaining = 0L
        private set

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    init {
        if (isInternetConnected(app)) {
            updateUserRecords()
        }
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun onStart() {
        if (repo.getLocation() != null) setupPrayersCard()

        _uiState.update { it.copy(
            telawatRecord = formatTelawatTime(repo.getTelawatPlaybackRecord()),
            quranPagesRecord = formatQuranPagesRecord(repo.getQuranPagesRecord()),
            todayWerdPage = getTodayWerdPage(),
            isWerdDone = repo.getIsWerdDone()
        )}
    }

    fun onStop() {
        timer?.cancel()
    }

    fun onGotoTodayWerdClick() {
        navigator.navigate(
            Screen.QuranViewer(
                "by_page",
                page = _uiState.value.todayWerdPage
            )
        )
    }

    fun gotoLeaderboard() {
        navigator.navigate(
            Screen.Leaderboard(
                latestUserRecord.userId.toString(),
                latestUserRecord.readingRecord.toString(),
                latestUserRecord.listeningRecord.toString()
            )
        )
    }

    private fun setupPrayersCard() {
        getTimes(repo.getLocation()!!)
        setupUpcomingPrayer()
    }

    private fun getTimes(location: Location) {
        val prayTimes = PrayTimes(repo.sp)

        val utcOffset = PTUtils.getUTCOffset(repo.sp, repo.db)

        val today = Calendar.getInstance()
        times = prayTimes.getPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), today
        )
        formattedTimes = prayTimes.getStrPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), today
        )

        val tomorrow = Calendar.getInstance()
        tomorrow[Calendar.DATE]++
        tomorrowFajr = prayTimes.getPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), tomorrow
        )[0]!!
        tomorrowFajr[Calendar.DATE]++
        formattedTomorrowFajr = prayTimes.getStrPrayerTimes(
            location.latitude, location.longitude, utcOffset.toDouble(), tomorrow
        )[0]
    }

    private fun setupUpcomingPrayer() {
        upcomingPrayer = findUpcoming()

        tomorrow = false
        if (upcomingPrayer == -1) {
            tomorrow = true
            upcomingPrayer = 0
        }

        var till = times[upcomingPrayer]!!.timeInMillis
        if (tomorrow) till = tomorrowFajr.timeInMillis

        _uiState.update { it.copy(
            upcomingPrayerName = prayerNames[upcomingPrayer],
            upcomingPrayerTime =
            if (tomorrow) formattedTomorrowFajr
            else formattedTimes[upcomingPrayer]
        )}

        count(till)
    }

    private fun count(till: Long) {
        timer = object : CountDownTimer(
            till - System.currentTimeMillis(), 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / (60 * 60 * 1000) % 24
                val minutes = millisUntilFinished / (60 * 1000) % 60
                val seconds = millisUntilFinished / 1000 % 60

                val hms = String.format(
                    Locale.US,
                    "%02d:%02d:%02d",
                    hours, minutes, seconds
                )

                _uiState.update { it.copy(
                    remainingTime = translateNums(
                        numeralsLanguage = numeralsLanguage,
                        string = hms,
                        timeFormat = true
                    )
                )}

                val past =
                    if (upcomingPrayer == 0) -1L
                    else times[upcomingPrayer - 1]!!.timeInMillis

                pastTime = past
                upcomingTime = times[upcomingPrayer]!!.timeInMillis
                remaining = millisUntilFinished
            }

            override fun onFinish() {
                counterCounter++
                if (counterCounter < 5)
                    setupPrayersCard()
            }
        }.start()
    }

    private fun findUpcoming(): Int {
        val currentMillis = System.currentTimeMillis()
        for (i in times.indices) {
            val millis = times[i]!!.timeInMillis
            if (millis > currentMillis) return i
        }
        return -1
    }

    private fun getTodayWerdPage(): String {
        return translateNums(
            numeralsLanguage = numeralsLanguage,
            string = repo.getTodayWerdPage().toString()
        )
    }

    private fun updateUserRecords() {
        viewModelScope.launch {
            when (
                val response = repo.getRemoteUserRecord(deviceId)
            ) {
                is Response.Success -> {
                    val remoteUserRecord = response.data
                    syncUserRecords(remoteUserRecord!!)
                }
                is Response.Error -> {
                    if (response.message == "Device not registered") {
                        val remoteUserRecord = repo.registerDevice(deviceId)
                        if (remoteUserRecord != null) {
                            latestUserRecord = remoteUserRecord

                            _uiState.update { it.copy(
                                leaderboardEnabled = true
                            )}
                        }
                    }
                }
            }
        }
    }

    private fun syncUserRecords(remoteRecord: UserRecord) {
        val localRecord = latestUserRecord.copy()

        latestUserRecord = UserRecord(
            userId = remoteRecord.userId,
            readingRecord = max(localRecord.readingRecord, remoteRecord.readingRecord),
            listeningRecord = max(localRecord.listeningRecord, remoteRecord.listeningRecord)
        )

        if (latestUserRecord.readingRecord > remoteRecord.readingRecord ||
            latestUserRecord.listeningRecord > remoteRecord.listeningRecord) {
            viewModelScope.launch {
                repo.setRemoteUserRecord(deviceId, latestUserRecord)
            }
        }

        if (latestUserRecord.readingRecord > localRecord.readingRecord)
            repo.setLocalQuranRecord(latestUserRecord.readingRecord)
        if (latestUserRecord.listeningRecord > localRecord.listeningRecord)
            repo.setLocalTelawatRecord(latestUserRecord.listeningRecord)

        _uiState.update { it.copy(
            quranPagesRecord = formatQuranPagesRecord(latestUserRecord.readingRecord),
            telawatRecord = formatTelawatTime(latestUserRecord.listeningRecord),
            leaderboardEnabled = true
        )}
    }

    private fun formatTelawatTime(millis: Long): String {
        val hours = millis / (60 * 60 * 1000) % 24
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60

        return translateNums(
            numeralsLanguage,
            String.format(
                Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds
            )
        )
    }

    private fun formatQuranPagesRecord(num: Int): String {
        return translateNums(
            numeralsLanguage = numeralsLanguage,
            string = num.toString()
        )
    }

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetwork != null &&
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) != null
    }

}