package bassamalim.hidaya.features.prayers.prayerReminder.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.PID
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrayerReminderDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val prayersRepo: PrayersRepository
) {

    val offsetMin = 30f

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    suspend fun getOffset(pid: PID) = prayersRepo.getTimeOffset(pid).first()

    fun getPrayerName(pid: PID) = prayersRepo.getPrayerName(pid)

}