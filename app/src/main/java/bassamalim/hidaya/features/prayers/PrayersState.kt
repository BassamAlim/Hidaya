package bassamalim.hidaya.features.prayers

import bassamalim.hidaya.core.enums.PID

data class PrayersState(
    val locationName: String = "",
    val prayersData: List<PrayerData> = emptyList(),
    val dateText: String = "",
    val settingsDialogShown: Boolean = false,
    val settingsDialogPID: PID = PID.FAJR,
    val tutorialDialogShown: Boolean = false
)