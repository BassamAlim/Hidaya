package bassamalim.hidaya.features.prayers.board.ui

import bassamalim.hidaya.core.enums.NotificationType

data class PrayerCardData(
    val text: String,
    val notificationType: NotificationType,
    val isExtraReminderOffsetSpecified: Boolean,
    val extraReminderOffset: String
)
