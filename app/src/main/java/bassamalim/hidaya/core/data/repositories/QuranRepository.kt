package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.QuranPreferencesDataSource
import bassamalim.hidaya.core.data.dataSources.room.daos.SurasDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VersesDao
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuranRepository @Inject constructor(
    private val quranPreferencesDataSource: QuranPreferencesDataSource,
    private val surasDao: SurasDao,
    private val versesDao: VersesDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) {

    fun observeAllSuras(language: Language) = surasDao.observeAll().map {
        it.map { sura ->
            Sura(
                id = sura.id,
                decoratedName = when (language) {
                    Language.ARABIC -> sura.decoratedNameAr
                    Language.ENGLISH -> sura.decoratedNameEn
                },
                plainName = when (language) {
                    Language.ARABIC -> sura.plainNameAr
                    Language.ENGLISH -> sura.plainNameEn?: sura.plainNameAr
                },
                revelation = sura.revelation,
                isFavorite = sura.isFavorite == 1
            )
        }
    }

    suspend fun getDecoratedSuraNames(language: Language) = withContext(dispatcher) {
        when (language) {
            Language.ARABIC -> surasDao.getDecoratedNamesAr()
            Language.ENGLISH -> surasDao.getDecoratedNamesEn()
        }
    }

    suspend fun getPlainSuraNames() = withContext(dispatcher) {
        surasDao.getPlainNamesAr()
    }

    fun setSuraFavoriteStatus(suraId: Int, isFavorite: Boolean) {
        scope.launch {
            withContext(dispatcher) {
                surasDao.setFavoriteStatus(suraId, if (isFavorite) 1 else 0)
            }
            setSuraFavoritesBackup(
                surasDao.observeIsFavorites().first().mapIndexed { index, value ->
                    index + 1 to (value == 1)
                }.toMap()
            )
        }
    }

    fun setSuraFavorites(map: Map<Int, Boolean>) {
        scope.launch {
            withContext(dispatcher) {
                map.forEach { (suraId, isFavorite) ->
                    surasDao.setFavoriteStatus(suraId, if (isFavorite) 1 else 0)
                }
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

    suspend fun getVerse(id: Int) =  withContext(dispatcher) {
        versesDao.getVerse(id)
    }
    
    fun getSuraFavoritesBackup() = quranPreferencesDataSource.getSuraFavorites()

    private suspend fun setSuraFavoritesBackup(suraFavorites: Map<Int, Boolean>) {
        quranPreferencesDataSource.updateSuraFavorites(suraFavorites.toPersistentMap())
    }

    fun getViewType() = quranPreferencesDataSource.getViewType()

    suspend fun setViewType(viewType: QuranViewType) {
        quranPreferencesDataSource.updateViewType(viewType)
    }

    fun getFillPage() = quranPreferencesDataSource.getFillPage()

    suspend fun setFillPage(fillPage: Boolean) {
        quranPreferencesDataSource.updateFillPage(fillPage)
    }

    fun getTextSize() = quranPreferencesDataSource.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        quranPreferencesDataSource.updateTextSize(textSize)
    }

    fun getKeepScreenOn() = quranPreferencesDataSource.getKeepScreenOn()

    suspend fun setKeepScreenOn(keepScreenOn: Boolean) {
        quranPreferencesDataSource.updateKeepScreenOn(keepScreenOn)
    }

    fun getBookmarks() = quranPreferencesDataSource.getBookmarks()

    fun setBookmark1VerseId(verseId: Int?) {
        scope.launch {
            quranPreferencesDataSource.updateBookmarks(
                quranPreferencesDataSource.getBookmarks().first()
                    .copy(bookmark1VerseId = verseId)
            )
        }
    }

    fun setBookmark2VerseId(verseId: Int?) {
        scope.launch {
            quranPreferencesDataSource.updateBookmarks(
                quranPreferencesDataSource.getBookmarks().first()
                    .copy(bookmark2VerseId = verseId)
            )
        }
    }

    fun setBookmark3VerseId(verseId: Int?) {
        scope.launch {
            quranPreferencesDataSource.updateBookmarks(
                quranPreferencesDataSource.getBookmarks().first()
                    .copy(bookmark3VerseId = verseId)
            )
        }
    }

    fun setBookmark4VerseId(verseId: Int?) {
        scope.launch {
            quranPreferencesDataSource.updateBookmarks(
                quranPreferencesDataSource.getBookmarks().first()
                    .copy(bookmark4VerseId = verseId)
            )
        }
    }

    fun getShouldShowMenuTutorial() = quranPreferencesDataSource.getShouldShowMenuTutorial()

    fun setShouldShowMenuTutorial(shouldShowMenuTutorial: Boolean) {
        scope.launch{
            quranPreferencesDataSource.updateShouldShowMenuTutorial(shouldShowMenuTutorial)
        }
    }

    fun getShouldShowReaderTutorial() = quranPreferencesDataSource.getShouldShowReaderTutorial()

    fun setShouldShowReaderTutorial(shouldShowReaderTutorial: Boolean) {
        scope.launch {
            quranPreferencesDataSource.updateShouldShowReaderTutorial(shouldShowReaderTutorial)
        }
    }

    fun getWerdPageNum() = quranPreferencesDataSource.getWerdPageNum()

    fun setWerdPageNum(werdPageNum: Int) {
        scope.launch {
            quranPreferencesDataSource.updateWerdPageNum(werdPageNum)
        }
    }

    fun isWerdDone() = quranPreferencesDataSource.getWerdDone()

    fun setWerdDone(isWerdDone: Boolean) {
        scope.launch {
            quranPreferencesDataSource.updateWerdDone(isWerdDone)
        }
    }

}