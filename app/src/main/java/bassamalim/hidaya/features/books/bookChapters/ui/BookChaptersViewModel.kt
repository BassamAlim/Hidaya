package bassamalim.hidaya.features.books.bookChapters.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.books.bookChapters.domain.BookChaptersDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
    private val favoritesFlow = domain.getFavorites(book)

    private val _uiState = MutableStateFlow(BookChaptersUiState(
        title = bookTitle
    ))
    val uiState = _uiState.asStateFlow()

    fun getItems(page: Int): Flow<List<BookChapter>> {
        val listType = ListType.entries[page]

        return favoritesFlow.map { favorites ->
            favorites.filter {
                (listType == ListType.ALL ||
                        listType == ListType.FAVORITES && favorites[it.key]!!)
                        && (_uiState.value.searchText.isEmpty() ||
                        book.chapters[it.key].title
                            .contains(_uiState.value.searchText, true))
            }.map { favorite ->
                BookChapter(
                    id = favorite.key,
                    title = book.chapters[favorite.key].title,
                    isFavorite = favorite.value
                )
            }
        }
    }

    fun onItemClick(chapter: BookChapter) {
        navigator.navigate(
            Screen.BookViewer(
                bookId = bookId.toString(),
                bookTitle = chapter.title,
                chapterId = chapter.id.toString()
            )
        )
    }

    fun onFavoriteClick(chapterNum: Int) {
        viewModelScope.launch {
            domain.setFavoriteStatus(
                bookId = bookId,
                chapterNum = chapterNum,
                newValue = !_uiState.value.favs[chapterNum]!!
            )
        }
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}