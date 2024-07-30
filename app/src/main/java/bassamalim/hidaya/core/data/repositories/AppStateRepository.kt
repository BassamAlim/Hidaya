package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.AppStatePreferencesDataSource
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppStateRepository @Inject constructor(
    private val appStatePreferencesDataSource: AppStatePreferencesDataSource
) {

    fun getIsOnboardingCompleted() = appStatePreferencesDataSource.flow.map {
        it.isOnboardingCompleted
    }
    suspend fun setIsOnboardingCompleted(isCompleted: Boolean) {
        appStatePreferencesDataSource.update { it.copy(
            isOnboardingCompleted = isCompleted
        )}
    }

    fun getLastDailyUpdateMillis() = appStatePreferencesDataSource.flow.map {
        it.lastDailyUpdateMillis
    }
    suspend fun setLastDailyUpdateMillis(millis: Long) {
        appStatePreferencesDataSource.update { it.copy(
            lastDailyUpdateMillis = millis
        )}
    }

    fun getLastDbVersion() = appStatePreferencesDataSource.flow.map {
        it.lastDBVersion
    }
    suspend fun setLastDbVersion(version: Int) {
        appStatePreferencesDataSource.update { it.copy(
            lastDBVersion = version
        )}
    }

}