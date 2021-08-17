package com.bassamalim.athkar;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import androidx.appcompat.app.AppCompatActivity;
import com.bassamalim.athkar.models.DataSaver;
import com.google.gson.Gson;
import java.util.Calendar;

public class Keeper extends AppCompatActivity {

    private Context context;
    private Gson gson;
    private SharedPreferences myPrefs;
    private String json;
    private DataSaver saver;
    private SharedPreferences.Editor editor;

    public Keeper(Context gContext) {
        context = gContext;
    }

    public Keeper(Context gContext, Location gLocation) {
        context = gContext;
        storeLocation(gLocation);
    }

    public Keeper(Context gContext, Calendar[] gTimes) {
        context = gContext;
        storeTimes(gTimes);
    }

    public Keeper(Context gContext, Location gLocation, Calendar[] gTimes) {
        context = gContext;
        storeLocation(gLocation);
        storeTimes(gTimes);
    }

    public void storeLocation(Location gLocation) {
        myPrefs = context.getSharedPreferences("location", Context.MODE_PRIVATE);
        editor = myPrefs.edit();

        gson = new Gson();
        json = gson.toJson(gLocation);

        editor.putString("location", json);
        editor.apply();
    }

    public void storeTimes(Calendar[] gTimes) {
        myPrefs = context.getSharedPreferences("times", Context.MODE_PRIVATE);
        editor = myPrefs.edit();

        DataSaver saver = new DataSaver();
        saver.times = gTimes;

        gson = new Gson();
        json = gson.toJson(saver);

        editor.putString("times", json);
        editor.apply();
    }

    public Location retrieveLocation() {
        gson = new Gson();
        myPrefs = context.getSharedPreferences("location", MODE_PRIVATE);
        json = myPrefs.getString("location", "");

        return gson.fromJson(json, Location.class);
    }

    public Calendar[] retrieveTimes() {
        gson = new Gson();
        myPrefs = context.getSharedPreferences("times", MODE_PRIVATE);
        json = myPrefs.getString("times", "");
        saver = gson.fromJson(json, DataSaver.class);

        return saver.times;
    }

}
