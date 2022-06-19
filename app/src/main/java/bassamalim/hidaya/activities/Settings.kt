package bassamalim.hidaya.activities

import android.R.id
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.ID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.other.Utils
import java.util.*

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        setContentView(R.layout.activity_settings)
        findViewById<View>(id.home).setOnClickListener { onBackPressed() }
        if (savedInstanceState == null) supportFragmentManager.beginTransaction()
            .replace(R.id.settings, SettingsFragment("normal")).commit()
    }

    class SettingsFragment(private val action: String) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            setInitialStates()
            setListeners()
        }

        private fun setInitialStates() {
            val pref: SharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
            var pSwitch: SwitchPreferenceCompat? = findPreference(keyGetter(ID.MORNING))
            assert(pSwitch != null)
            pSwitch!!.summary = pref.getString(
                ID.MORNING.toString() + "text",
                getString(R.string.default_morning_summary)
            )
            pSwitch = findPreference(keyGetter(ID.EVENING))
            assert(pSwitch != null)
            pSwitch!!.summary = pref.getString(
                ID.EVENING.toString() + "text",
                getString(R.string.default_evening_summary)
            )
            pSwitch = findPreference(keyGetter(ID.DAILY_WERD))
            assert(pSwitch != null)
            pSwitch!!.summary = pref.getString(
                ID.DAILY_WERD.toString() + "text",
                getString(R.string.default_werd_summary)
            )
            pSwitch = findPreference(keyGetter(ID.FRIDAY_KAHF))
            assert(pSwitch != null)
            pSwitch!!.summary = pref.getString(
                ID.FRIDAY_KAHF.toString() + "text",
                getString(R.string.default_kahf_summary)
            )
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
            val timePicker = TimePickerDialog(
                context,
                { _: TimePicker?, hourOfDay: Int, minute: Int ->
                    val nums = intArrayOf(hourOfDay, minute)
                    val fixed = formatText(requireContext(), nums)
                    pSwitch.summary = fixed
                    val myPref: SharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val editor: SharedPreferences.Editor = myPref.edit()
                    editor.putInt(id.toString() + "hour", hourOfDay)
                    editor.putInt(id.toString() + "minute", minute)
                    editor.putString(id.toString() + "text", fixed)
                    editor.apply()
                    Alarms(requireContext(), id)
                }, cHour, cMinute, false
            )
            timePicker.setOnCancelListener { setInitialStates() }
            timePicker.setOnDismissListener { setInitialStates() }
            timePicker.setTitle(getString(R.string.time_picker_title))
            timePicker.setButton(
                TimePickerDialog.BUTTON_POSITIVE,
                getString(R.string.select), null as Message?
            )
            timePicker.setButton(
                TimePickerDialog.BUTTON_NEGATIVE,
                getString(R.string.cancel), null as Message?
            )
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

    companion object {
        private fun formatText(context: Context, nums: IntArray): String {
            val locale: String = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                    context.getString(R.string.language_key),
                    context.getString(R.string.default_language)
                )!!
            val result: String
            val map = HashMap<Char, Char>()
            map['0'] = '٠'
            map['1'] = '١'
            map['2'] = '٢'
            map['3'] = '٣'
            map['4'] = '٤'
            map['5'] = '٥'
            map['6'] = '٦'
            map['7'] = '٧'
            map['8'] = '٨'
            map['9'] = '٩'
            map['A'] = 'ص'
            map['P'] = 'م'
            var h = nums[0]
            var m = nums[1].toString()
            var section = context.getString(R.string.at_morning)
            if (h == 0) h = 12 else if (h >= 12) {
                section = context.getString(R.string.at_evening)
                if (h > 12) h -= 12
            }
            if (m.length == 1) {
                if (m[0] == '0') m += '0' else m = "0" + m[0]
            }
            result = "$h:$m $section"
            val translated = StringBuilder()
            for (element in result) {
                if (map.containsKey(element)) {
                    if (locale == "ar") translated.append(map[element]) else translated.append(
                        element
                    )
                } else translated.append(element)
            }
            return translated.toString()
        }
    }
}