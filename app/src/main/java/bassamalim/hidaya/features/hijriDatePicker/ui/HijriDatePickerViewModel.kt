@file:OptIn(ExperimentalFoundationApi::class)

package bassamalim.hidaya.features.hijriDatePicker.ui

import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.hijriDatePicker.domain.HijriDatePickerDomain
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class HijriDatePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: HijriDatePickerDomain,
    private val navigator: Navigator
): ViewModel() {

    private val initialDate = savedStateHandle.get<String>("initial_date")!!

    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language
    private val monthsNames = domain.getMonthNames()
    private val weekDays = domain.getWeekDays()
    var initialPage by Delegates.notNull<Int>()
    val pageCount = { (domain.maxYear - domain.minYear + 1) * 12 }
    private lateinit var daysPagerState: PagerState
    private lateinit var coroutineScope: CoroutineScope
    private var displayedMonth by Delegates.notNull<Int>()
    private var displayedYear by Delegates.notNull<Int>()

    private val _uiState = MutableStateFlow(HijriDatePickerUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = HijriDatePickerUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()

            val date = initialDate.split("-")
            val year = date[0].toInt()
            val month = date[1].toInt()
            val day = date[2].toInt()

            initialPage = (year - domain.minYear) * 12 + month  // getpagenum
            displayedYear = year
            displayedMonth = month
            domain.setSelectedDate(year, month, day)

            _uiState.update { it.copy(
                isLoading = false,
                mainText = getMainText(),
                displayedYearText = translateNums(
                    numeralsLanguage = numeralsLanguage,
                    string = displayedYear.toString()
                ),
                displayedMonthText = getDisplayedMonthString(displayedMonth),
                selectedDay = day.toString(),
                weekDaysAbb = domain.getWeekDaysAbb(language),
                yearSelectorItems = getYearSelectorItems()
            )}
        }
    }

    fun onStart(
        daysPagerState: PagerState,
        coroutineScope: CoroutineScope
    ) {
        this.daysPagerState = daysPagerState
        this.coroutineScope = coroutineScope
    }

    fun onYearSelectorToggled() {
        _uiState.update { it.copy(
            selectorMode = when (it.selectorMode) {
                SelectorMode.DAY_MONTH -> SelectorMode.YEAR
                SelectorMode.YEAR -> SelectorMode.DAY_MONTH
            }
        )}
    }

    fun onYearSelected(year: String) {
        coroutineScope.launch {
            daysPagerState.scrollToPage(
                (year.toInt() - domain.minYear) * 12
                        + displayedMonth  // getpagenum
            )

            _uiState.update { it.copy(
                selectorMode = SelectorMode.DAY_MONTH
            )}

            updateDisplayed()
        }
    }

    fun onPreviousMonthClick() {
        coroutineScope.launch {
            daysPagerState.animateScrollToPage(daysPagerState.currentPage - 1)

            updateDisplayed()
        }
    }

    fun onNextMonthClick() {
        coroutineScope.launch {
            daysPagerState.animateScrollToPage(daysPagerState.currentPage + 1)

            updateDisplayed()
        }
    }

    fun onDaySelected(selection: String) {
        domain.setSelectedDate(
            year = displayedYear,
            month = displayedMonth,
            day = selection.toInt()
        )

        _uiState.update { it.copy(
            selectedDay = selection,
            mainText = getMainText()
        )}
    }

    fun onSelectClicked() {
        navigator.navigateBackWithResult(
            Bundle().apply {
                putSerializable(
                    "selected_date",
                    domain.getSelectedDate()
                )
            }
        )
    }

    fun onCancelClicked() {
        navigator.navigateBackWithResult(null)
    }

    private fun updateDisplayed() {
        displayedYear = (domain.minYear * 12 + daysPagerState.currentPage) / 12
        displayedMonth = (domain.minYear * 12 + daysPagerState.currentPage) % 12 + 1

        _uiState.update { it.copy(
            displayedYearText = translateNums(
                numeralsLanguage = numeralsLanguage,
                string = displayedYear.toString()
            ),
            displayedMonthText = getDisplayedMonthString(displayedMonth)
        )}
    }

    private fun getDisplayedMonthString(month: Int) =
        "${monthsNames[month-1]} " + translateNums(
            string = month.toString(),
            numeralsLanguage = numeralsLanguage
        )

    private fun getMainText(): String {
        val selectedDate = domain.getSelectedDate()
        return "${weekDays[selectedDate[Calendar.DAY_OF_WEEK] - 1]} " +
                "${
                    translateNums(
                        string = selectedDate[Calendar.DATE].toString(),
                        numeralsLanguage = numeralsLanguage
                    )
                } " +
                monthsNames[selectedDate[Calendar.MONTH]]
    }

    private fun getYearSelectorItems() =
        Array(domain.maxYear - domain.minYear + 1) { idx ->
            translateNums(
                string = (domain.minYear + idx).toString(),
                numeralsLanguage = numeralsLanguage
            )
        }.toList()

    fun getDaysGrid(page: Int): List<List<String>> {
        val absMonth = domain.minYear * 12 + page
        val current = UmmalquraCalendar()
        current[Calendar.YEAR] = absMonth / 12
        current[Calendar.MONTH] = absMonth % 12

        val offset = current[Calendar.DAY_OF_WEEK]
        val grid = Array(5) { row ->  // 1 hijri month spans 5 weeks at most
            Array(7) { col ->  // 7 days a week
                val idx = row * 7 + col - offset
                if (row == 0 && col < offset || idx >= current.lengthOfMonth()) ""
                else translateNums(
                    numeralsLanguage = numeralsLanguage,
                    string = (idx + 1).toString()
                )
            }.toList()
        }.toList()

        val currentDate = domain.getCurrentDate()
        _uiState.update { it.copy(
            selectedDay =
                if (current[Calendar.YEAR].toString() == _uiState.value.displayedYearText
                    && current[Calendar.MONTH].toString() == _uiState.value.displayedMonthText)
                    _uiState.value.selectedDay
                else ".",
            currentDay =
                if (current[Calendar.YEAR].toString() == currentDate[Calendar.YEAR].toString()
                    && current[Calendar.MONTH].toString() == currentDate[Calendar.MONTH].toString())
                    currentDate[Calendar.DATE].toString()
                else "."
        )}

        return grid
    }

    private fun getPageNum(year: Int, month: Int) = (year - domain.minYear) * 12 + (month-1)

    fun getYearAndMonth(pageNum: Int): Pair<Int, Int> {
        val year = domain.minYear + pageNum / 12
        val month = pageNum % 12 + 1
        return Pair(year, month)
    }

}