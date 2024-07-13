package bassamalim.hidaya.core.data.preferences.objects

import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.PrayerTimesCalculatorSettings
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class PrayersPreferences(
    val prayerTimesCalculatorSettings: PrayerTimesCalculatorSettings =
        PrayerTimesCalculatorSettings(),
    val timeOffsets: PersistentMap<PID, Int> = persistentMapOf(),
    val athanVoiceId: Int = 1,
    val shouldShowTutorial: Boolean = true,
)