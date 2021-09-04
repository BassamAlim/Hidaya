package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.bassamalim.athkar.receivers.DailyUpdateReceiver;
import java.util.Calendar;

public class DailyUpdate extends AppCompatActivity {

    private int hourOfTheDay = 0;

    public DailyUpdate(Context context) {
        Log.i(Constants.TAG, "in daily update");

        Intent intent = new Intent(context, DailyUpdateReceiver.class);
        intent.setAction("daily");
        intent.putExtra("time", hourOfTheDay);

        PendingIntent pendIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager myAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        myAlarm.setRepeating(AlarmManager.RTC_WAKEUP, time().getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendIntent);
    }

    private Calendar time() {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.set(Calendar.HOUR_OF_DAY, hourOfTheDay);

        return time;
    }

}
