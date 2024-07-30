package bassamalim.hidaya.features.bookChapters.data

import android.app.Application
import bassamalim.hidaya.core.data.preferences.dataSources.BooksPreferencesDataSource
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookChaptersRepository @Inject constructor(
    private val app: Application,
    private val booksPrefsRepo: BooksPreferencesDataSource,
    private val gson: Gson
) {

    fun getBook(bookId: Int): Book {
        val path = app.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return gson.fromJson(jsonStr, Book::class.java)
    }

    fun getFavorites(book: Book) =
        booksPrefsRepo.getChapterFavorites().map { favoriteChapters ->

            if (favoriteChapters.containsKey(book.bookInfo.bookId))
                favoriteChapters[book.bookInfo.bookId]!!
            else {
                val favs = book.chapters.associate {
                    it.chapterId to 0
                }

                booksPrefsRepo.update { it.copy(
                    chapterFavorites = it.chapterFavorites.mutate { oldMap ->
                        oldMap[book.bookInfo.bookId] = favs.toPersistentMap()
                    }
                )}

                favs
            }
        }

    suspend fun setFavorites(bookId: Int, favs: Map<Int, Int>) {
        booksPrefsRepo.update { it.copy(
            chapterFavorites = it.chapterFavorites.mutate { oldMap ->
                oldMap[bookId] = favs.toPersistentMap()
            }
        )}
    }

}