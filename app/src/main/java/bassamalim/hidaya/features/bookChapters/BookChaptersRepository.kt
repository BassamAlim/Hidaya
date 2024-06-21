package bassamalim.hidaya.features.bookChapters

import android.content.Context
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.BooksPreferencesRepository
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BookChaptersRepository @Inject constructor(
    private val ctx: Context,
    private val gson: Gson,
    private val appSettingsPrefsRepo: AppSettingsPreferencesRepository,
    private val booksPrefsRepo: BooksPreferencesRepository
) {

    suspend fun getLanguage() = appSettingsPrefsRepo.flow.first()
        .language

    fun getBook(bookId: Int): Book {
        val path = ctx.getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return gson.fromJson(jsonStr, Book::class.java)
    }

    suspend fun getFavs(book: Book): Map<Int, Int> {
        val allFavs = booksPrefsRepo.flow.first().chaptersFavs
        return if (allFavs.containsKey(book.bookInfo.bookId))
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

    suspend fun setFavorites(bookId: Int, favs: Map<Int, Int>) {
        booksPrefsRepo.update { it.copy(
            chaptersFavs = it.chaptersFavs.mutate { oldMap ->
                oldMap[bookId] = favs.toPersistentMap()
            }
        )}
    }

}