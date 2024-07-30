package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.QuranPreferencesDataSource
import bassamalim.hidaya.core.enums.AyaRepeat
import bassamalim.hidaya.core.enums.QuranViewType
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuranRepository @Inject constructor(
    private val quranPreferencesDataSource: QuranPreferencesDataSource
) {

    fun getSuraFavorites() = quranPreferencesDataSource.flow.map {
        it.suraFavorites.toMap()
    }
    suspend fun setSuraFavorites(suraFavorites: Map<Int, Int>) {
        quranPreferencesDataSource.update { it.copy(
            suraFavorites = suraFavorites.toPersistentMap()
        )}
    }

    fun getViewType() = quranPreferencesDataSource.flow.map {
        it.viewType
    }
    suspend fun setViewType(viewType: QuranViewType) {
        quranPreferencesDataSource.update { it.copy(
            viewType = viewType
        )}
    }

    fun getTextSize() = quranPreferencesDataSource.flow.map {
        it.textSize
    }
    suspend fun setTextSize(textSize: Float) {
        quranPreferencesDataSource.update { it.copy(
            textSize = textSize
        )}
    }

    fun getAyaReciterId() = quranPreferencesDataSource.flow.map {
        it.ayaReciterId
    }
    suspend fun setAyaReciterId(ayaReciterId: Int) {
        quranPreferencesDataSource.update { it.copy(
            ayaReciterId = ayaReciterId
        )}
    }

    fun getAyaRepeat() = quranPreferencesDataSource.flow.map {
        it.ayaRepeat
    }
    suspend fun setAyaRepeat(ayaRepeat: AyaRepeat) {
        quranPreferencesDataSource.update { it.copy(
            ayaRepeat = ayaRepeat
        )}
    }

    fun getShouldStopOnSuraEnd() = quranPreferencesDataSource.flow.map {
        it.shouldStopOnSuraEnd
    }
    suspend fun setShouldStopOnSuraEnd(shouldStopOnSuraEnd: Boolean) {
        quranPreferencesDataSource.update { it.copy(
            shouldStopOnSuraEnd = shouldStopOnSuraEnd
        )}
    }


    fun getShouldStopOnPageEnd() = quranPreferencesDataSource.flow.map {
        it.shouldStopOnPageEnd
    }
    suspend fun setShouldStopOnPageEnd(shouldStopOnPageEnd: Boolean) {
        quranPreferencesDataSource.update { it.copy(
            shouldStopOnPageEnd = shouldStopOnPageEnd
        )}
    }

    fun getBookmarkedPage() = quranPreferencesDataSource.flow.map {
        it.bookmarkedPage
    }
    suspend fun setBookmarkedPage(bookmarkedPage: Int) {
        quranPreferencesDataSource.update { it.copy(
            bookmarkedPage = bookmarkedPage
        )}
    }

    fun getBookmarkedSura() = quranPreferencesDataSource.flow.map {
        it.bookmarkedSura
    }
    suspend fun setBookmarkedSura(bookmarkedSura: Int) {
        quranPreferencesDataSource.update { it.copy(
            bookmarkedSura = bookmarkedSura
        )}
    }

    fun getSearchMaxMatches() = quranPreferencesDataSource.flow.map {
        it.searchMaxMatches
    }
    suspend fun setSearchMaxMatches(searchMaxMatches: Int) {
        quranPreferencesDataSource.update { it.copy(
            searchMaxMatches = searchMaxMatches
        )}
    }

    fun getShouldShowMenuTutorial() = quranPreferencesDataSource.flow.map {
        it.shouldShowMenuTutorial
    }
    suspend fun setShouldShowMenuTutorial(shouldShowMenuTutorial: Boolean) {
        quranPreferencesDataSource.update { it.copy(
            shouldShowMenuTutorial = shouldShowMenuTutorial
        )}
    }

    fun getShouldShowReaderTutorial() = quranPreferencesDataSource.flow.map {
        it.shouldShowReaderTutorial
    }
    suspend fun setShouldShowReaderTutorial(shouldShowReaderTutorial: Boolean) {
        quranPreferencesDataSource.update { it.copy(
            shouldShowReaderTutorial = shouldShowReaderTutorial
        )}
    }

    fun getWerdPage() = quranPreferencesDataSource.flow.map {
        it.werdPage
    }
    suspend fun setWerdPage(werdPage: Int) {
        quranPreferencesDataSource.update { it.copy(
            werdPage = werdPage
        )}
    }

    fun getIsWerdDone() = quranPreferencesDataSource.flow.map {
        it.isWerdDone
    }
    suspend fun setIsWerdDone(isWerdDone: Boolean) {
        quranPreferencesDataSource.update { it.copy(
            isWerdDone = isWerdDone
        )}
    }

}