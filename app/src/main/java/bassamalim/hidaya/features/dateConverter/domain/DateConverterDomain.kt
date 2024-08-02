package bassamalim.hidaya.features.dateConverter.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class DateConverterDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val appStateRepo: AppStateRepository
) {

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getHijriMonths() = appStateRepo.getHijriMonths()

    fun getGregorianMonths() = appStateRepo.getGregorianMonths()

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