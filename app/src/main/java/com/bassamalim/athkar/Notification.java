package com.bassamalim.athkar;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class Notification {

    private final String CHANNEL_ID = "channel id";
    private static final int NOTIFICATION_ID = 1;
    private NotificationCompat.Builder builder;
    private final Context context = MainActivity.getInstance();
    private Calendar time;
    private int prayer;

    public Notification() {

        Intent myIntent = new Intent(context, Notify.class);

        PendingIntent myPendIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager myAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        myAlarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time().getTimeInMillis(), myPendIntent);
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
        builder.setContentIntent(onClick());
        builder.setAutoCancel(true);
        return builder;
    }

    private PendingIntent onClick() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(MainActivity.getInstance(), QuranView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.getInstance(), 0, intent, 0);

        return pendingIntent;
    }

    private void send() {
        createNotificationChannel();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.getInstance());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }

    private Calendar time() {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, 6);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        return time;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "notification channel";
        String description = "blah blah blah, description";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = MainActivity.getInstance().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}
