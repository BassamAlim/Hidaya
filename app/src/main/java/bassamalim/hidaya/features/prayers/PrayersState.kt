package bassamalim.hidaya.features.prayers

data class PrayersState(
    val locationName: String = "",
    val prayersData: List<PrayerData> = emptyList(),
    val dateText: String = "",
    val tutorialDialogShown: Boolean = false
)