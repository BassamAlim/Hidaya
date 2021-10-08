package com.bassamalim.athkar.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.receivers.NotificationReceiver;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Alarms {

    private final Context context;
    private Context appContext;
    private final Calendar[] times;
    private SharedPreferences pref;
    private String action;
    private int num;

    public Alarms(Context gContext, Calendar[] gTimes) {
        context = gContext;
        times = gTimes;
        action = "all";

        setUp();
        recognize();
    }

    public Alarms(Context gContext, int alarm) {
        context = gContext;
        num = alarm;

        times = new Keeper(context).retrieveTimes();

        setUp();
        setAlarm();
    }

    private void setUp() {
        appContext = context.getApplicationContext();
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void recognize() {
        switch (action) {
            case "all":
                setPrayerAlarms();
                setExtraAlarms();
                break;
            case "prayers":
                setPrayerAlarms();
                break;
            case "extra":
                setExtraAlarms();
                break;
        }
    }

    private void setAlarm() {
        Log.i(Constants.TAG, "in set alarm");
        if (num >= 0 && num < 6)
            setPrayerAlarm(num);
        else if (num >= 6 && num < 10)
            setExtraAlarm(num);
    }

    private void setPrayerAlarms() {
        Log.i(Constants.TAG, "in set prayer alarms");
        for (int i = 0; i < times.length; i++) {
            if (pref.getInt(i + "notification_type", 2) != 0)
                setPrayerAlarm(i);
        }
    }

    private void setPrayerAlarm(int id) {
        Log.i(Constants.TAG, "in set alarm for: " + id);

        // adjust the time with the delay
        long adjustment = pref.getLong(id + "time_adjustment", 0);
        long adjusted = times[id].getTimeInMillis() + adjustment;

        if (System.currentTimeMillis() <= adjusted) {
            Intent intent = new Intent(appContext, NotificationReceiver.class);
            if (id == 1)
                intent.setAction("extra");
            else
                intent.setAction("prayer");
            intent.putExtra("id", id);
            intent.putExtra("time", adjusted);
            PendingIntent pendingIntent;
            AlarmManager myAlarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getBroadcast(appContext, id,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        times[id].getTimeInMillis(), pendingIntent);
            }
            else {
                pendingIntent = PendingIntent.getBroadcast(appContext, id,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

                myAlarm.setExact(AlarmManager.RTC_WAKEUP,
                        times[id].getTimeInMillis(), pendingIntent);
            }
            Log.i(Constants.TAG, "alarm " + id + " set");
        }
        else
            Log.i(Constants.TAG, id + " Passed");
    }

    private void setExtraAlarms() {
        Log.i(Constants.TAG, "in set extra alarms");

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        if (pref.getBoolean(context.getString(R.string.morning_athkar_key), true))
            setExtraAlarm(6);
        if (pref.getBoolean(context.getString(R.string.night_athkar_key), true))
            setExtraAlarm(7);
        if (pref.getBoolean(context.getString(R.string.daily_page_key), true))
            setExtraAlarm(8);
        if (pref.getBoolean(context.getString(R.string.friday_kahf_key), true)
                && today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
            setExtraAlarm(9);
    }

    private void setExtraAlarm(int id) {
        Log.i(Constants.TAG, "in set extra alarm");
        int hour;
        int minute;

        if (id == 9) {
            Location loc = new Keeper(appContext).retrieveLocation();
            Calendar[] times = getTimes(loc);
            Calendar duhr = times[2];
            hour = duhr.get(Calendar.HOUR_OF_DAY)+1;
            minute = duhr.get(Calendar.MINUTE);
        }
        else {
            int defHour = 0;
            int defMinute = 0;
            switch (id) {
                case 6:
                    defHour = 5;
                    break;
                case 7:
                    defHour = 16;
                    break;
                case 8:
                    defHour = 21;
                    break;
            }
            hour = pref.getInt(id + "hour", defHour);
            minute = pref.getInt(id + "minute", defMinute);
        }

        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);

        Intent intent = new Intent(appContext, NotificationReceiver.class);
        intent.setAction("extra");
        intent.putExtra("id", id);
        intent.putExtra("time", time.getTimeInMillis());

        PendingIntent pendIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendIntent = PendingIntent.getBroadcast(appContext, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pendIntent = PendingIntent.getBroadcast(appContext, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        AlarmManager myAlarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        myAlarm.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendIntent);

        Log.i(Constants.TAG, "alarm " + id + " set");
    }

    private Calendar[] getTimes(Location loc) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        return new PrayTimes().getPrayerTimesArray(calendar, loc.getLatitude(),
                loc.getLongitude(), timezone);
    }

    public static void cancelAlarm(Context gContext, int id) {
        Log.i(Constants.TAG, "in cancel alarm");
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(gContext, id, new Intent(),
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pendingIntent = PendingIntent.getBroadcast(gContext, id,
                    new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        }

        AlarmManager am = (AlarmManager) gContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

}
