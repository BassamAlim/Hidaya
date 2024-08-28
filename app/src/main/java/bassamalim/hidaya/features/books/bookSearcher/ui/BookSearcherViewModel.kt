package bassamalim.hidaya.features.books.bookSearcher.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.books.bookSearcher.domain.BookSearcherDomain
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
class BookSearcherViewModel @Inject constructor(
    private val domain: BookSearcherDomain
): ViewModel() {

    private var highlightColor: Color? = null
    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(BookSearcherUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getBookSelections(),
        domain.getMaxMatches()
    ) { state, bookSelections, maxMatches -> state.copy(
        bookSelections = bookSelections,
        maxMatches = maxMatches
    )}.stateIn(
        initialValue = BookSearcherUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    init {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()

            _uiState.update { it.copy(
                bookTitles = domain.getBookTitles(language),
                filtered = it.bookSelections.containsValue(false)
            )}
        }
    }

    fun onSearch(highlightColor: Color) {
        this.highlightColor = highlightColor

        viewModelScope.launch {
            _uiState.update { it.copy(
                matches = domain.search(
                    searchText = it.searchText,
                    bookSelections = it.bookSelections,
                    maxMatches = it.maxMatches,
                    highlightColor = highlightColor
                )
            )}
        }
    }

    fun onFilterClick() {
        _uiState.update { it.copy(
            filterDialogShown = true
        )}
    }

    fun onFilterDialogDismiss(selections: Map<Int, Boolean>) {
        val changed = selections != _uiState.value.bookSelections

        viewModelScope.launch {
            domain.setBookSelections(selections)

            _uiState.update { it.copy(
                filterDialogShown = false,
                filtered = selections.containsValue(false)
            )}

            if (changed) {
                highlightColor?.let {
                    domain.search(
                        searchText = _uiState.value.searchText,
                        bookSelections = selections,
                        maxMatches = _uiState.value.maxMatches,
                        highlightColor = it
                    )
                }  // re-search if already searched
            }
        }
    }

    fun onMaxMatchesIndexChange(newValue: Int) {
        viewModelScope.launch {
            domain.setMaxMatches(newValue)

            highlightColor?.let {
                domain.search(
                    searchText = _uiState.value.searchText,
                    bookSelections = _uiState.value.bookSelections,
                    maxMatches = _uiState.value.maxMatches,
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