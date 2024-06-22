package bassamalim.hidaya.features.about.ui

data class AboutUiState(
    val isDevModeEnabled: Boolean = false,
    val isDatabaseRebuilt: Boolean = false,
    val lastDailyUpdate: String = "",
    val shouldShowRebuilt: Int = 0
)