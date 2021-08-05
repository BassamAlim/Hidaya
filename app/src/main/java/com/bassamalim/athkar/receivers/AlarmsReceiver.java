package com.bassamalim.athkar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.bassamalim.athkar.services.NotificationService;

public class AlarmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, NotificationService.class);
        intent1.putExtra("prayer", intent.getIntExtra("prayer", 0));
        context.startService(intent1);
    }

}
