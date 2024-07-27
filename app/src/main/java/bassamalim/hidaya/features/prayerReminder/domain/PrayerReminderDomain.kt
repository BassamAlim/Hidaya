package bassamalim.hidaya.features.prayerReminder.domain

import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.features.prayerReminder.data.PrayerReminderRepository
import javax.inject.Inject

class PrayerReminderDomain @Inject constructor(
    private val repository: PrayerReminderRepository
) {

    val offsetMin = 30f

    suspend fun getNumeralsLanguage() = repository.numeralsLanguage()

    suspend fun getOffset(pid: PID) = repository.getOffset(pid)

    fun getPrayerName(pid: PID) = repository.getPrayerName(pid)

}