package bassamalim.hidaya.features.bookChapters.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.bookChapters.domain.BookChapter
import bassamalim.hidaya.features.bookChapters.domain.BookChaptersDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookChaptersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: BookChaptersDomain,
    private val navigator: Navigator
): ViewModel() {

    private val bookId = savedStateHandle.get<Int>("book_id")?: 0
    private val bookTitle = savedStateHandle.get<String>("book_title")?: ""

    private val book = domain.getBook(bookId)

    private val _uiState = MutableStateFlow(BookChaptersUiState(
        title = bookTitle
    ))
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getFavs(book)
    ) { state, favs -> state.copy(
        favs = favs
    )}.stateIn(
        initialValue = BookChaptersUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun getItems(page: Int): List<BookChapter> {
        val listType = ListType.entries[page]

        return domain.getItems(
            listType = listType,
            chapters = book.chapters,
            favs = _uiState.value.favs,
            searchText = _uiState.value.searchText
        )
    }

    fun onItemClick(item: BookChapter) {
        navigator.navigate(
            Screen.BookViewer(
                bookId = bookId.toString(),
                bookTitle = item.title,
                chapterId = item.id.toString()
            )
        )
    }

    fun onFavClick(itemId: Int) {
        _uiState.update { it.copy(
            favs = it.favs.toMutableMap().apply {
                this[itemId] = if (this[itemId] == 1) 0 else 1
            }
        )}

        viewModelScope.launch {
            domain.setFavs(
                bookId = bookId,
                favs = _uiState.value.favs.toMap()
            )
        }
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}