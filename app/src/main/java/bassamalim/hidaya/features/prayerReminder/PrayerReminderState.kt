package bassamalim.hidaya.features.prayerReminder

import bassamalim.hidaya.core.enums.PID

data class PrayerReminderState(
    val pid: PID,
    val prayerName: String,
    val offset: Int = 0
)