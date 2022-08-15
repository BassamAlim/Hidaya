package bassamalim.hidaya.fragments

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Message
import android.widget.TimePicker
import androidx.preference.*
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.ID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.other.Utils
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
        var switchP: SwitchPreferenceCompat = findPreference(keyGetter(ID.MORNING))!!
        switchP.summary = Utils.translateNumbers(requireContext(), Utils.formatTime(
            requireContext(), "${pref.getInt(ID.MORNING.toString() + "hour", 5)}:" +
                    "${pref.getInt(ID.MORNING.toString() + "minute", 0)}"
        ), true)

        switchP = findPreference(keyGetter(ID.EVENING))!!
        switchP.summary = Utils.translateNumbers(requireContext(), Utils.formatTime(
            requireContext(), "${pref.getInt(ID.EVENING.toString() + "hour", 16)}:" +
                    "${pref.getInt(ID.EVENING.toString() + "minute", 0)}"
        ), true)

        switchP = findPreference(keyGetter(ID.DAILY_WERD))!!
        switchP.summary = Utils.translateNumbers(requireContext(), Utils.formatTime(
            requireContext(), "${pref.getInt(ID.DAILY_WERD.toString() + "hour", 21)}:" +
                    "${pref.getInt(ID.DAILY_WERD.toString() + "minute", 0)}"
        ), true)

        switchP = findPreference(keyGetter(ID.FRIDAY_KAHF))!!
        switchP.summary = Utils.translateNumbers(requireContext(), Utils.formatTime(
            requireContext(), "${pref.getInt(ID.FRIDAY_KAHF.toString() + "hour", 13)}:" +
                    "${pref.getInt(ID.FRIDAY_KAHF.toString() + "minute", 0)}"
        ), true)
    }

    private fun setListeners() {
        val changeListener = Preference.OnPreferenceChangeListener { _, _ ->
            Utils.refresh(requireActivity())
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

        setSwitchListener(ID.MORNING)
        setSwitchListener(ID.EVENING)
        setSwitchListener(ID.DAILY_WERD)
        setSwitchListener(ID.FRIDAY_KAHF)
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
                pSwitch.summary = Utils.translateNumbers(requireContext(), Utils.formatTime(
                    requireContext(), "$hourOfDay:$minute"
                ), true)

                val editor = pref.edit()
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

    private fun cancelAlarm(id: ID) {
        Utils.cancelAlarm(requireContext(), id)
        val key = keyGetter(id)
        val pSwitch: SwitchPreferenceCompat = findPreference(key)!!
        pSwitch.summary = ""
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