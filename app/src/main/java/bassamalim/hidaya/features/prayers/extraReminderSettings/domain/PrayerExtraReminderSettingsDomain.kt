package bassamalim.hidaya.features.prayers.extraReminderSettings.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.Prayer
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrayerExtraReminderSettingsDomain @Inject constructor(
    private val prayersRepository: PrayersRepository,
    private val notificationsRepository: NotificationsRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    val offsetMin = 30f

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

    suspend fun getOffset(prayer: Prayer) =
        notificationsRepository.getPrayerExtraReminderTimeOffsets()
            .first()[prayer.toExtraReminder()]!!

    fun getPrayerName(prayer: Prayer) = prayersRepository.getPrayerName(prayer)

}