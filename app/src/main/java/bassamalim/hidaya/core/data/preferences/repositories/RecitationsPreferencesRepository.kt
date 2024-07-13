package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.RecitationsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class RecitationsPreferencesRepository(
    private val dataStore: DataStore<RecitationsPreferences>
) {

    private val flow: Flow<RecitationsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(RecitationsPreferences())
            else throw exception
        }

    suspend fun update(update: (RecitationsPreferences) -> RecitationsPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

    fun getReciterFavorites() = flow.map { it.reciterFavorites }
    fun getNarrationSelections() = flow.map { it.narrationSelections }
    fun getRepeatMode() = flow.map { it.repeatMode }
    fun getShuffle() = flow.map { it.shuffleMode }
    fun getLastPlayedMediaId() = flow.map { it.lastPlayedMediaId }
    fun getLastProgress() = flow.map { it.lastProgress }

}