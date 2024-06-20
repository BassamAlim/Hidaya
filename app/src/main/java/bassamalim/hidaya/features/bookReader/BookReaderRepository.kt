package bassamalim.hidaya.features.bookReader

import android.content.Context
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import javax.inject.Inject

class BookReaderRepository @Inject constructor(
    private val ctx: Context,
    private val prefs: PreferencesDataSource,
    private val gson: Gson
) {

    fun getTextSize() = prefs.getFloat(Preference.BooksTextSize)

    fun updateTextSize(textSize: Float) {
        prefs.setFloat(Preference.BooksTextSize, textSize)
    }

    fun getDoors(bookId: Int, chapterId: Int): List<Book.BookChapter.BookDoor> {
        val path = ctx.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        val book = gson.fromJson(jsonStr, Book::class.java)
        return book.chapters[chapterId].doors.toList()
    }

}