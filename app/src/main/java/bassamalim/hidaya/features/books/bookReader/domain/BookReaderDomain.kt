package bassamalim.hidaya.features.bookReader.domain

import bassamalim.hidaya.core.data.repositories.BooksRepository
import javax.inject.Inject

class BookReaderDomain @Inject constructor(
    private val booksRepo: BooksRepository
) {

    fun getTextSize() = booksRepo.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        booksRepo.setTextSize(textSize)
    }

    fun getDoors(bookId: Int, chapterId: Int) = booksRepo.getDoors(bookId, chapterId)

}