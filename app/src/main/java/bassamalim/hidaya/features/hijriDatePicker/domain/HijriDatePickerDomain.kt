package bassamalim.hidaya.features.hijriDatePicker.domain

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.hijriDatePicker.data.HijriDatePickerRepository
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.Calendar
import javax.inject.Inject

class HijriDatePickerDomain @Inject constructor(
    private val repository: HijriDatePickerRepository
) {

    private val selectedDate = UmmalquraCalendar()
    private val currentDate = UmmalquraCalendar()
    val minYear = currentDate[Calendar.YEAR] - 100
    val maxYear = currentDate[Calendar.YEAR] + 100

    suspend fun getLanguage() = repository.getLanguage()

    suspend fun getNumeralsLanguage() = repository.getNumeralsLanguage()

    fun getMonths() = repository.getMonths()

    fun getWeekDays() = repository.getWeekDays()

    fun getWeekDaysAbb(language: Language) = repository.getWeekDaysAbb(language)

    fun getSelectedDate() = selectedDate

    fun setSelectedDate(year: Int, month: Int, day: Int) {
        currentDate.set(year, month, day)
    }

    fun getCurrentDate() = currentDate

}