package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.BooksPreferencesDataSource
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val booksPreferencesDataSource: BooksPreferencesDataSource
) {

    fun getChapterFavorites() = booksPreferencesDataSource.flow.map {
        it.chapterFavorites.mapValues { entry ->
            entry.value.toMap()
        }.toMap()
    }
    suspend fun setChapterFavorites(chapterFavorites: Map<Int, Map<Int, Int>>) {
        booksPreferencesDataSource.update { it.copy(
            chapterFavorites = chapterFavorites.mapValues { entry ->
                entry.value.toPersistentMap()
            }.toPersistentMap()
        )}
    }

    fun getTextSize() = booksPreferencesDataSource.flow.map {
        it.textSize
    }
    suspend fun setTextSize(textSize: Float) {
        booksPreferencesDataSource.update { it.copy(
            textSize = textSize
        )}
    }

    fun getSearchSelections() = booksPreferencesDataSource.flow.map {
        it.searchSelections.toMap()
    }
    suspend fun setSearchSelections(searchSelections: Map<Int, Boolean>) {
        booksPreferencesDataSource.update { it.copy(
            searchSelections = searchSelections.toPersistentMap()
        )}
    }

    fun getSearchMaxMatches() = booksPreferencesDataSource.flow.map {
        it.searchMaxMatches
    }
    suspend fun setSearchMaxMatches(searchMaxMatches: Int) {
        booksPreferencesDataSource.update { it.copy(
            searchMaxMatches = searchMaxMatches
        )}
    }

    fun getShouldShowTutorial() = booksPreferencesDataSource.flow.map {
        it.shouldShowTutorial
    }
    suspend fun setShouldShowTutorial(shouldShowTutorial: Boolean) {
        booksPreferencesDataSource.update { it.copy(
            shouldShowTutorial = shouldShowTutorial
        )}
    }

}