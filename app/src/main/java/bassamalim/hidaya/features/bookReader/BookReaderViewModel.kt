package bassamalim.hidaya.features.bookReader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            items = repository.getDoors(bookId, chapterId).toList()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(
                textSize = repository.getTextSize()
            )}
        }
    }

    fun onTextSizeChange(textSize: Float) {
        _uiState.update { it.copy(
            textSize = textSize
        )}

        viewModelScope.launch {
            repository.setTextSize(textSize)
        }
    }

}