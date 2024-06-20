package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.AppStatePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class AppStatePreferencesRepository(
    private val dataStore: DataStore<AppStatePreferences>
) {

    val appStatePreferencesFlow: Flow<AppStatePreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(AppStatePreferences())
            else throw exception
        }

}