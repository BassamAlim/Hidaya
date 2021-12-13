package bassamalim.hidaya.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import bassamalim.hidaya.other.Constants;
import bassamalim.hidaya.R;
import bassamalim.hidaya.other.ID;
import bassamalim.hidaya.receivers.NotificationReceiver;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Alarms {

    private final Context context;
    private Context appContext;
    private final Calendar[] times;
    private SharedPreferences pref;
    private String action;
    private ID id;

    public Alarms(Context gContext, Calendar[] gTimes) {
        context = gContext;
        times = gTimes;
        action = "all";

        setUp();
        recognize();
    }

    public Alarms(Context gContext, ID id) {
        context = gContext;
        this.id = id;

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
        if (id.ordinal() >= 0 && id.ordinal() < 6)
            setPrayerAlarm(id);
        else if (id.ordinal() >= 6 && id.ordinal() < 10)
            setExtraAlarm(id);
    }

    private void setPrayerAlarms() {
        Log.i(Constants.TAG, "in set prayer alarms");
        for (int i = 0; i < times.length; i++) {
            ID mappedId = mapID(i);
            assert mappedId != null;
            if (pref.getInt(mappedId + "notification_type", 2) != 0)
                setPrayerAlarm(mappedId);
        }
    }

    private void setPrayerAlarm(ID id) {
        Log.i(Constants.TAG, "in set alarm for: " + id);

        // adjust the time with the delay
        long adjustment = pref.getLong(id + "time_adjustment", 0);
        long adjusted = times[id.ordinal()].getTimeInMillis() + adjustment;

        if (System.currentTimeMillis() <= adjusted) {
            Intent intent = new Intent(appContext, NotificationReceiver.class);
            if (id == ID.SHOROUQ)
                intent.setAction("extra");
            else
                intent.setAction("prayer");
            intent.putExtra("id", id.ordinal());
            intent.putExtra("time", adjusted);
            PendingIntent pendingIntent;
            AlarmManager myAlarm = (AlarmManager)
                    appContext.getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getBroadcast(appContext, id.ordinal(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        times[id.ordinal()].getTimeInMillis(), pendingIntent);
            }
            else {
                pendingIntent = PendingIntent.getBroadcast(appContext, id.ordinal(),
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

                myAlarm.setExact(AlarmManager.RTC_WAKEUP,
                        times[id.ordinal()].getTimeInMillis(), pendingIntent);
            }
            Log.i(Constants.TAG, "alarm " + id + " set");
        }
        else
            Log.i(Constants.TAG, id + " Passed");
    }

    private void setExtraAlarms() {
        Log.i(Constants.TAG, "in set extra alarms");

        Calendar today = Calendar.getInstance();

        if (pref.getBoolean(context.getString(R.string.morning_athkar_key), true))
            setExtraAlarm(ID.MORNING);
        if (pref.getBoolean(context.getString(R.string.evening_athkar_key), true))
            setExtraAlarm(ID.EVENING);
        if (pref.getBoolean(context.getString(R.string.daily_werd_key), true))
            setExtraAlarm(ID.DAILY_WERD);
        if (pref.getBoolean(context.getString(R.string.friday_kahf_key), true)
                && today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
            setExtraAlarm(ID.FRIDAY_KAHF);
    }

    private void setExtraAlarm(ID id) {
        Log.i(Constants.TAG, "in set extra alarm");
        int hour;
        int minute;

        Location loc = new Keeper(appContext).retrieveLocation();
        if (id == ID.FRIDAY_KAHF && loc != null) {
            Calendar[] times = getTimes(loc);
            Calendar duhr = times[2];
            hour = duhr.get(Calendar.HOUR_OF_DAY)+1;
            minute = duhr.get(Calendar.MINUTE);
        }
        else {
            int defHour = 0;
            int defMinute = 0;
            switch (id) {
                case MORNING:
                    defHour = 5;
                    break;
                case EVENING:
                    defHour = 16;
                    break;
                case DAILY_WERD:
                    defHour = 21;
                    break;
                case FRIDAY_KAHF:
                    defHour = 13;
                    break;
            }
            hour = pref.getInt(id + "hour", defHour);
            minute = pref.getInt(id + "minute", defMinute);
        }

        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(appContext, NotificationReceiver.class);
        intent.setAction("extra");
        intent.putExtra("id", id.ordinal());
        intent.putExtra("time", time.getTimeInMillis());

        PendingIntent pendIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendIntent = PendingIntent.getBroadcast(appContext, id.ordinal(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pendIntent = PendingIntent.getBroadcast(appContext, id.ordinal(), intent,
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

    public static void cancelAlarm(Context gContext, ID id) {
        Log.i(Constants.TAG, "in cancel alarm");
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(gContext, id.ordinal(), new Intent(),
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pendingIntent = PendingIntent.getBroadcast(gContext, id.ordinal(),
                    new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        }

        AlarmManager am = (AlarmManager) gContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    private ID mapID(int num) {
        switch (num) {
            case 0: return ID.FAJR;
            case 1: return ID.SHOROUQ;
            case 2: return ID.DUHR;
            case 3: return ID.ASR;
            case 4: return ID.MAGHRIB;
            case 5: return ID.ISHAA;
            default: return null;
        }
    }

}
