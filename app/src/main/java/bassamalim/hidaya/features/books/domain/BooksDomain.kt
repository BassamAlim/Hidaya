package bassamalim.hidaya.features.books.domain

import android.util.Log
import bassamalim.hidaya.core.data.database.dbs.BooksDB
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.other.Global
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BooksDomain @Inject constructor(
    private val booksRepo: BooksRepository,
    private val appSettingsRepo: AppSettingsRepository
) {

    suspend fun getLanguage() = appSettingsRepo.getLanguage().first()

    fun getBooks() = booksRepo.getBooks()

    fun getDownloadStates(books: List<BooksDB>) =
        books.associate {
            it.id to if (booksRepo.isDownloaded(it.id)) {
                if (booksRepo.isDownloading(it.id)) DownloadState.Downloading
                else DownloadState.Downloaded
            }
            else DownloadState.NotDownloaded
        }

    fun download(
        bookId: Int,
        onDownloadedCallback: () -> Unit
    ) {
        val downloadTask = booksRepo.download(bookId)
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
        booksRepo.deleteBook(bookId)
    }

    suspend fun getShowTutorial() = booksRepo.getShouldShowTutorial().first()

    suspend fun handleTutorialDialogDismiss(doNotShowAgain: Boolean) {
        if (doNotShowAgain)
            booksRepo.setDoNotShowAgain()
    }

}