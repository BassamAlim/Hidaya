package bassamalim.hidaya.viewmodel

import android.util.Log
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
    private val repo: BooksRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(BooksState(
        items = repo.getBooks()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(
            downloadStates = getDownloadStates()
        )}
    }

    fun onStart() {
        _uiState.update { it.copy(
            downloadStates = getDownloadStates()
        )}
    }

    fun getPath(itemId: Int): String {
        return repo.getPath(itemId)
    }

    fun onFileDeleted(itemId: Int) {
        val downloadStates = _uiState.value.downloadStates.toMutableList()
        downloadStates[itemId] = DownloadState.NotDownloaded
        _uiState.update { it.copy(
            downloadStates = downloadStates
        )}
    }

    fun onFabClick(navController: NavController) {
        navController.navigate(Screen.BookSearcher.route)
    }

    private fun getDownloadStates(): ArrayList<DownloadState> {
        val downloadStates = ArrayList<DownloadState>()
        for (book in _uiState.value.items) {
            downloadStates.add(
                if (repo.isDownloaded(book.id)) {
                    if (repo.isDownloading(book.id)) DownloadState.Downloading
                    else DownloadState.Downloaded
                }
                else DownloadState.NotDownloaded
            )
        }
        return downloadStates
    }

    fun download(item: BooksDB) {
        val downloadStates = _uiState.value.downloadStates.toMutableList()
        downloadStates[item.id] = DownloadState.Downloading
        _uiState.update { it.copy(
            downloadStates = downloadStates
        )}

        val downloadTask = repo.download(item)
        downloadTask
            .addOnSuccessListener {
                Log.i(Global.TAG, "File download succeeded")

                _uiState.update { it.copy(
                    downloadStates = getDownloadStates()
                )}
            }
            .addOnFailureListener {
                Log.e(Global.TAG, "File download failed")
            }
    }

    fun onItemClick(item: BooksDB, nc: NavController) {
        if (_uiState.value.downloadStates[item.id] == DownloadState.NotDownloaded) download(item)
        else if (_uiState.value.downloadStates[item.id] == DownloadState.Downloaded) {
            nc.navigate(
                Screen.BookChapters(
                    item.id.toString(),
                    if (repo.language == Language.ENGLISH) item.titleEn
                    else item.title
                ).route
            )
        }
        else showWaitMassage()
    }

    private fun showWaitMassage() {
        _uiState.update { it.copy(
            shouldShowWait = _uiState.value.shouldShowWait + 1
        )}
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            tutorialDialogShown = false
        )}

        if (doNotShowAgain) repo.setDoNotShowAgain()
    }

}