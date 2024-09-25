package bassamalim.hidaya.features.books.bookChapters.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.books.bookChapters.domain.BookChaptersDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
    private lateinit var book: Book

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
                title = book.title
            )}
        }
    }

    fun getItems(page: Int): Flow<List<Book.Chapter>> {
        val menuType = MenuType.entries[page]

        return when (menuType) {
            MenuType.FAVORITES -> book.chapters.filter { it.isFavorite }
            else -> book.chapters
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
                newValue = !book.chapters[chapterNum].isFavorite
            )
        }
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}