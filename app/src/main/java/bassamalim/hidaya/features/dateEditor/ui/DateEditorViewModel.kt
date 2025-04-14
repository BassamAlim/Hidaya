package bassamalim.hidaya.features.dateEditor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.helpers.Navigator
import bassamalim.hidaya.core.utils.LangUtils
import bassamalim.hidaya.features.dateEditor.domain.DateEditorDomain
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DateEditorViewModel @Inject constructor(
    private val domain: DateEditorDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(DateEditorUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = DateEditorUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            domain.assignDateOffset()

            numeralsLanguage = domain.getNumeralsLanguage()

            updateState()
        }
    }

    fun onNextDayClick() {
        domain.incrementDateOffset()

        updateState()
    }

    fun onPreviousDayClick() {
        domain.decrementDateOffset()

        updateState()
    }

    fun onSave() {
        viewModelScope.launch {
            domain.saveDateOffset()

            navigator.navigateBackWithResult(data = null)
        }
    }

    fun onDismiss() {
        navigator.navigateBackWithResult(data = null)
    }

    private fun updateState() {
        val dateOffset = domain.getDateOffset()
        if (dateOffset == 0) {
            _uiState.update { it.copy(
                isUnchanged = true,
                dateText = getDateText()
            )}
        }
        else {
            _uiState.update { it.copy(
                isUnchanged = false,
                dateOffsetText = getDateOffsetText(dateOffset),
                dateText = getDateText()
            )}
        }
    }

    private fun getDateText(): String {
        val cal = UmmalquraCalendar().apply {
            val millisInDay = 1000 * 60 * 60 * 24
            timeInMillis += (domain.getDateOffset() * millisInDay).toLong()
        }

        return LangUtils.translateNums(
            numeralsLanguage = numeralsLanguage,
            string = "${cal[Calendar.DATE]}/${cal[Calendar.MONTH] + 1}/${cal[Calendar.YEAR]}"
        )
    }

    private fun getDateOffsetText(dateOffset: Int): String {
        var offsetStr = dateOffset.toString()
        if (dateOffset > 0) offsetStr = "+$offsetStr"
        return LangUtils.translateNums(
            numeralsLanguage = numeralsLanguage,
            string = offsetStr
        )
    }

}