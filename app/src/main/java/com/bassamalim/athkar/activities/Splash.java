package com.bassamalim.athkar.activities;

import android.Manifest;
import android.content.Intent;
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

import com.bassamalim.athkar.helpers.Keeper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Collection;

public class Splash extends AppCompatActivity {

    private boolean granted = false;
    private final int build = Build.VERSION.SDK_INT;
    private final String[] PERMISSIONS =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private String BACKGROUND_PERMISSION;
    private int stupidity = 0;
    private boolean dump = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            BACKGROUND_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;

        if (permissionsGranted())
            getLocation();
        else
            requestMultiplePermissions.launch(PERMISSIONS);
    }

    private boolean permissionsGranted() {
        boolean given;
        if (build >= 29) {
            given = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED;
        }
        else {
            given = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED;
        }
        return given;
    }

    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                Intent intent = new Intent(this, MainActivity.class);

                if (location != null)
                    intent.putExtra("location", location);
                else {
                    Location storedLocation = new Keeper(this).retrieveLocation();
                    if (storedLocation == null) {
                        Toast.makeText(this, "No Location Available | الموقع غير متوفر",
                                Toast.LENGTH_LONG).show();
                        finishAffinity();
                    }
                    else
                        intent.putExtra("location", new Keeper(this).retrieveLocation());
                }
                startActivity(intent);
            });
        }
    }

    private final ActivityResultLauncher<String[]> requestMultiplePermissions =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {

                Collection<Boolean> collection = permissions.values();
                Boolean[] array = collection.toArray(new Boolean[2]);

                if (array[0] && array[1])
                    granted = true;

                if (granted) {
                    if (build >= 29) {
                        Toast.makeText(this, "اختر السماح طوال الوقت",
                                Toast.LENGTH_SHORT).show();
                        background();
                    }
                    else
                        getLocation();
                }
                else {
                    stupidity++;
                    stubborn(1);
                }
    });

    private final ActivityResultLauncher<String> requestBackground = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted)
                getLocation();
            else
                stubborn(2);
    });

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void background() {
        BACKGROUND_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        requestBackground.launch(BACKGROUND_PERMISSION);
    }

    private void stubborn(int per) {
        if (stupidity > 1)
            dump = true;

        if (!dump) {
            if (per == 1)
                Toast.makeText(this, "يجب السماح للتطبيق بالحصول على الموقع",
                        Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(this, "اذهب الى الاعدادت > التطبيقات > أذكار > الاذونات > " +
                        "السماح بالوصول للموقع طوال الوقت", Toast.LENGTH_SHORT).show();
            }
            if (per == 1)
                requestMultiplePermissions.launch(PERMISSIONS);
            else
                requestBackground.launch(BACKGROUND_PERMISSION);
        }
        else {
            Toast.makeText(this, "إذهب إلى إعدادات التطبيق وأختر السماح للموقع طوال الوقت",
                    Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }
    }

}
