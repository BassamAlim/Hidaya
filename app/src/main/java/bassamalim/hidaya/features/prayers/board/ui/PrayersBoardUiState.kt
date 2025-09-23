package bassamalim.hidaya.features.prayers.board.ui

import bassamalim.hidaya.core.enums.Prayer
import java.util.SortedMap

data class PrayersBoardUiState(
    val loading: Boolean = true,
    val locationAvailable: Boolean = false,
    val locationName: String = "",
    val prayersData: SortedMap<Prayer, PrayerCardData> = sortedMapOf(),
    val noDateOffset: Boolean = true,
    val dateText: String = "",
    val settingsDialogShown: Boolean = false,
    val timeCalculationSettingsDialogShown: Boolean = false,
)