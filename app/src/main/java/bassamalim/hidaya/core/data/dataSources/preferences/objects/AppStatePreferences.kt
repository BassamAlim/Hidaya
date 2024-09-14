package bassamalim.hidaya.core.data.dataSources.preferences.objects

import kotlinx.serialization.Serializable

@Serializable
data class AppStatePreferences(
    val isOnboardingCompleted: Boolean = false,
    val lastDailyUpdateMillis: Long = 0L,
    val lastDBVersion: Int = 1,
)