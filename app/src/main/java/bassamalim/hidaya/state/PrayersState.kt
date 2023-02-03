package bassamalim.hidaya.state

import bassamalim.hidaya.enums.PID

data class PrayersState(
    val locationName: String = "",
    val prayerTexts: List<String> = listOf("", "", "", "", "", ""),
    val notificationTypeIconIDs: List<Int> = emptyList(),
    val timeOffsetTexts: List<String> = emptyList(),
    val dateText: String = "",
    val dateOffset: Int = 0,
    val isSettingsDialogShown: Boolean = false,
    val settingsDialogPID: PID = PID.FAJR,
    val isTutorialDialogShown: Boolean = false,
)
