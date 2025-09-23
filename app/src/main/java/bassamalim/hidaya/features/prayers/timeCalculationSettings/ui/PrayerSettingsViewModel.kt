package bassamalim.hidaya.features.prayers.timeCalculationSettings.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.features.prayers.timeCalculationSettings.domain.PrayerTimeCalculationSettingsDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerSettingsViewModel @Inject constructor(
    private val domain: PrayerTimeCalculationSettingsDomain,
    private val navigator: Navigator
): ViewModel() {

    private val _uiState = MutableStateFlow(PrayerTimeCalculationSettingsUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = PrayerTimeCalculationSettingsUiState()
    )

    fun initializeData() {
        viewModelScope.launch {
            val prayerTimesCalculatorSettings = domain.getPrayerTimesCalculatorSettings().first()

            _uiState.update {
                it.copy(
                    continuousPrayersNotificationEnabled =
                        domain.getContinuousPrayersNotificationEnabled().first(),
                    calculationMethod = prayerTimesCalculatorSettings.calculationMethod,
                    juristicMethod = prayerTimesCalculatorSettings.juristicMethod,
                    highLatitudesAdjustment =
                        prayerTimesCalculatorSettings.highLatitudesAdjustmentMethod
                )
            }
        }
    }

    fun onContinuousNotificationsSwitch(isEnabled: Boolean) {
        _uiState.update { it.copy(
            continuousPrayersNotificationEnabled = isEnabled
        )}
    }

    fun onPrayerTimesCalculationMethodChange(newMethod: PrayerTimeCalculationMethod) {
        _uiState.update { it.copy(
            calculationMethod = newMethod
        )}
    }

    fun onPrayerTimesJuristicMethodChange(newMethod: PrayerTimeJuristicMethod) {
        _uiState.update { it.copy(
            juristicMethod = newMethod
        )}
    }

    fun onPrayerTimesHighLatitudesAdjustmentChange(newMethod: HighLatitudesAdjustmentMethod) {
        _uiState.update { it.copy(
            highLatitudesAdjustment = newMethod
        )}
    }

    fun onSave(context: Context) {
        val state = _uiState.value
        viewModelScope.launch {
            domain.setContinuousPrayersNotificationEnabled(
                enabled = state.continuousPrayersNotificationEnabled,
                context = context
            )
            domain.setPrayerTimesCalculatorSettings(
                PrayerTimeCalculatorSettings(
                    calculationMethod = state.calculationMethod,
                    juristicMethod = state.juristicMethod,
                    highLatitudesAdjustmentMethod = state.highLatitudesAdjustment
                )
            )

            domain.resetPrayerTimes()

            navigator.navigateBackWithResult(data = null)
        }
    }

    fun onDismiss() {
        navigator.popBackStack()
    }

}