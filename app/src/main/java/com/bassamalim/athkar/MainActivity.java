package com.bassamalim.athkar;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.bassamalim.athkar.receivers.DeviceBootReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.bassamalim.athkar.databinding.ActivityMainBinding;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private ActivityMainBinding binding;
    public final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    public static Location location;
    public static ArrayList<String> times;
    public Calendar[] formattedTimes;
    public Gson gson;
    public SharedPreferences myPrefs;
    public String json;
    public DataSaver saver;
    public SharedPreferences.Editor editor;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_prayers, R.id.navigation_alathkar, R.id.navigation_qibla).build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60).build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        if (!checkPermissions())
            requestPermissions();

        Intent intent = getIntent();
        location = intent.getParcelableExtra("location");

        storeLocation(location);

        times = getTimes();

        formattedTimes = formatTimes(times);
        //formattedTimes = test();

        storeTimes(formattedTimes);

        Alarms alarms = new Alarms(this, location, formattedTimes);

        DailyUpdate dailyUpdate = new DailyUpdate();

        receiver();

        Update update = new Update();
    }

    public Calendar[] test() {
        Calendar[] tester = new Calendar[7];

        tester[0] = Calendar.getInstance();
        tester[0].setTimeInMillis(System.currentTimeMillis());
        tester[0].set(Calendar.HOUR_OF_DAY, 22);
        tester[0].set(Calendar.MINUTE, 1);

        tester[1] = Calendar.getInstance();
        tester[1].setTimeInMillis(System.currentTimeMillis());
        tester[1].set(Calendar.HOUR_OF_DAY, 21);
        tester[1].set(Calendar.MINUTE, 38);

        tester[2] = Calendar.getInstance();
        tester[2].setTimeInMillis(System.currentTimeMillis());
        tester[2].set(Calendar.HOUR_OF_DAY, 10);
        tester[2].set(Calendar.MINUTE, 41);

        tester[3] = Calendar.getInstance();
        tester[3].setTimeInMillis(System.currentTimeMillis());
        tester[3].set(Calendar.HOUR_OF_DAY, 21);
        tester[3].set(Calendar.MINUTE, 41);

        tester[4] = Calendar.getInstance();
        tester[4].setTimeInMillis(System.currentTimeMillis());
        tester[4].set(Calendar.HOUR_OF_DAY, 16);
        tester[4].set(Calendar.MINUTE, 42);

        tester[5] = Calendar.getInstance();
        tester[5].setTimeInMillis(System.currentTimeMillis());
        tester[5].set(Calendar.HOUR_OF_DAY, 2);
        tester[5].set(Calendar.MINUTE, 43);

        tester[6] = Calendar.getInstance();
        tester[6].setTimeInMillis(System.currentTimeMillis());
        tester[6].set(Calendar.HOUR_OF_DAY, 11);
        tester[6].set(Calendar.MINUTE, 54);

        return tester;
    }

    public ArrayList<String> getTimes() {
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

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

            formattedTimes[i] = Calendar.getInstance();
            formattedTimes[i].setTimeInMillis(System.currentTimeMillis());
            formattedTimes[i].set(Calendar.HOUR_OF_DAY, hour);
            formattedTimes[i].set(Calendar.MINUTE, Integer.parseInt(givenTimes.get(i).substring(3, 5)));
        }
        return formattedTimes;
    }

    public void storeTimes(Calendar[] givenTimes) {
        myPrefs = getApplicationContext().getSharedPreferences("times", Context.MODE_PRIVATE);
        //may produce null;
        editor = myPrefs.edit();

        DataSaver saver = new DataSaver();
        saver.times = givenTimes;

        gson = new Gson();

        json = gson.toJson(saver);

        editor.putString("times", json);
        editor.apply();
    }

    public void storeLocation(Location givenLocation) {
        myPrefs = this.getApplicationContext().getSharedPreferences("location", Context.MODE_PRIVATE);
        editor = myPrefs.edit();

        DataSaver saver = new DataSaver();
        saver.location = givenLocation;

        gson = new Gson();

        json = gson.toJson(saver);

        editor.putString("location", json);
        editor.apply();
    }

    public void receiver() {
        ComponentName receiver = new ComponentName(getApplicationContext(), DeviceBootReceiver.class);
        PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getInstance(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getInstance(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions() {
        ActivityCompat.requestPermissions(getInstance(), new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        Toast.makeText(getInstance(), "لازم تسوي سماح للموقع عشان يشتغل", Toast.LENGTH_LONG).show();
        checkPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary, getTheme())));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.whatsapp) {
            String myNumber = "+966553145230";
            String url = "https://api.whatsapp.com/send?phone=" + myNumber;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

}