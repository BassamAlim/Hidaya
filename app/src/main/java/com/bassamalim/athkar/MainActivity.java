package com.bassamalim.athkar;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import com.bassamalim.athkar.receivers.DeviceBootReceiver;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import com.bassamalim.athkar.databinding.ActivityMainBinding;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import java.time.chrono.HijrahDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    public static Location location;
    public static ArrayList<String> times;
    public static Calendar[] formattedTimes;

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

        times = getTimes(location);

        formattedTimes = formatTimes(times);
        //Calendar[] formattedTimes = test();

        if (location.getLatitude() != 0.0 || location.getLongitude() != 0.0)
            new Keeper(this, location);
        else
            location = new Keeper(this).retrieveLocation();

        new Alarms(this, formattedTimes);

        dailyUpdate();
        setupBootReceiver();

        new Update(this);
    }

    private Calendar[] test() {
        Calendar[] tester = new Calendar[7];

        tester[0] = Calendar.getInstance();
        tester[0].setTimeInMillis(System.currentTimeMillis());
        tester[0].set(Calendar.HOUR_OF_DAY, 14);
        tester[0].set(Calendar.MINUTE, 2);
        tester[1] = Calendar.getInstance();
        tester[1].setTimeInMillis(System.currentTimeMillis());
        tester[1].set(Calendar.HOUR_OF_DAY, 2);
        tester[1].set(Calendar.MINUTE, 8);
        tester[2] = Calendar.getInstance();
        tester[2].setTimeInMillis(System.currentTimeMillis());
        tester[2].set(Calendar.HOUR_OF_DAY, 0);
        tester[2].set(Calendar.MINUTE, 1);
        tester[3] = Calendar.getInstance();
        tester[3].setTimeInMillis(System.currentTimeMillis());
        tester[3].set(Calendar.HOUR_OF_DAY, 0);
        tester[3].set(Calendar.MINUTE, 27);
        tester[4] = Calendar.getInstance();
        tester[4].setTimeInMillis(System.currentTimeMillis());
        tester[4].set(Calendar.HOUR_OF_DAY, 0);
        tester[4].set(Calendar.MINUTE, 5);
        tester[5] = Calendar.getInstance();
        tester[5].setTimeInMillis(System.currentTimeMillis());
        tester[5].set(Calendar.HOUR_OF_DAY, 2);
        tester[5].set(Calendar.MINUTE, 43);
        tester[6] = Calendar.getInstance();
        tester[6].setTimeInMillis(System.currentTimeMillis());
        tester[6].set(Calendar.HOUR_OF_DAY, 4);
        tester[6].set(Calendar.MINUTE, 43);

        return tester;
    }

    private void dailyUpdate() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int day = pref.getInt("last_day", 0);

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        if (day != today.get(Calendar.DAY_OF_MONTH)) {
            new DailyUpdate(this);
        }
    }

    private ArrayList<String> getTimes(Location loc) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        return new PrayTimes().getPrayerTimes(calendar, loc.getLatitude(),
                loc.getLongitude(), timezone);
    }

    private Calendar[] formatTimes(ArrayList<String> givenTimes) {
        Calendar[] formattedTimes = new Calendar[givenTimes.size()];

        for (int i = 0; i < givenTimes.size(); i++) {
            char m = givenTimes.get(i).charAt(6);
            int hour = Integer.parseInt(givenTimes.get(i).substring(0, 2));
            if (m == 'P')
                hour += 12;

            formattedTimes[i] = Calendar.getInstance();
            formattedTimes[i].setTimeInMillis(System.currentTimeMillis());
            formattedTimes[i].set(Calendar.HOUR_OF_DAY, hour);
            formattedTimes[i].set(Calendar.MINUTE, Integer.parseInt(givenTimes.get(i).substring(3, 5)));
        }
        return formattedTimes;
    }

    private void setHijri() {
        String text = whichDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) + " ";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Calendar cl = Calendar.getInstance();
            Date date = new Date();
            cl.setTime(date);

            HijrahDate hijriDate = HijrahDate.now();
            String hDate = hijriDate.toString();
            hDate = hDate.substring(19);

            String year = hDate.substring(0, 4);
            String month = whichMonth(Integer.parseInt(hDate.substring(5, 7)));
            String  day = hDate.substring(8, 10);

            year = translateNumbers(year);
            day = translateNumbers(day);

            text += day + " " + month + " " + year;
        }

        binding.hijriView.setText(text);

        //OUTPUT: Hijrah-umalqura AH 1436-02-03
        //substringed: 1436-02-03
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
        monthMap.put(1, "مُحَرَّم");
        monthMap.put(2, "صَفَر");
        monthMap.put(3, "ربيع الأول");
        monthMap.put(4, "ربيع الثاني");
        monthMap.put(5, "جُمادى الأول");
        monthMap.put(6, "جُمادى الآخر");
        monthMap.put(7, "رجب");
        monthMap.put(8, "شعبان");
        monthMap.put(9, "رَمَضان");
        monthMap.put(10, "شَوَّال");
        monthMap.put(11, "ذو القِعْدة");
        monthMap.put(12, "ذو الحِجَّة");

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