package com.bassamalim.athkar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bassamalim.athkar.Constants;

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Constants.TAG, "in device boot receiver");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ||
                intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON")) {

            Intent setAlarms = new Intent(context, DailyUpdateReceiver.class);
            context.startActivity(setAlarms);
        }
    }

}
