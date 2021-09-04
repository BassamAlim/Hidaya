package com.bassamalim.athkar.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.models.MyLocation;
import com.bassamalim.athkar.services.DailyUpdateService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.Calendar;

public class DailyUpdateReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context gContext, Intent intent) {
        Log.i(Constants.TAG, "in daily update receiver");
        context = gContext;

        if (intent.getAction().equals("daily")) {
            int time = intent.getIntExtra("time", 0);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.HOUR_OF_DAY, time);

            if (System.currentTimeMillis() >= cal.getTimeInMillis()) {
                process();
            }
        }
        else if (intent.getAction().equals("boot"))
            process();
    }

    private void process() {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
                MyLocation myLocation = new MyLocation(loc);

                Gson gson = new Gson();
                String st = gson.toJson(myLocation);

                Intent intent1 = new Intent(context, DailyUpdateService.class);
                intent1.putExtra("location", st);
                context.startService(intent1);
            });
        }
    }

}
