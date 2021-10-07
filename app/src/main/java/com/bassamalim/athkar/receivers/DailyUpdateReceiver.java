package com.bassamalim.athkar.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.Keeper;
import com.bassamalim.athkar.PrayTimes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DailyUpdateReceiver extends BroadcastReceiver {

    private Context context;
    private SharedPreferences pref;
    private int time;

    @Override
    public void onReceive(Context gContext, Intent intent) {
        Log.i(Constants.TAG, "in daily update receiver");
        context = gContext;
        pref = PreferenceManager.getDefaultSharedPreferences(context);

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

            fusedLocationClient.getLastLocation().addOnSuccessListener(this::update);
        }
    }

    private void update(Location location) {
        if (location != null) {
            new Keeper(context, location);

            Calendar[] times = getTimes(location);

            new Alarms(context, times);

            updateWidget();

            updated();
        }
        else
            Log.e(Constants.TAG, "Location is null in DailyUpdateService");
    }

    private Calendar[] getTimes(Location loc) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        return new PrayTimes().getPrayerTimesArray(calendar, loc.getLatitude(),
                loc.getLongitude(), timezone);
    }

    private void updateWidget() {

    }

    private void updated() {
        Calendar today = Calendar.getInstance();

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("last_day", today.get(Calendar.DAY_OF_MONTH));
        editor.apply();
    }

}
