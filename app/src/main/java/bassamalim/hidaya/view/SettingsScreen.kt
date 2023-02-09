package bassamalim.hidaya.view

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.state.SettingsState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.viewmodel.SettingsVM

@Composable
fun SettingsUI(
    nc: NavController = rememberNavController(),
    vm: SettingsVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsState()
    val ctx = LocalContext.current

    MyScaffold(
        stringResource(R.string.settings)
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            MyFatColumn {
                ExpandableCard(
                    R.string.appearance,
                    Modifier.padding(top = 4.dp, bottom = 2.dp)
                ) {
                    AppearanceSettings(ctx, vm.pref)
                }

                ExpandableCard(
                    R.string.extra_notifications,
                    Modifier.padding(vertical = 2.dp)
                ) {
                    ExtraNotificationsSettings(vm, st)
                }

                ExpandableCard(
                    R.string.prayer_time_settings,
                    Modifier.padding(vertical = 2.dp)
                ) {
                    PrayerTimesSettings(vm)
                }
            }
        }
    }
}

@Composable
private fun PrayerTimesSettings(viewModel: SettingsVM) {
    Column(
        Modifier.padding(bottom = 10.dp)
    ) {
        // Calculation method
        ListPref(
            sp = viewModel.pref,
            titleResId = R.string.calculation_method_title,
            pref = Prefs.PrayerTimesCalculationMethod,
            entries = stringArrayResource(R.array.prayer_times_calc_method_entries),
            values = stringArrayResource(R.array.prayer_times_calc_method_values)
        ) {
            viewModel.resetPrayerTimes()
        }

        // Juristic method
        ListPref(
            sp = viewModel.pref,
            titleResId = R.string.juristic_method_title,
            pref = Prefs.PrayerTimesJuristicMethod,
            entries = stringArrayResource(R.array.juristic_method_entries),
            values = stringArrayResource(R.array.juristic_method_values)
        ) {
            viewModel.resetPrayerTimes()
        }

        // High latitude adjustment
        ListPref(
            sp = viewModel.pref,
            titleResId = R.string.high_lat_adjustment_title,
            pref = Prefs.PrayerTimesAdjustment,
            entries = stringArrayResource(R.array.high_lat_adjustment_entries),
            values = stringArrayResource(R.array.high_lat_adjustment_values)
        ) {
            viewModel.resetPrayerTimes()
        }
    }
}

@Composable
private fun ExtraNotificationsSettings(
    vm: SettingsVM,
    st: SettingsState
) {
    val ctx = LocalContext.current

    Column(
        Modifier.padding(bottom = 10.dp)
    ) {
        SwitchPref(
            pref = vm.pref,
            prefObj = Prefs.NotifyExtraNotification(PID.MORNING),
            titleResId = R.string.morning_athkar_title,
            summary = st.morningSummary
        ) { checked ->
            vm.onSwitch(ctx, checked, PID.MORNING)
        }

        SwitchPref(
            pref = vm.pref,
            prefObj = Prefs.NotifyExtraNotification(PID.EVENING),
            titleResId = R.string.evening_athkar_title,
            summary = st.eveningSummary
        ) { checked ->
            vm.onSwitch(ctx, checked, PID.EVENING)
        }

        SwitchPref(
            pref = vm.pref,
            prefObj = Prefs.NotifyExtraNotification(PID.DAILY_WERD),
            titleResId = R.string.daily_werd_title,
            summary = st.werdSummary
        ) { checked ->
            vm.onSwitch(ctx, checked, PID.DAILY_WERD)
        }

        SwitchPref(
            pref = vm.pref,
            prefObj = Prefs.NotifyExtraNotification(PID.FRIDAY_KAHF),
            titleResId = R.string.friday_kahf_title,
            summary = st.kahfSummary
        ) { checked ->
            vm.onSwitch(ctx, checked, PID.FRIDAY_KAHF)
        }
    }
}

@Composable
fun AppearanceSettings(context: Context, pref: SharedPreferences) {
    Column(
        Modifier.padding(bottom = 10.dp)
    ) {
        // Language
        ListPref(
            sp = pref,
            titleResId = R.string.language,
            pref = Prefs.Language,
            iconResId = R.drawable.ic_translation,
            entries = stringArrayResource(R.array.language_entries),
            values = stringArrayResource(R.array.languages_values)
        ) {
            ActivityUtils.restartActivity(context as Activity)
        }

        // Numerals language
        ListPref(
            sp = pref,
            titleResId = R.string.numerals_language,
            pref = Prefs.NumeralsLanguage,
            iconResId = R.drawable.ic_translation,
            entries = stringArrayResource(R.array.numerals_language_entries),
            values = stringArrayResource(R.array.languages_values)
        ) {
            ActivityUtils.restartActivity(context as Activity)
        }

        // Time format
        val timeFormatEntries = stringArrayResource(R.array.time_format_values).map {
            LangUtils.translateNums(context, it)
        }.toTypedArray()
        ListPref(
            sp = pref,
            titleResId = R.string.time_format,
            pref = Prefs.TimeFormat,
            iconResId = R.drawable.ic_time_format,
            entries = timeFormatEntries,
            values = stringArrayResource(R.array.time_format_values)
        ) {
            ActivityUtils.restartActivity(context as Activity)
        }

        // Theme
        ListPref(
            sp = pref,
            titleResId = R.string.theme,
            pref = Prefs.Theme,
            iconResId = R.drawable.ic_theme,
            entries = stringArrayResource(R.array.themes_entries),
            values = stringArrayResource(R.array.theme_values)
        ) {
            ActivityUtils.restartActivity(context as Activity)
        }
    }
}