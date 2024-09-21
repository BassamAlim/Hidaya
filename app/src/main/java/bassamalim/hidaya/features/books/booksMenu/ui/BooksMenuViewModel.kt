package bassamalim.hidaya.features.books.booksMenu.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.data.dataSources.room.entities.Book
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.books.booksMenu.domain.BooksMenuDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksMenuViewModel @Inject constructor(
    app: Application,
    private val domain: BooksMenuDomain,
    private val navigator: Navigator
): AndroidViewModel(app) {

    private lateinit var language: Language

    private val _uiState = MutableStateFlow(BooksMenuUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = BooksMenuUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()

            _uiState.update { it.copy(
                books = domain.getBooks(),
                tutorialDialogShown = domain.getShowTutorial()
            )}
        }
    }

    fun onStart() {
        _uiState.update { it.copy(
            downloadStates = domain.getDownloadStates(books = it.books)
        )}
    }

    fun onFabClick() {
        navigator.navigate(Screen.BookSearcher)
    }

    fun onItemClick(item: Book) {
        when (_uiState.value.downloadStates[item.id]!!) {
            DownloadState.NOT_DOWNLOADED -> {
                _uiState.update { it.copy(
                    downloadStates = it.downloadStates.toMutableMap().apply {
                        this[item.id] = DownloadState.DOWNLOADING
                    }
                )}

                domain.download(
                    bookId = item.id,
                    onDownloadedCallback = {
                        _uiState.update { it.copy(
                            downloadStates = it.downloadStates.toMutableMap().apply {
                                this[item.id] = DownloadState.DOWNLOADED
                            }
                        )}
                    }
                )
            }
            DownloadState.DOWNLOADED -> {
                navigator.navigate(
                    Screen.BookChapters(
                        bookId = item.id.toString(),
                        bookTitle =
                            if (language == Language.ENGLISH) item.titleEn
                            else item.titleAr
                    )
                )
            }
            DownloadState.DOWNLOADING -> {
                _uiState.update { it.copy(
                    shouldShowWait = it.shouldShowWait + 1
                )}
            }
        }
    }

    fun onDownloadButtonClick(item: Book) {
        when (_uiState.value.downloadStates[item.id]!!) {
            DownloadState.NOT_DOWNLOADED -> {
                _uiState.update { it.copy(
                    downloadStates = it.downloadStates.toMutableMap().apply {
                        this[item.id] = DownloadState.DOWNLOADING
                    }
                )}

                domain.download(
                    bookId = item.id,
                    onDownloadedCallback = {
                        _uiState.update { it.copy(
                            downloadStates = it.downloadStates.toMutableMap().apply {
                                this[item.id] = DownloadState.DOWNLOADED
                            }
                        )}
                    }
                )
            }
            DownloadState.DOWNLOADED -> {
                _uiState.update { it.copy(
                    downloadStates = it.downloadStates.toMutableMap().apply {
                        this[item.id] = DownloadState.NOT_DOWNLOADED
                    }
                )}

                domain.deleteBook(item.id)
            }
            DownloadState.DOWNLOADING -> {
                _uiState.update { it.copy(
                    shouldShowWait = it.shouldShowWait + 1
                )}
            }
        }
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            tutorialDialogShown = false
        )}

        viewModelScope.launch {
            domain.handleTutorialDialogDismiss(doNotShowAgain)
        }
    }

}