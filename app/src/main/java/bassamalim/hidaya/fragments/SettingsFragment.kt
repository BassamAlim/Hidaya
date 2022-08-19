package bassamalim.hidaya.fragments

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Message
import android.widget.TimePicker
import androidx.preference.*
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.LangUtils
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private var initial = false
    private lateinit var pref: SharedPreferences

    companion object {
        fun newInstance(initial: Boolean = false): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            args.putBoolean("initial", initial)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initial = arguments?.getBoolean("initial", false)!!
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        setPreferencesFromResource(R.xml.preferences, rootKey)

        setInitialStates()
        setListeners()
    }

    private fun setInitialStates() {
        var switchP: SwitchPreferenceCompat = findPreference(keyGetter(PID.MORNING))!!
        switchP.summary = LangUtils.translateNumbers(requireContext(), PTUtils.formatTime(
            requireContext(), "${pref.getInt(PID.MORNING.toString() + "hour", 5)}:" +
                    "${pref.getInt(PID.MORNING.toString() + "minute", 0)}"
        ), true)

        switchP = findPreference(keyGetter(PID.EVENING))!!
        switchP.summary = LangUtils.translateNumbers(requireContext(), PTUtils.formatTime(
            requireContext(), "${pref.getInt(PID.EVENING.toString() + "hour", 16)}:" +
                    "${pref.getInt(PID.EVENING.toString() + "minute", 0)}"
        ), true)

        switchP = findPreference(keyGetter(PID.DAILY_WERD))!!
        switchP.summary = LangUtils.translateNumbers(requireContext(), PTUtils.formatTime(
            requireContext(), "${pref.getInt(PID.DAILY_WERD.toString() + "hour", 21)}:" +
                    "${pref.getInt(PID.DAILY_WERD.toString() + "minute", 0)}"
        ), true)

        switchP = findPreference(keyGetter(PID.FRIDAY_KAHF))!!
        switchP.summary = LangUtils.translateNumbers(requireContext(), PTUtils.formatTime(
            requireContext(), "${pref.getInt(PID.FRIDAY_KAHF.toString() + "hour", 13)}:" +
                    "${pref.getInt(PID.FRIDAY_KAHF.toString() + "minute", 0)}"
        ), true)
    }

    private fun setListeners() {
        val changeListener = Preference.OnPreferenceChangeListener { _, _ ->
            ActivityUtils.restartActivity(requireActivity())
            true
        }

        var listP: ListPreference = findPreference(getString(R.string.language_key))!!
        listP.onPreferenceChangeListener = changeListener

        listP = findPreference(getString(R.string.numerals_language_key))!!
        listP.onPreferenceChangeListener = changeListener

        listP = findPreference(getString(R.string.time_format_key))!!
        listP.onPreferenceChangeListener = changeListener

        listP = findPreference(getString(R.string.theme_key))!!
        listP.onPreferenceChangeListener = changeListener

        setSwitchListener(PID.MORNING)
        setSwitchListener(PID.EVENING)
        setSwitchListener(PID.DAILY_WERD)
        setSwitchListener(PID.FRIDAY_KAHF)
    }

    private fun setSwitchListener(pid: PID) {
        val key = keyGetter(pid)
        val pSwitch: SwitchPreferenceCompat = findPreference(key)!!

        pSwitch.setOnPreferenceChangeListener { _, newValue ->
            val on = newValue as Boolean
            if (on) showTimePicker(pid) else cancelAlarm(pid)
            true
        }
    }

    private fun showTimePicker(pid: PID) {
        val key = keyGetter(pid)
        val pSwitch: SwitchPreferenceCompat = findPreference(key)!!

        val currentTime = Calendar.getInstance()
        val cHour = currentTime[Calendar.HOUR_OF_DAY]
        val cMinute = currentTime[Calendar.MINUTE]

        val timePicker = TimePickerDialog(context,
            { _: TimePicker?, hourOfDay: Int, minute: Int ->
                pSwitch.summary = LangUtils.translateNumbers(requireContext(), PTUtils.formatTime(
                    requireContext(), "$hourOfDay:$minute"
                ), true)

                val editor = pref.edit()
                editor.putInt(pid.toString() + "hour", hourOfDay)
                editor.putInt(pid.toString() + "minute", minute)
                editor.apply()

                Alarms(requireContext(), pid)
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

    private fun cancelAlarm(pid: PID) {
        PTUtils.cancelAlarm(requireContext(), pid)
        val key = keyGetter(pid)
        val pSwitch: SwitchPreferenceCompat = findPreference(key)!!
        pSwitch.summary = ""
    }

    private fun keyGetter(pid: PID): String {
        return when (pid) {
            PID.MORNING -> getString(R.string.morning_athkar_key)
            PID.EVENING -> getString(R.string.evening_athkar_key)
            PID.DAILY_WERD -> getString(R.string.daily_werd_key)
            PID.FRIDAY_KAHF -> getString(R.string.friday_kahf_key)
            else -> ""
        }
    }
}