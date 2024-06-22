package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.PrayersPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class PrayersPreferencesRepository(
    private val dataStore: DataStore<PrayersPreferences>
) {

    val flow: Flow<PrayersPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(PrayersPreferences())
            else throw exception
        }

    suspend fun update(update: (PrayersPreferences) -> PrayersPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

}