package bassamalim.hidaya.state

import bassamalim.hidaya.enums.NotificationType
import bassamalim.hidaya.enums.PID

data class PrayersState(
    val locationName: String = "",
    val prayerTexts: List<String> = listOf("", "", "", "", "", ""),
    val notificationTypes: List<NotificationType> = emptyList(),
    val timeOffsets: List<Int> = emptyList(),
    val dateText: String = "",
    val isSettingsDialogShown: Boolean = false,
    val settingsDialogPID: PID = PID.FAJR,
    val isTutorialDialogShown: Boolean = false,
)
