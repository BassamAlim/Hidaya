package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(
    private val dataStore: DataStore<UserPreferences>
) {

    private val flow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(UserPreferences())
            else throw exception
        }

    suspend fun update(update: (UserPreferences) -> UserPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

    fun getLocation() = flow.map { it.location }
    fun getUserRecord() = flow.map { it.userRecord }

}