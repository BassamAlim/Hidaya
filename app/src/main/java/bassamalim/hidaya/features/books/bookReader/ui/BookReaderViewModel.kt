package bassamalim.hidaya.features.books.bookReader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.features.books.bookReader.domain.BookReaderDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookReaderViewModel @Inject constructor(
    private val domain: BookReaderDomain,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val bookId = savedStateHandle.get<Int>("book_id") ?: 0
    private val bookTitle = savedStateHandle.get<String>("book_title") ?: ""
    private val chapterId = savedStateHandle.get<Int>("chapter_id") ?: 0

    private val _uiState = MutableStateFlow(
        BookReaderUiState(
        bookTitle = bookTitle,
        items = domain.getDoors(bookId, chapterId).toList()
    )
    )
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getTextSize()
    ) { state, textSize -> state.copy(
        textSize = textSize
    )}.stateIn(
        initialValue = BookReaderUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun onTextSizeChange(textSize: Float) {
        viewModelScope.launch {
            domain.setTextSize(textSize)
        }
    }

}