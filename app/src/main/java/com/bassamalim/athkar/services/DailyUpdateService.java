package com.bassamalim.athkar.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.bassamalim.athkar.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.models.MyLocation;
import com.google.gson.Gson;
import java.util.Calendar;
import java.util.Date;

public class DailyUpdateService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Constants.TAG, "in daily update service");
        if (intent != null && intent.getStringExtra("location") != null) {
            Gson gson = new Gson();
            String json = intent.getStringExtra("location");

            MyLocation myLocation = gson.fromJson(json, MyLocation.class);
            Location location = MyLocation.toLocation(myLocation);

            Calendar[] times = getTimes(location);

            new Alarms(this, times);

            stopSelf();
        }
        else {
            Log.e(Constants.TAG, "Something is null in DailyUpdateService");
            stopSelf();
        }


        return START_REDELIVER_INTENT;
    }

    public Calendar[] getTimes(Location loc) {
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        return new PrayTimes().getPrayerTimesArray(calendar, loc.getLatitude(),
                loc.getLongitude(), Constants.TIME_ZONE);
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
