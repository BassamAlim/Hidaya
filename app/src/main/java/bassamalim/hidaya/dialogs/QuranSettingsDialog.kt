package bassamalim.hidaya.dialogs

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.DialogQuranSettingsBinding
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PrefUtils

class QuranSettingsDialog : AppCompatActivity() {

    private lateinit var binding: DialogQuranSettingsBinding
    private lateinit var pref: SharedPreferences
    private lateinit var radioGroup: RadioGroup
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        themeify()
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogQuranSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRadioGroup()

        binding.executeBtn.setOnClickListener { execute() }

        settingsFragment = SettingsFragment()
        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.quran_settings, settingsFragment).commit()
    }

    private fun initRadioGroup() {
        radioGroup = binding.radioGroup
        if (pref.getString("quran_view_type", "page") == "list") radioGroup.check(R.id.list_view)
        else radioGroup.check(R.id.page_view)
    }

    private fun themeify() {
        when (PrefUtils.getTheme(this, pref)) {
            "ThemeM" -> setTheme(R.style.RoundedDialogM)
            "ThemeR" -> setTheme(R.style.RoundedDialogM)
            else -> setTheme(R.style.RoundedDialogL)
        }
    }

    private fun execute() {
        val viewType =
            if (radioGroup.checkedRadioButtonId == R.id.list_view) "list"
            else "page"

        pref.edit()
            .putString("quran_view_type", viewType)
            .apply()

        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)

        finish()
    }

    override fun onDestroy() {
        execute()
        super.onDestroy()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.quran_preferences, rootKey)

            setupReciters()

            setupRepeatSeekbar()
        }

        private fun setupReciters() {
            val recitersDropdown = findPreference<DropDownPreference>(getString(R.string.aya_reciter_key))!!
            val reciterNames = DBUtils.getDB(requireContext()).ayatRecitersDao().getNames()
            val ids = arrayOfNulls<CharSequence>(reciterNames.size)
            for (i in reciterNames.indices) ids[i] = i.toString()
            recitersDropdown.entries = reciterNames.toTypedArray()
            recitersDropdown.entryValues = ids
        }

        private fun setupRepeatSeekbar() {
            val repeatSB =
                findPreference<SeekBarPreference>(getString(R.string.aya_repeat_key))!!

            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            repeatSB.setSummaryProvider {
                LangUtils.translateNums(
                    requireContext(), pref.getInt(getString(R.string.aya_repeat_key), 1).toString()
                )
            }

            repeatSB.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                    preference, newValue ->
                if (newValue == 11) preference.setSummaryProvider { getString(R.string.infinite) }
                else preference.setSummaryProvider {
                    LangUtils.translateNums(requireContext(), newValue.toString())
                }

                true
            }
        }
    }

}