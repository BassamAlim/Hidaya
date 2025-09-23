package bassamalim.hidaya.core.data.dataSources.preferences.objects

import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import kotlinx.serialization.Serializable

@Serializable
data class PrayersPreferences(
    val continuousPrayersNotificationEnabled: Boolean = false,
    val prayerTimeCalculatorSettings: PrayerTimeCalculatorSettings = PrayerTimeCalculatorSettings(),
    val athanAudioId: Int = 1,
)