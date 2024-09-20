package bassamalim.hidaya.features.prayers.extraReminderSettings.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.features.prayers.extraReminderSettings.domain.PrayerExtraReminderSettingsDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerExtraReminderSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    domain: PrayerExtraReminderSettingsDomain,
    private val navigator: Navigator
): ViewModel() {

    private val prayer = Prayer.valueOf(savedStateHandle.get<String>("prayer") ?: "")

    val offsetMin = domain.offsetMin
    lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(PrayerExtraReminderSettingsUiState(
        prayer = prayer,
        prayerName = domain.getPrayerName(prayer)
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()

            _uiState.update { it.copy(
                offset = domain.getOffset(prayer)
            )}
        }
    }

    fun onOffsetChange(offset: Int) {
        _uiState.update { it.copy(
            offset = offset
        )}
    }

    fun onSave() {
        navigator.navigateBackWithResult(
            data = Bundle().apply {
                putInt("offset", _uiState.value.offset)
            }
        )
    }

    fun onDismiss() {
        navigator.navigateBackWithResult(data = null)
    }

}