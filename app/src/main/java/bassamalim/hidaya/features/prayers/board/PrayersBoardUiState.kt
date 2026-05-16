package bassamalim.hidaya.features.prayers.board

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
    val reportDialogShown: Boolean = false,
    val reportStep: ReportStep = ReportStep.CHECKS,
    val reportCurrentMethodName: String = "",
    val reportIsAutoLocation: Boolean = false,
    val reportPrayerNames: Map<Prayer, String> = emptyMap(),
    val reportComputedTimes: Map<Prayer, String> = emptyMap(),
    val reportWrongPrayers: Set<Prayer> = emptySet(),
    val reportCorrectTimes: Map<Prayer, String> = emptyMap(),
    val reportNotes: String = "",
    val reportTimePickerTarget: Prayer? = null,
    val reportSubmitting: Boolean = false,
    val reportSubmitted: Boolean = false,
    val reportError: String? = null,
)
