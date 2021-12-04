package com.bassamalim.hidaya.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.preference.PreferenceManager;

import com.bassamalim.hidaya.models.MyLocation;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Keeper {

    private final Context context;
    private Gson gson;
    private SharedPreferences pref;
    private String lJson;
    private String tJson;

    public Keeper(Context gContext) {
        context = gContext;
        setUp();
    }

    public Keeper(Context gContext, Location gLocation) {
        context = gContext;
        setUp();
        storeLocation(gLocation);
        storeTimes(getTimes(gLocation));
        storeStrTimes(getStrTimes(gLocation));
    }

    private void setUp() {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
    }

    public void storeLocation(Location gLocation) {
        MyLocation loc = new MyLocation(gLocation);
        SharedPreferences.Editor editor = pref.edit();

        lJson = gson.toJson(loc);
        editor.putString("stored location", lJson);
        editor.apply();
    }

    public void storeTimes(Calendar[] times) {
        SharedPreferences.Editor editor = pref.edit();
        tJson = gson.toJson(times);
        editor.putString("stored times", tJson);
        editor.apply();
    }

    public void storeStrTimes(String[] times) {
        SharedPreferences.Editor editor = pref.edit();
        tJson = gson.toJson(times);
        editor.putString("stored string times", tJson);
        editor.apply();
    }

    public Location retrieveLocation() {
        lJson = pref.getString("stored location", "");
        MyLocation myLocation = gson.fromJson(lJson, MyLocation.class);
        if (myLocation == null)
            return null;
        else
            return MyLocation.toLocation(myLocation);
    }

    public Calendar[] retrieveTimes() {
        tJson = pref.getString("stored times", "");
        return gson.fromJson(tJson, Calendar[].class);
    }

    public String[] retrieveStrTimes() {
        tJson = pref.getString("stored string times", "");
        return gson.fromJson(tJson, String[].class);
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

    private String[] getStrTimes(Location loc) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        return reformatTimes(new PrayTimes().getPrayerTimes(calendar, loc.getLatitude(),
                loc.getLongitude(), timezone));
    }

    private String[] reformatTimes(ArrayList<String> givenTimes) {
        String[] arr = new String[givenTimes.size()-1];

        arr[0] = "الفجر\n" + givenTimes.get(0);
        arr[1] = "الظهر\n" + givenTimes.get(2);
        arr[2] = "العصر\n" + givenTimes.get(3);
        arr[3] = "المغرب\n" + givenTimes.get(4);
        arr[4] = "العشاء\n" + givenTimes.get(5);

        return arr;
    }

}
