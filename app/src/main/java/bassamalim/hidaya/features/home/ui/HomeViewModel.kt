package bassamalim.hidaya.features.home.ui

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.home.domain.HomeDomain
import bassamalim.hidaya.features.quran.reader.domain.QuranTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val domain: HomeDomain,
    private val navigator: Navigator
): ViewModel() {

    private val prayerNames = domain.getPrayerNames()
    private var times: Map<Prayer, Calendar?> = emptyMap()
    private var formattedTimes: Map<Prayer, String> = emptyMap()
    private var tomorrowFajr: Calendar = Calendar.getInstance()
    private var formattedTomorrowFajr: String = ""
    private var timer: CountDownTimer? = null
    private var upcomingPrayer: Prayer? = null
    private var tomorrow = false
    private var counterCounter = 0

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getNumeralsLanguage(),
        domain.getLocation(),
        domain.getWerdPage(),
        domain.isWerdDone()
    ) { state, numeralsLanguage, location, werdPage, isWerdDone ->
        if (location != null && timer == null)
            setupPrayersCard(location)

        state.copy(
            werdPage = translateNums(
                string = werdPage.toString(),
                numeralsLanguage = numeralsLanguage
            ),
            isWerdDone = isWerdDone,
            numeralsLanguage = numeralsLanguage,
            location = location
        )
    }.combine(
        domain.getLocalRecord()
    ) { state, localRecord ->
        state.copy(
            quranRecord = translateNums(
                string = localRecord.quranPages.toString(),
                numeralsLanguage = state.numeralsLanguage
            ),
            recitationsRecord = formatRecitationsTime(
                millis = localRecord.recitationsTime,
                numeralsLanguage = state.numeralsLanguage
            )
        )
    }.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = HomeUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            val isSuccess = domain.syncRecords()
            if (isSuccess) {
                _uiState.update { it.copy(
                    isLeaderboardEnabled = true
                )}
            }
        }
    }

    fun onStart() {

    }

    fun onStop() {
        timer?.cancel()
    }

    fun onGotoTodayWerdClick() {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.PAGE.name,
                targetValue = _uiState.value.werdPage
            )
        )
    }

    fun onLeaderboardClick() {
        navigator.navigate(Screen.Leaderboard)
    }

    private fun setupPrayersCard(location: Location) {
        viewModelScope.launch {
            times = domain.getPrayerTimeMap(location)
            formattedTimes = domain.getStrPrayerTimeMap(location)
            tomorrowFajr = domain.getTomorrowFajr(location)
            formattedTomorrowFajr = domain.getStrTomorrowFajr(location)

            setupUpcomingPrayer(location)
        }
    }

    private fun setupUpcomingPrayer(location: Location) {
        upcomingPrayer = domain.getUpcomingPrayer(times)

        tomorrow = false
        if (upcomingPrayer == null) {
            tomorrow = true
            upcomingPrayer = Prayer.FAJR
        }

        val till =
            if (tomorrow) tomorrowFajr.timeInMillis
            else times[upcomingPrayer]!!.timeInMillis

        _uiState.update { it.copy(
            upcomingPrayerName = prayerNames[upcomingPrayer]!!,
            upcomingPrayerTime =
                if (tomorrow) formattedTomorrowFajr
                else formattedTimes[upcomingPrayer]!!
        )}

        count(till, location)
    }

    private fun count(till: Long, location: Location) {
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
                    remaining = translateNums(
                        string = hms,
                        numeralsLanguage = it.numeralsLanguage,
                        isTime = true
                    ),
                    timeFromPreviousPrayer =
                        if (upcomingPrayer == Prayer.FAJR) -1L
                        else times[Prayer.entries[
                            Prayer.entries.indexOf(upcomingPrayer) - 1
                        ]]!!.timeInMillis,
                    timeToNextPrayer = millisUntilFinished
                )}
            }

            override fun onFinish() {
                counterCounter++
                if (counterCounter < 5)
                    setupPrayersCard(location)
            }
        }.start()
    }

    private fun formatRecitationsTime(millis: Long, numeralsLanguage: Language): String {
        val hours = millis / (60 * 60 * 1000) % 24
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60

        return translateNums(
            string = String.format(
                Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds
            ),
            numeralsLanguage = numeralsLanguage
        )
    }

}