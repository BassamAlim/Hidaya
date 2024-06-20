package bassamalim.hidaya.features.onboarding

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repo: OnboardingRepository,
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