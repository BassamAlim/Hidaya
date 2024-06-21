package bassamalim.hidaya.features.books

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.data.database.dbs.BooksDB
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.other.Global
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    app: Application,
    private val repository: BooksRepository,
    private val navigator: Navigator
): AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(BooksState(
        items = repository.getBooks()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(
                language = repository.getLanguage(),
                tutorialDialogShown = repository.getShowTutorial()
            )}
        }
    }

    fun onStart() {
        _uiState.update { it.copy(
            downloadStates = getDownloadStates()
        )}
    }

    fun onFabClick() {
        navigator.navigate(Screen.BookSearcher)
    }

    private fun getDownloadStates(): ArrayList<DownloadState> {
        val downloadStates = ArrayList<DownloadState>()
        for (book in _uiState.value.items) {
            downloadStates.add(
                if (repository.isDownloaded(book.id)) {
                    if (repository.isDownloading(book.id)) DownloadState.Downloading
                    else DownloadState.Downloaded
                }
                else DownloadState.NotDownloaded
            )
        }
        return downloadStates
    }

    private fun download(item: BooksDB) {
        _uiState.update { it.copy(
            downloadStates = it.downloadStates.toMutableList().apply {
                this[item.id] = DownloadState.Downloading
            }
        )}

        val downloadTask = repository.download(item)
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

    fun onItemClick(item: BooksDB) {
        if (_uiState.value.downloadStates[item.id] == DownloadState.NotDownloaded) download(item)
        else if (_uiState.value.downloadStates[item.id] == DownloadState.Downloaded) {
            navigator.navigate(
                Screen.BookChapters(
                    bookId = item.id.toString(),
                    bookTitle =
                        if (_uiState.value.language == Language.ENGLISH) item.titleEn
                        else item.title
                )
            )
        }
        else showWaitMassage()
    }

    private fun showWaitMassage() {
        _uiState.update { it.copy(
            shouldShowWait = it.shouldShowWait + 1
        )}
    }

    fun onDownloadButtonClick(item: BooksDB) {
        val state =
            if (_uiState.value.downloadStates.isEmpty()) DownloadState.NotDownloaded
            else _uiState.value.downloadStates[item.id]

        if (state == DownloadState.Downloaded) {
            _uiState.update { it.copy(
                downloadStates = it.downloadStates.toMutableList().apply {
                    this[item.id] = DownloadState.NotDownloaded
                }
            )}

            repository.deleteBook(item.id)
        }
        else download(item)
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            tutorialDialogShown = false
        )}

        if (doNotShowAgain) {
            viewModelScope.launch {
                repository.setDoNotShowAgain()
            }
        }
    }

}