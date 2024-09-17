package bassamalim.hidaya.core.data.dataSources.preferences.objects

import bassamalim.hidaya.core.enums.PrayerTimePoint
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Serializable

@Serializable
data class PrayersPreferences(
    val prayerTimeCalculatorSettings: PrayerTimeCalculatorSettings = PrayerTimeCalculatorSettings(),
    val timeOffsets: PersistentMap<PrayerTimePoint, Int> =
        PrayerTimePoint.entries.associateWith { 0 }.toPersistentMap(),
    val athanAudioId: Int = 1,
    val shouldShowTutorial: Boolean = true,
)