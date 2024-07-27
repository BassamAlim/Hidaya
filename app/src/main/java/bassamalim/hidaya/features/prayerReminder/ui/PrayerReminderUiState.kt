package bassamalim.hidaya.features.prayerReminder.ui

import bassamalim.hidaya.core.enums.PID

data class PrayerReminderUiState(
    val pid: PID,
    val prayerName: String,
    val offset: Int = 0
)