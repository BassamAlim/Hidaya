package com.bassamalim.athkar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class Notify extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 3;
    private final String CHANNEL_ID = "channel id";
    private Calendar time;
    private NotificationCompat.Builder builder;


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MyNewIntentService.class);
        context.startService(intent1);
    }

    public NotificationCompat.Builder getData(int prayer) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.getInstance(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_athan);

        switch (prayer) {
            case 1: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("قوم صلي ال");
                break;
            }
            case 2: {
                builder.setContentTitle("صلاة الظهر");
                builder.setContentText("قوم صلي الظهر");
                break;
            }
            case 3: {
                builder.setContentTitle("صلاة العصر");
                builder.setContentText("قوم صلي العصر");
                break;
            }
            case 4: {
                builder.setContentTitle("صلاة المغرب");
                builder.setContentText("قوم صلي المغرب");
                break;
            }
            case 5: {
                builder.setContentTitle("صلاة العشاء");
                builder.setContentText("قوم صلي العشاء");
                break;
            }
        }

        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //builder.setContentIntent(onClick());
        builder.setAutoCancel(true);
        return builder;
    }

}
