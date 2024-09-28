package bassamalim.hidaya.features.books.bookChaptersMenu.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BookChaptersDomain @Inject constructor(
    private val booksRepository: BooksRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getBook(bookId: Int, language: Language): Flow<Book> =
        booksRepository.getFullBook(bookId, language)

    suspend fun setFavoriteStatus(bookId: Int, chapterNum: Int, newValue: Boolean) {
        booksRepository.setChapterFavorite(bookId, chapterNum, newValue)
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

}