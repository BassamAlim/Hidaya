package bassamalim.hidaya.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.BookChapter
import bassamalim.hidaya.repository.BookChaptersRepo
import bassamalim.hidaya.state.BookChaptersState
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

    private val book = repository.getBook(bookId)
    var searchText by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(BookChaptersState(
        title = bookTitle,
        items = getItems(ListType.All),
        favs = repository.getFavs(book)
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(listType: ListType): List<BookChapter> {
        val items = ArrayList<BookChapter>()
        for (i in book.chapters.indices) {
            val chapter = book.chapters[i]
            if (listType == ListType.All ||
                listType == ListType.Favorite && _uiState.value.favs[i] == 1)
                items.add(BookChapter(chapter.chapterId, chapter.chapterTitle))
        }
        return items
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
        val mutableFavs = _uiState.value.favs.toMutableList()
        mutableFavs[itemId] = if (mutableFavs[itemId] == 1) 0 else 1

        _uiState.update { it.copy(
            favs = mutableFavs
        )}

        repository.updateFavorites(bookId, mutableFavs.toList())
    }

    fun onListTypeChange(pageNum: Int) {
        val listType = ListType.values()[pageNum]

        _uiState.update { it.copy(
            listType = listType,
            items = getItems(listType)
        )}
    }

    fun onSearchTextChange(text: String) {
        searchText = text
    }

}