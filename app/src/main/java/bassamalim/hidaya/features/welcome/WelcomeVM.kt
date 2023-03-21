package bassamalim.hidaya.features.welcome

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.core.nav.Screen
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