package com.bassamalim.athkar.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.models.MyLocation;
import com.google.gson.Gson;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DailyUpdateService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Constants.TAG, "in daily update service");

        Location loc = new Location("");
        Calendar[] times1 = getTimes(loc);

        new Alarms(this, times1);

        if (intent != null && intent.getStringExtra("location") != null) {
            Gson gson = new Gson();
            String json = intent.getStringExtra("location");

            MyLocation myLocation = gson.fromJson(json, MyLocation.class);
            Location location = MyLocation.toLocation(myLocation);

            Calendar[] times = getTimes(location);

            new Alarms(this, times);

            updated();
        }
        else
            Log.e(Constants.TAG, "Something is null in DailyUpdateService");

        stopSelf();
        return START_REDELIVER_INTENT;
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

    private void updated() {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = myPref.edit();
        editor.putInt("last_day", today.get(Calendar.DAY_OF_MONTH));
        editor.apply();
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
