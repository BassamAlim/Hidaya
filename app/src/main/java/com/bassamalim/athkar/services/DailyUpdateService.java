package com.bassamalim.athkar.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.bassamalim.athkar.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.DataSaver;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.receivers.NotificationReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import java.util.Calendar;
import java.util.Date;

public class DailyUpdateService extends Service {

    FusedLocationProviderClient fusedLocationClient;
    Location location;
    public static Calendar[] times;
    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;
    private Calendar calendar;
    private Date now;
    public Gson gson;
    public static String json;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
                location = loc;
                storeLocation(location);

                times = getTimes();

                storeTimes(times);

                Alarms alarms = new Alarms(getApplicationContext(),loc, times);
            });
        }
        return START_STICKY;
    }


    public Calendar[] getTimes() {
        now = new Date();

        calendar = Calendar.getInstance();
        calendar.setTime(now);

        return new PrayTimes().getPrayerTimesArray(calendar, location.getLatitude(),
                location.getLongitude(), Constants.TIME_ZONE);
    }

    public void storeLocation(Location givenLocation) {
        myPrefs = this.getApplicationContext().getSharedPreferences("location", MODE_PRIVATE);
        editor = myPrefs.edit();

        DataSaver saver = new DataSaver();
        saver.location = givenLocation;

        gson = new Gson();

        json = gson.toJson(saver);

        editor.putString("location", json);
        editor.apply();
    }

    public void storeTimes(Calendar[] givenTimes) {
        myPrefs = getApplicationContext().getSharedPreferences("times", MODE_PRIVATE);
        //may produce null;
        editor = myPrefs.edit();

        DataSaver saver = new DataSaver();
        saver.times = givenTimes;

        gson = new Gson();

        json = gson.toJson(saver);

        editor.putString("times", json);
        editor.apply();
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
