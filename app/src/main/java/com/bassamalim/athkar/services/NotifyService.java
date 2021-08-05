package com.bassamalim.athkar.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.QuranView;
import com.bassamalim.athkar.R;

public class NotifyService extends IntentService {

    private static final int NOTIFICATION_ID = 3;
    private NotificationManagerCompat managerCompat;
    Notification notificationCompat;
    int prayer;
    NotificationChannel Athan;
    NotificationManager notificationManager;

    public NotifyService() {
        super("Notify");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager = getSystemService(NotificationManager.class);

        NotificationChannel notificationChannel = createNotificationChannel();

        prayer = intent.getIntExtra("prayer", 0);

        notificationCompat = getNotification(prayer);

        startForeground(NOTIFICATION_ID, notificationCompat);

        /*managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, notificationCompat);*/
    }

    public Notification getNotification(int prayer) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_athan);

        switch (prayer) {
            case 1: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("قوم صلي الفجر");
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

        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setContentIntent(onClick());
        builder.setAutoCancel(true);
        builder.setOngoing(true);

        return builder.build();
    }

    private PendingIntent onClick() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(MainActivity.getInstance(), QuranView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private NotificationChannel createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = Constants.CHANNEL_ID;
            CharSequence name = "Athan Channel";
            String description = "The channel that gives athan notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            Athan = new NotificationChannel(channelId, name, importance);
            Athan.setDescription(description);
            Athan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
        }
        return Athan;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean stopService(Intent name) {
        stopForeground(true);
        stopSelf();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent("com.android.techtrainner");
        intent.putExtra("yourvalue", "torestore");
        sendBroadcast(intent);
    }
}
