package bassamalim.hidaya.features.home.ui

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.models.TimeOfDay
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
    private var yesterdayIshaa: Calendar = Calendar.getInstance()
    private var formattedYesterdayIshaa: String = ""
    private var tomorrowFajr: Calendar = Calendar.getInstance()
    private var formattedTomorrowFajr: String = ""
    private var timer: CountDownTimer? = null
    private var previousPrayer: Prayer? = null
    private var nextPrayer: Prayer? = null
    private var previousPrayerWasYesterday = false
    private var nextPrayerIsTomorrow = false
    private var werdPage by Delegates.notNull<Int>()
    private var shouldCount = false

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
        val previousPrayer = getPreviousPrayer()
        val nextPrayer = getNextPrayer()

        state.copy(
            previousPrayerName = prayerNames[previousPrayer]!!,
            previousPrayerTimeText = translateNums(
                string = if (previousPrayerWasYesterday) formattedYesterdayIshaa
                else formattedTimes[previousPrayer]!!,
                numeralsLanguage = numeralsLanguage
            ),
            nextPrayerName = prayerNames[nextPrayer]!!,
            nextPrayerTimeText = translateNums(
                string = if (nextPrayerIsTomorrow) formattedTomorrowFajr
                else formattedTimes[nextPrayer]!!,
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
        state.copy(isWerdDone = isWerdDone)
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
                yesterdayIshaa = domain.getYesterdayIshaa(location)
                formattedYesterdayIshaa = domain.getStrYesterdayIshaa(location)
                tomorrowFajr = domain.getTomorrowFajr(location)
                formattedTomorrowFajr = domain.getStrTomorrowFajr(location)
            }

            shouldCount = location != null && times.isNotEmpty()

            val leaderboardConnected = domain.syncRecords()

            _uiState.update { it.copy(
                isLoading = false,
                isLeaderboardEnabled = leaderboardConnected
            )}
        }
    }

    fun onStart() {
        initializeData()

        if (shouldCount)
            count()
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

    private fun getPreviousPrayer(): Prayer? {
        previousPrayer = domain.getPreviousPrayer(times)

        previousPrayerWasYesterday = false
        if (previousPrayer == null) {
            previousPrayerWasYesterday = true
            previousPrayer = Prayer.ISHAA
        }

        return previousPrayer
    }

    private fun getNextPrayer(): Prayer? {
        nextPrayer = domain.getNextPrayer(times)

        nextPrayerIsTomorrow = false
        if (nextPrayer == null) {
            nextPrayerIsTomorrow = true
            nextPrayer = Prayer.FAJR
        }

        return nextPrayer
    }

    private fun count() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }

        val till =
            if (nextPrayerIsTomorrow) tomorrowFajr.timeInMillis
            else times[nextPrayer]!!.timeInMillis
        timer = object : CountDownTimer(
            /* millisInFuture = */ till - System.currentTimeMillis(),
            /* countDownInterval = */ 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val previousPrayerTime =
                    if (nextPrayer == Prayer.FAJR) yesterdayIshaa
                    else times[previousPrayer]!!
                val nextPrayerTime =
                    if (nextPrayerIsTomorrow) tomorrowFajr
                    else times[nextPrayer]!!

                val timeFromPreviousPrayer =
                    if (nextPrayer == Prayer.FAJR)
                        System.currentTimeMillis() - previousPrayerTime.timeInMillis
                    else
                        System.currentTimeMillis() - times[previousPrayer]!!.timeInMillis
                val timeFromPreviousPrayerHours = timeFromPreviousPrayer / (60 * 60 * 1000) % 24
                val timeFromPreviousPrayerMinutes = timeFromPreviousPrayer / (60 * 1000) % 60
                val timeFromPreviousPrayerSeconds = timeFromPreviousPrayer / 1000 % 60
                val timeFromPreviousPrayerHms = String.format(
                    Locale.US,
                    "%02d:%02d:%02d",
                    timeFromPreviousPrayerHours,
                    timeFromPreviousPrayerMinutes,
                    timeFromPreviousPrayerSeconds
                )

                val timeToNextPrayerHours = millisUntilFinished / (60 * 60 * 1000) % 24
                val timeToNextPrayerMinutes = millisUntilFinished / (60 * 1000) % 60
                val timeToNextPrayerSeconds = millisUntilFinished / 1000 % 60
                val timeToNextPrayerHms = String.format(
                    Locale.US,
                    "%02d:%02d:%02d",
                    timeToNextPrayerHours,
                    timeToNextPrayerMinutes,
                    timeToNextPrayerSeconds
                )

                viewModelScope.launch {
                    _uiState.update { it.copy(
                        passed = translateTimeNums(
                            string = timeFromPreviousPrayerHms,
                            language = it.language,
                            numeralsLanguage = it.numeralsLanguage
                        ),
                        remaining = translateTimeNums(
                            string = timeToNextPrayerHms,
                            language = it.language,
                            numeralsLanguage = it.numeralsLanguage
                        ),
                        previousPrayerTime = TimeOfDay.fromCalendar(previousPrayerTime),
                        nextPrayerTime = TimeOfDay.fromCalendar(nextPrayerTime)
                    )}
                }
            }

            override fun onFinish() {
                onStart()
            }
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