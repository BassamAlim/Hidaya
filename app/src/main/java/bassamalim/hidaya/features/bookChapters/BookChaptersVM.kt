package bassamalim.hidaya.features.bookChapters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.BookChapter
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BookChaptersVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BookChaptersRepo,
    private val navigator: Navigator
): ViewModel() {

    private val bookId = savedStateHandle.get<Int>("book_id")?: 0
    private val bookTitle = savedStateHandle.get<String>("book_title")?: ""

    private val book = repository.getBook(bookId)

    private val _uiState = MutableStateFlow(BookChaptersState(
        title = bookTitle,
        favs = repository.getFavs(book)
    ))
    val uiState = _uiState.asStateFlow()

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
                bookId.toString(),
                item.title,
                item.id.toString()
            )
        )
    }

    fun onFavClick(itemId: Int) {
        _uiState.update { it.copy(
            favs = _uiState.value.favs.toMutableList().apply {
                this[itemId] = if (this[itemId] == 1) 0 else 1
            }
        )}

        repository.updateFavorites(bookId, _uiState.value.favs.toList())
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}