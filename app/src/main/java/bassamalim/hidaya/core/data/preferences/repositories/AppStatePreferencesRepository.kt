package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.AppStatePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AppStatePreferencesRepository(
    private val dataStore: DataStore<AppStatePreferences>
) {

    private val flow: Flow<AppStatePreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(AppStatePreferences())
            else throw exception
        }

    suspend fun update(update: (AppStatePreferences) -> AppStatePreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

    fun getIsOnboardingCompleted() = flow.map { it.isOnboardingCompleted }
    fun getLastDailyUpdateMillis() = flow.map { it.lastDailyUpdateMillis }
    fun getLastDbVersion() = flow.map { it.lastDBVersion }

}