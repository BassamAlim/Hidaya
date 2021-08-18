package com.bassamalim.athkar.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.bassamalim.athkar.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.PrayTimes;
import java.util.Calendar;
import java.util.Date;

public class DailyUpdateService extends Service {

    Location location;
    public static Calendar[] times;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        location = intent.getParcelableExtra("location");

        times = getTimes();

        new Alarms(this, times);

        return START_STICKY;
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
