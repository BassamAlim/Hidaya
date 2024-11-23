package bassamalim.hidaya.features.about.ui

import bassamalim.hidaya.core.models.Source

data class AboutUiState(
    val sources: List<Source> = emptyList(),
    val isDevModeEnabled: Boolean = false,
    val isDatabaseRebuilt: Boolean = false,
    val lastDailyUpdate: String = "",
    val shouldShowRebuilt: Int = 0
)