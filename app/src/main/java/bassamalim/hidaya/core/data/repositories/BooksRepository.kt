package bassamalim.hidaya.core.data.repositories

import android.app.Application
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.BooksPreferencesDataSource
import bassamalim.hidaya.core.data.dataSources.room.daos.BooksDao
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.models.BookContent
import bassamalim.hidaya.core.models.BookInfo
import bassamalim.hidaya.core.utils.FileUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val app: Application,
    private val booksDao: BooksDao,
    private val booksPreferencesDataSource: BooksPreferencesDataSource,
    private val appSettingsRepository: AppSettingsRepository,
    private val gson: Gson,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) {

    private val prefix = "/Books/"
    private val dir = "${app.getExternalFilesDir(null)}/Books/"

    suspend fun getBooksMenu(language: Language) = withContext(dispatcher) {
        booksDao.getAll().map {
            BookInfo(
                id = it.id,
                title = when (language) {
                    Language.ARABIC -> it.titleAr
                    Language.ENGLISH -> it.titleEn
                },
                author = when (language) {
                    Language.ARABIC -> it.authorAr
                    Language.ENGLISH -> it.authorEn
                },
                url = it.url,
                isFavorite = it.isFavorite == 1
            )
        }
    }

    suspend fun getBookInfo(id: Int, language: Language) = withContext(dispatcher) {
        booksDao.getById(id).let {
            BookInfo(
                id = it.id,
                title = when (language) {
                    Language.ARABIC -> it.titleAr
                    Language.ENGLISH -> it.titleEn
                },
                author = when (language) {
                    Language.ARABIC -> it.authorAr
                    Language.ENGLISH -> it.authorEn
                },
                url = it.url,
                isFavorite = it.isFavorite == 1
            )
        }
    }

    private fun getBookContent(bookId: Int): BookContent? {
        if (!isDownloaded(bookId)) return null
        val jsonStr = FileUtils.getJsonFromDownloads("$dir$bookId.json")
        return gson.fromJson(jsonStr, BookContent::class.java)
    }

    suspend fun getBookContents(language: Language): List<BookContent> {
        if (!File(dir).exists()) return emptyList()

        return getBooksMenu(language)
            .filter { bookInfo -> isDownloaded(bookInfo.id) }
            .map { bookInfo ->
                val jsonStr = FileUtils.getJsonFromDownloads("${dir}${bookInfo.id}.json")
                gson.fromJson(jsonStr, BookContent::class.java)
            }
    }

    suspend fun getFullBook(bookId: Int, language: Language): Book {
        val bookInfo = getBookInfo(bookId, language)
        val bookContent = getBookContent(bookId)!!
        val favorites = getChapterFavorites(bookId).first()
        return Book(
            id = bookInfo.id,
            title = bookInfo.title,
            chapters = bookContent.chapters.map { chapter ->
                Book.Chapter(
                    id = chapter.id,
                    title = chapter.title,
                    doors = bookContent.chapters[chapter.id].doors.map { door ->
                        Book.Chapter.Door(
                            id = door.id,
                            title = door.title,
                            text = door.text
                        )
                    },
                    isFavorite = favorites[chapter.id]!!
                )
            }
        )
    }

    suspend fun getBookTitles(language: Language) = withContext(dispatcher) {
        when (language) {
            Language.ARABIC -> booksDao.getTitlesAr()
            Language.ENGLISH -> booksDao.getTitlesEn()
        }
    }

    suspend fun getBookTitle(bookId: Int, language: Language) = withContext(dispatcher) {
        when (language) {
            Language.ARABIC -> booksDao.getTitleAr(bookId)
            Language.ENGLISH -> booksDao.getTitleEn(bookId)
        }
    }

    fun getDoors(bookId: Int, chapterId: Int) =
        getBookContent(bookId)!!.chapters[chapterId].doors.toList()

    fun getChapterFavorites(bookId: Int): Flow<Map<Int, Boolean>> {
        val bookContent = getBookContent(bookId)

        return booksPreferencesDataSource.getChapterFavorites().map {
            if (it.containsKey(bookId)) it[bookId]!!.toMap()
            else {
                val favs = bookContent!!.chapters.associate { it.id to false }
                it.mutate { oldMap -> oldMap[bookId] = favs.toPersistentMap() }.toMap()
            }
        }
    }

    suspend fun setChapterFavorite(bookId: Int, chapterNum: Int, newValue: Boolean) {
        booksPreferencesDataSource.updateChapterFavorites(
            booksPreferencesDataSource.getChapterFavorites().first().mutate { oldMap ->
                oldMap[bookId] = oldMap[bookId]!!.mutate { it[chapterNum] = newValue }
            }
        )
    }

    fun getTextSize() = booksPreferencesDataSource.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        booksPreferencesDataSource.updateTextSize(textSize)
    }

    fun getSearchSelections(language: Language): Flow<Map<Int, Boolean>> {
        return booksPreferencesDataSource.getSearchSelections().map {
            it.ifEmpty {
                val books = getBooksMenu(language)
                it.mutate { oldMap ->
                    books.forEach { book -> oldMap[book.id] = true }
                }
            }.map { (id, isSelected) ->
                id to if (isDownloaded(id)) isSelected else false
            }.toMap()
        }
    }

    suspend fun setSearchSelections(selections: Map<Int, Boolean>) {
        booksPreferencesDataSource.updateSearchSelections(selections.toPersistentMap())
    }

    fun getSearchMaxMatches() = booksPreferencesDataSource.getSearchMaxMatches()

    suspend fun setSearchMaxMatches(searchMaxMatches: Int) {
        booksPreferencesDataSource.updateSearchMaxMatches(searchMaxMatches)
    }

    fun getShouldShowTutorial() = booksPreferencesDataSource.getShouldShowTutorial()

    suspend fun setShouldShowTutorial(shouldShowTutorial: Boolean) {
        booksPreferencesDataSource.updateShouldShowTutorial(shouldShowTutorial)
    }

    fun download(bookId: Int): FileDownloadTask {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val fileRef = storageRef.child("${prefix.substring(1)}$bookId.json")

        FileUtils.createDir(app, prefix)
        val file = File("${app.getExternalFilesDir(null)}/$prefix/${fileRef.name}")
        file.createNewFile()

        return fileRef.getFile(file)
    }

    fun isDownloaded(id: Int): Boolean {
        val dir = File(dir)
        if (!dir.exists()) return false

        for (file in dir.listFiles()!!) {
            val fileName = file.name
            val n = fileName.substring(0, fileName.length - 5)
            try {
                val num = n.toInt()
                if (num == id) return true
            } catch (ignored: NumberFormatException) {}
        }
        return false
    }

    fun isDownloading(bookId: Int): Boolean {
        val path = "$dir$bookId.json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return try {
            gson.fromJson(jsonStr, BookContent::class.java)
            false
        } catch (e: Exception) {
            true
        }
    }

    fun deleteBook(bookId: Int) {
        FileUtils.deleteFile(
            context = app,
            path = "${prefix}$bookId.json"
        )
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

}