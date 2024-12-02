package bassamalim.hidaya.features.prayers.settings.ui

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer

data class PrayerSettingsUiState(
    val prayer: Prayer = Prayer.FAJR,
    val prayerName: String = "",
    val notificationType: NotificationType = NotificationType.NOTIFICATION,
)