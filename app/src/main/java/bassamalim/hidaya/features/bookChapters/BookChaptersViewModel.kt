package bassamalim.hidaya.features.bookChapters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.BookChapter
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookChaptersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BookChaptersRepository,
    private val navigator: Navigator
): ViewModel() {

    private val bookId = savedStateHandle.get<Int>("book_id")?: 0
    private val bookTitle = savedStateHandle.get<String>("book_title")?: ""

    private val book = repository.getBook(bookId)

    private val _uiState = MutableStateFlow(BookChaptersState(
        title = bookTitle,
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(
                language = repository.getLanguage(),
                favs = repository.getFavs(book)
            )}
        }
    }

    fun getItems(page: Int): List<BookChapter> {
        val listType = ListType.entries[page]

        val items = ArrayList<BookChapter>()
        for (i in book.chapters.indices) {
            val chapter = book.chapters[i]
            if (listType == ListType.All ||
                listType == ListType.Favorite && _uiState.value.favs[i] == 1)
                items.add(BookChapter(chapter.chapterId, chapter.chapterTitle))
        }

        return if (_uiState.value.searchText.isEmpty()) items
        else items.filter {
            it.title.contains(_uiState.value.searchText, true)
        }
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
            repository.setFavorites(bookId, _uiState.value.favs.toMap())
        }
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}