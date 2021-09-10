package com.bassamalim.athkar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.services.NotificationService;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int id = intent.getIntExtra("id", 0);

        Log.i(Constants.TAG, "in notification receiver for " + id);
        Intent intent1 = new Intent(context, NotificationService.class);
        intent1.setAction(action);
        intent1.putExtra("id", id);
        intent1.putExtra("time", intent.getLongExtra("time", 0));
        context.startService(intent1);

        /*if (Build.VERSION.SDK_INT >= 26)
            context.startForegroundService(intent1);
        else
            context.startService(intent1);*/
    }

}
