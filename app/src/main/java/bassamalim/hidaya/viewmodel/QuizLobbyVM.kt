package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuizLobbyVM @Inject constructor(): ViewModel() {

    fun onStartQuizClick(navController: NavController) {
        navController.navigate(Screen.Quiz.route)
    }

}