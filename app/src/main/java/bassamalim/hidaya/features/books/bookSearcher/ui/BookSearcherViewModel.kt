package bassamalim.hidaya.features.books.bookSearcher.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.books.bookSearcher.domain.BookSearcherDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSearcherViewModel @Inject constructor(
    private val domain: BookSearcherDomain,
    private val navigator: Navigator
): ViewModel() {

    private var highlightColor: Color? = null
    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(BookSearcherUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getBookSelections(language),
        domain.getMaxMatches()
    ) { state, bookSelections, maxMatches -> state.copy(
        bookSelections = bookSelections,
        maxMatches = maxMatches,
        filtered = bookSelections.containsValue(false)
    )}.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = BookSearcherUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()

            _uiState.update { it.copy(
                isLoading = false,
                bookTitles = domain.getBookTitles(language)
            )}
        }
    }

    fun onStart() {
        highlightColor?.let {
            viewModelScope.launch {
                domain.search(
                    searchText = _uiState.value.searchText,
                    bookSelections = _uiState.value.bookSelections,
                    maxMatches = _uiState.value.maxMatches,
                    language = language,
                    highlightColor = it
                )
            }
        }  // re-search if already searched
    }

    fun onSearch(highlightColor: Color) {
        this.highlightColor = highlightColor

        viewModelScope.launch {
            _uiState.update { it.copy(
                matches = domain.search(
                    searchText = it.searchText,
                    bookSelections = it.bookSelections,
                    maxMatches = it.maxMatches,
                    language = language,
                    highlightColor = highlightColor
                )
            )}
        }
    }

    fun onFilterClick() {
        navigator.navigate(Screen.BooksMenuFilter)
    }

    fun onMaxMatchesIndexChange(newValue: Int) {
        viewModelScope.launch {
            domain.setMaxMatches(newValue)

            highlightColor?.let {
                domain.search(
                    searchText = _uiState.value.searchText,
                    bookSelections = _uiState.value.bookSelections,
                    maxMatches = _uiState.value.maxMatches,
                    language = language,
                    highlightColor = it
                )
            }  // re-search if already searched
        }
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}