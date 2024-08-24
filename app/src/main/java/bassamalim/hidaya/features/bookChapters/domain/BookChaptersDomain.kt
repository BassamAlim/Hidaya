package bassamalim.hidaya.features.bookChapters.domain

import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.models.Book
import javax.inject.Inject

class BookChaptersDomain @Inject constructor(
    private val booksRepo: BooksRepository,
) {

    fun getBook(bookId: Int) = booksRepo.getBook(bookId)

    fun getFavorites(book: Book) = booksRepo.getChapterFavorites(book)

    suspend fun setIsFavorite(bookId: Int, chapterNum: Int, newValue: Boolean) {
        booksRepo.setChapterFavorite(bookId, chapterNum, newValue)
    }

}