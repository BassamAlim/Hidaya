package bassamalim.hidaya.features.bookSearcher.data

import android.app.Application
import android.content.res.Resources
import android.util.Log
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.BooksPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class BookSearcherRepository @Inject constructor(
    app: Application,
    private val resources: Resources,
    private val db: AppDatabase,
    private val appSettingsPrefsRepo: AppSettingsPreferencesDataSource,
    private val booksPreferencesRepo: BooksPreferencesDataSource,
    private val gson: Gson
) {

    private val path = "${app.getExternalFilesDir(null)}/Books/"

    suspend fun getLanguage() = appSettingsPrefsRepo.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsPrefsRepo.getNumeralsLanguage().first()

    fun getBookSelections() =
        booksPreferencesRepo.getSearchSelections().map {
            it.ifEmpty {
                val books = getBooks()
                it.mutate { oldMap ->
                    books.forEach { book -> oldMap[book.id] = true }
                }
            }.toMap()
        }

    suspend fun setBookSelections(selections: Map<Int, Boolean>) {
        booksPreferencesRepo.update { it.copy(
            searchSelections = selections.toPersistentMap()
        )}
    }

    fun getMaxMatches() = booksPreferencesRepo.getSearchMaxMatches()

    suspend fun setMaxMatches(value: Int) {
        booksPreferencesRepo.update { it.copy(
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
        if (language == Language.ENGLISH) db.booksDao().getTitlesEn()
        else db.booksDao().getTitles()

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

    private fun getBooks() = db.booksDao().getAll()

}