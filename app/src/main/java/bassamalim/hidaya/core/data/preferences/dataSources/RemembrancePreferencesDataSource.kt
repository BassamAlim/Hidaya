package bassamalim.hidaya.core.data.preferences.dataSources

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.RemembrancesPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class RemembrancePreferencesDataSource(
    private val dataStore: DataStore<RemembrancesPreferences>
) {

    val flow: Flow<RemembrancesPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(RemembrancesPreferences())
            else throw exception
        }

    suspend fun update(update: (RemembrancesPreferences) -> RemembrancesPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

}