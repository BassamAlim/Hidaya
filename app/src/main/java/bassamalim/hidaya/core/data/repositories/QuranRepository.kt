package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.database.daos.SurasDao
import bassamalim.hidaya.core.data.database.daos.VersesDao
import bassamalim.hidaya.core.data.preferences.dataSources.QuranPreferencesDataSource
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.QuranPageBookmark
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuranRepository @Inject constructor(
    private val quranPreferencesDataSource: QuranPreferencesDataSource,
    private val surasDao: SurasDao,
    private val versesDao: VersesDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {

    fun observeAllSuras() = surasDao.observeAll()

    suspend fun getDecoratedSuraNames(language: Language) = withContext(dispatcher) {
        if (language == Language.ENGLISH) surasDao.getDecoratedNamesEn()
        else surasDao.getDecoratedNamesAr()
    }

    suspend fun getPlainSuraNames() = withContext(dispatcher) {
        surasDao.getPlainNamesAr()
    }

    fun getSuraFavorites() = surasDao.observeIsFavorites().map {
        it.mapIndexed { index, value -> index + 1 to (value == 1) }.toMap()
    }

    suspend fun setSuraFavoriteStatus(suraId: Int, isFavorite: Boolean) {
        withContext(dispatcher) {
            surasDao.setFavoriteStatus(suraId, if (isFavorite) 1 else 0)
        }
        setSuraFavoritesBackup(
            surasDao.observeIsFavorites().first().mapIndexed { index, value ->
                index + 1 to (value == 1)
            }.toMap()
        )
    }

    suspend fun setSuraFavorites(map: Map<Int, Boolean>) {
        withContext(dispatcher) {
            map.forEach { (suraId, isFavorite) ->
                surasDao.setFavoriteStatus(suraId, if (isFavorite) 1 else 0)
            }
        }
    }

    suspend fun getSuraPageNum(suraId: Int) = withContext(dispatcher) {
        surasDao.getSuraStartPage(suraId)
    }

    suspend fun getVersePageNum(verseId: Int) = withContext(dispatcher) {
        versesDao.getVersePageNum(verseId)
    }

    suspend fun getAllVerses() =  withContext(dispatcher) {
        versesDao.getAll()
    }
    
    fun getSuraFavoritesBackup() = quranPreferencesDataSource.flow.map {
        it.suraFavorites.toMap()
    }

    private suspend fun setSuraFavoritesBackup(suraFavorites: Map<Int, Boolean>) {
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

    fun getPageBookmark() = quranPreferencesDataSource.flow.map {
        it.pageBookmark
    }

    suspend fun setPageBookmark(bookmark: QuranPageBookmark?) {
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

    fun isWerdDone() = quranPreferencesDataSource.flow.map {
        it.isWerdDone
    }

    suspend fun setWerdDone(isWerdDone: Boolean) {
        quranPreferencesDataSource.update { it.copy(
            isWerdDone = isWerdDone
        )}
    }

}