package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.QuizLobbyRepo
import bassamalim.hidaya.state.QuizLobbyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuizLobbyVM @Inject constructor(
    private val repository: QuizLobbyRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(QuizLobbyState())
    val uiState = _uiState.asStateFlow()

}