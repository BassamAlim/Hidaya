package bassamalim.hidaya.features.dateConverter.ui

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.dateConverter.domain.DateConverterDomain
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
class DateConverterViewModel @Inject constructor(
    private val domain: DateConverterDomain,
    private val navigator: Navigator
): ViewModel() {

    private var hijriCalendar = UmmalquraCalendar()
    private var gregorianCalendar = Calendar.getInstance()
    private val hijriMonth = domain.getHijriMonths()
    private val gregorianMonths = domain.getGregorianMonths()
    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(DateConverterUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = DateConverterUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()

            _uiState.update { it.copy(
                gregorianDatePickerMillis = gregorianCalendar.timeInMillis
            )}
        }
    }

    fun onPickGregorianClick() {
        _uiState.update { it.copy(
            isGregorianDatePickerShown = true
        )}
    }

    fun onGregorianDatePicked(millis: Long?) {
        if (millis == null) return

        val pickedDate = Calendar.getInstance().apply {
            timeInMillis = millis
        }

        gregorianCalendar = pickedDate

        hijriCalendar = domain.gregorianToHijri(pickedDate) as UmmalquraCalendar

        updateDates()

        _uiState.update { it.copy(
            gregorianDatePickerMillis = pickedDate.timeInMillis,
            isGregorianDatePickerShown = false
        )}
    }

    fun onGregorianDatePickerDismiss() {
        _uiState.update { it.copy(
            isGregorianDatePickerShown = false
        )}
    }

    fun onPickHijriClick() {
        val dateStr = "${hijriCalendar[Calendar.YEAR]}" +
                "-${hijriCalendar[Calendar.MONTH] + 1}" +
                "-${hijriCalendar[Calendar.DATE]}"

        navigator.navigateForResult(
            Screen.HijriDatePicker(initialDate = dateStr)
        ) { result ->
            if (result != null) {
                val date =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        result.getSerializable("selected_date", UmmalquraCalendar::class.java)
                    else
                        result.getSerializable("selected_date") as UmmalquraCalendar

                hijriCalendar = date!!
                gregorianCalendar = domain.hijriToGregorian(date)

                updateDates()
            }
        }
    }

    private fun updateDates() {
        _uiState.update { it.copy(
            hijriDate = Date(
                year = translateNums(
                    numeralsLanguage = numeralsLanguage,
                    string = hijriCalendar[Calendar.YEAR].toString()
                ),
                month = hijriMonth[hijriCalendar[Calendar.MONTH]],
                day = translateNums(
                    numeralsLanguage = numeralsLanguage,
                    string = hijriCalendar[Calendar.DATE].toString()
                )
            ),
            gregorianDate = Date(
                year = translateNums(
                    numeralsLanguage = numeralsLanguage,
                    string = gregorianCalendar[Calendar.YEAR].toString()
                ),
                month = gregorianMonths[gregorianCalendar[Calendar.MONTH]],
                day = translateNums(
                    numeralsLanguage = numeralsLanguage,
                    string = gregorianCalendar[Calendar.DATE].toString()
                )
            )
        )}
    }

}