package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.BookChaptersRepo
import bassamalim.hidaya.state.BookChapterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BookChaptersVM @Inject constructor(
    private val repository: BookChaptersRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(BookChapterState())
    val uiState = _uiState.asStateFlow()

}