package bassamalim.hidaya.core.models

import bassamalim.hidaya.core.enums.HighLatAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimesCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimesJuristicMethod
import kotlinx.serialization.Serializable

@Serializable
data class PrayerTimesCalculatorSettings(
    val calculationMethod: PrayerTimesCalculationMethod = PrayerTimesCalculationMethod.MECCA,
    val juristicMethod: PrayerTimesJuristicMethod = PrayerTimesJuristicMethod.SHAFII,
    val highLatAdjustmentMethod: HighLatAdjustmentMethod = HighLatAdjustmentMethod.NONE,
)
