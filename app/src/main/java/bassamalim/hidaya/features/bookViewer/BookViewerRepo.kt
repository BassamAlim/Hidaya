package bassamalim.hidaya.features.bookViewer

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class BookViewerRepo @Inject constructor(
    private val context: Context,
    private val pref: SharedPreferences,
    private val gson: Gson
) {

    fun getTextSize() = PrefUtils.getFloat(pref, bassamalim.hidaya.core.data.Prefs.BooksTextSize)

    fun updateTextSize(textSize: Float) {
        pref.edit()
            .putFloat(bassamalim.hidaya.core.data.Prefs.BooksTextSize.key, textSize)
            .apply()
    }

    fun getDoors(bookId: Int, chapterId: Int): List<Book.BookChapter.BookDoor> {
        val path = context.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        val book = gson.fromJson(jsonStr, Book::class.java)
        return book.chapters[chapterId].doors.toList()
    }

}