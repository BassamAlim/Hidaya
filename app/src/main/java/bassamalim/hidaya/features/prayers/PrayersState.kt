package bassamalim.hidaya.features.prayers

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID

data class PrayersState(
    val locationName: String = "",
    val prayerTexts: List<String> = listOf("", "", "", "", "", ""),
    val notificationTypes: List<NotificationType> = emptyList(),
    val timeOffsets: List<Int> = emptyList(),
    val dateText: String = "",
    val settingsDialogShown: Boolean = false,
    val settingsDialogPID: PID = PID.FAJR,
    val tutorialDialogShown: Boolean = false
)