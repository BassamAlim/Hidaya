package bassamalim.hidaya.features.settings.ui

import android.app.Activity
import android.app.TimePickerDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.enums.Reminder
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
    private var timePickerReminder: Reminder.Devotional? = null
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
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = SettingsUiState()
    )

    fun onLanguageChange(newLanguage: Language, activity: Activity) {
        viewModelScope.launch {
            domain.setLanguage(newLanguage)
            domain.restartActivity(activity)
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

    fun onDevotionReminderSwitch(devotion: Reminder.Devotional, isEnabled: Boolean) {
        _uiState.update { it.copy(
            isTimePickerShown = isEnabled
        )}

        if (isEnabled) timePickerReminder = devotion
        else {
            viewModelScope.launch {
                domain.setDevotionReminderEnabled(false, devotion)
            }

            domain.cancelAlarm(devotion)
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
            domain.setDevotionReminderEnabled(true, timePickerReminder!!)
            domain.setDevotionReminderTimeOfDay(
                timeOfDay = TimeOfDay(hour = hour, minute = minute),
                reminder = timePickerReminder!!
            )

            domain.setAlarm(timePickerReminder!!)

            timePickerReminder = null
        }
    }

    fun onTimePickerDismiss() {
        _uiState.update { it.copy(
            isTimePickerShown = false
        )}
    }

    private fun formatTime(timeOfDay: TimeOfDay): String {
        val string = when (_uiState.value.timeFormat) {
            TimeFormat.TWENTY_FOUR -> {
                val hour = timeOfDay.hour
                val minute = String.format("%02d", timeOfDay.minute)
                "$hour:$minute"
            }
            TimeFormat.TWELVE -> {
                val hour = if (timeOfDay.hour == 0) 12 else timeOfDay.hour % 12
                val minute = String.format("%02d", timeOfDay.minute)
                val amPm = if (timeOfDay.hour >= 12) "pm" else "am"
                "$hour:$minute $amPm"
            }
        }
        return translateNums(
            numeralsLanguage = _uiState.value.numeralsLanguage,
            string = string,
            isTime = true
        )
    }

}