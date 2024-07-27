package bassamalim.hidaya.features.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.onboarding.domain.OnboardingDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val domain: OnboardingDomain,
    private val navigator: Navigator
): ViewModel() {

    fun onSaveClick() {
        navigator.navigate(Screen.Locator("initial")) {
            popUpTo(Screen.Welcome.route) {
                inclusive = true
            }
        }

        viewModelScope.launch {
            domain.unsetFirstTime()
        }
    }

}