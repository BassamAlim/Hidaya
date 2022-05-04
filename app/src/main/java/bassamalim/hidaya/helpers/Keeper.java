package bassamalim.hidaya.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.preference.PreferenceManager;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.MyLocation;
import bassamalim.hidaya.other.Utils;

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
        storeTimes(Utils.getTimes(context, gLocation));
        storeStrTimes(getStrTimes(gLocation));
    }

    /**
     * It sets up the shared preferences and the gson object.
     */
    private void setUp() {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
    }

    /**
     * Store the location in the shared preferences
     *
     * @param gLocation The Location object that you want to store.
     */
    public void storeLocation(Location gLocation) {
        MyLocation loc = new MyLocation(gLocation);
        SharedPreferences.Editor editor = pref.edit();

        lJson = gson.toJson(loc);
        editor.putString("stored location", lJson);
        editor.apply();
    }

    /**
     * Stores the times in the array times in the shared preferences
     *
     * @param times The array of Calendar objects that you want to store.
     */
    public void storeTimes(Calendar[] times) {
        SharedPreferences.Editor editor = pref.edit();
        tJson = gson.toJson(times);
        editor.putString("stored times", tJson);
        editor.apply();
    }

    /**
     * It stores the times in the shared preferences.
     *
     * @param times The array of times to store.
     */
    public void storeStrTimes(String[] times) {
        SharedPreferences.Editor editor = pref.edit();
        tJson = gson.toJson(times);
        editor.putString("stored string times", tJson);
        editor.apply();
    }

    /**
     * Retrieve the stored location from the shared preferences
     *
     * @return The location object.
     */
    public Location retrieveLocation() {
        lJson = pref.getString("stored location", "");
        MyLocation myLocation = gson.fromJson(lJson, MyLocation.class);
        if (myLocation == null)
            return null;
        else
            return MyLocation.toLocation(myLocation);
    }

    /**
     * Retrieves the stored times from the shared preferences
     *
     * @return An array of Calendar objects.
     */
    public Calendar[] retrieveTimes() {
        tJson = pref.getString("stored times", "");
        return gson.fromJson(tJson, Calendar[].class);
    }

    /**
     * Retrieves the stored string times from the shared preferences
     *
     * @return An array of strings.
     */
    public String[] retrieveStrTimes() {
        tJson = pref.getString("stored string times", "");
        return gson.fromJson(tJson, String[].class);
    }

    /**
     * This function takes a Location object and returns an array of strings representing the prayer
     * times for that location
     *
     * @param loc Location object
     * @return An array of strings.
     */
    private String[] getStrTimes(Location loc) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        return reformatTimes(new PrayTimes(context).getPrayerTimes(calendar, loc.getLatitude(),
                loc.getLongitude(), timezone));
    }

    /**
     * Given a list of strings, reformat the strings into a list of strings, where each string is a
     * time
     *
     * @param givenTimes an ArrayList of the times in the given prayer times
     * @return An array of strings.
     */
    private String[] reformatTimes(ArrayList<String> givenTimes) {
        String[] arr = new String[givenTimes.size()-1];
        String[] prayerNames = context.getResources().getStringArray(R.array.prayer_names);

        int index = 0;
        for (int i = 0; i < 5; i++) {
            arr[i] = prayerNames[index] + "\n" + givenTimes.get(index);

            if (++index == 1)
                index = 2;
        }

        return arr;
    }

}
