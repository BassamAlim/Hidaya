package bassamalim.hidaya.features.books.bookReader

import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.LangUtils
import javax.inject.Inject

class BookReaderDomain @Inject constructor(
    private val booksRepository: BooksRepository
) {

    suspend fun getBookTitle(bookId: Int, language: Language) =
        booksRepository.getBookTitle(bookId, language)

    fun getDoors(bookId: Int, chapterId: Int) = booksRepository.getDoors(bookId, chapterId)

    fun getTextSize() = booksRepository.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        booksRepository.setTextSize(textSize)
    }

    fun getLanguage() = LangUtils.getAppLanguage()

}