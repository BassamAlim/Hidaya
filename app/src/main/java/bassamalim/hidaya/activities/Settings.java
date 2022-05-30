package bassamalim.hidaya.activities;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import bassamalim.hidaya.R;
import bassamalim.hidaya.enums.ID;
import bassamalim.hidaya.helpers.Alarms;
import bassamalim.hidaya.other.Utils;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.myOnActivityCreated(this);
        setContentView(R.layout.activity_settings);
        findViewById(android.R.id.home).setOnClickListener(v -> onBackPressed());

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings, new SettingsFragment("normal")).commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final String action;

        public SettingsFragment(String action) {
            this.action = action;
        }

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
            pSwitch.setSummary(pref.getString(ID.MORNING+"text",
                    getString(R.string.default_morning_summary)));

            pSwitch = findPreference(keyGetter(ID.EVENING));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(ID.EVENING+"text",
                    getString(R.string.default_evening_summary)));

            pSwitch = findPreference(keyGetter(ID.DAILY_WERD));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(ID.DAILY_WERD+"text",
                    getString(R.string.default_werd_summary)));

            pSwitch = findPreference(keyGetter(ID.FRIDAY_KAHF));
            assert pSwitch != null;
            pSwitch.setSummary(pref.getString(ID.FRIDAY_KAHF + "text",
                            getString(R.string.default_kahf_summary)));
        }

        private void setListeners() {
            setSwitchListener(ID.MORNING);
            setSwitchListener(ID.EVENING);
            setSwitchListener(ID.DAILY_WERD);
            setSwitchListener(ID.FRIDAY_KAHF);

            ListPreference languages = findPreference(getString(R.string.language_key));
            assert languages != null;
            languages.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!newValue.equals(PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString(requireContext().getString(R.string.language_key), getString(R.string.default_language)))) {
                    setLocale((String) newValue);
                    Utils.refresh(requireActivity());
                }
                return true;
            });

            if (action.equals("initial"))
                languages.setSummaryProvider(null);

            ListPreference themes = findPreference(getString(R.string.theme_key));
            assert themes != null;
            themes.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!newValue.equals(PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString(requireContext().getString(R.string.theme_key), getString(R.string.default_theme))))
                    Utils.refresh(requireActivity());
                return true;
            });

            if (action.equals("initial"))
                themes.setSummaryProvider(null);
        }

        private void setSwitchListener(ID id) {
            String key = keyGetter(id);
            SwitchPreferenceCompat pSwitch = findPreference(key);
            assert pSwitch != null;

            pSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean on = (Boolean) newValue;
                if (on)
                    showTimePicker(id);
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
                String fixed = formatText(getContext(), nums);

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

            timePicker.setOnCancelListener(dialog -> setInitialStates());
            timePicker.setOnDismissListener(dialog -> setInitialStates());

            timePicker.setTitle(getString(R.string.time_picker_title));
            timePicker.setButton(TimePickerDialog.BUTTON_POSITIVE,
                    getString(R.string.select), (Message) null);
            timePicker.setButton(TimePickerDialog.BUTTON_NEGATIVE,
                    getString(R.string.cancel), (Message) null);
            timePicker.setCancelable(true);

            timePicker.show();
        }

        private void setLocale(String language) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);

            Resources resources = getResources();

            Configuration configuration = resources.getConfiguration();
            configuration.locale = locale;
            configuration.setLayoutDirection(locale);

            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }

        private void cancelAlarm(ID id) {
            Utils.cancelAlarm(getContext(), id);
            String key = keyGetter(id);
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

    private static String formatText(Context context, int[] nums) {
        String locale = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.language_key),
                        context.getString(R.string.default_language));

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
        String section = context.getString(R.string.at_morning);
        if (h == 0)
            h = 12;
        else if (h >= 12) {
            section = context.getString(R.string.at_evening);
            if (h > 12)
                h -= 12;
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
            if (map.containsKey(c)) {
                if (locale.equals("ar"))
                    translated.append(map.get(c));
                else
                    translated.append(c);
            }
            else
                translated.append(c);
        }

        return translated.toString();
    }

}
