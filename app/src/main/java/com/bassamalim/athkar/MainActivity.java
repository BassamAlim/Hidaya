package com.bassamalim.athkar;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import com.bassamalim.athkar.receivers.DeviceBootReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.bassamalim.athkar.databinding.ActivityMainBinding;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import java.time.chrono.HijrahDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private ActivityMainBinding binding;
    public final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    private final Calendar calendar = Calendar.getInstance();
    private final Date date = new Date();
    public static Location location;
    public static ArrayList<String> times;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setHijri();
        setContentView(binding.getRoot());

        Toolbar hijriBar = findViewById(R.id.my_hijri_bar);
        setSupportActionBar(hijriBar);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_prayers, R.id.navigation_alathkar, R.id.navigation_qibla).build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        // for the action bar which i removed to create my own
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600 * 24).build();

        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        Intent intent = getIntent();
        location = intent.getParcelableExtra("location");

        times = getTimes();

        Calendar[] formattedTimes = formatTimes(times);

        //test();

        if (location.getLatitude() != 0.0)
            new Keeper(this, location);
        else
            location = new Keeper(this).retrieveLocation();

        if (getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.athan_enable_key), true)) {
            new Alarms(this, formattedTimes);
            new DailyUpdate();
            receiver();
        }

        new Update();
    }

    public void test() {
        Calendar[] tester = new Calendar[7];

        tester[0] = calendar;
        tester[0].setTimeInMillis(System.currentTimeMillis());
        tester[0].set(Calendar.HOUR_OF_DAY, 2);
        tester[0].set(Calendar.MINUTE, 4);

        tester[1] = calendar;
        tester[1].setTimeInMillis(System.currentTimeMillis());
        tester[1].set(Calendar.HOUR_OF_DAY, 2);
        tester[1].set(Calendar.MINUTE, 8);

        tester[2] = calendar;
        tester[2].setTimeInMillis(System.currentTimeMillis());
        tester[2].set(Calendar.HOUR_OF_DAY, 10);
        tester[2].set(Calendar.MINUTE, 41);

        tester[3] = calendar;
        tester[3].setTimeInMillis(System.currentTimeMillis());
        tester[3].set(Calendar.HOUR_OF_DAY, 21);
        tester[3].set(Calendar.MINUTE, 41);

        tester[4] = calendar;
        tester[4].setTimeInMillis(System.currentTimeMillis());
        tester[4].set(Calendar.HOUR_OF_DAY, 16);
        tester[4].set(Calendar.MINUTE, 42);

        tester[5] = calendar;
        tester[5].setTimeInMillis(System.currentTimeMillis());
        tester[5].set(Calendar.HOUR_OF_DAY, 2);
        tester[5].set(Calendar.MINUTE, 43);

        tester[6] = calendar;
        tester[6].setTimeInMillis(System.currentTimeMillis());
        tester[6].set(Calendar.HOUR_OF_DAY, 11);
        tester[6].set(Calendar.MINUTE, 54);

        /*Intent intent1 = new Intent(this, NotificationService.class);
        intent1.putExtra("prayer", 1);
        startService(intent1);*/
    }

    public ArrayList<String> getTimes() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return new PrayTimes().getPrayerTimes(calendar, location.getLatitude(),
                location.getLongitude(), Constants.TIME_ZONE);
    }

    private Calendar[] formatTimes(ArrayList<String> givenTimes) {
        Calendar[] formattedTimes = new Calendar[givenTimes.size()];

        for (int i = 0; i < givenTimes.size(); i++) {
            char m = givenTimes.get(i).charAt(6);
            int hour = Integer.parseInt(givenTimes.get(i).substring(0, 2));
            if (m == 'P')
                hour += 12;

            formattedTimes[i] = calendar;
            formattedTimes[i].setTimeInMillis(System.currentTimeMillis());
            formattedTimes[i].set(Calendar.HOUR_OF_DAY, hour);
            formattedTimes[i].set(Calendar.MINUTE, Integer.parseInt(givenTimes.get(i).substring(3, 5)));
        }
        return formattedTimes;
    }

    private void setHijri() {
        String text = whichDay(calendar.get(Calendar.DAY_OF_WEEK)) + " ";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Calendar cl = Calendar.getInstance();
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

    public static String translateNumbers(String english) {
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

    public void receiver() {
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
}