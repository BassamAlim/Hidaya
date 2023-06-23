package bassamalim.hidaya.features.settings

import android.app.Activity
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.ui.components.ExpandableCard
import bassamalim.hidaya.core.ui.components.ListPref
import bassamalim.hidaya.core.ui.components.MyFatColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SwitchPref
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun SettingsUI(
    vm: SettingsVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

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
                    AppearanceSettings(activity, vm.sp)
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
            sp = viewModel.sp,
            titleResId = R.string.calculation_method_title,
            pref = Prefs.PrayerTimesCalculationMethod,
            entries = stringArrayResource(R.array.prayer_times_calc_method_entries),
            values = stringArrayResource(R.array.prayer_times_calc_method_values)
        ) { viewModel.onPrayerTimesCalculationMethodCh() }

        // Juristic method
        ListPref(
            sp = viewModel.sp,
            titleResId = R.string.juristic_method_title,
            pref = Prefs.PrayerTimesJuristicMethod,
            entries = stringArrayResource(R.array.juristic_method_entries),
            values = stringArrayResource(R.array.juristic_method_values)
        ) { viewModel.onPrayerTimesJuristicMethodCh() }

        // High latitude adjustment
        ListPref(
            sp = viewModel.sp,
            titleResId = R.string.high_lat_adjustment_title,
            pref = Prefs.PrayerTimesAdjustment,
            entries = stringArrayResource(R.array.high_lat_adjustment_entries),
            values = stringArrayResource(R.array.high_lat_adjustment_values)
        ) { viewModel.onPrayerTimesHighLatAdjustmentCh() }
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
            sp = vm.sp,
            pref = Prefs.NotifyExtraNotification(PID.MORNING),
            titleResId = R.string.morning_athkar_title,
            summary = st.morningSummary
        ) { checked ->
            vm.onSwitch(ctx, checked, PID.MORNING)
        }

        SwitchPref(
            sp = vm.sp,
            pref = Prefs.NotifyExtraNotification(PID.EVENING),
            titleResId = R.string.evening_athkar_title,
            summary = st.eveningSummary
        ) { checked ->
            vm.onSwitch(ctx, checked, PID.EVENING)
        }

        SwitchPref(
            sp = vm.sp,
            pref = Prefs.NotifyExtraNotification(PID.DAILY_WERD),
            titleResId = R.string.daily_werd_title,
            summary = st.werdSummary
        ) { checked ->
            vm.onSwitch(ctx, checked, PID.DAILY_WERD)
        }

        SwitchPref(
            sp = vm.sp,
            pref = Prefs.NotifyExtraNotification(PID.FRIDAY_KAHF),
            titleResId = R.string.friday_kahf_title,
            summary = st.kahfSummary
        ) { checked ->
            vm.onSwitch(ctx, checked, PID.FRIDAY_KAHF)
        }
    }
}

@Composable
fun AppearanceSettings(activity: Activity, pref: SharedPreferences) {
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
            values = Language.values().map { it.name }.toTypedArray()
        ) {
            ActivityUtils.restartActivity(activity)
        }

        // Numerals language
        ListPref(
            sp = pref,
            titleResId = R.string.numerals_language,
            pref = Prefs.NumeralsLanguage,
            iconResId = R.drawable.ic_translation,
            entries = stringArrayResource(R.array.numerals_language_entries),
            values = Language.values().map { it.name }.toTypedArray()
        ) {
            ActivityUtils.restartActivity(activity)
        }

        // Time format
        val timeFormatEntries = stringArrayResource(R.array.time_format_entries).map {
            translateNums(pref, it)
        }.toTypedArray()
        ListPref(
            sp = pref,
            titleResId = R.string.time_format,
            pref = Prefs.TimeFormat,
            iconResId = R.drawable.ic_time_format,
            entries = timeFormatEntries,
            values = TimeFormat.values().map { it.name }.toTypedArray()
        ) {
            ActivityUtils.restartActivity(activity)
        }

        // Theme
        ListPref(
            sp = pref,
            titleResId = R.string.theme,
            pref = Prefs.Theme,
            iconResId = R.drawable.ic_theme,
            entries = stringArrayResource(R.array.themes_entries),
            values = Theme.values().map { it.name }.toTypedArray()
        ) {
            ActivityUtils.restartActivity(activity)
        }
    }
}