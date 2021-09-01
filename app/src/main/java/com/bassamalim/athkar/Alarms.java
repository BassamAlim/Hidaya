package com.bassamalim.athkar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.bassamalim.athkar.receivers.NotificationReceiver;
import java.util.Calendar;
import java.util.Date;

public class Alarms extends AppCompatActivity {

    private final Context context;
    private final Activity activity;
    private Context appContext;
    private final Calendar[] times;
    private SharedPreferences myPref;
    private SharedPreferences mySharedPref;

    public Alarms(Context gContext) {
        context = gContext;
        activity = (Activity) gContext;

        Location location = new Keeper(gContext).retrieveLocation();
        times = getTimes(location);

        setup();
    }

    public Alarms(Context gContext, Calendar[] gTimes) {
        context = gContext;
        activity = (Activity) gContext;

        times = gTimes;

        setup();
    }

    private void setup() {
        appContext = context.getApplicationContext();

        myPref = activity.getPreferences(MODE_PRIVATE);
        mySharedPref = context.getSharedPreferences("daily_page_time", MODE_PRIVATE);

        setAlarms();
        setExtraAlarms();
    }

    private void setAlarms() {
        Log.i(Constants.TAG, "in set alarms");
        for (int i = 1; i <= times.length; i++) {
            if (System.currentTimeMillis() <= times[i-1].getTimeInMillis()) {
                Intent intent = new Intent(appContext, NotificationReceiver.class);
                intent.setAction("prayer");
                intent.putExtra("id", i);
                intent.putExtra("time", times[i-1].getTimeInMillis());
                PendingIntent pendingIntent;
                AlarmManager myAlarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pendingIntent = PendingIntent.getBroadcast(appContext, i,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                            // maybe change to one shot
                    myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            times[i-1].getTimeInMillis(), pendingIntent);
                }
                else {
                    pendingIntent = PendingIntent.getBroadcast(appContext, i,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        // maybe change to one shot
                    myAlarm.setExact(AlarmManager.RTC_WAKEUP,
                            times[i-1].getTimeInMillis(), pendingIntent);
                }
                Log.i(Constants.TAG, "alarm " + i + " set");
            }
            else
                Log.i(Constants.TAG, i + " Passed");
        }
    }

    private void setExtraAlarms() {
        Log.i(Constants.TAG, "in set extra alarms");
        int id;
        if (myPref.getBoolean(context.getString(R.string.daily_page_key), true)) {
            id = 8;

            int hour = mySharedPref.getInt("hour", 21);
            int minute = mySharedPref.getInt("minute", 0);
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(System.currentTimeMillis());
            time.set(Calendar.HOUR_OF_DAY, hour);
            time.set(Calendar.MINUTE, minute);

            if (System.currentTimeMillis() <= time.getTimeInMillis()) {
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
            else
                Log.i(Constants.TAG, id + " Passed");
        }
        else
            Log.i(Constants.TAG, "Daily alarms are off in settings");
    }

    private Calendar[] getTimes(Location loc) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        return new PrayTimes().getPrayerTimesArray(calendar, loc.getLatitude(),
                loc.getLongitude(), Constants.TIME_ZONE);
    }

}
