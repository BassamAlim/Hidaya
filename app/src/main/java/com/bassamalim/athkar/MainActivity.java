package com.bassamalim.athkar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.bassamalim.athkar.databinding.ActivityMainBinding;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private ActivityMainBinding binding;
    private final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    public static final String UPDATE_AVAILABLE = "update_available";
    public static final String LATEST_VERSION = "latest_app_version";
    public static final String UPDATE_URL = "update_url";
    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationClient;
    public static Location userLocation = null;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private int[] grantResults = null;


    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_prayers, R.id.navigation_alathkar, R.id.navigation_qibla).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600).build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        instance = this;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!checkPermissions())
            requestPermissions();

        findLocation();

        checkForUpdate();
    }

    public static boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getInstance(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getInstance(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions() {
        ActivityCompat.requestPermissions(getInstance(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        Toast.makeText(getInstance(), "لازم تسوي سماح للموقع عشان يشتغل", Toast.LENGTH_LONG).show();
        checkPermissions();
    }

    public void findLocation() {
        if (ActivityCompat.checkSelfPermission(getInstance(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getInstance(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLocation = location;
                    //Toast.makeText(this, "location found", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this, "no location", Toast.LENGTH_LONG).show();
            });
        }
        else
            findLocation();
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

    private void checkForUpdate() {
        remoteConfig.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "remote config is fetched.");
                remoteConfig.fetchAndActivate();
            }
            boolean available = remoteConfig.getBoolean(UPDATE_AVAILABLE);
            remoteConfig.fetchAndActivate();
            if (available) {
                Log.i(TAG, "windBlows");
                String latestVersion = remoteConfig.getString(LATEST_VERSION);
                String currentVersion = getAppVersion(this);
                if (!TextUtils.equals(currentVersion, latestVersion))
                    showUpdatePrompt();
            }
        });
    }

    public void showUpdatePrompt() {
        UpdateDialog updateDialog = new UpdateDialog(this);
        updateDialog.show();
    }

    private String getAppVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }

    public void update() {
        String url;
        url = remoteConfig.getString(UPDATE_URL);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}