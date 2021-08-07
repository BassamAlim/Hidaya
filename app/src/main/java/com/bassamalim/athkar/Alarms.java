package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bassamalim.athkar.receivers.DailyUpdateReceiver;
import com.bassamalim.athkar.receivers.NotificationReceiver;
import com.google.gson.Gson;
import java.util.Calendar;
import java.util.Date;

public class Alarms extends AppCompatActivity {

    Context context;
    Context appContext;
    Gson gson;
    SharedPreferences myPrefs;
    DataSaver saver;
    Calendar[] times;
    Location location;
    String json;

    public Alarms(Context gContext) {
        context = gContext;
        location = retrieveLocation();
        times = retrieveTimes();

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
        location = retrieveLocation();
        times = retrieveTimes();

        setAlarms();
    }

    public Alarms(Context gContext, Location gLocation, Calendar[] gTimes) {
        context = gContext;
        location = gLocation;
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

            PendingIntent pendIntent = PendingIntent.getBroadcast(appContext, prayer+1,
                    intent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager myAlarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

            myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, times[i].getTimeInMillis(), pendIntent);
        }
    }

    public Location retrieveLocation() {
        gson = new Gson();
        myPrefs = getApplicationContext().getSharedPreferences("location", MODE_PRIVATE);
        json = myPrefs.getString("location", "");
        saver = gson.fromJson(json, DataSaver.class);

        return saver.location;
    }

    public Calendar[] retrieveTimes() {
        gson = new Gson();
        myPrefs = getApplicationContext().getSharedPreferences("times", MODE_PRIVATE);
        json = myPrefs.getString("times", "");
        saver = gson.fromJson(json, DataSaver.class);

        return saver.times;
    }

    public Calendar[] getTimes() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        return new PrayTimes().getPrayerTimesArray(calendar, location.getLatitude(),
                location.getLongitude(), Constants.TIME_ZONE);
    }

}
