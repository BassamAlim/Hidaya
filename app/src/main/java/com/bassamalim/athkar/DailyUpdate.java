package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.bassamalim.athkar.receivers.DailyUpdateReceiver;

import java.util.Calendar;

public class DailyUpdate {

    private int hourOfTheDay = 0;

    public DailyUpdate() {
        Context context = MainActivity.getInstance();

        Intent myIntent = new Intent(context.getApplicationContext(), DailyUpdateReceiver.class);

        PendingIntent pendIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager myAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        myAlarm.setRepeating(AlarmManager.RTC_WAKEUP, time().getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendIntent);
    }

    public Calendar time() {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.set(Calendar.HOUR_OF_DAY, hourOfTheDay);

        return time;
    }

}
