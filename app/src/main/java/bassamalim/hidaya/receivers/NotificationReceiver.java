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

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.AlathkarActivity;
import bassamalim.hidaya.activities.QuranActivity;
import bassamalim.hidaya.activities.Splash;
import bassamalim.hidaya.other.Const;
import bassamalim.hidaya.enums.ID;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.services.AthanService;

import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {

    private Context context;
    private ID id;
    private boolean isPrayer;
    private String channelId = "";
    private int type;
    private long time;

    @Override
    public void onReceive(Context gContext, Intent intent) {
        context = gContext;
        id = Utils.mapID(intent.getIntExtra("id", 0));
        String action = intent.getAction();
        time = intent.getLongExtra("time", 0);
        isPrayer = action.equals("prayer");

        Log.i(Const.TAG, "in notification receiver for " + id);

        int defaultType = 2;
        if (id == ID.SHOROUQ)
            defaultType = 0;

        type = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(id+"notification_type", defaultType);

        if (type != 0)
            prepare();
    }

    private void prepare() {
        long max = time + 120000;
        if (System.currentTimeMillis() <= max) {
            if (type == 3 && isPrayer)
                startService();
            else
                showNotification();
        }
        else
            Log.e(Const.TAG, "Late intent walking");
    }

    private void showNotification() {
        createNotificationChannel();
        Notification notification = build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        if (id == ID.EVENING)    // so that night notification would replace morning notification
            id = ID.MORNING;

        if (isPrayer) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus                                   // Request permanent focus.
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }

        managerCompat.notify(id.ordinal(), notification);
    }

    private void startService() {
        Intent intent1 = new Intent(context, AthanService.class);
        intent1.setAction(Const.PLAY_ATHAN);
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

        switch (id) {
            case FAJR: {
                builder.setContentTitle("صلاة الفجر");
                builder.setContentText("حان موعد أذان الفجر");
                break;
            }
            case SHOROUQ: {
                builder.setContentTitle("الشروق");
                builder.setContentText("حان وقت الشروق");
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
            case MORNING: {
                builder.setContentTitle("أذكار الصباح");
                builder.setContentText("اضغط لقراءة أذكار الصباح");
                break;
            }
            case EVENING: {
                builder.setContentTitle("أذكار المساء");
                builder.setContentText("اضغط لقراءة أذكار المساء");
                break;
            }
            case DAILY_WERD: {
                builder.setContentTitle("الوِرد اليومي");
                builder.setContentText("اضغط لقراءة صفحة اليوم من المصحف");
                break;
            }
            case FRIDAY_KAHF: {
                builder.setContentTitle("سورة الكهف نور ما بين الجمعتين");
                builder.setContentText("اضغط لقراءة سورة الكهف");
                break;
            }
        }

        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            builder.setColor(context.getColor(R.color.click_M));
        else
            builder.setColor(context.getResources().getColor(R.color.click_M));
        builder.setContentIntent(onClick(id));

        if (type==1)
            builder.setSilent(true);

        return builder.build();
    }

    private PendingIntent onClick(ID id) {
        Intent intent;
        PendingIntent pendingIntent;

        switch (id) {
            case MORNING:
                intent = new Intent(context, AlathkarActivity.class);
                intent.putExtra("category", 0);
                intent.putExtra("thikr_id", 0);
                intent.putExtra("title", "أذكار الصباح");
                break;
            case EVENING:
                intent = new Intent(context, AlathkarActivity.class);
                intent.putExtra("category", 0);
                intent.putExtra("thikr_id", 1);
                intent.putExtra("title", "أذكار المساء");
                break;
            case DAILY_WERD:
                intent = new Intent(context, QuranActivity.class);
                intent.setAction("random");
                break;
            case FRIDAY_KAHF:
                intent = new Intent(context, QuranActivity.class);
                intent.setAction("by_surah");
                intent.putExtra("surah_id", 17);    // alkahf
                break;
            default:
                intent = new Intent(context, Splash.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, id.ordinal(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pendingIntent = PendingIntent.getActivity(context, id.ordinal(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
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
            else {
                switch (id) {
                    case SHOROUQ:
                        channelId = "sunrise";
                        name = "إشعار الشروق";
                        break;
                    case MORNING:
                    case EVENING:
                        channelId = "morning and night";
                        name = "إشعارات أذكار الصباح والمساء";
                        break;
                    case DAILY_WERD:
                        channelId = "daily_page";
                        name = "إشعار صفحة اليوم";
                        break;
                    case FRIDAY_KAHF:
                        channelId = "friday_kahf";
                        name = "إشعار سورة الكهف";
                        break;
                }
            }
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
