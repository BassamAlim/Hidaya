package bassamalim.hidaya.features.hijriDatePicker.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.enums.Language
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.Calendar
import javax.inject.Inject

class HijriDatePickerDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val appStateRepo: AppStateRepository
) {

    private val selectedDate = UmmalquraCalendar()
    private val currentDate = UmmalquraCalendar()
    val minYear = currentDate[Calendar.YEAR] - 100
    val maxYear = currentDate[Calendar.YEAR] + 100

    suspend fun getLanguage() = appSettingsRepo.getLanguage()

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage()

    fun getMonths() = appStateRepo.getMonths()

    fun getWeekDays() = appStateRepo.getWeekDays()

    fun getWeekDaysAbb(language: Language) = appStateRepo.getWeekDaysAbb(language)

    fun getSelectedDate() = selectedDate

    fun setSelectedDate(year: Int, month: Int, day: Int) {
        currentDate.set(year, month, day)
    }

    fun getCurrentDate() = currentDate

}