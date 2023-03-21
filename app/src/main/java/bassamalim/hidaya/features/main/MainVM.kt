package bassamalim.hidaya.features.main

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    private val repo: MainRepo
): ViewModel() {

    private var dateOffset = repo.getDateOffset()

    private val _uiState = MutableStateFlow(MainState(
        hijriDate = getHijriDate(),
        gregorianDate = getGregorianDate()
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
        hijri.timeInMillis = hijri.timeInMillis + dateOffset * millisInDay

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
        dateOffset++
        updateDateEditor()
    }

    fun onDateEditorPrevDay() {
        dateOffset--
        updateDateEditor()
    }

    private fun updateDateEditor() {
        val cal = UmmalquraCalendar()
        val millisInDay = 1000 * 60 * 60 * 24
        cal.timeInMillis = cal.timeInMillis + dateOffset * millisInDay

        // update tvs
        val dateText = translateNums(
            repo.numeralsLanguage,
            "${cal[Calendar.DATE]}/${cal[Calendar.MONTH] + 1}/${cal[Calendar.YEAR]}"
        )

        var offsetText = repo.getUnchangedStr()
        if (dateOffset != 0) {
            var offsetStr = dateOffset.toString()
            if (dateOffset > 0) offsetStr = "+$offsetStr"
            offsetText = translateNums(repo.numeralsLanguage, offsetStr)
        }

        _uiState.update { it.copy(
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

        repo.updateDateOffset(dateOffset)
    }

    fun onDateEditorCancel() {
        _uiState.update { it.copy(
            dateEditorShown = false
        )}
    }

}