package bassamalim.hidaya.activities

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Message
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import java.util.*

class Settings : ComponentActivity() {

    private lateinit var pref: SharedPreferences
    private val morningSummary = mutableStateOf("")
    private val eveningSummary = mutableStateOf("")
    private val werdSummary = mutableStateOf("")
    private val kahfSummary = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        setSummaries()

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    private fun setSummaries() {
        morningSummary.value = LangUtils.translateNums(this, PTUtils.formatTime(
            this, "${PrefUtils.getInt(pref, "${PID.MORNING} hour", 5)}:" +
                    "${PrefUtils.getInt(pref, "${PID.MORNING} minute", 0)}"
        ), true)

        eveningSummary.value = LangUtils.translateNums(this, PTUtils.formatTime(
            this, "${PrefUtils.getInt(pref, "${PID.EVENING} hour", 16)}:" +
                    "${PrefUtils.getInt(pref, "${PID.EVENING} minute", 0)}"
        ), true)

        werdSummary.value = LangUtils.translateNums(this, PTUtils.formatTime(
            this, "${PrefUtils.getInt(pref, "${PID.DAILY_WERD} hour", 21)}:" +
                    "${PrefUtils.getInt(pref, "${PID.DAILY_WERD} minute", 0)}"
        ), true)

        kahfSummary.value = LangUtils.translateNums(this, PTUtils.formatTime(
            this, "${PrefUtils.getInt(pref, "${PID.FRIDAY_KAHF} hour", 13)}:" +
                    "${PrefUtils.getInt(pref, "${PID.FRIDAY_KAHF} minute", 0)}"
        ), true)
    }

    private fun onSwitch(checked: Boolean, pid: PID, summary: MutableState<String>) {
        if (checked) showTimePicker(pid, summary)
        else cancelAlarm(pid, summary)
    }

