package com.bassamalim.athkar;

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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.Collection;

public class Splash extends AppCompatActivity {

    boolean granted = false;
    int build = Build.VERSION.SDK_INT;
    private final String[] PERMISSIONS =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    String BACKGROUND_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    private int stupidity = 0;
    private boolean dump = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (permissionsGranted())
            getLocation();
        else
            requestMultiplePermissions.launch(PERMISSIONS);
    }

    public boolean permissionsGranted() {


        boolean given;
        if (build >= 29) {
            given = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        else {
            given = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return given;
    }

    public void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                Intent intent = new Intent(this, MainActivity.class);

                if (location != null)
                    intent.putExtra("location", location);
                else
                    intent.putExtra("location", new Keeper(this).retrieveLocation());

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
                        Toast.makeText(this, "اختار السماح طوال الوقت", Toast.LENGTH_SHORT).show();
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

    private final ActivityResultLauncher<String> requestBackground =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

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
                Toast.makeText(this, "لازم تسوي سماح للموقع عشان يشتغل",
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "اذهب الى الاعدادت > التطبيقات > أذكار > الاذونات > السماح بالوصول للموقع طوال الوقت", Toast.LENGTH_SHORT).show();
            if (per == 1)
                requestMultiplePermissions.launch(PERMISSIONS);
            else
                requestBackground.launch(BACKGROUND_PERMISSION);
        }
        else {
            Toast.makeText(this, "روح لاعدادات التطبيق وسوي سماح للموقع", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }
    }

}
