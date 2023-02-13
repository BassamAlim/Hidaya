package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.data.Prefs
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.utils.FileUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class BookChaptersRepo @Inject constructor(
    private val context: Context,
    private val pref: SharedPreferences,
    private val gson: Gson
) {

    val language = PrefUtils.getLanguage(pref)

    fun getBook(bookId: Int): Book {
        val path = context.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return gson.fromJson(jsonStr, Book::class.java)
    }

    fun getFavs(book: Book): List<Int> {
        val favsStr = PrefUtils.getString(pref, Prefs.BookChaptersFavs(book.bookInfo.bookId))
        return if (favsStr.isNotEmpty())
            gson.fromJson(favsStr, IntArray::class.java).toList()
        else {
            val favs = mutableListOf<Int>()
            book.chapters.forEach { _ -> favs.add(0) }
            favs
        }
    }

    fun updateFavorites(bookId: Int, favs: List<Int>) {
        val json = gson.toJson(favs.toIntArray())
        pref.edit()
            .putString(Prefs.BookChaptersFavs(bookId).key, json)
            .apply()
    }

}