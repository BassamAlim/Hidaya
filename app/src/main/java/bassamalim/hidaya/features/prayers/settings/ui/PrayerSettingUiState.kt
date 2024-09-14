package bassamalim.hidaya.features.prayers.settings.ui

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID

data class PrayerSettingUiState(
    val pid: PID = PID.FAJR,
    val prayerName: String = "",
    val notificationType: NotificationType = NotificationType.NOTIFICATION,
    val timeOffset: Int = 0
)