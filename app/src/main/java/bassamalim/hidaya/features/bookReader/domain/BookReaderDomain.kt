package bassamalim.hidaya.features.bookReader.domain

import bassamalim.hidaya.features.bookReader.data.BookReaderRepository
import javax.inject.Inject

class BookReaderDomain @Inject constructor(
    private val repository: BookReaderRepository
) {

    fun getTextSize() = repository.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        repository.setTextSize(textSize)
    }

    fun getDoors(bookId: Int, chapterId: Int) = repository.getDoors(bookId, chapterId)

}