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
import kotlinx.coroutines.flow.first
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

    private val initialDate = savedStateHandle.get<String>("initial_date")

    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language
    private val months = domain.getMonths()
    private val weekDays = domain.getWeekDays()
    var initialPage by Delegates.notNull<Int>()
    val pageCount = { (domain.maxYear - domain.minYear + 1) * 12 }
    private lateinit var daysPagerState: PagerState
    private lateinit var coroutineScope: CoroutineScope

    private val _uiState = MutableStateFlow(HijriDatePickerUiState(
        displayedYear = getDisplayedYearString(),
        yearSelectorItems = getYearSelectorItems(),
        mainText = getMainText(),
        displayedMonth = getDisplayedMonthString(initialPage)
    ))
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = HijriDatePickerUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage().first()
            numeralsLanguage = domain.getNumeralsLanguage().first()

            _uiState.update { it.copy(
                weekDaysAbb = domain.getWeekDaysAbb(language),
            )}

            initialPage = ((_uiState.value.displayedYear.toInt() - domain.minYear) * 12
                    + _uiState.value.displayedMonth.toInt())

            initialDate?.let { dateStr ->
                val date = dateStr.split("-")
                val year = date[0].toInt()
                val month = date[1].toInt() - 1  // 0-based
                val day = date[2].toInt()

                _uiState.update { it.copy(
                    displayedYear = year.toString(),
                    displayedMonth = month.toString(),
                    selectedDay = day.toString(),
                )}
            }
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
                (year.toInt() - domain.minYear) * 12 + _uiState.value.displayedMonth.toInt()
            )
        }

        _uiState.update { it.copy(
            selectorMode = SelectorMode.DAY_MONTH
        )}
    }

    fun onPreviousMonthClick() {
        coroutineScope.launch {
            daysPagerState.animateScrollToPage(daysPagerState.currentPage - 1)
        }
    }

    fun onNextMonthClick() {
        coroutineScope.launch {
            daysPagerState.animateScrollToPage(daysPagerState.currentPage + 1)
        }
    }

    fun onDaySelected(selection: String) {
        domain.setSelectedDate(
            year = _uiState.value.displayedYear.toInt(),
            month = _uiState.value.displayedMonth.toInt(),
            day = selection.toInt()
        )

        _uiState.update { it.copy(
            selectedDay = selection
        )}
    }

    fun onSelectClicked() {
        navigator.navigateBackWithResult(
            Bundle().apply {
                putSerializable(
                    "selected_date",
                    UmmalquraCalendar(
                        _uiState.value.displayedYear.toInt(),
                        _uiState.value.displayedMonth.toInt(),
                        _uiState.value.selectedDay.toInt()
                    )
                )
            }
        )
    }

    fun onCancelClicked() {
        navigator.navigateBackWithResult(null)
    }

    private fun getDisplayedYearString() =
        translateNums(
            numeralsLanguage = numeralsLanguage,
            string = ((domain.minYear * 12 + daysPagerState.currentPage) / 12).toString()
        )

    private fun getDisplayedMonthString(currentPage: Int): String {
        val absMonth = domain.minYear * 12 + currentPage
        return "${months[absMonth % 12]} " +
                translateNums(
                    string = (absMonth / 12).toString(),
                    numeralsLanguage = numeralsLanguage
                )
    }

    private fun getMainText(): String {
        val selectedDate = domain.getSelectedDate()
        return "${weekDays[selectedDate[Calendar.DAY_OF_WEEK] - 1]} " +
                "${
                    translateNums(
                        string = selectedDate[Calendar.DATE].toString(),
                        numeralsLanguage = numeralsLanguage
                    )
                } " +
                months[selectedDate[Calendar.MONTH]]
    }

    private fun getYearSelectorItems() =
        Array(domain.maxYear - domain.minYear + 1) { idx ->
            (domain.minYear + idx).toString()
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
                if (current[Calendar.YEAR].toString() == _uiState.value.displayedYear
                    && current[Calendar.MONTH].toString() == _uiState.value.displayedMonth)
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

}