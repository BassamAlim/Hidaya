package bassamalim.hidaya.popups;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

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
import bassamalim.hidaya.databinding.PopupQuranSettingsBinding;

public class QuranSettingsPopup extends AppCompatActivity {

    private PopupQuranSettingsBinding binding;
    private SharedPreferences pref;
    private QuranSettingsPopup.SettingsFragment settingsFragment;
    private int initialTextSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        themeify();
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = PopupQuranSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialTextSize = pref.getInt(getString(R.string.quran_text_size_key), 30);

        binding.executeBtn.setOnClickListener(v -> execute());

        settingsFragment = new SettingsFragment();
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.quran_settings, settingsFragment).commit();
    }

    private void themeify() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString(getString(R.string.theme_key), "ThemeM");

        switch (theme) {
            case "ThemeM":
                setTheme(R.style.RoundedPopupM);
                break;
            case "ThemeL":
                setTheme(R.style.RoundedPopupL);
                break;
        }
    }

    private void execute() {
        if (settingsFragment.textSizeSB.getValue() != initialTextSize) {
            Intent intent = new Intent();
            intent.putExtra("text_size", settingsFragment.textSizeSB.getValue());
            setResult(RESULT_OK, intent);
            finish();
        }
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
