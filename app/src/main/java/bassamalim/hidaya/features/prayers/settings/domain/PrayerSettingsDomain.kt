package bassamalim.hidaya.features.prayers.settings.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.PID
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrayerSettingsDomain @Inject constructor(
    private val prayersRepository: PrayersRepository,
    private val notificationsRepository: NotificationsRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    suspend fun getNotificationType(pid: PID) =
        notificationsRepository.getNotificationType(pid).first()

    suspend fun getTimeOffset(pid: PID) = prayersRepository.getTimeOffset(pid).first()

    fun getPrayerName(pid: PID) = prayersRepository.getPrayerName(pid)

}