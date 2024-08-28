package bassamalim.hidaya.features.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.onboarding.domain.OnboardingDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val domain: OnboardingDomain,
    private val navigator: Navigator
): ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onLanguageChange(language: Language) {
        viewModelScope.launch {
            domain.setLanguage(language)
        }
    }

    fun onNumeralsLanguageChange(language: Language) {
        viewModelScope.launch {
            domain.setNumeralsLanguage(language)
        }
    }

    fun onTimeFormatChange(timeFormat: TimeFormat) {
        viewModelScope.launch {
            domain.setTimeFormat(timeFormat)
        }
    }

    fun onThemeChange(theme: Theme) {
        viewModelScope.launch {
            domain.setTheme(theme)
        }
    }

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