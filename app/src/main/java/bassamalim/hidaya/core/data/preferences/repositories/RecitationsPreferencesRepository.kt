package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.RecitationsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class RecitationsPreferencesRepository(
    private val dataStore: DataStore<RecitationsPreferences>
) {

    val flow: Flow<RecitationsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(RecitationsPreferences())
            else throw exception
        }

    suspend fun update(update: (RecitationsPreferences) -> RecitationsPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

}