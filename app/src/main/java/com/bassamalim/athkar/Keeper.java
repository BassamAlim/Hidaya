package com.bassamalim.athkar;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.models.MyLocation;
import com.google.gson.Gson;

public class Keeper extends AppCompatActivity {

    private Gson gson;
    private final SharedPreferences pref;
    private String json;

    public Keeper(Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Keeper(Context context, Location gLocation) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        storeLocation(gLocation);
    }

    public void storeLocation(Location gLocation) {
        MyLocation loc = new MyLocation(gLocation);
        SharedPreferences.Editor editor = pref.edit();
        gson = new Gson();
        json = gson.toJson(loc);
        editor.putString("stored location", json);
        editor.apply();
    }

    public Location retrieveLocation() {
        gson = new Gson();
        json = pref.getString("stored location", "");
        MyLocation myLocation = gson.fromJson(json, MyLocation.class);
        return MyLocation.toLocation(myLocation);
    }

}
