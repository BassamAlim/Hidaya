package bassamalim.hidaya.features.bookReader

import android.content.Context
import bassamalim.hidaya.core.data.preferences.repositories.BooksPreferencesRepository
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BookReaderRepository @Inject constructor(
    private val ctx: Context,
    private val gson: Gson,
    private val booksPrefsRepo: BooksPreferencesRepository
) {

    suspend fun getTextSize() = booksPrefsRepo.flow.first()
        .textSize

    suspend fun setTextSize(textSize: Float) {
        booksPrefsRepo.update { it.copy(
            textSize = textSize
        )}
    }

    fun getDoors(bookId: Int, chapterId: Int): List<Book.BookChapter.BookDoor> {
        val path = ctx.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        val book = gson.fromJson(jsonStr, Book::class.java)
        return book.chapters[chapterId].doors.toList()
    }

}