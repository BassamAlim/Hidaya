package bassamalim.hidaya.features.prayers.settings.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrayerSettingsDomain @Inject constructor(
    private val prayersRepository: PrayersRepository,
    private val notificationsRepository: NotificationsRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    suspend fun getNotificationType(prayer: Prayer) =
        notificationsRepository.getNotificationType(prayer.toReminder()).first()

    fun setNotificationType(type: NotificationType, prayer: Prayer) =
        notificationsRepository.setNotificationType(type, prayer.toReminder())

    fun getPrayerName(prayer: Prayer) = prayersRepository.getPrayerName(prayer)

}