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
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
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



            setInitial();

            setListeners();
        }

        private void setInitial() {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
            SwitchPreferenceCompat pSwitch;

            pSwitch = findPreference(getString(R.string.morning_athkar_key));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(8 + "text", "٩:٠٠ مساءاً"));

            pSwitch = findPreference(getString(R.string.night_athkar_key));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(9 + "text", "٥:٠٠ صباحاً"));

            pSwitch = findPreference(getString(R.string.daily_page_key));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(10 + "text", "٤:٠٠ مساءاًً"));
        }

        private void setListeners() {
            SwitchPreferenceCompat pSwitch = findPreference(getString(R.string.athan_enable_key));
            assert pSwitch != null;
            pSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
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
            setSwitchListener(getString(R.string.morning_athkar_key));
            setSwitchListener(getString(R.string.night_athkar_key));
            setSwitchListener(getString(R.string.daily_page_key));
        }

        private void setSwitchListener(String key) {
            SwitchPreferenceCompat pSwitch = findPreference(key);
            assert pSwitch != null;

            pSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean on = (Boolean) newValue;
                if (on)
                    showTimePicker(key);
                else
                    cancelAlarm(key);
                return true;
            });
        }

        private void showTimePicker(String key) {
            SwitchPreferenceCompat pSwitch = findPreference(key);
            assert pSwitch != null;

            Calendar currentTime = Calendar.getInstance();
            int cHour = currentTime.get(Calendar.HOUR_OF_DAY);
            int cMinute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                    (view, hourOfDay, minute) -> {

                int[] nums = {hourOfDay, minute};
                String fixed = fixText(nums);

                pSwitch.setSummary(fixed);

                SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(requireContext());
                SharedPreferences.Editor editor = myPref.edit();
                editor.putInt(key + "hour", hourOfDay);
                editor.putInt(key + "minute", minute);
                editor.putString(key + "text", fixed);
                editor.apply();

                new Alarms(getContext(), "extra_only");

                }, cHour, cMinute, false);

            timePicker.setTitle("اختر وقت الإشعار");
            timePicker.setButton(TimePickerDialog.BUTTON_POSITIVE, "حفظ", (Message) null);
            timePicker.setButton(TimePickerDialog.BUTTON_NEGATIVE, "إلغاء", (Message) null);
            timePicker.setCancelable(true);

            timePicker.show();
        }

        private void cancelAlarm(String key) {
            int id = 0;
            if (key.equals(getString(R.string.morning_athkar_key)))
                id = 8;
            else if (key.equals(getString(R.string.night_athkar_key)))
                id = 9;
            else if (key.equals(getString(R.string.daily_page_key)))
                id = 10;

            SwitchPreferenceCompat pSwitch = findPreference(key);
            assert pSwitch != null;
            pSwitch.setSummary("");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),
                    id, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT
                            | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager am = (AlarmManager) requireContext().getApplicationContext()
                    .getSystemService(Context.ALARM_SERVICE);

            am.cancel(pendingIntent);
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
