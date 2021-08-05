package com.bassamalim.athkar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.bassamalim.athkar.services.NotificationService;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Intent intent1 = new Intent(context, NotifyService.class);
        Intent intent1 = new Intent(context, NotificationService.class);
        intent1.putExtra("prayer", intent.getIntExtra("prayer", 0));
        intent1.putExtra("time", (Parcelable) intent.getParcelableExtra("time"));

        context.startService(intent1);

        /*if (Build.VERSION.SDK_INT >= 26)
            context.startForegroundService(intent1);
        else
            context.startService(intent1);*/
    }

}
