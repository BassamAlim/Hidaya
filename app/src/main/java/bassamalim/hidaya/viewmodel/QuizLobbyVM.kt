package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class QuizLobbyVM : ViewModel() {

    fun onStartQuizClick(navController: NavController) {
        navController.navigate(Screen.Quiz.route)
    }

}