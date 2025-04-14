package bassamalim.hidaya.features.books.bookChaptersMenu.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.helpers.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.books.bookChaptersMenu.domain.BookChaptersDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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

    private lateinit var language: Language
    private lateinit var book: Flow<Book>

    private val _uiState = MutableStateFlow(BookChaptersUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookChaptersUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()
            book = domain.getBook(bookId, language)

            _uiState.update { it.copy(
                isLoading = false,
                title = book.first().title
            )}
        }
    }

    fun getItems(page: Int): Flow<List<Book.Chapter>> {
        val menuType = MenuType.entries[page]

        val items = when (menuType) {
            MenuType.FAVORITES -> book.map {
                it.chapters.filter { book -> book.isFavorite }
            }
            else -> book.map { it.chapters }
        }

        return if (_uiState.value.searchText.isEmpty()) items
        else items.map {
            it.filter {
                it.title.contains(_uiState.value.searchText, true)
            }
        }
    }

    fun onItemClick(chapter: Book.Chapter) {
        navigator.navigate(
            Screen.BookReader(bookId = bookId.toString(), chapterId = chapter.id.toString())
        )
    }

    fun onFavoriteClick(chapterNum: Int) {
        viewModelScope.launch {
            domain.setFavoriteStatus(
                bookId = bookId,
                chapterNum = chapterNum,
                newValue = !book.first().chapters[chapterNum].isFavorite
            )
        }
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}