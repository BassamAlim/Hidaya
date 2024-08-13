package bassamalim.hidaya.core.data.preferences.dataSources

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.SupplicationsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class RemembrancePreferencesDataSource(
    private val dataStore: DataStore<SupplicationsPreferences>
) {

    val flow: Flow<SupplicationsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(SupplicationsPreferences())
            else throw exception
        }

    suspend fun update(update: (SupplicationsPreferences) -> SupplicationsPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

}