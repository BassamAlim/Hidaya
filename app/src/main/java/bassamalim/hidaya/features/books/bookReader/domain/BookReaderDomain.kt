package bassamalim.hidaya.features.books.bookReader.domain

import bassamalim.hidaya.core.data.repositories.BooksRepository
import javax.inject.Inject

class BookReaderDomain @Inject constructor(
    private val booksRepository: BooksRepository
) {

    fun getTextSize() = booksRepository.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        booksRepository.setTextSize(textSize)
    }

    fun getDoors(bookId: Int, chapterId: Int) = booksRepository.getDoors(bookId, chapterId)

}