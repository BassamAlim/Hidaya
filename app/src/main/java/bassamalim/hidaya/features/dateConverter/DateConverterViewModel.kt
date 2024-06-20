package bassamalim.hidaya.features.dateConverter

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.widget.DatePicker
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DateConverterViewModel @Inject constructor(
    private val repo: DateConverterRepository,
    private val navigator: Navigator
): ViewModel() {

    var hijriCalendar = UmmalquraCalendar()
    private var gregorianCalendar = Calendar.getInstance()
    private val hijriMonths = repo.getHijriMonths()
    private val gregorianMonths = repo.getGregorianMonths()

    private val _uiState = MutableStateFlow(DateConverterState())
    val uiState = _uiState.asStateFlow()

    fun onPickGregorianClk(ctx: Context) {
        val datePicker = DatePickerDialog(
            ctx, { _: DatePicker?, year: Int, month: Int, day: Int ->
                val choice = Calendar.getInstance()
                choice[Calendar.YEAR] = year
                choice[Calendar.MONTH] = month // starts from 0
                choice[Calendar.DATE] = day

                gregorianCalendar = choice
                hijriCalendar = gregorianToHijri(choice) as UmmalquraCalendar

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

    fun onPickHijriClk() {
        val dateStr = "${hijriCalendar[Calendar.YEAR]}" +
                "-${hijriCalendar[Calendar.MONTH] + 1}" +
                "-${hijriCalendar[Calendar.DATE]}"

        navigator.navigateForResult(
            Screen.HijriDatePicker(dateStr)
        ) { result ->
            if (result != null) {
                val date =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        result.getSerializable("selected_date", UmmalquraCalendar::class.java)
                    else
                        result.getSerializable("selected_date") as UmmalquraCalendar

                onHijriSelected(date!!)
            }
        }
    }

    fun onHijriSelected(pickedHijriDate: UmmalquraCalendar) {
        hijriCalendar = pickedHijriDate
        gregorianCalendar = hijriToGregorian(pickedHijriDate)

        display()
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

    private fun display() {
        _uiState.update { it.copy(
            hijriValues = listOf(
                translateNums(
                    repo.numeralsLanguage,
                    hijriCalendar[Calendar.YEAR].toString()
                ),
                hijriMonths[hijriCalendar[Calendar.MONTH]],
                translateNums(
                    repo.numeralsLanguage,
                    hijriCalendar[Calendar.DATE].toString()
                )
            ),
            gregorianValues = listOf(
                translateNums(
                    repo.numeralsLanguage,
                    gregorianCalendar[Calendar.YEAR].toString()
                ),
                gregorianMonths[gregorianCalendar[Calendar.MONTH]],
                translateNums(
                    repo.numeralsLanguage,
                    gregorianCalendar[Calendar.DATE].toString()
                )
            )
        )}
    }

}