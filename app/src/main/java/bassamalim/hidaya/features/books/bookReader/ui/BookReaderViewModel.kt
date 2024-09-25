package bassamalim.hidaya.features.books.bookReader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.books.bookReader.domain.BookReaderDomain
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
class BookReaderViewModel @Inject constructor(
    private val domain: BookReaderDomain,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val bookId = savedStateHandle.get<Int>("book_id") ?: 0
    private val chapterId = savedStateHandle.get<Int>("chapter_id") ?: 0

    private lateinit var language: Language

    private val _uiState = MutableStateFlow(BookReaderUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getTextSize()
    ) { state, textSize -> state.copy(
        textSize = textSize
    )}.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = BookReaderUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()

            _uiState.update { it.copy(
                isLoading = false,
                bookTitle = domain.getBookTitle(bookId, language),
                doors = domain.getDoors(bookId, chapterId).toList()
            )}
        }
    }

    fun onTextSizeChange(textSize: Float) {
        viewModelScope.launch {
            domain.setTextSize(textSize)
        }
    }

}