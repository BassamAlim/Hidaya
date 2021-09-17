package com.bassamalim.athkar.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.models.MyLocation;
import com.bassamalim.athkar.services.DailyUpdateService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.Calendar;

public class DailyUpdateReceiver extends BroadcastReceiver {

    private Context context;
    private int time;

    @Override
    public void onReceive(Context gContext, Intent intent) {
        Log.i(Constants.TAG, "in daily update receiver");
        context = gContext;

        if (intent.getAction().equals("daily")) {
            time = intent.getIntExtra("time", 0);
            if (needed())
                process();
            else
                Log.i(Constants.TAG, "dead intent walking in daily update receiver");
        }
        else if (intent.getAction().equals("boot"))
            process();
    }

    private boolean needed() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int day = pref.getInt("last_day", 0);

        Calendar supposed = Calendar.getInstance();
        supposed.set(Calendar.HOUR_OF_DAY, time);

        Calendar now = Calendar.getInstance();

        return day != now.get(Calendar.DAY_OF_MONTH) && time >= now.get(Calendar.HOUR_OF_DAY);
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
