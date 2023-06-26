package bassamalim.hidaya.features.prayers

data class PrayersState(
    val locationName: String = "",
    val prayersData: List<PrayerData> = emptyList(),
    val dateText: String = "",
    val settingsDialogShown: Boolean = false,
    val tutorialDialogShown: Boolean = false
)