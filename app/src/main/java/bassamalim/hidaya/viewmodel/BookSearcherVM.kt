package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.BookSearcherRepo
import bassamalim.hidaya.state.BookSearcherState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BookSearcherVM @Inject constructor(
    private val repository: BookSearcherRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(BookSearcherState())
    val uiState = _uiState.asStateFlow()

}