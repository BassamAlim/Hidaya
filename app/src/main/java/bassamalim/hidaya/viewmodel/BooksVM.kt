package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.BooksRepo
import bassamalim.hidaya.state.BooksState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BooksVM @Inject constructor(
    private val repository: BooksRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(BooksState())
    val uiState = _uiState.asStateFlow()

}