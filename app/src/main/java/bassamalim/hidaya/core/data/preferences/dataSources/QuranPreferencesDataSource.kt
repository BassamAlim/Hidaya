package bassamalim.hidaya.core.data.preferences.dataSources

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.QuranPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class QuranPreferencesDataSource(
    private val dataStore: DataStore<QuranPreferences>
) {

    val flow: Flow<QuranPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(QuranPreferences())
            else throw exception
        }

    suspend fun update(update: (QuranPreferences) -> QuranPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

}