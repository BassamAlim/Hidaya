package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.QuranPreferencesDataSource
import bassamalim.hidaya.core.data.dataSources.room.daos.SurasDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VersesDao
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.QuranViewType
import bassamalim.hidaya.core.models.Sura
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

    suspend fun setBookmarkVerseId(index: Int, verseId: Int?) {
        val current = quranPreferencesDataSource.getBookmarks().first()
        quranPreferencesDataSource.updateBookmarks(
            when (index) {
                1 -> current.copy(bookmark1VerseId = verseId)
                2 -> current.copy(bookmark2VerseId = verseId)
                3 -> current.copy(bookmark3VerseId = verseId)
                4 -> current.copy(bookmark4VerseId = verseId)
                else -> throw IllegalArgumentException("Invalid bookmark index: $index")
            }
        )
    }

    fun getShouldShowReaderTutorial() = quranPreferencesDataSource.getShouldShowReaderTutorial()

    suspend fun setShouldShowReaderTutorial(shouldShowReaderTutorial: Boolean) {
        quranPreferencesDataSource.updateShouldShowReaderTutorial(shouldShowReaderTutorial)
    }

    fun getWerdPageNum() = quranPreferencesDataSource.getWerdPageNum()

    suspend fun setWerdPageNum(werdPageNum: Int) {
        quranPreferencesDataSource.updateWerdPageNum(werdPageNum)
    }

    fun isWerdDone() = quranPreferencesDataSource.getWerdDone()

    suspend fun setWerdDone(isWerdDone: Boolean) {
        quranPreferencesDataSource.updateWerdDone(isWerdDone)
    }

}