    private fun showTimePicker(pid: PID, summary: MutableState<String>) {
        val currentTime = Calendar.getInstance()
        val cHour = currentTime[Calendar.HOUR_OF_DAY]
        val cMinute = currentTime[Calendar.MINUTE]

        val timePicker = TimePickerDialog(this,
            { _: TimePicker?, hourOfDay: Int, minute: Int ->
                summary.value = LangUtils.translateNums(this, PTUtils.formatTime(
                    this, "$hourOfDay:$minute"
                ), true)

                pref.edit()
                    .putInt("$pid hour", hourOfDay)
                    .putInt("$pid minute", minute)
                    .apply()

                Alarms(this, pid)
            }, cHour, cMinute, false
        )

        timePicker.setOnCancelListener { setSummaries() }
        timePicker.setOnDismissListener { setSummaries() }
        timePicker.setTitle(getString(R.string.time_picker_title))
        timePicker.setButton(
            TimePickerDialog.BUTTON_POSITIVE, getString(R.string.select), null as Message?
        )
        timePicker.setButton(
            TimePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), null as Message?
        )
        timePicker.setCancelable(true)
        timePicker.show()
    }

    private fun cancelAlarm(pid: PID, summary: MutableState<String>) {
        PTUtils.cancelAlarm(this, pid)
        summary.value = ""
    }

    @Composable
    private fun UI() {
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
                        AppearanceSettings(this@Settings, pref)
                    }

                    ExpandableCard(
                        R.string.extra_notifications,
                        Modifier.padding(vertical = 2.dp)
                    ) {
                        ExtraNotificationsSettings()
                    }

                    ExpandableCard(
                        R.string.prayer_time_settings,
                        Modifier.padding(vertical = 2.dp)
                    ) {
                        PrayerTimesSettings()
                    }
                }
            }
        }
    }

    @Composable
    private fun PrayerTimesSettings() {
        Column(
            Modifier.padding(bottom = 10.dp)
        ) {
            // Calculation method
            ListPref(
                pref = pref,
                titleResId = R.string.calculation_method_title,
                keyResId = R.string.prayer_times_calc_method_key,
                entries = stringArrayResource(R.array.prayer_times_calc_method_entries),
                values = stringArrayResource(R.array.prayer_times_calc_method_values),
                defaultValue = stringResource(R.string.default_prayer_times_calc_method)
            ) {
                val prayerTimes = PTUtils.getTimes(this@Settings)
                if (prayerTimes != null) Alarms(this@Settings, prayerTimes)
            }

            // Juristic method
            ListPref(
                pref = pref,
                titleResId = R.string.juristic_method_title,
                keyResId = R.string.juristic_method_key,
                entries = stringArrayResource(R.array.juristic_method_entries),
                values = stringArrayResource(R.array.juristic_method_values),
                defaultValue = stringResource(R.string.default_juristic_method)
            ) {
                val prayerTimes = PTUtils.getTimes(this@Settings)
                if (prayerTimes != null) Alarms(this@Settings, prayerTimes)
            }

            // High latitude adjustment
            ListPref(
                pref = pref,
                titleResId = R.string.high_lat_adjustment_title,
                keyResId = R.string.high_lat_adjustment_key,
                entries = stringArrayResource(R.array.high_lat_adjustment_entries),
                values = stringArrayResource(R.array.high_lat_adjustment_values),
                defaultValue = stringResource(R.string.default_high_lat_adjustment)
            ) {
                val prayerTimes = PTUtils.getTimes(this@Settings)
                if (prayerTimes != null) Alarms(this@Settings, prayerTimes)
            }
        }
    }

    @Composable
    private fun ExtraNotificationsSettings() {
        Column(
            Modifier.padding(bottom = 10.dp)
        ) {
            SwitchPref(
                pref = pref,
                keyResId = R.string.morning_athkar_key,
                titleResId = R.string.morning_athkar_title,
                summary = morningSummary
            ) { checked ->
                onSwitch(checked, PID.MORNING, morningSummary)
            }

            SwitchPref(
                pref = pref,
                keyResId = R.string.evening_athkar_key,
                titleResId = R.string.evening_athkar_title,
                summary = eveningSummary
            ) { checked ->
                onSwitch(checked, PID.EVENING, eveningSummary)
            }

            SwitchPref(
                pref = pref,
                keyResId = R.string.daily_werd_key,
                titleResId = R.string.daily_werd_title,
                summary = werdSummary
            ) { checked ->
                onSwitch(checked, PID.DAILY_WERD, werdSummary)
            }

            SwitchPref(
                pref = pref,
                keyResId = R.string.friday_kahf_key,
                titleResId = R.string.friday_kahf_title,
                summary = kahfSummary
            ) { checked ->
                onSwitch(checked, PID.FRIDAY_KAHF, kahfSummary)
            }
        }
    }

    companion object {
        @Composable
        fun AppearanceSettings(context: Context, pref: SharedPreferences) {
            Column(
                Modifier.padding(bottom = 10.dp)
            ) {
                // Language
                ListPref(
                    pref = pref,
                    titleResId = R.string.language,
                    keyResId = R.string.language_key,
                    iconResId = R.drawable.ic_translation,
                    entries = stringArrayResource(R.array.language_entries),
                    values = stringArrayResource(R.array.languages_values),
                    defaultValue = stringResource(R.string.default_language)
                ) {
                    ActivityUtils.restartActivity(context as Activity)
                }

                // Numerals language
                ListPref(
                    pref = pref,
                    titleResId = R.string.numerals_language,
                    keyResId = R.string.numerals_language_key,
                    iconResId = R.drawable.ic_translation,
                    entries = stringArrayResource(R.array.numerals_language_entries),
                    values = stringArrayResource(R.array.languages_values),
                    defaultValue = stringResource(R.string.default_language)
                ) {
                    ActivityUtils.restartActivity(context as Activity)
                }

                // Time format
                val timeFormatEntries = stringArrayResource(R.array.time_format_values).map {
                    LangUtils.translateNums(context, it)
                }.toTypedArray()
                ListPref(
                    pref = pref,
                    titleResId = R.string.time_format,
                    keyResId = R.string.time_format_key,
                    iconResId = R.drawable.ic_time_format,
                    entries = timeFormatEntries,
                    values = stringArrayResource(R.array.time_format_values),
                    defaultValue = stringResource(R.string.default_time_format)
                ) {
                    ActivityUtils.restartActivity(context as Activity)
                }

                // Theme
                ListPref(
                    pref = pref,
                    titleResId = R.string.theme,
                    keyResId = R.string.theme_key,
                    iconResId = R.drawable.ic_theme,
                    entries = stringArrayResource(R.array.themes_entries),
                    values = stringArrayResource(R.array.theme_values),
                    defaultValue = stringResource(R.string.default_theme)
                ) {
                    ActivityUtils.restartActivity(context as Activity)
                }
            }
        }
    }

}