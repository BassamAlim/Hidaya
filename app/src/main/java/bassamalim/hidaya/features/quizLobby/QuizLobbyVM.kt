package bassamalim.hidaya.features.quizLobby

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.features.destinations.QuizUIDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuizLobbyVM @Inject constructor(): ViewModel() {

    fun onStartQuizClick(navigator: DestinationsNavigator) {
        navigator.navigate(QuizUIDestination)
    }

}