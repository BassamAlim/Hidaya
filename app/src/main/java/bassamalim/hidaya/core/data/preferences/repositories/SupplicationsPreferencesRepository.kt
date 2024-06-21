package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.SupplicationsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class SupplicationsPreferencesRepository(
    private val dataStore: DataStore<SupplicationsPreferences>
) {

    val flow: Flow<SupplicationsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(SupplicationsPreferences())
            else throw exception
        }

}