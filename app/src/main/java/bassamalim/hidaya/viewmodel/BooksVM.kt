package bassamalim.hidaya.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.database.dbs.BooksDB
import bassamalim.hidaya.enums.DownloadState
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.repository.BooksRepo
import bassamalim.hidaya.state.BooksState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BooksVM @Inject constructor(
    private val repository: BooksRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(BooksState(
        items = repository.getBooks()
    ))
    val uiState = _uiState.asStateFlow()

    val downloadStates = mutableStateListOf<DownloadState>()

    fun onStart() {
        checkDownloads()
    }

    fun getPath(itemId: Int): String {
        return repository.getPath(itemId)
    }

    fun onFileDeleted(itemId: Int) {
        downloadStates[itemId] = DownloadState.NotDownloaded
    }

    fun onFabClick(navController: NavController) {
        navController.navigate(Screen.BookSearcher.route)
    }

    private fun checkDownloads() {
        downloadStates.clear()
        for (book in _uiState.value.items) {
            downloadStates.add(
                if (repository.isDownloaded(book.id)) {
                    if (repository.isDownloading(book.id)) DownloadState.Downloading
                    else DownloadState.Downloaded
                }
                else DownloadState.NotDownloaded
            )
        }
    }

    fun download(item: BooksDB) {
        downloadStates[item.id] = DownloadState.Downloading

        val downloadTask = repository.download(item)

        downloadTask.addOnSuccessListener {
            Log.i(Global.TAG, "File download succeeded")
            checkDownloads()
        }.addOnFailureListener {
            Log.e(Global.TAG, "File download failed")
        }
    }

    fun onItemClick(item: BooksDB, navController: NavController) {
        if (downloadStates[item.id] == DownloadState.NotDownloaded) download(item)
        else if (downloadStates[item.id] == DownloadState.Downloaded) {
            navController.navigate(Screen.BookChapters.withArgs(
                item.id.toString(),
                if (repository.language == Language.ENGLISH) item.titleEn
                else item.title
            ))
        }
        else showWaitMassage()
    }

    private fun showWaitMassage() {
        _uiState.update { it.copy(
            shouldShowWaitMassage = true
        )}
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            isTutorialDialogShown = false
        )}

        if (doNotShowAgain) repository.setDoNotShowAgain()
    }

}