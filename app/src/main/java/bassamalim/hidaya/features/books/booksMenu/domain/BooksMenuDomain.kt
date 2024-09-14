package bassamalim.hidaya.features.books.booksMenu.domain

import android.util.Log
import bassamalim.hidaya.core.data.database.models.Book
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.other.Global
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BooksMenuDomain @Inject constructor(
    private val booksRepository: BooksRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getBooks() = booksRepository.getBooks()

    fun getDownloadStates(books: List<Book>) =
        books.associate {
            it.id to if (booksRepository.isDownloaded(it.id)) {
                if (booksRepository.isDownloading(it.id)) DownloadState.DOWNLOADING
                else DownloadState.DOWNLOADED
            }
            else DownloadState.NOT_DOWNLOADED
        }

    fun download(
        bookId: Int,
        onDownloadedCallback: () -> Unit
    ) {
        val downloadTask = booksRepository.download(bookId)
        downloadTask
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

    suspend fun getShowTutorial() = booksRepository.getShouldShowTutorial().first()

    suspend fun handleTutorialDialogDismiss(doNotShowAgain: Boolean) {
        if (doNotShowAgain)
            booksRepository.setDoNotShowAgain()
    }

}