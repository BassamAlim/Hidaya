package bassamalim.hidaya.features.bookChapters

import android.content.Context
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import javax.inject.Inject

class BookChaptersRepo @Inject constructor(
    private val ctx: Context,
    private val preferencesDS: PreferencesDataSource,
    private val gson: Gson
) {

    fun getLanguage() = preferencesDS.getLanguage()

    fun getBook(bookId: Int): Book {
        val path = ctx.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return gson.fromJson(jsonStr, Book::class.java)
    }

    fun getFavs(book: Book): List<Int> {
        val favsStr = preferencesDS.getString(Preference.BookChaptersFavs(book.bookInfo.bookId))
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
        preferencesDS.setString(Preference.BookChaptersFavs(bookId), json)
    }

}