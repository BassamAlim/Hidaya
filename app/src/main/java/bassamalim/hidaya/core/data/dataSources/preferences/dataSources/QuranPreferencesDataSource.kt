package bassamalim.hidaya.core.data.dataSources.preferences.dataSources

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.dataSources.preferences.objects.QuranPreferences
import bassamalim.hidaya.core.models.QuranPageBookmark
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import kotlinx.collections.immutable.PersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class QuranPreferencesDataSource(
    private val dataStore: DataStore<QuranPreferences>
) {

    private val flow: Flow<QuranPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(QuranPreferences())
            else throw exception
        }

    fun getSuraFavorites() = flow.map { it.suraFavorites }
    suspend fun updateSuraFavorites(favorites: PersistentMap<Int, Boolean>) {
        dataStore.updateData { preferences ->
            preferences.copy(suraFavorites = favorites)
        }
    }

    fun getViewType() = flow.map { it.viewType }
    suspend fun updateViewType(viewType: QuranViewType) {
        dataStore.updateData { preferences ->
            preferences.copy(viewType = viewType)
        }
    }

    fun getFillPage() = flow.map { it.fillPage }
    suspend fun updateFillPage(fillPage: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(fillPage = fillPage)
        }
    }

    fun getTextSize() = flow.map { it.textSize }
    suspend fun updateTextSize(textSize: Float) {
        dataStore.updateData { preferences ->
            preferences.copy(textSize = textSize)
        }
    }

    fun getPageBookmark() = flow.map { it.pageBookmark }
    suspend fun updatePageBookmark(page: QuranPageBookmark?) {
        dataStore.updateData { preferences ->
            preferences.copy(pageBookmark = page)
        }
    }

    fun getSearchMaxMatches() = flow.map { it.searchMaxMatches }
    suspend fun updateSearchMaxMatches(maxMatches: Int) {
        dataStore.updateData { preferences ->
            preferences.copy(searchMaxMatches = maxMatches)
        }
    }

    fun getShouldShowMenuTutorial() = flow.map { it.shouldShowMenuTutorial }
    suspend fun updateShouldShowMenuTutorial(shouldShow: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(shouldShowMenuTutorial = shouldShow)
        }
    }

    fun getShouldShowReaderTutorial() = flow.map { it.shouldShowReaderTutorial }
    suspend fun updateShouldShowReaderTutorial(shouldShow: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(shouldShowReaderTutorial = shouldShow)
        }
    }

    fun getWerdPageNum() = flow.map { it.werdPageNum }
    suspend fun updateWerdPageNum(num: Int) {
        dataStore.updateData { preferences ->
            preferences.copy(werdPageNum = num)
        }
    }

    fun getWerdDone() = flow.map { it.isWerdDone }
    suspend fun updateWerdDone(isDone: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(isWerdDone = isDone)
        }
    }

}