package bassamalim.hidaya.popups;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.DropDownPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;
import androidx.room.Room;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.databinding.PopupQuranSettingsBinding;

public class QuranSettingsPopup extends AppCompatActivity {

    private PopupQuranSettingsBinding binding;
    private QuranSettingsPopup.SettingsFragment settingsFragment;
    private boolean change = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        themeify();
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = PopupQuranSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.executeBtn.setOnClickListener(v -> execute());

        settingsFragment = new SettingsFragment();
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.quran_settings, settingsFragment).commit();
    }

    private void themeify() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString(getString(R.string.quran_theme_key), "DarkTheme");

        switch (theme) {
            case "DarkTheme":
                setTheme(R.style.RoundedPopupDark);
                break;
            case "LightTheme":
                setTheme(R.style.RoundedPopupLight);
                break;
        }
    }

    private void execute() {
        change = true;

        Intent intent = new Intent();
        intent.putExtra("text_size", settingsFragment.textSizeSeekbar.getValue());
        intent.putExtra("theme", settingsFragment.themesList.getValue());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void restoreState() {
        settingsFragment.textSizeSeekbar.setValue(settingsFragment.initialTextSize);
        settingsFragment.themesList.setValue(settingsFragment.initialTheme);
        settingsFragment.recitersDropdown.setValue(settingsFragment.initialReciter);
        settingsFragment.repeatModeDropdown.setValue(settingsFragment.initialRepeat);
        settingsFragment.stopOnSuraSwitch.setChecked(settingsFragment.initialStopSura);
        settingsFragment.stopOnPageSwitch.setChecked(settingsFragment.initialStopPage);
    }

    @Override
    protected void onDestroy() {
        if (!change)
            restoreState();
        super.onDestroy();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SeekBarPreference textSizeSeekbar;
        private ListPreference themesList;
        private DropDownPreference recitersDropdown;
        private DropDownPreference repeatModeDropdown;
        private SwitchPreference stopOnSuraSwitch;
        private SwitchPreference stopOnPageSwitch;
        private int initialTextSize;
        private String initialTheme;
        private String initialReciter;
        private String initialRepeat;
        private boolean initialStopSura;
        private boolean initialStopPage;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.quran_preferences, rootKey);
            unReserve();

            setupReciters();

            setInitial();
        }

        private void setInitial() {
            initialTextSize = textSizeSeekbar.getValue();
            initialTheme = themesList.getValue();
            initialReciter = recitersDropdown.getValue();
            initialRepeat = repeatModeDropdown.getValue();
            initialStopSura = stopOnSuraSwitch.isChecked();
            initialStopPage = stopOnPageSwitch.isChecked();
        }

        private void unReserve() {
            PreferenceScreen preferenceScreen = findPreference("quran_settings_screen_key");
            assert preferenceScreen != null;
            preferenceScreen.setIconSpaceReserved(false);
            textSizeSeekbar = findPreference(getString(R.string.quran_text_size_key));
            assert textSizeSeekbar != null;
            textSizeSeekbar.setIconSpaceReserved(false);
            themesList = findPreference(getString(R.string.quran_theme_key));
            assert themesList != null;
            themesList.setIconSpaceReserved(false);
            recitersDropdown = findPreference(getString(R.string.aya_reciter_key));
            assert recitersDropdown != null;
            recitersDropdown.setIconSpaceReserved(false);
            repeatModeDropdown = findPreference(getString(R.string.aya_repeat_mode_key));
            assert repeatModeDropdown != null;
            repeatModeDropdown.setIconSpaceReserved(false);
            stopOnSuraSwitch = findPreference(getString(R.string.stop_on_sura_key));
            assert stopOnSuraSwitch != null;
            stopOnSuraSwitch.setIconSpaceReserved(false);
            stopOnPageSwitch = findPreference(getString(R.string.stop_on_page_key));
            assert stopOnPageSwitch != null;
            stopOnPageSwitch.setIconSpaceReserved(false);
        }

        private void setupReciters() {
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
