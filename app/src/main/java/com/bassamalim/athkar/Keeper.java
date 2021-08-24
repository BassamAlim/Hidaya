package com.bassamalim.athkar;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import androidx.appcompat.app.AppCompatActivity;
import com.bassamalim.athkar.models.MyLocation;
import com.google.gson.Gson;

public class Keeper extends AppCompatActivity {

    private final Context context;
    private Gson gson;
    private SharedPreferences myPrefs;
    private String json;

    public Keeper(Context gContext) {
        context = gContext;
    }

    public Keeper(Context gContext, Location gLocation) {
        context = gContext;
        storeLocation(gLocation);
    }

    public void storeLocation(Location gLocation) {
        MyLocation loc = new MyLocation(gLocation);

        myPrefs = context.getSharedPreferences("location file", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();

        gson = new Gson();
        json = gson.toJson(loc);

        editor.putString("stored location", json);
        editor.apply();
    }

    public Location retrieveLocation() {
        gson = new Gson();
        myPrefs = context.getSharedPreferences("location file", MODE_PRIVATE);

        json = myPrefs.getString("stored location", "");

        MyLocation myLocation = gson.fromJson(json, MyLocation.class);

        return MyLocation.toLocation(myLocation);
    }

}
