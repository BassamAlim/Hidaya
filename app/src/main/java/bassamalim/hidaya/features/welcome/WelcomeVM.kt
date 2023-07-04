package bassamalim.hidaya.features.welcome

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WelcomeVM @Inject constructor(
    private val repo: WelcomeRepo,
    private val navigator: Navigator
): ViewModel() {

    val pref = repo.pref

    fun save() {
        navigator.navigate(Screen.Locator("initial")) {
            popUpTo(Screen.Welcome.route) {
                inclusive = true
            }
        }

        repo.unsetFirstTime()
    }

}