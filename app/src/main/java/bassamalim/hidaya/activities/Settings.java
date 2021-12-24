package bassamalim.hidaya.activities;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import bassamalim.hidaya.helpers.Alarms;
import bassamalim.hidaya.R;
import bassamalim.hidaya.enums.ID;

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
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        boolean located = true;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            setInitialStates();

            setListeners();
        }

        private void setInitialStates() {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(requireContext());
            SwitchPreferenceCompat pSwitch;

            pSwitch = findPreference(keyGetter(ID.MORNING));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(ID.MORNING+"text", "٥:٠٠ صباحاً"));

            pSwitch = findPreference(keyGetter(ID.EVENING));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(ID.EVENING+"text", "٤:٠٠ مساءاًً"));

            pSwitch = findPreference(keyGetter(ID.DAILY_WERD));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(ID.DAILY_WERD+"text", "٩:٠٠ مساءاً"));

            if (!MainActivity.located) {
                located = false;
                pSwitch = findPreference(keyGetter(ID.FRIDAY_KAHF));
                assert pSwitch != null;
                pSwitch.setSummary(pref.getString(ID.FRIDAY_KAHF+"text", "١:٠٠ مساءاً"));
            }
        }

        private void setListeners() {
            setSwitchListener(ID.MORNING, true);
            setSwitchListener(ID.EVENING, true);
            setSwitchListener(ID.DAILY_WERD, true);
            setSwitchListener(ID.FRIDAY_KAHF, !located);
        }

        private void setSwitchListener(ID id, boolean timed) {
            String key = keyGetter(id);
            SwitchPreferenceCompat pSwitch = findPreference(key);
            assert pSwitch != null;

            pSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean on = (Boolean) newValue;
                if (on) {
                    if (timed)
                        showTimePicker(id);
                    else
                        new Alarms(getContext(), id);
                }
                else
                    cancelAlarm(id);
                return true;
            });
        }

        private void showTimePicker(ID id) {
            String key = keyGetter(id);
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

                SharedPreferences myPref = PreferenceManager.
                        getDefaultSharedPreferences(requireContext());
                SharedPreferences.Editor editor = myPref.edit();
                editor.putInt(id + "hour", hourOfDay);
                editor.putInt(id + "minute", minute);
                editor.putString(id + "text", fixed);
                editor.apply();

                new Alarms(getContext(), id);

                }, cHour, cMinute, false);

            timePicker.setTitle("اختر وقت الإشعار");
            timePicker.setButton(TimePickerDialog.BUTTON_POSITIVE, "حفظ", (Message) null);
            timePicker.setButton(TimePickerDialog.BUTTON_NEGATIVE, "إلغاء", (Message) null);
            timePicker.setCancelable(true);

            timePicker.show();
        }

        private void cancelAlarm(ID id) {
            Alarms.cancelAlarm(getContext(), id);

            String key = keyGetter(id);
            // its not a prayer
            SwitchPreferenceCompat pSwitch = findPreference(key);
            assert pSwitch != null;
            pSwitch.setSummary("");
        }

        private String keyGetter(ID id) {
            switch (id) {
                case MORNING:
                    return getString(R.string.morning_athkar_key);
                case EVENING:
                    return getString(R.string.evening_athkar_key);
                case DAILY_WERD:
                    return getString(R.string.daily_werd_key);
                case FRIDAY_KAHF:
                    return getString(R.string.friday_kahf_key);
                default:
                    return "";
            }
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
        else if (h == 0)
            h = 12;
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