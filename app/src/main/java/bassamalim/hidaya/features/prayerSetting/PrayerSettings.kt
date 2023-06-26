package bassamalim.hidaya.features.prayerSetting

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID

data class PrayerSettings(
    val pid: PID,
    val notificationType: NotificationType,
    val timeOffset: Int,
    val reminderOffset: Int
)
