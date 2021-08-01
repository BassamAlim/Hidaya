package com.bassamalim.athkar;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyNewIntentService extends IntentService {

    private static final int NOTIFICATION_ID = 3;
    private NotificationCompat.Builder builder;
    private final String CHANNEL_ID = "channel id";

    public MyNewIntentService() {
        super("MyNewIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        builder = new NotificationCompat.Builder(MainActivity.getInstance(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_athan);
        builder.setContentTitle("blah");
        builder.setContentText("blah blah");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        /*// Create an explicit intent for an Activity in your app
        Intent intent = new Intent(MainActivity.getInstance(), QuranView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);*/

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //to be able to launch your activity from the notification
        builder.setContentIntent(pendingIntent);

        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, notificationCompat);
    }

}
