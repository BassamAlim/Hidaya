package bassamalim.hidaya.features.prayers.board.ui

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID

data class PrayerCardData(
    val text: String,
    val pid: PID,
    val notificationType: NotificationType,
    val isTimeOffsetSpecified: Boolean,
    val timeOffset: String,
    val isReminderOffsetSpecified: Boolean,
    val reminderOffset: String
)
