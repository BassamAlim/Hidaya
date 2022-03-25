package bassamalim.hidaya.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.Splash;
import bassamalim.hidaya.other.Const;
import bassamalim.hidaya.enums.ID;

import java.util.Calendar;

public class AthanService extends Service {

    private ID id;
    private String channelId = "";
    private MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;

        id = (ID) intent.getSerializableExtra("id");
        Log.i(Const.TAG, "In athan service for " + id);
        //int Notification_ID = (int) System.currentTimeMillis() % 10000;

        createNotificationChannel();
        startForeground(id.ordinal()+1, build());

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Request audio focus                                   // Request permanent focus.
        am.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN);

        play();

        return START_NOT_STICKY;
    }

    private Notification build() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_athan);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_athan);
        builder.setLargeIcon(icon);
        builder.setTicker(getResources().getString(R.string.app_name));
        switch (id) {
            case FAJR: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("حان موعد أذان الفجر");
                break;
            }
            case DUHR: {
                if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                    builder.setContentTitle("صلاة الجمعة");
                    builder.setContentText("حان موعد الأذان الثاني لصلاة الجمعة");
                }
                else {
                    builder.setContentTitle("صلاة الظهر");
                    builder.setContentText("حان موعد أذان الظهر");
                }
                break;
            }
            case ASR: {
                builder.setContentTitle("صلاة العصر");
                builder.setContentText("حان موعد أذان العصر");
                break;
            }
            case MAGHRIB: {
                builder.setContentTitle("صلاة المغرب");
                builder.setContentText("حان موعد أذان المغرب");
                break;
            }
            case ISHAA: {
                builder.setContentTitle("صلاة العشاء");
                builder.setContentText("حان موعد أذان العشاء");
                break;
            }
        }
        builder.addAction(0, "إيقاف الأذان", getStopIntent());
        builder.setContentIntent(getStopAndOpenIntent());
        builder.setDeleteIntent(getStopIntent());
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);
        builder.setColor(getColor(R.color.click_M));

        return builder.build();
    }

    private PendingIntent getStopIntent() {
        return PendingIntent.getService(this, 11, new Intent(
                        this, AthanService.class).setAction(Const.STOP_ATHAN)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent getStopAndOpenIntent() {
        return PendingIntent.getActivity(this, 12, new Intent(
                        this, Splash.class).setAction(Const.STOP_ATHAN),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name;
            String description = "";

            channelId = "Athan";
            name = "إشعارات الصلوات";

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel  = new NotificationChannel(
                    channelId, name, importance);
            notificationChannel.setDescription(description);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void play() {
        Log.i(Const.TAG, "Playing Athan");
        mediaPlayer = MediaPlayer.create(this, R.raw.athan1);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA).build()
        );
        mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        mediaPlayer.setOnCompletionListener(mp -> stopMyService());
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopMyService() {
        stopForeground(true);
        stopSelf();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMyService();
    }
}
