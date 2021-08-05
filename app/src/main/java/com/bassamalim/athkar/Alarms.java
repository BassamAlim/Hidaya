package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import androidx.appcompat.app.AppCompatActivity;

import com.bassamalim.athkar.receivers.DailyUpdateReceiver;
import com.google.gson.Gson;
import java.util.Calendar;

public class Alarms extends AppCompatActivity {

    Gson gson;
    SharedPreferences myPrefs;
    DataSaver saver;
    Calendar[] times;
    Location location;
    String json;

    public Alarms() {
        retrieveLocation();
        retrieveTimes();

        setAlarms();
    }

    public void setAlarms() {
        for (int i = 0; i < times.length; i++) {
            int prayer = i;

            Intent intent = new Intent(getApplicationContext(), DailyUpdateReceiver.class);
            intent.putExtra("prayer", prayer);

            PendingIntent pendIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager myAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            myAlarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, times[i].getTimeInMillis(), pendIntent);
        }
    }

    public Location retrieveLocation() {
        gson = new Gson();

        myPrefs = getApplicationContext().getSharedPreferences("location", MODE_PRIVATE);

        json = myPrefs.getString("location", "");

        saver = gson.fromJson(json, DataSaver.class);

        location = saver.location;

        return location;
    }

    public Calendar[] retrieveTimes() {
        gson = new Gson();

        myPrefs = getApplicationContext().getSharedPreferences("times", MODE_PRIVATE);

        json = myPrefs.getString("times", "");

        saver = gson.fromJson(json, DataSaver.class);

        times = saver.times;

        return times;
    }

}
