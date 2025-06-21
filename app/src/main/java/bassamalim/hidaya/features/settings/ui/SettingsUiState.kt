package bassamalim.hidaya.features.settings.ui

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import bassamalim.hidaya.core.models.TimeOfDay

data class SettingsUiState(
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC,
    val timeFormat: TimeFormat = TimeFormat.TWELVE,
    val theme: Theme = Theme.ORIGINAL,
    val devotionalReminderEnabledStatuses: Map<Reminder.Devotional, Boolean> = emptyMap(),
    val devotionalReminderTimes: Map<Reminder.Devotional, TimeOfDay> = emptyMap(),
    val devotionalReminderSummaries: Map<Reminder.Devotional, String> = emptyMap(),
    val isTimePickerShown: Boolean = false,
    val continuousPrayersNotificationEnabled: Boolean = false,
    val prayerTimeCalculatorSettings: PrayerTimeCalculatorSettings =
        PrayerTimeCalculatorSettings(),
    val athanAudioId: Int = 0,
    val morningAndEveningRemembrancesEnabled: Boolean = false,
)
