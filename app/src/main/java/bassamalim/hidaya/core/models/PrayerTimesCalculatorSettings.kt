package bassamalim.hidaya.core.models

import bassamalim.hidaya.core.enums.HighLatAdjustmentMethod
import bassamalim.hidaya.core.enums.PTCalculationMethod
import bassamalim.hidaya.core.enums.PTJuristicMethod
import kotlinx.serialization.Serializable

@Serializable
data class PrayerTimesCalculatorSettings(
    val calculationMethod: PTCalculationMethod = PTCalculationMethod.MECCA,
    val juristicMethod: PTJuristicMethod = PTJuristicMethod.SHAFII,
    val highLatAdjustmentMethod: HighLatAdjustmentMethod = HighLatAdjustmentMethod.NONE,
)
