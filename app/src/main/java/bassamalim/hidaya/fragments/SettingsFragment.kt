package bassamalim.hidaya.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.os.Message
import android.widget.TimePicker
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.ID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.other.Utils
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private var action: String = "normal"

    companion object {
        fun newInstance(action: String): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            args.putString("action", action)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        action = arguments?.getString("aciton", "")!!
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setInitialStates()

        setListeners()
    }

    private fun setInitialStates() {
        val pref: SharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(requireContext())

        var pSwitch: SwitchPreferenceCompat = findPreference(keyGetter(ID.MORNING))!!
        pSwitch.summary = formatText(requireContext(), intArrayOf(
            pref.getInt(ID.MORNING.toString() + "hour", 5),
            pref.getInt(ID.MORNING.toString() + "minute", 0)))

        pSwitch = findPreference(keyGetter(ID.EVENING))!!
        pSwitch.summary = formatText(requireContext(), intArrayOf(
            pref.getInt(ID.EVENING.toString() + "hour", 16),
            pref.getInt(ID.EVENING.toString() + "minute", 0)))

        pSwitch = findPreference(keyGetter(ID.DAILY_WERD))!!
        pSwitch.summary = formatText(requireContext(), intArrayOf(
            pref.getInt(ID.DAILY_WERD.toString() + "hour", 21),
            pref.getInt(ID.DAILY_WERD.toString() + "minute", 0)))

        pSwitch = findPreference(keyGetter(ID.FRIDAY_KAHF))!!
        pSwitch.summary = formatText(requireContext(), intArrayOf(
            pref.getInt(ID.FRIDAY_KAHF.toString() + "hour", 13),
            pref.getInt(ID.FRIDAY_KAHF.toString() + "minute", 0)))
    }

    private fun setListeners() {
        setSwitchListener(ID.MORNING)
        setSwitchListener(ID.EVENING)
        setSwitchListener(ID.DAILY_WERD)
        setSwitchListener(ID.FRIDAY_KAHF)

        val languages: ListPreference = findPreference(getString(R.string.language_key))!!
        languages.setOnPreferenceChangeListener { _, newValue ->
            if (!newValue.equals(PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString(requireContext().getString(R.string.language_key), getString(R.string.default_language)))) {
                setLocale(newValue as String)
                Utils.refresh(requireActivity())
            }
            true
        }
        if (action == "initial") languages.summaryProvider = null

        val numeralsLanguages: ListPreference = findPreference(getString(R.string.numerals_language_key))!!
        numeralsLanguages.setOnPreferenceChangeListener { _, newValue ->
            if (!newValue.equals(PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString(requireContext().getString(R.string.numerals_language_key), getString(R.string.default_language))))
                Utils.refresh(requireActivity())
            true
        }
        if (action == "initial") numeralsLanguages.summaryProvider = null

        val timeFormat: ListPreference = findPreference(getString(R.string.time_format_key))!!
        timeFormat.setOnPreferenceChangeListener { _, newValue ->
            if (!newValue.equals(PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString(requireContext().getString(R.string.time_format_key), getString(R.string.default_time_format))))
                Utils.refresh(requireActivity())
            true
        }
        if (action == "initial") timeFormat.summaryProvider = null

        val themes: ListPreference = findPreference(getString(R.string.theme_key))!!
        themes.setOnPreferenceChangeListener { _, newValue ->
            if (!newValue.equals(PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString(requireContext().getString(R.string.theme_key), getString(R.string.default_theme))))
                Utils.refresh(requireActivity())
            true
        }
        if (action == "initial") themes.summaryProvider = null
    }

    private fun setSwitchListener(id: ID) {
        val key = keyGetter(id)
        val pSwitch: SwitchPreferenceCompat = findPreference(key)!!

        pSwitch.setOnPreferenceChangeListener { _, newValue ->
            val on = newValue as Boolean
            if (on) showTimePicker(id) else cancelAlarm(id)
            true
        }
    }

    private fun showTimePicker(id: ID) {
        val key = keyGetter(id)
        val pSwitch: SwitchPreferenceCompat = findPreference(key)!!

        val currentTime = Calendar.getInstance()
        val cHour = currentTime[Calendar.HOUR_OF_DAY]
        val cMinute = currentTime[Calendar.MINUTE]

        val timePicker = TimePickerDialog(context,
            { _: TimePicker?, hourOfDay: Int, minute: Int ->
                val nums = intArrayOf(hourOfDay, minute)
                pSwitch.summary = formatText(requireContext(), nums)

                val myPref: SharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                val editor: SharedPreferences.Editor = myPref.edit()
                editor.putInt(id.toString() + "hour", hourOfDay)
                editor.putInt(id.toString() + "minute", minute)
                editor.apply()

                Alarms(requireContext(), id)
            }, cHour, cMinute, false
        )

        timePicker.setOnCancelListener { setInitialStates() }
        timePicker.setOnDismissListener { setInitialStates() }

        timePicker.setTitle(getString(R.string.time_picker_title))
        timePicker.setButton(TimePickerDialog.BUTTON_POSITIVE, getString(R.string.select), null as Message?)
        timePicker.setButton(TimePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), null as Message?)
        timePicker.setCancelable(true)

        timePicker.show()
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = resources

        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun cancelAlarm(id: ID) {
        Utils.cancelAlarm(requireContext(), id)
        val key = keyGetter(id)
        val pSwitch: SwitchPreferenceCompat = findPreference(key)!!
        pSwitch.summary = ""
    }

    private fun formatText(context: Context, nums: IntArray): String {
        var hour = nums[0]
        var minute = nums[1].toString()
        var postfix = context.getString(R.string.at_morning)

        if (hour == 0) hour = 12
        else if (hour >= 12) {
            postfix = context.getString(R.string.at_evening)
            if (hour > 12) hour -= 12
        }

        if (minute.length == 1) minute = "0" + minute[0]

        val str = Utils.translateNumbers(context, "$hour:$minute", false)

        return "$str $postfix"
    }

    private fun keyGetter(id: ID): String {
        return when (id) {
            ID.MORNING -> getString(R.string.morning_athkar_key)
            ID.EVENING -> getString(R.string.evening_athkar_key)
            ID.DAILY_WERD -> getString(R.string.daily_werd_key)
            ID.FRIDAY_KAHF -> getString(R.string.friday_kahf_key)
            else -> ""
        }
    }
}