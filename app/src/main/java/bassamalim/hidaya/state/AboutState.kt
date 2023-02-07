package bassamalim.hidaya.state

import android.content.Intent

data class AboutState(
    val isDevModeOn: Boolean = false,
    val isDatabaseRebuilt: Boolean = false,
    val lastDailyUpdate: String = "",
    val shouldShowRebuiltToast: Boolean = false,
    val quickUpdateIntent: Intent? = null
)