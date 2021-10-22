package com.bassamalim.athkar.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.ActivityMainBinding;
import com.bassamalim.athkar.helpers.Alarms;
import com.bassamalim.athkar.helpers.DailyUpdate;
import com.bassamalim.athkar.helpers.Keeper;
import com.bassamalim.athkar.helpers.PrayTimes;
import com.bassamalim.athkar.helpers.Update;
import com.bassamalim.athkar.receivers.DeviceBootReceiver;
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    public static Location location;
    public static Calendar[] times;
    public static boolean located;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setHijri();
        setContentView(binding.getRoot());

        setSupportActionBar(binding.myHijriBar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navController);

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600 * 24).build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        Intent intent = getIntent();
        location = intent.getParcelableExtra("location");
        located = intent.getBooleanExtra("located", false);

        if (located) {
            new Keeper(this, location);
            times = getTimes(location);
            //times = test();
            new Alarms(this, times);
        }
        else {
            Toast.makeText(this,
                    "لا يمكن الوصول للموقع، يرجى إعطاء أذن الوصول للموقع لحساب أوقات الصلاة والقبلة",
                    Toast.LENGTH_SHORT).show();
        }

        dailyUpdate();
        setupBootReceiver();

        new Update(this);
    }

    private Calendar[] test() {
        Calendar[] tester = new Calendar[6];

        tester[0] = Calendar.getInstance();
        tester[0].set(Calendar.HOUR_OF_DAY, 14);
        tester[0].set(Calendar.MINUTE, 2);
        tester[1] = Calendar.getInstance();
        tester[1].set(Calendar.HOUR_OF_DAY, 13);
        tester[1].set(Calendar.MINUTE, 48);
        tester[2] = Calendar.getInstance();
        tester[2].set(Calendar.HOUR_OF_DAY, 0);
        tester[2].set(Calendar.MINUTE, 1);
        tester[3] = Calendar.getInstance();
        tester[3].set(Calendar.HOUR_OF_DAY, 0);
        tester[3].set(Calendar.MINUTE, 27);
        tester[4] = Calendar.getInstance();
        tester[4].set(Calendar.HOUR_OF_DAY, 0);
        tester[4].set(Calendar.MINUTE, 5);
        tester[5] = Calendar.getInstance();
        tester[5].set(Calendar.HOUR_OF_DAY, 2);
        tester[5].set(Calendar.MINUTE, 43);

        return tester;
    }

    private void dailyUpdate() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int day = pref.getInt("last_day", 0);

        Calendar today = Calendar.getInstance();

        if (day != today.get(Calendar.DAY_OF_MONTH))
            new DailyUpdate(this);
    }

    private Calendar[] getTimes(Location loc) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        return new PrayTimes().getPrayerTimesArray(calendar, loc.getLatitude(),
                loc.getLongitude(), timezone);
    }

    private void setHijri() {
        UmmalquraCalendar hijri = new UmmalquraCalendar();
        String text = whichDay(hijri.get(Calendar.DAY_OF_WEEK)) + " ";

        String year = " " + hijri.get(Calendar.YEAR);
        String month = " " + whichMonth(hijri.get(Calendar.MONTH));
        String day = String.valueOf(hijri.get(Calendar.DATE));

        year = translateNumbers(year);
        day = translateNumbers(day);

        text += day + month + year;

        binding.hijriView.setText(text);
    }

    private String whichDay(int day) {
        String result;
        HashMap<Integer, String> weekMap = new HashMap<>();
        weekMap.put(Calendar.SUNDAY, "الأحد");
        weekMap.put(Calendar.MONDAY, "الأثنين");
        weekMap.put(Calendar.TUESDAY, "الثلاثاء");
        weekMap.put(Calendar.WEDNESDAY, "الأربعاء");
        weekMap.put(Calendar.THURSDAY, "الخميس");
        weekMap.put(Calendar.FRIDAY, "الجمعة");
        weekMap.put(Calendar.SATURDAY, "السبت");

        result = weekMap.get(day);
        return result;
    }

    private String whichMonth(int num) {
        String result;
        HashMap<Integer, String> monthMap = new HashMap<>();
        monthMap.put(0, "مُحَرَّم");
        monthMap.put(1, "صَفَر");
        monthMap.put(2, "ربيع الأول");
        monthMap.put(3, "ربيع الثاني");
        monthMap.put(4, "جُمادى الأول");
        monthMap.put(5, "جُمادى الآخر");
        monthMap.put(6, "رجب");
        monthMap.put(7, "شعبان");
        monthMap.put(8, "رَمَضان");
        monthMap.put(9, "شَوَّال");
        monthMap.put(10, "ذو القِعْدة");
        monthMap.put(11, "ذو الحِجَّة");

        result = monthMap.get(num);
        return result;
    }

    private String translateNumbers(String english) {
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

            if (english.charAt(0) == '0')
                english = english.replaceFirst("0", "");

            StringBuilder temp = new StringBuilder();
            for (int j = 0; j < english.length(); j++) {
                char t = english.charAt(j);
                if (map.containsKey(t))
                    t = map.get(t);
                temp.append(t);
            }
            result = temp.toString();

        return result;
    }

    private void setupBootReceiver() {
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishAffinity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        remoteConfig = null;
    }
}
