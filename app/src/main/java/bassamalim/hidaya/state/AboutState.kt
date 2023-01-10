package bassamalim.hidaya.state

data class AboutState(
    val isDevModeOn: Boolean = false,
    val isDatabaseRebuilt: Boolean = false,
    val lastDailyUpdate: String = ""
)