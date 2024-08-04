package bassamalim.hidaya.features.main.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class MainDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val appStateRepository: AppStateRepository
) {

    private val millisInDay = 1000 * 60 * 60 * 24

    fun getDateOffset() = appSettingsRepo.getDateOffset()

    fun getHijriDateCalendar(dateOffset: Int) =
        UmmalquraCalendar().apply {
            timeInMillis += dateOffset * millisInDay
        }

    fun getGregorianDateCalendar(): Calendar = Calendar.getInstance()

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getWeekDays() = appStateRepository.getWeekDays()

    fun getHijriMonths() = appStateRepository.getHijriMonths()

    fun getGregorianMonths() = appStateRepository.getGregorianMonths()

}