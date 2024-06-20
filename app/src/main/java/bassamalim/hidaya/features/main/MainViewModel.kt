package bassamalim.hidaya.features.main

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: MainRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(MainState(
        hijriDate = getHijriDate(),
        gregorianDate = getGregorianDate(),
        dateOffset = repo.getDateOffset()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        updateDateEditor()
    }

    fun showDateEditor() {
        _uiState.update { it.copy(
            dateEditorShown = true
        )}
    }

    private fun getHijriDate(): String {
        val hijri = UmmalquraCalendar()
        val hDayName = repo.getWeekDays()[hijri[Calendar.DAY_OF_WEEK] - 1]

        val millisInDay = 1000 * 60 * 60 * 24
        hijri.timeInMillis += _uiState.value.dateOffset * millisInDay

        val hMonth = repo.getHijriMonths()[hijri[Calendar.MONTH]]
        val hijriStr = "$hDayName ${hijri[Calendar.DATE]} $hMonth ${hijri[Calendar.YEAR]}"
        return translateNums(repo.numeralsLanguage, hijriStr)
    }

    private fun getGregorianDate(): String {
        val gregorian = Calendar.getInstance()
        val mMonth = repo.getGregorianMonths()[gregorian[Calendar.MONTH]]
        val gregorianStr = "${gregorian[Calendar.DATE]} $mMonth ${gregorian[Calendar.YEAR]}"
        return translateNums(repo.numeralsLanguage, gregorianStr)
    }

    fun onDateEditorNextDay() {
        updateDateEditor(newDateOffset = _uiState.value.dateOffset + 1)
    }

    fun onDateEditorPrevDay() {
        updateDateEditor(newDateOffset = _uiState.value.dateOffset - 1)
    }

    private fun updateDateEditor(newDateOffset: Int = _uiState.value.dateOffset) {
        val cal = UmmalquraCalendar()
        val millisInDay = 1000 * 60 * 60 * 24
        cal.timeInMillis += newDateOffset * millisInDay

        val dateText = translateNums(
            repo.numeralsLanguage,
            "${cal[Calendar.DATE]}/${cal[Calendar.MONTH] + 1}/${cal[Calendar.YEAR]}"
        )

        var offsetText = repo.unchangedStr
        if (newDateOffset != 0) {
            var offsetStr = newDateOffset.toString()
            if (newDateOffset > 0) offsetStr = "+$offsetStr"
            offsetText = translateNums(repo.numeralsLanguage, offsetStr)
        }

        _uiState.update { it.copy(
            dateOffset = newDateOffset,
            dateEditorOffsetText = offsetText,
            dateEditorDateText = dateText
        )}
    }

    fun onDateEditorSubmit() {
        _uiState.update { it.copy(
            hijriDate = getHijriDate(),
            gregorianDate = getGregorianDate(),
            dateEditorShown = false
        )}

        repo.updateDateOffset(_uiState.value.dateOffset)
    }

    fun onDateEditorCancel() {
        _uiState.update { it.copy(
            dateEditorShown = false
        )}
    }

}