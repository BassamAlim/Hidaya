package bassamalim.hidaya.features.bookReader.data

import android.app.Application
import bassamalim.hidaya.core.data.preferences.repositories.BooksPreferencesRepository
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookReaderRepository @Inject constructor(
    private val app: Application,
    private val gson: Gson,
    private val booksPrefsRepo: BooksPreferencesRepository
) {

    fun getTextSize() = booksPrefsRepo.flow.map {
        it.textSize
    }

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