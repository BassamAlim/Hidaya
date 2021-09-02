package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import java.util.Calendar;
import java.util.HashMap;

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

            SharedPreferences mySharedPref = requireContext().getSharedPreferences(
                    "daily_page_time", MODE_PRIVATE);

            SwitchPreferenceCompat dailyPageSwitch = findPreference(getString(R.string.daily_page_key));
            assert dailyPageSwitch != null;
            dailyPageSwitch.setSummary(mySharedPref.getString("text", "9:00 مساءاً"));

            setListeners();
        }

        private void setListeners() {
            SwitchPreferenceCompat athanSwitch = findPreference(getString(R.string.athan_enable_key));
            assert athanSwitch != null;
            athanSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean on = (Boolean) newValue;
                if (on)
                    new Alarms(getContext());
                else {
                    for (int i = 1; i <= 7; i++) {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), i,
                                new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);

                        AlarmManager am = (AlarmManager) requireContext()
                                .getSystemService(Context.ALARM_SERVICE);
                        am.cancel(pendingIntent);
                    }
                }
                return true;
            });

            SwitchPreferenceCompat dailyPageSwitch = findPreference(getString(R.string.daily_page_key));
            assert dailyPageSwitch != null;
            dailyPageSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean on = (Boolean) newValue;
                if (on) {
                    showTimePicker();
                }
                else {
                    athanSwitch.setSummary("");

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),
                            8, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT
                                    | PendingIntent.FLAG_IMMUTABLE);

                    AlarmManager am = (AlarmManager) requireContext().getApplicationContext()
                            .getSystemService(Context.ALARM_SERVICE);

                    am.cancel(pendingIntent);
                }
                return true;
            });

        }

        private void showTimePicker() {
            SwitchPreferenceCompat dailyPageSwitch = findPreference(getString(R.string.daily_page_key));
            assert dailyPageSwitch != null;

            Calendar currentTime = Calendar.getInstance();
            int cHour = currentTime.get(Calendar.HOUR_OF_DAY);
            int cMinute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                    (view, hourOfDay, minute) -> {

                int[] nums = {hourOfDay, minute};
                String fixed = fixText(nums);

                dailyPageSwitch.setSummary(fixed);

                SharedPreferences myPref = requireContext().getSharedPreferences(
                        "daily_page_time", MODE_PRIVATE);
                SharedPreferences.Editor editor = myPref.edit();
                editor.putInt("hour", hourOfDay);
                editor.putInt("minute", minute);
                editor.putString("text", fixed);
                editor.apply();

                new Alarms(getContext(), "extra_only");

                }, cHour, cMinute, false);

            timePicker.setTitle("اختر وقت اشعار الصفحة اليومية");
            timePicker.setButton(TimePickerDialog.BUTTON_POSITIVE, "حفظ", (Message) null);
            timePicker.setButton(TimePickerDialog.BUTTON_NEGATIVE, "إلغاء", (Message) null);
            timePicker.setCancelable(true);

            timePicker.show();
        }
    }

    private static String fixText(int[] nums) {
        String result;
        HashMap<Character, Character> map = new HashMap<>();
        map.put('0', '٠');
        map.put('1', '١');
        map.put('2', '٢');
        map.put('3', '٣');
        map.put('4', '٤');
        map.put('5', '٥');
        map.put('6', '٦');
        map.put('7', '٧');
        map.put('8', '٨');
        map.put('9', '٩');
        map.put('A', 'ص');
        map.put('P', 'م');

        int h = nums[0];
        String m = String.valueOf(nums[1]);
        String section =  "صباحاً";
        if (h > 12) {
            h -= 12;
            section = "مساءاً";
        }
        if (m.length() == 1) {
            if (m.charAt(0) == '0')
                m += '0';
            else
                m = "0" + m.charAt(0);
        }

        result = h + ":" + m + " " + section;
        StringBuilder translated = new StringBuilder();
        for (int i = 0; i<result.length(); i++) {
            char c = result.charAt(i);
            if (map.containsKey(c))
                translated.append(map.get(c));
            else
                translated.append(c);
        }

        return translated.toString();
    }

}
