package bassamalim.hidaya.features.welcome

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.features.destinations.LocatorUIDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WelcomeVM @Inject constructor(
    private val repo: WelcomeRepo
): ViewModel() {

    val pref = repo.pref

    fun save(navigator: DestinationsNavigator) {
        repo.unsetFirstTime()

        navigator.navigate(
            LocatorUIDestination(
                type = "initial"
            )
        )
        // TODO
//        navController.navigate(Screen.Locator("initial").route) {
//            popUpTo(Screen.Welcome.route) {
//                inclusive = true
//            }
//        }
    }

}