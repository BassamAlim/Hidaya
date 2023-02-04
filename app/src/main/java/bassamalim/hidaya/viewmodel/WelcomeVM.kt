package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.repository.WelcomeRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WelcomeVM @Inject constructor(
    private val repository: WelcomeRepo
): ViewModel() {

    fun save(navController: NavController) {
        repository.unsetFirstTime()

        navController.navigate(
            Screen.Locator(
                "initial"
            ).route
        ) {
            popUpTo(Screen.Welcome.route) {
                inclusive = true
            }
        }
    }

}