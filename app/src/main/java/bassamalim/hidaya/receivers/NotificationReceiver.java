package bassamalim.hidaya.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.AthkarViewer;
import bassamalim.hidaya.activities.QuranViewer;
import bassamalim.hidaya.activities.Splash;
import bassamalim.hidaya.enums.ID;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.services.AthanService;

public class NotificationReceiver extends BroadcastReceiver {

    private Context context;
    private ID id;
    private boolean isPrayer;
    private String channelId = "";
    private int type;
    private long time;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        id = Utils.mapID(intent.getIntExtra("id", 0));
        time = intent.getLongExtra("time", 0);
        isPrayer = intent.getAction().equals("prayer");

        Utils.onActivityCreateSetLocale(context);

        Log.i(Global.TAG, "in notification receiver for " + id);

        int defaultType = id == ID.SHOROUQ ? 0 : 2;

        type = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(id+"notification_type", defaultType);

        if (type != 0)
            prepare();
    }

    private void prepare() {
        long max = time + 120000;
        if (System.currentTimeMillis() <= max) {
            if (type == 3)
                startService();
            else
                showNotification();
        }
        else
            Log.i(Global.TAG, id + " Passed");
    }

    private void showNotification() {
        createNotificationChannel();

        if (id == ID.EVENING)    // so that night notification would replace morning notification
            id = ID.MORNING;

        if (isPrayer) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus                                   // Request permanent focus.
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }

        NotificationManagerCompat.from(context).notify(id.ordinal(), build());
    }

    private void startService() {
        Intent intent1 = new Intent(context, AthanService.class);
        intent1.setAction(Global.PLAY_ATHAN);
        intent1.putExtra("id", id);
        intent1.putExtra("time", time);

        if (Build.VERSION.SDK_INT >= 26)
            context.startForegroundService(intent1);
        else
            context.startService(intent1);
    }

    private Notification build() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setSmallIcon(R.drawable.ic_athan);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_athan);
        builder.setLargeIcon(icon);
        builder.setTicker(context.getResources().getString(R.string.app_name));

        int i = id.ordinal();
        if (id == ID.DUHR && Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
            i = 10;
        builder.setContentTitle(context.getResources().getStringArray(R.array.prayer_titles)[i]);
        builder.setContentText(context.getResources().getStringArray(R.array.prayer_subtitles)[i]);

        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);
        builder.setColor(context.getColor(R.color.surface_M));
        builder.setContentIntent(onClick(id));

        if (type == 1)
            builder.setSilent(true);

        return builder.build();
    }

    private PendingIntent onClick(ID id) {
        Intent intent;

        switch (id) {
            case MORNING:
                intent = new Intent(context, AthkarViewer.class);
                intent.putExtra("category", 0);
                intent.putExtra("thikr_id", 0);
                intent.putExtra("title", "أذكار الصباح");
                break;
            case EVENING:
                intent = new Intent(context, AthkarViewer.class);
                intent.putExtra("category", 0);
                intent.putExtra("thikr_id", 1);
                intent.putExtra("title", "أذكار المساء");
                break;
            case DAILY_WERD:
                intent = new Intent(context, QuranViewer.class);
                intent.setAction("random");
                break;
            case FRIDAY_KAHF:
                intent = new Intent(context, QuranViewer.class);
                intent.setAction("by_surah");
                intent.putExtra("surah_id", 17);    // alkahf
                break;
            default:
                intent = new Intent(context, Splash.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return PendingIntent.getActivity(context, id.ordinal(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name;
            String description = "";

            channelId = context.getResources().getStringArray(R.array.channel_ids)[id.ordinal()];
            name = context.getResources().getStringArray(R.array.reminders)[id.ordinal()];

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel  = new NotificationChannel(
                    channelId, name, importance);
            notificationChannel.setDescription(description);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
