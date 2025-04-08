package bassamalim.hidaya.features.settings.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.models.TimeOfDay
import bassamalim.hidaya.core.utils.LangUtils.translateTimeNums
import bassamalim.hidaya.features.settings.domain.SettingsDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val domain: SettingsDomain
): ViewModel() {

    private var targetReminder: Reminder.Devotional? = null
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
    }.combine(domain.getDevotionReminderEnabledMap()) { state, devotionReminderEnabledMap ->
        state.copy(devotionalReminderEnabledStatuses = devotionReminderEnabledMap)
    }.combine(domain.getDevotionReminderTimeOfDayMap()) { state, devotionReminderTimeOfDayMap ->
        state.copy(
            devotionalReminderTimes = devotionReminderTimeOfDayMap,
            devotionalReminderSummaries = devotionReminderTimeOfDayMap.mapValues {
                if (!state.devotionalReminderEnabledStatuses[it.key]!!) ""
                else formatTime(it.value, state.language, state.numeralsLanguage, state.timeFormat)
            }.toMutableMap()
        )
    }.combine(domain.getPrayerTimesCalculatorSettings()) { state, prayerTimesCalculatorSettings ->
        state.copy(prayerTimeCalculatorSettings = prayerTimesCalculatorSettings)
    }.combine(domain.getAthanAudioId()) { state, athanAudioId ->
        state.copy(athanAudioId = athanAudioId)
    }.combine(domain.getLocation()) { state, location ->
        state.copy(morningAndEveningRemembrancesEnabled = location != null)
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
        if (devotion != Reminder.Devotional.EveningRemembrances
            && devotion != Reminder.Devotional.MorningRemembrances) {
            _uiState.update { it.copy(
                isTimePickerShown = isEnabled
            )}
        }

        if (isEnabled) {
            targetReminder = devotion
            if (devotion == Reminder.Devotional.EveningRemembrances
                || devotion == Reminder.Devotional.MorningRemembrances) {
                viewModelScope.launch {
                    val prayer =
                        if (devotion == Reminder.Devotional.MorningRemembrances) Prayer.FAJR
                        else Prayer.ASR
                    val prayerTime = domain.getPrayerTime(prayer)
                    onTimePicked(
                        hour = prayerTime.get(Calendar.HOUR_OF_DAY),
                        minute = prayerTime.get(Calendar.MINUTE)
                    )
                }
            }
        }
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

    fun onTimePicked(hour: Int, minute: Int) {
        viewModelScope.launch {
            domain.setDevotionReminderEnabled(true, targetReminder!!)
            domain.setDevotionReminderTimeOfDay(
                timeOfDay = TimeOfDay(hour = hour, minute = minute),
                reminder = targetReminder!!
            )

            domain.setAlarm(targetReminder!!)

            targetReminder = null
            _uiState.update { it.copy(
                isTimePickerShown = false
            )}
        }
    }

    fun onTimePickerDismiss() {
        _uiState.update { it.copy(
            isTimePickerShown = false
        )}
    }

    // TODO: fix english numerals with 24 hour format
    private fun formatTime(
        timeOfDay: TimeOfDay,
        language: Language,
        numeralsLanguage: Language,
        timeFormat: TimeFormat
    ): String {
        val string = when (timeFormat) {
            TimeFormat.TWENTY_FOUR -> {
                val hour = String.format(locale = Locale.US, format = "%02d", timeOfDay.hour)
                val minute = String.format(locale = Locale.US, format = "%02d", timeOfDay.minute)
                "$hour:$minute"
            }
            TimeFormat.TWELVE -> {
                var hour = timeOfDay.hour
                val suffix = when (language) {
                    Language.ENGLISH -> { if (hour >= 12) "pm" else "am" }
                    Language.ARABIC -> { if (hour >= 12) "م" else "ص" }
                }
                hour = (hour + 12 - 1) % 12 + 1
                val formattedMinute = String.format(
                    locale = Locale.US,
                    format = "%02d",
                    timeOfDay.minute
                )
                "$hour:$formattedMinute $suffix"
            }
        }

        return translateTimeNums(
            language = language,
            numeralsLanguage = numeralsLanguage,
            string = string,
            removeLeadingZeros = false
        )
    }

}