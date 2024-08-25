package bassamalim.hidaya.features.prayers.prayerSettings.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.PID
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrayerSettingsDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val prayersRepo: PrayersRepository,
    private val notificationsRepo: NotificationsRepository
) {

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    suspend fun getNotificationType(pid: PID) = notificationsRepo.getNotificationType(pid).first()

    suspend fun getTimeOffset(pid: PID) = prayersRepo.getTimeOffset(pid).first()

    fun getPrayerName(pid: PID) = prayersRepo.getPrayerName(pid)

}