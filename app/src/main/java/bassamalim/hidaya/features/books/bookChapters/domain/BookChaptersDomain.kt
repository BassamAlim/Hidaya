package bassamalim.hidaya.features.books.bookChapters.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BookChaptersDomain @Inject constructor(
    private val booksRepository: BooksRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getBook(bookId: Int, language: Language) =
        booksRepository.getFullBook(bookId, language)

    fun getFavoriteStatuses(bookId: Int) =
        booksRepository.getChapterFavorites(bookId)

    suspend fun setFavoriteStatus(bookId: Int, chapterNum: Int, newValue: Boolean) {
        booksRepository.setChapterFavorite(bookId, chapterNum, newValue)
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

}