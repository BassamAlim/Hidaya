package bassamalim.hidaya.features.bookChapters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.BookChapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BookChaptersVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BookChaptersRepo
): ViewModel() {

    private val bookId = savedStateHandle.get<Int>("book_id")?: 0
    private val bookTitle = savedStateHandle.get<String>("book_title")?: ""

    private var listType = ListType.All
    private val book = repository.getBook(bookId)
    var searchText by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(
        BookChaptersState(
        title = bookTitle,
        favs = repository.getFavs(book)
    )
    )
    val uiState = _uiState.asStateFlow()

    private fun getItems(listType: ListType): List<BookChapter> {
        val items = ArrayList<BookChapter>()
        for (i in book.chapters.indices) {
            val chapter = book.chapters[i]
            if (listType == ListType.All ||
                listType == ListType.Favorite && _uiState.value.favs[i] == 1)
                items.add(BookChapter(chapter.chapterId, chapter.chapterTitle))
        }

        return if (searchText.isNotEmpty())
            items.filter { it.title.contains(searchText, true) }
        else items
    }

    fun onItemClick(item: BookChapter, navController: NavController) {
        navController.navigate(
            Screen.BookViewer(
                bookId.toString(),
                item.title,
                item.id.toString()
            ).route
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

    fun onListTypeChange(pageNum: Int, currentPage: Int) {
        if (pageNum != currentPage) return

        listType = ListType.values()[pageNum]

        _uiState.update { it.copy(
            items = getItems(listType)
        )}
    }

    fun onSearchTextChange(text: String) {
        searchText = text

        getItems(listType)
    }

}