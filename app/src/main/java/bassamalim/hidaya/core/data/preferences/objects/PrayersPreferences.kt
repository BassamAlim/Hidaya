package bassamalim.hidaya.core.data.preferences.objects

import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class PrayersPreferences(
    val prayerTimeCalculatorSettings: PrayerTimeCalculatorSettings =
        PrayerTimeCalculatorSettings(),
    val timeOffsets: PersistentMap<PID, Int> = persistentMapOf(),
    val athanAudioId: Int = 1,
    val shouldShowTutorial: Boolean = true,
)