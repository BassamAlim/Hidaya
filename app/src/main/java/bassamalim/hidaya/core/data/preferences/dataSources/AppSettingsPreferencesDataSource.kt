package bassamalim.hidaya.core.data.preferences.dataSources

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.AppSettingsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class AppSettingsPreferencesDataSource(
    private val dataStore: DataStore<AppSettingsPreferences>
) {

    val flow: Flow<AppSettingsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(AppSettingsPreferences())
            else throw exception
        }

    suspend fun update(update: (AppSettingsPreferences) -> AppSettingsPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

}