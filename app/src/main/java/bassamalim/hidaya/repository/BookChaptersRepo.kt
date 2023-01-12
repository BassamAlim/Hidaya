package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import bassamalim.hidaya.Prefs
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

    fun getFavs(book: Book): SnapshotStateList<Int> {
        var favs = mutableStateListOf<Int>()

        val favsPref = Prefs.BookChaptersFavs(book.bookInfo.bookId)
        val favsStr = PrefUtils.getString(pref, favsPref.key, favsPref.default as String)
        if (favsStr.isNotEmpty())
            favs = gson.fromJson(favsStr, SnapshotStateList::class.java) as SnapshotStateList<Int>
        else book.chapters.forEach { _ -> favs.add(0) }

        return favs
    }

    fun updateFavorites(bookId: Int, favs: SnapshotStateList<Int>) {
        val favStr = gson.toJson(favs)

        val favsPref = Prefs.BookChaptersFavs(bookId)
        pref.edit()
            .putString(favsPref.key, favStr)
            .apply()
    }

}