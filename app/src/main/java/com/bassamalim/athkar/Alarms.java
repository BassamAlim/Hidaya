package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import com.bassamalim.athkar.models.DataSaver;
import com.bassamalim.athkar.receivers.NotificationReceiver;
import com.google.gson.Gson;
import java.util.Calendar;
import java.util.Date;

public class Alarms extends AppCompatActivity {

    Context context;
    Context appContext;
    Keeper keeper;
    Calendar[] times;
    Location location;

    public Alarms(Context gContext) {
        context = gContext;
        keeper = new Keeper(this);
        location = keeper.retrieveLocation();
        times = getTimes();

        setAlarms();
    }

    public Alarms(Context gContext, Location gLocation) {
        context = gContext;
        location = gLocation;
        times = getTimes();

        setAlarms();
    }

    public Alarms(Context gContext, Calendar[] gTimes) {
        context = gContext;
        times = gTimes;

        setAlarms();
    }

    public void setAlarms() {
        appContext = context.getApplicationContext();
        for (int i = 0; i < times.length; i++) {
            int prayer = i;

            Intent intent = new Intent(appContext, NotificationReceiver.class);
            intent.putExtra("prayer", prayer);
            intent.putExtra("time", times[i].getTimeInMillis());

            PendingIntent pendIntent = PendingIntent.getBroadcast(appContext, prayer,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager myAlarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        times[i].getTimeInMillis(), pendIntent);
            }
            else {
                myAlarm.setExact(AlarmManager.RTC_WAKEUP,
                        times[i].getTimeInMillis(), pendIntent);
            }

        }
    }

    public Calendar[] getTimes() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        return new PrayTimes().getPrayerTimesArray(calendar, location.getLatitude(),
                location.getLongitude(), Constants.TIME_ZONE);
    }

}
