package bassamalim.hidaya.features.books.bookReader.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BookReaderDomain @Inject constructor(
    private val booksRepository: BooksRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getBookTitle(bookId: Int, language: Language) =
        booksRepository.getBookTitle(bookId, language)

    fun getDoors(bookId: Int, chapterId: Int) = booksRepository.getDoors(bookId, chapterId)

    fun getTextSize() = booksRepository.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        booksRepository.setTextSize(textSize)
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

}