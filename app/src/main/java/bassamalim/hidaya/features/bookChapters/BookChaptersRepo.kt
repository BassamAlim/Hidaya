package bassamalim.hidaya.features.bookChapters

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class BookChaptersRepo @Inject constructor(
    private val ctx: Context,
    private val sp: SharedPreferences,
    private val gson: Gson
) {

    fun getLanguage() = PrefUtils.getLanguage(sp)

    fun getBook(bookId: Int): Book {
        val path = ctx.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return gson.fromJson(jsonStr, Book::class.java)
    }

    fun getFavs(book: Book): List<Int> {
        val favsStr = PrefUtils.getString(sp, Prefs.BookChaptersFavs(book.bookInfo.bookId))
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
        sp.edit()
            .putString(Prefs.BookChaptersFavs(bookId).key, json)
            .apply()
    }

}