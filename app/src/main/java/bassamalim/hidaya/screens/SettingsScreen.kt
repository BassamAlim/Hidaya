package bassamalim.hidaya.screens

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.os.Message
import android.widget.TimePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import bassamalim.hidaya.ui.components.CategoryTitle
import bassamalim.hidaya.ui.components.ListPref
import bassamalim.hidaya.ui.components.MyHorizontalDivider
import bassamalim.hidaya.ui.components.SwitchPref
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import java.util.*

class SettingsScreen(
    private val context: Context
) {
    
    private val pref = PreferenceManager.getDefaultSharedPreferences(context)
    private val morningSummary = mutableStateOf("")
    private val eveningSummary = mutableStateOf("")
    private val werdSummary = mutableStateOf("")
    private val kahfSummary = mutableStateOf("")

    init {
        setSummaries()
    }

    private fun setSummaries() {
        morningSummary.value = LangUtils.translateNums(context, PTUtils.formatTime(
            context, "${pref.getInt("${PID.MORNING} hour", 5)}:" +
                    "${pref.getInt("${PID.MORNING} minute", 0)}"
        ), true)

        eveningSummary.value = LangUtils.translateNums(context, PTUtils.formatTime(
            context, "${pref.getInt("${PID.EVENING} hour", 16)}:" +
                    "${pref.getInt("${PID.EVENING} minute", 0)}"
        ), true)

        werdSummary.value = LangUtils.translateNums(context, PTUtils.formatTime(
            context, "${pref.getInt("${PID.DAILY_WERD} hour", 21)}:" +
                    "${pref.getInt("${PID.DAILY_WERD} minute", 0)}"
        ), true)

        kahfSummary.value = LangUtils.translateNums(context, PTUtils.formatTime(
            context, "${pref.getInt("${PID.FRIDAY_KAHF} hour", 13)}:" +
                    "${pref.getInt("${PID.FRIDAY_KAHF} minute", 0)}"
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

        val timePicker = TimePickerDialog(context,
            { _: TimePicker?, hourOfDay: Int, minute: Int ->
                summary.value = LangUtils.translateNums(context, PTUtils.formatTime(
                    context, "$hourOfDay:$minute"
                ), true)

                pref.edit()
                    .putInt("$pid hour", hourOfDay)
                    .putInt("$pid minute", minute)
                    .apply()

                Alarms(context, pid)
            }, cHour, cMinute, false
        )

        timePicker.setOnCancelListener { setSummaries() }
        timePicker.setOnDismissListener { setSummaries() }
        timePicker.setTitle(context.getString(R.string.time_picker_title))
        timePicker.setButton(
            TimePickerDialog.BUTTON_POSITIVE, context.getString(R.string.select), null as Message?
        )
        timePicker.setButton(
            TimePickerDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel), null as Message?
        )
        timePicker.setCancelable(true)
        timePicker.show()
    }

    private fun cancelAlarm(pid: PID, summary: MutableState<String>) {
        PTUtils.cancelAlarm(context, pid)
        summary.value = ""
    }

    @Composable
    fun InitialSettingsUI() {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CategoryTitle(titleResId = R.string.appearance)

            AppearanceSettings()

            MyHorizontalDivider()

            CategoryTitle(R.string.extra_notifications)

            ExtraNotificationsSettings()
        }
    }

    @Composable
    fun AppearanceSettings() {
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

    @Composable
    fun ExtraNotificationsSettings() {
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

}