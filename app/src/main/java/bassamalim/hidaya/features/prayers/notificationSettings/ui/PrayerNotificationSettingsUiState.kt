package bassamalim.hidaya.features.prayers.notificationSettings.ui

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer

data class PrayerNotificationSettingsUiState(
    val prayer: Prayer = Prayer.FAJR,
    val prayerName: String = "",
    val notificationType: NotificationType = NotificationType.NOTIFICATION,
)