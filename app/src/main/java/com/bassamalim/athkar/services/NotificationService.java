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
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.Splash;
import com.bassamalim.athkar.views.AlathkarView;
import com.bassamalim.athkar.views.QuranView;

public class NotificationService extends Service {

    private boolean isPrayer;
    private String channelId = "";
    private int id;
    private int type;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            long max = intent.getLongExtra("time", 0) + 120000;
            if (System.currentTimeMillis() <= max) {
                isPrayer = intent.getAction().equals("prayer");
                id = intent.getIntExtra("id", 10);
                type = PreferenceManager.getDefaultSharedPreferences(this)
                        .getInt(id+"notification_type", 2);

                Log.i(Constants.TAG, "in notification service for " + id);

                createNotificationChannel();
                Notification notification = build();

                if (isPrayer) {
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    // Request audio focus                                   // Request permanent focus.
                    am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                }

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
                managerCompat.notify(id, notification);

                //startForeground(NOTIFICATION_ID, notification);
            }
            else
                Log.e(Constants.TAG, "Late intent walking");
        }
        else
            Log.e(Constants.TAG, "Dead intent walking");

        stopSelf();
        return START_REDELIVER_INTENT;
    }

    private Notification build() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_athan);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_athan);
        builder.setLargeIcon(icon);
        builder.setTicker(getResources().getString(R.string.app_name));

        switch (id) {
            case 0: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("حان موعد أذان الفجر");
                break;
            }
            case 1: {
                builder.setContentTitle("الشروق");
                builder.setContentText("حان وقت الشروق");
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
                builder.setContentTitle("صلاة المغرب");
                builder.setContentText("حان موعد أذان المغرب");
                break;
            }
            case 5: {
                builder.setContentTitle("صلاة العشاء");
                builder.setContentText("حان موعد أذان العشاء");
                break;
            }
            case 6: {
                builder.setContentTitle("أذكار الصباح");
                builder.setContentText("اضغط لقراءة أذكار الصباح");
                break;
            }
            case 7: {
                builder.setContentTitle("أذكار المساء");
                builder.setContentText("اضغط لقراءة أذكار المساء");
                break;
            }
            case 8: {
                builder.setContentTitle("صفحة اليوم");
                builder.setContentText("اضغط لقراءة صفحة اليوم من المصحف");
                break;
            }
            case 9: {
                builder.setContentTitle("جمعة مباركة");
                builder.setContentText("اضغط لقراءة سورة الكهف");
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
        builder.setContentIntent(onClick(id));

        if (type==1)
            builder.setSilent(true);

        //builder.setOngoing(true);
        //builder.setDeleteIntent(contentPendingIntent)  // if needed

        // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        //notification.flags = Notification.FLAG_NO_CLEAR;
        return builder.build();
    }

    private PendingIntent onClick(int variable) {
        Intent intent;
        PendingIntent pendingIntent;

        if (variable == 6) {
            intent = new Intent(this, AlathkarView.class);
            intent.putExtra("thikrs", getResources().getStringArray(R.array.morning));
            intent.putExtra("title", "أذكار الصباح");
        }
        else if (variable == 7) {
            intent = new Intent(this, AlathkarView.class);
            intent.putExtra("thikrs", getResources().getStringArray(R.array.night));
            intent.putExtra("title", "أذكار المساء");
        }
        else if (variable == 8) {
            intent = new Intent(this, QuranView.class);
            intent.setAction("random");
        }
        else if (variable == 9) {
            intent = new Intent(this, QuranView.class);
            intent.setAction("specific");
            intent.putExtra("surah_index", 17);    // alkahf
        }
        else
            intent = new Intent(this, Splash.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this,
                    variable, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pendingIntent = PendingIntent.getActivity(this,
                    variable, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return pendingIntent;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "";
            String description = "";
            if (isPrayer) {
                channelId = "Athan";
                name = "إشعارات الصلوات";
            }
            else if (id == 1) {
                channelId = "sunrise";
                name = "إشعار الشروق";
            }
            else if (id == 6 || id == 7) {
                channelId = "morning and night";
                name = "إشعارات أذكار الصباح والمساء";
            }
            else if (id == 8) {
                channelId = "daily_page";
                name = "إشعار صفحة اليوم";
            }
            else if (id == 9) {
                channelId = "friday_kahf";
                name = "إشعار سورة الكهف";
            }
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel  = new NotificationChannel(
                    channelId, name, importance);
            notificationChannel.setDescription(description);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
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
    }
}

