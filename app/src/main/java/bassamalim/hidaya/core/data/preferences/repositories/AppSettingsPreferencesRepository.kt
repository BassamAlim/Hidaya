package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.AppSettingsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class AppSettingsPreferencesRepository(
    private val dataStore: DataStore<AppSettingsPreferences>
) {

    val flow: Flow<AppSettingsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(AppSettingsPreferences())
            else throw exception
        }

}