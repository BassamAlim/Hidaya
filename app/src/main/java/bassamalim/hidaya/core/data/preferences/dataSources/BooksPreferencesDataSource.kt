package bassamalim.hidaya.core.data.preferences.dataSources

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.BooksPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class BooksPreferencesDataSource(
    private val dataStore: DataStore<BooksPreferences>
) {

    val flow: Flow<BooksPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(BooksPreferences())
            else throw exception
        }

    suspend fun update(update: (BooksPreferences) -> BooksPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

}