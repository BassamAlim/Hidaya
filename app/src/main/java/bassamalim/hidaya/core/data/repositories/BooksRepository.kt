package bassamalim.hidaya.core.data.repositories

import android.app.Application
import android.content.res.Resources
import android.util.Log
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.daos.BooksDao
import bassamalim.hidaya.core.data.preferences.dataSources.BooksPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.FileUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val app: Application,
    private val resources: Resources,
    private val booksDao: BooksDao,
    private val booksPreferencesDataSource: BooksPreferencesDataSource,
    private val gson: Gson
) {

    private val prefix = "/Books/"
    private val path = "${app.getExternalFilesDir(null)}/Books/"

    fun getBooks() = booksDao.getAll()

    fun getBook(bookId: Int): Book {
        val path = app.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return gson.fromJson(jsonStr, Book::class.java)
    }

    fun getDoors(bookId: Int, chapterId: Int): List<Book.BookChapter.BookDoor> {
        val book = getBook(bookId)
        return book.chapters[chapterId].doors.toList()
    }

    fun getChapterFavorites(book: Book) = booksPreferencesDataSource.flow.map { bookPrefs ->
        if (bookPrefs.chapterFavorites.containsKey(book.bookInfo.bookId))
            bookPrefs.chapterFavorites[book.bookInfo.bookId]!!
        else {
            val favs = book.chapters.associate { it.chapterId to 0 }
            booksPreferencesDataSource.update { it.copy(
                chapterFavorites = it.chapterFavorites.mutate { oldMap ->
                    oldMap[book.bookInfo.bookId] = favs.toPersistentMap()
                }
            )}
            favs
        }
    }
    suspend fun setChapterFavorites(bookId: Int, favs: Map<Int, Int>) {
        booksPreferencesDataSource.update { it.copy(
            chapterFavorites = it.chapterFavorites.mutate { oldMap ->
                oldMap[bookId] = favs.toPersistentMap()
            }
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

    suspend fun setDoNotShowAgain() {
        booksPreferencesDataSource.update { it.copy(
            shouldShowTutorial = false
        )}
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
        val dir = File(path)
        if (!dir.exists()) return false

        val files = dir.listFiles()
        for (element in files!!) {
            val name = element.name
            val n = name.substring(0, name.length - 5)
            try {
                val num = n.toInt()
                if (num == id) return true
            } catch (ignored: NumberFormatException) {}
        }
        return false
    }

    fun isDownloading(bookId: Int): Boolean {
        val path = app.getExternalFilesDir(null).toString() + prefix + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return try {
            gson.fromJson(jsonStr, Book::class.java)
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

    fun getBookSelections() =
        booksPreferencesDataSource.flow.map {
            it.searchSelections.ifEmpty {
                val books = getBooks()
                it.searchSelections.mutate { oldMap ->
                    books.forEach { book -> oldMap[book.id] = true }
                }
            }.toMap()
        }

    suspend fun setBookSelections(selections: Map<Int, Boolean>) {
        booksPreferencesDataSource.update { it.copy(
            searchSelections = selections.toPersistentMap()
        )}
    }

    fun getMaxMatches() = booksPreferencesDataSource.flow.map {
        it.searchMaxMatches
    }
    suspend fun setMaxMatches(value: Int) {
        booksPreferencesDataSource.update { it.copy(
            searchMaxMatches = value
        )}
    }

    fun getBookContents(): List<Book> {
        val dir = File(path)
        if (!dir.exists()) return emptyList()

        val books = getBooks()

        val bookContents = ArrayList<Book>()
        for (i in books.indices) {
            val jsonStr = FileUtils.getJsonFromDownloads("$path$i.json")
            try {
                val bookContent = gson.fromJson(jsonStr, Book::class.java)
                if (bookContent != null) bookContents.add(bookContent)
            } catch (e: Exception) {
                Log.e(Global.TAG, "Error in json read in BookSearcher")
                e.printStackTrace()
                continue
            }
        }

        return bookContents
    }

    fun getMaxMatchesItems(): Array<String> =
        resources.getStringArray(R.array.searcher_matches_en)

    fun getBookTitles(language: Language): List<String> =
        if (language == Language.ENGLISH) booksDao.getTitlesEn()
        else booksDao.getTitlesAr()

}