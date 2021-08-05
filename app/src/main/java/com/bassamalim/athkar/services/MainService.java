package com.bassamalim.athkar.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.receivers.NotificationReceiver;

import java.util.ArrayList;
import java.util.Calendar;


public class MainService extends Service {

    private final String CHANNEL_ID = "channel id";
    private static final int NOTIFICATION_ID = 1;
    private NotificationCompat.Builder builder;
    private final Context CONTEXT = MainActivity.getInstance();
    public static Calendar[] times;
    public int prayer;
    private Intent myIntent;
    private PendingIntent pendIntent;
    private AlarmManager myAlarm;
    Calendar test;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {




        return super.onStartCommand(intent, flags, startId);
    }

    public void setAlarms(Calendar[] alarmTimes) {
        myIntent = new Intent(CONTEXT.getApplicationContext(), NotificationReceiver.class);
        myIntent.putExtra("prayer", prayer);
        //myIntent.setAction("com.bassamalim.athkar.");

        pendIntent = PendingIntent.getBroadcast(CONTEXT.getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        myAlarm = (AlarmManager) CONTEXT.getSystemService(Context.ALARM_SERVICE);

        for (Calendar alarmTime : alarmTimes)
            myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendIntent);

    }

    private Calendar[] formatTimes(ArrayList<String> givenTimes) {
        Calendar[] formattedTimes = new Calendar[givenTimes.size()];

        for (int i = 0; i < givenTimes.size(); i++) {
            formattedTimes[i] = Calendar.getInstance();

            formattedTimes[i].setTimeInMillis(System.currentTimeMillis());
            formattedTimes[i].set(Calendar.HOUR_OF_DAY, Integer.parseInt(givenTimes.get(i).substring(0, 2)));
            formattedTimes[i].set(Calendar.MINUTE, Integer.parseInt(givenTimes.get(i).substring(3, 5)));
        }
        return formattedTimes;
    }

}
