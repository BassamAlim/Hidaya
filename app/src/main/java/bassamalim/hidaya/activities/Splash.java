package bassamalim.hidaya.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationServices;

import java.util.Collection;

import bassamalim.hidaya.helpers.Keeper;
import bassamalim.hidaya.services.AthanService;

public class Splash extends AppCompatActivity {

    private final int build = Build.VERSION.SDK_INT;
    private final String[] PERMISSIONS =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> true );

        super.onCreate(savedInstanceState);

        stopService(new Intent(this, AthanService.class));

        newUser();

        if (granted()) {
            getLocation();

            if (build >= 29 && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED)
                background();
        }
        else {
            if (new Keeper(this).retrieveLocation() == null)
                requestMultiplePermissions.launch(PERMISSIONS);
            else
                launch(null);
        }
    }

    private void newUser() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean newUser = pref.getBoolean("new_user", true);

        if (newUser) {
            welcome();

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("new_user", false);
            editor.apply();

            finish();
        }
    }

    private void welcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean granted() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                    .addOnSuccessListener(this, this::launch);
        }
    }

    private void launch(Location location) {
        Intent intent = new Intent(this, MainActivity.class);

        if (location == null) {
            location = new Keeper(this).retrieveLocation();
            intent.putExtra("located", location != null);
        }
        else
            intent.putExtra("located", true);

        intent.putExtra("location", location);
        startActivity(intent);
        finish();
    }

    private final ActivityResultLauncher<String[]> requestMultiplePermissions =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {

                Collection<Boolean> collection = permissions.values();
                Boolean[] array = collection.toArray(new Boolean[2]);

                if (array[0] && array[1]) {
                    if (build >= 29)
                        background();
                    getLocation();
                }
                else
                    launch(null);
    });

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void background() {
        Toast.makeText(this, "اختر السماح طوال الوقت لإبقاء الموقع دقيق",
                Toast.LENGTH_SHORT).show();
        requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                0);
    }

}
