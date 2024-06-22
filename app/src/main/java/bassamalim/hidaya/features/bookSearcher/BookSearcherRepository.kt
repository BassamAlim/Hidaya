package bassamalim.hidaya.features.bookSearcher

import android.content.Context
import android.content.res.Resources
import android.util.Log
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.BooksPreferencesRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

class BookSearcherRepository @Inject constructor(
    ctx: Context,
    private val resources: Resources,
    private val db: AppDatabase,
    private val gson: Gson,
    private val appSettingsPrefsRepo: AppSettingsPreferencesRepository,
    private val booksPreferencesRepo: BooksPreferencesRepository
) {

    private val path = "${ctx.getExternalFilesDir(null)}/Books/"

    suspend fun getLanguage() = appSettingsPrefsRepo.flow.first()
        .language

    suspend fun getNumeralsLanguage() = appSettingsPrefsRepo.flow.first()
        .numeralsLanguage

    private fun getBooks() = db.booksDao().getAll()

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

    suspend fun getBookSelections(): Map<Int, Boolean> {
        val books = getBooks()

        val selections = booksPreferencesRepo.flow.first()
            .searchSelections

        return if (selections.isNotEmpty()) selections
        else {
            selections.mutate { oldMap ->
                books.forEach {
                    oldMap[it.id] = true
                }
            }
        }
    }

    suspend fun getMaxMatches() =
        booksPreferencesRepo.flow.first().searcherMaxMatches

    suspend fun setMaxMatches(value: Int) {
        booksPreferencesRepo.update { it.copy(
            searcherMaxMatches = value
        )}
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

}