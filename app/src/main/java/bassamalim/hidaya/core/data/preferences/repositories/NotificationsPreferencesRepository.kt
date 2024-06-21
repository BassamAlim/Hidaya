package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.NotificationsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class NotificationsPreferencesRepository(
    private val dataStore: DataStore<NotificationsPreferences>
) {

    val flow: Flow<NotificationsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(NotificationsPreferences())
            else throw exception
        }

}