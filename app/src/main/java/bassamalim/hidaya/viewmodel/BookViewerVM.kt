package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.BookViewerRepo
import bassamalim.hidaya.state.BookViewerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BookViewerVM @Inject constructor(
    private val repository: BookViewerRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(BookViewerState())
    val uiState = _uiState.asStateFlow()

}