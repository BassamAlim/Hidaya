package bassamalim.hidaya.features.settings.ui

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.ui.components.ExpandableCard
import bassamalim.hidaya.core.ui.components.MyFatColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.TimePickerDialog
import bassamalim.hidaya.core.utils.LangUtils.translateNums

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    MyScaffold(title = stringResource(R.string.settings)) { padding ->
        Box(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            MyFatColumn {
                ExpandableCard(
                    title = stringResource(R.string.appearance),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                    expandedContent = {
                        AppearanceSettings(
                            selectedLanguage = state.language,
                            onLanguageChange = {
                                viewModel.onLanguageChange(newLanguage = it, activity = activity)
                            },
                            selectedNumeralsLanguage = state.numeralsLanguage,
                            onNumeralsLanguageChange = viewModel::onNumeralsLanguageChange,
                            selectedTimeFormat = state.timeFormat,
                            onTimeFormatChange = viewModel::onTimeFormatChange,
                            selectedTheme = state.theme,
                            onThemeChange = viewModel::onThemeChange,
                            numeralsLanguage = state.numeralsLanguage
                        )
                    }
                )

                ExpandableCard(
                    title = stringResource(R.string.extra_notifications),
                    modifier = Modifier.padding(vertical = 2.dp),
                    expandedContent = {
                        DevotionReminderSettings(
                            devotionReminderEnabledStatuses =
                            state.devotionalReminderEnabledStatuses,
                            devotionReminderSummaries = state.devotionalReminderSummaries,
                            onDevotionReminderSwitch = viewModel::onDevotionReminderSwitch
                        )
                    }
                )

                ExpandableCard(
                    title = stringResource(R.string.prayer_time_settings),
                    modifier = Modifier.padding(vertical = 2.dp),
                    expandedContent = {
                        PrayerTimesSettings(
                            calculationMethod =
                            state.prayerTimeCalculatorSettings.calculationMethod,
                            onCalculationMethodChange =
                            viewModel::onPrayerTimesCalculationMethodChange,
                            juristicMethod = state.prayerTimeCalculatorSettings.juristicMethod,
                            onJuristicMethodChange = viewModel::onPrayerTimesJuristicMethodChange,
                            highLatitudesAdjustment =
                            state.prayerTimeCalculatorSettings.highLatitudesAdjustmentMethod,
                            onHighLatitudesAdjustmentChange =
                            viewModel::onPrayerTimesHighLatitudesAdjustmentChange
                        )
                    }
                )

                ExpandableCard(
                    title = stringResource(R.string.athan_settings),
                    modifier = Modifier.padding(vertical = 2.dp),
                    expandedContent = {
                        AthanSettings(
                            athanAudioId = state.athanAudioId,
                            onAthanAudioIdChange = viewModel::onAthanAudioIdChange
                        )
                    }
                )
            }
        }
    }

    if (state.isTimePickerShown) {
        TimePickerDialog(
            initialHour = viewModel.timePickerInitialHour,
            initialMinute = viewModel.timePickerInitialMinute,
            onConfirm = viewModel::onTimePicked,
            onDismiss = viewModel::onTimePickerDismiss
        )
    }
}

@Composable
fun AppearanceSettings(
    selectedLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    selectedNumeralsLanguage: Language,
    onNumeralsLanguageChange: (Language) -> Unit,
    selectedTimeFormat: TimeFormat,
    onTimeFormatChange: (TimeFormat) -> Unit,
    selectedTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    numeralsLanguage: Language
) {
    Column(
        Modifier.padding(bottom = 10.dp)
    ) {
        // Language
        MenuSetting(
            selection = selectedLanguage,
            items = Language.entries.toTypedArray(),
            entries = stringArrayResource(R.array.language_entries),
            title = stringResource(R.string.language),
            icon = R.drawable.ic_translation,
            onSelection = onLanguageChange
        )

        // Numerals language
        MenuSetting(
            selection = selectedNumeralsLanguage,
            items = Language.entries.toTypedArray(),
            entries = stringArrayResource(R.array.numerals_language_entries),
            title = stringResource(R.string.numerals_language),
            icon = R.drawable.ic_translation,
            onSelection = onNumeralsLanguageChange
        )

        // Time format
        MenuSetting(
            selection = selectedTimeFormat,
            items = TimeFormat.entries.toTypedArray(),
            entries = stringArrayResource(R.array.time_format_entries).map {
                translateNums(numeralsLanguage = numeralsLanguage, string = it)
            }.toTypedArray(),
            title = stringResource(R.string.time_format),
            icon = R.drawable.ic_time_format,
            onSelection = onTimeFormatChange
        )

        // Theme
        MenuSetting(
            selection = selectedTheme,
            items = Theme.entries.toTypedArray(),
            entries = stringArrayResource(R.array.themes_entries),
            title = stringResource(R.string.theme),
            icon = R.drawable.ic_theme,
            onSelection = onThemeChange
        )
    }
}

