package bassamalim.hidaya.features.main.domain

import bassamalim.hidaya.features.main.data.MainRepository
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.Calendar
import javax.inject.Inject

class MainDomain @Inject constructor(
    private val repository: MainRepository
) {

    private val millisInDay = 1000 * 60 * 60 * 24

    fun getDateOffset() = repository.getDateOffset()

    fun getHijriDateCalendar(dateOffset: Int) =
        UmmalquraCalendar().apply {
            timeInMillis += dateOffset * millisInDay
        }

    fun getGregorianDateCalendar(): Calendar = Calendar.getInstance()

    suspend fun getNumeralsLanguage() = repository.getNumeralsLanguage()

    fun getWeekDays() = repository.getWeekDays()

    fun getHijriMonths() = repository.getHijriMonths()

    fun getGregorianMonths() = repository.getGregorianMonths()

}