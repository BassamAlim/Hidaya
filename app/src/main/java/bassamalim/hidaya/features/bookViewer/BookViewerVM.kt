package bassamalim.hidaya.features.bookViewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.features.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BookViewerVM @Inject constructor(
    private val repository: BookViewerRepo,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val navArgs = savedStateHandle.navArgs<BookViewerNavArgs>()

    private val _uiState = MutableStateFlow(BookViewerState(
        bookTitle = navArgs.bookTitle,
        textSize = repository.getTextSize(),
        items = repository.getDoors(navArgs.bookId, navArgs.chapterId).toList()
    ))
    val uiState = _uiState.asStateFlow()

    fun onTextSizeChange(textSize: Float) {
        _uiState.update { it.copy(
            textSize = textSize
        )}

        repository.updateTextSize(textSize)
    }

}