package bassamalim.hidaya.features.prayers.prayerReminder.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.PID
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrayerReminderDomain @Inject constructor(
    private val prayersRepository: PrayersRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    val offsetMin = 30f

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    suspend fun getOffset(pid: PID) = prayersRepository.getTimeOffset(pid).first()

    fun getPrayerName(pid: PID) = prayersRepository.getPrayerName(pid)

}