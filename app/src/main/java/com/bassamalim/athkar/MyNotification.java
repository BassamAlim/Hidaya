package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.bassamalim.athkar.receivers.NotificationReceiver;
import com.bassamalim.athkar.services.NotificationService;
import java.util.ArrayList;
import java.util.Calendar;

public class MyNotification {

    private static final int NOTIFICATION_ID = 1;
    private final Context CONTEXT = MainActivity.getInstance();
    public static Calendar[] times;
    public int prayer;
    private Intent myIntent;
    private PendingIntent pendIntent;
    private AlarmManager myAlarm;
    Calendar test;

    public MyNotification(Calendar[] givenTimes) {
        times = givenTimes;

        setAlarms(times);
        //test();
    }

    public void setAlarms(Calendar[] alarmTimes) {
        myIntent = new Intent(CONTEXT.getApplicationContext(), NotificationReceiver.class);

        pendIntent = PendingIntent.getBroadcast(CONTEXT.getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        myAlarm = (AlarmManager) CONTEXT.getSystemService(Context.ALARM_SERVICE);

        for (Calendar alarmTime : alarmTimes) {
            if (Calendar.getInstance().getTimeInMillis() <= alarmTime.getTimeInMillis()) {
                myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendIntent);
            }
        }

    }

    private void test() {
        test = Calendar.getInstance();
        test.setTimeInMillis(System.currentTimeMillis());
        test.set(Calendar.HOUR_OF_DAY, 1);
        test.set(Calendar.MINUTE, 41);
        test.set(Calendar.SECOND, 30);

        myIntent = new Intent(CONTEXT, NotificationReceiver.class);
        myIntent.putExtra("prayer", 1);
        myIntent.putExtra("time", test);

        pendIntent = PendingIntent.getBroadcast(CONTEXT.getApplicationContext(), 0, myIntent, PendingIntent.FLAG_ONE_SHOT);

        myAlarm = (AlarmManager) CONTEXT.getSystemService(Context.ALARM_SERVICE);

        myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, test.getTimeInMillis(), pendIntent);

        /*if (Calendar.getInstance().getTimeInMillis() <= test.getTimeInMillis())
            myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, test.getTimeInMillis(), pendIntent);
        else
            Toast.makeText(CONTEXT, "Passed", Toast.LENGTH_SHORT).show();*/
    }

}
