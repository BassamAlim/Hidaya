package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import bassamalim.hidaya.core.models.Location

data class PrayerTimesReport(
    val language: Language,
    val location: Location?,
    val locationName: String,
    val calculatorSettings: PrayerTimeCalculatorSettings,
    val computedTimes: Map<Prayer, String>,
    val wrongPrayers: Set<Prayer>,
    val correctTimes: Map<Prayer, String>,
    val notes: String
)
