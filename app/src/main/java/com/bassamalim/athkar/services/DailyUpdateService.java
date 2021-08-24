package com.bassamalim.athkar.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.bassamalim.athkar.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.models.MyLocation;
import com.google.gson.Gson;
import java.util.Calendar;
import java.util.Date;

public class DailyUpdateService extends Service {

    private Location location;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Gson gson = new Gson();
        String json = intent.getStringExtra("location");

        MyLocation myLocation = gson.fromJson(json, MyLocation.class);
        location = MyLocation.toLocation(myLocation);

        Calendar[] times = getTimes();

        new Alarms(this, times);

        return START_REDELIVER_INTENT;
    }

    public Calendar[] getTimes() {
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        return new PrayTimes().getPrayerTimesArray(calendar, location.getLatitude(),
                location.getLongitude(), Constants.TIME_ZONE);
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
