package bassamalim.hidaya.features.books.booksMenu.domain

import android.util.Log
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.books.booksMenu.ui.Book
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BooksMenuDomain @Inject constructor(
    private val booksRepository: BooksRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getBooks(language: Language) = booksRepository.getBooks().associate {
        it.id to Book(
            title = when (language) {
                Language.ARABIC -> it.titleAr
                Language.ENGLISH -> it.titleEn
            },
            author = when (language) {
                Language.ARABIC -> it.authorAr
                Language.ENGLISH -> it.authorEn
            },
            url = it.url,
            isFavorite = it.isFavorite == 1,
            downloadState = getDownloadState(it.id)
        )
    }

    private fun getDownloadState(bookId: Int) =
        if (booksRepository.isDownloaded(bookId)) {
            if (booksRepository.isDownloading(bookId)) DownloadState.DOWNLOADING
            else DownloadState.DOWNLOADED
        }
        else DownloadState.NOT_DOWNLOADED

    fun downloadBook(bookId: Int, onDownloadedCallback: () -> Unit) {
        booksRepository.download(bookId)
            .addOnSuccessListener {
                Log.i(Global.TAG, "File download succeeded")

                onDownloadedCallback()
            }
            .addOnFailureListener {
                Log.e(Global.TAG, "File download failed")
            }
    }

    fun deleteBook(bookId: Int) {
        booksRepository.deleteBook(bookId)
    }

    fun getShowTutorial() = booksRepository.getShouldShowTutorial()

    suspend fun handleTutorialDialogDismiss(doNotShowAgain: Boolean) {
        if (doNotShowAgain) booksRepository.setShouldShowTutorial(false)
    }

}