package bassamalim.hidaya.core.data.preferences.objects

import bassamalim.hidaya.core.enums.HighLatAdjustmentMethod
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.PTCalculationMethod
import bassamalim.hidaya.core.enums.PTJuristicMethod
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class PrayersPreferences(
    val athanVoiceId: Int = 1,
    val calculationMethod: PTCalculationMethod = PTCalculationMethod.MECCA,
    val juristicMethod: PTJuristicMethod = PTJuristicMethod.SHAFII,
    val highLatAdjustmentMethod: HighLatAdjustmentMethod = HighLatAdjustmentMethod.NONE,
    val shouldShowTutorial: Boolean = true,
    val timeOffsets: PersistentMap<PID, Int> = persistentMapOf(),
)