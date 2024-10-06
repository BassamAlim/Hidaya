package bassamalim.hidaya.features.prayers.board.ui

import bassamalim.hidaya.core.enums.Prayer
import java.util.SortedMap

data class PrayersBoardUiState(
    val isLoading: Boolean = true,
    val isLocationAvailable: Boolean = false,
    val locationName: String = "",
    val prayersData: SortedMap<Prayer, PrayerCardData> = sortedMapOf(),
    val isNoDateOffset: Boolean = true,
    val dateText: String = "",
    val settingsDialogShown: Boolean = false,
    val isTutorialDialogShown: Boolean = false,
    val shouldShowLocationFailedToast: Boolean = false
)