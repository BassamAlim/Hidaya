package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.nav.Screen
import bassamalim.hidaya.repository.WelcomeRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WelcomeVM @Inject constructor(
    private val repo: WelcomeRepo
): ViewModel() {

    val pref = repo.pref

    fun save(navController: NavController) {
        repo.unsetFirstTime()

        navController.navigate(Screen.Locator("initial").route) {
            popUpTo(Screen.Welcome.route) {
                inclusive = true
            }
        }
    }

}