@Composable
private fun DevotionReminderSettings(
    devotionReminderEnabledStatuses: Map<Reminder.Devotional, Boolean>,
    devotionReminderSummaries: Map<Reminder.Devotional, String>,
    onDevotionReminderSwitch: (Reminder.Devotional, Boolean) -> Unit,
) {
    val titles = linkedMapOf(  // Preserve order
        Reminder.Devotional.MorningRemembrances to stringResource(R.string.morning_remembrance_title),
        Reminder.Devotional.EveningRemembrances to stringResource(R.string.evening_remembrance_title),
        Reminder.Devotional.DailyWerd to stringResource(R.string.daily_werd_title),
        Reminder.Devotional.FridayKahf to stringResource(R.string.friday_kahf_title)
    )

    Column(
        Modifier.padding(bottom = 10.dp)
    ) {
        titles.forEach { (reminder, title) ->
            SwitchSetting(
                value = devotionReminderEnabledStatuses[reminder]!!,
                title = title,
                summary = devotionReminderSummaries[reminder]!!,
                onSwitch = { onDevotionReminderSwitch(reminder, it) }
            )
        }
    }
}

@Composable
private fun PrayerTimesSettings(
    calculationMethod: PrayerTimeCalculationMethod,
    onCalculationMethodChange: (PrayerTimeCalculationMethod) -> Unit,
    juristicMethod: PrayerTimeJuristicMethod,
    onJuristicMethodChange: (PrayerTimeJuristicMethod) -> Unit,
    highLatitudesAdjustment: HighLatitudesAdjustmentMethod,
    onHighLatitudesAdjustmentChange: (HighLatitudesAdjustmentMethod) -> Unit
) {
    Column(
        Modifier.padding(bottom = 10.dp)
    ) {
        // Calculation method
        MenuSetting(
            selection = calculationMethod,
            items = PrayerTimeCalculationMethod.entries.toTypedArray(),
            entries = stringArrayResource(R.array.prayer_times_calc_method_entries),
            title = stringResource(R.string.calculation_method_title),
            onSelection = onCalculationMethodChange
        )

        // Juristic method
        MenuSetting(
            selection = juristicMethod,
            items = PrayerTimeJuristicMethod.entries.toTypedArray(),
            entries = stringArrayResource(R.array.juristic_method_entries),
            title = stringResource(R.string.juristic_method_title),
            onSelection = onJuristicMethodChange
        )

        // High latitude adjustment
        MenuSetting(
            selection = highLatitudesAdjustment,
            items = HighLatitudesAdjustmentMethod.entries.toTypedArray(),
            entries = stringArrayResource(R.array.high_lat_adjustment_entries),
            title = stringResource(R.string.high_lat_adjustment_title),
            onSelection = onHighLatitudesAdjustmentChange
        )
    }
}

@Composable
private fun AthanSettings(athanAudioId: Int, onAthanAudioIdChange: (Int) -> Unit) {
    Column(
        Modifier.padding(bottom = 10.dp)
    ) {
        MenuSetting(
            selection = athanAudioId,
            items = stringArrayResource(R.array.athan_voices_entries)
                .mapIndexed { i, _ -> i+1 }.toTypedArray(),
            entries = stringArrayResource(R.array.athan_voices_entries),
            title = stringResource(R.string.athan_voice),
            icon = Icons.Default.Campaign,
            onSelection = onAthanAudioIdChange
        )
    }
}