package bassamalim.hidaya.features.settings.ui

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import bassamalim.hidaya.core.models.TimeOfDay

data class SettingsUiState(
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC,
    val timeFormat: TimeFormat = TimeFormat.TWELVE,
    val theme: Theme = Theme.DARK,
    val devotionReminderEnabledMap: Map<PID, Boolean> = emptyMap(),
    val devotionReminderTimeOfDayMap: Map<PID, TimeOfDay> = emptyMap(),
    val devotionReminderSummaryMap: Map<PID, String> = emptyMap(),
    val isTimePickerShown: Boolean = false,
    val prayerTimeCalculatorSettings: PrayerTimeCalculatorSettings =
        PrayerTimeCalculatorSettings(),
    val athanId: Int = 0,
)
