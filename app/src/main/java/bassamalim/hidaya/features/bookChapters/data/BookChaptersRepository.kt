package bassamalim.hidaya.features.bookChapters.data

import android.app.Application
import bassamalim.hidaya.core.data.preferences.repositories.BooksPreferencesRepository
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookChaptersRepository @Inject constructor(
    private val app: Application,
    private val gson: Gson,
    private val booksPrefsRepo: BooksPreferencesRepository
) {

    fun getBook(bookId: Int): Book {
        val path = app.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return gson.fromJson(jsonStr, Book::class.java)
    }

    fun getFavs(book: Book) =
        booksPrefsRepo.flow.map { preferences ->
            val allFavs = preferences.chaptersFavs

            if (allFavs.containsKey(book.bookInfo.bookId))
                allFavs[book.bookInfo.bookId]!!
            else {
                val favs = book.chapters.associate {
                    it.chapterId to 0
                }

                booksPrefsRepo.update { it.copy(
                    chaptersFavs = it.chaptersFavs.mutate { oldMap ->
                        oldMap[book.bookInfo.bookId] = favs.toPersistentMap()
                    }
                )}

                favs
            }
        }

    suspend fun setFavs(bookId: Int, favs: Map<Int, Int>) {
        booksPrefsRepo.update { it.copy(
            chaptersFavs = it.chaptersFavs.mutate { oldMap ->
                oldMap[bookId] = favs.toPersistentMap()
            }
        )}
    }

}