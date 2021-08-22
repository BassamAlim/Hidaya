package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings, new SettingsFragment()).commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            setListeners();
        }

        private void setListeners() {
            SwitchPreferenceCompat athanSwitch = findPreference(getString(R.string.athan_enable_key));
            assert athanSwitch != null;
            athanSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean on = (Boolean) newValue;
                if (on) {

                }
                else {
                    for (int i = 0; i < 7; i++) {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                MainActivity.getInstance(), i, new Intent(), PendingIntent
                                        .FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        AlarmManager am = (AlarmManager) MainActivity.getInstance()
                                .getSystemService(Context.ALARM_SERVICE);
                        am.cancel(pendingIntent);
                    }
                }
                return true;
            });

        }

    }
}