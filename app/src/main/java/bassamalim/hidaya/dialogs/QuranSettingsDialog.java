package bassamalim.hidaya.dialogs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.DropDownPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.room.Room;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.databinding.DialogQuranSettingsBinding;

public class QuranSettingsDialog extends AppCompatActivity {

    private DialogQuranSettingsBinding binding;
    private SharedPreferences pref;
    private RadioGroup radioGroup;
    private QuranSettingsDialog.SettingsFragment settingsFragment;
    private String initialViewType;
    private int initialTextSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        themeify();
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogQuranSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialViewType = pref.getString("quran_view_type", "page");
        initialTextSize = pref.getInt(getString(R.string.quran_text_size_key), 30);

        initRadioGroup();

        binding.executeBtn.setOnClickListener(v -> execute());

        settingsFragment = new SettingsFragment();
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.quran_settings, settingsFragment).commit();
    }

    private void initRadioGroup() {
        radioGroup = binding.radioGroup;

        if (initialViewType.equals("page"))
            radioGroup.check(R.id.page_view);
        else
            radioGroup.check(R.id.list_view);
    }

    private void themeify() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString(getString(R.string.theme_key),
                getString(R.string.default_theme));

        switch (theme) {
            case "ThemeM":
                setTheme(R.style.RoundedDialogM);
                break;
            case "ThemeL":
                setTheme(R.style.RoundedDialogL);
                break;
        }
    }

    private void execute() {
        String viewType = radioGroup.getCheckedRadioButtonId() == R.id.list_view ? "list" : "page";
        if (!viewType.equals(initialViewType)) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("quran_view_type", viewType);
            editor.apply();
        }

        if (settingsFragment.textSizeSB.getValue() != initialTextSize
                || !viewType.equals(initialViewType)) {
            Intent intent = new Intent();
            if (radioGroup.getCheckedRadioButtonId() == R.id.list_view)
                intent.putExtra("view_type", "list");
            else
                intent.putExtra("view_type", "page");
            intent.putExtra("text_size", settingsFragment.textSizeSB.getValue());

            setResult(RESULT_OK, intent);
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        execute();
        super.onDestroy();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SeekBarPreference textSizeSB;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.quran_preferences, rootKey);

            textSizeSB = findPreference(getString(R.string.quran_text_size_key));

            setupReciters();
        }

        private void setupReciters() {
            DropDownPreference recitersDropdown =
                    findPreference(getString(R.string.aya_reciter_key));
            assert recitersDropdown != null;
            CharSequence[] reciterNames = getReciterNames().toArray(new CharSequence[0]);
            CharSequence[] ids = new CharSequence[reciterNames.length];
            for (int i = 0; i < reciterNames.length; i++)
                ids[i] = String.valueOf(i);
            recitersDropdown.setEntries(reciterNames);
            recitersDropdown.setEntryValues(ids);
        }

        private List<String> getReciterNames() {
            return Room.databaseBuilder(requireContext().getApplicationContext(), AppDatabase.class,
                    "HidayaDB").createFromAsset("databases/HidayaDB.db")
                    .allowMainThreadQueries().build().ayatRecitersDao().getNames();
        }
    }
}
