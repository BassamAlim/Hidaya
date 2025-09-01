package bassamalim.hidaya.features.quiz.lobby.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.helpers.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.quiz.lobby.domain.QuizLobbyDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizLobbyViewModel @Inject constructor(
    private val domain: QuizLobbyDomain,
    private val navigator: Navigator
): ViewModel() {

    private val _uiState = MutableStateFlow(QuizLobbyUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QuizLobbyUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = false,
                quizCategories = domain.getQuizCategories()
            )}
        }
    }

    fun onStartQuizClick() {
        navigator.navigate(Screen.QuizTest())
    }

    fun onCategoryClick(category: String) {
        navigator.navigate(Screen.QuizTest(category = category))

        domain.trackQuizCategoryViewed(category)
    }

}