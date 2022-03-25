package bassamalim.hidaya.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.Calendar;

import bassamalim.hidaya.R;
import bassamalim.hidaya.enums.ID;
import bassamalim.hidaya.other.Const;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.receivers.NotificationReceiver;

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

    /**
     * Creates a new instance of the application context and a new instance of the SharedPreferences
     * object
     */
    private void setUp() {
        appContext = context.getApplicationContext();
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Finds out if the desired function and executes it
     */
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

    /**
     * It sets the alarm, using the proper method
     */
    private void setAlarm() {
        Log.i(Const.TAG, "in set alarm");
        if (id.ordinal() >= 0 && id.ordinal() < 6)
            setPrayerAlarm(id);
        else if (id.ordinal() >= 6 && id.ordinal() < 10)
            setExtraAlarm(id);
    }

    private void setPrayerAlarms() {
        Log.i(Const.TAG, "in set prayer alarms");
        for (int i = 0; i < times.length; i++) {
            ID mappedId = Utils.mapID(i);
            assert mappedId != null;
            if (pref.getInt(mappedId + "notification_type", 2) != 0)
                setPrayerAlarm(mappedId);
        }
    }

    /**
     * Set an alarm for the given prayer time
     *
     * @param id the ID of the prayer
     */
    private void setPrayerAlarm(ID id) {
        Log.i(Const.TAG, "in set alarm for: " + id);

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

            AlarmManager myAlarm =
                    (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, id.ordinal(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE);

            myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, adjusted, pendingIntent);

            Log.i(Const.TAG, "alarm " + id + " set");
        }
        else
            Log.i(Const.TAG, id + " Passed");
    }

    /**
     * Set the extra alarms based on the user preferences
     */
    private void setExtraAlarms() {
        Log.i(Const.TAG, "in set extra alarms");

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

    /**
     * Set an alarm for a specific time
     *
     * @param id The ID of the alarm.
     */
    private void setExtraAlarm(ID id) {
        Log.i(Const.TAG, "in set extra alarm");

        int defaultH = 0;
        int defaultM = 0;
        switch (id) {
            case MORNING:
                defaultH = 5;
                break;
            case EVENING:
                defaultH = 16;
                break;
            case DAILY_WERD:
                defaultH = 21;
                break;
            case FRIDAY_KAHF:
                defaultH = 13;
                break;
        }

        int hour = pref.getInt(id + "hour", defaultH);
        int minute = pref.getInt(id + "minute", defaultM);

        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(appContext, NotificationReceiver.class);
        intent.setAction("extra");
        intent.putExtra("id", id.ordinal());
        intent.putExtra("time", time.getTimeInMillis());

        AlarmManager myAlarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, id.ordinal(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                pendingIntent);

        Log.i(Const.TAG, "alarm " + id + " set");
    }

}
