package bassamalim.hidaya.features.prayers.settings.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.helpers.Navigator
import bassamalim.hidaya.features.prayers.settings.domain.PrayerSettingsDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: PrayerSettingsDomain,
    private val navigator: Navigator
): ViewModel() {

    private val prayer = Prayer.valueOf(savedStateHandle.get<String>("prayer_name") ?: "")

    lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(PrayerSettingsUiState(
        prayer = prayer,
        prayerName = domain.getPrayerName(prayer)
    ))
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = PrayerSettingsUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()

            _uiState.update { it.copy(
                notificationType = domain.getNotificationType(prayer)
            )}
        }
    }

    fun onNotificationTypeChange(notificationType: NotificationType) {
        _uiState.update { it.copy(
            notificationType = notificationType
        )}
    }

    fun onSave() {
        viewModelScope.launch {
            domain.setNotificationType(_uiState.value.notificationType, prayer)

            navigator.popBackStack()
        }
    }

    fun onDismiss() {
        navigator.popBackStack()
    }

}