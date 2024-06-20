@file:OptIn(ExperimentalFoundationApi::class)

package bassamalim.hidaya.features.hijriDatePicker

import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.nav.Navigator
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HijriDatePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: HijriDatePickerRepository,
    private val navigator: Navigator
): ViewModel() {

    private val initialDate = savedStateHandle.get<String>("initial_date")

    val numeralsLanguage = repo.numeralsLanguage
    val months = repo.months
    val weekDays = repo.weekDays
    val weekDaysAbb = repo.weekDaysAbb
    val now = UmmalquraCalendar()
    private lateinit var daysPagerState: PagerState
    private lateinit var coroutineScope: CoroutineScope
    val minYear = now[Calendar.YEAR] - 100
    val maxYear = now[Calendar.YEAR] + 100

    private val _uiState = MutableStateFlow(HijriDatePickerState())
    val uiState = _uiState.asStateFlow()

    init {
        initialDate?.let { dateStr ->
            val date = dateStr.split("-")
            val year = date[0].toInt()
            val month = date[1].toInt() - 1  // 0-based
            val day = date[2].toInt()
            val selected = UmmalquraCalendar(year, month, day)

            _uiState.update { it.copy(
                selected = selected,
            )}
        }
    }

    fun onStart(daysPagerState: PagerState, coroutineScope: CoroutineScope) {
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

    fun onYearSelected(year: Int) {
        coroutineScope.launch {
            daysPagerState.scrollToPage(
                (year - minYear) * 12 + _uiState.value.selected[Calendar.MONTH]
            )
        }

        _uiState.update { it.copy(
            selectorMode = SelectorMode.DAY_MONTH
        )}
    }

    fun onPrevMonthClk() {
        coroutineScope.launch {
            daysPagerState.animateScrollToPage(daysPagerState.currentPage - 1)
        }
    }

    fun onNextMonthClk() {
        coroutineScope.launch {
            daysPagerState.animateScrollToPage(daysPagerState.currentPage + 1)
        }
    }

    fun onDaySelected(year: Int, month: Int, day: Int) {
        _uiState.update { it.copy(
            selected = UmmalquraCalendar(year, month, day)
        )}
    }

    fun onDateSelected() {
        navigator.navigateBackWithResult(
            Bundle().apply {
                putSerializable("selected_date", _uiState.value.selected)
            }
        )
    }

    fun onCanceled() {
        navigator.navigateBackWithResult(null)
    }

    fun buildGrid(current: UmmalquraCalendar): Array<Array<String>> {
        val offset = current[Calendar.DAY_OF_WEEK]
        val grid = Array(5) { row ->  // 1 hijri month spans 5 weeks at most
            Array(7) { col ->  // 7 days a week
                val idx = row * 7 + col - offset
                if (row == 0 && col < offset || idx >= current.lengthOfMonth()) ""
                else (idx + 1).toString()
            }
        }
        return grid
    }

}