package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.utils.FileUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.gson.Gson
import java.io.File
import javax.inject.Inject

class BookSearcherRepo @Inject constructor(
    private val context: Context,
    private val pref: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    private val prefix = "/Books/"
    private val books = db.booksDao().getAll()
    private val language = PrefUtils.getLanguage(pref)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    fun getBookContents(): List<Book> {
        val dir = File(context.getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return emptyList()

        val bookContents = ArrayList<Book>()
        for (i in books.indices) {
            val jsonStr = FileUtils.getJsonFromDownloads(
                context.getExternalFilesDir(null).toString() + prefix + i + ".json"
            )
            val bookContent =
                try {
                    gson.fromJson(jsonStr, Book::class.java)
                } catch (e: Exception) {
                    Log.e(Global.TAG, "Error in json read in BookSearcher")
                    e.printStackTrace()
                    continue
                }

            bookContents.add(bookContent)
        }
        return bookContents
    }

    fun getBookSelections(): Array<Boolean> {
        val selections = Array(books.size) { true }

        val json = PrefUtils.getString(pref, Prefs.SelectedSearchBooks)
        if (json.isNotEmpty()) {
            val boolArr =  gson.fromJson(json, BooleanArray::class.java)
            boolArr.forEachIndexed { index, bool ->
                selections[index] = bool
            }
        }

        return selections
    }

    fun getMaxMatchesIndex() = PrefUtils.getInt(pref, Prefs.BookSearcherMaxMatchesIndex)

    fun getMaxMatchesItems(): Array<String> {
        return context.resources.getStringArray(R.array.searcher_matches_en)
    }

    fun setMaxMatchesIndex(index: Int) {
        pref.edit()
            .putInt(Prefs.BookSearcherMaxMatchesIndex.key, index)
            .apply()
    }

    fun getBookTitles(): List<String> {
        return if (language == Language.ENGLISH) db.booksDao().getTitlesEn()
        else db.booksDao().getTitles()
    }

    fun isDownloaded(id: Int): Boolean {
        val dir = File(context.getExternalFilesDir(null).toString() + "/Books/")
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