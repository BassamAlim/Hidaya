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

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bassamalim.athkar.QuranView;
import com.bassamalim.athkar.R;

import java.util.Calendar;

public class NotificationService extends Service {

    public static boolean isServiceRunning = false;
    private static final int NOTIFICATION_ID = 3;
    private String channelId;
    private NotificationManagerCompat managerCompat;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";

        int prayer = intent.getIntExtra("prayer", 0);

        Notification notification = buildNotification(prayer);

        //startForeground(NOTIFICATION_ID, notification);

        managerCompat = NotificationManagerCompat.from(this);

        managerCompat.notify(NOTIFICATION_ID, notification);

        return START_NOT_STICKY;
    }


    public Notification buildNotification(int prayer) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channelId");
        builder.setSmallIcon(R.drawable.ic_athan);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_athan);
        builder.setLargeIcon(icon);
        builder.setTicker(getResources().getString(R.string.app_name));

        switch (prayer) {
            case 0: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("قوم صلي الفجر");
                break;
            }
            /*case 1: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("قوم صلي الفجر");
                break;
            }*/
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
            /*case 4: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("قوم صلي الفجر");
                break;
            }*/
            case 5: {
                builder.setContentTitle("صلاة المغرب");
                builder.setContentText("قوم صلي المغرب");
                break;
            }
            case 6: {
                builder.setContentTitle("صلاة العشاء");
                builder.setContentText("قوم صلي العشاء");
                break;
            }
        }
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setContentIntent(onClick());
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);
        //builder.setOngoing(true);
        //builder.setDeleteIntent(contentPendingIntent)  // if needed

        Notification notification = builder.build();

        // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        //notification.flags = Notification.FLAG_NO_CLEAR;

        return notification;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel Athan;
            channelId = "channelId";
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

    private PendingIntent onClick() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, QuranView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    void stopMyService() {
        stopForeground(true);
        stopSelf();
        isServiceRunning = false;
    }

    // In case the service is deleted or crashes some how
    @Override
    public void onDestroy() {
        isServiceRunning = false;
        super.onDestroy();
    }
}

