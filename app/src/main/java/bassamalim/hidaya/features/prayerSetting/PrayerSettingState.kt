package bassamalim.hidaya.features.prayerSetting

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID

data class PrayerSettingState(
    val pid: PID,
    val prayerName: String,
    val notificationType: NotificationType,
    val timeOffset: Int
)