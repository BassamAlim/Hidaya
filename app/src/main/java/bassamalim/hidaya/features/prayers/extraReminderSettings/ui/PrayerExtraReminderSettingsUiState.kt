package bassamalim.hidaya.features.prayers.extraReminderSettings.ui

import bassamalim.hidaya.core.enums.Prayer

data class PrayerExtraReminderSettingsUiState(
    val prayer: Prayer = Prayer.FAJR,
    val prayerName: String = "",
    val offset: Int = 0
)