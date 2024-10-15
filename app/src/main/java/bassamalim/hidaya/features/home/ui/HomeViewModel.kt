package bassamalim.hidaya.features.home.ui

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.core.utils.LangUtils.translateTimeNums
import bassamalim.hidaya.features.home.domain.HomeDomain
import bassamalim.hidaya.features.quran.reader.domain.QuranTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.properties.Delegates

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
    private var werdPage by Delegates.notNull<Int>()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getLanguage(),
        domain.getNumeralsLanguage(),
        domain.getLocation(),
        domain.getWerdPage()
    ) { state, language, numeralsLanguage, location, werdPage ->
        if (state.isLoading) return@combine state

        this.werdPage = werdPage
        val upcomingPrayer = getUpcomingPrayer()
        if (location != null && timer == null)
            count()

        state.copy(
            upcomingPrayerName = prayerNames[upcomingPrayer]!!,
            upcomingPrayerTime = translateNums(
                string = if (tomorrow) formattedTomorrowFajr
                else formattedTimes[upcomingPrayer]!!,
                numeralsLanguage = numeralsLanguage
            ),
            werdPage = translateNums(
                string = werdPage.toString(),
                numeralsLanguage = numeralsLanguage
            ),
            language = language,
            numeralsLanguage = numeralsLanguage,
        )
    }.combine(
        domain.isWerdDone()
    ) { state, isWerdDone ->
        state.copy(isWerdDone = isWerdDone,)
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
                language = state.language,
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
            val location = domain.getLocation().first()
            if (location != null) {
                times = domain.getPrayerTimeMap(location)
                formattedTimes = domain.getStrPrayerTimeMap(location)
                tomorrowFajr = domain.getTomorrowFajr(location)
                formattedTomorrowFajr = domain.getStrTomorrowFajr(location)
            }

            val leaderboardConnected = domain.syncRecords()

            _uiState.update { it.copy(
                isLoading = false,
                isLeaderboardEnabled = leaderboardConnected
            )}
        }
    }

    fun onStart() {
        timer?.start()
    }

    fun onStop() {
        timer?.cancel()
    }

    fun onGotoTodayWerdClick() {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.PAGE.name,
                targetValue = werdPage.toString()
            )
        )
    }

    fun onLeaderboardClick() {
        navigator.navigate(Screen.Leaderboard)
    }

    private fun getUpcomingPrayer(): Prayer? {
        upcomingPrayer = domain.getUpcomingPrayer(times)

        tomorrow = false
        if (upcomingPrayer == null) {
            tomorrow = true
            upcomingPrayer = Prayer.FAJR
        }

        return upcomingPrayer
    }

    private fun count() {
        val till =
            if (tomorrow) tomorrowFajr.timeInMillis
            else times[upcomingPrayer]!!.timeInMillis
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

                viewModelScope.launch {
                    val numeralsLanguage = domain.getNumeralsLanguage().first()
                    _uiState.update { it.copy(
                        remaining = translateTimeNums(
                            string = hms,
                            language = it.language,
                            numeralsLanguage = numeralsLanguage
                        ),
                        timeFromPreviousPrayer =
                            if (upcomingPrayer == Prayer.FAJR) -1L
                            else times[Prayer.entries[
                                Prayer.entries.indexOf(upcomingPrayer) - 1
                            ]]!!.timeInMillis,
                        timeToNextPrayer = millisUntilFinished
                    )}
                }
            }

            override fun onFinish() {}
        }.start()
    }

    private fun formatRecitationsTime(
        millis: Long,
        language: Language,
        numeralsLanguage: Language
    ): String {
        val hours = millis / (60 * 60 * 1000) % 24
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60

        return translateTimeNums(
            string = String.format(
                Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds
            ),
            language = language,
            numeralsLanguage = numeralsLanguage
        )
    }

}