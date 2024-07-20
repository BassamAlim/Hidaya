package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.BooksPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class BooksPreferencesRepository(
    private val dataStore: DataStore<BooksPreferences>
) {

    private val flow: Flow<BooksPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(BooksPreferences())
            else throw exception
        }

    suspend fun update(update: (BooksPreferences) -> BooksPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

    fun getChapterFavorites() = flow.map { it.chapterFavorites }
    fun getTextSize() = flow.map { it.textSize }
    fun getSearchSelections() = flow.map { it.searchSelections }
    fun getSearchMaxMatches() = flow.map { it.searchMaxMatches }
    fun getShouldShowTutorial() = flow.map { it.shouldShowTutorial }

}