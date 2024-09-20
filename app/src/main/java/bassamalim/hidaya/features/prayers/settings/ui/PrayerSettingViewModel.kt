package bassamalim.hidaya.features.prayers.settings.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.features.prayers.settings.domain.PrayerSettingsDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerSettingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    domain: PrayerSettingsDomain,
    private val navigator: Navigator
): ViewModel() {

    private val prayer = Prayer.valueOf(savedStateHandle.get<String>("prayer") ?: "")

    lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(PrayerSettingUiState(
        prayer = prayer,
        prayerName = domain.getPrayerName(prayer)
    ))
    val uiState = _uiState.asStateFlow()

    val notificationTypeOptions = listOf(
        Pair(R.string.athan_speaker, R.drawable.ic_speaker),
        Pair(R.string.enable_notification, R.drawable.ic_sound),
        Pair(R.string.silent_notification, R.drawable.ic_silent),
        Pair(R.string.disable_notification, R.drawable.ic_block)
    )

    init {
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
        val prayerSettings = PrayerSettings(notificationType = uiState.value.notificationType)

        navigator.navigateBackWithResult(
            data = Bundle().apply {
                putParcelable("prayer_settings", prayerSettings)
            }
        )
    }

    fun onDismiss() {
        navigator.navigateBackWithResult(
            data = null
        )
    }

}