package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.QuizRepo
import bassamalim.hidaya.state.QuizState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuizVM @Inject constructor(
    private val repository: QuizRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(QuizState())
    val uiState = _uiState.asStateFlow()

}