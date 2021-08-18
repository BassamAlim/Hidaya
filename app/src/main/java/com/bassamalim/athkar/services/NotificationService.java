package com.bassamalim.athkar.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.bassamalim.athkar.Splash;
import com.bassamalim.athkar.views.AlathkarView;
import com.bassamalim.athkar.R;

public class NotificationService extends Service {

    public static boolean isServiceRunning = false;
    private static int notificationId;
    private String channelId = "Athan";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                createNotificationChannel(notificationManager) : "Athan";

        int prayer = intent.getIntExtra("prayer", 0);

        Notification notification = buildNotification(prayer);

        //startForeground(NOTIFICATION_ID, notification);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        notificationId = prayer;

        managerCompat.notify(notificationId, notification);

        return START_STICKY;
    }

    public Notification buildNotification(int prayer) {
        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(this, "Athan");
        builder.setSmallIcon(R.drawable.ic_athan);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_athan);
        builder.setLargeIcon(icon);
        builder.setTicker(getResources().getString(R.string.app_name));

        switch (prayer) {
            case 0: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("حان موعد أذان الفجر");
                break;
            }
            case 1: {
                builder.setContentTitle("وقت الشروق");
                builder.setContentText("إقرأ أذكار الصباح");
                break;
            }
            case 2: {
                builder.setContentTitle("صلاة الظهر");
                builder.setContentText("حان موعد أذان الظهر");
                break;
            }
            case 3: {
                builder.setContentTitle("صلاة العصر");
                builder.setContentText("حان موعد أذان العصر");
                break;
            }
            case 4: {
                builder.setContentTitle("وقت الغروب");
                builder.setContentText("إقرأ أذكار المساء");
                break;
            }
            case 5: {
                builder.setContentTitle("صلاة المغرب");
                builder.setContentText("حان موعد أذان المغرب");
                break;
            }
            case 6: {
                builder.setContentTitle("صلاة العشاء");
                builder.setContentText("حان موعد أذان العشاء");
                break;
            }
        }
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);
        builder.setColor(getColor(R.color.secondary));
        builder.setContentIntent(onClick(prayer));
        //builder.setOngoing(true);
        //builder.setDeleteIntent(contentPendingIntent)  // if needed

        Notification notification = builder.build();

        // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        //notification.flags = Notification.FLAG_NO_CLEAR;

        return notification;
    }

    private String createNotificationChannel(NotificationManager notificationManager) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel Athan;
            channelId = "Athan";
            CharSequence name = "Athan Channel";
            String description = "The channel that gives athan notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            Athan = new NotificationChannel(channelId, name, importance);
            Athan.setDescription(description);
            Athan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            //notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(Athan);
        }
        return channelId;
    }

    private PendingIntent onClick(int prayer) {
        // Create an explicit intent for an Activity in your app
        Intent intent;
        PendingIntent pendingIntent;

        if (prayer == 1) {
            intent = new Intent(this, AlathkarView.class);
            intent.putExtra("thikrs", getResources().getStringArray(R.array.morning));
            intent.putExtra("title", "أذكار الصباح");
        }
        else if (prayer == 4) {
            intent = new Intent(this, AlathkarView.class);
            intent.putExtra("thikrs", getResources().getStringArray(R.array.night));
            intent.putExtra("title", "أذكار المساء");
        }
        else
            intent = new Intent(this, Splash.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this,
                prayer, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    /*void stopMyService() {
        stopForeground(true);
        stopSelf();
        isServiceRunning = false;
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        isServiceRunning = false;
    }
}

