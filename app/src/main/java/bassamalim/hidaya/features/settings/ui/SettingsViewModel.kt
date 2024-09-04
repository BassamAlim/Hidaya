package bassamalim.hidaya.features.settings.ui

import android.app.TimePickerDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.models.TimeOfDay
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.settings.domain.SettingsDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val domain: SettingsDomain
): ViewModel() {

    private var timePicker: TimePickerDialog? = null
    private var timePickerPid: PID? = null
    var timePickerInitialHour: Int = 0
        private set
    var timePickerInitialMinute: Int = 0
        private set

    private val _uiState = MutableStateFlow(SettingsUiState())
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
    }.combine(
        domain.getDevotionReminderEnabledMap()
    ) { state, devotionReminderEnabledMap ->
        state.copy(devotionalReminderEnabledStatuses = devotionReminderEnabledMap)
    }.combine(
        domain.getDevotionReminderTimeOfDayMap()
    ) { state, devotionReminderTimeOfDayMap ->
        state.copy(
            devotionalReminderTimes = devotionReminderTimeOfDayMap,
            devotionalReminderSummaries = devotionReminderTimeOfDayMap.mapValues {
                if (!state.devotionalReminderEnabledStatuses[it.key]!!) ""
                else formatTime(it.value)
            }.toMutableMap()
        )
    }.combine(
        domain.getPrayerTimesCalculatorSettings()
    ) { state, prayerTimesCalculatorSettings ->
        state.copy(prayerTimeCalculatorSettings = prayerTimesCalculatorSettings)
    }.combine(
        domain.getAthanAudioId()
    ) { state, athanAudioId ->
        state.copy(athanAudioId = athanAudioId)
    }.stateIn(
        initialValue = SettingsUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    fun onLanguageChange(newLanguage: Language) {
        viewModelScope.launch {
            domain.setLanguage(newLanguage)
        }
    }

    fun onNumeralsLanguageChange(newLanguage: Language) {
        viewModelScope.launch {
            domain.setNumeralsLanguage(newLanguage)
        }
    }

    fun onTimeFormatChange(newTimeFormat: TimeFormat) {
        viewModelScope.launch {
            domain.setTimeFormat(newTimeFormat)
        }
    }

    fun onThemeChange(newTheme: Theme) {
        viewModelScope.launch {
            domain.setTheme(newTheme)
        }
    }

    fun onDevotionReminderSwitch(pid: PID, isEnabled: Boolean) {
        _uiState.update { it.copy(
            isTimePickerShown = true
        )}

        if (isEnabled) timePickerPid = pid
        else {
            viewModelScope.launch {
                domain.setDevotionReminderEnabled(false, pid)
            }

            domain.cancelAlarm(pid)
        }
    }

    fun onPrayerTimesCalculationMethodChange(newMethod: PrayerTimeCalculationMethod) {
        viewModelScope.launch {
            domain.setPrayerTimeCalculationMethod(newMethod)
            domain.resetPrayerTimes()
        }
    }

    fun onPrayerTimesJuristicMethodChange(newMethod: PrayerTimeJuristicMethod) {
        viewModelScope.launch {
            domain.setPrayerTimeJuristicMethod(newMethod)
            domain.resetPrayerTimes()
        }
    }

    fun onPrayerTimesHighLatitudesAdjustmentChange(newMethod: HighLatitudesAdjustmentMethod) {
        viewModelScope.launch {
            domain.setHighLatitudesAdjustmentMethod(newMethod)
            domain.resetPrayerTimes()
        }
    }

    fun onAthanAudioIdChange(newValue: Int) {
        viewModelScope.launch {
            domain.setAthanAudioId(newValue)
        }
    }

    fun assignTimePicker(timePicker: TimePickerDialog) {
        this.timePicker = timePicker
    }

    fun onTimePicked(hour: Int, minute: Int) {
        viewModelScope.launch {
            domain.setDevotionReminderEnabled(true, timePickerPid!!)
            domain.setDevotionReminderTimeOfDay(
                timeOfDay = TimeOfDay(hour = hour, minute = minute),
                pid = timePickerPid!!
            )

            domain.setAlarm(timePickerPid!!)

            timePickerPid = null
        }
    }

    fun onTimePickerDismiss() {
        _uiState.update { it.copy(
            isTimePickerShown = false
        )}
    }

    private fun formatTime(timeOfDay: TimeOfDay): String {
        val formatted = "${timeOfDay.hour.toString().format("%02d")}:" +
                timeOfDay.minute.toString().format("%02d")
        return translateNums(
            numeralsLanguage = _uiState.value.numeralsLanguage,
            string = formatted,
            timeFormat = true
        )
    }

}