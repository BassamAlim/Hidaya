package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.database.daos.SurasDao
import bassamalim.hidaya.core.data.database.daos.VersesDao
import bassamalim.hidaya.core.data.preferences.dataSources.QuranPreferencesDataSource
import bassamalim.hidaya.core.enums.VerseRepeat
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.QuranPageBookmark
import bassamalim.hidaya.features.quranReader.ui.QuranViewType
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuranRepository @Inject constructor(
    private val quranPreferencesDataSource: QuranPreferencesDataSource,
    private val surasDao: SurasDao,
    private val versesDao: VersesDao
) {

    fun observeAllSuar() = surasDao.observeAll()

    fun getSuraNames(language: Language) =
        if (language == Language.ENGLISH) surasDao.getDecoratedNamesEn()
        else surasDao.getDecoratedNamesAr()

    fun getSuraFavorites() = surasDao.observeIsFavorites()

    suspend fun setSuraFavorites(suraId: Int, fav: Int) {
        surasDao.setIsFavorite(suraId, fav)
        setBackupSuraFavorites(
            surasDao.observeIsFavorites().first().mapIndexed { index, value ->
                index + 1 to value
            }.toMap()
        )
    }
    
    fun getBackupSuraFavorites() = quranPreferencesDataSource.flow.map {
        it.suraFavorites.toMap()
    }

    private suspend fun setBackupSuraFavorites(suraFavorites: Map<Int, Int>) {
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
        it.verseRepeat
    }

    suspend fun setAyaRepeat(verseRepeat: VerseRepeat) {
        quranPreferencesDataSource.update { it.copy(
            verseRepeat = verseRepeat
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

    fun getPageBookmark() = quranPreferencesDataSource.flow.map {
        it.pageBookmark
    }

    suspend fun setPageBookmark(bookmark: QuranPageBookmark) {
        quranPreferencesDataSource.update { it.copy(
            pageBookmark = bookmark
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

    fun getSuraPageNum(suraId: Int) = surasDao.getSuraStartPage(suraId)

    fun getAyaPageNum(ayaId: Int) = versesDao.getAyaPageNum(ayaId)

    fun getAyas() = versesDao.getAll()

}