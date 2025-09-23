package bassamalim.hidaya.features.prayers.timeCalculationSettings.ui

import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod

data class PrayerTimeCalculationSettingsUiState(
    val continuousPrayersNotificationEnabled: Boolean = false,
    val calculationMethod: PrayerTimeCalculationMethod = PrayerTimeCalculationMethod.MECCA,
    val juristicMethod: PrayerTimeJuristicMethod = PrayerTimeJuristicMethod.SHAFII,
    val highLatitudesAdjustment: HighLatitudesAdjustmentMethod = HighLatitudesAdjustmentMethod.NONE,
)