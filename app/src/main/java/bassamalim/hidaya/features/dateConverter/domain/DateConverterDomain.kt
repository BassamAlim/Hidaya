package bassamalim.hidaya.features.dateConverter.domain

import bassamalim.hidaya.features.dateConverter.data.DateConverterRepository
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.Calendar
import javax.inject.Inject

class DateConverterDomain @Inject constructor(
    private val repository: DateConverterRepository
) {

    suspend fun getNumeralsLanguage() = repository.getNumeralsLanguage()

    fun getHijriMonths() = repository.getHijriMonths()

    fun getGregorianMonths() = repository.getGregorianMonths()

    fun gregorianToHijri(gregorian: Calendar): Calendar {
        val hijri = UmmalquraCalendar()
        hijri.time = gregorian.time
        return hijri
    }

    fun hijriToGregorian(hijri: Calendar): Calendar {
        val gregorian = Calendar.getInstance()
        gregorian.time = hijri.time
        return gregorian
    }

}