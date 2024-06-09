package bassamalim.hidaya.features.about

data class AboutState(
    val isDevModeOn: Boolean = false,
    val isDatabaseRebuilt: Boolean = false,
    val lastDailyUpdate: String = "",
    val shouldShowRebuilt: Int = 0
)