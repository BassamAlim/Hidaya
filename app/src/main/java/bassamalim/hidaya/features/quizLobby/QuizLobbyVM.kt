package bassamalim.hidaya.features.quizLobby

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuizLobbyVM @Inject constructor(
    private val navigator: Navigator
): ViewModel() {

    fun onStartQuizClick() {
        navigator.navigate(Screen.Quiz)
    }

}