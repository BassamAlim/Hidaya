package com.bassamalim.athkar.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.Splash;
import com.bassamalim.athkar.views.AlathkarView;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.views.QuranView;

public class NotificationService extends Service {

    public static boolean isOn = false;
    private boolean isPrayer;
    private String channelId;
    private int id;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long max = intent.getLongExtra("time", 0) + 60000;
        if (System.currentTimeMillis() <= max) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    createNotificationChannel(notificationManager) : "channel";

            isPrayer = intent.getAction().equals("prayer");
            id = intent.getIntExtra("id", 0);

            Log.i(Constants.TAG, "in notification service for " + id);

            Notification notification = build(id);

            if (isPrayer) {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                // Request audio focus                                   // Request permanent focus.
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }

            //startForeground(NOTIFICATION_ID, notification);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(id, notification);
        }
        else
            Log.i(Constants.TAG, "dead intent walking");

        stopSelf();
        return START_REDELIVER_INTENT;
    }

    private Notification build(int variable) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        builder.setSmallIcon(R.drawable.ic_athan);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_athan);
        builder.setLargeIcon(icon);
        builder.setTicker(getResources().getString(R.string.app_name));

        switch (variable) {
            case 1: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("حان موعد أذان الفجر");
                break;
            }
            case 3: {
                builder.setContentTitle("صلاة الظهر");
                builder.setContentText("حان موعد أذان الظهر");
                break;
            }
            case 4: {
                builder.setContentTitle("صلاة العصر");
                builder.setContentText("حان موعد أذان العصر");
                break;
            }

            case 6: {
                builder.setContentTitle("صلاة المغرب");
                builder.setContentText("حان موعد أذان المغرب");
                break;
            }
            case 7: {
                builder.setContentTitle("صلاة العشاء");
                builder.setContentText("حان موعد أذان العشاء");
                break;
            }
            case 8: {
                builder.setContentTitle("أذكار الصباح");
                builder.setContentText("اضغط لقراءة أذكار الصباح");
                break;
            }
            case 9: {
                builder.setContentTitle("أذكار المساء");
                builder.setContentText("اضغط لقراءة أذكار المساء");
                break;
            }
            case 10: {
                builder.setContentTitle("صفحة اليوم");
                builder.setContentText("اضغط لقراءة صفحة اليوم من المصحف");
                break;
            }
        }
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            builder.setColor(getColor(R.color.secondary));
        else
            builder.setColor(getResources().getColor(R.color.secondary));
        builder.setContentIntent(onClick(variable));
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
            NotificationChannel notificationChannel;
            CharSequence name = "";
            if (isPrayer) {
                channelId = "Athan";
                name = "إشعارات الصلوات";
            }
            else if (id == 8 || id == 9) {
                channelId = "morning_and_night";
                name = "أذكار الصباح والمساء";
            }
            else if (id == 10) {
                channelId = "daily_page";
                name = "إشعار صفحة اليوم";
            }
            int importance = NotificationManager.IMPORTANCE_HIGH;
            notificationChannel = new NotificationChannel(channelId, name, importance);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            //notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return channelId;
    }

    private PendingIntent onClick(int variable) {
        Intent intent;
        PendingIntent pendingIntent;

        if (variable == 8) {
            intent = new Intent(this, AlathkarView.class);
            intent.putExtra("thikrs", getResources().getStringArray(R.array.morning));
            intent.putExtra("title", "أذكار الصباح");
        }
        else if (variable == 9) {
            intent = new Intent(this, AlathkarView.class);
            intent.putExtra("thikrs", getResources().getStringArray(R.array.night));
            intent.putExtra("title", "أذكار المساء");
        }
        else if (variable == 10) {
            intent = new Intent(this, QuranView.class);
            intent.setAction("random");
            intent.putExtra("title", "صفحة اليوم");
        }
        else
            intent = new Intent(this, Splash.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this,
                variable, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    /*public void stopMyService() {
        stopForeground(true);
        stopSelf();
        isServiceRunning = false;
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        isOn = false;
    }
}

