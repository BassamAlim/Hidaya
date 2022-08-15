package bassamalim.hidaya.dialogs

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.DialogQuranSettingsBinding
import bassamalim.hidaya.other.Utils

class QuranSettingsDialog : AppCompatActivity() {

    private lateinit var binding: DialogQuranSettingsBinding
    private lateinit var pref: SharedPreferences
    private lateinit var radioGroup: RadioGroup
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var initialViewType: String
    private var initialTextSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        themeify()
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogQuranSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialViewType = pref.getString("quran_view_type", "page")!!
        initialTextSize = pref.getInt(getString(R.string.quran_text_size_key), 30)

        initRadioGroup()

        binding.executeBtn.setOnClickListener { execute() }
        settingsFragment = SettingsFragment()

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.quran_settings, settingsFragment).commit()
    }

    private fun initRadioGroup() {
        radioGroup = binding.radioGroup
        if (initialViewType == "page") radioGroup.check(R.id.page_view)
        else radioGroup.check(R.id.list_view)
    }

    private fun themeify() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        val theme: String? = pref.getString(
            getString(R.string.theme_key),
            getString(R.string.default_theme)
        )
        when (theme) {
            "ThemeM" -> setTheme(R.style.RoundedDialogM)
            "ThemeR" -> setTheme(R.style.RoundedDialogM)
            else -> setTheme(R.style.RoundedDialogL)
        }
    }

    private fun execute() {
        val viewType =
            if (radioGroup.checkedRadioButtonId == R.id.list_view) "list"
            else "page"
        if (viewType != initialViewType) {
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putString("quran_view_type", viewType)
            editor.apply()
        }

        if (settingsFragment.textSizeSB!!.value != initialTextSize
            || viewType != initialViewType) {
            val intent = Intent()
            if (radioGroup.checkedRadioButtonId == R.id.list_view)
                intent.putExtra("view_type", "list")
            else
                intent.putExtra("view_type", "page")
            intent.putExtra("text_size", settingsFragment.textSizeSB!!.value)

            setResult(Activity.RESULT_OK, intent)
        }

        finish()
    }

    override fun onDestroy() {
        execute()
        super.onDestroy()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        var textSizeSB: SeekBarPreference? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.quran_preferences, rootKey)

            textSizeSB = findPreference(getString(R.string.quran_text_size_key))

            setupReciters()
        }

        private fun setupReciters() {
            val recitersDropdown: DropDownPreference = findPreference(getString(R.string.aya_reciter_key))!!
            val reciterNames : List<String?> = getReciterNames()
            val ids = arrayOfNulls<CharSequence>(reciterNames.size)
            for (i in reciterNames.indices) ids[i] = i.toString()
            recitersDropdown.entries = reciterNames.toTypedArray()
            recitersDropdown.entryValues = ids
        }

        private fun getReciterNames(): List<String?> {
            return Utils.getDB(requireContext()).ayatRecitersDao().getNames()
        }
    }

}