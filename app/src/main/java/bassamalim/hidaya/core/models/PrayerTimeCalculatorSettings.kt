package bassamalim.hidaya.core.models

import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import kotlinx.serialization.Serializable

@Serializable
data class PrayerTimeCalculatorSettings(
    val calculationMethod: PrayerTimeCalculationMethod = PrayerTimeCalculationMethod.MECCA,
    val juristicMethod: PrayerTimeJuristicMethod = PrayerTimeJuristicMethod.SHAFII,
    val highLatitudesAdjustmentMethod: HighLatitudesAdjustmentMethod = HighLatitudesAdjustmentMethod.NONE,
)
