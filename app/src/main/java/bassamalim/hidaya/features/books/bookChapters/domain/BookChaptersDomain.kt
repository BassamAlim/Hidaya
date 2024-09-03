package bassamalim.hidaya.features.books.bookChapters.domain

import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.models.Book
import javax.inject.Inject

class BookChaptersDomain @Inject constructor(
    private val booksRepository: BooksRepository,
) {

    fun getBook(bookId: Int) = booksRepository.getBook(bookId)

    fun getFavorites(book: Book) = booksRepository.getChapterFavorites(book)

    suspend fun setFavoriteStatus(bookId: Int, chapterNum: Int, newValue: Boolean) {
        booksRepository.setChapterFavorite(bookId, chapterNum, newValue)
    }

}