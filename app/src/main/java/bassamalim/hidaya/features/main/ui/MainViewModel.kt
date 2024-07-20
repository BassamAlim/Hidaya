package bassamalim.hidaya.features.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.main.domain.MainDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val domain: MainDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(MainUiState(
        gregorianDate = getGregorianDate()
    ))
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getDateOffset()
    ) { state, dateOffset -> state.copy(
        hijriDate = getHijriDate(dateOffset)
    )}.stateIn(
        initialValue = MainUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    init {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()
        }
    }

    fun onDateClick() {
        navigator.navigateForResult(
            destination = Screen.DateEditor,
            onResult = {}
        )
    }

    private fun getHijriDate(dateOffset: Int): String {
        val hijri = domain.getHijriDateCalendar(dateOffset)

        val hDayName = domain.getWeekDays()[hijri[Calendar.DAY_OF_WEEK] - 1]
        val hMonth = domain.getHijriMonths()[hijri[Calendar.MONTH]]
        val hijriStr = "$hDayName ${hijri[Calendar.DATE]} $hMonth ${hijri[Calendar.YEAR]}"
        return translateNums(
            numeralsLanguage = numeralsLanguage,
            string = hijriStr
        )
    }

    private fun getGregorianDate(): String {
        val gregorian = domain.getGregorianDateCalendar()

        val mMonth = domain.getGregorianMonths()[gregorian[Calendar.MONTH]]
        val gregorianStr = "${gregorian[Calendar.DATE]} $mMonth ${gregorian[Calendar.YEAR]}"
        return translateNums(
            numeralsLanguage = numeralsLanguage,
            string = gregorianStr
        )
    }

}