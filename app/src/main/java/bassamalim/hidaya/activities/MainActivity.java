package bassamalim.hidaya.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Calendar;
import java.util.HashMap;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.databinding.ActivityMainBinding;
import bassamalim.hidaya.helpers.Alarms;
import bassamalim.hidaya.helpers.Keeper;
import bassamalim.hidaya.other.Const;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.receivers.DailyUpdateReceiver;
import bassamalim.hidaya.receivers.DeviceBootReceiver;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    public static Location location;
    public static Calendar[] times;
    public static boolean located;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setTodayScreen();
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);

        initNavBar();

        initFirebase();

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        setAlarms();

        testDb();

        dailyUpdate();

        setupBootReceiver();
    }

    private void initNavBar() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void initFirebase() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600 * 6).build(); // update at most every six hours
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
    }

    private void setAlarms() {
        Intent intent = getIntent();
        located = intent.getBooleanExtra("located", false);
        if (located) {
            location = intent.getParcelableExtra("location");
            new Keeper(this, location);
            times = Utils.getTimes(location);
            //times = test();
            new Alarms(this, times);
        }
        else {
            Toast.makeText(this,
                    "لا يمكن الوصول للموقع، يرجى إعطاء أذن الوصول للموقع لحساب أوقات الصلاة والقبلة",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Calendar[] test() {
        Calendar[] tester = new Calendar[6];

        tester[0] = Calendar.getInstance();
        tester[0].set(Calendar.HOUR_OF_DAY, 0);
        tester[0].set(Calendar.MINUTE, 9);
        tester[0].set(Calendar.SECOND, 0);
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

    private void testDb() {
        try {
            AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                    .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();
            db.suraDao().getFav();    // if there is a problem in the db it will cause an error
        } catch (Exception e) {
            Utils.reviveDb(this);
        }

        int lastVer = pref.getInt("last_db_version", 1);
        if (Const.dbVer > lastVer)
            Utils.reviveDb(this);
    }

    private void dailyUpdate() {
        int day = pref.getInt("last_day", 0);

        Calendar today = Calendar.getInstance();
        if (day != today.get(Calendar.DAY_OF_MONTH)) {
            final int DAILY_UPDATE_HOUR = 0;

            Intent intent = new Intent(this, DailyUpdateReceiver.class);
            intent.setAction("daily");
            intent.putExtra("time", DAILY_UPDATE_HOUR);

            Calendar time = Calendar.getInstance();
            time.set(Calendar.HOUR_OF_DAY, DAILY_UPDATE_HOUR);

            PendingIntent pendIntent = PendingIntent.getBroadcast(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager myAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            myAlarm.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendIntent);

            sendBroadcast(intent);
        }
    }

    private void setTodayScreen() {
        UmmalquraCalendar hijri = new UmmalquraCalendar();
        String hYear = " " + hijri.get(Calendar.YEAR);
        String hMonth = " " + Utils.whichHijriMonth(hijri.get(Calendar.MONTH));
        String hDay = "" + hijri.get(Calendar.DATE);
        String hijriStr = whichDay(hijri.get(Calendar.DAY_OF_WEEK)) + " ";
        hijriStr += Utils.translateNumbers(hDay) + hMonth + Utils.translateNumbers(hYear);
        binding.hijriView.setText(hijriStr);

        Calendar meladi = Calendar.getInstance();
        String mYear = " " + meladi.get(Calendar.YEAR);
        String mMonth = " " + whichMeladiMonth(meladi.get(Calendar.MONTH));
        String mDay = "" + meladi.get(Calendar.DATE);
        String meladiStr = Utils.translateNumbers(mDay) + mMonth + Utils.translateNumbers(mYear);
        binding.meladiView.setText(meladiStr);
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

    private String whichMeladiMonth(int num) {
        String result;
        HashMap<Integer, String> monthMap = new HashMap<>();
        monthMap.put(0, "يناير");
        monthMap.put(1, "فبرابر");
        monthMap.put(2, "مارس");
        monthMap.put(3, "أبريل");
        monthMap.put(4, "مايو");
        monthMap.put(5, "يونيو");
        monthMap.put(6, "يوليو");
        monthMap.put(7, "أغسطس");
        monthMap.put(8, "سبتمبر");
        monthMap.put(9, "أكتوبر");
        monthMap.put(10, "نوفمبر");
        monthMap.put(11, "ديسمبر");

        result = monthMap.get(num);
        return result;
    }

    private void setupBootReceiver() {
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        remoteConfig = null;
    }
}
