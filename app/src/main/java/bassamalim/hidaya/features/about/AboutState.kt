package bassamalim.hidaya.features.about

import android.content.Intent

data class AboutState(
    val isDevModeOn: Boolean = false,
    val isDatabaseRebuilt: Boolean = false,
    val lastDailyUpdate: String = "",
    val shouldShowRebuilt: Int = 0,
    val quickUpdateIntent: Intent? = null
)