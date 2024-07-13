package bassamalim.hidaya.features.bookReader.data

import android.app.Application
import bassamalim.hidaya.core.data.preferences.repositories.BooksPreferencesRepository
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import javax.inject.Inject

class BookReaderRepository @Inject constructor(
    private val app: Application,
    private val booksPrefsRepo: BooksPreferencesRepository,
    private val gson: Gson
) {

    fun getTextSize() = booksPrefsRepo.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        booksPrefsRepo.update { it.copy(
            textSize = textSize
        )}
    }

    fun getDoors(bookId: Int, chapterId: Int): List<Book.BookChapter.BookDoor> {
        val path = app.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        val book = gson.fromJson(jsonStr, Book::class.java)
        return book.chapters[chapterId].doors.toList()
    }

}