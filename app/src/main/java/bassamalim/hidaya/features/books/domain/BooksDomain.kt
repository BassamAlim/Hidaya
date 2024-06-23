package bassamalim.hidaya.features.books.domain

import android.util.Log
import bassamalim.hidaya.core.data.database.dbs.BooksDB
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.books.data.BooksRepository
import javax.inject.Inject

class BooksDomain @Inject constructor(
    private val repository: BooksRepository
) {

    suspend fun getLanguage() = repository.getLanguage()

    fun getBooks() = repository.getBooks()

    fun getDownloadStates(books: List<BooksDB>) =
        books.associate {
            it.id to if (repository.isDownloaded(it.id)) {
                if (repository.isDownloading(it.id)) DownloadState.Downloading
                else DownloadState.Downloaded
            }
            else DownloadState.NotDownloaded
        }

    fun download(
        bookId: Int,
        onDownloadedCallback: () -> Unit
    ) {
        val downloadTask = repository.download(bookId)
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
        repository.deleteBook(bookId)
    }

    suspend fun getShowTutorial() = repository.getShowTutorial()

    suspend fun handleTutorialDialogDismiss(doNotShowAgain: Boolean) {
        if (doNotShowAgain)
            repository.setDoNotShowAgain()
    }

}