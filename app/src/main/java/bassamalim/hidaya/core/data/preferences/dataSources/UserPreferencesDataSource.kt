package bassamalim.hidaya.core.data.preferences.dataSources

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class UserPreferencesDataSource(
    private val dataStore: DataStore<UserPreferences>
) {

    val flow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(UserPreferences())
            else throw exception
        }

    suspend fun update(update: (UserPreferences) -> UserPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

}