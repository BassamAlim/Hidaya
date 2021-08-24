package com.bassamalim.athkar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // on device boot complete, reset the alarm
            Intent setAlarms = new Intent(context, DailyUpdateReceiver.class);
            context.startActivity(setAlarms);
        }
    }

}
