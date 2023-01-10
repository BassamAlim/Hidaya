package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.QuizResultRepo
import bassamalim.hidaya.state.QuizResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuizResultVM @Inject constructor(
    private val repository: QuizResultRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(QuizResultState())
    val uiState = _uiState.asStateFlow()

}