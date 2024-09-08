package bassamalim.hidaya.features.onboarding.ui

import android.app.Activity
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val domain: OnboardingDomain,
    private val navigator: Navigator
): ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getLanguage(),
        domain.getNumeralsLanguage(),
        domain.getTimeFormat(),
        domain.getTheme()
    ) { state, language, numeralsLanguage, timeFormat, theme ->
        state.copy(
            language = language,
            numeralsLanguage = numeralsLanguage,
            timeFormat = timeFormat,
            theme = theme
        )
    }.stateIn(
        initialValue = OnboardingUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun onLanguageChange(language: Language, activity: Activity) {
        viewModelScope.launch {
            domain.setLanguage(language)
            domain.restartActivity(activity)
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
            popUpTo(Screen.Onboarding.route) {
                inclusive = true
            }
        }

        viewModelScope.launch {
            domain.unsetFirstTime()
        }
    }

}