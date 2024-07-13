package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.QuranPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class QuranPreferencesRepository(
    private val dataStore: DataStore<QuranPreferences>
) {

    private val flow: Flow<QuranPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(QuranPreferences())
            else throw exception
        }

    suspend fun update(update: (QuranPreferences) -> QuranPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

    fun getSuraFavorites() = flow.map { it.suraFavorites }
    fun getViewType() = flow.map { it.viewType }
    fun getTextSize() = flow.map { it.textSize }
    fun getAyaReciterId() = flow.map { it.ayaReciterId }
    fun getAyaRepeat() = flow.map { it.ayaRepeat }
    fun getShouldStopOnSuraEnd() = flow.map { it.shouldStopOnSuraEnd }
    fun getShouldStopOnPageEnd() = flow.map { it.shouldStopOnPageEnd }
    fun getBookmarkedPage() = flow.map { it.bookmarkedPage }
    fun getBookmarkedSura() = flow.map { it.bookmarkedSura }
    fun getSearchMaxMatches() = flow.map { it.searchMaxMatches }
    fun getShouldShowMenuTutorial() = flow.map { it.shouldShowMenuTutorial }
    fun getShouldShowReaderTutorial() = flow.map { it.shouldShowReaderTutorial }
    fun getWerdPage() = flow.map { it.werdPage }
    fun getIsWerdDone() = flow.map { it.isWerdDone }

}