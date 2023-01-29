package bassamalim.hidaya.viewmodel

import android.app.Application
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.repository.DateConverterRepo
import bassamalim.hidaya.state.DateConverterState
import bassamalim.hidaya.utils.LangUtils.translateNums
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DateConverterVM @Inject constructor(
    private val app: Application,
    private val repository: DateConverterRepo
): AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(DateConverterState())
    val uiState = _uiState.asStateFlow()

    val hijriCalendar = UmmalquraCalendar()
    private val gregorianCalendar = Calendar.getInstance()
    private val hijriMonths = repository.getHijriMonths()
    private val gregorianMonths = repository.getGregorianMonths()

    fun onHijriPickCancel() {
        _uiState.update { it.copy(
            hijriDatePickerShown = false
        )}
    }

    fun onHijriPick(pickedHijri: UmmalquraCalendar) {
        _uiState.update { it.copy(
            hijriDatePickerShown = false
        )}

        val gregorianCal = hijriToGregorian(pickedHijri)
        updateCalendars(pickedHijri, gregorianCal)

        display()
    }

    fun pickGregorian() {
        val ctx = app.applicationContext

        val datePicker = DatePickerDialog(
            ctx,
            { _: DatePicker?, year: Int, month: Int, day: Int ->
                val choice = Calendar.getInstance()
                choice[Calendar.YEAR] = year
                choice[Calendar.MONTH] = month // starts from 0
                choice[Calendar.DATE] = day

                val hijriCal = gregorianToHijri(choice)
                updateCalendars(hijriCal, choice)
                display()
            },
            gregorianCalendar[Calendar.YEAR],
            gregorianCalendar[Calendar.MONTH],
            gregorianCalendar[Calendar.DATE]
        )
        datePicker.setButton(
            DatePickerDialog.BUTTON_POSITIVE,
            ctx.getString(R.string.select), datePicker
        )
        datePicker.setButton(
            DatePickerDialog.BUTTON_NEGATIVE,
            ctx.getString(R.string.cancel), datePicker
        )
        datePicker.show()
    }

    fun pickHijri() {
        _uiState.update { it.copy(
            hijriDatePickerShown = true
        )}
    }

    private fun gregorianToHijri(gregorian: Calendar): Calendar {
        val hijri = UmmalquraCalendar()
        hijri.time = gregorian.time
        return hijri
    }

    private fun hijriToGregorian(hijri: Calendar): Calendar {
        val gregorian = Calendar.getInstance()
        gregorian.time = hijri.time
        return gregorian
    }

    private fun updateCalendars(hijri: Calendar, gregorian: Calendar) {
        hijriCalendar[Calendar.YEAR] = hijri[0]
        hijriCalendar[Calendar.MONTH] = hijri[1]
        hijriCalendar[Calendar.DATE] = hijri[2]

        gregorianCalendar[Calendar.YEAR] = gregorian[0]
        gregorianCalendar[Calendar.MONTH] = gregorian[1]
        gregorianCalendar[Calendar.DATE] = gregorian[2]
    }

    private fun display() {
        _uiState.update { it.copy(
            hijriValues = listOf(
                translateNums(
                    repository.pref, hijriCalendar[Calendar.YEAR].toString()
                ),
                hijriMonths[hijriCalendar[Calendar.MONTH]],
                translateNums(
                    repository.pref, hijriCalendar[Calendar.DATE].toString()
                )
            ),
            gregorianValues = listOf(
                translateNums(
                    repository.pref, gregorianCalendar[Calendar.YEAR].toString()
                ),
                gregorianMonths[gregorianCalendar[Calendar.MONTH]],
                translateNums(
                    repository.pref, gregorianCalendar[Calendar.DATE].toString()
                )
            )
        )}
    }

}