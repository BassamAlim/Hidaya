package bassamalim.hidaya.features.bookReader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BookReaderViewModel @Inject constructor(
    private val repository: BookReaderRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val bookId = savedStateHandle.get<Int>("book_id") ?: 0
    val bookTitle = savedStateHandle.get<String>("book_title") ?: ""
    private val chapterId = savedStateHandle.get<Int>("chapter_id") ?: 0

    private val _uiState = MutableStateFlow(
        BookReaderState(
            textSize = repository.getTextSize(),
            items = repository.getDoors(bookId, chapterId).toList()
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onTextSizeChange(textSize: Float) {
        _uiState.update { it.copy(
            textSize = textSize
        )}

        repository.updateTextSize(textSize)
    }

}